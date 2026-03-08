package nesski.de.services.web

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.engine.mock.toByteArray
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlansServiceTest {

    private fun createMockClient(responseBody: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = responseBody,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    @Test
    fun `fetchPlans returns empty list when no plans`() = runBlocking {
        val client = createMockClient("""{"data":{"userPlan":[]}}""")
        val service = PlansService(client, "test-token")

        val result = service.fetchPlans(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `fetchPlans returns plan items`() = runBlocking {
        val responseJson = """
            {
                "data": {
                    "userPlan": [
                        {
                            "day": 1,
                            "plannedDate": "2026-03-10T00:00:00.000Z",
                            "rank": 0,
                            "status": "planned",
                            "type": "workout",
                            "prospects": [
                                {
                                    "name": "The Shovel",
                                    "style": "cycling",
                                    "plannedDuration": 3600
                                }
                            ],
                            "plan": {
                                "id": "plan-001",
                                "name": "All-Purpose Road",
                                "level": "intermediate"
                            }
                        },
                        {
                            "day": 2,
                            "plannedDate": "2026-03-11T00:00:00.000Z",
                            "rank": 0,
                            "status": "planned",
                            "type": "workout",
                            "prospects": [
                                {
                                    "name": "Cadence Builds",
                                    "style": "cycling",
                                    "plannedDuration": 2700
                                }
                            ],
                            "plan": {
                                "id": "plan-001",
                                "name": "All-Purpose Road",
                                "level": "intermediate"
                            }
                        }
                    ]
                }
            }
        """.trimIndent()

        val client = createMockClient(responseJson)
        val service = PlansService(client, "test-token")

        val result = service.fetchPlans(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
        )

        assertEquals(2, result.size)

        assertEquals("The Shovel", result[0].prospects!![0].name)
        assertEquals(3600.0, result[0].prospects!![0].plannedDuration)
        assertEquals("planned", result[0].status)
        assertEquals("2026-03-10T00:00:00.000Z", result[0].plannedDate)

        assertEquals("Cadence Builds", result[1].prospects!![0].name)
        assertEquals("All-Purpose Road", result[1].plan?.name)
    }

    @Test
    fun `fetchPlans throws GraphQLException on errors`() = runBlocking {
        val responseJson = """
            {
                "data": null,
                "errors": [
                    {
                        "message": "Not authenticated",
                        "locations": [{"line": 1, "column": 1}]
                    }
                ]
            }
        """.trimIndent()

        val client = createMockClient(responseJson)
        val service = PlansService(client, "bad-token")

        val exception = assertFailsWith<GraphQLException> {
            service.fetchPlans(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
            )
        }

        assertEquals("Not authenticated", exception.message)
        assertEquals(1, exception.errors.size)
    }

    @Test
    fun `fetchPlans returns empty list when data is null`() = runBlocking {
        val responseJson = """{"data": null}"""

        val client = createMockClient(responseJson)
        val service = PlansService(client, "test-token")

        val result = service.fetchPlans(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `fetchPlans sends correct request format`() = runBlocking {
        var capturedBody: String? = null

        val mockEngine = MockEngine { request ->
            capturedBody = String(request.body.toByteArray())
            respond(
                content = """{"data":{"userPlan":[]}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        val service = PlansService(client, "my-token", timezone = "Europe/Berlin")
        service.fetchPlans(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
        )

        val body = capturedBody!!
        // Verify the request contains the correct operation name
        assertTrue(body.contains("GetUserPlansRange"), "Should contain operationName")
        // Verify it uses userPlan, not userPlansRange
        assertTrue(body.contains("userPlan("), "Query should use 'userPlan' field")
        assertTrue(!body.contains("userPlansRange("), "Query should NOT use 'userPlansRange' field")
        // Verify date format includes time portion
        assertTrue(body.contains("2026-03-01T00:00:00.000Z"), "Start date should include time")
        assertTrue(body.contains("2026-03-31T23:59:59.999Z"), "End date should include time")
        // Verify timezone is included
        assertTrue(body.contains("Europe/Berlin"), "Should include timezone")
        // Verify queryParams with limit
        assertTrue(body.contains("1000"), "Should include limit in queryParams")
    }

    @Test
    fun `fetchPlans sends authorization header`() = runBlocking {
        var capturedAuth: String? = null

        val mockEngine = MockEngine { request ->
            capturedAuth = request.headers["Authorization"]
            respond(
                content = """{"data":{"userPlan":[]}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        val service = PlansService(client, "secret-token-123")
        service.fetchPlans(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31))

        assertEquals("Bearer secret-token-123", capturedAuth)
    }

    @Test
    fun `fetchPlans handles items with completion data`() = runBlocking {
        val responseJson = """
            {
                "data": {
                    "userPlan": [
                        {
                            "day": 5,
                            "plannedDate": "2026-03-12T00:00:00.000Z",
                            "status": "completed",
                            "type": "workout",
                            "completionData": {
                                "name": "Morning Ride",
                                "date": "2026-03-12T07:00:00.000Z",
                                "activityId": "act-123",
                                "durationSeconds": 3700,
                                "style": "cycling",
                                "deleted": false
                            },
                            "prospects": [
                                {
                                    "name": "The Shovel",
                                    "plannedDuration": 3600
                                }
                            ],
                            "plan": {
                                "id": "p1",
                                "name": "Test Plan"
                            }
                        }
                    ]
                }
            }
        """.trimIndent()

        val client = createMockClient(responseJson)
        val service = PlansService(client, "test-token")

        val result = service.fetchPlans(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
        )

        assertEquals(1, result.size)
        assertEquals("completed", result[0].status)
        assertEquals("Morning Ride", result[0].completionData?.name)
        assertEquals(3700, result[0].completionData?.durationSeconds)
    }
}
