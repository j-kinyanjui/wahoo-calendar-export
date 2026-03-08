# Phase 3: ICS Export & Email - Context

**Gathered:** 2026-03-08
**Status:** Ready for planning

<domain>
## Phase Boundary

Generate .ics VTODO entries from fetched workouts and save them to a file. Each VTODO will have a due date (date only, no time), sport emoji in the SUMMARY, and be Apple Reminders compatible. Emailing is deferred to a future phase.

</domain>

<decisions>
## Implementation Decisions

### ICS Structure & Content
- SUMMARY format: Emoji + workout name only (e.g., `🧘🏽‍♂️ Meditation`)
- Include DESCRIPTION field with workout type and Systm plan URL
- DUE-DATE format: date-only (e.g., `20260310`), no time component

### File Management
- Generate one combined .ics file per run (all workouts → single file)
- Filename format: `workouts_{range}_{date}.ics` (e.g., `workouts_2w_2026-03-08.ics`)
- File save location: current working directory by default, overridable via config file
- Prompt user before overwriting existing files with the same name

### Error Handling & Reporting
- Skip workouts with bad data; continue generating .ics for valid workouts
- Display error summary in console output (e.g., "5 workouts exported, 1 failed")
- Also write detailed error log to file for later review
- Auto-create missing directories when saving to configured paths

### Output Confirmation
- After successful generation, print the .ics file path to console

### OpenCode's Discretion
- Exact error log format and location
- Detailed DESCRIPTION template (how to format type + URL)
- Config file location and structure (can follow Phase 2 conventions)
- VTODO CREATED, LAST-MODIFIED timestamps
- iCalendar RFC compliance details

</decisions>

<specifics>
## Specific Ideas

- Keep .ics files simple and focused on Apple Reminders compatibility
- Users should be able to review generated files easily by filename (date + range visible)
- No automatic file cleanup; let user manage old exports

</specifics>

<deferred>
## Deferred Ideas

- Email delivery of .ics files — future phase or enhancement
- File archiving/retention policy — future enhancement
- Batch operations (e.g., combining multiple exports) — future enhancement

</deferred>

---

*Phase: 03-ics-export-email*
*Context gathered: 2026-03-08*
