# Stack Research

**Domain:** Kotlin/Ktor GraphQL Client + JWT Token Handling
**Researched:** 2026-03-01
**Confidence:** HIGH

## Recommended Stack

### GraphQL Client

| Library | Version | Purpose | Why Recommended |
|---------|---------|---------|----------------|
| `graphql-kotlin-ktor-client` | 8.x.x (latest 9.0.0) | Type-safe GraphQL HTTP client for Ktor | Official GraphQL Kotlin client, built on Ktor HTTP Client, supports kotlinx.serialization (matching existing project setup). Type-safe models generated at build time. |
| `graphql-kotlin-hooks-provider` | (same as client) | Custom scalar support | Needed if Systm API returns custom date/time formats |

**Alternative considered:** Apollo Kotlin — More feature-rich, but heavier. GraphQL Kotlin is lighter and more idiomatic for Ktor.

### JWT Token Handling

| Library | Version | Purpose | Why Recommended |
|---------|---------|---------|----------------|
| `com.auth0:java-jwt` | 4.5.1 | Decode/validate JWT tokens | Mature, widely-used library. For client use case (adding Bearer token to requests), minimal usage needed — can simply extract token from user input. |

**Alternative considered:** None — java-jwt is the de facto standard for JVM JWT handling.

### Ktor Plugins (Already in use)

| Plugin | Version | Purpose |
|--------|---------|---------|
| `ktor-client-core` | 2.3.10 | HTTP client foundation |
| `ktor-client-cio` | 2.3.10 | Coroutine-based HTTP engine |
| `ktor-client-content-negotiation` | 2.3.10 | JSON serialization |
| `ktor-serialization-kotlinx-json` | 2.3.10 | kotlinx.serialization JSON |

## Installation

```kotlin
// build.gradle.kts additions

// GraphQL Kotlin Client
plugins {
    id("com.expediagroup.graphql") version "8.x.x"
}

dependencies {
    implementation("com.expediagroup:graphql-kotlin-ktor-client:8.x.x")
}

// JWT (only needed if validating token locally)
implementation("com.auth0:java-jwt:4.5.1")
```

**Note:** GraphQL Kotlin 9.x.x requires Kotlin 2.x. Stay on 8.x.x for Kotlin 1.9.x compatibility.

## GraphQL Client Usage Pattern

```kotlin
// 1. Define queries in src/main/resources/*.graphql files
// 2. Plugin generates type-safe Kotlin classes at build time
// 3. Use generated classes with GraphQLKtorClient

val client = GraphQLKtorClient(
    url = URL("https://api.thesufferfest.com/graphql"),
    httpClient = existingKtorClient // reuse existing Ktor client
)

// Add Bearer token via request customizer
val response = client.execute(
    GetUserPlansRange(variables = GetUserPlansRange.Variables(startDate, endDate)),
    requestCustomizer = {
        headers {
            append("Authorization", "Bearer $jwtToken")
        }
    }
)
```

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| Apollo Kotlin | Heavy, more complexity than needed for simple queries | graphql-kotlin-ktor-client |
| `kotlin-jwt` (Auth0's Kotlin wrapper) | Newer, less battle-tested than java-jwt | java-jwt |
| Manual HTTP request building | Error-prone, no type safety | GraphQL Kotlin client with code generation |

## JWT Token for Systm GraphQL

Based on PROJECT.md context:
- Systm uses Bearer JWT token authentication
- Token format contains: `{id, sessionToken, username, wahooId, wahooToken, platform, version, roles}`
- For client use: simply pass token in `Authorization: Bearer <token>` header
- No JWT validation needed server-side (Systm API handles that)

**Recommendation:** Store the JWT token as provided by user (manual input). When making GraphQL requests, add as Bearer token header. Use java-jwt only if you need to:
- Check token expiration before making requests
- Extract user info from token for logging

## Version Compatibility

| Package | Project Version | Compatible With |
|---------|-----------------|-----------------|
| graphql-kotlin-ktor-client | 8.x.x | Kotlin 1.9.x, Ktor 2.x |
| java-jwt | 4.5.1 | Java 8+, Kotlin any |
| Ktor | 2.3.10 | Kotlin 1.9.x |

## Sources

- GraphQL Kotlin Ktor Client: https://expediagroup.github.io/graphql-kotlin/docs/client/client-overview — HIGH confidence
- GraphQL Kotlin latest version: https://github.com/ExpediaGroup/graphql-kotlin/releases (9.0.0, Feb 2026) — HIGH confidence
- java-jwt: https://mvnrepository.com/artifact/com.auth0/java-jwt (4.5.1, Feb 2026) — HIGH confidence
- Ktor JWT docs: https://ktor.io/docs/server-jwt.html — HIGH confidence

---

*Stack research for: Systm GraphQL API integration*
*Researched: 2026-03-01*
