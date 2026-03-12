# Wahoo Plan to Calendar

A lightweight CLI tool that fetches your Wahoo Systm training plans and exports them to your calendar.

## Features

- 🏃 **Fetch Plans** — Query Wahoo Systm API for training plans in any date range
- 📅 **Export to Calendar** — Generate RFC 5545-compliant .ics files for Apple Calendar, Google Calendar, Outlook, or any calendar app
- 🔐 **Secure Credentials** — Store credentials in config file with environment variable overrides for CI/CD
- ⚡ **Instant Export** — No web UI, no sync daemon — fetch and export on demand
- 📧 **Email Delivery** (optional) — Send generated .ics files to your email or save to disk

## Installation

### Prerequisites

- Java 17+
- Gradle 9.4+

Or use the Nix flake:

```bash
nix flake show
nix develop
```

### Build

```bash
./gradlew build
./gradlew installDist
```

The CLI is available in `build/install/wahoo-cli/bin/wahoo-cli`.

## Configuration

Create a config file at `~/.config/wahoo-cli/config`:

```toml
[credentials]
username = "your-systm-email@example.com"
password = "your-systm-password"

[output]
ics_save_path = "."

[email]
enabled = false
# smtp_host = "smtp.gmail.com"
# smtp_port = 587
# smtp_username = "your-email@gmail.com"
# smtp_password = "your-app-password"
# from_address = "your-email@gmail.com"
# to_address = "recipient@example.com"
```

Or use environment variables to override:

```bash
export SYSTM_USER="your-email@example.com"
export SYSTM_PASSWORD="your-password"
```

## Usage

### Fetch and export plans to .ics

```bash
wahoo-cli
# Exports to: workouts_2w_<date>.ics (past 7 days + next 14 days)
```

### Specify date range

```bash
# Using shorthand
wahoo-cli --range 1m    # Past 1 month
wahoo-cli --range 2w    # Past 2 weeks

# Using explicit dates (max 2 months)
wahoo-cli --from 2026-03-01 --to 2026-05-01
```

### Use custom config path

```bash
wahoo-cli --config /path/to/custom/config
```

### Email the .ics file (if configured)

Enable email in config, then run normally. The .ics will be emailed; if SMTP fails, it saves to disk as backup.

## What You Get

A `.ics` file with all your workouts as calendar events:
- **Event Title:** Workout name + sport emoji + duration (e.g., "🚴 Sweet Spot Build 45 min")
- **Date:** All-day event (you can drag to preferred time in your calendar)
- **Type:** RFC 5545 VEVENT format (compatible with all major calendar apps)

## Examples

### Apple Calendar

```bash
./wahoo-cli
# Open workouts_2w_2026-03-11.ics with Calendar.app
# Click "Add" → events appear in your calendar
```

### Google Calendar

```bash
./wahoo-cli --from 2026-03-01 --to 2026-04-01
# Go to Google Calendar → Settings → Import & Export → Upload workouts_*.ics
```

### Command Line (any system)

```bash
./wahoo-cli && open workouts_*.ics
# or: ./wahoo-cli && xdg-open workouts_*.ics
```

## Project Status

**v1.0 Shipped** (2026-03-10)
- ✅ Fetch training plans from Systm GraphQL API
- ✅ Parse workouts with name, date, type, and status
- ✅ Export as RFC 5545-compliant .ics (VEVENT all-day events)
- ✅ Email delivery with disk fallback
- ✅ Config file + environment variable support
- ✅ Date range validation (max 2 months)
- ✅ Clear error messages for invalid input

**Future (v1.1+)**
- Email scheduling (daily/weekly digests)
- Workout filtering by type
- Custom date range presets
- Quiet/verbose output modes

## Development

### Run tests

```bash
./gradlew test
```

### Build and run locally

```bash
./gradlew run --args="--from 2026-03-01 --to 2026-03-31"
```

### Project structure

```
src/main/kotlin/nesski/de/
├── cli/WahooCli.kt                 # CLI entry point (Clikt)
├── auth/SystmAuthService.kt        # JWT authentication
├── api/PlansService.kt             # GraphQL API client
├── config/AppConfig.kt             # Config loading + validation
├── ics/IcsBuilder.kt               # RFC 5545 calendar builder
├── email/EmailService.kt           # SMTP delivery
└── models/                         # GraphQL response models
```

## License

MIT

## Support

For issues or questions:
- Check the code in `src/main/kotlin/`
- Review test files in `src/test/kotlin/`
- See `.planning/PROJECT.md` for architecture decisions
