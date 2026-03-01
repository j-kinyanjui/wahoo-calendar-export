# Architecture

**Analysis Date:** 2026-03-01

## Pattern Overview

**Overall:** Ktor-based Kotlin Web Application with OAuth2 Authentication

**Key Characteristics:**
- Stateless REST API backend built on Ktor framework
- OAuth2 authentication flow with Wahoo API integration
- Session-based user state management via cookies
- Netty engine for production HTTP serving
- Kotlin Coroutines for asynchronous operations

## Layers

**Application Layer:**
- Purpose: Entry point and module configuration
- Location: `src/main/kotlin/nesski/de/Application.kt`
- Contains: Main function, Ktor application module setup
- Depends on: Ktor server core, authentication plugin

**Plugin Layer:**
- Purpose: Authentication and HTTP client configuration
- Location: `src/main/kotlin/nesski/de/plugins/`
- Contains: OAuth2 configuration, HTTP client setup, routing
- Depends on: Ktor server, Ktor client
- Used by: Application module

**Model Layer:**
- Purpose: Data classes and serialization
- Location: `src/main/kotlin/nesski/de/models/`
- Contains: WahooWorkouts, UserSession, Workouts data classes
- Depends on: Kotlinx serialization

**Utility Layer:**
- Purpose: Custom serializers and helpers
- Location: `src/main/kotlin/nesski/de/utils/`
- Contains: InstantSerializer for ISO date handling

**Test Layer:**
- Purpose: Integration testing
- Location: `src/test/kotlin/nesski/de/`
- Contains: ApplicationTest with Ktor test utilities

## Data Flow

**OAuth2 Authentication Flow:**

1. Client accesses `/login` endpoint
2. Ktor redirects to Wahoo OAuth2 authorize URL
3. User authenticates with Wahoo
4. Wahoo redirects to `/callback` with authorization code
5. Application exchanges code for access token
6. Token stored in UserSession cookie
7. Session cookie sent with subsequent requests

**Workout Data Fetching:**

1. Client requests protected resource (e.g., `/plans`)
2. `UserSession.getSession()` validates session cookie
3. If invalid, redirects to login with return URL
4. If valid, `getPlans()` makes authenticated API call
5. Wahoo API returns workout data
6. Response serialized and returned to client

**State Management:**
- Server-side session via encrypted cookies
- Session contains: OAuth state token, access token
- No external database - stateless session storage

## Key Abstractions

**OAuth2 Server Settings:**
- Purpose: Wahoo API OAuth2 configuration
- Examples: `WahooAuthenticationOauth2.kt`
- Pattern: Ktor Authentication Provider with OAuth2

**HttpClient:**
- Purpose: Shared HTTP client for external API calls
- Examples: `RestClient.kt` - `applicationHttpClient`
- Pattern: Ktor CIO engine with JSON content negotiation

**UserSession:**
- Purpose: Represents authenticated user state
- Examples: `models/UserSession.kt`
- Pattern: Ktor Session with companion object for retrieval

## Entry Points

**Main Entry:**
- Location: `src/main/kotlin/nesski/de/Application.kt`
- Triggers: `io.ktor.server.netty.EngineMain`
- Responsibilities: Application bootstrap, module configuration

**Authentication Module:**
- Location: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt`
- Triggers: OAuth2 flow endpoints (`/login`, `/callback`)
- Responsibilities: OAuth setup, token exchange, session management

**HTTP Routing:**
- Location: `WahooAuthenticationOauth2.kt` (inline routing block)
- Triggers: HTTP requests to defined routes
- Responsibilities: Route handling, session validation, API proxy

## Error Handling

**Strategy:** Result wrapping with `runCatching`

**Patterns:**
- `getPlans()` uses `runCatching` with `fold()` for success/failure
- Failed API calls log error and rethrow exception
- Missing session redirects to login with redirectUrl parameter

## Cross-Cutting Concerns

**Logging:** KtorSimpleLogger for structured logging

**Validation:** OAuth2 parameter validation via Ktor auth

**Authentication:** OAuth2 with cookie-based session tokens

---

*Architecture analysis: 2026-03-01*
