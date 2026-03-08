---
phase: 02-cli-migration-plan-export
verified: 2026-03-08T12:00:00Z
status: gaps_found
score: 8/9 must-haves verified
re_verification: false
gaps:
  - truth: "CLI-01 and CLI-02 requirements are defined and traceable"
    status: failed
    reason: "PLAN frontmatter references CLI-01, CLI-02 but these IDs do not exist in REQUIREMENTS.md"
    artifacts: []
    missing:
      - "Add CLI-01 and CLI-02 requirement definitions to REQUIREMENTS.md (e.g., CLI-01: Application runs as CLI via Clikt, CLI-02: CLI accepts --range/--from/--to/--config options)"
      - "Add CLI-01, CLI-02 to the traceability table in REQUIREMENTS.md mapped to Phase 2"
  - truth: "First-run prompts for credentials when none found"
    status: partial
    reason: "promptForCredentials() collects credentials but does NOT actually use them — it prints 'Re-run the command to fetch plans' and returns without running auth/fetch. The TODO on line 122 confirms credential saving is not implemented."
    artifacts:
      - path: "src/main/kotlin/nesski/de/cli/WahooCli.kt"
        issue: "promptForCredentials() at line 111 returns early without executing the auth→fetch→display flow. Credentials are collected then discarded."
    missing:
      - "After prompting, use the collected credentials to run the auth→fetch→display flow in the same invocation (or at minimum save them to config)"
      - "Implement the TODO at line 122: write credentials to config file when user confirms save"
human_verification:
  - test: "Run ./gradlew run --args='--range 2w' with valid SYSTM_USER/SYSTM_PASSWORD env vars"
    expected: "CLI authenticates, fetches plans, and displays formatted workout list with name, date, type, status"
    why_human: "Requires real Wahoo SYSTM credentials and network access to API"
  - test: "Run ./gradlew run --args='--range 2w' with invalid credentials"
    expected: "Clear error message: '✗ Authentication failed: ...' and non-zero exit code"
    why_human: "Requires network access and auth service response"
  - test: "Run ./gradlew run with no env vars and no config file"
    expected: "Interactive credential prompt appears for username and password"
    why_human: "Requires interactive terminal input"
---

# Phase 2: CLI Migration Verification Report

**Phase Goal:** Migrate from Ktor server to a Clikt-based CLI application that auto-authenticates, fetches training plans for a user-specified time range, and displays them in the console
**Verified:** 2026-03-08T12:00:00Z
**Status:** gaps_found
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Ktor server dependencies removed (server-core, netty, auth, config-yaml) | ✓ VERIFIED | build.gradle.kts has no `ktor-server` dependencies. grep for `ktor-server` in build file returns nothing. Only Ktor client deps remain. |
| 2 | Application runs as a CLI via Clikt with `--range`, `--from`/`--to`, `--config` options | ✓ VERIFIED | WahooCli.kt extends CliktCommand with all 4 options (lines 26-30). Application.kt delegates to `WahooCli().main(args)`. `./gradlew compileKotlin` succeeds. |
| 3 | Credentials loaded from env vars (`SYSTM_USER`, `SYSTM_PASSWORD`) with fallback to `~/.config/wahoo-cli/config` | ✓ VERIFIED | AppConfig.resolveCredentials() checks `System.getenv("SYSTM_USER")` and `System.getenv("SYSTM_PASSWORD")` with fallback to config values (lines 43-46). WahooCli.run() loads config then resolves credentials (lines 37-40). |
| 4 | Auto-login on each run using existing SystmAuthService | ✓ VERIFIED | WahooCli.run() creates `SystmAuthService(wahooHttpClient, username, password)` and calls `authService.login()` (lines 59-60). Token stored and used for subsequent fetch. |
| 5 | Plans fetched for specified range: `now`, `1w`, `2w`, `1m`, `2m`, or explicit `--from`/`--to` (max 2 months) | ✓ VERIFIED | DateRangeParser.kt handles all shorthands (lines 31-41), --from/--to parsing (lines 45-57), max 2-month validation (line 51), and mutual exclusion (lines 23-27). Default is 2 weeks (line 61). |
| 6 | Application extracts workout name, date, type, and status from GraphQL response | ✓ VERIFIED | Models.kt: Plan has name, status, type, workouts fields. Workout has name, scheduledDate, type, status. WahooCli.kt displays all four fields at line 98. |
| 7 | User sees formatted list of workouts in the console | ✓ VERIFIED | WahooCli.kt lines 87-103: header with date range, per-plan output with emoji and status, per-workout line with name/date/type/status, summary count. Empty plans case handled. |
| 8 | nginx/Docker infrastructure removed | ✓ VERIFIED | `ls infra/` returns error (directory gone). `ls src/main/resources/application.yaml` returns error (deleted). `ls src/main/kotlin/nesski/de/modules/` returns error (deleted). logback.xml retained. |
| 9 | Clear error messages for auth failures, API errors, and invalid ranges | ✓ VERIFIED | WahooCli.kt: auth failures echo "✗ Authentication failed" + ProgramResult(1) (lines 63, 70-71). API errors echo "✗ API error" (line 79). Date range errors wrapped in UsageError (line 52). |

