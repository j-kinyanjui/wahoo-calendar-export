---
created: 2026-03-13T08:19:21.034Z
title: Add GitHub Action to build and package app
area: tooling
files:
  - .github/dependabot.yml
  - build.gradle.kts
  - Dockerfile
  - gradlew
---

## Problem

The project has no CI/CD pipeline. There is no GitHub Actions workflow to automatically build or package the application. The project is a Kotlin/Gradle app with a Dockerfile, so the workflow should handle Gradle builds and potentially Docker image creation. Currently only a Dependabot config exists under `.github/`.

## Solution

Create a GitHub Actions workflow (`.github/workflows/build.yml` or similar) that:
- Triggers on push/PR to main
- Sets up JDK and Gradle
- Runs the Gradle build (and tests if present)
- Packages the app (e.g., build a Docker image or produce a distributable artifact)
- Optionally publishes/pushes the Docker image to a registry
