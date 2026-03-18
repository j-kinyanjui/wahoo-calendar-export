package nesski.de.config

import com.akuleshov7.ktoml.file.TomlFileReader
import java.io.File
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val credentials: CredentialsConfig,
    val output: OutputConfig,
    val email: EmailConfig = EmailConfig(),
) {
    companion object {
        /**
         * Load config from a TOML file at the given path. If the file doesn't exist or can't be
         * parsed, returns defaults.
         */
        fun load(config: File): AppConfig {
            return try {
                TomlFileReader.decodeFromFile(serializer(), config.path)
            } catch (e: Exception) {
                error("Failed to parse config file at ${config.path}: ${e.message}")
            }
        }
    }

    /**
     * Resolve credentials with env var overrides. Env vars SYSTM_USER and SYSTM_PASSWORD take
     * precedence over config file values.
     */
    fun resolvedCredentials(): CredentialsConfig {
        val username = System.getenv("SYSTM_USER") ?: credentials.username
        val password = System.getenv("SYSTM_PASSWORD") ?: credentials.password
        return CredentialsConfig(username, password)
    }
}

@Serializable data class CredentialsConfig(val username: String, val password: String)

@Serializable data class OutputConfig(@SerialName("ics_save_path") val icsSavePath: String)

@Serializable
data class EmailConfig(
    val enabled: Boolean = false,
    @SerialName("smtp_host") val smtpHost: String = "",
    @SerialName("smtp_port") val smtpPort: Int = 587,
    @SerialName("smtp_username") val smtpUsername: String = "",
    @SerialName("smtp_password") val smtpPassword: String = "",
    @SerialName("from_address") val fromAddress: String = "",
    @SerialName("to_address") val toAddress: String = "",
    val subject: String = "Wahoo SYSTM Workout Plan",
    @SerialName("use_tls") val useTls: Boolean = true,
)
