package nesski.de.cli

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.runBlocking
import nesski.de.config.AppConfig
import nesski.de.config.TokenStorage
import nesski.de.config.wahooClient
import nesski.de.email.EmailService
import nesski.de.ics.IcsBuilder
import nesski.de.ics.IcsFileWriter
import nesski.de.models.UserPlanItem
import nesski.de.utils.DateRange
import nesski.de.utils.parseDateRange
import nesski.de.wahoo.PlansService
import nesski.de.wahoo.SystmAuthService
import java.io.File
import java.time.LocalDate
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class WahooCli : CliktCommand(
    name = "wahoo-cli"
) {
    override fun help(context: Context): String =
        "Fetch Wahoo SYSTM training plans and export as .ics"

    private val configFile by option("--config", "-c", help = "Config file path")
        .file(canBeFile = true, canBeDir = false)
        .defaultLazy { defaultConfigFile() }

    private val range by option("--range", "-r", help = "Time range shorthand: now, 1w, 2w, 1m, 2m. Default: 2w")
        .convert { input ->
            try {
                Range.parse(input)
            } catch (e: IllegalArgumentException) {
                fail(e.message ?: "Invalid date range")
            }
        }
    private val from by option("--from", help = "Start date (YYYY-MM-DD)")
        .convert { input ->
            parseDate(input) ?: fail("Invalid date")
        }
    private val to by option("--to", help = "End date (YYYY-MM-DD)")
        .convert { input ->
            parseDate(input) ?: fail("Invalid date")
        }
    private val outputFile by option("--out", "-o", help = "Output file location.")
        .file(canBeFile = true, canBeDir = false)

    override fun run() {
        val config = AppConfig.load(configFile)

        val dateRange = try {
            parseDateRange(range, from, to)
        } catch (e: IllegalArgumentException) {
            throw UsageError(e.message ?: "Invalid date range options")
        }

        TokenStorage.token = runBlocking {
            runCatching {
                SystmAuthService(wahooClient, config.resolvedCredentials()).login()
            }
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                echo("Authentication failed: ${e.message}")
                throw ProgramResult(1)
            }
        )

        val items = runBlocking {
            runCatching {
                PlansService(wahooClient, TokenStorage.token)
                    .fetchPlans(dateRange.start, dateRange.end)
            }
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                echo("API error: ${e.message}")
                throw ProgramResult(1)
            }
        )

        informUser(items, dateRange)

        runCatching {
            if (items.isEmpty()) {
                echo("No plans found for this date range.")
                Abort()
            }
            IcsBuilder.build(items)
        }.getOrElse { e ->
            echo("Encountered an exception building the ics file: ${e.message}")
            throw ProgramResult(1)
        }.let { result ->
            echo("ICS export: ${result.exportedCount} workouts exported, ${result.skippedCount} skipped")

            if (result.exportedCount == 0) {
                echo("No workouts to export.")
                ProgramResult(0)
            }

            val rangeLabel = range ?: "${dateRange.start}_${dateRange.end}"
            val filename = "workouts_${rangeLabel}_${LocalDate.now()}.ics"

            if (config.email.enabled) {
                val emailResult = EmailService.send(
                    config = config.email,
                    icsContent = result.icsContent,
                    filename = filename
                )

                if (emailResult.success) {
                    echo("Email sent successfully with $filename attached")
                } else {
                    echo("Email failed: ${emailResult.errorMessage}")
                    // Fallback: save to disk
                    saveIcsToDisk(
                        outputFile ?: File(config.output.icsSavePath, filename),
                        result.icsContent)
                }
            } else {
                // No email configured — save to disk directly
                saveIcsToDisk(
                    outputFile ?: File(config.output.icsSavePath, filename),
                    result.icsContent)
            }

            // Report skipped items
            if (result.skippedReasons.isNotEmpty()) {
                echo("\nSkipped items:")
                result.skippedReasons.forEach { echo("  - $it") }
            }
        }
    }

    /**
     * Save .ics content to disk at the configured path.
     * Auto-creates directories if they don't exist.
     *
     * @return The absolute path of the saved file
     */
    private fun saveIcsToDisk(file: File, icsContent: String): String {
        val savedPath = IcsFileWriter.write(file, icsContent)
        echo("ICS file saved to: $savedPath")
        return savedPath
    }

    private fun informUser(items: List<UserPlanItem>, dateRange: DateRange) {
        if (items.isEmpty()) {
            echo("No plans found for this date range.")
        }

        echo("\nWorkouts for period (${dateRange.start} to ${dateRange.end}):\n")
        // Group items by plan name for a nicer display
        val byPlan = items.groupBy { it.plan?.name ?: "Unassigned" }

        var totalItems = 0
        for ((planName, planItems) in byPlan) {
            val planInfo = planItems.firstNotNullOfOrNull { it.plan }
            planInfo?.level
                ?.let { echo("$planName [$it]")  }
                ?:  echo(planName)

            for (item in planItems.sortedBy { it.plannedDate }) {
                totalItems++
                val workoutName = item.prospects?.firstOrNull()?.name ?: item.type ?: "unknown"
                val date = formatPlannedDate(item.plannedDate)
                val duration = formatDuration(item.prospects?.firstOrNull()?.plannedDuration)
                val status = item.status ?: "planned"
                val prospect = item.prospects?.firstOrNull()
                val style = (prospect?.style ?: prospect?.type ?: item.type)?.let { " ($it)" } ?: ""

                echo("  $date  $workoutName$style  $duration  [$status]")
            }
            echo("")
        }

        echo("$totalItems workout(s) across ${byPlan.size} plan(s)\n")
    }

    /** Extract just the date portion from an ISO-8601 datetime string. */
    private fun formatPlannedDate(isoDateTime: String?): String {
        if (isoDateTime == null) return "no date   "
        // The API returns strings like "2026-03-10T00:00:00.000Z"
        return isoDateTime.substringBefore("T").padEnd(10)
    }

    /** Format a duration (in hours, fractional) into a human-readable string. */
    private fun formatDuration(hours: Double?): String {
        return hours?.toDuration(DurationUnit.HOURS)?.absoluteValue?.toString().orEmpty()
    }
}
