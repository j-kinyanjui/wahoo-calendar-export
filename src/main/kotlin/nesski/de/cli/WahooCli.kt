package nesski.de.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.terminal.StringPrompt
import com.github.ajalt.mordant.terminal.YesNoPrompt
import nesski.de.config.AppConfig
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

        // 5. Skeleton output — full orchestration wired in Plan 02
        echo("Fetching plans from ${dateRange.start} to ${dateRange.end}")

        // TODO: Wire auth, fetch, display in Plan 02
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
