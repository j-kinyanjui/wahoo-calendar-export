# Roadmap: Wahoo Plan to Calendar

## Phases

- [x] **Phase 1: Authentication & GraphQL Setup** - JWT token management and GraphQL client (completed 2026-03-02)
- [ ] **Phase 2: CLI Migration** - Migrate from Ktor server to Clikt CLI, fetch and display plans
- [ ] **Phase 3: ICS Export & Email** - Generate .ics VTODO entries and email them

## Overview

| Phase            | Goal                                                                                                           | Requirements                                                      |
| ---------------- | -------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| 1 - Auth & Fetch | 3/3 | Complete    | 2026-03-02 | User can view parsed workout data with error handling                                                          | PARSE-01, PARSE-02, PARSE-03, PARSE-04, DISP-01, DISP-02, DISP-03 |

---

## Phase Details

### Phase 1: Authentication & GraphQL Setup

**Goal:** User can authenticate by euther using OAuth 2 or input JWT token and application can fetch training plans via GraphQL

**Depends on:** Nothing (first phase)

**Requirements:** AUTH-01, AUTH-02, AUTH-03, DATA-01, DATA-02, DATA-03, DATA-04

**Success Criteria** (what must be TRUE):

1. User can authenticate by eiher inputing a Systm JWT token via web form and submit or OAuth flow for Systm
2. User's Systm token is persisted in encrypted session storage
3. User can clear/reset their Systm token at any time
4. Application can make HTTP POST requests to api.thesufferfest.com/graphql
5. Application sends Bearer JWT token in Authorization header with GraphQL requests
6. Application executes GetUserPlansRange GraphQL query with date parameters
7. Application handles GraphQL error responses (HTTP 200 with errors field) gracefully

**Plans:** 3/3 plans complete
- [x] 01-01-PLAN.md — Session infrastructure + GraphQL client
- [x] 01-02-PLAN.md — Login form + JWT validation + logout
- [x] 01-03-PLAN.md — GetUserPlansRange query + error handling

---

### Phase 2: CLI Migration

**Goal:** Migrate from Ktor server to a Clikt-based CLI application that auto-authenticates, fetches training plans for a user-specified time range, and displays them in the console

**Depends on:** Phase 1

**Requirements:** PARSE-01, PARSE-02, PARSE-03, PARSE-04, DISP-01, DISP-02, DISP-03, CLI-01, CLI-02

**Success Criteria** (what must be TRUE):

1. Ktor server dependencies removed (server-core, netty, auth, config-yaml)
2. Application runs as a CLI via Clikt with `--range`, `--from`/`--to`, `--config` options
3. Credentials loaded from env vars (`SYSTM_USER`, `SYSTM_PASSWORD`) with fallback to `~/.config/wahoo-cli/config`
4. Auto-login on each run using existing SystmAuthService
5. Plans fetched for specified range: `now`, `1w`, `2w`, `1m`, `2m`, or explicit `--from`/`--to` (max 2 months)
6. Application extracts workout name, date, type, and status from GraphQL response
7. User sees formatted list of workouts in the console
8. nginx/Docker infrastructure removed
9. Clear error messages for auth failures, API errors, and invalid ranges

**Plans:** 2 plans
- [x] 02-01-PLAN.md — CLI foundation: build migration, Clikt entry point, TOML config, date range parsing
- [ ] 02-02-PLAN.md — Integration: wire auth → fetch → display, remove server files

---

### Phase 3: ICS Export & Email

**Goal:** Generate .ics VTODO entries (Apple Reminders tasks) from fetched workouts and email them to a provided address

**Depends on:** Phase 2

**Requirements:** EXPORT-01

**Success Criteria** (what must be TRUE):

1. Each workout generates a VTODO .ics entry with due date (compatible with Apple Reminders)
2. .ics file emailed to the provided `--email` address via SMTP
3. Sport emoji in VTODO SUMMARY (🧘🏽‍♂️ yoga, 🚴 cycling, 🏋🏾‍♀️ strength)
4. On email failure, .ics saved to disk with error message
5. .ics save location configurable in config file
6. Clear error messages for email delivery issues

**Plans:** TBD

---

## Progress

| Phase                     | Plans Complete | Status      | Completed |
| ------------------------- | -------------- | ----------- | --------- |
| 1 - Auth & GraphQL Setup  | 3/3            | Complete    | 2026-03-02 |
| 2 - CLI Migration               | 1/2            | In Progress | -         |
| 3 - ICS Export & Email          | 0/?            | Not started | -         |

---

## Coverage

- v1 requirements: 14 total
- Mapped to phases: 14 ✓
- Unmapped: 0 ✓

---

_Roadmap created: 2026-03-01_
_Depth: quick (3 phases)_
