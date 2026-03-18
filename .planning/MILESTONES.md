# Milestones

## v1.0 MVP - Auth, CLI, ICS Export (Shipped: 2026-03-10)

**Phases completed:** 4 phases, 10 plans, 86 commits

**Timeline:** 2024-02-17 → 2026-03-10 (24 months)

**Codebase:** 2,900 LOC Kotlin, 100+ tests, full RFC 5545 calendar compliance

**Key accomplishments:**
- ✓ Systm authentication with JWT token management and session persistence
- ✓ GraphQL API integration for fetching training plans with full error handling
- ✓ Migrated from Ktor web server to standalone Clikt CLI application
- ✓ TOML config loading with environment variable overrides and XDG conventions
- ✓ RFC 5545 VTODO export for Apple Reminders compatibility
- ✓ SMTP email delivery with disk fallback on failure
- ✓ RFC 5545 VEVENT all-day calendar events using ical4j (universal calendar support)
- ✓ Sport emoji mapping and duration hints in calendar exports
- ✓ 17/17 v1 requirements validated and shipped

**Requirements shipped:**
- AUTH-01, AUTH-02, AUTH-03 (Authentication)
- DATA-01, DATA-02, DATA-03, DATA-04 (GraphQL API)
- PARSE-01, PARSE-02, PARSE-03, PARSE-04 (Data parsing)
- DISP-01, DISP-02, DISP-03 (Console display)
- CLI-01, CLI-02 (CLI framework)
- EXPORT-01, EXPORT-02 (ICS export — VTODO and VEVENT)

**Decisions locked in v1.0:**
- CLI-first (no web UI) — simpler, proven approach
- ical4j for RFC 5545 compliance — no hand-rolled ICS
- SMTP with disk fallback — robust delivery semantics
- All-day DATE events — users control timing in calendar
- Sport emoji in SUMMARY — visual identification

**Technical debt deferred:**
- Phase directories remain in .planning/phases — archive via /gsd-cleanup
- Email credentials via config.toml — production should use env vars only
- No token refresh mechanism — users re-run with fresh credentials

## v1.1 CI/CD Pipeline (Shipped: 2026-03-14)

**Phases completed:** 2 phases, 3 plans

**Timeline:** 2026-03-13 → 2026-03-14 (2 days)

**Codebase:** CI/CD pipeline live — 3 new workflow files (ci.yml, cd.yml, dependabot.yml)

**Key accomplishments:**
- ✓ GitHub Actions CI workflow — build + test + format check on push and PR to main
- ✓ Gradle dependency caching via `gradle/actions/setup-gradle@v4`
- ✓ Spotless/ktfmt formatting enforcement in CI
- ✓ Dependabot for automated dependency updates (Gradle + GitHub Actions ecosystems)
- ✓ CD workflow with `workflow_run` CI→CD chaining (Docker builds only after CI passes)
- ✓ Multi-platform Docker images (linux/amd64 + linux/arm64) pushed to GHCR
- ✓ GitHub Release with auto-generated changelog triggered by `v*.*.*` git tags
- ✓ End-to-end pipeline verified via v1.1.0 tag push

**Requirements shipped:**
- CI-01, CI-02, CI-03 (Continuous Integration)
- CD-01, CD-02, CD-03 (Continuous Delivery)

**Decisions locked in v1.1:**
- `workflow_run` CI→CD chaining — Docker never runs unless CI passes
- Multi-platform Docker (amd64+arm64) — future-proofs for ARM servers and Apple Silicon
- Spotless/ktfmt in CI — formatting enforced, not just suggested
- Dependabot — automated security and version updates

**Technical debt deferred:**
- Branch protection rules — private repo on free plan; enable when public
- GHCR package visibility — one-time manual toggle to Public required
- Docker → Gradle `assembleDist` migration — lighter releases without Docker dependency

---


