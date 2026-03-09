## Research Summary

This phase converts fetched workouts into Apple Reminders-compatible VTODO entries inside an .ics file and delivers it via email. Planning hinges on correct iCalendar formatting, SMTP configuration, and error-handling paths.

## iCalendar / VTODO Requirements
- Use RFC 5545 iCalendar format; Apple Reminders accepts VTODO components.
- Required fields: `BEGIN:VCALENDAR`, `VERSION:2.0`, `PRODID`, `BEGIN:VTODO`, `UID`, `DTSTAMP`, `SUMMARY`, `DUE`, `END:VTODO`, `END:VCALENDAR`.
- `DUE` should be UTC or local with TZID; Apple Reminders reliably accepts UTC (`YYYYMMDDTHHMMSSZ`).
- One VTODO per workout; multiple VTODOs can exist in a single .ics file.
- Emojis are supported in `SUMMARY`; ensure UTF-8 output.

## Mapping Workouts to VTODO
- SUMMARY: `{emoji} {Workout Name}`.
- DUE: workout scheduled start or end time (confirm with prior phase data model).
- UID: deterministic (e.g., workout id + date) to avoid duplicates on re-import.

## Sport Emoji Mapping
- Yoga → 🧘🏽‍♂️
- Cycling → 🚴
- Strength → 🏋🏾‍♀️
- Fallback: no emoji or generic ✅.

## Email Delivery
- Use SMTP via a Node mailer library (e.g., nodemailer).
- Config-driven SMTP settings: host, port, secure, user, password.
- Attach generated .ics with `text/calendar; method=PUBLISH`.
- CLI flag `--email` supplies recipient.

## Failure Modes
- SMTP failure: write .ics file to disk and surface clear error message.
- Config missing: fail fast with actionable message.

## Validation Architecture
- Unit test: .ics generation produces valid VCALENDAR/VTODO blocks.
- Unit test: emoji mapping by sport.
- Integration test (mock SMTP): email send invoked with correct attachment.
- Error-path test: SMTP failure triggers file save.

## Planning Implications
- Need a dedicated ICS builder module.
- Need config schema extension for email + save path.
- CLI surface: `--email` flag validation.

## RESEARCH COMPLETE
