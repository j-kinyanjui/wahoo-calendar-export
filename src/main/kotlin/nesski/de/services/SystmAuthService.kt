package nesski.de.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.application.environment
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import nesski.de.plugins.GraphQLRequest
import nesski.de.plugins.GraphQLResponse
import nesski.de.plugins.SYSTM_GRAPHQL_ENDPOINT

const val SYSTM_GRAPHQL_ENDPOINT = "https://api.thesufferfest.com/graphql"

internal val log = KtorSimpleLogger("SystmAuthService")

/**
 * Serializer for Any type to handle GraphQL variables
 */
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is String -> encoder.encodeString(value)
            is Number -> encoder.encodeDouble(value.toDouble())
            is Boolean -> encoder.encodeBoolean(value)
            else -> encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        return when (val primitive = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> {
                when {
                    primitive.isString -> primitive.content
                    primitive.boolean != null -> primitive.boolean
                    primitive.int != null -> primitive.int
                    primitive.long != null -> primitive.long
                    primitive.double != null -> primitive.double
                    else -> primitive.content
                }
            }
            is JsonObject -> primitive.toString()
            else -> primitive.toString()
        }
    }
}

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, @Serializable(with = AnySerializer::class) Any>? = null,
    val operationName: String? = null
)

/**
 * Data class representing user claims parsed from JWT token
 */
data class SystmUserClaims(
    val id: String,
    val sessionToken: String,
    val username: String,
    val wahooId: String?,
    val wahooToken: String?,
    val platform: String?,
    val version: String?,
    val roles: List<String>
)

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
        private val LOGIN_MUTATION = """
            mutation Login(${"$"}email: String!, ${"$"}password: String!) {
                login(email: ${"$"}email, password: ${"$"}password) {
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

        /**
         * Load Systm credentials from application environment config
         */
        fun loadCredentials(application: Application): Pair<String, String>? {
            val username = application.environment.config.propertyOrNull("systm.username")?.getString()
                ?: System.getenv("SYSTM_USERNAME")
            val password = application.environment.config.propertyOrNull("systm.password")?.getString()
                ?: System.getenv("SYSTM_PASSWORD")

            return if (username != null && password != null && username.isNotBlank() && password.isNotBlank()) {
                log.info("Loaded Systm credentials from config")
                Pair(username, password)
            } else {
                log.warn("Systm credentials not configured")
                null
            }
        }

        /**
         * Parse JWT claims from token
         * Note: No local JWT validation - Systm validates on each API call
         */
        fun parseJwtClaims(token: String): SystmUserClaims? {
            return try {
                val decodedJWT = JWT.decode(token)
                val claims = decodedJWT.claims

                SystmUserClaims(
                    id = claims["id"]?.asString() ?: decodedJWT.subject ?: "",
                    sessionToken = token,
                    username = claims["username"]?.asString() ?: "",
                    wahooId = claims["wahooId"]?.asString(),
                    wahooToken = claims["wahooToken"]?.asString(),
                    platform = claims["platform"]?.asString(),
                    version = claims["version"]?.asString(),
                    roles = claims["roles"]?.asArray()?.map { it.asString() } ?: emptyList()
                )
            } catch (e: Exception) {
                log.error("Failed to parse JWT claims: ${e.message}")
                null
            }
        }
    }

    /**
     * Login to Systm using credentials from config
     * @return Pair of (token, user claims) or null if login fails
     */
    suspend fun login(): Pair<String, SystmUserClaims>? {
        return try {
            log.info("Attempting Systm login for user: $username")

            val request = GraphQLRequest(
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

            if (response.errors != null && response.errors.isNotEmpty()) {
                log.error("GraphQL errors during login: ${response.errors}")
                return null
            }

            val loginData = response.data?.login
            if (loginData == null) {
                log.error("No login data returned")
                return null
            }

            val token = loginData.token
            val claims = parseJwtClaims(token)

            if (claims == null) {
                log.error("Failed to parse JWT claims from login response")
                return null
            }

            log.info("Successfully logged in as: ${claims.username}")
            Pair(token, claims)
        } catch (e: Exception) {
            log.error("Login failed: ${e.message}")
            null
        }
    }
}
