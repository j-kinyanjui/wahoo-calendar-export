---
created: 2026-03-18T20:31:00.593Z
title: Migrate CI/CD from Docker build to Gradle assembleDist artifacts
area: tooling
files:
  - .github/workflows/cd.yml
---

## Problem

The current v1.1 CD pipeline (`cd.yml`) builds multi-platform Docker images and pushes them to GHCR, then creates GitHub Releases. The preference is to drop Docker entirely and instead produce distribution archives via `gradle assembleDist` (zip + tar of the CLI with launcher scripts), publishing those archives as GitHub Release assets instead.

This would simplify the pipeline — no Docker daemon, no GHCR package management, no multi-platform builds — and deliver the CLI as a self-contained distribution that users can download and run directly.

## Solution

1. Remove Docker build/push steps from `cd.yml`
2. Add `./gradlew assembleDist` step to produce `build/distributions/*.zip` and `build/distributions/*.tar`
3. Upload the resulting archives as GitHub Release assets (using `softprops/action-gh-release` or similar)
4. Update release notes / README to reflect artifact-based distribution instead of Docker image
5. Optionally remove `Dockerfile` if Docker is fully abandoned
