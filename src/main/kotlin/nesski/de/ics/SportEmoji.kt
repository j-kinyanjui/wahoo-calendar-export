package nesski.de.ics

/**
 * Maps Wahoo SYSTM workout sport types to emoji for VEVENT SUMMARY display.
 *
 * The mapping uses the `prospect.type` field (or fallback `item.type`) which contains values like
 * "Cycling", "Yoga", "Strength", "Running", etc. Matching is case-insensitive.
 */
object SportEmoji {

    private val EMOJI_MAP: Map<String, String> =
        mapOf(
            "cycling" to "\uD83D\uDEB4", // U+1F6B4
            "yoga" to "\uD83E\uDDD8", // U+1F9D8
            "strength" to "\uD83C\uDFCB\uFE0F", // U+1F3CB + variation selector
            "running" to "\uD83C\uDFC3", // U+1F3C3
            "swimming" to "\uD83C\uDFCA", // U+1F3CA
            "mental" to "\uD83E\uDDD0", // U+1F9D0 (face with monocle — for mental training)
            "meditation" to "\uD83E\uDDD8", // U+1F9D8 (same as yoga — seated meditation)
            "rowing" to "\uD83D\uDEA3", // U+1F6A3
            "rest" to "\uD83D\uDCA4", // U+1F4A4 (zzz)
            "workout" to "\uD83D\uDCAA", // U+1F4AA (flexed bicep — generic workout)
        )

    private const val DEFAULT_EMOJI = "\uD83C\uDFCB\uFE0F" // U+1F3CB — generic fitness

    /**
     * Get the emoji for a workout sport type.
     *
     * @param type The sport/workout type string (e.g. "Cycling", "Yoga", "Strength").
     *   Case-insensitive. Null or unrecognized types return the default emoji.
     * @return The corresponding emoji string
     */
    fun forType(type: String?): String {
        if (type.isNullOrBlank()) return DEFAULT_EMOJI
        return EMOJI_MAP[type.lowercase()] ?: DEFAULT_EMOJI
    }
}
