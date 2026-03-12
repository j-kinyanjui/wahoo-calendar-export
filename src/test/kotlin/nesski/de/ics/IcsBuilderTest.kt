package nesski.de.ics

import nesski.de.models.PlanInfo
import nesski.de.models.Prospect
import nesski.de.models.UserPlanItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for IcsBuilder — converting UserPlanItems to RFC 5545 VCALENDAR/VEVENT.
 *
 * Tests verify:
 * - Valid VCALENDAR envelope wrapping (VERSION, PRODID, CALSCALE)
 * - VEVENT structure with correct fields (UID, DTSTART, DTEND, SUMMARY, STATUS, DESCRIPTION)
 * - DATE-only DTSTART/DTEND format (all-day placeholder events)
 * - TRANSP:TRANSPARENT (doesn't block the whole day)
 * - STATUS:CONFIRMED for all events
 * - Skipping of rest days, missing dates, missing workout details
 * - UID resolution priority (agendaId > workoutId > generated) with @wahoo suffix
 * - Duration hint in SUMMARY text
 * - ical4j-generated output (RFC 5545 compliant)
 */
class IcsBuilderTest {

    // ── VCALENDAR envelope ──────────────────────────────────────────

    @Test
    fun `build produces valid VCALENDAR wrapper with CALSCALE`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "agenda-1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.startsWith("BEGIN:VCALENDAR"))
        assertTrue(result.icsContent.contains("VERSION:2.0"))
        assertTrue(result.icsContent.contains("PRODID:-//WahooCLI//SYSTM Plan Export//EN"))
        assertTrue(result.icsContent.contains("CALSCALE:GREGORIAN"))
        assertTrue(result.icsContent.trimEnd().endsWith("END:VCALENDAR"))
    }

    @Test
    fun `build with empty list produces calendar with no VEVENTs`() {
        val result = IcsBuilder.build(emptyList())

        assertTrue(result.icsContent.contains("BEGIN:VCALENDAR"))
        assertTrue(result.icsContent.contains("END:VCALENDAR"))
        assertFalse(result.icsContent.contains("BEGIN:VEVENT"))
        assertFalse(result.icsContent.contains("BEGIN:VTODO"), "Should not contain VTODO")
        assertEquals(0, result.exportedCount)
        assertEquals(0, result.skippedCount)
    }

    // ── VEVENT structure ────────────────────────────────────────────

    @Test
    fun `build produces VEVENT with DATE-only DTSTART and DTEND`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "agenda-1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("DTSTART;VALUE=DATE:20260310"))
        assertTrue(result.icsContent.contains("DTEND;VALUE=DATE:20260311"))
        assertFalse(result.icsContent.contains("DUE"), "Should not contain DUE property")
    }

    @Test
    fun `build produces VEVENT with UID from agendaId plus wahoo suffix`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "xu8fKNWU5M_7")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("UID:xu8fKNWU5M_7@wahoo"))
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

        assertTrue(result.icsContent.contains("UID:wo-123@wahoo"))
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
        assertTrue(result.icsContent.contains("@wahoo"))
    }

    @Test
    fun `build sets STATUS CONFIRMED for all events`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("STATUS:CONFIRMED"))
        assertFalse(result.icsContent.contains("STATUS:NEEDS-ACTION"), "Should not contain NEEDS-ACTION")
        assertFalse(result.icsContent.contains("STATUS:COMPLETED"), "Should not use VTODO status mapping")
    }

    @Test
    fun `build sets TRANSP TRANSPARENT for all events`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("TRANSP:TRANSPARENT"))
    }

    @Test
    fun `build includes DTSTAMP in VEVENT`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1")
        )

        val result = IcsBuilder.build(items)

        // DTSTAMP format: YYYYMMDDTHHmmssZ
        assertTrue(result.icsContent.contains(Regex("DTSTAMP:\\d{8}T\\d{6}Z")))
    }

    @Test
    fun `build includes DESCRIPTION with workout details and drag instruction`() {
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

        // ical4j uses RFC 5545 line folding at 75 chars — unfold before asserting
        val unfolded = unfoldIcs(result.icsContent)
        assertTrue(unfolded.contains("DESCRIPTION:"))
        assertTrue(unfolded.contains("Type: Cycling"))
        assertTrue(unfolded.contains("Plan: 6 Week - Fitness Kickstarter"))
        assertTrue(unfolded.contains("Duration: 30 min"))
        assertTrue(unfolded.contains("Drag this event to a time that works for you"))
    }

    @Test
    fun `build uses VEVENT not VTODO`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("BEGIN:VEVENT"))
        assertTrue(result.icsContent.contains("END:VEVENT"))
        assertFalse(result.icsContent.contains("BEGIN:VTODO"), "Must not contain VTODO")
        assertFalse(result.icsContent.contains("END:VTODO"), "Must not contain VTODO")
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

        assertFalse(result.icsContent.contains("BEGIN:VEVENT"))
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

        assertFalse(result.icsContent.contains("BEGIN:VEVENT"))
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
        // Count VEVENT blocks
        val veventCount = Regex("BEGIN:VEVENT").findAll(result.icsContent).count()
        assertEquals(2, veventCount)
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

    // ── Duration hint in SUMMARY ────────────────────────────────────

    @Test
    fun `build includes duration hint in SUMMARY when duration available`() {
        val items = listOf(
            createCyclingItem("2026-03-10T00:00:00.000Z", "a1")
        )

        val result = IcsBuilder.build(items)

        // 0.6 hours = 36 min
        assertTrue(result.icsContent.contains("(36 min)"))
    }

    @Test
    fun `build omits duration hint in SUMMARY when no duration`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = listOf(
                Prospect(type = "Cycling", name = "Easy Spin", plannedDuration = null)
            )
        )

        val result = IcsBuilder.build(listOf(item))

        assertFalse(result.icsContent.contains("min)"))
    }

    // ── Helpers ──────────────────────────────────────────────────────

    /**
     * Unfold RFC 5545 line folding — continuation lines start with a space.
     * This lets assertions check content without worrying about line breaks.
     */
    private fun unfoldIcs(icsContent: String): String {
        return icsContent.replace("\r\n ", "").replace("\n ", "")
    }

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
