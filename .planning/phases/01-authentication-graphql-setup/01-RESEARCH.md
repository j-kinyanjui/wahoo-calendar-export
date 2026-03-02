# Phase 1: Authentication & GraphQL Setup - Research

**Researched:** 2026-03-02
**Domain:** Ktor + GraphQL client + JWT token handling
**Confidence:** HIGH

## Summary

Phase 1 implements user authentication to the Systm GraphQL API (api.thesufferfest.com) via a login mutation, storing the JWT in Ktor sessions, and fetching training plans. The existing Ktor project already has session handling for OAuth2 tokens - this phase adds GraphQL-based authentication and data fetching.

**Primary recommendation:** Use plain Ktor HttpClient with kotlinx.serialization for GraphQL (simpler than adding graphql-kotlin-client). Extend existing UserSession model to support Systm tokens. No local JWT validation needed - Systm validates on API calls.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- GraphQL Login mutation (not OAuth2) - username/password to api.thesufferfest.com
- JWT token stored in encrypted session (existing Ktor sessions pattern)
- No local JWT validation - Systm validates on API calls
- Default fetch range: past 7 days + next 14 days (21 days total)
- Parse GraphQL error responses even when HTTP 200

### OpenCode's Discretion
- Exact form field design (username, password inputs)
- Session cookie configuration
- Error message wording
- Date picker UI if user-selectable later

### Deferred Ideas (OUT OF SCOPE)
- OAuth2 flow for Systm - GraphQL login is simpler
- Custom date range selection - use default first
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| AUTH-01 | User can input Systm JWT token via web form | GraphQL mutation login pattern using Ktor HTTP client POST with JSON body |
| AUTH-02 | User's Systm token stored in session | Existing UserSession data class + Ktor Sessions with cookie |
| AUTH-03 | User can clear/reset Systm token | Ktor sessions clear/set pattern |
| DATA-01 | Application can make GraphQL requests to api.thesufferfest.com | Ktor HttpClient with content negotiation or raw POST |
| DATA-02 | Application sends Bearer JWT token with GraphQL requests | Ktor HTTP headers append pattern |
| DATA-03 | Application executes GetUserPlansRange query with date parameters | GraphQL query with variables, date range params |
| DATA-04 | Application handles GraphQL error responses (HTTP 200 with errors field) | Parse GraphQL response errors field |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Ktor HTTP Client | 2.3.x (from project) | HTTP requests | Already in project |
| kotlinx.serialization | 1.4.32 (from project) | JSON serialization | Already in project |
| java-jwt | 4.5.1 (from STATE.md) | JWT decode/validation | Standard for JWT handling |
| Ktor Sessions | 2.3.x (from project) | Session management | Already in project |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| graphql-kotlin-ktor-client | 8.x.x | Type-safe GraphQL client | Only if generated schema available |
| ktor-server-content-negotiation | 2.3.x | JSON response handling | For deserializing GraphQL responses |

### Installation
```kotlin
// Add to build.gradle.kts
implementation("com.auth0:java-jwt:4.5.1")
implementation("com.expediagroup:graphql-kotlin-ktor-client:8.0.0") // Optional - only if needed
```

**Note:** Plain Ktor HttpClient is sufficient for GraphQL without adding graphql-kotlin-client:
```kotlin
httpClient.post("https://api.thesufferfest.com/graphql") {
    contentType(ContentType.Application.Json)
    setBody(GraphQLRequestBody(query = mutation, variables = vars))
}
```

## Architecture Patterns

### Recommended Project Structure
```
src/main/kotlin/nesski/de/
├── plugins/
│   ├── SystmAuthentication.kt    # NEW: GraphQL login mutation + session
│   └── WahooAuthenticationOauth2.kt  # Existing: OAuth2 for Wahoo
├── routes/
│   ├── LoginRoute.kt             # NEW: Login form + mutation handling
│   └── PlansRoute.kt             # NEW: Fetch plans via GraphQL
├── models/
│   ├── UserSession.kt            # MODIFY: Add Systm token support
│   ├── GraphQL.kt                # NEW: GraphQL request/response models
│   └── SystmModels.kt            # NEW: Systm-specific data models
└── services/
    └── SystmGraphQLClient.kt     # NEW: GraphQL client wrapper
```

