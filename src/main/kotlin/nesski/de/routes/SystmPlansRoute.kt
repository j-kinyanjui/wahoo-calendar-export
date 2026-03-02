package nesski.de.routes

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
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.Serializable
import nesski.de.models.Plan
import nesski.de.models.SystmSession
import nesski.de.plugins.applicationHttpClient
import nesski.de.services.GraphQLException
import nesski.de.services.SystmPlansService

internal val log = KtorSimpleLogger("SystmPlansRoute")

/**
 * Response wrapper for plans endpoint
 */
@Serializable
data class PlansResponse(
    val success: Boolean,
    val plans: List<Plan> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

/**
 * Configure Systm plans routes
 */
fun Application.configureSystmPlans(httpClient: HttpClient = applicationHttpClient) {
    install(Sessions) {
        cookie<SystmSession>("systm_session")
    }

    val plansService = SystmPlansService(httpClient)

    routing {
        /**
         * GET /systm-plans - returns user's training plans
         * Requires valid Systm session (token)
         */
        get("/systm-plans") {
            val session: SystmSession? = call.sessions.get()

            // Check for valid session
            if (session == null) {
                log.warn("No session found for /systm-plans")
                call.respondRedirect("http://wahoo.calendar:8484/systm-login?redirectUrl=${call.request.uri}")
                return@get
            }

            // Check if session is expired
            if (session.isExpired()) {
                log.warn("Session expired for /systm-plans")
                call.respondRedirect("http://wahoo.calendar:8484/systm-login?redirectUrl=${call.request.uri}&reason=expired")
                return@get
            }

            try {
                // Fetch plans using the service
                val plans = plansService.fetchPlans(session.token)

                log.info("Successfully fetched ${plans.size} plans for user")
                call.respond(
                    HttpStatusCode.OK,
                    PlansResponse(
                        success = true,
                        plans = plans,
                        message = "Found ${plans.size} plans"
                    )
                )
            } catch (e: GraphQLException) {
                log.error("GraphQL error fetching plans: ${e.errors}")
                call.respond(
                    HttpStatusCode.BadGateway,
                    PlansResponse(
                        success = false,
                        error = "GraphQL error",
                        message = e.errors.joinToString("; ") { it.message }
                    )
                )
            } catch (e: Exception) {
                log.error("Error fetching plans: ${e.message}")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    PlansResponse(
                        success = false,
                        error = "Internal error",
                        message = e.message
                    )
                )
            }
        }
    }
}
