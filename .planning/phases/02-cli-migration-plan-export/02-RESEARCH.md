# Phase 2: CLI Migration & Plan Export - Research

**Researched:** 2026-03-08
**Domain:** CLI application architecture, iCalendar VTODO generation, SMTP email, TOML configuration
**Confidence:** HIGH

## Summary

Phase 2 migrates the application from a Ktor server to a Clikt-based CLI tool. The existing services (`SystmAuthService`, `SystmPlansService`, `SystmGraphQLClient`) and models are retained with minimal changes — the primary work is removing Ktor server dependencies, wiring a new CLI entry point, and adding three new capabilities: TOML config parsing, iCalendar VTODO generation, and SMTP email delivery.

The standard stack is well-established: Clikt 5.0.3 for CLI, biweekly 0.6.8 for iCalendar generation (simpler API than ical4j for building VTODOs), ktoml 0.7.0 for TOML parsing (Kotlin-native, kotlinx.serialization-based), and Simple Java Mail 8.12.6 for SMTP (clean builder API, TLS support, attachment handling). All libraries are current, well-maintained, and available on Maven Central.

**Primary recommendation:** Use biweekly for VTODO generation (simplest API, no VTODO gotchas), ktoml for TOML (Kotlin-native, integrates with existing kotlinx.serialization), and Simple Java Mail for SMTP. Retain existing Ktor HTTP client + kotlinx.serialization for GraphQL calls — only remove Ktor server modules.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Default range when no flags specified: next 2 weeks from today
- `--range` accepts shorthands: `now`, `1w`, `2w`, `1m`, `2m` (always start from today)
- `--from` / `--to` accepts ISO 8601 dates only (YYYY-MM-DD)
- Both `--range` and `--from`/`--to` are supported, but using them together is an error with a clear message: "Cannot use --range with --from/--to. Use one or the other."
- Maximum range enforced: 2 months
- Single .ics file (one VCALENDAR containing all VTODOs)
- SUMMARY format: sport emoji + workout name + type, e.g. `SUMMARY:🚴 The Wretched (Cycling)`
- Sport emoji mapping by workout type:
  - Yoga: 🧘🏽‍♂️
  - Cycling: 🚴
  - Strength: 🏋🏾‍♀️
  - Other types: researcher to investigate available types and propose emoji mapping
