# Roadmap: Wahoo Plan to Calendar

## Milestones

- ✅ **v1.0 MVP** — Phases 1-4 (shipped 2026-03-10)
- ◆ **v1.1 CI/CD Pipeline** — Phases 5-6 (in progress)

## Shipped Milestones

<details>
<summary>✅ v1.0 MVP (Phases 1-4) — SHIPPED 2026-03-10</summary>

- [x] Phase 1: Authentication & GraphQL Setup (3/3 plans) — completed 2026-03-02
- [x] Phase 2: CLI Migration (3/3 plans) — completed 2026-03-08
- [x] Phase 3: ICS Export & Email (2/2 plans) — completed 2026-03-09
- [x] Phase 4: Calendar Export v2 (1/1 plan) — completed 2026-03-10

**Archive:** See `.planning/milestones/v1.0-ROADMAP.md` for full details

</details>

## Active Phases — v1.1 CI/CD Pipeline

### Phase 5: CI Pipeline

**Goal:** Automated build and test on every push and PR to main, with Gradle dependency caching.

**Requirements:** CI-01, CI-02, CI-03

**Success Criteria:**
1. Pushing a commit to main triggers a GitHub Actions workflow that builds the project
2. Opening a PR to main triggers the same workflow
3. All tests run during CI; a failing test causes the workflow to fail with visible error
4. Subsequent workflow runs use cached Gradle dependencies (faster than first run)

**Dependencies:** None (first phase)

---

### Phase 6: Docker & Release Pipeline

**Goal:** Docker image build and push to GHCR on main merge, plus versioned releases triggered by git tags.

**Requirements:** CD-01, CD-02, CD-03

**Success Criteria:**
1. Merging to main builds a Docker image and pushes it to GHCR tagged `latest`
2. Pushing a `v*.*.*` git tag builds a Docker image and pushes it to GHCR with the version tag
3. Pushing a version tag creates a GitHub Release with auto-generated changelog
4. GHCR images are publicly pullable from the repository's package registry

**Dependencies:** Phase 5 (CI must pass before Docker build/push)

---

## Progress Summary

| Milestone         | Phases | Plans | Status     | Shipped    |
| ----------------- | ------ | ----- | ---------- | ---------- |
| v1.0 MVP          | 1-4    | 10    | Complete   | 2026-03-10 |
| v1.1 CI/CD        | 5-6    | TBD   | In Progress | —          |

## v1.1 Requirements Coverage

- 6 v1.1 requirements defined
- 6 mapped to phases (100%)
- 0 unmapped ✓

| Category | Requirements | Phase |
|----------|-------------|-------|
| CI       | CI-01, CI-02, CI-03 | Phase 5 |
| CD       | CD-01, CD-02, CD-03 | Phase 6 |

## v1.0 Requirements Coverage

- ✓ 17 v1.0 requirements shipped (100%)

**Categories shipped:**
- Authentication (3) — OAuth 2 / JWT token input ✓
- Data Fetching (4) — GraphQL API integration ✓
- Data Parsing (4) — Workout extraction and parsing ✓
- Display (3) — Console formatting ✓
- CLI (2) — Clikt framework with config ✓
- Export (2) — VTODO and VEVENT calendar formats ✓

---

_For v1.0 phase details, see `.planning/milestones/v1.0-ROADMAP.md`_

_Roadmap created: 2026-03-01 | Last updated: 2026-03-13 after v1.1 roadmap creation_
