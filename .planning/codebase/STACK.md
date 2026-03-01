# Technology Stack

**Analysis Date:** 2026-03-01

## Languages

**Primary:**
- Kotlin 1.9.23 - Server-side application logic

**Secondary:**
- None detected

## Runtime

**Environment:**
- Java/JVM - Required for Kotlin execution

**Build Tool:**
- Gradle 8.x (via gradlew wrapper)
- Kotlin DSL: `build.gradle.kts`

**Package Manager:**
- Gradle dependencies (Maven Central)

## Frameworks

**Core:**
- Ktor 2.3.10 - Web framework (server)
  - `ktor-server-core-jvm`
  - `ktor-server-netty-jvm`
  - `ktor-server-auth` (OAuth2)
  - `ktor-server-config-yaml`
- Kotlinx Serialization 1.4.32 - JSON serialization

**Testing:**
- Ktor Test Application - Test server
- Kotlin Test - Testing framework

**Logging:**
- Logback 1.5.6 - Logging implementation

## Key Dependencies

**Ktor Server:**
- `io.ktor:ktor-server-core-jvm` - Core server functionality
- `io.ktor:ktor-server-netty-jvm` - Netty engine
- `io.ktor:ktor-server-auth` - Authentication (OAuth2)
- `io.ktor:ktor-server-config-yaml:2.3.10` - YAML configuration

**Ktor Client:**
- `io.ktor:ktor-client-core` - HTTP client
- `io.ktor:ktor-client-cio` - CIO HTTP engine
- `io.ktor:ktor-client-content-negotiation` - Content negotiation
- `io.ktor:ktor-serialization-kotlinx-json` - JSON serialization

**Serialization:**
- `org.jetbrains.kotlin:kotlin-stdlib-jdk8` - Standard library
- `kotlinx-serialization` - JSON serialization via Ktor plugin

**Logging:**
- `ch.qos.logback:logback-classic` - SLF4J implementation

## Configuration

**Environment:**
- YAML-based configuration: `src/main/resources/application.yaml`
- Environment variables fallback: `CLIENT_ID`, `CLIENT_SECRET`
- Ktor deployment config (port, host)

**Key Config Properties:**
```yaml
ktor:
  development: true
  deployment:
    port: 8484
    host: wahoo.nesski.com
oauth:
  clientId: ""  # From env
  clientSecret: ""  # From env
```

**Build Configuration:**
- `build.gradle.kts` - Gradle Kotlin DSL build file
- `gradle.properties` - Version properties
- `settings.gradle.kts` - Project settings

## Platform Requirements

**Development:**
- Java 17+ (recommended)
- Gradle (via gradlew wrapper)
- Devenv (optional, for development environment)
- Docker/Docker Compose (for nginx reverse proxy)

**Production:**
- JVM runtime
- HTTPS via nginx reverse proxy
- SSL certificates

---

*Stack analysis: 2026-03-01*
