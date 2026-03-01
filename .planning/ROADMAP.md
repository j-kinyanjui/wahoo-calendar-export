# Roadmap: Wahoo Plan to Calendar

## Phases

- [ ] **Phase 1: Authentication & GraphQL Setup** - JWT token management and GraphQL client
- [ ] **Phase 2: Training Plan Display** - Parse and display fetched workouts

## Overview

| Phase            | Goal                                                                                                           | Requirements                                                      |
| ---------------- | -------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| 1 - Auth & Fetch | OAuth flow for Systm — if this is not plausible, User can input JWT token and fetch training plans via GraphQL | AUTH-01, AUTH-02, AUTH-03, DATA-01, DATA-02, DATA-03, DATA-04     |
| 2 - Display      | User can view parsed workout data with error handling                                                          | PARSE-01, PARSE-02, PARSE-03, PARSE-04, DISP-01, DISP-02, DISP-03 |

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

**Plans:** TBD

---

### Phase 2: Training Plan Display

**Goal:** User can view fetched training plans with workout details

**Depends on:** Phase 1

**Requirements:** PARSE-01, PARSE-02, PARSE-03, PARSE-04, DISP-01, DISP-02, DISP-03

**Success Criteria** (what must be TRUE):

1. Application extracts workout name from each GraphQL response item
2. Application extracts planned/scheduled date from each workout
3. Application identifies and categorizes workout types (ride, run, strength, etc.)
4. Application tracks workout status (completed, planned, missed)
5. User can view a list of all fetched training plans on screen
6. Each displayed plan shows workout name and scheduled date
7. Application displays clear error messages when requests fail

**Plans:** TBD

---

## Progress

| Phase                     | Plans Complete | Status      | Completed |
| ------------------------- | -------------- | ----------- | --------- |
| 1 - Auth & GraphQL Setup  | 0/1            | Not started | -         |
| 2 - Training Plan Display | 0/1            | Not started | -         |

---

## Coverage

- v1 requirements: 14 total
- Mapped to phases: 14 ✓
- Unmapped: 0 ✓

---

_Roadmap created: 2026-03-01_
_Depth: quick (2 phases)_
