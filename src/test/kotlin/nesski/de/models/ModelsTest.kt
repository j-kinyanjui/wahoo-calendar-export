package nesski.de.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for deserialization of Wahoo SYSTM GraphQL API responses into the Kotlin model classes.
 *
 * Models are trimmed to fields needed for VTODO/VCALENDAR generation. Test data reflects real API
 * response patterns (capitalized values, null style, etc.).
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
        assertTrue(response.userPlan.isEmpty())
    }

    @Test
    fun `deserialize full GraphQL response with empty data`() {
        val raw = """{"data":{"userPlan":[]},"errors":null}"""
        val response = json.decodeFromString<GraphQLResponse<GetUserPlansRangeResponse>>(raw)
        assertNull(response.errors)
        assertNotNull(response.data)
        assertTrue(response.data.userPlan!!.isEmpty())
    }

    // ── UserPlanItem ────────────────────────────────────────────────

    @Test
    fun `deserialize minimal UserPlanItem`() {
        val raw =
            """
            {
                "plannedDate": "2026-03-10T00:00:00.000Z",
                "status": "Planned",
                "type": "Cycling",
                "prospects": [],
                "plan": null
            }
            """
                .trimIndent()
        val item = json.decodeFromString<UserPlanItem>(raw)
        assertEquals("2026-03-10T00:00:00.000Z", item.plannedDate)
        assertEquals("Planned", item.status)
        assertEquals("Cycling", item.type)
        assertNotNull(item.prospects)
        assertTrue(item.prospects.isEmpty())
        assertNull(item.plan)
    }

    @Test
    fun `deserialize UserPlanItem with all retained fields`() {
        val raw =
            """
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
            }
            """
                .trimIndent()

        val item = json.decodeFromString<UserPlanItem>(raw)

        // Top-level fields
        assertEquals("2026-03-17T00:00:00.000Z", item.plannedDate)
        assertEquals("xu8fKNWU5M_7", item.agendaId)
        assertEquals("Planned", item.status)
        assertEquals("Cycling", item.type)

        // Prospects
        assertNotNull(item.prospects)
        assertEquals(1, item.prospects.size)
        val prospect = item.prospects[0]
        assertEquals("Cycling", prospect.type)
        assertEquals("Costa Blanca: Puerto de la Vall de Ebo (Recharger Ride)", prospect.name)
        assertNull(prospect.style)
        assertEquals(0.6011111111111112, prospect.plannedDuration)
        assertEquals("rUrrfvb8ii", prospect.workoutId)

        // PlanInfo
        assertNotNull(item.plan)
        assertEquals("xu8fKNWU5M", item.plan.id)
        assertEquals("6 Week - Fitness Kickstarter ", item.plan.name)
        assertEquals("", item.plan.level)
    }

    @Test
    fun `deserialize UserPlanItem ignores unknown fields from full API response`() {
        val raw =
            """
            {
                "day": 15,
                "plannedDate": "2026-03-17T00:00:00.000Z",
                "rank": 200,
                "agendaId": "xu8fKNWU5M_7",
                "status": "Planned",
                "type": "Cycling",
                "appliedTimeZone": "Europe/Berlin",
                "wahooWorkoutId": 433533025,
                "completionData": null,
                "linkData": null,
                "__typename": "UserPlanItem",
                "prospects": [],
                "plan": null
            }
            """
                .trimIndent()
        val item = json.decodeFromString<UserPlanItem>(raw)
        assertEquals("2026-03-17T00:00:00.000Z", item.plannedDate)
        assertEquals("xu8fKNWU5M_7", item.agendaId)
        assertEquals("Planned", item.status)
        assertEquals("Cycling", item.type)
    }

    // ── GraphQL error handling ──────────────────────────────────────

    @Test
    fun `deserialize GraphQL error response`() {
        val raw =
            """
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
            """
                .trimIndent()
        val response = json.decodeFromString<GraphQLResponse<GetUserPlansRangeResponse>>(raw)
        assertNull(response.data)
        assertNotNull(response.errors)
        assertEquals(1, response.errors.size)
        assertEquals("Not authenticated", response.errors[0].message)
        assertEquals(2, response.errors[0].locations!![0].line)
    }

    // ── Full realistic response (from real API) ─────────────────────

    @Test
    fun `deserialize real API response with multiple plan items`() {
        val raw =
            """
            {
                "data": {
                    "userPlan": [
                        {
                            "day": 15,
                            "plannedDate": "2026-03-17T00:00:00.000Z",
                            "rank": 200,
                            "agendaId": "xu8fKNWU5M_7",
                            "status": "Planned",
                            "type": "Cycling",
                            "appliedTimeZone": "Europe/Berlin",
                            "wahooWorkoutId": 433533025,
                            "completionData": null,
                            "prospects": [
                                {
                                    "type": "Cycling",
                                    "name": "Costa Blanca: Puerto de la Vall de Ebo (Recharger Ride)",
                                    "compatibility": 100,
                                    "description": "Recovery ride description...",
                                    "style": null,
                                    "plannedDuration": 0.6011111111111112,
                                    "workoutId": "rUrrfvb8ii",
                                    "__typename": "Prospect"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter ",
                                "color": "#0F5D60",
                                "level": "",
                                "__typename": "UserPlan"
                            },
                            "linkData": null,
                            "__typename": "UserPlanItem"
                        },
                        {
                            "day": 16,
                            "plannedDate": "2026-03-18T00:00:00.000Z",
                            "rank": 300,
                            "agendaId": "xu8fKNWU5M_29",
                            "status": "Planned",
                            "type": "Yoga",
                            "prospects": [
                                {
                                    "type": "Yoga",
                                    "name": "Morning Yoga Routine",
                                    "style": null,
                                    "plannedDuration": 0.25305555555555553,
                                    "workoutId": "3dSiDxhXEJ",
                                    "__typename": "Prospect"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter ",
                                "level": "",
                                "__typename": "UserPlan"
                            },
                            "__typename": "UserPlanItem"
                        },
                        {
                            "day": 19,
                            "plannedDate": "2026-03-20T00:00:00.000Z",
                            "rank": 200,
                            "agendaId": "xu8fKNWU5M_22",
                            "status": "Planned",
                            "type": "Strength",
                            "prospects": [
                                {
                                    "type": "Strength",
                                    "name": "Full Body 02",
                                    "style": null,
                                    "plannedDuration": 0.19555555555555557,
                                    "workoutId": "oysCVNFmCC",
                                    "__typename": "Prospect"
                                }
                            ],
                            "plan": {
                                "id": "xu8fKNWU5M",
                                "name": "6 Week - Fitness Kickstarter ",
                                "level": "",
                                "__typename": "UserPlan"
                            },
                            "__typename": "UserPlanItem"
                        }
                    ]
                }
            }
            """
                .trimIndent()

        val response = json.decodeFromString<GraphQLResponse<GetUserPlansRangeResponse>>(raw)
        assertNull(response.errors)
        val items = response.data!!.userPlan!!
        assertEquals(3, items.size)

        // Cycling workout
        val cycling = items[0]
        assertEquals(
            "Costa Blanca: Puerto de la Vall de Ebo (Recharger Ride)",
            cycling.prospects!![0].name,
        )
        assertEquals("Cycling", cycling.prospects[0].type)
        assertNull(cycling.prospects[0].style)
        assertEquals(0.6011111111111112, cycling.prospects[0].plannedDuration)
        assertEquals("xu8fKNWU5M_7", cycling.agendaId)

        // Yoga workout
        val yoga = items[1]
        assertEquals("Morning Yoga Routine", yoga.prospects!![0].name)
        assertEquals("Yoga", yoga.type)
        assertEquals("Yoga", yoga.prospects[0].type)

        // Strength workout
        val strength = items[2]
        assertEquals("Full Body 02", strength.prospects!![0].name)
        assertEquals("Strength", strength.type)
        assertEquals(0.19555555555555557, strength.prospects[0].plannedDuration)

        // All belong to the same plan
        assertTrue(items.all { it.plan?.name == "6 Week - Fitness Kickstarter " })
    }

    // ── Prospect ────────────────────────────────────────────────────

    @Test
    fun `deserialize Prospect with null style (real API pattern)`() {
        val raw =
            """
            {
                "type": "Cycling",
                "name": "Cadence Builds",
                "style": null,
                "plannedDuration": 0.5480555555555555,
                "workoutId": "04BfbUGDMk"
            }
            """
                .trimIndent()
        val prospect = json.decodeFromString<Prospect>(raw)
        assertEquals("Cadence Builds", prospect.name)
        assertEquals("Cycling", prospect.type)
        assertNull(prospect.style)
        assertEquals(0.5480555555555555, prospect.plannedDuration)
        assertEquals("04BfbUGDMk", prospect.workoutId)
    }

    @Test
    fun `deserialize Prospect with style populated (legacy pattern)`() {
        val raw =
            """
            {
                "name": "Recovery Spin",
                "style": "cycling",
                "plannedDuration": 0.5,
                "workoutId": "wo-1"
            }
            """
                .trimIndent()
        val prospect = json.decodeFromString<Prospect>(raw)
        assertEquals("Recovery Spin", prospect.name)
        assertEquals("cycling", prospect.style)
        assertNull(prospect.type)
    }

    @Test
    fun `deserialize Prospect with fractional plannedDuration`() {
        val raw =
            """
            {
                "type": "Yoga",
                "name": "Morning Yoga Routine",
                "style": null,
                "plannedDuration": 0.25305555555555553
            }
            """
                .trimIndent()
        val prospect = json.decodeFromString<Prospect>(raw)
        assertEquals("Morning Yoga Routine", prospect.name)
        assertEquals(0.25305555555555553, prospect.plannedDuration)
        assertEquals("Yoga", prospect.type)
    }

    @Test
    fun `deserialize Prospect ignores unknown fields from full API response`() {
        val raw =
            """
            {
                "type": "Cycling",
                "name": "The Shovel",
                "style": null,
                "plannedDuration": 0.6,
                "workoutId": "wo-123",
                "compatibility": 100,
                "description": "A tough FTP workout",
                "intensity": {"master": 0.85, "ftp": 0.85},
                "trainerSetting": {"mode": "ERG", "level": null},
                "durationType": "Time",
                "metrics": {"ratings": {"ftp": 8.5}},
                "contentId": "content-abc",
                "notes": null,
                "fourDPWorkoutGraph": null,
                "__typename": "Prospect"
            }
            """
                .trimIndent()
        val prospect = json.decodeFromString<Prospect>(raw)
        assertEquals("The Shovel", prospect.name)
        assertEquals("Cycling", prospect.type)
        assertNull(prospect.style)
        assertEquals(0.6, prospect.plannedDuration)
        assertEquals("wo-123", prospect.workoutId)
    }

    @Test
    fun `deserialize UserPlanItem with null prospects`() {
        val raw =
            """
            {
                "plannedDate": "2026-03-10T00:00:00.000Z",
                "status": "Planned",
                "type": "Rest",
                "prospects": null,
                "plan": null
            }
            """
                .trimIndent()
        val item = json.decodeFromString<UserPlanItem>(raw)
        assertEquals("Rest", item.type)
        assertNull(item.prospects)
    }

    // ── PlanInfo ────────────────────────────────────────────────────

    @Test
    fun `deserialize PlanInfo ignores unknown fields from full API response`() {
        val raw =
            """
            {
                "id": "xu8fKNWU5M",
                "name": "6 Week - Fitness Kickstarter ",
                "level": "",
                "color": "#0F5D60",
                "deleted": null,
                "durationDays": 41,
                "startDate": "2026-03-02T00:00:00.000Z",
                "endDate": "2026-04-12T00:00:00.000Z",
                "addons": ["Yoga - Intro", "Strength - Intro"],
                "category": "None",
                "volume": "None",
                "type": "Cycling",
                "__typename": "UserPlan"
            }
            """
                .trimIndent()
        val planInfo = json.decodeFromString<PlanInfo>(raw)
        assertEquals("xu8fKNWU5M", planInfo.id)
        assertEquals("6 Week - Fitness Kickstarter ", planInfo.name)
        assertEquals("", planInfo.level)
    }

    @Test
    fun `deserialize PlanInfo with empty level string`() {
        val raw =
            """
            {
                "id": "p1",
                "name": "Test Plan",
                "level": ""
            }
            """
                .trimIndent()
        val planInfo = json.decodeFromString<PlanInfo>(raw)
        assertEquals("", planInfo.level)
    }
}