- Completed workouts included with VTODO STATUS:COMPLETED (not filtered out)
- DESCRIPTION field: empty (no description/notes)
- DUE field: workout planned date
- Optional exploration: include Wahoo SYSTM workout URL in VTODO URL field for deep-linking to the SYSTM app (researcher to investigate URL format and feasibility)
- SMTP credentials: env vars (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS`) with fallback to config file
- Email subject: date-range based, e.g. "Wahoo Training Plan: Mar 8 - Mar 22, 2026"
- Email body: plain text summary listing workout count and date range, e.g. "8 workouts from Mar 8-22. Open the attachment to import."
- .ics file attached to email
- .ics save location is configurable in config file (general setting, not just failure fallback)
- On email send failure: save .ics to configured location, display error with instructions
- Config file format: TOML
- Default config location: `~/.config/wahoo-cli/config`
- Custom config location via `--config` flag
- Credential precedence: env vars (`SYSTM_USER`, `SYSTM_PASSWORD`) override config file values
- First-run experience (no creds found): interactive prompt for username/password, offer to save to config file
- Secrets stored in plain text with warning about file permissions (recommend chmod 600)
- VTODO SUMMARY should use skin-toned emoji variants as specified: 🧘🏽‍♂️ (yoga), 🚴 (cycling), 🏋🏾‍♀️ (strength)
- Config file follows XDG convention (`~/.config/wahoo-cli/config`)

### OpenCode's Discretion
- CLI output formatting (colors, progress indicators)
- Exact SMTP connection handling (TLS, timeouts)
- .ics file naming convention
- Emoji mapping for workout types beyond yoga/cycling/strength
- Error message wording and exit codes

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| PARSE-01 | Application extracts workout name from GraphQL response | Existing `Workout.name` field in `Models.kt:48-53`; no new library needed |
| PARSE-02 | Application extracts planned date from GraphQL response | Existing `Workout.scheduledDate` field in `Models.kt:54`; parsed as String, convert to LocalDate |
| PARSE-03 | Application handles workout types (type field) | Existing `Workout.type` field in `Models.kt:58`; used for emoji mapping in VTODO SUMMARY |
| PARSE-04 | Application handles workout status (completed, planned, etc.) | Existing `Workout.status` field in `Models.kt:60`; mapped to VTODO STATUS property |
| DISP-01 | User can view list of fetched training plans | CLI console output via Clikt `echo()`; display plan/workout list after fetch |
| DISP-02 | Each plan shows name and scheduled date | Format each workout with emoji + name + date in CLI output |
| DISP-03 | Application displays error messages for failed requests | Existing `GraphQLException` in `PlansService.kt:25-27`; surface via Clikt error handling |
| CLI-01 | CLI accepts --email, --range, --from/--to, --config options | Clikt `option()` with custom types and mutually exclusive groups |
| CLI-02 | CLI loads config from TOML file with env var overrides | ktoml `TomlFileReader.decodeFromFile()` + env var precedence logic |
| EXPORT-01 | Application generates .ics file with VTODO entries and emails it | biweekly for VTODO generation + Simple Java Mail for SMTP delivery |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Clikt | 5.0.3 | CLI framework | Kotlin-native, typed options, prompts, env var support, auto-help generation |
| biweekly | 0.6.8 | iCalendar VTODO generation | Simplest API for building iCal components; supports VTODO, no parsing overhead |
| ktoml-core | 0.7.0 | TOML deserialization | Kotlin Multiplatform, kotlinx.serialization integration, no Java deps |
| ktoml-file | 0.7.0 | TOML file reading | File reading companion to ktoml-core, uses okio |
| Simple Java Mail | 8.12.6 | SMTP email sending | Clean builder API, TLS/STARTTLS, attachment support, well-maintained |

### Supporting (retained from Phase 1)
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Ktor Client (CIO) | 3.4.1 | HTTP client for GraphQL | Retained for API calls to api.thesufferfest.com |
| kotlinx-serialization-json | (via Ktor) | JSON serialization | Retained for GraphQL request/response handling |
| logback-classic | 1.5.32 | Logging | Retained for SLF4J logging |
| java-jwt | 4.5.1 | JWT parsing | Retained if JWT inspection is needed |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| biweekly | ical4j 4.1.1 | ical4j is more powerful (full RFC5545 compliance, timezone registry) but heavier; biweekly has simpler builder API for generating VTODOs without parsing |
| ktoml | tomlj (Java) | tomlj is the reference TOML parser for JVM but not Kotlin-native; ktoml integrates with kotlinx.serialization which the project already uses |
| Simple Java Mail | Jakarta Mail (raw) | Jakarta Mail is lower-level; Simple Java Mail wraps it with builder API and handles TLS/auth boilerplate |

**Installation (Gradle Kotlin DSL):**
```kotlin
// NEW dependencies for Phase 2
implementation("com.github.ajalt.clikt:clikt:5.0.3")
implementation("net.sf.biweekly:biweekly:0.6.8")
implementation("com.akuleshov7:ktoml-core:0.7.0")
implementation("com.akuleshov7:ktoml-file:0.7.0")
implementation("org.simplejavamail:simple-java-mail:8.12.6")

// RETAINED from Phase 1
implementation("io.ktor:ktor-client-core:$ktor_version")
implementation("io.ktor:ktor-client-cio:$ktor_version")
implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
implementation("ch.qos.logback:logback-classic:$logback_version")

// REMOVED in Phase 2
// implementation("io.ktor:ktor-server-core-jvm")
// implementation("io.ktor:ktor-server-auth:$ktor_version")
// implementation("io.ktor:ktor-server-netty-jvm")
// implementation("io.ktor:ktor-server-config-yaml:2.3.10")
```

## Architecture Patterns

### Recommended Project Structure
```
src/main/kotlin/nesski/de/
├── Application.kt           # NEW: main() calls Clikt command
├── cli/
│   └── WahooCli.kt          # NEW: CliktCommand with options, run() orchestration
├── config/
│   └── AppConfig.kt         # NEW: TOML config data class + loading logic
├── models/
│   └── Models.kt            # RETAINED: GraphQL models (Plan, Workout, etc.)
├── plugins/
│   └── SystmGraphQLClient.kt # RETAINED: HTTP client + GraphQL execution
├── services/
│   ├── web/
│   │   ├── AuthService.kt   # RETAINED: SystmAuthService (login)
│   │   └── PlansService.kt  # RETAINED: SystmPlansService (fetchPlans)
│   ├── IcsGeneratorService.kt # NEW: VTODO .ics generation via biweekly
│   └── EmailService.kt      # NEW: SMTP email sending via Simple Java Mail
└── utils/
    ├── ContentNegotiationConfig.kt # RETAINED: AnySerializer
    └── DateRangeParser.kt   # NEW: Parse --range/--from/--to into date range
```

### Files to DELETE
```
src/main/kotlin/nesski/de/modules/WahooSystmWeb.kt  # Ktor Application extension
src/main/resources/application.yaml                   # Ktor server config
infra/nginx/docker-compose.yml                        # nginx reverse proxy
```

### Pattern 1: Single CliktCommand Entry Point
**What:** Single command (no subcommands) that orchestrates the entire workflow
**When to use:** Simple batch CLI tools with a single operation flow
**Example:**
```kotlin
// Source: https://ajalt.github.io/clikt/ (verified 2026-03-08)
class WahooCli : CliktCommand(
    name = "wahoo-cli",
    help = "Fetch Wahoo SYSTM training plans and export as .ics tasks"
) {
    // Options
    val email by option("--email", "-e", help = "Email address to send .ics file")
    val range by option("--range", "-r", help = "Time range: now, 1w, 2w, 1m, 2m")
    val from by option("--from", help = "Start date (YYYY-MM-DD)")
    val to by option("--to", help = "End date (YYYY-MM-DD)")
    val configPath by option("--config", "-c", help = "Config file path")
        .default("~/.config/wahoo-cli/config")

    override fun run() {
        // 1. Load config (TOML file + env var overrides)
        // 2. Resolve credentials (env > config > interactive prompt)
        // 3. Validate date range options (mutually exclusive check)
        // 4. Authenticate with SYSTM API
        // 5. Fetch plans for date range
        // 6. Generate .ics VTODO file
        // 7. Email .ics or save to disk
    }
}

fun main(args: Array<String>) = WahooCli().main(args)
```

### Pattern 2: TOML Config with kotlinx.serialization
**What:** Typed config file using ktoml + @Serializable data classes
**When to use:** Loading structured config from TOML files
**Example:**
```kotlin
// Source: https://github.com/orchestr7/ktoml (verified 2026-03-08)
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.file.TomlFileReader
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AppConfig(
    val credentials: CredentialsConfig = CredentialsConfig(),
    val smtp: SmtpConfig = SmtpConfig(),
    val output: OutputConfig = OutputConfig()
)

@Serializable
data class CredentialsConfig(
    val username: String = "",
    val password: String = ""
)

@Serializable
data class SmtpConfig(
    val host: String = "",
    val port: Int = 587,
    val user: String = "",
    val pass: String = ""
)

@Serializable
data class OutputConfig(
    @SerialName("ics_save_path")
    val icsSavePath: String = "."
)

// Loading:
val config = TomlFileReader.decodeFromFile<AppConfig>(
    serializer = AppConfig.serializer(),
    tomlFilePath = configPath
)
```

Corresponding TOML file:
```toml
[credentials]
username = "user@example.com"
password = "your-password"

[smtp]
host = "smtp.example.com"
port = 587
user = "smtp-user"
pass = "smtp-password"

[output]
ics_save_path = "/tmp/wahoo"
```

### Pattern 3: VTODO Generation with biweekly
**What:** Building iCalendar VTODO components programmatically
**When to use:** Generating .ics files with task entries
**Example:**
```kotlin
// Source: https://github.com/mangstadt/biweekly (verified 2026-03-08)
import biweekly.ICalendar
import biweekly.component.VTodo
import biweekly.property.Status
import biweekly.Biweekly
import java.time.LocalDate
import java.util.Date

fun generateIcs(workouts: List<Workout>): String {
    val ical = ICalendar()
    ical.setProductId("-//Wahoo CLI//SYSTM Plan Export//EN")

    for (workout in workouts) {
        val vtodo = VTodo()
        vtodo.setSummary("${emojiFor(workout.type)} ${workout.name} (${workout.type})")

        // DUE date
        val dueDate = LocalDate.parse(workout.scheduledDate)
        val due = biweekly.util.com.google.ical.values.DateValue(
            dueDate.year, dueDate.monthValue, dueDate.dayOfMonth
        )
        vtodo.setDateDue(Date.from(dueDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))

        // STATUS
        if (workout.status == "completed") {
            vtodo.setStatus(Status.completed())
        } else {
            vtodo.setStatus(Status.needsAction())
        }

        // UID (unique identifier)
        vtodo.setUid(workout.id)

        ical.addTodo(vtodo)
    }

    return Biweekly.write(ical).go()
}
```

### Pattern 4: Email with Attachment via Simple Java Mail
**What:** Sending email with .ics file attachment
**When to use:** SMTP email delivery with file attachments
**Example:**
```kotlin
// Source: https://www.simplejavamail.org/ (training data, MEDIUM confidence)
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder

fun sendIcsEmail(
    recipientEmail: String,
    subject: String,
    body: String,
    icsContent: String,
    smtpConfig: SmtpConfig
) {
    val email = EmailBuilder.startingBlank()
        .from("Wahoo CLI", smtpConfig.user)
        .to(recipientEmail)
        .withSubject(subject)
        .withPlainText(body)
        .withAttachment(
            "wahoo-plan.ics",
            icsContent.toByteArray(Charsets.UTF_8),
            "text/calendar"
        )
        .buildEmail()

    val mailer = MailerBuilder
        .withSMTPServer(smtpConfig.host, smtpConfig.port, smtpConfig.user, smtpConfig.pass)
        .withTransportStrategy(TransportStrategy.SMTP_TLS)
        .buildMailer()

    mailer.sendMail(email)
}
```

### Pattern 5: Env Var Override for Config Values
**What:** Env vars take precedence over config file values
**When to use:** Credential and config management with multiple sources
**Example:**
```kotlin
fun resolveCredentials(config: AppConfig): Pair<String, String> {
    val username = System.getenv("SYSTM_USER") ?: config.credentials.username
    val password = System.getenv("SYSTM_PASSWORD") ?: config.credentials.password
    return Pair(username, password)
}

fun resolveSmtpConfig(config: AppConfig): SmtpConfig {
    return SmtpConfig(
        host = System.getenv("SMTP_HOST") ?: config.smtp.host,
        port = System.getenv("SMTP_PORT")?.toIntOrNull() ?: config.smtp.port,
        user = System.getenv("SMTP_USER") ?: config.smtp.user,
        pass = System.getenv("SMTP_PASS") ?: config.smtp.pass
    )
}
```

### Pattern 6: Interactive First-Run Credential Prompt
**What:** Clikt's built-in prompt for collecting credentials on first run
**When to use:** No credentials found in env vars or config file
**Example:**
```kotlin
// Source: https://ajalt.github.io/clikt/options/#prompting-for-input (verified 2026-03-08)
fun promptForCredentials(command: CliktCommand): Pair<String, String> {
    command.echo("No credentials found. Please enter your SYSTM credentials.")
    val username = command.prompt("SYSTM username (email)")!!
    val password = command.prompt("SYSTM password", hideInput = true)!!

    val save = command.confirm("Save credentials to config file?") ?: false
    if (save) {
        // Write to config file with chmod 600 warning
    }
    return Pair(username, password)
}
```

### Anti-Patterns to Avoid
- **Retaining Ktor server Application extensions:** The `WahooSystmWeb.kt` module uses `Application.wahooSystmWeb()` which depends on `environment.config` (Ktor's YAML config). This must be replaced entirely, not adapted.
- **Using Ktor's config system for TOML:** Ktor's config is YAML/HOCON-based. Don't try to adapt it — use ktoml directly.
- **Blocking coroutines at top level:** The existing code uses `runBlocking` in `WahooSystmWeb.kt`. In the CLI context, `runBlocking` in `main()` is acceptable since there's no event loop to block.
- **Hardcoding the Ktor plugin for serialization plugin version:** The build.gradle.kts uses `id("io.ktor.plugin")` which pulls in Ktor server assumptions. Remove this plugin and manage the `application` plugin directly.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| iCalendar VTODO generation | Custom string templates for .ics | biweekly library | RFC 5545 compliance requires proper line folding (75 chars), UID generation, PRODID, and escape sequences for special characters |
| SMTP email with TLS | Raw javax.mail/jakarta.mail socket code | Simple Java Mail | TLS negotiation, auth mechanisms (PLAIN, LOGIN, XOAUTH2), attachment MIME encoding are complex |
| TOML parsing | Manual file parsing or regex | ktoml | TOML has edge cases (multiline strings, inline tables, escape sequences) that are error-prone to hand-roll |
| CLI option parsing | Manual args array parsing | Clikt | Help generation, error messages, type conversion, env var reading, mutual exclusion — all handled |
| Date range calculation | Manual date arithmetic | java.time.LocalDate | Built-in `.plusWeeks()`, `.plusMonths()`, `ChronoUnit.DAYS.between()` handle edge cases correctly |

**Key insight:** The iCalendar format in particular has subtle requirements (CRLF line endings, line folding at 75 octets, property value escaping) that are easy to get wrong when hand-rolling string templates. Apple Reminders is also known to be strict about VTODO format compliance.

## Common Pitfalls

### Pitfall 1: Apple Reminders VTODO Compatibility
**What goes wrong:** Generated VTODOs don't appear in Apple Reminders, or appear with missing fields.
**Why it happens:** Apple Reminders requires specific VTODO properties: UID (unique), DTSTAMP, DUE (as DATE not DATE-TIME for all-day tasks), and STATUS. Missing UID causes duplicates; missing DTSTAMP causes rejection.
**How to avoid:** Always set UID (use workout ID), DTSTAMP (generation time), DUE (date-only value), STATUS, and SUMMARY on every VTODO. Use biweekly which handles DTSTAMP and UID automatically.
**Warning signs:** VTODOs not appearing in Reminders after import; duplicate entries.

### Pitfall 2: Ktor Plugin Removal in build.gradle.kts
**What goes wrong:** Build fails or runtime errors after removing Ktor server deps.
**Why it happens:** The `id("io.ktor.plugin")` in build.gradle.kts configures the `application` plugin and `mainClass` pointing to `io.ktor.server.netty.EngineMain`. Removing server deps without updating the plugin and mainClass causes failures.
**How to avoid:** Replace `id("io.ktor.plugin")` with standard `application` plugin. Update `mainClass` to point to new CLI entry point. Remove `ktor-server-*` dependencies but keep `ktor-client-*`.
**Warning signs:** `ClassNotFoundException: io.ktor.server.netty.EngineMain`.

### Pitfall 3: TOML Config File Not Found on First Run
**What goes wrong:** App crashes because config file doesn't exist at `~/.config/wahoo-cli/config`.
**Why it happens:** First-time users won't have the config directory or file.
**How to avoid:** Check file existence before reading. If missing, fall back to defaults + env vars. If neither env vars nor config have credentials, trigger the interactive prompt.
**Warning signs:** `FileNotFoundException` or `NoSuchFileException` on startup.

### Pitfall 4: Emoji Encoding in .ics Files
**What goes wrong:** Emoji in SUMMARY field appear garbled or cause parsing errors in Apple Reminders.
**Why it happens:** iCalendar spec requires UTF-8 encoding. If the .ics is written with a different charset or the email attachment sets wrong content-type charset, emoji break.
**How to avoid:** Always write .ics with UTF-8 charset. Set email attachment content-type to `text/calendar; charset=utf-8`. biweekly writes UTF-8 by default.
**Warning signs:** Garbled characters in Reminders app; emoji replaced with question marks.

### Pitfall 5: SMTP TLS Configuration
**What goes wrong:** Email sending fails with TLS handshake errors or authentication failures.
**Why it happens:** Different SMTP servers use different TLS strategies (STARTTLS on port 587, direct TLS on port 465, plain on port 25). Using the wrong strategy for the port causes failures.
**How to avoid:** Default to `TransportStrategy.SMTP_TLS` (STARTTLS on port 587) which is the most common. Allow port to be configured. Simple Java Mail auto-detects strategy based on port if using `withTransportModeLogging`.
**Warning signs:** `javax.mail.MessagingException`, `SSLHandshakeException`, connection timeout.

### Pitfall 6: Mutually Exclusive CLI Options (--range vs --from/--to)
**What goes wrong:** User passes both `--range 2w` and `--from 2026-03-01 --to 2026-03-15`, resulting in ambiguous behavior.
**Why it happens:** Clikt doesn't enforce mutual exclusion by default.
**How to avoid:** Validate in `run()` — check if `range` is provided alongside `from`/`to` and throw `UsageError` with clear message. Alternatively, use Clikt's `mutuallyExclusiveOptions` grouping.
**Warning signs:** Unexpected date ranges in output.

## Code Examples

Verified patterns from official sources:

### Date Range Parsing
```kotlin
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DateRange(val start: LocalDate, val end: LocalDate)

fun parseDateRange(range: String?, from: String?, to: String?): DateRange {
    val today = LocalDate.now()

    // Mutual exclusion check
    if (range != null && (from != null || to != null)) {
        throw IllegalArgumentException(
            "Cannot use --range with --from/--to. Use one or the other."
        )
    }

    // Parse --range shorthand
    if (range != null) {
        val end = when (range) {
            "now" -> today
            "1w" -> today.plusWeeks(1)
            "2w" -> today.plusWeeks(2)
            "1m" -> today.plusMonths(1)
            "2m" -> today.plusMonths(2)
            else -> throw IllegalArgumentException(
                "Invalid range: $range. Use: now, 1w, 2w, 1m, 2m"
            )
        }
        return DateRange(today, end)
    }

    // Parse --from/--to
    if (from != null || to != null) {
        val startDate = from?.let { LocalDate.parse(it) } ?: today
        val endDate = to?.let { LocalDate.parse(it) } ?: startDate.plusWeeks(2)

        // Enforce max 2 months
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
        if (daysBetween > 62) {
            throw IllegalArgumentException(
                "Date range exceeds maximum of 2 months ($daysBetween days)."
            )
        }

        return DateRange(startDate, endDate)
    }

    // Default: next 2 weeks
    return DateRange(today, today.plusWeeks(2))
}
```

### Clikt Mutually Exclusive Options Approach
```kotlin
// Source: https://ajalt.github.io/clikt/options/#mutually-exclusive-option-groups (verified 2026-03-08)
// Alternative approach using Clikt's built-in mutual exclusion
// However, since --from and --to are a pair (not a single option), manual validation
// in run() is simpler and clearer than Clikt's mutuallyExclusiveOptions for this case.
```

### build.gradle.kts Transformation
```kotlin
// BEFORE (Phase 1):
plugins {
    kotlin("jvm") version "2.3.10"
    id("io.ktor.plugin") version "3.4.1"
    kotlin("plugin.serialization") version "1.4.32"
}
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

// AFTER (Phase 2):
plugins {
    kotlin("jvm") version "2.3.10"
    application
    kotlin("plugin.serialization") version "1.4.32"
}
application {
    mainClass.set("nesski.de.ApplicationKt")
}
```

### Emoji Mapping
```kotlin
/**
 * Map workout type to sport emoji.
 * Known types from SYSTM API (needs validation against actual API responses):
 * - Yoga, Cycling, Strength are confirmed by user decisions
 * - Running, Swimming, Mental Training are plausible additional types
 */
fun emojiFor(type: String?): String = when (type?.lowercase()) {
    "yoga" -> "🧘🏽‍♂️"
    "cycling" -> "🚴"
    "strength" -> "🏋🏾‍♀️"
    "running" -> "🏃"
    "swimming" -> "🏊"
    "mental", "mental training" -> "🧠"
    else -> "🏋️"  // Generic fitness fallback
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Ktor server + Netty | Clikt CLI (no server) | Phase 2 decision | Remove ktor-server-*, application.yaml, nginx/Docker |
| YAML config (application.yaml) | TOML config (~/.config/wahoo-cli/config) | Phase 2 decision | New config format, XDG convention |
| Cookie session storage | No sessions (stateless CLI) | Phase 2 decision | Auth on every run, no persistent session |
| Ktor routes for plans display | CLI console output | Phase 2 decision | echo() instead of HTTP response |

**Deprecated/outdated:**
- `WahooSystmWeb.kt` module: Ktor Application extension, replaced by Clikt command
- `application.yaml`: Ktor server config, replaced by TOML config file
- `infra/nginx/docker-compose.yml`: Reverse proxy, no longer needed for CLI

## Existing Code Migration Analysis

### Services to RETAIN (minimal changes)
| File | Current Location | Changes Needed |
|------|-----------------|----------------|
| `SystmAuthService` | `services/web/AuthService.kt` | Move to `services/AuthService.kt`; remove Ktor logger dependency (use SLF4J directly); constructor takes HttpClient + credentials |
| `SystmPlansService` | `services/web/PlansService.kt` | Move to `services/PlansService.kt`; remove Ktor logger dependency; keep fetchPlans(token, startDate, endDate) |
| `SystmGraphQLClient` | `plugins/SystmGraphQLClient.kt` | Keep as-is or move to `client/`; `wahooHttpClient` and `TokenStorage` retained |
| `Models.kt` | `models/Models.kt` | Keep all GraphQL models; Plan, Workout, GraphQLRequest, GraphQLResponse, etc. |
| `ContentNegotiationConfig.kt` | `utils/ContentNegotiationConfig.kt` | Keep AnySerializer as-is |

### Services to CREATE
| Service | Purpose | Key Dependencies |
|---------|---------|-----------------|
| `IcsGeneratorService` | Convert List<Workout> → .ics String | biweekly |
| `EmailService` | Send .ics via SMTP | Simple Java Mail |
| `ConfigService` | Load TOML config + env var overrides | ktoml |
| `DateRangeParser` | Parse CLI range options to LocalDate pair | java.time |

### Files to DELETE
| File | Reason |
|------|--------|
| `modules/WahooSystmWeb.kt` | Ktor Application extension function |
| `src/main/resources/application.yaml` | Ktor server config |
| `infra/nginx/docker-compose.yml` | nginx reverse proxy |

## Open Questions

1. **Exact workout types from SYSTM GraphQL API**
   - What we know: The `Workout.type` field exists as a String. User confirmed yoga, cycling, strength types.
   - What's unclear: Complete list of possible type values returned by the API. Could include: running, swimming, mental training, etc.
   - Recommendation: Use the known mappings (yoga → 🧘🏽‍♂️, cycling → 🚴, strength → 🏋🏾‍♀️) and add a generic fallback emoji for unknown types. The executor can log unknown types encountered during real API calls for future mapping updates.

2. **SYSTM app deep-link URL format**
   - What we know: User requested optional investigation. Wahoo SYSTM is a desktop/mobile app.
   - What's unclear: Whether SYSTM has a URL scheme (e.g., `systm://workout/{id}`) or web URL (e.g., `https://app.wahoofitness.com/workout/{id}`).
   - Recommendation: LOW priority. Skip for initial implementation. If workout IDs from the API are stable UUIDs, a future phase could add `URL:https://app.wahoofitness.com/workouts/{id}` to VTODOs if a URL pattern is discovered. Mark as deferred.

3. **Kotlin serialization plugin version mismatch**
   - What we know: `build.gradle.kts` has `kotlin("plugin.serialization") version "1.4.32"` but Kotlin version is `2.3.10`. The serialization plugin version should match the Kotlin version.
   - What's unclear: Whether this causes issues currently (it may work if the runtime lib version is compatible).
   - Recommendation: Update serialization plugin version to match Kotlin version during the build.gradle.kts migration.

4. **`io.ktor.plugin` behavior on removal**
   - What we know: The Ktor Gradle plugin configures the `application` plugin, fatJar packaging, and Docker image generation.
   - What's unclear: Whether removing it breaks any other build functionality beyond `mainClass`.
   - Recommendation: Replace with standard `application` plugin. If fatJar is needed, add Shadow plugin separately. Test build after removal.

## Sources

### Primary (HIGH confidence)
- Maven Central API - Verified latest versions: Clikt 5.0.3, biweekly 0.6.8, ical4j 4.1.1, ktoml-core 0.7.0, Simple Java Mail 8.12.6
- https://ajalt.github.io/clikt/ - Clikt documentation: options, prompts, env vars, commands (fetched 2026-03-08)
- https://github.com/orchestr7/ktoml - ktoml documentation: deserialization, file reading, config (fetched 2026-03-08)
- https://github.com/mangstadt/biweekly - biweekly documentation: API, VTODO creation (fetched 2026-03-08)
- Existing codebase: `Models.kt`, `AuthService.kt`, `PlansService.kt`, `SystmGraphQLClient.kt` (read 2026-03-08)

### Secondary (MEDIUM confidence)
- https://github.com/ical4j/ical4j - ical4j README: version 4.x API, VTODO support (fetched 2026-03-08)
- Simple Java Mail API - version 8.12.6, builder pattern, transport strategies (Maven Central verified)
- biweekly VTODO API - Based on documented examples and README code samples

### Tertiary (LOW confidence)
- SYSTM workout type values beyond yoga/cycling/strength - inferred from domain knowledge, not verified against API
- SYSTM app deep-link URL scheme - not investigated, marked as optional exploration
- Emoji rendering in Apple Reminders VTODO imports - based on general iCalendar UTF-8 knowledge, not tested

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All library versions verified on Maven Central, APIs verified from official docs
- Architecture: HIGH - Migration path is clear; existing services are well-isolated from Ktor server
- Pitfalls: HIGH - Apple Reminders VTODO compatibility and build.gradle.kts migration are well-understood
- Code examples: MEDIUM - biweekly VTODO creation verified from docs; Simple Java Mail API from training data

**Research date:** 2026-03-08
**Valid until:** 2026-04-08 (stable libraries, 30-day window)
