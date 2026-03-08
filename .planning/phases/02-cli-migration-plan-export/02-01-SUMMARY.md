---
phase: 02-cli-migration-plan-export
plan: 01
subsystem: cli
tags: [clikt, ktoml, toml, cli, gradle, kotlin]

# Dependency graph
requires:
  - phase: 01-authentication-graphql-setup
    provides: GraphQL client, auth service, plan models
provides:
  - CLI entry point with Clikt command framework
  - TOML config loading with env var overrides
  - Date range parsing (shorthand and ISO dates)
  - Credential prompting for first-run experience
affects: [02-02-PLAN]

# Tech tracking
tech-stack:
  added: [clikt 5.0.3, ktoml-core 0.7.0, ktoml-file 0.7.0]
  patterns: [CliktCommand with override help(), Mordant terminal prompting, TOML config with @Serializable data classes, env var override precedence]

key-files:
  created:
    - src/main/kotlin/nesski/de/cli/WahooCli.kt
    - src/main/kotlin/nesski/de/config/AppConfig.kt
    - src/main/kotlin/nesski/de/utils/DateRangeParser.kt
  modified:
    - build.gradle.kts
    - src/main/kotlin/nesski/de/Application.kt

key-decisions:
  - "Used Clikt 5.0.3 API (constructor takes only name, help is override method, main() is extension function)"
  - "Deleted WahooSystmWeb.kt module — blocking Ktor server references removed"
  - "Updated kotlin serialization plugin version from 1.4.32 to 2.3.10 to match Kotlin version"
  - "Used Mordant StringPrompt/YesNoPrompt for interactive credential prompting instead of Clikt prompt()"

patterns-established:
  - "Clikt 5.x pattern: override fun help(context: Context) for command help text"
  - "Config loading: AppConfig.load() with graceful fallback to defaults"
  - "Credential resolution: env vars > config file > interactive prompt"

requirements-completed: [CLI-01, CLI-02]

# Metrics
duration: 21min
completed: 2026-03-08
---

# Phase 2 Plan 1: CLI Migration & Build Setup Summary

**Clikt 5.0.3 CLI skeleton with TOML config loading (ktoml), date range parsing, and Gradle build migrated from Ktor server to standalone application**

## Performance

- **Duration:** 21 min
- **Started:** 2026-03-08T10:56:40Z
- **Completed:** 2026-03-08T11:17:51Z
- **Tasks:** 2
- **Files modified:** 6 (3 created, 2 modified, 1 deleted)

## Accomplishments
- Migrated build.gradle.kts from Ktor server plugin to standalone application plugin with Clikt + ktoml deps
- Created WahooCli CliktCommand with --range, --from/--to, --config options and first-run credential prompt
- Created AppConfig TOML loader with env var override precedence and graceful defaults
- Created DateRangeParser with shorthand support (now/1w/2w/1m/2m) and mutual exclusion validation

## Task Commits

Each task was committed atomically:

1. **Task 1: Migrate build.gradle.kts and rewrite Application.kt** - `a234040` (feat)
2. **Task 2: Create WahooCli, AppConfig, and DateRangeParser** - `fcff06e` (feat)

## Files Created/Modified
- `build.gradle.kts` - Removed Ktor server plugin/deps, added Clikt + ktoml, updated mainClass
- `src/main/kotlin/nesski/de/Application.kt` - CLI entry point delegating to WahooCli
- `src/main/kotlin/nesski/de/cli/WahooCli.kt` - CliktCommand with all CLI options and skeleton run()
- `src/main/kotlin/nesski/de/config/AppConfig.kt` - TOML config data classes with loading and env var overrides
- `src/main/kotlin/nesski/de/utils/DateRangeParser.kt` - Date range parsing with shorthand and mutual exclusion
- `src/main/kotlin/nesski/de/modules/WahooSystmWeb.kt` - DELETED (obsolete Ktor module)

## Decisions Made
- Used Clikt 5.0.3 API which differs significantly from 4.x (help is override method, main() is extension, prompt/confirm via Mordant terminal)
- Updated kotlin("plugin.serialization") version from 1.4.32 to 2.3.10 to match Kotlin compiler version
- Used Mordant StringPrompt/YesNoPrompt for first-run credential prompting (Clikt 5 moved prompt methods to Mordant)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Adapted to Clikt 5.0.3 API breaking changes**
- **Found during:** Task 2 (Create WahooCli)
- **Issue:** Plan used Clikt 4.x API patterns (help as constructor parameter, prompt/confirm as command methods, main() as instance method). Clikt 5.0.3 moved these to overrides and extensions.
- **Fix:** Used `override fun help(context: Context)`, `import com.github.ajalt.clikt.core.main`, Mordant `StringPrompt`/`YesNoPrompt` for interactive prompting
- **Files modified:** WahooCli.kt, Application.kt
- **Verification:** `./gradlew compileKotlin` succeeds, `./gradlew run --args="--help"` shows correct output
- **Committed in:** fcff06e

**2. [Rule 3 - Blocking] Deleted obsolete WahooSystmWeb.kt module**
- **Found during:** Task 2 (compilation after removing Ktor server deps)
- **Issue:** `modules/WahooSystmWeb.kt` imported `io.ktor.server.application.Application` and `io.ktor.util.logging.KtorSimpleLogger` which no longer exist after removing Ktor server dependencies
- **Fix:** Deleted the file and empty modules/ directory — functionality replaced by WahooCli.run()
- **Files modified:** src/main/kotlin/nesski/de/modules/WahooSystmWeb.kt (deleted)
- **Verification:** `./gradlew compileKotlin` succeeds
- **Committed in:** fcff06e

**3. [Rule 1 - Bug] Fixed serialization plugin version mismatch**
- **Found during:** Task 1 (build.gradle.kts migration)
- **Issue:** `kotlin("plugin.serialization") version "1.4.32"` mismatched with Kotlin `2.3.10` — flagged in research as Open Question #3
- **Fix:** Updated to `kotlin("plugin.serialization") version "2.3.10"`
- **Files modified:** build.gradle.kts
- **Verification:** Build succeeds with no compatibility warnings
- **Committed in:** a234040

---

**Total deviations:** 3 auto-fixed (1 bug, 2 blocking)
**Impact on plan:** All fixes necessary for compilation. Clikt 5.x API changes were the main deviation; the plan's code examples were based on 4.x patterns.

## Issues Encountered
None beyond the documented deviations above.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- CLI skeleton complete with all options, config loading, and date range parsing
- Ready for Plan 02: Wire auth service, plan fetching, and console display into WahooCli.run()
- Existing services (SystmAuthService, SystmPlansService, SystmGraphQLClient) are retained and ready to integrate

## Self-Check: PASSED

All created files verified on disk. All commit hashes verified in git log. Deleted file confirmed absent.

---
*Phase: 02-cli-migration-plan-export*
*Completed: 2026-03-08*
