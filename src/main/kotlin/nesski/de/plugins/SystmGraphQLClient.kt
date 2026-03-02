package nesski.de.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
import nesski.de.models.SystmSession
import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions

const val SYSTM_BASE_URL = "https://api.thesufferfest.com"
const val SYSTM_GRAPHQL_ENDPOINT = "$SYSTM_BASE_URL/graphql"

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

@Serializable
data class GraphQLError(
    val message: String,
    val locations: List<GraphQLLocation>? = null,
    val path: List<String>? = null
)

@Serializable
data class GraphQLLocation(
    val line: Int,
    val column: Int
)

@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

/**
 * Execute a GraphQL query against the Systm API
 * @param query The GraphQL query string
 * @param variables Optional variables for the query
 * @param systmSession The session containing the auth token
 * @param httpClient The HTTP client to use for the request
 * @return GraphQLResponse<T> containing the response data or errors
 */
suspend inline fun <reified T> executeSystmQuery(
    query: String,
    variables: Map<String, Any>? = null,
    systmSession: SystmSession,
    httpClient: HttpClient
): GraphQLResponse<T> {
    val request = GraphQLRequest(
        query = query,
        variables = variables
    )

    return httpClient.post(SYSTM_GRAPHQL_ENDPOINT) {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer ${systmSession.token}")
        setBody(request)
    }.body()
}

/**
 * Get SystmSession from application call
 */
suspend fun getSystmSession(call: ApplicationCall): SystmSession? {
    return call.sessions.get()
}

/**
 * Execute a GraphQL query using the session from the application call
 * @param query The GraphQL query string
 * @param variables Optional variables for the query
 * @param call The application call containing the session
 * @param httpClient The HTTP client to use for the request
 * @return GraphQLResponse<T> containing the response data or errors
 */
suspend inline fun <reified T> executeSystmQueryFromCall(
    query: String,
    variables: Map<String, Any>? = null,
    call: ApplicationCall,
    httpClient: HttpClient
): GraphQLResponse<T>? {
    val systmSession = getSystmSession(call) ?: return null
    return executeSystmQuery(query, variables, systmSession, httpClient)
}
