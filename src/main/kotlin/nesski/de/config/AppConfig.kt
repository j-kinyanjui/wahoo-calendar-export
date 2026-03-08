package nesski.de.config

import com.akuleshov7.ktoml.file.TomlFileReader
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger("AppConfig")

@Serializable
data class AppConfig(
    val credentials: CredentialsConfig = CredentialsConfig(),
    val output: OutputConfig = OutputConfig()
) {
    companion object {
        /**
         * Load config from a TOML file at the given path.
         * If the file doesn't exist or can't be parsed, returns defaults.
         */
        fun load(configPath: String): AppConfig {
            val file = File(configPath)
            if (!file.exists()) {
                logger.info("Config file not found at $configPath, using defaults")
                return AppConfig()
            }

            return try {
                TomlFileReader.decodeFromFile<AppConfig>(
                    serializer(),
                    configPath
                )
            } catch (e: Exception) {
                logger.warn("Failed to parse config file at $configPath: ${e.message}, using defaults")
                AppConfig()
            }
        }

        /**
         * Resolve credentials with env var overrides.
         * Env vars SYSTM_USER and SYSTM_PASSWORD take precedence over config file values.
         */
        fun resolveCredentials(config: AppConfig): Pair<String, String> {
            val username = System.getenv("SYSTM_USER") ?: config.credentials.username
            val password = System.getenv("SYSTM_PASSWORD") ?: config.credentials.password
            return Pair(username, password)
        }
    }
}

@Serializable
data class CredentialsConfig(
    val username: String = "",
    val password: String = ""
)

@Serializable
data class OutputConfig(
    @SerialName("ics_save_path")
    val icsSavePath: String = "."
)
