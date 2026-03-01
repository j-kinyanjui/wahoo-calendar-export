# Codebase Structure

**Analysis Date:** 2026-03-01

## Directory Layout

```
wahoo-plan-to-calendar/
├── src/
│   ├── main/
│   │   ├── kotlin/nesski/de/
│   │   │   ├── Application.kt
│   │   │   ├── models/
│   │   │   ├── plugins/
│   │   │   └── utils/
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── kotlin/nesski/de/
├── infra/
│   └── nginx/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

## Directory Purposes

**`src/main/kotlin/nesski/de/`:**
- Purpose: Primary Kotlin source code
- Contains: Application, models, plugins, utilities
- Key files: `Application.kt`

**`src/main/kotlin/nesski/de/models/`:**
- Purpose: Data classes with serialization annotations
- Contains: WahooWorkouts, UserSession, Workouts
- Key files: `WahooWorkouts.kt`, `UserSession.kt`

**`src/main/kotlin/nesski/de/plugins/`:**
- Purpose: Ktor plugins (authentication, HTTP client, routing)
- Contains: Authentication configuration, HTTP client setup
- Key files: `WahooAuthenticationOauth2.kt`, `RestClient.kt`

**`src/main/kotlin/nesski/de/utils/`:**
- Purpose: Utility classes and custom serializers
- Contains: Date/time serialization helpers
- Key files: `InstantSerializer.kt`

**`src/main/resources/`:**
- Purpose: Application configuration
- Contains: `application.yaml` - Ktor and OAuth config

**`src/test/kotlin/nesski/de/`:**
- Purpose: Unit and integration tests
- Contains: ApplicationTest

**`infra/nginx/`:**
- Purpose: Deployment configuration
- Contains: Docker compose for nginx reverse proxy

## Key File Locations

**Entry Points:**
- `src/main/kotlin/nesski/de/Application.kt`: Main function and application module

**Configuration:**
- `src/main/resources/application.yaml`: Ktor server and OAuth settings
- `build.gradle.kts`: Dependencies and build configuration

**Core Logic:**
- `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt`: Authentication and routing (122 lines)
- `src/main/kotlin/nesski/de/plugins/RestClient.kt`: HTTP client configuration

**Models:**
- `src/main/kotlin/nesski/de/models/WahooWorkouts.kt`: API response data classes
- `src/main/kotlin/nesski/de/models/UserSession.kt`: Session data class

**Testing:**
- `src/test/kotlin/nesski/de/ApplicationTest.kt`: Test suite

## Naming Conventions

**Files:**
- PascalCase: `WahooAuthenticationOauth2.kt`, `UserSession.kt`
- Suffix pattern: `*.kt` for Kotlin source files

**Directories:**
- All lowercase: `models`, `plugins`, `utils`
- No separators: conventional package-style directories

**Functions:**
- camelCase: `getPlans()`, `getSession()`, `configureAuthentication()`

**Data Classes:**
- PascalCase: `WahooWorkouts`, `UserSession`, `Workouts`

**Constants:**
- UPPER_SNAKE_CASE: `BASE_URL`, `clientId`, `clientSecret`

## Where to Add New Code

**New Feature/Routing:**
- Primary code: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt`
- Add new routes in the `routing { }` block

**New API Endpoint:**
- Implementation: Add new route in `WahooAuthenticationOauth2.kt`
- Response model: Add to `src/main/kotlin/nesski/de/models/`

**New Data Model:**
- Implementation: Create new file in `src/main/kotlin/nesski/de/models/`
- Use `@Serializable` annotation for serialization

**New Utility:**
- Implementation: Create new file in `src/main/kotlin/nesski/de/utils/`

**New Test:**
- Implementation: Add to `src/test/kotlin/nesski/de/ApplicationTest.kt`

## Special Directories

**`gradle/`:**
- Purpose: Gradle wrapper scripts
- Generated: Yes (via `gradle wrapper`)
- Committed: Yes

**`infra/nginx/`:**
- Purpose: Reverse proxy and deployment config
- Generated: No
- Committed: Yes

**`.devenv/`:**
- Purpose: Development environment configuration
- Generated: Yes (devenv)
- Committed: Yes

---

*Structure analysis: 2026-03-01*
