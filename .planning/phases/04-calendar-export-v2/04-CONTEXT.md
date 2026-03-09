# Phase 4: Calendar Export v2 - Context

**Gathered:** 2026-03-09
**Status:** Ready for planning

<domain>
## Phase Boundary

Migrate .ics export from VTODO (reminder) entries to VEVENT (calendar event) entries using ical4j. Workouts become all-day placeholder events that users can drag to their preferred time. No VTODO, no reminders — cross-calendar compatible (Apple, Google, Yahoo, Outlook).

</domain>

<decisions>
## Implementation Decisions

### Calendar primitive
- VEVENT only — no VTODO, no reminders
- All-day placeholder events (DATE-only DTSTART/DTEND)
- User chooses start time by dragging the event

### Duration communication
- Duration is communicated in SUMMARY text: "Workout Name (30 min)"
- Duration is also in DESCRIPTION for detail
- No enforced DURATION property (user controls timing)

### ICS generation library
- Replace hand-rolled ICS string building with ical4j
- ical4j handles RFC 5545 compliance, line folding, UTF-8
- Version: 4.0.0+ (latest stable)

### Event properties
- DTSTART;VALUE=DATE — planned date
- DTEND;VALUE=DATE — planned date + 1 day
- UID — stable workout ID + @wahoo suffix
- DTSTAMP — generation timestamp
- SUMMARY — emoji + workout name + (duration min)
- DESCRIPTION — workout details + "Drag to set a time"
- STATUS:CONFIRMED
- TRANSP:TRANSPARENT — does not block whole day
- CALSCALE:GREGORIAN

### What to remove
- All VTODO generation code
- DUE property usage
- STATUS:NEEDS-ACTION / STATUS:COMPLETED mapping
- Hand-rolled ICS string building

### OpenCode's Discretion
- Exact ical4j API usage for Kotlin
- Test structure adjustments
- Any additional ical4j configuration

</decisions>

<specifics>
## Specific Ideas

- "I want to migrate to a typical calendar event and not a reminders item"
- "Use https://www.ical4j.org/ for a much cleaner API"
- Events should import cleanly into Apple Calendar — no "Reminders" prompt

</specifics>

<deferred>
## Deferred Ideas

- Timed default start times — future phase
- VALARM notifications — future phase
- Timezone-aware scheduling — future phase
- Event update semantics (same UID) — future phase

</deferred>

---

*Phase: 04-calendar-export-v2*
*Context gathered: 2026-03-09*
