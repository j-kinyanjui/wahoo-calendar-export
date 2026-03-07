# State: Wahoo Plan to Calendar

## Project Reference

**Core Value:** Allow users to view their Wahoo/Systm training plans in their personal calendar for better workout scheduling and tracking.

**Current Focus:** Phase 2 - CLI Migration & Plan Export

---

## Current Position

| Field | Value |
|-------|-------|
| **Phase** | 1 - Authentication & GraphQL Setup |
| **Plan** | 01-03 complete |
| **Status** | Complete |
| **Progress** | ██████████ 100% |

---

## Performance Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| v1 Requirements | 14 | All mapped to phases |
| Phases | 2 | Quick depth approach |
| Current Phase Progress | 3/3 | All plans complete |
| Blockers | 0 | None identified |

---
| Phase 01-authentication-graphql-setup P03 | 3 min | 3 tasks | 4 files |

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
| Migrate to CLI (drop Ktor server) | No server routes needed; app is a batch/CLI process | Decided for Phase 2 |
| Clikt for CLI framework | Kotlin-native, subcommands, typed options, clean API | Decided for Phase 2 |
| Single command flow | Auto-login + fetch + export in one invocation | Decided for Phase 2 |
| VTODO .ics for Apple Reminders | Workouts appear as tasks with due dates | Decided for Phase 2 |
| Email .ics delivery | Send generated .ics to user-provided email via SMTP | Decided for Phase 2 |
| Env vars + config file for creds | SYSTM_USER/SYSTM_PASSWORD env vars, fallback to ~/.wahoo-cli/config | Decided for Phase 2 |
| Relative + absolute time ranges | Support now/1w/1m/2m shorthand and --from/--to dates, max 2 months | Decided for Phase 2 |

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
- 14 v1 requirements documented across 4 categories
- Research completed on GraphQL Kotlin client and JWT handling
- Roadmap created with 2 phases
- Phase 1 Plan 01 completed: Session infrastructure + GraphQL client
- Phase 1 Plan 02 completed: Config-based auth with auto-login
- Phase 1 Plan 03 completed: GetUserPlansRange query + error handling

### What's Next

- Phase 2: CLI Migration & Plan Export
  - Remove Ktor server, migrate to Clikt CLI
  - Single command: auto-login, fetch plans, generate .ics VTODO, email to user
  - Time range support: now, 1w, 2w, 1m, 2m, or explicit --from/--to (max 2 months)

### User Preferences

- Manual JWT input acceptable (no OAuth flow needed)
- On-demand/daily sync (not real-time)
- CLI app, not a web server
- Apple Reminders integration via VTODO .ics tasks
- Email delivery of .ics files

---

*State updated: 2026-03-07*
