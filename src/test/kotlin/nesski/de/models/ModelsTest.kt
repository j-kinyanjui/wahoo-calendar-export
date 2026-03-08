package nesski.de.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for deserialization of Wahoo SYSTM GraphQL API responses
 * into the Kotlin model classes.
 */
class ModelsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ── GetUserPlansRangeResponse ────────────────────────────────────

    @Test
    fun `deserialize empty userPlan array`() {
        val raw = """{"userPlan":[]}"""
        val response = json.decodeFromString<GetUserPlansRangeResponse>(raw)
        assertNotNull(response.userPlan)
        assertTrue(response.userPlan!!.isEmpty())
    }

    @Test
    fun `deserialize full GraphQL response with empty data`() {
        val raw = """{"data":{"userPlan":[]},"errors":null}"""
        val response = json.decodeFromString<GraphQLResponse<GetUserPlansRangeResponse>>(raw)
        assertNull(response.errors)
        assertNotNull(response.data)
        assertTrue(response.data!!.userPlan!!.isEmpty())
    }

    // ── UserPlanItem ────────────────────────────────────────────────

    @Test
    fun `deserialize minimal UserPlanItem`() {
        val raw = """
            {
                "day": 5,
                "plannedDate": "2026-03-10T00:00:00.000Z",
                "rank": 1,
                "status": "planned",
                "type": "workout",
                "prospects": [],
                "plan": null
            }
        """.trimIndent()
        val item = json.decodeFromString<UserPlanItem>(raw)
        assertEquals(5, item.day)
        assertEquals("2026-03-10T00:00:00.000Z", item.plannedDate)
        assertEquals(1, item.rank)
        assertEquals("planned", item.status)
        assertEquals("workout", item.type)
        assertNotNull(item.prospects)
        assertTrue(item.prospects!!.isEmpty())
        assertNull(item.plan)
    }

    @Test
    fun `deserialize UserPlanItem with all fields`() {
        val raw = """
            {
                "day": 12,
                "plannedDate": "2026-03-17T00:00:00.000Z",
                "rank": 0,
                "agendaId": "agenda-123",
                "status": "completed",
                "type": "workout",
                "appliedTimeZone": "Europe/Berlin",
                "wahooWorkoutId": "wahoo-456",
                "completionData": {
                    "name": "Morning Ride",
                    "date": "2026-03-17T07:30:00.000Z",
                    "activityId": "act-789",
                    "durationSeconds": 3600,
                    "style": "cycling",
                    "deleted": false
                },
                "prospects": [
                    {
                        "type": "cycling",
                        "name": "The Shovel",
                        "compatibility": "high",
                        "description": "A tough FTP workout",
                        "style": "cycling",
                        "intensity": {
                            "master": 0.85,
                            "nm": 0.0,
                            "ac": 0.3,
                            "map": 0.6,
                            "ftp": 0.85
                        },
                        "trainerSetting": {
                            "mode": "erg",
                            "level": 5
                        },
                        "plannedDuration": 3600,
                        "durationType": "time",
                        "metrics": {
                            "ratings": {
                                "nm": 1.0,
                                "ac": 3.0,
                                "map": 6.0,
                                "ftp": 8.5
                            }
                        },
                        "contentId": "content-abc",
                        "workoutId": "wo-def",
                        "notes": "Stay in the zone",
                        "fourDPWorkoutGraph": [
                            {"time": 0.0, "value": 100.0, "type": "warmup"},
                            {"time": 300.0, "value": 250.0, "type": "interval"}
                        ]
                    }
                ],
                "plan": {
                    "id": "plan-001",
                    "name": "All-Purpose Road",
                    "color": "#FF5733",
                    "deleted": false,
                    "durationDays": 84,
                    "startDate": "2026-02-01T00:00:00.000Z",
                    "endDate": "2026-04-26T00:00:00.000Z",
                    "addons": null,
                    "level": "intermediate",
                    "subcategory": "road",
                    "weakness": "sustained",
                    "description": "A comprehensive road plan",
                    "category": "road",
                    "grouping": "cycling",
                    "option": null,
                    "uniqueToPlan": true,
                    "type": "training",
                    "progression": "linear",
                    "planDescription": "Build your road fitness",
                    "volume": "medium"
                },
                "linkData": {
                    "name": "Linked Ride",
                    "date": "2026-03-17T08:00:00.000Z",
                    "activityId": "link-111",
                    "durationSeconds": 4200,
                    "style": "cycling",
                    "deleted": false
                }
            }
        """.trimIndent()

        val item = json.decodeFromString<UserPlanItem>(raw)

        // Top-level fields
        assertEquals(12, item.day)
        assertEquals("2026-03-17T00:00:00.000Z", item.plannedDate)
        assertEquals("completed", item.status)
        assertEquals("Europe/Berlin", item.appliedTimeZone)
        assertEquals("wahoo-456", item.wahooWorkoutId)

        // CompletionData
        assertNotNull(item.completionData)
        assertEquals("Morning Ride", item.completionData!!.name)
        assertEquals(3600, item.completionData!!.durationSeconds)
        assertEquals(false, item.completionData!!.deleted)

        // Prospects
        assertNotNull(item.prospects)
        assertEquals(1, item.prospects!!.size)
        val prospect = item.prospects!![0]
        assertEquals("The Shovel", prospect.name)
        assertEquals("cycling", prospect.style)
        assertEquals(3600.0, prospect.plannedDuration)
        assertEquals("time", prospect.durationType)
        assertEquals("Stay in the zone", prospect.notes)

        // Intensity
        assertNotNull(prospect.intensity)
        assertEquals(0.85, prospect.intensity!!.ftp)
        assertEquals(0.6, prospect.intensity!!.map)

        // TrainerSetting
        assertNotNull(prospect.trainerSetting)
        assertEquals("erg", prospect.trainerSetting!!.mode)
        assertEquals(5, prospect.trainerSetting!!.level)

        // Metrics
        assertNotNull(prospect.metrics?.ratings)
        assertEquals(8.5, prospect.metrics!!.ratings!!.ftp)

        // 4DP Graph
        assertNotNull(prospect.fourDPWorkoutGraph)
        assertEquals(2, prospect.fourDPWorkoutGraph!!.size)
        assertEquals(0.0, prospect.fourDPWorkoutGraph!![0].time)
        assertEquals("warmup", prospect.fourDPWorkoutGraph!![0].type)

        // PlanInfo
        assertNotNull(item.plan)
        assertEquals("plan-001", item.plan!!.id)
        assertEquals("All-Purpose Road", item.plan!!.name)
        assertEquals(84, item.plan!!.durationDays)
        assertEquals("intermediate", item.plan!!.level)
        assertEquals(true, item.plan!!.uniqueToPlan)

        // LinkData
        assertNotNull(item.linkData)
        assertEquals("Linked Ride", item.linkData!!.name)
        assertEquals(4200, item.linkData!!.durationSeconds)
    }

    @Test
    fun `deserialize UserPlanItem ignores unknown fields`() {
        val raw = """
            {
                "day": 1,
                "plannedDate": "2026-03-01T00:00:00.000Z",
                "someUnknownField": "should be ignored",
                "__typename": "UserPlanItem",
                "prospects": [],
                "plan": null
            }
        """.trimIndent()
        val item = json.decodeFromString<UserPlanItem>(raw)
        assertEquals(1, item.day)
    }

    // ── GraphQL error handling ──────────────────────────────────────

    @Test
    fun `deserialize GraphQL error response`() {
        val raw = """
            {
                "data": null,
                "errors": [
                    {
                        "message": "Not authenticated",
                        "locations": [{"line": 2, "column": 3}],
                        "path": null
                    }
                ]
            }
        """.trimIndent()
        val response = json.decodeFromString<GraphQLResponse<GetUserPlansRangeResponse>>(raw)
        assertNull(response.data)
        assertNotNull(response.errors)
        assertEquals(1, response.errors!!.size)
        assertEquals("Not authenticated", response.errors!![0].message)
        assertEquals(2, response.errors!![0].locations!![0].line)
    }

    // ── Full realistic response ─────────────────────────────────────

    @Test
    fun `deserialize response with multiple plan items`() {
        val raw = """
            {
                "data": {
                    "userPlan": [
                        {
                            "day": 1,
                            "plannedDate": "2026-03-08T00:00:00.000Z",
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
                                "id": "p1",
                                "name": "All-Purpose Road"
                            }
                        },
                        {
                            "day": 2,
                            "plannedDate": "2026-03-09T00:00:00.000Z",
                            "rank": 0,
                            "status": "planned",
                            "type": "rest",
                            "prospects": [],
                            "plan": {
                                "id": "p1",
                                "name": "All-Purpose Road"
                            }
                        },
                        {
                            "day": 3,
                            "plannedDate": "2026-03-10T00:00:00.000Z",
                            "rank": 0,
                            "status": "planned",
                            "type": "workout",
                            "prospects": [
                                {
                                    "name": "Half Monty",
                                    "style": "cycling",
                                    "plannedDuration": 3600
                                }
                            ],
                            "plan": {
                                "id": "p1",
                                "name": "All-Purpose Road"
                            }
                        }
                    ]
                }
            }
        """.trimIndent()

        val response = json.decodeFromString<GraphQLResponse<GetUserPlansRangeResponse>>(raw)
        assertNull(response.errors)
        val items = response.data!!.userPlan!!
        assertEquals(3, items.size)

        assertEquals("Cadence Builds", items[0].prospects!![0].name)
        assertEquals(2700.0, items[0].prospects!![0].plannedDuration)
        assertEquals("rest", items[1].type)
        assertNotNull(items[1].prospects)
        assertTrue(items[1].prospects!!.isEmpty())
        assertEquals("Half Monty", items[2].prospects!![0].name)

        // All belong to the same plan
        assertTrue(items.all { it.plan?.name == "All-Purpose Road" })
    }

    // ── Prospect with null optional fields ──────────────────────────

    @Test
    fun `deserialize Prospect with minimal fields`() {
        val raw = """
            {
                "name": "Recovery Spin",
                "plannedDuration": 1800,
                "style": "cycling"
            }
        """.trimIndent()
        val prospect = json.decodeFromString<Prospect>(raw)
        assertEquals("Recovery Spin", prospect.name)
        assertEquals(1800.0, prospect.plannedDuration)
        assertNull(prospect.intensity)
        assertNull(prospect.trainerSetting)
        assertNull(prospect.metrics)
        assertNull(prospect.fourDPWorkoutGraph)
    }

    @Test
    fun `deserialize Prospect with fractional plannedDuration`() {
        val raw = """
            {
                "name": "Yoga",
                "plannedDuration": 0.19555555555555557,
                "style": "yoga"
            }
        """.trimIndent()
        val prospect = json.decodeFromString<Prospect>(raw)
        assertEquals("Yoga", prospect.name)
        assertEquals(0.19555555555555557, prospect.plannedDuration)
        assertEquals("yoga", prospect.style)
    }

    @Test
    fun `deserialize Prospect with null fourDPWorkoutGraph`() {
        val raw = """
            {
                "name": "Endurance Ride",
                "fourDPWorkoutGraph": null,
                "__typename": "Prospect"
            }
        """.trimIndent()
        val prospect = json.decodeFromString<Prospect>(raw)
        assertEquals("Endurance Ride", prospect.name)
        assertNull(prospect.fourDPWorkoutGraph)
    }

    @Test
    fun `deserialize UserPlanItem with null prospects`() {
        val raw = """
            {
                "day": 3,
                "plannedDate": "2026-03-10T00:00:00.000Z",
                "status": "planned",
                "type": "rest",
                "prospects": null,
                "plan": null
            }
        """.trimIndent()
        val item = json.decodeFromString<UserPlanItem>(raw)
        assertEquals("rest", item.type)
        assertNull(item.prospects)
    }
}
