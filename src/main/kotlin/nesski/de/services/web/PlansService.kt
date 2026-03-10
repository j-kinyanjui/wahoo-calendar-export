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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import nesski.de.models.GetUserPlansRangeResponse
import nesski.de.models.GraphQLError
import nesski.de.models.GraphQLRequest
import nesski.de.models.GraphQLResponse
import nesski.de.models.UserPlanItem
import nesski.de.plugins.SYSTM_GRAPHQL_ENDPOINT

private val log = LoggerFactory.getLogger("SystmPlansService")

/**
 * Exception thrown when GraphQL returns errors
 */
class GraphQLException(val errors: List<GraphQLError>) : Exception(
    errors.joinToString(", ") { it.message }
)

/**
 * Service for fetching Systm training plans via the `userPlan` GraphQL query.
 */
class PlansService(
    private val httpClient: HttpClient,
    private val token: String,
    private val timezone: String = ZoneId.systemDefault().id,
) {
    companion object {
        /**
         * GraphQL query matching the real Wahoo SYSTM API.
         *
         * The query field is `userPlan` (NOT `userPlansRange`).
         * Variable types use the custom scalars `Date`, `QueryParams`, and `TimeZone`.
         */
        /**
         * Trimmed to only the fields needed for VEVENT/VCALENDAR generation:
         *  - plannedDate  → VEVENT DTSTART/DTEND
         *  - agendaId     → VEVENT UID
         *  - status       → filtering
         *  - type         → filter out "rest" days
         *  - prospect: type, name, style, plannedDuration, workoutId → SUMMARY, emoji, duration, UID fallback
         *  - plan: id, name, level → grouping / email body
         */
        private val GET_USER_PLANS_QUERY = $$"""
            query GetUserPlansRange($startDate: Date, $endDate: Date, $queryParams: QueryParams, $timezone: TimeZone) {
              userPlan(
                startDate: $startDate
                endDate: $endDate
                queryParams: $queryParams
                timezone: $timezone
              ) {
                ...userPlanItemFragment
                __typename
              }
            }

            fragment userPlanItemFragment on UserPlanItem {
              plannedDate
              agendaId
              status
              type
              prospects {
                type
                name
                style
                plannedDuration
                workoutId
                __typename
              }
              plan {
                id
                name
                level
                __typename
              }
              __typename
            }
        """.trimIndent()

        /** The API expects ISO-8601 datetime strings with time portion. */
        private val START_OF_DAY_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'")
        private val END_OF_DAY_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'23:59:59.999'Z'")
    }

    /**
     * Fetch plan items for a given date range.
     *
     * @param startDate Start date for the range (inclusive)
     * @param endDate   End date for the range (inclusive)
     * @return List of [UserPlanItem]s within the range
     * @throws GraphQLException if the GraphQL query returns errors
     */
    suspend fun fetchPlans(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<UserPlanItem> {
        log.info("Fetching plans from $startDate to $endDate (tz=$timezone)")

        val request = GraphQLRequest(
            operationName = "GetUserPlansRange",
            query = GET_USER_PLANS_QUERY,
            variables = mapOf(
                "startDate" to startDate.format(START_OF_DAY_FORMATTER),
                "endDate" to endDate.format(END_OF_DAY_FORMATTER),
                "queryParams" to mapOf("limit" to 1000),
                "timezone" to timezone
            )
        )

        val response: GraphQLResponse<GetUserPlansRangeResponse> =
            httpClient.post(SYSTM_GRAPHQL_ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(request)
            }.body()

        // Check for GraphQL errors first
        if (!response.errors.isNullOrEmpty()) {
            log.error("GraphQL errors during fetchPlans: ${response.errors}")
            throw GraphQLException(response.errors)
        }

        val items = response.data?.userPlan
        if (items == null) {
            log.warn("No data returned from userPlan query")
            return emptyList()
        }

        log.info("Successfully fetched ${items.size} plan item(s)")
        return items
    }
}
