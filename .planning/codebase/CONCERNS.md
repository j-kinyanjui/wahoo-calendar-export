# Codebase Concerns

**Analysis Date:** 2026-03-01

## Tech Debt

**Broken Test Suite:**
- Issue: Test file calls `configureRouting()` which doesn't exist
- Files: `src/test/kotlin/nesski/de/ApplicationTest.kt`
- Impact: Tests fail immediately - test calls `configureRouting()` on line 14, but the actual function is `configureAuthentication()` in `WahooAuthenticationOauth2.kt`
- Fix approach: Replace `configureRouting()` with `configureAuthentication()` or import the correct function

**Inconsistent Kotlin Versions:**
- Issue: Kotlin version mismatch between build files
- Files: `build.gradle.kts` (line 6: "1.9.22"), `gradle.properties` (line 2: "1.9.23")
- Impact: Potential build inconsistencies
- Fix approach: Unify to single version (recommend 1.9.23)

**Hardcoded Environment URLs:**
- Issue: Multiple hardcoded URLs for different environments scattered in code
- Files: 
  - `src/main/kotlin/nesski/de/models/UserSession.kt` (line 17: "http://wahoo.calendar:8484/login")
  - `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt` (line 46: "https://wahoo.nesski.com/callback", line 88: "http://localhost:8484/login")
- Impact: Different URLs for development vs production baked into source; requires code changes to deploy to new environment
- Fix approach: Externalize to configuration (application.yaml)

**Incomplete Data Model:**
- Issue: Trailing comma and commented field in data class
- Files: `src/main/kotlin/nesski/de/models/WahooWorkouts.kt` (line 30: "// val workout_summary: null,")
- Impact: Unclear if workout_summary was intentionally omitted or forgotten
- Fix approach: Either remove comment or add the field properly

## Known Bugs

**Missing Error Response in /home Route:**
- Symptoms: When user has no session, they're redirected to login. But if session exists but is invalid/malformed, no response is sent
- Files: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt` (lines 90-95)
- Trigger: Call `/home` with corrupted session cookie
- Workaround: Clear cookies and re-login

**Unused Route Parameter:**
- Symptoms: The `{path}` route parameter is captured but never used
- Files: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt` (line 96: `get("/{path}")`)
- Trigger: Any request to dynamic paths - parameter is ignored
- Workaround: None needed for current functionality

## Security Considerations

**HAR File with Credentials:**
- Risk: 34MB HAR file exists in project root containing captured network traffic
- Files: `systm.wahoofitness.com.har` (root directory)
- Current mitigation: None - file is committed to git
- Recommendations: Remove from git history and add to .gitignore

**Session Cookie Without httpOnly:**
- Risk: Cookie can be accessed via JavaScript XSS attacks
- Files: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt` (line 35: `cookie<UserSession>("wahoo_user_session")`)
- Current mitigation: None
- Recommendations: Add `httpOnly = true` to cookie configuration

**No OAuth Credentials Validation:**
- Risk: Application starts with empty credentials, fails silently on OAuth flow
- Files: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt` (lines 39-41)
- Current mitigation: None
- Recommendations: Validate credentials at startup and fail fast if missing

**Insecure Redirect URL in Session:**
- Risk: Redirect URL stored in mutable map without validation - open redirect vulnerability
- Files: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt` (lines 58-61, 78-80)
- Current mitigation: None - any URL from query param is stored and used
- Recommendations: Validate redirect URLs are safe (same origin) before storing

## Performance Bottlenecks

**Memory Leak in Redirect Map:**
- Problem: `mutableMapOf<String, String>` accumulates entries but never cleans them up
- Files: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt` (line 38)
- Cause: State -> redirectUrl mapping stored but never removed after use
- Improvement path: Use a cache with TTL or remove entries after redirect

## Fragile Areas

**OAuth Flow State Management:**
- Why fragile: Uses in-memory mutable map for OAuth state tracking; doesn't work with multiple instances
- Files: `src/main/kotlin/nesski/de/plugins/WahooAuthenticationOauth2.kt`
- Safe modification: Ensure single-instance deployment or use distributed cache
- Test coverage: No tests for OAuth flow

**InstantSerializer No Error Handling:**
- Why fragile: Instant.parse() will throw on invalid input with no graceful fallback
- Files: `src/main/kotlin/nesski/de/utils/InstantSerializer.kt`
- Safe modification: Wrap in try-catch and return null or default value
- Test coverage: No tests for malformed dates

## Dependencies at Risk

**Ktor Version Mismatch:**
- Risk: Different Ktor versions referenced across build files
- Impact: Potential runtime issues from version incompatibility
- Migration plan: Use single version from gradle.properties (2.3.10) consistently

**Outdated Serialization Plugin:**
- Risk: kotlin.serialization plugin version 1.4.32 in build.gradle.kts vs Kotlin 1.9.22
- Impact: May have bugs fixed in newer versions
- Migration plan: Update to version compatible with Kotlin 1.9.x

## Missing Critical Features

**No Calendar Export:**
- Problem: Project goal is "Plan to Calendar" but no calendar integration exists
- Blocks: Cannot export Wahoo workouts to any calendar (Google, Apple, Outlook)

**No Workout Data Display:**
- Problem: Only returns count of workouts, not actual workout data
- Blocks: Cannot display workout details to users

**No Logout Functionality:**
- Problem: No way to clear session
- Blocks: Users stuck logged in until cookie expires

## Test Coverage Gaps

**No Unit Tests:**
- What's not tested: All business logic - serializers, models, OAuth flow
- Files: Entire `src/main/kotlin/` has no test coverage
- Risk: Silent failures in data parsing and API integration
- Priority: High

**No Integration Tests:**
- What's not tested: OAuth flow end-to-end, API client calls
- Files: `src/test/kotlin/nesski/de/ApplicationTest.kt` is broken
- Risk: Broken authentication not caught before deployment
- Priority: High

---

*Concerns audit: 2026-03-01*
