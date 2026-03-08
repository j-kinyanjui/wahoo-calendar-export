# Phase 2: CLI Migration & Plan Export - Context

**Gathered:** 2026-03-08
**Status:** Ready for planning

<domain>
## Phase Boundary

Migrate from Ktor server to a Clikt-based CLI application that auto-authenticates, fetches training plans for a user-specified time range, generates .ics VTODO entries (Apple Reminders tasks), and emails them to a provided address. Ktor server and nginx/Docker infrastructure are removed. Restore, sync scheduling, and calendar export beyond .ics email are out of scope.

</domain>

<decisions>
## Implementation Decisions

### Date range defaults & UX
- Default range when no flags specified: next 2 weeks from today
- `--range` accepts shorthands: `now`, `1w`, `2w`, `1m`, `2m` (always start from today)
- `--from` / `--to` accepts ISO 8601 dates only (YYYY-MM-DD)
- Both `--range` and `--from`/`--to` are supported, but using them together is an error with a clear message: "Cannot use --range with --from/--to. Use one or the other."
- Maximum range enforced: 2 months

### VTODO content & structure
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

### Email delivery setup
- SMTP credentials: env vars (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS`) with fallback to config file
- Email subject: date-range based, e.g. "Wahoo Training Plan: Mar 8 - Mar 22, 2026"
- Email body: plain text summary listing workout count and date range, e.g. "8 workouts from Mar 8-22. Open the attachment to import."
- .ics file attached to email
- .ics save location is configurable in config file (general setting, not just failure fallback)
- On email send failure: save .ics to configured location, display error with instructions

### Credential management
- Config file format: TOML
- Default config location: `~/.config/wahoo-cli/config`
- Custom config location via `--config` flag
- Credential precedence: env vars (`SYSTM_USER`, `SYSTM_PASSWORD`) override config file values
- First-run experience (no creds found): interactive prompt for username/password, offer to save to config file
- Secrets stored in plain text with warning about file permissions (recommend chmod 600)

### OpenCode's Discretion
- CLI output formatting (colors, progress indicators)
- Exact SMTP connection handling (TLS, timeouts)
- .ics file naming convention
- Emoji mapping for workout types beyond yoga/cycling/strength
- Error message wording and exit codes

</decisions>

<specifics>
## Specific Ideas

- VTODO SUMMARY should use skin-toned emoji variants as specified: 🧘🏽‍♂️ (yoga), 🚴 (cycling), 🏋🏾‍♀️ (strength)
- SYSTM app deep-link in VTODO URL field is an optional exploration item -- researcher should investigate whether SYSTM has a URL scheme and what the workout URL format looks like
- Config file follows XDG convention (`~/.config/wahoo-cli/config`)

</specifics>

<deferred>
## Deferred Ideas

None -- discussion stayed within phase scope

</deferred>

---

*Phase: 02-cli-migration-plan-export*
*Context gathered: 2026-03-08*
