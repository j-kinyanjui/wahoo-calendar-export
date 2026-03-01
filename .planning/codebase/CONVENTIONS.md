# Coding Conventions

**Analysis Date:** 2026-03-01

## Language & Framework

**Language:**
- Kotlin 1.9.22 - Primary language for all source code

**Framework:**
- Ktor 2.3.10 - Server-side framework (Netty engine)
- kotlinx.serialization - JSON serialization

## Naming Patterns

**Packages:**
- Pattern: `nesski.de.<module>.<submodule>`
- Examples: `nesski.de.plugins`, `nesski.de.models`, `nesski.de.utils`

**Files:**
- PascalCase for class/object files: `Application.kt`, `WahooWorkouts.kt`
- CamelCase for utility files: `InstantSerializer.kt`

**Functions:**
- CamelCase: `getSession()`, `configureAuthentication()`, `getPlans()`

**Variables/Properties:**
- CamelCase: `userSession`, `httpClient`, `clientId`
- Private properties may use underscore prefix in some contexts

**Classes/Data Classes:**
- PascalCase: `UserSession`, `WahooWorkouts`, `Workouts`
- Data classes used for DTOs and models

**Constants:**
- UPPER_SNAKE_CASE: `BASE_URL`

## Code Style

**Formatting:**
- Uses Kotlin official code style (per `gradle.properties`: `kotlin.code.style=official`)
- No explicit formatter config detected - relies on default IntelliJ/IDEA formatting

**Imports:**
Organized as follows:
1. Kotlin standard library (`kotlin.*`, `java.*`)
2. Ktor framework imports (`io.ktor.*`)
3. kotlinx serialization (`kotlinx.serialization`)
4. Internal project imports (`nesski.de.*`)

**Line Length:**
- Not explicitly configured - defaults apply

## Data Classes & Serialization

**DTO Pattern:**
```kotlin
@Serializable
data class WahooWorkouts(
    val total: Int,
    val page: Int,
    val order: String,
    val sort: String,
    val workouts: List<Workouts>,
)
```

**Custom Serializers:**
- Declared with `@Serializable(SerializerName::class)` annotation
- Example: `@Serializable(InstantSerializer::class)` on `Instant` fields

**Serializer Implementation:**
```kotlin
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = ...
    override fun serialize(encoder: Encoder, value: Instant) = ...
    override fun deserialize(decoder: Decoder): Instant = ...
}
```

## Error Handling

**Result Pattern:**
- Uses `runCatching` with `fold` for Result handling
```kotlin
suspend fun getPlans(httpClient: HttpClient, userSession: UserSession): WahooWorkouts =
    runCatching {
        httpClient.get("$BASE_URL/v1/plans") { ... }
    }.fold(
        onSuccess = { it.body() },
        onFailure = {
            log.error("Encountered error while getting plans: $it")
            throw it
        },
    )
```

**Null Handling:**
- Nullable types with safe calls: `UserSession?`
- Elvis operator used where needed

## Logging

**Framework:** KtorSimpleLogger
```kotlin
internal val log = KtorSimpleLogger("WahooAuthenticationOauth2")

log.error("Encountered error while getting plans: $it")
```

**Logback Configuration:**
- Pattern: `%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`
- Root level: trace
- Jetty/Netty: INFO level

## Configuration

**Environment:**
- YAML-based: `application.yaml`
- Environment variables: `System.getenv("CLIENT_ID")`
- Config property fallback: `environment.config.propertyOrNull(...).getString()`

**Application Setup:**
```kotlin
fun Application.module() {
    configureAuthentication()
}
```

## Routing & HTTP

**Ktor Routing:**
- DSL-based routing in `routing { }` block
- Authentication via `authenticate("auth-oauth-wahoo") { }`
- Extension function pattern: `call.respondRedirect()`, `call.respondText()`

**HTTP Client:**
- Ktor HttpClient with CIO engine
- Content negotiation for JSON

## Session Management

**Session Configuration:**
```kotlin
install(Sessions) {
    cookie<UserSession>("wahoo_user_session")
}
```

**Session Retrieval:**
- Companion object pattern with suspend function: `UserSession.getSession(call)`

---

*Convention analysis: 2026-03-01*
