package nesski.de.ics

import nesski.de.models.PlanInfo
import nesski.de.models.Prospect
import nesski.de.models.UserPlanItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for VTODO SUMMARY formatting — emoji + workout name.
 *
 * Context format per CONTEXT.md: "Emoji + workout name"
 * Examples: "🚴 Costa Blanca: Puerto de la Vall de Ebo", "🧘 Morning Yoga Routine"
 *
 * Verifies:
 * - Emoji from prospect.type takes priority
 * - Falls back to prospect.style, then item.type
 * - Workout name from prospect.name, fallback to item.type
 * - SUMMARY appears correctly in generated ICS output
 */
class SummaryFormattingTest {

    // ── Format: emoji + workout name ────────────────────────────────

    @Test
    fun `summary uses emoji from prospect type and prospect name`() {
        val item = createItem(
            prospectType = "Cycling",
            prospectName = "Costa Blanca: Puerto de la Vall de Ebo"
        )
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        // Should be cycling emoji + space + workout name
        assertEquals("\uD83D\uDEB4 Costa Blanca: Puerto de la Vall de Ebo", summary)
    }

    @Test
    fun `summary uses yoga emoji for yoga workout`() {
        val item = createItem(prospectType = "Yoga", prospectName = "Morning Yoga Routine")
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        assertEquals("\uD83E\uDDD8 Morning Yoga Routine", summary)
    }

    @Test
    fun `summary uses strength emoji for strength workout`() {
        val item = createItem(prospectType = "Strength", prospectName = "Full Body 02")
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        assertEquals("\uD83C\uDFCB\uFE0F Full Body 02", summary)
    }

    // ── Fallback priority ───────────────────────────────────────────

    @Test
    fun `summary falls back to prospect style for emoji when prospect type is null`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = listOf(
                Prospect(type = null, name = "Recovery Spin", style = "cycling")
            )
        )
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        // Should use cycling emoji from style fallback
        assertEquals("\uD83D\uDEB4 Recovery Spin", summary)
    }

    @Test
    fun `summary falls back to item type for emoji when prospect has no type or style`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Yoga",
            prospects = listOf(
                Prospect(type = null, name = "Stretch Session", style = null)
            )
        )
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        // Should use yoga emoji from item.type fallback
        assertEquals("\uD83E\uDDD8 Stretch Session", summary)
    }

    @Test
    fun `summary uses item type as workout name when no prospect`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = "Cycling",
            prospects = emptyList()
        )

        val summary = IcsBuilder.formatSummary(item, null)

        // Should use cycling emoji and "Cycling" as name
        assertEquals("\uD83D\uDEB4 Cycling", summary)
    }

    @Test
    fun `summary uses default emoji and Workout when no type info available`() {
        val item = UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = "a1",
            status = "Planned",
            type = null,
            prospects = listOf(
                Prospect(type = null, name = "Mystery Session", style = null)
            )
        )
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        // Default emoji + actual name
        assertEquals("\uD83C\uDFCB\uFE0F Mystery Session", summary)
    }

    // ── Integration: SUMMARY in ICS output ──────────────────────────

    @Test
    fun `generated ICS contains correct SUMMARY line for cycling`() {
        val items = listOf(
            createItem(prospectType = "Cycling", prospectName = "The Shovel")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("SUMMARY:\uD83D\uDEB4 The Shovel"))
    }

    @Test
    fun `generated ICS contains correct SUMMARY line for yoga`() {
        val items = listOf(
            createItem(prospectType = "Yoga", prospectName = "Side Bends")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("SUMMARY:\uD83E\uDDD8 Side Bends"))
    }

    @Test
    fun `generated ICS with multiple workouts has correct emoji per type`() {
        val items = listOf(
            createItem(prospectType = "Cycling", prospectName = "Cadence Builds"),
            createItem(prospectType = "Yoga", prospectName = "Morning Yoga Routine", agendaId = "a2"),
            createItem(prospectType = "Strength", prospectName = "Full Body 02", agendaId = "a3"),
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("SUMMARY:\uD83D\uDEB4 Cadence Builds"))
        assertTrue(result.icsContent.contains("SUMMARY:\uD83E\uDDD8 Morning Yoga Routine"))
        assertTrue(result.icsContent.contains("SUMMARY:\uD83C\uDFCB\uFE0F Full Body 02"))
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private fun createItem(
        prospectType: String,
        prospectName: String,
        agendaId: String = "a1"
    ): UserPlanItem {
        return UserPlanItem(
            plannedDate = "2026-03-10T00:00:00.000Z",
            agendaId = agendaId,
            status = "Planned",
            type = prospectType,
            prospects = listOf(
                Prospect(
                    type = prospectType,
                    name = prospectName,
                    style = null,
                    plannedDuration = 0.5,
                    workoutId = "wo-1"
                )
            ),
            plan = PlanInfo(id = "p1", name = "Test Plan", level = "")
        )
    }
}
