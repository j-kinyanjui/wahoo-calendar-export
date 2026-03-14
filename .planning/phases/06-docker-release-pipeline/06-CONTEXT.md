# Phase 6: Docker & Release Pipeline - Context

**Gathered:** 2026-03-14
**Status:** Ready for planning

<domain>
## Phase Boundary

Docker image build and push to GHCR on main merge, plus versioned releases triggered by git tags. Covers requirements CD-01 (Docker image on main merge with `latest` tag), CD-02 (version tag triggers versioned image push), and CD-03 (GitHub Release with changelog on version tag). CI workflow (Phase 5) is a prerequisite — Docker builds only run after CI passes.

</domain>

<decisions>
## Implementation Decisions

### Workflow structure
- Separate workflow file (cd.yml) from existing ci.yml — cleaner separation, easier to debug/disable independently
- CI must pass before Docker build starts — Docker job uses `needs` to depend on CI
- Single workflow handles both main-merge Docker builds AND tag-triggered release builds — different image tags applied based on trigger context (push to main vs v* tag push)
- Docker build uses existing Dockerfile from scratch (multi-stage build) — no artifact passing from CI job

### Image tagging strategy
- Main merge: `latest` tag only — always points to newest main build
- Version tag (v*.*.*): full version tag only (e.g., `v1.1.0`) — no major.minor shorthand tags
- Git tag is the source of truth for versioning — build.gradle.kts version (`0.0.1`) is not kept in sync
- Standard OCI labels included on images (org.opencontainers.image.version, .revision, .created, etc.) — free metadata for debugging and auditing

### Release content & changelog
- GitHub auto-generated release notes — lists PRs and contributors since last tag, zero maintenance
- Attach Gradle distribution archive (fat JAR / distribution zip) as release artifact — for users who want to run without Docker
- All releases marked as 'latest' — no pre-release distinction needed
- Include Docker pull command in release body (e.g., `docker pull ghcr.io/j-kinyanjui/wahoo-plan-to-calendar:vX.Y.Z`)

### Image visibility & access
- GHCR images set to public — anyone can `docker pull` without auth (matches CD-04 success criteria)
- Note: package visibility must be manually set to public in GitHub package settings after first push (or via API)
- GITHUB_TOKEN used for GHCR authentication — built-in, no PAT setup needed
- Existing Dockerfile used as-is — no CI-specific optimizations needed
- Multi-platform build (amd64 + arm64) — supports M-series Macs and ARM servers natively, requires docker/buildx-action and QEMU

### OpenCode's Discretion
- Exact GitHub Actions workflow YAML structure and job naming
- docker/build-push-action vs manual docker build/push commands
- QEMU and buildx setup details for multi-platform builds
- Gradle distribution archive format (zip vs tar) and naming
- OCI label values and formatting
- Release body template structure beyond Docker pull command

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- `Dockerfile`: Multi-stage build already working — `gradle:8.4-jdk17` builder stage, `eclipse-temurin:17-jre-ubi9-minimal` runtime
- `.github/workflows/ci.yml`: Existing CI workflow with triggers on push to main, PRs, and `v*` tags — cd.yml can reference the CI workflow or the `build` job
- `.github/dependabot.yml`: Already configured for Gradle + GitHub Actions dependency updates
- `build.gradle.kts`: Has `application` plugin with `installDist` task — Gradle distribution archive available via `./gradlew distZip` or `distTar`

### Established Patterns
- Kotlin 2.3.10 with JDK 17 target
- Gradle wrapper 9.4.0 — CI uses `gradle/actions/setup-gradle@v5`
- Dockerfile uses `gradle clean installDist -x test --no-daemon` for build
- ENTRYPOINT is `/app/wahoo-cli/bin/wahoo-plan-to-calendar`

### Integration Points
- ci.yml already triggers on `v*` tags — cd.yml needs to avoid conflicting triggers or use `workflow_run` to chain after CI
- GHCR package URL: `ghcr.io/j-kinyanjui/wahoo-plan-to-calendar`
- Repo is private — GHCR package visibility must be set to public manually after first push
- Phase 5 branch protection was deferred (private repo, free plan) — no blockers for Phase 6

</code_context>

<specifics>
## Specific Ideas

No specific requirements — open to standard approaches for GitHub Actions Docker/GHCR/Release workflows.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 06-docker-release-pipeline*
*Context gathered: 2026-03-14*
