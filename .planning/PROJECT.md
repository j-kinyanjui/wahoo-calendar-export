# Wahoo Plan to Calendar

## What This Is

A command-line application that fetches Wahoo Systm training plans via GraphQL API, displays them in the console, and exports them as RFC 5545-compliant calendar events. Auto-authenticates using stored credentials or environment variables, supports flexible date range queries, and integrates with calendar applications (Apple Calendar, Google Calendar, Outlook).

## Core Value

Simple, non-interactive CLI access to training plans with instant calendar export — authenticate once, fetch plans for any date range, export as .ics for any calendar application.

## Current State (v1.1 Shipped — 2026-03-14)

**Codebase:**
- 13 completed plans across 6 phases (v1.0: 10 plans, v1.1: 3 plans)
- ~2,900+ LOC Kotlin + Gradle (including 100+ tests)
- CI/CD pipeline live on GitHub Actions

**Tech Stack:**
- Kotlin 1.9.23
- Clikt 5.0.3 for CLI framework
- ktoml 0.7.0 for TOML config parsing
- kotlinx.coroutines for async/await
- ical4j 4.0.8 for RFC 5545 calendar generation
- simple-java-mail 8.12.4 for SMTP delivery
- SLF4J for logging
- GitHub Actions CI (build + test + format check)
- GitHub Actions CD (Docker → GHCR + GitHub Releases)

**Architecture:**
- CLI entry point (WahooCli.kt) with Clikt command
- AppConfig for credential and config file management
- SystmAuthService for JWT token authentication
- PlansService for GraphQL plan fetching
- IcsBuilder for RFC 5545 calendar event generation (VEVENT)
- EmailService for SMTP delivery with disk fallback
- DateRangeParser for flexible date input

**Production Ready:**
- Credentials loaded from env vars or ~/.config/wahoo-cli/config (XDG convention)
- Auto-retry on auth, graceful error messages on auth/API failures
- Email fallback: on SMTP failure, .ics saved to disk instead of lost
- RFC 5545 compliant .ics output with duration hints in SUMMARY
- CI: automated build + test + format check on every push and PR to main
- CD: Docker image to GHCR on main merge; versioned releases on git tags

## Next Milestone

Start with `/gsd-new-milestone` to define requirements and roadmap for the next milestone.

**Known candidates (from technical debt / deferred work):**
- Migrate CI/CD from Docker build to Gradle `assembleDist` artifacts for lighter releases
- Email scheduling (daily/weekly digest of upcoming workouts)
- Workout filtering by type (cycling, strength, yoga, etc.)
- Branch protection rules (when repo is public)
- Token expiration tracking and user notification

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
- ✓ ICS file export (.ics VTODO entries for Apple Reminders) — v1.0
- ✓ Email delivery of workouts to provided address via SMTP — v1.0
- ✓ RFC 5545-compliant .ics export with VEVENT all-day entries (cross-calendar: Apple, Google, Outlook, Yahoo) — v1.0

### Validated (v1.1)

- ✓ GitHub Actions CI workflow (build + test + format check on push/PR to main)
- ✓ Gradle dependency caching between CI runs
- ✓ Docker image build and push to GHCR on main merge
- ✓ Versioned Docker image build and push triggered by git tags
- ✓ GitHub Release with auto-generated changelog on version tag push

### Future (v1.2+)

- [ ] Email scheduling (daily/weekly digest of upcoming workouts)
- [ ] Workout filtering by type (cycling, strength, yoga, etc.)
- [ ] Custom date range presets in config
- [ ] Quiet/verbose output modes
- [ ] Token expiration tracking and notification

### Out of Scope

- Web UI — CLI-first approach
- Real-time sync — fetch on-demand only
- OAuth flow — manual token input acceptable
- Two-way sync (calendar → Systm) — export only

## Constraints

- **Tech Stack**: Kotlin/Ktor — existing, not changing
- **Token Handling**: Manual JWT input acceptable — no OAuth flow needed for Systm
- **Sync Frequency**: On-demand/daily — not real-time

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| CLI-first (no web UI) | Simpler scope, faster iteration, easier testing | ✓ Shipped v1.0 |
| Clikt 5.0.3 framework | Mature, well-documented, supports Kotlin idioms | ✓ Shipped v1.0 |
| TOML config with env var override | Flexible credential management, security-conscious default | ✓ Shipped v1.0 |
| On-demand fetch (no sync daemon) | Aligns with CLI design, user controls when to fetch | ✓ Shipped v1.0 |
| XDG config path (~/.config/) | Standard Linux/Unix convention | ✓ Shipped v1.0 |
| Manual JWT input (no OAuth) | Simpler, acceptable for CLI tool | ✓ Shipped v1.0 |
| RFC 5545 VEVENT (not VTODO) | Universal calendar compatibility (Apple, Google, Outlook, Yahoo) | ✓ Shipped v1.0 |
| ical4j 4.0.8 for ICS generation | RFC 5545 compliance, no hand-rolled ICS strings | ✓ Shipped v1.0 |
| SMTP email with disk fallback | Robust delivery: email on success, .ics on SMTP failure | ✓ Shipped v1.0 |
| `workflow_run` CI→CD chaining | Docker builds only after CI passes | ✓ Shipped v1.1 |
| Multi-platform Docker (amd64+arm64) | Support both Intel and ARM architectures | ✓ Shipped v1.1 |
| Spotless/ktfmt for formatting enforcement | Consistent code style enforced in CI | ✓ Shipped v1.1 |
| Dependabot for Gradle + Actions | Automated dependency security updates | ✓ Shipped v1.1 |

## Patterns Established

- **Config pattern:** TOML file with env var override precedence
- **Builder pattern:** Object singleton builders for stateless operations (IcsBuilder, SportEmoji)
- **Result type pattern:** Data classes tracking success counts + skip reasons for partial operations
- **Email fallback:** Try primary delivery → on failure save locally with error message
- **ICS output:** ical4j CalendarOutputter for all ICS generation (no hand-rolled strings)
- **CI/CD chain:** `workflow_run` ensures CD never runs unless CI passes

## Technical Debt & Future Work

- Phase directories remain in `.planning/phases/` — archive retroactively via `/gsd-cleanup`
- Email service uses config.toml for SMTP settings — production users should use env vars only
- No token refresh mechanism — Systm tokens expire, users must re-run with fresh credentials
- Branch protection rules deferred — private repo on free plan; enable when public
- GHCR package visibility requires one-time manual toggle to Public after first push

<details>
<summary>Previous milestone context (v1.0)</summary>

## Current Milestone: v1.0 MVP (Shipped 2026-03-10)

**Goal:** Authenticated CLI application that fetches Wahoo Systm training plans and exports them as RFC 5545-compliant calendar events.

**Codebase:**
- 10 completed plans across 4 phases
- ~2,900 LOC Kotlin + Gradle (including 100+ tests)
- 86 commits, 2024-02-17 to 2026-03-10
- All 17 v1.0 requirements validated and shipped

</details>

---

_Last updated: 2026-03-18 after v1.1 milestone archived_
_Shipped: v1.0 (4 phases, 10 plans, 17 requirements) + v1.1 (2 phases, 3 plans, 6 requirements)_
