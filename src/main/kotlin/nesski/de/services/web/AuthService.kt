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
 * GraphQL response wrapper for the loginUser mutation.
 * Maps to: { "data": { "loginUser": { ... } } }
 */
@Serializable
data class LoginResponse(
    @SerialName("loginUser")
    val loginUser: LoginUserResult?
)

/**
 * Result of the loginUser mutation.
 * The API always returns HTTP 200 — a failed login is indicated by
 * a null [token] & non-null [failureId].
 */
@Serializable
data class LoginUserResult(
    val status: String,
    val message: String? = null,
    val token: String? = null,
    val failureId: String? = null,
    val user: UserData? = null
)

@Serializable
data class UserData(
    val id: String,
    val fullName: String? = null,
    val email: String? = null
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
        private const val LOGIN_MUTATION = $$"""
            mutation Login($appInformation: AppInformation!, $username: String!, $password: String!) {
                loginUser(
                    appInformation: $appInformation
                    username: $username
                    password: $password
                ) {
                    status
                    message
                    user {
                        id
                        fullName
                        email
                    }
                    token
                    failureId
                }
            }
        """
    }

    /**
     * Login to Systm using credentials from config.
     * @return token on success, or null if login fails.
     */
    suspend fun login(): String? {
        logger.info("Attempting Systm login for user: $username")

        val request = GraphQLRequest(
            operationName = "Login",
            query = LOGIN_MUTATION,
            variables = mapOf(
                "appInformation" to mapOf(
                    "platform" to "web",
                    "version" to "7.105.0-web.3516-9-g193a6cfb",
                    "installId" to "538496F7A02E17E14DF16ECCE8F5DF04"
                ),
                "username" to username,
                "password" to password
            )
        )

        val response: GraphQLResponse<LoginResponse> = httpClient.post(SYSTM_GRAPHQL_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        //logger.info("Body {}", response.toString())

        val result = response.data?.loginUser
        if (result?.failureId != null) {
            throw IllegalStateException(
                "Login failed: status=${result.status}, " +
                        "message=${result.message}, failureId=${result.failureId}"
            )
        }

        logger.info("Successfully logged in as: ${result?.user?.fullName}")
        return result?.token
    }
}
