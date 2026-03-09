package nesski.de.ics

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for SportEmoji — mapping Wahoo SYSTM workout types to emoji.
 *
 * Verifies:
 * - All known sport types map to correct emoji
 * - Case-insensitive matching
 * - Null/blank/unknown types return default emoji
 * - Emoji are valid Unicode strings
 */
class SportEmojiTest {

    // ── Known sport types ───────────────────────────────────────────

    @Test
    fun `cycling maps to bicycle emoji`() {
        assertEquals("\uD83D\uDEB4", SportEmoji.forType("Cycling"))
    }

    @Test
    fun `yoga maps to person in lotus position emoji`() {
        assertEquals("\uD83E\uDDD8", SportEmoji.forType("Yoga"))
    }

    @Test
    fun `strength maps to weight lifter emoji`() {
        assertEquals("\uD83C\uDFCB\uFE0F", SportEmoji.forType("Strength"))
    }

    @Test
    fun `running maps to runner emoji`() {
        assertEquals("\uD83C\uDFC3", SportEmoji.forType("Running"))
    }

    @Test
    fun `swimming maps to swimmer emoji`() {
        assertEquals("\uD83C\uDFCA", SportEmoji.forType("Swimming"))
    }

    @Test
    fun `mental maps to monocle face emoji`() {
        assertEquals("\uD83E\uDDD0", SportEmoji.forType("Mental"))
    }

    @Test
    fun `meditation maps to person in lotus position emoji`() {
        assertEquals("\uD83E\uDDD8", SportEmoji.forType("Meditation"))
    }

    @Test
    fun `rowing maps to rower emoji`() {
        assertEquals("\uD83D\uDEA3", SportEmoji.forType("Rowing"))
    }

    @Test
    fun `rest maps to zzz emoji`() {
        assertEquals("\uD83D\uDCA4", SportEmoji.forType("Rest"))
    }

    @Test
    fun `workout maps to flexed bicep emoji`() {
        assertEquals("\uD83D\uDCAA", SportEmoji.forType("workout"))
    }

    // ── Case insensitivity ──────────────────────────────────────────

    @Test
    fun `matching is case insensitive - lowercase`() {
        assertEquals("\uD83D\uDEB4", SportEmoji.forType("cycling"))
    }

    @Test
    fun `matching is case insensitive - uppercase`() {
        assertEquals("\uD83D\uDEB4", SportEmoji.forType("CYCLING"))
    }

    @Test
    fun `matching is case insensitive - mixed case`() {
        assertEquals("\uD83E\uDDD8", SportEmoji.forType("yOgA"))
    }

    // ── Edge cases ──────────────────────────────────────────────────

    @Test
    fun `null type returns default emoji`() {
        assertEquals("\uD83C\uDFCB\uFE0F", SportEmoji.forType(null))
    }

    @Test
    fun `blank type returns default emoji`() {
        assertEquals("\uD83C\uDFCB\uFE0F", SportEmoji.forType(""))
    }

    @Test
    fun `whitespace-only type returns default emoji`() {
        assertEquals("\uD83C\uDFCB\uFE0F", SportEmoji.forType("   "))
    }

    @Test
    fun `unknown type returns default emoji`() {
        assertEquals("\uD83C\uDFCB\uFE0F", SportEmoji.forType("UnknownSport"))
    }
}
