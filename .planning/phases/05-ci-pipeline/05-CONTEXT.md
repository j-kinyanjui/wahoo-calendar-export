# Phase 5: CI Pipeline - Context

**Gathered:** 2026-03-13
**Status:** Ready for planning

<domain>
## Phase Boundary

Automated build and test on every push and PR to main, with Gradle dependency caching. Branch protection rules configured to enforce CI-gated merges. Docker image build, GHCR publishing, and release workflows are Phase 6.

</domain>

<decisions>
## Implementation Decisions

### Branch protection strategy
- PRs to main require CI status check to pass before merge
- Direct pushes to main are blocked — all changes must go through PRs
- No review requirement (single-contributor project — can self-merge after CI passes)
- Branch protection rules configured as part of this phase via GitHub repo settings (not just documented)

### Build verification depth
- Full build pipeline: compile + test + formatting check
- Run `./gradlew build` for compile, test, and distribution packaging
- Enforce ktfmt formatting in CI — fail if code isn't formatted (ktfmtPrecommit task exists in build.gradle.kts)
- Console output for test results (no artifact upload to GitHub Actions)

### Workflow triggers
- Trigger on push to main and PRs targeting main (per requirements CI-01)
- All tests execute during CI; failing test causes workflow failure (per CI-02)

### Gradle caching
- Cache Gradle dependencies between workflow runs for faster builds (per CI-03)

### Dependabot configuration
- Configure existing dependabot.yml (currently unconfigured) for Gradle dependency updates
- Low effort — file already exists with empty package-ecosystem field

### OpenCode's Discretion
- Exact GitHub Actions workflow YAML structure and job naming
- Gradle task ordering and parallelism within the workflow
- ktfmt check vs format task selection (whichever fits CI check pattern)
- Dependabot schedule and update strategy (weekly, daily, etc.)
- JDK setup details (actions/setup-java distribution choice)
- Gradle cache configuration approach (actions/cache vs gradle-build-action)

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- `gradlew` / `gradlew.bat`: Gradle wrapper present (version 9.4.0) — CI uses wrapper directly
- `build.gradle.kts`: Kotlin DSL build with `application` plugin — `./gradlew build` compiles, tests, and packages
- `ktfmtPrecommit` task: Custom KtfmtFormatTask in build.gradle.kts — can be adapted for CI check mode
- `.github/dependabot.yml`: Skeleton file exists — needs `package-ecosystem: gradle` filled in

### Established Patterns
- Kotlin 2.3.10 with JDK 17 target (from Dockerfile: `gradle:8.4-jdk17` builder, `eclipse-temurin:17-jre`)
- 7 test files in `src/test/kotlin/nesski/de/` using `kotlin.test` / JUnit
- Ktor `MockEngine` for HTTP client test mocking
- No existing CI/CD infrastructure — clean slate

### Integration Points
- `.github/workflows/` directory does not exist — will be created
- Branch protection rules need to reference the workflow's status check name
- Phase 6 (Docker & Release) will extend or add workflows that depend on Phase 5's CI passing

</code_context>

<specifics>
## Specific Ideas

No specific requirements — open to standard approaches for GitHub Actions CI with Gradle/Kotlin projects.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 05-ci-pipeline*
*Context gathered: 2026-03-13*