**Score:** 9/9 success criteria truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `build.gradle.kts` | Gradle build with Clikt + ktoml, no Ktor server deps | ✓ VERIFIED | Contains `com.github.ajalt.clikt:clikt:5.0.3`, `ktoml-core`, `ktoml-file`. No ktor-server deps. No biweekly/SimpleJavaMail. |
| `src/main/kotlin/nesski/de/Application.kt` | CLI main entry point | ✓ VERIFIED | 6 lines. `fun main(args)` delegates to `WahooCli().main(args)`. |
| `src/main/kotlin/nesski/de/cli/WahooCli.kt` | Complete CLI orchestration: auth, fetch, display | ✓ VERIFIED | 127 lines. CliktCommand with all options. run() implements auth→fetch→display with error handling. |
| `src/main/kotlin/nesski/de/config/AppConfig.kt` | TOML config with env var overrides | ✓ VERIFIED | 61 lines. @Serializable data classes. load() with graceful defaults. resolveCredentials() with env var override. |
| `src/main/kotlin/nesski/de/utils/DateRangeParser.kt` | Date range parsing from CLI options | ✓ VERIFIED | 62 lines. parseDateRange() with shorthand, ISO dates, mutual exclusion, max 2-month validation. |
| `src/main/kotlin/nesski/de/services/web/AuthService.kt` | Auth service with SLF4J logger | ✓ VERIFIED | Uses `LoggerFactory.getLogger("SystmAuthService")`. No Ktor server or KtorSimpleLogger imports. |
| `src/main/kotlin/nesski/de/services/web/PlansService.kt` | Plans service with SLF4J logger | ✓ VERIFIED | Uses `LoggerFactory.getLogger("SystmPlansService")`. No Ktor server or KtorSimpleLogger imports. |
| `src/main/kotlin/nesski/de/modules/WahooSystmWeb.kt` | DELETED | ✓ VERIFIED | Directory `modules/` no longer exists. |
| `src/main/resources/application.yaml` | DELETED | ✓ VERIFIED | File does not exist. |
| `infra/nginx/docker-compose.yml` | DELETED | ✓ VERIFIED | Directory `infra/` no longer exists. |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| Application.kt | WahooCli.kt | `main()` calls `WahooCli().main(args)` | ✓ WIRED | Line 6: `fun main(args: Array<String>) = WahooCli().main(args)` |
| WahooCli.kt | AppConfig.kt | `run()` loads config via `AppConfig.load()` | ✓ WIRED | Import at line 13, `AppConfig.load()` at line 37, `AppConfig.resolveCredentials()` at line 40 |
| WahooCli.kt | DateRangeParser.kt | `run()` calls `parseDateRange()` | ✓ WIRED | Import at line 18, `parseDateRange(range, from, to)` at line 50 |
| WahooCli.kt | AuthService.kt | Creates `SystmAuthService` and calls `login()` | ✓ WIRED | Import at line 16, `SystmAuthService(wahooHttpClient, username, password)` at line 59, `authService.login()` at line 60, token stored and used |
| WahooCli.kt | PlansService.kt | Creates `SystmPlansService` and calls `fetchPlans()` | ✓ WIRED | Import at line 17, `SystmPlansService(wahooHttpClient)` at line 76, `plansService.fetchPlans(token, dateRange.start, dateRange.end)` at line 77, result iterated for display |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|------------|-------------|--------|----------|
| PARSE-01 | 02-02 | Extracts workout name from GraphQL response | ✓ SATISFIED | Workout model has `name` field (Models.kt:51). WahooCli displays `workout.name` (line 98). |
| PARSE-02 | 02-02 | Extracts planned date from GraphQL response | ✓ SATISFIED | Workout model has `scheduledDate` field (Models.kt:54). WahooCli displays `workout.scheduledDate` (line 98). |
| PARSE-03 | 02-02 | Handles workout types (type field) | ✓ SATISFIED | Workout model has `type` field (Models.kt:58). WahooCli displays `workout.type` (line 98). |
| PARSE-04 | 02-02 | Handles workout status (completed, planned, etc.) | ✓ SATISFIED | Workout model has `status` field (Models.kt:60). WahooCli displays `workout.status` (line 98). |
| DISP-01 | 02-02 | User can view list of fetched training plans | ✓ SATISFIED | WahooCli.run() iterates plans and workouts (lines 95-101), displays formatted output. |
| DISP-02 | 02-02 | Each plan shows name and scheduled date | ✓ SATISFIED | Plan header shows name and status (line 96). Workout lines show name and scheduledDate (line 98). |
| DISP-03 | 02-02 | Displays error messages for failed requests | ✓ SATISFIED | Auth failures (lines 63, 70-71), GraphQL errors (line 79), general errors (line 82) all display clear messages. |
| CLI-01 | 02-01 | (NOT IN REQUIREMENTS.md) | ⚠️ ORPHANED | Requirement ID referenced in PLAN but has no definition in REQUIREMENTS.md. Appears to cover "CLI as application entry point" which IS implemented. |
| CLI-02 | 02-01 | (NOT IN REQUIREMENTS.md) | ⚠️ ORPHANED | Requirement ID referenced in PLAN but has no definition in REQUIREMENTS.md. Appears to cover "CLI options and config" which IS implemented. |

