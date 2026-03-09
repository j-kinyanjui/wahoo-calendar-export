package nesski.de.ics

import nesski.de.models.PlanInfo
import nesski.de.models.Prospect
import nesski.de.models.UserPlanItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for IcsBuilder — converting UserPlanItems to RFC 5545 VCALENDAR/VTODO.
 *
 * Tests verify:
 * - Valid VCALENDAR envelope wrapping
 * - VTODO structure with correct fields (UID, DUE, SUMMARY, STATUS, DESCRIPTION)
 * - Date-only DUE format (Apple Reminders compatible)
 * - Skipping of rest days, missing dates, missing workout details
 * - Status mapping (Completed → COMPLETED, else → NEEDS-ACTION)
 * - UID resolution priority (agendaId > workoutId > generated)
 * - ICS text escaping
 */
class IcsBuilderTest {

    // ── VCALENDAR envelope ──────────────────────────────────────────

    @Test
    fun `build produces valid VCALENDAR wrapper`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "agenda-1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.startsWith("BEGIN:VCALENDAR"))
        assertTrue(result.icsContent.contains("VERSION:2.0"))
        assertTrue(result.icsContent.contains("PRODID:-//WahooCLI//SYSTM Plan Export//EN"))
        assertTrue(result.icsContent.endsWith("END:VCALENDAR"))
    }

    @Test
    fun `build with empty list produces calendar with no VTODOs`() {
        val result = IcsBuilder.build(emptyList())

        assertTrue(result.icsContent.contains("BEGIN:VCALENDAR"))
        assertTrue(result.icsContent.contains("END:VCALENDAR"))
        assertFalse(result.icsContent.contains("BEGIN:VTODO"))
        assertEquals(0, result.exportedCount)
        assertEquals(0, result.skippedCount)
    }

    // ── VTODO structure ─────────────────────────────────────────────

    @Test
    fun `build produces VTODO with correct DUE DATE format`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "agenda-1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("DUE;VALUE=DATE:20260310"))
    }

    @Test
    fun `build produces VTODO with UID from agendaId`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "xu8fKNWU5M_7")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("UID:xu8fKNWU5M_7"))
    }

    @Test
    fun `build uses workoutId as UID fallback when agendaId is null`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = null,
            status = "Planned",
            type = "Cycling",
            prospects = listOf(
                Prospect(type = "Cycling", name = "Test Ride", workoutId = "wo-123")
            )
        )

        val result = IcsBuilder.build(listOf(item))

        assertTrue(result.icsContent.contains("UID:wo-123"))
    }

    @Test
    fun `build generates UID when both agendaId and workoutId are null`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = null,
            status = "Planned",
            type = "Cycling",
            prospects = listOf(
                Prospect(type = "Cycling", name = "Test Ride", workoutId = null)
            )
        )

        val result = IcsBuilder.build(listOf(item))

        assertTrue(result.icsContent.contains("UID:wahoo-"))
    }

    @Test
    fun `build maps completed status to COMPLETED`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Completed",
            type = "Cycling",
            prospects = listOf(
                Prospect(type = "Cycling", name = "Test Ride")
            )
        )

        val result = IcsBuilder.build(listOf(item))

        assertTrue(result.icsContent.contains("STATUS:COMPLETED"))
    }

    @Test
    fun `build maps planned status to NEEDS-ACTION`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("STATUS:NEEDS-ACTION"))
    }

    @Test
    fun `build includes DTSTAMP in VTODO`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1")
        )

        val result = IcsBuilder.build(items)

        // DTSTAMP format: YYYYMMDDTHHmmssZ
        assertTrue(result.icsContent.contains(Regex("DTSTAMP:\\d{8}T\\d{6}Z")))
    }

    @Test
    fun `build includes DESCRIPTION with workout type and plan info`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = listOf(
                Prospect(type = "Cycling", name = "Test Ride", plannedDuration = 0.5)
            ),
            plan = PlanInfo(id = "p1", name = "6 Week - Fitness Kickstarter", level = "")
        )

        val result = IcsBuilder.build(listOf(item))

        assertTrue(result.icsContent.contains("DESCRIPTION:"))
        assertTrue(result.icsContent.contains("Type: Cycling"))
        assertTrue(result.icsContent.contains("Plan: 6 Week - Fitness Kickstarter"))
        assertTrue(result.icsContent.contains("Duration: 30min"))
    }

    // ── Skipping logic ──────────────────────────────────────────────

    @Test
    fun `build skips rest days`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Rest",
            prospects = null
        )

        val result = IcsBuilder.build(listOf(item))

        assertFalse(result.icsContent.contains("BEGIN:VTODO"))
        assertEquals(0, result.exportedCount)
        assertEquals(1, result.skippedCount)
        assertTrue(result.skippedReasons[0].contains("Rest day"))
    }

    @Test
    fun `build skips items with null planned date`() {
        val item = UserPlanItem(
            plannedDate = null,
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = listOf(Prospect(type = "Cycling", name = "Test"))
        )

        val result = IcsBuilder.build(listOf(item))

        assertFalse(result.icsContent.contains("BEGIN:VTODO"))
        assertEquals(0, result.exportedCount)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `build skips items with blank planned date`() {
        val item = UserPlanItem(
            plannedDate = "",
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = listOf(Prospect(type = "Cycling", name = "Test"))
        )

        val result = IcsBuilder.build(listOf(item))

        assertEquals(0, result.exportedCount)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `build skips items with no prospect and no type`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = null,
            prospects = null
        )

        val result = IcsBuilder.build(listOf(item))

        assertEquals(0, result.exportedCount)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `build exports item with type but no prospect`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = emptyList()
        )

        val result = IcsBuilder.build(listOf(item))

        assertEquals(1, result.exportedCount)
        assertTrue(result.icsContent.contains("SUMMARY:"))
    }

    // ── Multiple items ──────────────────────────────────────────────

    @Test
    fun `build handles multiple workouts correctly`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1"),
            UserPlanItem(
                plannedDate = "2026-03-11T00:00:00.000Z",
                agendaId = "a2",
                status = "Planned",
                type = "Yoga",
                prospects = listOf(Prospect(type = "Yoga", name = "Morning Yoga"))
            ),
            UserPlanItem(
                plannedDate = "2026-03-12T00:00:00.000Z",
                agendaId = "a3",
                status = "Planned",
                type = "Rest"
            )
        )

        val result = IcsBuilder.build(items)

        assertEquals(2, result.exportedCount)
        assertEquals(1, result.skippedCount) // rest day
        // Count VTODO blocks
        val vtodoCount = Regex("BEGIN:VTODO").findAll(result.icsContent).count()
        assertEquals(2, vtodoCount)
    }

    @Test
    fun `build counts exported and skipped correctly with mixed items`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1"),        // exported
            UserPlanItem(plannedDate = null, type = "Cycling",          // skipped: no date
                prospects = listOf(Prospect(type = "Cycling", name = "X"))),
            UserPlanItem(plannedDate = "2026-03-11T00:00:00.000Z",      // skipped: rest
                type = "Rest"),
            UserPlanItem(plannedDate = "2026-03-12T00:00:00.000Z",      // exported
                agendaId = "a4", status = "Completed", type = "Strength",
                prospects = listOf(Prospect(type = "Strength", name = "Full Body")))
        )

        val result = IcsBuilder.build(items)

        assertEquals(2, result.exportedCount)
        assertEquals(2, result.skippedCount)
    }

    // ── ICS text escaping ───────────────────────────────────────────

    @Test
    fun `build escapes special characters in description`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = listOf(Prospect(type = "Cycling", name = "Test Ride")),
            plan = PlanInfo(id = "p1", name = "Plan; with, special chars")
        )

        val result = IcsBuilder.build(listOf(item))

        // Semicolons and commas should be escaped in DESCRIPTION
        assertTrue(result.icsContent.contains("Plan\\; with\\, special chars"))
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private fun createCyclingItem(date: String, agendaId: String): UserPlanItem {
        return UserPlanItem(
            plannedDate = date,
            agendaId = agendaId,
            status = "Planned",
            type = "Cycling",
            prospects = listOf(
                Prospect(
                    type = "Cycling",
                    name = "Costa Blanca: Puerto de la Vall de Ebo",
                    style = null,
                    plannedDuration = 0.6,
                    workoutId = "rUrrfvb8ii"
                )
            ),
            plan = PlanInfo(id = "xu8fKNWU5M", name = "6 Week - Fitness Kickstarter", level = "")
        )
    }
}
