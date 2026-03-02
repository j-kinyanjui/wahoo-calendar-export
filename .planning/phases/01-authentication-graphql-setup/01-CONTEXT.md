# Phase 1: Authentication & GraphQL Setup - Context

**Gathered:** 2026-03-01
**Status:** Ready for planning

<domain>
## Phase Boundary

User can authenticate to Systm via username/password (GraphQL login mutation) and fetch training plans via GraphQL API. The JWT token is stored in session and used for subsequent GraphQL requests.

</domain>

<decisions>
## Implementation Decisions

### Authentication Method
- GraphQL Login mutation (not OAuth2) - matches how Systm web app authenticates
- Username/password submitted via GraphQL mutation to api.thesufferfest.com
- Returns JWT token that can be used for subsequent GraphQL requests
- User registers OAuth app on Wahoo developer portal for callback URL

### Token Storage
- Store JWT token in encrypted session (existing Ktor sessions pattern)
- Session persists across browser refreshes

### Token Validation
- No JWT validation needed - token received from successful login
- Store token as-is; Systm API validates on each request

### Date Range
- Default fetch range: past 7 days + next 14 days (21 days total)
- Matches typical training cycle (week ahead + history)

### Error Handling
- Parse GraphQL error responses even when HTTP 200
- Display user-friendly error messages for:
  - Invalid credentials
  - Expired token
  - Network errors
  - GraphQL validation errors

### OpenCode's Discretion
- Exact form field design (username, password inputs)
- Session cookie configuration
- Error message wording
- Date picker UI if user-selectable later

</decisions>

<specifics>
## Specific Ideas

- "Use same pattern as Wahoo REST API - redirect flow, callback URL, store token in session"
- "Default 7-14 days (typical training cycle)"

</specifics>

<deferred>
## Deferred Ideas

- OAuth2 flow for Systm - GraphQL login is simpler and matches actual API
- Custom date range selection - use default first, add to backlog

</deferred>

---

*Phase: 01-authentication-graphql-setup*
*Context gathered: 2026-03-01*
