package nesski.de.utils

import nesski.de.cli.Range
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
fun parseDateRange(range: Range?, from: LocalDate?, to: LocalDate?): DateRange {
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
            Range.Now -> today
            is Range.Weeks -> range.weeks.let { today.plusWeeks(it.toLong()) }
            is Range.Months -> range.months.let { today.plusMonths(it.toLong()) }
        }
        return DateRange(today, end)
    }

    // Parse --from/--to
    if (from != null && to != null) {
        // Enforce max 2 months
        val daysBetween = ChronoUnit.DAYS.between(from, to)
        if (daysBetween > 62) {
            throw IllegalArgumentException(
                "Date range exceeds maximum of 2 months ($daysBetween days)."
            )
        }

        return DateRange(from, to)
    }

    // Default: next 2 weeks
    return DateRange(today, today.plusWeeks(2))
}
