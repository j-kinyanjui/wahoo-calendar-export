# State: Wahoo Plan to Calendar

## Project Reference

**Core Value:** Allow users to view their Wahoo/Systm training plans in their personal calendar for better workout scheduling and tracking.

**Current Focus:** Phase 1 - Authentication & GraphQL Setup

---

## Current Position

| Field | Value |
|-------|-------|
| **Phase** | 1 - Authentication & GraphQL Setup |
| **Plan** | Not started |
| **Status** | Ready to begin |
| **Progress** | ░░░░░░░░░░ 0% |

---

## Performance Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| v1 Requirements | 14 | All mapped to phases |
| Phases | 2 | Quick depth approach |
| Current Phase Progress | 0/7 | Success criteria |
| Blockers | 0 | None identified |

---

## Accumulated Context

### Key Decisions

| Decision | Rationale | Status |
|----------|-----------|--------|
| Manual JWT input for Systm | Simpler than implementing OAuth | Pending implementation |
| On-demand sync | Not a real-time use case | Pending implementation |
| 2-phase structure | Quick depth = 1-2 phases max | Defined |

### Technical Notes

- Uses `graphql-kotlin-ktor-client` 8.x.x for GraphQL
- JWT handling via `java-jwt` 4.5.1
- Bearer token in Authorization header
- GraphQL endpoint: https://api.thesufferfest.com/graphql
- Key query: GetUserPlansRange

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

### What's Next

- Plan Phase 1 (Authentication & GraphQL Setup)
- Execute Phase 1 implementation
- Validate Phase 1 success criteria

### User Preferences

- Manual JWT input acceptable (no OAuth flow needed)
- On-demand/daily sync (not real-time)
- Calendar export deferred to v2

---

*State updated: 2026-03-01*
