package nesski.de.services.web

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import nesski.de.models.GetUserPlansRangeResponse
import nesski.de.models.GraphQLError
import nesski.de.models.GraphQLRequest
import nesski.de.models.GraphQLResponse
import nesski.de.models.Plan
import nesski.de.plugins.SYSTM_GRAPHQL_ENDPOINT

private val log = LoggerFactory.getLogger("SystmPlansService")

/**
 * Exception thrown when GraphQL returns errors
 */
class GraphQLException(val errors: List<GraphQLError>) : Exception(
    errors.joinToString(", ") { it.message }
)

/**
 * Service for fetching Systm training plans
 */
class SystmPlansService(
    private val httpClient: HttpClient,
    private val token: String,
) {
    companion object {
        // GraphQL query for getting user plans within a date range
        private val GET_USER_PLANS_RANGE_QUERY = $$"""
            query GetUserPlansRange($startDate: String!, $endDate: String!) {
                userPlansRange(startDate: $startDate, endDate: $endDate) {
                    plans {
                        id
                        name
                        scheduledDate
                        status
                        type
                        workouts {
                            id
                            name
                            scheduledDate
                            duration
                            type
                            status
                        }
                    }
                }
            }
        """.trimIndent()

        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }

    /**
     * Fetch plans for a given token within a date range
     * @param token The Bearer token for authentication
     * @param startDate Start date for the range (inclusive)
     * @param endDate End date for the range (inclusive)
     * @return List of plans within the specified date range
     * @throws GraphQLException if the GraphQL query returns errors
     */
    suspend fun fetchPlans(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Plan> {
        log.info("Fetching plans from $startDate to $endDate")

        val request = GraphQLRequest(
            query = GET_USER_PLANS_RANGE_QUERY,
            variables = mapOf(
                "startDate" to startDate.format(DATE_FORMATTER),
                "endDate" to endDate.format(DATE_FORMATTER)
            )
        )

        val response: GraphQLResponse<GetUserPlansRangeResponse> = httpClient.post(SYSTM_GRAPHQL_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }.body()

        // Check for GraphQL errors first
        if (!response.errors.isNullOrEmpty()) {
            log.error("GraphQL errors during fetchPlans: ${response.errors}")
            throw GraphQLException(response.errors)
        }

        val plansData = response.data?.userPlansRange
        if (plansData == null) {
            log.warn("No data returned from userPlansRange query")
            return emptyList()
        }

        log.info("Successfully fetched ${plansData.plans.size} plans")
        return plansData.plans
    }
}
