package nesski.de.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.terminal.StringPrompt
import com.github.ajalt.mordant.terminal.YesNoPrompt
import kotlinx.coroutines.runBlocking
import nesski.de.config.AppConfig
import nesski.de.plugins.TokenStorage
import nesski.de.plugins.wahooHttpClient
import nesski.de.services.web.GraphQLException
import nesski.de.services.web.SystmAuthService
import nesski.de.services.web.SystmPlansService
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
        .default("~/.config/wahoo-cli/config.toml")

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
            val plans = try {
                val plansService = SystmPlansService(wahooHttpClient, TokenStorage.token)
                plansService.fetchPlans(dateRange.start, dateRange.end)
            } catch (e: GraphQLException) {
                echo("API error: ${e.message}")
                throw ProgramResult(1)
            } catch (e: Exception) {
                echo("Failed to fetch plans: ${e.message}")
                throw ProgramResult(1)
            }

            // 7. Display plans in console
            echo("\nTraining Plans (${dateRange.start} to ${dateRange.end}):\n")

            if (plans.isEmpty()) {
                echo("No plans found for this date range.")
                return@runBlocking
            }

            var totalWorkouts = 0
            for (plan in plans) {
                echo("\uD83D\uDCCB ${plan.name} (${plan.status ?: "scheduled"})")
                for (workout in plan.workouts) {
                    echo("  ${workout.name} \u2014 ${workout.scheduledDate ?: "no date"} [${workout.type ?: "unknown"}] [${workout.status ?: "planned"}]")
                    totalWorkouts++
                }
            }

            echo("\n$totalWorkouts workout(s) across ${plans.size} plan(s)\n")
        }
    }
}
