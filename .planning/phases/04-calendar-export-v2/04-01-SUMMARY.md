---
phase: 04-calendar-export-v2
plan: 01
subsystem: ics
tags: [ical4j, vevent, rfc5545, calendar, apple-calendar, google-calendar]

# Dependency graph
requires:
  - phase: 03-ics-export-email
    provides: IcsBuilder with VTODO generation, SportEmoji mapping, IcsFileWriter
provides:
  - VEVENT all-day calendar events via ical4j (cross-calendar compatible)
  - RFC 5545 compliant ICS output with CalendarOutputter
  - Duration hint in SUMMARY for user scheduling
affects: []

# Tech tracking
tech-stack:
  added: [ical4j 4.0.8]
  patterns: [ical4j CalendarOutputter for RFC 5545, all-day DATE events, TRANSP:TRANSPARENT]

key-files:
  created: []
  modified:
    - build.gradle.kts
    - src/main/kotlin/nesski/de/ics/IcsBuilder.kt
    - src/main/kotlin/nesski/de/ics/SportEmoji.kt
    - src/main/kotlin/nesski/de/models/Models.kt
    - src/main/kotlin/nesski/de/email/EmailService.kt
    - src/main/kotlin/nesski/de/services/web/PlansService.kt
    - src/test/kotlin/nesski/de/ics/IcsBuilderTest.kt
    - src/test/kotlin/nesski/de/ics/SummaryFormattingTest.kt
    - src/test/kotlin/nesski/de/email/EmailServiceTest.kt

key-decisions:
  - "ical4j 4.0.8 for RFC 5545 compliance — replaces hand-rolled ICS strings"
  - "VEVENT all-day events with DATE-only DTSTART/DTEND — users drag to preferred time"
  - "TRANSP:TRANSPARENT — all-day events don't block calendar"
  - "UID gets @wahoo suffix for domain uniqueness per RFC 5545"
  - "Duration hint in SUMMARY text — communicates workout length without enforcing it"

patterns-established:
  - "ical4j CalendarOutputter for all ICS generation — no more hand-rolled strings"
  - "unfoldIcs test helper for RFC 5545 line-folding assertions"

requirements-completed: [EXPORT-02]

# Metrics
duration: 8min
completed: 2026-03-10
---

# Phase 4 Plan 01: Migrate VTODO to VEVENT using ical4j Summary

**Complete VTODO-to-VEVENT migration using ical4j 4.0.8 — workouts export as all-day calendar events importable by Apple, Google, Yahoo, and Outlook**

## Performance

- **Duration:** 8 min
- **Started:** 2026-03-10T08:20:00Z
- **Completed:** 2026-03-10T08:28:00Z
- **Tasks:** 4
- **Files modified:** 8

## Accomplishments
- Replaced hand-rolled ICS string builder with ical4j 4.0.8 for RFC 5545 compliance
- Migrated from VTODO (reminder) to VEVENT (calendar event) all-day format
- Added CALSCALE:GREGORIAN, TRANSP:TRANSPARENT, STATUS:CONFIRMED for universal compatibility
- Duration hint appended to SUMMARY (e.g. "🚴 Costa Blanca (36 min)")
- Updated all 82 tests — all passing with new VEVENT assertions

## Task Commits

Each task was committed atomically:

1. **Task 1: Add ical4j dependency** - `067e471` (chore)
2. **Task 2: Rewrite IcsBuilder VTODO→VEVENT** - `63faec8` (feat)
3. **Task 3: Migrate all tests to VEVENT** - `f238c89` (test)
4. **Task 4: Update email body and comments** - `cf4aeca` (fix)

## Files Created/Modified
- `build.gradle.kts` - Added ical4j 4.0.8 dependency
- `src/main/kotlin/nesski/de/ics/IcsBuilder.kt` - Complete rewrite: VTODO→VEVENT, ical4j CalendarOutputter
- `src/main/kotlin/nesski/de/ics/SportEmoji.kt` - Comment update VTODO→VEVENT
- `src/main/kotlin/nesski/de/models/Models.kt` - KDoc comments updated to VEVENT terminology
- `src/main/kotlin/nesski/de/email/EmailService.kt` - Body text references calendar events, not reminders
- `src/main/kotlin/nesski/de/services/web/PlansService.kt` - Query comments updated
- `src/test/kotlin/nesski/de/ics/IcsBuilderTest.kt` - All assertions migrated to VEVENT
- `src/test/kotlin/nesski/de/ics/SummaryFormattingTest.kt` - Duration hint in expected values
- `src/test/kotlin/nesski/de/email/EmailServiceTest.kt` - Sample ICS updated to VEVENT

## Decisions Made
- ical4j 4.0.8 chosen (latest stable, Java time API support)
- `Version(ParameterList(), Version.VALUE_2_0)` constructor to avoid Kotlin overload ambiguity
- `CalendarOutputter(false)` to skip internal validation (we control structure)
- unfoldIcs test helper handles RFC 5545 line folding at 75 chars

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed ical4j 4.x API differences from documented 3.x examples**
- **Found during:** Task 2 (IcsBuilder rewrite)
- **Issue:** ical4j 4.x removed `Version.VERSION_2_0` and `CalScale.GREGORIAN` pre-built instances. Uses `VALUE_2_0`/`VALUE_GREGORIAN` string constants instead.
- **Fix:** Used `Version(ParameterList(), Version.VALUE_2_0)` and `CalScale(CalScale.VALUE_GREGORIAN)` constructors
- **Files modified:** src/main/kotlin/nesski/de/ics/IcsBuilder.kt
- **Verification:** Compilation passes, output contains correct VERSION:2.0 and CALSCALE:GREGORIAN
- **Committed in:** 63faec8

**2. [Rule 1 - Bug] Fixed line-folding assertion failures in tests**
- **Found during:** Task 3 (test migration)
- **Issue:** ical4j CalendarOutputter folds lines at 75 chars per RFC 5545. DESCRIPTION content was split across lines, breaking `contains()` assertions.
- **Fix:** Added `unfoldIcs()` test helper that removes continuation lines before asserting
- **Files modified:** src/test/kotlin/nesski/de/ics/IcsBuilderTest.kt
- **Verification:** All 82 tests pass
- **Committed in:** f238c89

---

**Total deviations:** 2 auto-fixed (1 blocking, 1 bug)
**Impact on plan:** Both fixes necessary for correctness. No scope creep.

## Issues Encountered
None beyond the auto-fixed deviations above.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- VEVENT export complete and universally compatible
- Future phases can add: timed default start times, VALARM notifications, timezone-aware scheduling
- ical4j foundation enables all future calendar enhancements

## Self-Check: PASSED

- All 9 files verified present on disk
- All 4 commit hashes verified in git log (067e471, 63faec8, f238c89, cf4aeca)
- All 82 tests passing (BUILD SUCCESSFUL)
- EXPORT-02 requirement marked complete

---
*Phase: 04-calendar-export-v2*
*Completed: 2026-03-10*
