---
gsd_state_version: 1.0
milestone: v1.1
milestone_name: CI/CD Pipeline
status: defining_requirements
last_updated: "2026-03-13T19:30:00.000Z"
progress:
  total_phases: 0
  completed_phases: 0
  total_plans: 0
  completed_plans: 0
---

# State: Wahoo Plan to Calendar

## Project Reference

**Core Value:** Simple, non-interactive CLI access to training plans with instant calendar export.

**Current Focus:** Milestone v1.1 — CI/CD Pipeline

---

## Current Position

| Field | Value |
|-------|-------|
| **Phase** | Not started (defining requirements) |
| **Current Plan** | — |
| **Status** | Defining requirements |
| **Last Activity** | 2026-03-13 — Milestone v1.1 started |

---

## Performance Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| v1.1 Requirements | TBD | Being defined |
| Phases | TBD | Pending roadmap |
| Blockers | 0 | None |

## Accumulated Context

### Key Decisions

| Decision | Rationale | Status |
|----------|-----------|--------|
| Manual JWT input for Systm | Simpler than implementing OAuth | Implemented in 01-01 |
| On-demand sync | Not a real-time use case | Pending implementation |
| 2-phase structure | Quick depth = 1-2 phases max | Defined |
| Cookie-based session storage | Uses Ktor Sessions with encrypted cookies | Implemented in 01-01 |
| Bearer token auth for GraphQL | Standard approach for JWT in Authorization header | Implemented in 01-01 |
| Config-based auth for Systm | YAML file for credentials, auto-login on startup | Implemented in 01-02 |
| Default date range for plans | Past 7 + next 14 days for practical workout planning | Implemented in 01-03 |
| GraphQLException on errors | Handle HTTP 200 with errors field properly | Implemented in 01-03 |
| Migrate to CLI (drop Ktor server) | No server routes needed; app is a batch/CLI process | Implemented in 02-01 |
| Clikt 5.0.3 for CLI framework | Kotlin-native, typed options, clean API; 5.x API differs from 4.x | Implemented in 02-01 |
| Single command flow | Auto-login + fetch + export in one invocation | Decided for Phase 2 |
| VTODO .ics for Apple Reminders | Workouts appear as tasks with due dates | Decided for Phase 2 |
| Email .ics delivery | Send generated .ics to user-provided email via SMTP | Decided for Phase 2 |
| TOML config at ~/.config/wahoo-cli/config | XDG convention, env var overrides, graceful defaults | Implemented in 02-01 |
| Relative + absolute time ranges | now/1w/2w/1m/2m shorthand and --from/--to dates, max 2 months | Implemented in 02-01 |
| Serialization plugin version match | Updated from 1.4.32 to 2.3.10 to match Kotlin version | Implemented in 02-01 |
| runBlocking for CLI suspend bridging | Ktor client calls are suspend; runBlocking bridges to Clikt sync run() | Implemented in 02-02 |
| ProgramResult for exit codes | Clikt's recommended way to exit with non-zero code | Implemented in 02-02 |
| SLF4J for service logging | Decouple from Ktor server utilities; logback-classic provides SLF4J | Implemented in 02-02 |
| CLI-01/CLI-02 marked complete | Functionality already implemented; definitions were missing from REQUIREMENTS.md | Documented in 02-03 |
| Default --config path to ~/.config/wahoo-cli/config | Match CLI-02 spec; XDG convention for user config | Implemented in 02-04 |
| ICS export file-based only (no email) | Simplify Phase 3 to focus on .ics generation; email delivery deferred | Decided for Phase 3 |
| Combined .ics file per run | All workouts in single file rather than one per workout | Decided for Phase 3 |
| Filename format with range | workouts_{range}_{date}.ics shows date range fetched | Decided for Phase 3 |
| Save to current directory by default | CWD is default, configurable via config file | Decided for Phase 3 |
| VTODO with date-only due dates | DUE-DATE format, no time component for Apple Reminders | Decided for Phase 3 |
| Error summary + detailed log | Console summary of successes/failures; separate error log file | Decided for Phase 3 |
| Object singletons for IcsBuilder/SportEmoji | Stateless builders need no instantiation | Implemented in 03-01 |
| IcsBuildResult with export statistics | Track exported/skipped counts + reasons for console reporting | Implemented in 03-01 |
| UID priority: agendaId > workoutId > generated | Best available unique ID for each VTODO entry | Implemented in 03-01 |
| 10 sport-to-emoji mappings | Cover all known Wahoo SYSTM workout types | Implemented in 03-01 |
| Simple Java Mail for SMTP | Lightweight, no Jakarta EE server needed | Implemented in 03-02 |
| Env var overrides for SMTP creds | SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM, SMTP_TO | Implemented in 03-02 |
| Email disabled by default | Opt-in via config.toml email.enabled=true | Implemented in 03-02 |
| Disk fallback on email failure | Save .ics to configured path when SMTP fails | Implemented in 03-02 |
| IcsFileWriter extracted singleton | Testable file I/O without Clikt context | Implemented in 03-02 |
| ical4j 4.0.8 for ICS generation | RFC 5545 compliance, replaces hand-rolled ICS strings | Implemented in 04-01 |
| VEVENT all-day events | DATE-only DTSTART/DTEND — users drag to preferred time | Implemented in 04-01 |
| TRANSP:TRANSPARENT for all-day events | Don't block calendar when events are all-day placeholders | Implemented in 04-01 |
| UID @wahoo suffix | Domain uniqueness per RFC 5545 | Implemented in 04-01 |
| Duration hint in SUMMARY text | Communicates workout length without enforcing it | Implemented in 04-01 |

### Technical Notes

- Uses `graphql-kotlin-ktor-client` 8.x.x for GraphQL
- JWT handling via `java-jwt` 4.5.1
- Bearer token in Authorization header
- GraphQL endpoint: https://api.thesufferfest.com/graphql
- Key query: GetUserPlansRange
- ICS generation via ical4j 4.0.8 (VEVENT all-day events, CalendarOutputter)
- ical4j 4.x API: `Version(ParameterList(), VALUE_2_0)`, `CalScale(VALUE_GREGORIAN)` — differs from 3.x docs

### Pending Todos

1. **Add GitHub Action to build and package app** (area: tooling) — `.planning/todos/pending/2026-03-13-add-github-action-to-build-and-package-app.md`

### Known Risks

1. JWT token security during storage - mitigated via encrypted sessions
2. Expired token handling - parse `exp` claim, provide clear error
3. GraphQL errors in 200 OK responses - must parse `errors` field

---

## Session Continuity

### What's Been Done

- v1.0 MVP shipped (4 phases, 10 plans, 17 requirements)
- Milestone v1.1 started: CI/CD Pipeline

### What's Next

- Define v1.1 requirements
- Create roadmap
- Plan and execute CI/CD phases

### User Preferences

- Manual JWT input acceptable (no OAuth flow needed)
- On-demand/daily sync (not real-time)
- CLI app, not a web server
- VEVENT calendar events via ical4j (replaces VTODO reminders)
- Cross-calendar compatible: Apple, Google, Outlook, Yahoo

---

*State updated: 2026-03-13 (Milestone v1.1 started — CI/CD Pipeline)*
