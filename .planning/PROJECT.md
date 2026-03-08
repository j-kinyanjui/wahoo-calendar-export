# Wahoo Plan to Calendar

## What This Is

A command-line application that fetches Wahoo Systm training plans via GraphQL API and displays them in the console. Auto-authenticates using stored credentials or environment variables, supports flexible date range queries, and formats workouts by plan with key details (date, type, duration, status).

## Core Value

Simple, non-interactive CLI access to training plans — authenticate once, fetch plans for any date range, see them immediately formatted in console.

## Requirements

### Validated (v1.0)

- ✓ User authenticates via stored credentials or environment variables (SYSTM_USER, SYSTM_PASSWORD)
- ✓ Application runs as Clikt CLI with --range, --from/--to, --config options
- ✓ Credentials loaded from TOML config (~/.config/wahoo-cli/config) with env var overrides
- ✓ Training plans fetched from Systm GraphQL API (api.thesufferfest.com)
- ✓ Workouts parsed with name, date, type, status extracted from GraphQL response
- ✓ Formatted console display showing plan names and indented workout details
- ✓ Date range support: shorthand (now, 1w, 2w, 1m, 2m) and explicit (--from/--to YYYY-MM-DD)
- ✓ Maximum 2-month date range validation
- ✓ Clear error messages for auth failures and invalid input

### Active (v1.1+)

- [ ] ICS file export (.ics VTODO entries for Apple Reminders)
- [ ] Email delivery of workouts to provided address
- [ ] Custom date range presets (add to config)
- [ ] Quiet/verbose output modes

### Out of Scope

- Web UI — CLI-first approach
- Real-time sync — fetch on-demand only
- OAuth flow — manual token input acceptable

## Current State (v1.0 Shipped)

**Codebase:**
- 7 completed plans across 2 phases
- ~17k LOC added (Kotlin, build config, tests)
- 69 files modified, Ktor server removed, nginx/Docker removed

**Tech Stack:**
- Kotlin 1.9.23
- Clikt 5.0.3 for CLI framework
- ktoml 0.7.0 for TOML config parsing
- kotlinx.coroutines for async/await
- SLF4J for logging

**Architecture:**
- CLI entry point (WahooCli.kt) with Clikt command
- AppConfig for credential and config file management
- SystmAuthService for JWT token authentication
- PlansService for GraphQL plan fetching
- DateRangeParser for flexible date input

**Production Ready:**
- Credentials loaded from env vars or ~/.config/wahoo-cli/config (XDG convention)
- Auto-retry on auth, graceful error messages on auth/API failures
- All 16 v1.0 requirements validated and shipped

## Constraints

- **Tech Stack**: Kotlin/Ktor — existing, not changing
- **Token Handling**: Manual JWT input acceptable — no OAuth flow needed for Systm
- **Sync Frequency**: On-demand/daily — not real-time
- **Calendar Export**: TODO — deferred to future phase

## Key Decisions (v1.0)

| Decision                          | Rationale                                                   | Outcome        |
| --------------------------------- | ----------------------------------------------------------- | -------------- |
| CLI-first (no web UI)             | Simpler scope, faster iteration, easier testing             | ✓ Shipped v1.0 |
| Clikt framework                   | Mature, well-documented, supports Kotlin idioms             | ✓ Shipped v1.0 |
| TOML config with env var override | Flexible credential management, security-conscious default  | ✓ Shipped v1.0 |
| On-demand fetch (no sync daemon)  | Aligns with CLI design, user controls when to fetch         | ✓ Shipped v1.0 |
| XDG config path (~/.config/)      | Standard Linux/Unix convention, future-proofs for standards | ✓ Shipped v1.0 |
| Gap closure for CLI-02            | Config path mismatch found in UAT, fixed before v1.0 ship   | ✓ Resolved      |

---

## Next Milestone (v1.1+)

**ICS Export & Email** — Convert fetched training plans to iCalendar format and email them.

- Generate .ics VTODO entries formatted for Apple Reminders task import
- Accept recipient email address via CLI option
- Integrate with mail service (SMTP or platform-specific)
- Extend date range support to include export in the same invocation

Depends on: v1.0 CLI foundation

---

_Last updated: 2026-03-08 after v1.0 milestone completion_
