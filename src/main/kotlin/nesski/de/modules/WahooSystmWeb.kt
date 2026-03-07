package nesski.de.modules

import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlinx.serialization.Serializable
import nesski.de.plugins.applicationHttpClient
import nesski.de.services.web.SystmAuthService
import nesski.de.services.web.logger

internal val logger: Logger = KtorSimpleLogger("SystmAuthentication")

/**
 * Auth status response
 */
@Serializable
data class SystmAuthStatus(
    val status: String,
    val loggedIn: Boolean,
    val username: String? = null,
    val message: String? = null
)

/**
 * Configure Systm authentication with auto-login, logout, and status routes
 */
fun Application.wahooSystmWeb(httpClient: HttpClient = applicationHttpClient) {
    install(Sessions) {
        cookie<SystmSession>("systm_session")
    }

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
    authService.login()


    routing {
        /**
         * GET /systm-logout - clears session and redirects to error
         * Does NOT auto-relogin (config-based auth, unlike OAuth)
         */
        get("/systm-logout") {
            call.sessions.clear<SystmSession>()
            logger.info("User logged out from Systm")

            // Redirect to error page with logout reason
            val redirectUrl = call.request.queryParameters["redirectUrl"]
            if (redirectUrl != null) {
                call.respondRedirect("$redirectUrl?reason=logged_out")
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    SystmAuthStatus(
                        status = "logged_out",
                        loggedIn = false,
                        message = "Successfully logged out. Restart app to re-authenticate with config credentials."
                    )
                )
            }
        }

        /**
         * POST /systm-login - manual login trigger (optional, for testing)
         */
        get("/systm-login") {
            val session: SystmSession? = call.sessions.get()

            if (session != null && !session.isExpired()) {
                // Already logged in
                call.respondRedirect("/systm-status")
                return@get
            }

            // Try to login with config credentials
            if (authService != null) {
                logger.info("Manual login triggered, attempting to authenticate...")
                val loginResult = authService.login()
                if (loginResult != null) {
                    val (token, claims) = loginResult
                    val systmSession = SystmSession.create(token)
                    call.sessions.set(systmSession)
                    logger.info("Successfully authenticated as: ${claims.username}")

                    val redirectUrl = call.request.queryParameters["redirectUrl"]
                    if (redirectUrl != null) {
                        call.respondRedirect(redirectUrl)
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            SystmAuthStatus(
                                status = "authenticated",
                                loggedIn = true,
                                username = claims.username,
                                message = "Successfully logged in"
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        SystmAuthStatus(
                            status = "login_failed",
                            loggedIn = false,
                            message = "Login failed. Check credentials in config/systm.yaml"
                        )
                    )
                }
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SystmAuthStatus(
                        status = "not_configured",
                        loggedIn = false,
                        message = "No credentials configured. Add username and password to config/systm.yaml"
                    )
                )
            }
        }
    }
}
