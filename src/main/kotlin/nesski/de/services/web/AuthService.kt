package nesski.de.services.web

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nesski.de.models.GraphQLRequest
import nesski.de.models.GraphQLResponse
import nesski.de.plugins.SYSTM_GRAPHQL_ENDPOINT

internal val logger: Logger = KtorSimpleLogger("SystmAuthService")

/**
 * GraphQL response for login mutation
 */
@Serializable
data class LoginResponse(
    @SerialName("login")
    val login: LoginData?
)

@Serializable
data class LoginData(
    @SerialName("token")
    val token: String,
    @SerialName("user")
    val user: UserData?
)

@Serializable
data class UserData(
    @SerialName("id")
    val id: String,
    @SerialName("username")
    val username: String,
    @SerialName("wahooId")
    val wahooId: String? = null,
    @SerialName("wahooToken")
    val wahooToken: String? = null
)

/**
 * Service for handling Systm authentication
 */
class SystmAuthService(
    private val httpClient: HttpClient,
    private val username: String,
    private val password: String
) {
    companion object {
        // GraphQL login mutation
        private val LOGIN_MUTATION = $$"""
            mutation Login($email: String!, $password: String!) {
                login(email: $email, password: $password) {
                    token
                    user {
                        id
                        username
                        wahooId
                        wahooToken
                    }
                }
            }
        """.trimIndent()
    }

    /**
     * Login to Systm using credentials from config
     * @return Pair of (token, user claims) or null if login fails
     */
    suspend fun login(): Pair<String, SystmUserClaims>? {
        return try {
            logger.info("Attempting Systm login for user: $username")

            val request = GraphQLRequest(
                operationName = "Login",
                query = LOGIN_MUTATION,
                variables = mapOf(
                    "email" to username,
                    "password" to password
                )
            )

            val response: GraphQLResponse<LoginResponse> = httpClient.post(SYSTM_GRAPHQL_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            if (!response.errors.isNullOrEmpty()) {
                logger.error("GraphQL errors during login: ${response.errors}")
                return null
            }

            val loginData = response.data?.login
            if (loginData == null) {
                logger.error("No login data returned")
                return null
            }

            val token = loginData.token
            val claims = parseJwtClaims(token)

            if (claims == null) {
                logger.error("Failed to parse JWT claims from login response")
                return null
            }

            logger.info("Successfully logged in as: ${claims.username}")
            Pair(token, claims)
        } catch (e: Exception) {
            logger.error("Login failed: ${e.message}")
            null
        }
    }
}
