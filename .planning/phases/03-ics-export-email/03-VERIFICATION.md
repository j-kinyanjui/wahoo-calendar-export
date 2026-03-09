---
phase: 03-ics-export-email
verified: 2026-03-09T16:23:29Z
status: gaps_found
score: 5/6 must-haves verified
gaps:
  - truth: ".ics file emailed to the provided --email address via SMTP"
    status: failed
    reason: "SMTP sending exists, but CLI has no --email option; recipient is only configurable via config/email.to_address or SMTP_TO env var."
    artifacts:
      - path: "src/main/kotlin/nesski/de/cli/WahooCli.kt"
        issue: "Missing --email CLI option and no wiring from CLI arg to EmailService recipient"
      - path: "src/main/kotlin/nesski/de/email/EmailService.kt"
        issue: "send() consumes EmailConfig.toAddress (or SMTP_TO), not a CLI-provided --email value"
    missing:
      - "Add --email option to CLI"
      - "Wire --email value to EmailService recipient (highest precedence over config/env)"
      - "Add tests proving --email controls destination address"
---

# Phase 3: ICS Export & Email Verification Report

**Phase Goal:** Generate .ics VTODO entries (Apple Reminders tasks) from fetched workouts and email them to a provided address  
**Verified:** 2026-03-09T16:23:29Z  
**Status:** gaps_found  
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
| --- | --- | --- | --- |
| 1 | Each workout generates a VTODO .ics entry with due date (Apple Reminders compatible) | ✓ VERIFIED | `IcsBuilder` emits `BEGIN:VTODO` + `DUE;VALUE=DATE:yyyyMMdd` (`src/main/kotlin/nesski/de/ics/IcsBuilder.kt:131-135`); tests validate VCALENDAR/VTODO and date-only DUE (`src/test/kotlin/nesski/de/ics/IcsBuilderTest.kt:55-63`). |
| 2 | .ics file emailed to the provided `--email` address via SMTP | ✗ FAILED | SMTP mailer exists (`EmailService.send`, `src/main/kotlin/nesski/de/email/EmailService.kt:40-109`) and is called by CLI (`WahooCli.kt:128`), but CLI defines no `--email` option (`WahooCli.kt:31-35`; repo search found no `--email`). |
| 3 | Sport emoji in VTODO SUMMARY (🧘🏽‍♂️ yoga, 🚴 cycling, 🏋🏾‍♀️ strength) | ✓ VERIFIED | SUMMARY built as `"$emoji $workoutName"` using `SportEmoji.forType(...)` (`IcsBuilder.kt:172-177`); emoji mappings include yoga/cycling/strength categories (`SportEmoji.kt:12-16`); integration tests assert emoji in SUMMARY lines (`SummaryFormattingTest.kt:145-171`). |
| 4 | On email failure, .ics saved to disk with error message | ✓ VERIFIED | On send failure CLI prints `Email failed: ...` then calls disk save (`WahooCli.kt:136-140`); disk write implemented in `IcsFileWriter.write` (`IcsFileWriter.kt:22-33`). |
| 5 | .ics save location configurable in config file | ✓ VERIFIED | Config model exposes `output.ics_save_path` (`AppConfig.kt:59-62`), template config defines it (`config.toml:4-6`), CLI uses `config.output.icsSavePath` for save target (`WahooCli.kt:139,143`). |
| 6 | Clear error messages for email delivery issues | ✓ VERIFIED | Explicit messages for disabled SMTP and missing required fields (`EmailService.kt:46-65`) plus runtime failure prefix `Failed to send email:` (`EmailService.kt:105`); tests assert message quality (`EmailServiceTest.kt:26-47,78-93`). |

**Score:** 5/6 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
| --- | --- | --- | --- |
| `src/main/kotlin/nesski/de/ics/IcsBuilder.kt` | Build RFC5545 VCALENDAR/VTODO with date-only DUE and SUMMARY | ✓ VERIFIED | Exists, substantive implementation, invoked from CLI (`WahooCli.kt:114`). |
| `src/main/kotlin/nesski/de/ics/SportEmoji.kt` | Sport→emoji mapping for SUMMARY | ✓ VERIFIED | Exists, non-stub mapping table, used by `IcsBuilder.formatSummary` (`IcsBuilder.kt:175`). |
| `src/main/kotlin/nesski/de/email/EmailService.kt` | SMTP send with .ics attachment + errors | ✓ VERIFIED | Exists, builds/sends SMTP message with attachment and structured failures. |
| `src/main/kotlin/nesski/de/ics/IcsFileWriter.kt` | Disk fallback writer | ✓ VERIFIED | Exists, creates directories and writes UTF-8 file; called by CLI (`WahooCli.kt:161`). |
| `src/main/kotlin/nesski/de/config/AppConfig.kt` + `src/main/resources/config.toml` | Configurable output path/email settings | ✓ VERIFIED | TOML schema + defaults + CLI consumption confirmed. |
| `src/main/kotlin/nesski/de/cli/WahooCli.kt` | Wire fetch→ICS→email/fallback and accept provided recipient | ⚠️ PARTIAL | Wiring exists for build/send/fallback, but no `--email` argument path to recipient address. |

### Key Link Verification

| From | To | Via | Status | Details |
| --- | --- | --- | --- | --- |
| `WahooCli` | `IcsBuilder` | `IcsBuilder.build(items)` | ✓ WIRED | `WahooCli.kt:114` |
| `IcsBuilder` | `SportEmoji` | `SportEmoji.forType(sportType)` | ✓ WIRED | `IcsBuilder.kt:175` |
| `WahooCli` | `EmailService` | `EmailService.send(config.email, icsContent, filename)` | ✓ WIRED | `WahooCli.kt:128-132` |
| `WahooCli` | `IcsFileWriter` | `saveIcsToDisk` → `IcsFileWriter.write(...)` | ✓ WIRED | `WahooCli.kt:139-143,160-163` |
| `config.toml`/`AppConfig` | disk output path | `output.ics_save_path` → `config.output.icsSavePath` | ✓ WIRED | `AppConfig.kt:59-62`, `WahooCli.kt:139,143` |
| CLI user input | email recipient | `--email` → `EmailService` destination | ✗ NOT_WIRED | No `--email` option declared in CLI; recipient only from config/env. |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| --- | --- | --- | --- | --- |
| EXPORT-01 | `03-01-PLAN.md`, `03-02-PLAN.md` | Generate RFC 5545-compliant .ics with VTODO entries (Apple Reminders compatible, sport emoji in SUMMARY, date-only DUE) | ✓ SATISFIED | `IcsBuilder.kt` produces VTODO + date-only DUE + SUMMARY emoji; tests verify behavior (`IcsBuilderTest`, `SummaryFormattingTest`). |

Orphaned requirements for Phase 3: **None** (REQUIREMENTS traceability maps only `EXPORT-01` to Phase 3, and both plans declare `EXPORT-01`).

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| --- | --- | --- | --- | --- |
| _None_ | - | No TODO/FIXME placeholders, empty impls, or log-only stubs found in relevant phase files | ℹ️ Info | No blocker anti-patterns detected |

### Gaps Summary

Core export/email pipeline is implemented and wired (ICS generation, SMTP send, disk fallback, configurable save path, and clear messaging).  
However, the phase criterion requiring delivery to a **provided `--email` address** is not met because CLI does not expose `--email` and does not wire any such argument into recipient selection.

---

_Verified: 2026-03-09T16:23:29Z_  
_Verifier: OpenCode (gsd-verifier)_
