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
- User config file (YAML) with Systm username/password
- Credentials read from config on startup
- GraphQL login mutation authenticates automatically
- JWT token stored in session (no web login form)

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
- Config file location and name (config/systm.yaml or similar)
- Session cookie configuration
- Auto-login error handling (what happens if config credentials fail)
- Status endpoint design

</decisions>

<specifics>
## Specific Ideas

- "Use YAML config file for credentials"
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
