# Testing Patterns

**Analysis Date:** 2026-03-01

## Test Framework

**Test Runner:**
- Ktor Test Host (`io.ktor:ktor-server-test-host`) - 2.3.10
- Kotlin Test (`org.jetbrains.kotlin:kotlin-test`) - 1.9.23

**Assertion Library:**
- Kotlin Test assertions (`kotlin.test`)

**Build Configuration (build.gradle.kts):**
```kotlin
testImplementation("io.ktor:ktor-server-tests-jvm")
testImplementation("org.jetbrains.kotlin:kotlin-test")
testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
testImplementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
```

## Test File Organization

**Location:**
- Tests reside in `src/test/kotlin/`
- Mirrors source package structure: `src/test/kotlin/nesski/de/`

**Naming:**
- Pattern: `<ClassName>Test.kt`
- Example: `ApplicationTest.kt`

**Structure:**
```
src/test/kotlin/
└── nesski/de/
    └── ApplicationTest.kt
```

## Test Structure

**Test Class Pattern:**
```kotlin
class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            // configure test application
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
```

**Key Patterns:**
- `testApplication` block - Creates test application context
- `application { }` - Configures the application for testing
- `client` - Pre-configured HTTP client for making requests
- `apply` block - Allows chaining assertions on response

## Test Application Configuration

**Function-Based Configuration:**
- Tests call configuration functions from plugins
- Example: `configureRouting()` (note: appears to be missing in current codebase)

**Application Module:**
```kotlin
application {
    configureAuthentication()
}
```

## Assertions

**Common Assertions:**
```kotlin
assertEquals(HttpStatusCode.OK, status)
assertEquals("Expected body", bodyAsText())
```

**Available Assertion Types:**
- `assertEquals(expected, actual)`
- `assertTrue(condition)`
- `assertFalse(condition)`
- `assertNotNull(value)`
- `assertNull(value)`

## HTTP Client Testing

**Request Methods:**
- `client.get(path)`
- `client.post(path) { ... }`
- `client.put(path) { ... }`
- `client.delete(path)`

**Response Handling:**
```kotlin
val response = client.get("/")
val status = response.status
val body = response.bodyAsText()
val bodyAsBytes = response.bodyAsChannel()
```

## Mocking & Test Dependencies

**Note:** No explicit mocking framework detected in dependencies (e.g., MockK, Mockito)

**Current Test Dependencies:**
- `ktor-server-tests-jvm` - Core testing utilities
- `ktor-server-test-host` - Embedded server testing
- `ktor-server-content-negotiation` - Content negotiation testing
- `kotlin-test-junit` - JUnit integration

**Potential Additions for Enhanced Testing:**
- MockK - For mocking in Kotlin
- Kotest - Alternative test framework with more assertion styles

## Test Execution

**Run All Tests:**
```bash
./gradlew test
```

**Run with Watch:**
```bash
./gradlew test --continuous
# or
./gradlew test -t
```

**Run Single Test:**
```bash
./gradlew test --tests "nesski.de.ApplicationTest.testRoot"
```

**Generate Coverage:**
```bash
./gradlew test jacocoTestReport
# Note: jacoco plugin not currently configured
```

## Current Test Coverage

**Existing Tests:**
- `ApplicationTest.kt` - Single test file with one test case
- Test: `testRoot()` - Tests root endpoint (`/`)

**Coverage Gaps:**
- No tests for `UserSession` model
- No tests for `WahooWorkouts` serialization
- No tests for `getPlans()` function
- No tests for OAuth flow
- No tests for authentication/authorization
- No tests for error handling

## Testing Best Practices Observed

**Good Practices:**
- Uses `testApplication` for integration-style tests
- Tests use actual HTTP client for realistic testing
- Assertions on both status code and body

**Areas for Improvement:**
- Test configuration function `configureRouting()` referenced but not defined
- Missing test for `InstantSerializer`
- No unit tests for pure functions/data classes
- No test fixtures or factories
- No parameterized tests

## Test Types

**Current:**
- Integration tests via `testApplication` (main approach)

**Not Present:**
- Unit tests for data classes
- Unit tests for serializers
- Mock-based tests
- E2E tests
- Performance/load tests

---

*Testing analysis: 2026-03-01*
