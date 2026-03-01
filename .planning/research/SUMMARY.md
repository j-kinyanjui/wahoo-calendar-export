# Project Research Summary

**Project:** Wahoo Plan to Calendar (Systm GraphQL Integration)
**Domain:** Kotlin/Ktor GraphQL API Client + JWT Authentication
**Researched:** 2026-03-01
**Confidence:** HIGH

## Executive Summary

This project integrates the Systm GraphQL API into an existing Wahoo-to-Calendar sync application. The Systm API uses Bearer JWT token authentication (different from Wahoo's OAuth2), requiring a new GraphQL client layer alongside the existing REST client. Research confirms using `graphql-kotlin-ktor-client` 8.x.x with Ktor's existing HTTP infrastructure is the optimal approach—it's lightweight, type-safe, and idiomatic for the Kotlin ecosystem.

The recommended approach prioritizes an MVP that fetches and displays training plans using a user-provided JWT token. Token validation and expiration handling are critical security concerns that must be addressed in the initial phase. GraphQL responses always return 200 OK even on errors, so proper error parsing is essential—ignoring the `errors` field is a common pitfall. Architecture follows a shared HttpClient pattern with per-request authentication headers, aligning with existing project patterns.

Key risks include: (1) JWT token security during storage, (2) handling expired tokens gracefully, and (3) properly parsing GraphQL error responses. These are addressed through explicit validation, session-based token storage with expiration tracking, and comprehensive response parsing.

## Key Findings

### Recommended Stack

**Core technologies:**
- `graphql-kotlin-ktor-client` 8.x.x — Type-safe GraphQL HTTP client for Ktor, built on existing Ktor HTTP Client, supports kotlinx.serialization
- `com.auth0:java-jwt` 4.5.1 — JWT decode/validate (minimal use needed for client; primarily for expiration checking)
- `graphql-kotlin-gradle-plugin` 8.x.x — Generates type-safe Kotlin classes from GraphQL queries at build time

**Compatibility note:** GraphQL Kotlin 9.x.x requires Kotlin 2.x. Stay on 8.x.x for Kotlin 1.9.x compatibility with existing project.

### Expected Features

**Must have (table stakes):**
- Manual JWT token input — core authentication mechanism, users obtain token from browser DevTools
- GraphQL client with GetUserPlansRange query — fetches training plans from Systm
- Parse workout names and dates — extracts needed data from GraphQL response
- Display fetched plans — shows user what was retrieved before sync
- Token storage via Ktor sessions — persists across requests

**Should have (competitive):**
- Token validation before use — immediate feedback on token validity
- Date range selector — control over which training plans to fetch
- Workout type filtering — reduce noise from unwanted workout types
- Plan preview before sync — shows what will be synced

**Defer (v2+):**
- Calendar export — scope creep, can add after validating core fetch
- Automatic token refresh — Systm tokens are long-lived
- Multiple account support — manage multiple Systm users

### Architecture Approach

The architecture follows three key patterns: (1) Shared HttpClient across REST and GraphQL for connection pooling efficiency, (2) Per-request authentication where JWT is injected as Bearer token header rather than globally, and (3) Plugin-based configuration encapsulating Systm-specific logic in dedicated Ktor plugins.

**Major components:**
1. `SystmClient.kt` (plugins/) — GraphQLKtorClient wrapper with JWT auth setup
2. `SystmAuthentication.kt` (plugins/) — JWT token storage/retrieval via Ktor Sessions
3. `GetUserPlansRange.graphql` (graphql/queries/) — GraphQL query file for code generation
4. `SystmRoutes.kt` (routes/) — Endpoint to fetch plans via GraphQL
5. `SystmModels.kt` (models/) — GraphQL response models (auto-generated)

### Critical Pitfalls

1. **JWT Token Not Validated Before Use** — Always validate JWT signature, expiration, and issuer before making API calls. Don't trust user-provided tokens blindly.

2. **Expired Token Handling** — Parse token's `exp` claim, track expiration, provide clear error message: "Systm token expired. Please provide a new token."

3. **GraphQL Error Responses Ignored** — GraphQL always returns 200 OK. Parse both `data` and `errors` fields from response body.

4. **Token Storage Not Secure** — Use Ktor's encrypted session storage, not plain config files. Don't log tokens in plaintext.

5. **No Rate Limiting Handling** — Implement retry with exponential backoff, handle 429 responses specifically.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: JWT Authentication & GraphQL Setup
**Rationale:** Authentication is foundational—can't fetch data without valid credentials. This phase establishes the security foundation and GraphQL client infrastructure.
**Delivers:** JWT token input UI, token validation, session storage with expiration tracking, GraphQL client configured with Bearer auth
**Addresses:** Manual JWT token input, Token storage, GraphQL client setup (P1 features)
**Avoids:** Pitfall #1 (JWT not validated), Pitfall #2 (expired token), Pitfall #5 (insecure storage)

### Phase 2: Training Plan Fetching
**Rationale:** Core value proposition—fetching and displaying workouts from Systm. Depends on Phase 1's client infrastructure.
**Delivers:** GetUserPlansRange query execution, response parsing (workout names, dates, types), plan display UI
**Addresses:** Fetch training plans, Parse workout data, Display fetched plans (P1 features)
**Avoids:** Pitfall #3 (GraphQL errors ignored), Pitfall #4 (N+1 queries - use batched queries)

### Phase 3: Enhanced Filtering & Preview
**Rationale:** Differentiators that improve UX without adding major complexity. Builds on core fetch functionality.
**Delivers:** Date range selector, workout type filtering, plan preview before sync
**Addresses:** Token validation, Date range selector, Workout type filtering, Plan preview (P2 features)
**Avoids:** Pitfall #7 (rate limiting - add retry logic)

### Phase Ordering Rationale

- **Authentication before fetching** — Can't make valid API calls without working JWT flow
- **Core fetch before enhancements** — Must validate basic sync works before adding filters
- **Error handling throughout** — Each phase incorporates pitfall prevention from research
- **Minimal MVP focus** — Defer calendar export, multiple accounts, scheduled sync to v2+

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 2:** GraphQL response structure details — need actual GetUserPlansRange query schema from HAR analysis to confirm field names
- **Phase 3:** Rate limiting specifics — need to verify Systm API limits through testing

Phases with standard patterns (skip research-phase):
- **Phase 1:** JWT handling is well-documented, Ktor Sessions is existing pattern
- **Phase 3:** Date pickers and filters are standard UI patterns

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Official GraphQL Kotlin and java-jwt documentation, version compatibility verified |
| Features | HIGH | Based on clear MVP definition, competitor analysis, and user workflow dependencies |
| Architecture | HIGH | Follows established Ktor patterns, integrates with existing project structure |
| Pitfalls | MEDIUM | JWT and GraphQL best practices well-documented; specific Systm API behavior needs validation |

**Overall confidence:** HIGH

### Gaps to Address

- **GetUserPlansRange query structure:** Need to verify exact GraphQL response fields from HAR capture or live API
- **Systm API rate limits:** Unknown specific limits; may need adjustment during Phase 3
- **Token expiration duration:** Need to verify typical token lifetime to inform expiration handling

## Sources

### Primary (HIGH confidence)
- GraphQL Kotlin Ktor Client: https://expediagroup.github.io/graphql-kotlin/docs/client/client-overview
- java-jwt: https://mvnrepository.com/artifact/com.auth0/java-jwt (4.5.1)
- Ktor JWT Documentation: https://ktor.io/docs/server-jwt.html

### Secondary (MEDIUM confidence)
- suffersync (reference implementation): https://github.com/bakermat/suffersync
- JWT Best Practices: https://jwt.app/blog/jwt-best-practices/

### Tertiary (LOW confidence)
- Project context from PROJECT.md — assumed existing Wahoo REST sync pattern

---
*Research completed: 2026-03-01*
*Ready for roadmap: yes*
