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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlansServiceTest {

    private fun createMockClient(responseBody: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val mockEngine = MockEngine { _ ->
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
    fun `fetchPlans returns plan items with VTODO-relevant fields`() = runBlocking {
        val responseJson = """
            {
                "data": {
                    "userPlan": [
                        {
                            "plannedDate": "2026-03-17T00:00:00.000Z",
                            "agendaId": "xu8fKNWU5M_7",
                            "status": "Planned",
                            "type": "Cycling",
                            "prospects": [
                                {
                                    "type": "Cycling",
                                    "name": "Costa Blanca: Puerto de la Vall de Ebo (Recharger Ride)",
                                    "style": null,
                                    "plannedDuration": 0.6011111111111112,
                                    "workoutId": "rUrrfvb8ii"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter ",
                                "level": ""
                            }
                        },
                        {
                            "plannedDate": "2026-03-18T00:00:00.000Z",
                            "agendaId": "xu8fKNWU5M_29",
                            "status": "Planned",
                            "type": "Yoga",
                            "prospects": [
                                {
                                    "type": "Yoga",
                                    "name": "Morning Yoga Routine",
                                    "style": null,
                                    "plannedDuration": 0.25305555555555553,
                                    "workoutId": "3dSiDxhXEJ"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter ",
                                "level": ""
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

        // VTODO-relevant fields — cycling workout
        assertEquals("Costa Blanca: Puerto de la Vall de Ebo (Recharger Ride)", result[0].prospects!![0].name)
        assertEquals("Cycling", result[0].prospects!![0].type)
        assertNull(result[0].prospects!![0].style)
        assertEquals(0.6011111111111112, result[0].prospects!![0].plannedDuration)
        assertEquals("rUrrfvb8ii", result[0].prospects!![0].workoutId)
        assertEquals("Planned", result[0].status)
        assertEquals("2026-03-17T00:00:00.000Z", result[0].plannedDate)
        assertEquals("xu8fKNWU5M_7", result[0].agendaId)

        // Yoga workout
        assertEquals("Morning Yoga Routine", result[1].prospects!![0].name)
        assertEquals("6 Week - Fitness Kickstarter ", result[1].plan?.name)
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
        // Verify trimmed query requests VTODO-relevant fields only
        assertTrue(body.contains("plannedDate"), "Should request plannedDate")
        assertTrue(body.contains("agendaId"), "Should request agendaId")
        assertTrue(body.contains("status"), "Should request status")
        // Verify removed fields are not in the query
        assertTrue(!body.contains("completionData"), "Should NOT request completionData")
        assertTrue(!body.contains("linkData"), "Should NOT request linkData")
        assertTrue(!body.contains("fourDPWorkoutGraph"), "Should NOT request fourDPWorkoutGraph")
        assertTrue(!body.contains("intensity"), "Should NOT request intensity")
        assertTrue(!body.contains("trainerSetting"), "Should NOT request trainerSetting")
        assertTrue(!body.contains("metrics"), "Should NOT request metrics")
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
    fun `fetchPlans handles completed workout items`() = runBlocking {
        val responseJson = """
            {
                "data": {
                    "userPlan": [
                        {
                            "plannedDate": "2026-03-12T00:00:00.000Z",
                            "agendaId": "xu8fKNWU5M_5",
                            "status": "Completed",
                            "type": "Cycling",
                            "prospects": [
                                {
                                    "type": "Cycling",
                                    "name": "The Shovel",
                                    "style": null,
                                    "plannedDuration": 0.6,
                                    "workoutId": "wo-5"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter "
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
        assertEquals("Completed", result[0].status)
        assertEquals("The Shovel", result[0].prospects!![0].name)
        assertEquals("xu8fKNWU5M_5", result[0].agendaId)
    }

    @Test
    fun `fetchPlans handles strength and yoga workout types`() = runBlocking {
        val responseJson = """
            {
                "data": {
                    "userPlan": [
                        {
                            "plannedDate": "2026-03-20T00:00:00.000Z",
                            "agendaId": "xu8fKNWU5M_22",
                            "status": "Planned",
                            "type": "Strength",
                            "prospects": [
                                {
                                    "type": "Strength",
                                    "name": "Full Body 02",
                                    "style": null,
                                    "plannedDuration": 0.19555555555555557,
                                    "workoutId": "oysCVNFmCC"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter "
                            }
                        },
                        {
                            "plannedDate": "2026-03-22T00:00:00.000Z",
                            "agendaId": "xu8fKNWU5M_30",
                            "status": "Planned",
                            "type": "Yoga",
                            "prospects": [
                                {
                                    "type": "Yoga",
                                    "name": "Side Bends",
                                    "style": null,
                                    "plannedDuration": 0.24138888888888888,
                                    "workoutId": "PvHKAMB04y"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter "
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
        assertEquals("Strength", result[0].type)
        assertEquals("Full Body 02", result[0].prospects!![0].name)
        assertEquals("Yoga", result[1].type)
        assertEquals("Side Bends", result[1].prospects!![0].name)
    }
}
