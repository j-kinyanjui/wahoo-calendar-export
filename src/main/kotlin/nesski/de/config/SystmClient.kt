package nesski.de.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val SYSTM_BASE_URL = "https://api.thesufferfest.com"
const val SYSTM_GRAPHQL_ENDPOINT = "$SYSTM_BASE_URL/graphql"

object TokenStorage {
    lateinit var token: String
}

val wahooClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
