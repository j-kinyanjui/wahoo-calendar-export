---
status: complete
phase: 02-cli-migration-plan-export
source: 02-01-SUMMARY.md, 02-02-SUMMARY.md, 02-03-SUMMARY.md, 02-04-SUMMARY.md
started: 2026-03-08T18:45:00Z
updated: 2026-03-08T18:53:00Z
---

## Current Test

[testing complete]

## Tests

### 1. CLI runs and shows help
expected: Running `./gradlew run --args="--help"` displays Clikt help text with --range, --from/--to, --config options
result: pass

### 2. CLI accepts date range shorthand
expected: Running with `--range now` returns today's plans; `--range 1w` returns plans for the past week
result: pass

### 3. CLI accepts explicit date range
expected: Running with `--from 2026-03-01 --to 2026-03-08` fetches plans within that date range
result: pass

### 4. CLI loads credentials from environment variables
expected: Setting SYSTM_USER and SYSTM_PASSWORD env vars, then running CLI without --config allows authentication without prompting
result: pass

### 5. CLI loads credentials from config file
expected: Creating ~/.config/wahoo-cli/config with credentials, running CLI loads them (no prompt) and authenticates
result: pass

### 6. CLI displays formatted workout list
expected: After authentication and fetch, CLI displays workouts with name, date, type, and status in a clean table/list format
result: pass

### 7. CLI handles auth failure gracefully
expected: Running with invalid credentials shows "Auth failed" or similar error message and exits with non-zero code
result: pass

### 8. CLI handles date range validation
expected: Running with `--from 2026-05-01 --to 2026-07-15` (>2 months) shows error about date range exceeding 62 days
result: pass

## Summary

total: 8
passed: 8
issues: 0
pending: 0
skipped: 0

## Gaps

[none yet]