### Pattern 1: GraphQL Login Mutation
**What:** Submit username/password via GraphQL mutation to get JWT token
**When to use:** For authenticating to Systm API
**Example:**
```kotlin
// Source: Ktor docs + GraphQL conventions
data class GraphQLRequest(val query: String, val variables: Map<String, Any>? = null)

suspend fun login(httpClient: HttpClient, username: String, password: String): String {
    val mutation = """
        mutation Login(${'$'}username: String!, ${'$'}password: String!) {
            login(username: ${'$'}username, password: ${'$'}password) {
                token
                expiresAt
            }
        }
    """.trimIndent()
    
    val response = httpClient.post("https://api.thesufferfest.com/graphql") {
        contentType(ContentType.Application.Json)
        setBody(GraphQLRequest(
            query = mutation,
            variables = mapOf("username" to username, "password" to password)
        ))
    }
    
    val result = response.body<GraphQLResponse<LoginData>>()
    if (result.errors != null) {
        throw AuthException(result.errors.map { it.message }.joinToString(", "))
    }
    return result.data!!.login.token
}
```

### Pattern 2: JWT Expiration Validation
**What:** Decode JWT and validate expiration before storing/using
**When to use:** Every time a JWT is received
**Example:**
```kotlin
// Source: java-jwt documentation
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.Date

fun validateAndDecodeJWT(token: String): DecodedJWT {
    val decoded = JWT.decode(token)
    val expiresAt = decoded.expiresAt
    
    if (expiresAt.before(Date())) {
        throw JWTVerificationException("Token expired at $expiresAt")
    }
    return decoded
}

fun isTokenExpired(token: String): Boolean {
    return try {
        validateAndDecodeJWT(token)
        false
    } catch (e: JWTVerificationException) {
        true
    }
}
```

### Pattern 3: GraphQL Error Handling
**What:** Parse errors array even in HTTP 200 responses
**When to use:** Every GraphQL response
**Example:**
```kotlin
// Source: GraphQL spec
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<GraphQLError>?
)

data class GraphQLError(
    val message: String,
    val locations: List<ErrorLocation>?,
    val path: List<Any>?
)

fun <T> GraphQLResponse<T>.throwOnError() {
    errors?.let { 
        throw GraphQLException(it.map { e -> e.message }.joinToString("; "))
    }
}
```

### Pattern 4: Bearer Token in Headers
**What:** Add Authorization header with Bearer token to requests
**When to use:** Every authenticated GraphQL request
**Example:**
```kotlin
// Source: Ktor HTTP client docs
httpClient.post("https://api.thesufferfest.com/graphql") {
    contentType(ContentType.Application.Json)
    headers {
        append(HttpHeaders.Authorization, "Bearer $jwtToken")
    }
    setBody(GraphQLRequestBody(query = query, variables = variables))
}
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JWT decoding | Parse JSON manually | java-jwt | Handles all claims, Base64URL, edge cases |
| GraphQL client | Raw HTTP construction | Ktor HttpClient with setBody | Already in project, handles serialization |
| Session encryption | Build custom encryption | Ktor Sessions with cookie<Session> | Built-in, tested |
| Date parsing | Manual date handling | kotlinx.serialization Instant/LocalDate | Already in project |

## Common Pitfalls

### Pitfall 1: GraphQL Errors in HTTP 200
**What goes wrong:** GraphQL returns errors in `errors` field even when HTTP status is 200 OK
**Why it happens:** GraphQL spec allows partial success - data may be null while errors exist
**How to avoid:** Always check `errors` field before using `data`
**Warning signs:** Getting null data despite successful HTTP response

### Pitfall 2: JWT Not Validated Before Storage
**What goes wrong:** Storing expired tokens leads to confusing errors later
**Why it happens:** Token looks valid, but fails when used for API calls
**How to avoid:** Validate `exp` claim immediately after receiving token
**Warning signs:** "Unauthorized" errors immediately after successful login

### Pitfall 3: Session Not Persisted
**What goes wrong:** User has to login after every page refresh
**Why it happens:** Missing cookie configuration or session not set properly
**How to avoid:** Ensure `call.sessions.set()` is called and cookie is configured
**Warning signs:** Session is null on subsequent requests

### Pitfall 4: Content-Type Mismatch
**What goes wrong:** GraphQL requests fail with 400 Bad Request
**Why it happens:** Wrong Content-Type or malformed JSON body
**How to avoid:** Use `contentType(ContentType.Application.Json)` and proper JSON structure
**Warning signs:** HTTP 400 errors on all GraphQL requests

## Code Examples

### GetUserPlansRange Query
```kotlin
// Source: Based on GraphQL conventions + Ktor client
data class PlansQueryVariables(
    val startDate: String,  // ISO 8601 format
    val endDate: String
)

