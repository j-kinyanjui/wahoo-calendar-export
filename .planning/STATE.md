# State: Wahoo Plan to Calendar

## Project Reference

**Core Value:** Allow users to view their Wahoo/Systm training plans in their personal calendar for better workout scheduling and tracking.

**Current Focus:** Phase 3 - ICS Export & Email

---

## Current Position

| Field | Value |
|-------|-------|
| **Phase** | 3 - ICS Export & Email |
| **Current Plan** | 2 of 2 |
| **Status** | In progress |
| **Progress** | Plan 1 complete, Plan 2 remaining |

---

## Performance Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| v1 Requirements | 16 | All mapped to phases |
| Phases | 2 | Quick depth approach |
| Current Phase Progress | 1/2 | Plan 1 complete |
| Blockers | 0 | None identified |

| Phase-Plan | Duration | Tasks | Files |
|------------|----------|-------|-------|
| Phase 01-authentication-graphql-setup P03 | 3 min | 3 tasks | 4 files |
| Phase 02-cli-migration-plan-export P01 | 21 min | 2 tasks | 6 files |
| Phase 02-cli-migration-plan-export P02 | 2 min | 2 tasks | 5 files |
| Phase 02-cli-migration-plan-export P03 | 1 min | 1 tasks | 2 files |
| Phase 02-cli-migration-plan-export P04 | 1 min | 1 tasks | 1 files |
| Phase 03-ics-export-email P01 | 4 min | 2 tasks | 5 files |

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

### Technical Notes

- Uses `graphql-kotlin-ktor-client` 8.x.x for GraphQL
- JWT handling via `java-jwt` 4.5.1
- Bearer token in Authorization header
- GraphQL endpoint: https://api.thesufferfest.com/graphql
- Key query: GetUserPlansRange

### Pending Todos

No pending todos.

### Known Risks

1. JWT token security during storage - mitigated via encrypted sessions
2. Expired token handling - parse `exp` claim, provide clear error
3. GraphQL errors in 200 OK responses - must parse `errors` field

---

## Session Continuity

### What's Been Done

- Project initialized with core value defined
- 16 v1 requirements documented across 5 categories (added CLI section)
- Research completed on GraphQL Kotlin client and JWT handling
- Roadmap created with 2 phases
- Phase 1 Plan 01 completed: Session infrastructure + GraphQL client
- Phase 1 Plan 02 completed: Config-based auth with auto-login
- Phase 1 Plan 03 completed: GetUserPlansRange query + error handling
- Phase 2 Plan 01 completed: CLI migration — build, entry point, config, date range parser
- Phase 2 Plan 02 completed: CLI wired end-to-end — auth, fetch, console display, server cleanup
- Phase 2 Plan 03 completed: Gap closure — added CLI-01/CLI-02 requirement definitions and traceability
- Phase 2 Plan 04 completed: Gap closure — changed default --config path to ~/.config/wahoo-cli/config
- Phase 3 Plan 01 completed: IcsBuilder + SportEmoji — RFC 5545 VTODO builder with sport emoji mapping

### What's Next

- Phase 3 Plan 01 complete: IcsBuilder + SportEmoji with 45 tests
- Next: Phase 3 Plan 02 — CLI integration (wire IcsBuilder into WahooCli, file I/O)

### User Preferences

- Manual JWT input acceptable (no OAuth flow needed)
- On-demand/daily sync (not real-time)
- CLI app, not a web server
- Apple Reminders integration via VTODO .ics tasks
- ICS file-based export (email delivery deferred to future phase)

---

*State updated: 2026-03-09 (Phase 3 Plan 01 complete)*
