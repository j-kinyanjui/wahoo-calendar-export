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

- [ ] **PARSE-01**: Application extracts workout name from GraphQL response
- [ ] **PARSE-02**: Application extracts planned date from GraphQL response
- [ ] **PARSE-03**: Application handles workout types (type field)
- [ ] **PARSE-04**: Application handles workout status (completed, planned, etc.)

### Display

- [ ] **DISP-01**: User can view list of fetched training plans
- [ ] **DISP-02**: Each plan shows name and scheduled date
- [ ] **DISP-03**: Application displays error messages for failed requests

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
| Calendar export        | Explicitly deferred to future phase                         |
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
| PARSE-01    | Phase 2 | Pending |
| PARSE-02    | Phase 2 | Pending |
| PARSE-03    | Phase 2 | Pending |
| PARSE-04    | Phase 2 | Pending |
| DISP-01     | Phase 2 | Pending |
| DISP-02     | Phase 2 | Pending |
| DISP-03     | Phase 2 | Pending |

**Coverage:**

- v1 requirements: 14 total
- Mapped to phases: 14
- Unmapped: 0 ✓

---

_Requirements defined: 2026-03-01_
_Last updated: 2026-03-02 after plan 01-03 completion_