data class GetUserPlansRangeResponse(
    val data: UserPlansData?,
    val errors: List<GraphQLError>?
)

data class UserPlansData(
    val userPlansRange: List<Plan>
)

data class Plan(
    val id: String,
    val name: String,
    val scheduledDate: String,
    val status: String,
    val type: String
)

suspend fun fetchPlans(
    httpClient: HttpClient,
    token: String,
    startDate: LocalDate,
    endDate: LocalDate
): List<Plan> {
    val query = """
        query GetUserPlansRange(${'$'}startDate: String!, ${'$'}endDate: String!) {
            userPlansRange(startDate: ${'$'}startDate, endDate: ${'$'}endDate) {
                id
                name
                scheduledDate
                status
                type
            }
        }
    """.trimIndent()
    
    val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE
    val response = httpClient.post("https://api.thesufferfest.com/graphql") {
        contentType(ContentType.Application.Json)
        headers { append(HttpHeaders.Authorization, "Bearer $token") }
        setBody(GraphQLRequest(
            query = query,
            variables = mapOf(
                "startDate" to startDate.format(dateFormat),
                "endDate" to endDate.format(dateFormat)
            )
        ))
    }
    
    val result = response.body<GetUserPlansRangeResponse>()
    result.throwOnError()
    return result.data?.userPlansRange ?: emptyList()
}
```

### Session Management
```kotlin
// Source: Based on existing UserSession + Ktor Sessions
data class SystmSession(
    val token: String,
    val expiresAt: Long
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
    
    companion object {
        fun getSession(call: ApplicationCall): SystmSession? = call.sessions.get()
    }
}

// In route handler
call.sessions.set(SystmSession(token = jwtToken, expiresAt = expirationTime))
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| OAuth2 flow | GraphQL mutation | Current phase | Simpler, matches Systm web app |
| Token in memory | Token in encrypted session | Existing | Persists across refreshes |
| No JWT validation | java-jwt decode + exp check | Current phase | Prevents expired token errors |

**Deprecated/outdated:**
- OAuth2 for Systm: Replaced by GraphQL login mutation (simpler, matches actual API)

## Open Questions

1. **What is the exact GraphQL schema for the Systm API?**
   - What we know: Endpoint is api.thesufferfest.com/graphql, query is GetUserPlansRange
   - What's unclear: Exact field names, mutation name for login, required variables
   - Recommendation: Use introspection query or check Systm API docs if available

2. **What is the JWT signing algorithm for Systm tokens?**
   - What we know: Standard JWT format with exp claim
   - What's unclear: HS256, RS256, or other algorithm
   - Recommendation: Start with java-jwt's default validation, adjust as needed

3. **How to handle token refresh?**
   - What we know: Auth-01 allows manual token input
   - What's unclear: Is there a refresh token mechanism?
   - Recommendation: Implement basic flow first, add refresh to backlog

## Sources

### Primary (HIGH confidence)
- Ktor HTTP Client docs - POST requests, content negotiation, headers
- java-jwt GitHub - JWT decode, expiration validation
- GraphQL spec - Error handling in HTTP 200

### Secondary (MEDIUM confidence)
- Context7: graphql-kotlin-ktor-client - Alternative client option
- Stack Overflow: Ktor JSON POST patterns

### Tertiary (LOW confidence)
- Systm API documentation - Not publicly available, need to discover via introspection

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Existing Ktor project with known dependencies
- Architecture: HIGH - Based on existing patterns in codebase
- Pitfalls: HIGH - Common GraphQL/Ktor patterns well documented

**Research date:** 2026-03-02
**Valid until:** 2026-04-02 (30 days - stable domain)
