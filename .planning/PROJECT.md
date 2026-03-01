# Wahoo Plan to Calendar

## What This Is

A Kotlin/Ktor application that syncs training plans from Wahoo ecosystem to a local calendar. Currently fetches plans from the Wahoo REST API (api.wahooligan.com), with new capability to fetch from Systm's GraphQL API (api.thesufferfest.com).

## Core Value

Allow users to view their Wahoo/Systm training plans in their personal calendar for better workout scheduling and tracking.

## Requirements

### Validated

- ✓ OAuth2 authentication with Wahoo REST API — existing
- ✓ Fetch training plans from api.wahooligan.com/v1/plans — existing

### Active

- [ ] Fetch training plans from Systm GraphQL API (api.thesufferfest.com)
- [ ] Support manual JWT token input for Systm authentication
- [ ] Parse GraphQL response to extract workout names and scheduled dates
- [ ] Basic schedule sync (on-demand/daily, not real-time)

### Out of Scope

- Calendar export format — deferred to future phase
- Real-time sync — on-demand/daily only
- OAuth flow for Systm — manual token input acceptable

## Context

**Technical Environment:**
- Kotlin with Ktor framework
- Existing OAuth2 authentication plugin for Wahoo REST API
- HTTP client already configured
- Session management via Ktor sessions

**Systm GraphQL Details (from HAR analysis):**
- Endpoint: https://api.thesufferfest.com/graphql
- Authentication: Bearer JWT token (different from REST API token)
- Key Query: GetUserPlansRange - fetches user training plan items
- Token format: JWT containing {id, sessionToken, username, wahooId, wahooToken, platform, version, roles}

**Known Issues:**
- Training plans created in Systm web app cannot be pulled via existing REST API
- Need separate authentication mechanism for Systm GraphQL API

## Constraints

- **Tech Stack**: Kotlin/Ktor — existing, not changing
- **Token Handling**: Manual JWT input acceptable — no OAuth flow needed for Systm
- **Sync Frequency**: On-demand/daily — not real-time
- **Calendar Export**: TODO — deferred to future phase

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Manual JWT input for Systm | Simpler than implementing OAuth, user can extract from DevTools | — Pending |
| On-demand/daily sync | Not a real-time use case, reduces complexity | — Pending |
| Defer calendar export | User explicitly deferred, focus on data fetching first | — Pending |

---
*Last updated: 2026-03-01 after project initialization*
