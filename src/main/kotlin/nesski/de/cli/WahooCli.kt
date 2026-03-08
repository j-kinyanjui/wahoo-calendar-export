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
        .default("~/.config/wahoo-cli/config")

    override fun run() {
        // 1. Resolve config path (expand ~ to user home)
        val resolvedConfigPath = configPath.replaceFirst("~", System.getProperty("user.home"))

        // 2. Load config from TOML file
        val config = AppConfig.load(resolvedConfigPath)

        // 3. Resolve credentials: env vars override config values
        val (username, password) = AppConfig.resolveCredentials(config)

        // If no credentials found anywhere, prompt interactively
        if (username.isBlank() || password.isBlank()) {
            promptForCredentials()
            return
        }

        // 4. Parse date range (mutual exclusion validated inside)
        val dateRange = try {
            parseDateRange(range, from, to)
        } catch (e: IllegalArgumentException) {
            throw UsageError(e.message ?: "Invalid date range options")
        }

        runBlocking {
            // 5. Authenticate with SYSTM API
            val token: String
            try {
                val authService = SystmAuthService(wahooHttpClient, username, password)
                val result = authService.login()
                if (result == null) {
                    echo("\u2717 Authentication failed: no token received")
                    throw ProgramResult(1)
                }
                token = result
                echo("\u2713 Authenticated as $username")
            } catch (e: ProgramResult) {
                throw e
            } catch (e: Exception) {
                echo("\u2717 Authentication failed: ${e.message}")
                throw ProgramResult(1)
            }

            // 6. Fetch plans for date range
            val plans = try {
                val plansService = SystmPlansService(wahooHttpClient)
                plansService.fetchPlans(token, dateRange.start, dateRange.end)
            } catch (e: GraphQLException) {
                echo("\u2717 API error: ${e.message}")
                throw ProgramResult(1)
            } catch (e: Exception) {
                echo("\u2717 Failed to fetch plans: ${e.message}")
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

    /**
     * Prompt user for credentials when none found in env vars or config.
     * Offers to save credentials to the config file.
     */
    private fun promptForCredentials() {
        echo("No credentials found. Please enter your SYSTM credentials.")
        val t = terminal
        val user = StringPrompt("SYSTM username (email)", t).ask()
            ?: throw UsageError("Username is required")
        val pass = StringPrompt("SYSTM password", t, hideInput = true).ask()
            ?: throw UsageError("Password is required")

        val save = YesNoPrompt("Save credentials to config file?", t).ask() ?: false
        if (save) {
            echo("Note: Credentials will be stored in plain text. Consider running: chmod 600 ${configPath.replaceFirst("~", System.getProperty("user.home"))}")
            // TODO: Write credentials to config file
        }

        echo("Credentials received. Re-run the command to fetch plans.")
    }
}
