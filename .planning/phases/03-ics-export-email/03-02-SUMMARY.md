---
phase: 03-ics-export-email
plan: 02
subsystem: email
tags: [smtp, ics, email, simple-java-mail, fallback]

# Dependency graph
requires:
  - phase: 03-ics-export-email
    provides: IcsBuilder and IcsBuildResult for generating VCALENDAR content
provides:
  - EmailService for SMTP delivery of .ics attachments
  - IcsFileWriter for disk-based .ics file output
  - Config-driven email settings (TOML + env var overrides)
  - CLI integration wiring IcsBuilder into WahooCli
affects: [cli-usage, deployment]

# Tech tracking
tech-stack:
  added: [simple-java-mail 8.12.4]
  patterns: [config-driven email with env var overrides, disk fallback on email failure, extracted file writer singleton]

key-files:
  created:
    - src/main/kotlin/nesski/de/email/EmailService.kt
    - src/main/kotlin/nesski/de/ics/IcsFileWriter.kt
    - src/test/kotlin/nesski/de/email/EmailServiceTest.kt
    - src/test/kotlin/nesski/de/cli/IcsExportIntegrationTest.kt
  modified:
    - build.gradle.kts
    - src/main/kotlin/nesski/de/cli/WahooCli.kt
    - src/main/kotlin/nesski/de/config/AppConfig.kt
    - src/main/resources/config.toml

key-decisions:
  - "Simple Java Mail 8.12.4 for SMTP — lightweight, no Jakarta EE dependencies"
  - "Env var overrides for SMTP credentials (SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM, SMTP_TO)"
  - "EmailConfig.enabled defaults to false — opt-in email delivery"
  - "Disk fallback: on email failure, .ics saved to configured output.ics_save_path"
  - "IcsFileWriter extracted as standalone singleton for testable file I/O"

patterns-established:
  - "Email fallback pattern: try email → on failure save to disk with error message"
  - "Config env var override pattern: TOML value used unless env var present"

requirements-completed: [EXPORT-01]

# Metrics
duration: 5min
completed: 2026-03-09
---

# Phase 3 Plan 2: SMTP Email with .ics Attachment Summary

**SMTP email delivery of .ics workout plans via Simple Java Mail with disk-save fallback on failure**

## Performance

- **Duration:** 5 min
- **Started:** 2026-03-09T16:13:54Z
- **Completed:** 2026-03-09T16:19:10Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- EmailService sends .ics attachments via SMTP with config-driven settings
- WahooCli fully wired: fetch workouts → build ICS → email or save to disk
- Robust failure handling: validation errors, SMTP failures, and disabled email all produce clear messages
- IcsFileWriter handles disk output with auto-directory creation and UTF-8 encoding
- 13 new tests across EmailServiceTest (7) and IcsExportIntegrationTest (6)

## Task Commits

Each task was committed atomically:

1. **Task 1: SMTP email sending with .ics attachment** - `e0767ee` (feat)
2. **Task 2: Failure handling with disk fallback and tests** - `91c250e` (feat)

## Files Created/Modified
- `src/main/kotlin/nesski/de/email/EmailService.kt` - SMTP email sending with .ics attachment via Simple Java Mail
- `src/main/kotlin/nesski/de/ics/IcsFileWriter.kt` - Disk file writer with directory auto-creation
- `src/test/kotlin/nesski/de/email/EmailServiceTest.kt` - 7 tests for email validation, SMTP failure, result types
- `src/test/kotlin/nesski/de/cli/IcsExportIntegrationTest.kt` - 6 tests for config, file writing, UTF-8, paths
- `build.gradle.kts` - Added simple-java-mail 8.12.4 dependency
- `src/main/kotlin/nesski/de/cli/WahooCli.kt` - Wired IcsBuilder + EmailService into CLI flow
- `src/main/kotlin/nesski/de/config/AppConfig.kt` - Added EmailConfig data class with SMTP settings
- `src/main/resources/config.toml` - Added [email] section with SMTP configuration template

## Decisions Made
- Simple Java Mail 8.12.4 chosen for SMTP — lightweight, mature, no Jakarta EE server needed
- Environment variable overrides for sensitive SMTP values (SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM, SMTP_TO)
- Email disabled by default (enabled=false) — user must opt-in via config
- On email failure: save .ics to disk at configured path, emit error message, continue execution
- IcsFileWriter extracted as a separate singleton for testability (avoid Clikt context dependency in tests)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Extracted IcsFileWriter for testable file I/O**
- **Found during:** Task 2 (failure handling tests)
- **Issue:** WahooCli.saveIcsToDisk() used Clikt's echo() which requires runtime context, making unit tests fail with IllegalStateException
- **Fix:** Extracted file writing logic to standalone IcsFileWriter object, WahooCli delegates to it
- **Files modified:** WahooCli.kt, IcsFileWriter.kt (new)
- **Verification:** All 80 tests pass including 6 new file I/O tests
- **Committed in:** 91c250e (task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Necessary extraction for testability. No scope creep.

## Issues Encountered
None

## User Setup Required
None - email is disabled by default. Users configure SMTP in config.toml or via env vars when ready.

## Next Phase Readiness
- Phase 3 complete: ICS builder, sport emoji, email delivery, disk fallback all implemented
- All 80 tests pass (45 from plan 01 + 35 existing + 13 new in plan 02 = 80 total across project)
- Ready for milestone completion

## Self-Check: PASSED

- All 4 created files verified on disk
- Both task commits (e0767ee, 91c250e) verified in git history

---
*Phase: 03-ics-export-email*
*Completed: 2026-03-09*
