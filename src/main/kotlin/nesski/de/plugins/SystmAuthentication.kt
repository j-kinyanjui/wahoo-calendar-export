package nesski.de.plugins

import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.set
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.Serializable
import nesski.de.models.SystmSession
import nesski.de.services.SystmAuthService

internal val log = KtorSimpleLogger("SystmAuthentication")

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
fun Application.configureSystmAuthentication(httpClient: HttpClient = applicationHttpClient) {
    install(Sessions) {
        cookie<SystmSession>("systm_session")
    }

    // Try to load credentials and auto-login on startup
    val credentials = SystmAuthService.loadCredentials(this)
    val authService = if (credentials != null) {
        SystmAuthService(httpClient, credentials.first, credentials.second)
    } else {
        null
    }

    // Auto-authenticate on startup if credentials are available
    if (authService != null) {
        log.info("Attempting auto-login on startup with configured credentials")
        // Note: Auto-login will be performed on first request via interceptors
        // or can be triggered here by calling authService.login()
    }

    routing {
        /**
         * GET /systm-logout - clears session and redirects to error
         * Does NOT auto-relogin (config-based auth, unlike OAuth)
         */
        get("/systm-logout") {
            call.sessions.clear<SystmSession>()
            log.info("User logged out from Systm")

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
         * GET /systm-status - returns auth status
         */
        get("/systm-status") {
            val session: SystmSession? = call.sessions.get()

            if (session != null) {
                if (session.isExpired()) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        SystmAuthStatus(
                            status = "expired",
                            loggedIn = false,
                            message = "Session has expired. Restart app to re-authenticate."
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        SystmAuthStatus(
                            status = "authenticated",
                            loggedIn = true,
                            message = "Active session"
                        )
                    )
                }
            } else {
                val hasCredentials = credentials != null
                call.respond(
                    HttpStatusCode.Unauthorized,
                    SystmAuthStatus(
                        status = if (hasCredentials) "needs_login" else "not_configured",
                        loggedIn = false,
                        message = if (hasCredentials) "Credentials configured but not logged in. Restart app to auto-login." else "No credentials configured in systm.yaml"
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
                log.info("Manual login triggered, attempting to authenticate...")
                val loginResult = authService.login()
                if (loginResult != null) {
                    val (token, claims) = loginResult
                    val systmSession = SystmSession.create(token)
                    call.sessions.set(systmSession)
                    log.info("Successfully authenticated as: ${claims.username}")

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