**Note:** CLI-01 and CLI-02 are referenced in the ROADMAP.md phase requirements and in 02-01-PLAN.md, but these IDs are never defined in REQUIREMENTS.md. The implementation satisfies what these requirements likely describe, but the traceability chain is broken — there is no authoritative definition to verify against.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| WahooCli.kt | 122 | `// TODO: Write credentials to config file` | ⚠️ Warning | Credential save flow collects credentials but discards them. User is told to "Re-run the command." This is a UX gap but not a blocker for the core auth→fetch→display flow since env vars and config file reading work. |

### Human Verification Required

### 1. End-to-End Auth + Fetch + Display

**Test:** Run `SYSTM_USER=<email> SYSTM_PASSWORD=<pass> ./gradlew run --args="--range 2w"`
**Expected:** Authenticated message, followed by formatted workout list with names, dates, types, and statuses
**Why human:** Requires real Wahoo SYSTM credentials and network access to live API

### 2. Auth Failure Message

**Test:** Run `SYSTM_USER=bad SYSTM_PASSWORD=bad ./gradlew run --args="--range 2w"`
**Expected:** Clear error: `✗ Authentication failed: ...` and non-zero exit code
**Why human:** Requires network access and auth service response

### 3. Interactive Credential Prompt

**Test:** Run `./gradlew run` with no env vars and no config file
**Expected:** Interactive prompt for username and password appears
**Why human:** Requires interactive terminal input

### Gaps Summary

**Two categories of gaps found:**

1. **Requirements traceability (CLI-01, CLI-02):** The ROADMAP and PLAN files reference CLI-01 and CLI-02 requirement IDs, but these are never defined in REQUIREMENTS.md. The functionality these likely represent IS implemented, but there's a documentation gap in the requirements traceability chain. This needs CLI-01 and CLI-02 added to REQUIREMENTS.md with definitions and traceability entries.

2. **Credential prompt flow (minor):** The first-run credential prompt collects username/password but does not use them to proceed with auth→fetch→display. It also has a TODO for saving to config file. This is a minor UX gap — the core flow works via env vars and config file. The prompt path is an edge case for users who have neither configured.

**Impact assessment:** The core phase goal — "CLI application that auto-authenticates, fetches training plans, and displays them" — is functionally achieved via env vars and TOML config. All 9 ROADMAP success criteria are met. The gaps are documentation (missing requirement definitions) and a minor UX edge case (first-run prompt), not core functionality blockers.

**ROADMAP staleness note:** ROADMAP.md shows `- [ ] 02-02-PLAN.md` (incomplete) but STATE.md and 02-02-SUMMARY.md confirm it was executed. ROADMAP progress checkboxes need updating.

---

_Verified: 2026-03-08T12:00:00Z_
_Verifier: OpenCode (gsd-verifier)_
