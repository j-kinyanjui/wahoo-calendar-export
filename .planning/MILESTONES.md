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

---

