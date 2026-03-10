# Requirements: Wahoo Plan to Calendar

**Defined:** 2026-03-01
**Core Value:** Allow users to view their Wahoo/Systm training plans in their personal calendar for better workout scheduling and tracking.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Authentication

- [x] **AUTH-01**: OAuth flow for Systm — manual token input acceptable OR User can input Systm JWT token via web form
- [x] **AUTH-02**: User's Systm token is stored in session (existing Ktor sessions)
- [x] **AUTH-03**: User can clear/reset their Systm token

### Data Fetching

- [x] **DATA-01**: Application can make GraphQL requests to api.thesufferfest.com
- [x] **DATA-02**: Application sends Bearer JWT token with GraphQL requests
- [x] **DATA-03**: Application executes GetUserPlansRange query with date parameters
- [x] **DATA-04**: Application handles GraphQL error responses (HTTP 200 with errors field)

### Data Parsing

- [x] **PARSE-01**: Application extracts workout name from GraphQL response
- [x] **PARSE-02**: Application extracts planned date from GraphQL response
- [x] **PARSE-03**: Application handles workout types (type field)
- [x] **PARSE-04**: Application handles workout status (completed, planned, etc.)

### Display

- [x] **DISP-01**: User can view list of fetched training plans
- [x] **DISP-02**: Each plan shows name and scheduled date
- [x] **DISP-03**: Application displays error messages for failed requests

### CLI

- [x] **CLI-01**: Application runs as a Clikt CLI with `--range`, `--from`/`--to`, `--config` options (no web server)
- [x] **CLI-02**: CLI loads credentials and settings from TOML config file (`~/.config/wahoo-cli/config`) with env var overrides (`SYSTM_USER`, `SYSTM_PASSWORD`)

### ICS Export

- [x] **EXPORT-01**: Application generates RFC 5545-compliant .ics file with VTODO entries from fetched workouts (Apple Reminders compatible, sport emoji in SUMMARY, date-only DUE)
- [x] **EXPORT-02**: Application generates RFC 5545-compliant .ics file with VEVENT all-day entries using ical4j (cross-calendar compatible: Apple, Google, Outlook, Yahoo; sport emoji + duration in SUMMARY)

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Enhanced Features

- **ENH-01**: Date range filter for fetching plans
- **ENH-02**: Workout type filtering
- **ENH-03**: Calendar export (iCal/Google Calendar)
- **ENH-04**: Automatic/daily sync scheduling

### Token Management

- **TOKEN-01**: Token expiration tracking and user notification
- **TOKEN-02**: Token refresh mechanism (if available from Systm)

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature                | Reason                                                      |
| ---------------------- | ----------------------------------------------------------- |
| Real-time sync         | On-demand/daily sync sufficient per requirements            |
| Wahoo REST API removal | Keep existing functionality, add Systm as additional source |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase   | Status  |
| ----------- | ------- | ------- |
| AUTH-01     | Phase 1 | Complete |
| AUTH-02     | Phase 1 | Complete |
| AUTH-03     | Phase 1 | Complete |
| DATA-01     | Phase 1 | Complete |
| DATA-02     | Phase 1 | Complete |
| DATA-03     | Phase 1 | Complete |
| DATA-04     | Phase 1 | Complete |
| PARSE-01    | Phase 2 | Complete |
| PARSE-02    | Phase 2 | Complete |
| PARSE-03    | Phase 2 | Complete |
| PARSE-04    | Phase 2 | Complete |
| DISP-01     | Phase 2 | Complete |
| DISP-02     | Phase 2 | Complete |
| DISP-03     | Phase 2 | Complete |
| CLI-01      | Phase 2 | Complete |
| CLI-02      | Phase 2 | Complete |
| EXPORT-01   | Phase 3 | Complete |
| EXPORT-02   | Phase 4 | Complete |

**Coverage:**

- v1 requirements: 17 total (all complete)
- v2 requirements: 1 (EXPORT-02 — complete)
- Mapped to phases: 18
- Unmapped: 0 ✓

---

_Requirements defined: 2026-03-01_
_Last updated: 2026-03-10 after plan 04-01 completion_
