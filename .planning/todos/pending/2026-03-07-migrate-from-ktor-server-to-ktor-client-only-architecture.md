---
created: 2026-03-07T19:30:27.389Z
title: Migrate from Ktor server to Ktor client-only architecture
area: general
files:
  - build.gradle.kts
  - src/main/kotlin/nesski/de/Application.kt
  - src/main/kotlin/nesski/de/modules/WahooSystmWeb.kt
  - src/main/kotlin/nesski/de/plugins/SystmGraphQLClient.kt
  - src/main/kotlin/nesski/de/services/web/AuthService.kt
  - src/main/kotlin/nesski/de/services/web/PlansService.kt
  - src/main/resources/application.yaml
  - infra/nginx/docker-compose.yml
  - gradle.properties
---

## Problem

The project currently uses a full Ktor server (Netty) to host an application that fundamentally just makes outbound HTTP requests to the Wahoo SYSTM GraphQL API. No server routes are defined, and the server primarily exists to boot, authenticate, and store a JWT token. This adds unnecessary complexity:

- A Netty server runs with no endpoints to serve
- An nginx reverse proxy (Docker Compose) exists for HTTPS termination with nothing to proxy to
- Server-side auth plugin is declared but unused
- The `application.yaml` configures server host/port that aren't meaningful for a client-only app
- The Application module uses `EngineMain` server bootstrap for what is essentially a CLI/batch process

## Solution

Remove ktor-server dependencies (`ktor-server-core-jvm`, `ktor-server-netty-jvm`, `ktor-server-auth`, `ktor-server-config-yaml`) from `build.gradle.kts`. Remove the nginx/Docker infrastructure. Restructure `Application.kt` as a standard Kotlin `main()` that directly invokes the existing `SystmAuthService` and `SystmPlansService` using the already-configured `wahooHttpClient` (CIO engine). Keep the existing client-side services, models, and GraphQL helper largely intact since they already use the Ktor client. Move configuration (credentials, etc.) to environment variables or a simple properties file instead of Ktor's `application.yaml`.
