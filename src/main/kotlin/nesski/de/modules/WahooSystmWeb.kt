package nesski.de.modules

import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.runBlocking
import nesski.de.plugins.TokenStorage
import nesski.de.plugins.wahooHttpClient
import nesski.de.services.web.SystmAuthService
import kotlin.system.exitProcess

private val logger = KtorSimpleLogger("WahooSystmWeb")

/**
 * Configure Systm authentication with auto-login, logout, and status routes
 */
fun Application.wahooSystmWeb(httpClient: HttpClient = wahooHttpClient) {

    /**
     * Load Systm credentials from application environment config
     */
    fun loadCredentials(): Pair<String, String> {
        val username = environment.config.property("systm.username")
        val password = environment.config.property("systm.password")
        return Pair(username.getString(), password.getString())
    }

    val credentials = loadCredentials()
    val authService = SystmAuthService(httpClient, credentials.first, credentials.second)

    val token = try {
        runBlocking { authService.login() }
    } catch (e: Exception) {
        logger.error("Login failed with exception: ${e.message}")
        null
    }

    if (token == null) {
        logger.error("Could not authenticate with Wahoo — exiting process.")
        exitProcess(1)
    }

    TokenStorage.token = token
    logger.info("Wahoo authentication successful")
}
