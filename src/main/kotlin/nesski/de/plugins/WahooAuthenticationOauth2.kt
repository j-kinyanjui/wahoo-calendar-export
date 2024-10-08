package nesski.de.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headers
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.util.logging.KtorSimpleLogger
import nesski.de.models.UserSession
import nesski.de.models.WahooWorkouts

const val BASE_URL = "https://api.wahooligan.com"
internal val log = KtorSimpleLogger("WahooAuthenticationOauth2")

fun Application.configureAuthentication(httpClient: HttpClient = applicationHttpClient) {
    install(Sessions) {
        cookie<UserSession>("wahoo_user_session")
    }

    val redirects = mutableMapOf<String, String>()
    val clientId = environment.config.propertyOrNull("oauth.clientId")?.getString() ?: System.getenv("CLIENT_ID")
    val clientSecret =
        environment.config.propertyOrNull("oauth.clientSecret")?.getString() ?: System.getenv("CLIENT_SECRET")

    install(Authentication) {
        oauth("auth-oauth-wahoo") {
            // Configure oauth authentication
            urlProvider = { "https://wahoo.nesski.com/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "wahoo-system",
                    authorizeUrl = "$BASE_URL/oauth/authorize",
                    accessTokenUrl = "$BASE_URL/oauth/token",
                    requestMethod = HttpMethod.Post,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    defaultScopes = listOf("email", "user_read", "workouts_read", "plans_read"),
                    // /extraAuthParameters = listOf("access_type" to "offline"),
                    onStateCreated = { call, state ->
                        // saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    },
                )
            }
            client = httpClient
        }
    }
    routing {
        authenticate("auth-oauth-wahoo") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }
            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                currentPrincipal?.let { principal ->
                    principal.state?.let { state ->
                        call.sessions.set(UserSession(state = state, token = principal.accessToken))
                        redirects[state]?.let { redirect ->
                            call.respondRedirect(redirect)
                            return@get
                        }
                    }
                }
                call.respondRedirect("/home")
            }
        }
        get("/") {
            call.respondRedirect("http://localhost:8484/login")
        }
        get("/home") {
            val userSession: UserSession? = UserSession.getSession(call)
            if (userSession != null) {
                call.respondText("Hello, Piiiiimmmmp! Welcome home!")
            }
        }
        get("/{path}") {
            val userSession: UserSession? = UserSession.getSession(call)
            if (userSession != null) {
                val workouts: WahooWorkouts = getPlans(httpClient, userSession)
                call.respondText("Found, ${workouts.workouts.size} workouts from user profile!")
            }
        }
    }
}

suspend fun getPlans(
    httpClient: HttpClient,
    userSession: UserSession,
): WahooWorkouts =
    runCatching {
        httpClient.get("$BASE_URL/v1/plans") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
            }
        }
    }.fold(
        onSuccess = { it.body() },
        onFailure = {
            log.error("Encountered error while getting plans: $it")
            throw it
        },
    )
