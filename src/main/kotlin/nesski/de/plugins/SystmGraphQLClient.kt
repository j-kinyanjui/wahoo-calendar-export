package nesski.de.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nesski.de.models.GraphQLRequest
import nesski.de.models.GraphQLResponse

const val SYSTM_BASE_URL = "https://api.thesufferfest.com"
const val SYSTM_GRAPHQL_ENDPOINT = "$SYSTM_BASE_URL/graphql"

object TokenStorage {
    var token: String? = null
}

val wahooHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

/**
 * Execute a GraphQL query against the Systm API
 * @param query The GraphQL query string
 * @param variables Optional variables for the query
 * @return GraphQLResponse<T> containing the response data or errors
 */
suspend inline fun <reified T> executeSystmQuery(
    query: String,
    variables: Map<String, String>? = null,
): GraphQLResponse<T> {
    val request = GraphQLRequest(
        query = query,
        variables = variables
    )

    return wahooHttpClient.post(SYSTM_GRAPHQL_ENDPOINT) {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer ${TokenStorage.token}")
        setBody(request)
    }.body()
}
