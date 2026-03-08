package nesski.de.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.runBlocking
import nesski.de.config.AppConfig
import nesski.de.plugins.TokenStorage
import nesski.de.plugins.wahooHttpClient
import nesski.de.services.web.GraphQLException
import nesski.de.services.web.PlansService
import nesski.de.services.web.SystmAuthService
import nesski.de.utils.parseDateRange

class WahooCli : CliktCommand(
    name = "wahoo-cli"
) {
    override fun help(context: Context): String =
        "Fetch Wahoo SYSTM training plans and display them"

    private val range by option("--range", "-r", help = "Time range shorthand: now, 1w, 2w, 1m, 2m")
    private val from by option("--from", help = "Start date (YYYY-MM-DD)")
    private val to by option("--to", help = "End date (YYYY-MM-DD)")
    private val configPath by option("--config", "-c", help = "Config file path")
        //.default("~/.config/wahoo-cli/config.toml")
        .default("src/main/resources/config.toml")

    override fun run() {
        val resolvedConfigPath = configPath.replaceFirst("~", System.getProperty("user.home"))
        val config = AppConfig.load(resolvedConfigPath)

        val (username, password) = AppConfig.resolveCredentials(config)
        if (username.isBlank() || password.isBlank()) {
            throw UsageError("Credentials must not be empty")
        }

        val dateRange = try {
            parseDateRange(range, from, to)
        } catch (e: IllegalArgumentException) {
            throw UsageError(e.message ?: "Invalid date range options")
        }

        TokenStorage.token = runBlocking {
            try {
                val result = SystmAuthService(wahooHttpClient, username, password).login()
                if (result == null) {
                    echo("Authentication failed: no token received")
                    throw ProgramResult(1)
                }
                echo("Authenticated as $username")
                result
            } catch (e: ProgramResult) {
                throw e
            } catch (e: Exception) {
                echo("Authentication failed: ${e.message}")
                throw ProgramResult(1)
            }
        }

        runBlocking {
            val items = try {
                val plansService = PlansService(wahooHttpClient, TokenStorage.token)
                plansService.fetchPlans(dateRange.start, dateRange.end)
            } catch (e: GraphQLException) {
                echo("API error: ${e.message}")
                throw ProgramResult(1)
            } catch (e: Exception) {
                echo("Failed to fetch plans: ${e.message}")
                throw ProgramResult(1)
            }

            if (items.isEmpty()) {
                echo("No plans found for this date range.")
                return@runBlocking
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
