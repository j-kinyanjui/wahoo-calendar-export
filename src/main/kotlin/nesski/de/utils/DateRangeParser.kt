package nesski.de.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DateRange(val start: LocalDate, val end: LocalDate)

/**
 * Parse CLI date range options into a DateRange.
 *
 * Supports three modes:
 * 1. --range shorthand: now, 1w, 2w, 1m, 2m (always from today)
 * 2. --from/--to ISO dates (YYYY-MM-DD)
 * 3. Default: next 2 weeks from today
 *
 * --range and --from/--to are mutually exclusive.
 * Maximum range enforced: 2 months (62 days).
 */
fun parseDateRange(range: String?, from: String?, to: String?): DateRange {
    val today = LocalDate.now()

    // Mutual exclusion check
    if (range != null && (from != null || to != null)) {
        throw IllegalArgumentException(
            "Cannot use --range with --from/--to. Use one or the other."
        )
    }

    // Parse --range shorthand
    if (range != null) {
        val end = when (range) {
            "now" -> today
            "1w" -> today.plusWeeks(1)
            "2w" -> today.plusWeeks(2)
            "1m" -> today.plusMonths(1)
            "2m" -> today.plusMonths(2)
            else -> throw IllegalArgumentException(
                "Invalid range: $range. Valid values: now, 1w, 2w, 1m, 2m"
            )
        }
        return DateRange(today, end)
    }

    // Parse --from/--to
    if (from != null || to != null) {
        val startDate = from?.let { LocalDate.parse(it) } ?: today
        val endDate = to?.let { LocalDate.parse(it) } ?: startDate.plusWeeks(2)

        // Enforce max 2 months
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
        if (daysBetween > 62) {
            throw IllegalArgumentException(
                "Date range exceeds maximum of 2 months ($daysBetween days)."
            )
        }

        return DateRange(startDate, endDate)
    }

    // Default: next 2 weeks
    return DateRange(today, today.plusWeeks(2))
}
