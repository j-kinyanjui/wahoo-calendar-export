package nesski.de.ics

import nesski.de.models.PlanInfo
import nesski.de.models.Prospect
import nesski.de.models.UserPlanItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for VEVENT SUMMARY formatting — emoji + workout name + duration hint.
 *
 * Format: "emoji WorkoutName (Xmin)" when duration is available.
 * Format: "emoji WorkoutName" when no duration.
 * Examples: "🚴 Costa Blanca (36 min)", "🧘 Morning Yoga Routine (30 min)"
 *
 * Verifies:
 * - Emoji from prospect.type takes priority
 * - Falls back to prospect.style, then item.type
 * - Workout name from prospect.name, fallback to item.type
 * - Duration hint appended when plannedDuration is available
 * - SUMMARY appears correctly in generated ICS output
 */
class SummaryFormattingTest {

    // ── Format: emoji + workout name + duration ─────────────────────

    @Test
    fun `summary uses emoji from prospect type and prospect name with duration`() {
        val item = createItem(
            prospectType = "Cycling",
            prospectName = "Costa Blanca: Puerto de la Vall de Ebo"
        )
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        // Should be cycling emoji + space + workout name + duration hint
        assertEquals("\uD83D\uDEB4 Costa Blanca: Puerto de la Vall de Ebo (30 min)", summary)
    }

    @Test
    fun `summary uses yoga emoji for yoga workout`() {
        val item = createItem(prospectType = "Yoga", prospectName = "Morning Yoga Routine")
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        assertEquals("\uD83E\uDDD8 Morning Yoga Routine (30 min)", summary)
    }

    @Test
    fun `summary uses strength emoji for strength workout`() {
        val item = createItem(prospectType = "Strength", prospectName = "Full Body 02")
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        assertEquals("\uD83C\uDFCB\uFE0F Full Body 02 (30 min)", summary)
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
                Prospect(type = null, name = "Recovery Spin", style = "cycling", plannedDuration = 0.5)
            )
        )
        val prospect = item.prospects!!.first()

        val summary = IcsBuilder.formatSummary(item, prospect)

        // Should use cycling emoji from style fallback
        assertEquals("\uD83D\uDEB4 Recovery Spin (30 min)", summary)
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

        // No duration → no hint
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

        // Should use cycling emoji and "Cycling" as name, no duration
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

        // Default emoji + actual name, no duration
        assertEquals("\uD83C\uDFCB\uFE0F Mystery Session", summary)
    }

    // ── Integration: SUMMARY in ICS output ──────────────────────────

    @Test
    fun `generated ICS contains correct SUMMARY line for cycling with duration`() {
        val items = listOf(
            createItem(prospectType = "Cycling", prospectName = "The Shovel")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("The Shovel (30 min)"))
    }

    @Test
    fun `generated ICS contains correct SUMMARY line for yoga with duration`() {
        val items = listOf(
            createItem(prospectType = "Yoga", prospectName = "Side Bends")
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("Side Bends (30 min)"))
    }

    @Test
    fun `generated ICS with multiple workouts has correct emoji per type`() {
        val items = listOf(
            createItem(prospectType = "Cycling", prospectName = "Cadence Builds"),
            createItem(prospectType = "Yoga", prospectName = "Morning Yoga Routine", agendaId = "a2"),
            createItem(prospectType = "Strength", prospectName = "Full Body 02", agendaId = "a3"),
        )

        val result = IcsBuilder.build(items)

        assertTrue(result.icsContent.contains("Cadence Builds"))
        assertTrue(result.icsContent.contains("Morning Yoga Routine"))
        assertTrue(result.icsContent.contains("Full Body 02"))
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
