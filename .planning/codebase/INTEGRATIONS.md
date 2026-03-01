# External Integrations

**Analysis Date:** 2026-03-01

## APIs & External Services

**Wahoo Fitness API:**
- OAuth2-based integration for workout data
- Base URL: `https://api.wahooligan.com`
- SDK/Client: Ktor HTTP Client (`io.ktor:ktor-client-cio`)
- Authentication: OAuth2 with scopes: `email`, `user_read`, `workouts_read`, `plans_read`
- Endpoints used:
  - `GET /oauth/authorize` - Authorization endpoint
  - `GET /oauth/token` - Token endpoint
  - `GET /v1/plans` - Fetch user workout plans
- Auth env vars: `CLIENT_ID`, `CLIENT_SECRET`
- Redirect URI: `https://wahoo.nesski.com/callback`

**Calendar Integration:**
- Not yet implemented (application name suggests Google Calendar export planned)
- No API client detected

## Data Storage

**Databases:**
- None detected - Application is stateless, uses in-memory session storage

**File Storage:**
- Local filesystem only (logging, configuration)

**Caching:**
- None detected

## Authentication & Identity

**OAuth2 Provider:**
- Wahoo Fitness OAuth2 (`wahoo-system`)
  - Implementation: Ktor Authentication plugin (`ktor-server-auth`)
  - Flow: OAuth2 with state parameter
  - Session storage: Cookie-based (`wahoo_user_session`)
  - Token storage: Bearer token in session

**Session Management:**
- Ktor Sessions with cookie backend
- Cookie name: `wahoo_user_session`
- Contains: OAuth state and access token

## Monitoring & Observability

**Error Tracking:**
- None detected

**Logs:**
- Ktor Simple Logger (`KtorSimpleLogger`)
- Logback configuration (default)
- Log location: Standard output/file

## CI/CD & Deployment

**Hosting:**
- Self-hosted (likely VPS/cloud VM)
- Domain: `wahoo.nesski.com`

**Reverse Proxy:**
- nginx (Docker container)
  - Image: `blacklabelops/nginx`
  - SSL/TLS termination
  - Configuration: `infra/nginx/docker-compose.yml`

**CI Pipeline:**
- None detected (no GitHub Actions, CircleCI, etc.)

## Environment Configuration

**Required env vars:**
- `CLIENT_ID` - Wahoo OAuth2 client ID
- `CLIENT_SECRET` - Wahoo OAuth2 client secret
- Fallback: Configured in `application.yaml` (empty by default)

**Secrets location:**
- Environment variables (production)
- `application.yaml` (development only, should be empty)

## Webhooks & Callbacks

**Incoming:**
- OAuth2 callback: `GET /callback` - Handles OAuth redirect from Wahoo
- No other webhooks detected

**Outgoing:**
- Redirect to Wahoo OAuth authorize URL for login flow

---

*Integration audit: 2026-03-01*
