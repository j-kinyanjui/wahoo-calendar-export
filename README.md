# Wahoo Plan to Calendar

A lightweight CLI tool that fetches your Wahoo Systm training plans and exports an .ics files for any calendar app.

## Installation

### Option 1: Local Build

**Prerequisites:**
- Java 17+
- Gradle 9.4+

**Build and run locally:**

```bash
./gradlew run --args="--help"
```

**Build Executable:**

```bash
./gradlew installDist
```

The CLI is available in `build/install/wahoo-cal/bin/wahoo-cal`.

```bash
./build/install/wahoo-cal/bin/wahoo-cal --help
```

### Option 2: Docker

**Build the Docker image:**

```bash
docker build -t wahoo-cal:latest .
```

**Run with Docker:**

```bash
docker run --rm wahoo-cal:latest --help 
```

## Configuration

Create a config file at a location of choice. For example: `~/.config/wahoo-cal/config.toml`

```toml
[credentials]
username = "your-systm-email@example.com"
password = "your-systm-password"

[output]
ics_save_path = "."
```

Or use environment variables to override some properties:

```bash
export SYSTM_USER="your-email@example.com"
export SYSTM_PASSWORD="your-password"
export SMTP_USERNAME="smtp username"
export SMTP_PASSWORD="smtp password"
export SMTP_FROM="sending email"
export SMTP_TO="receiving email"
```

## Usage

### Local Installation

**Fetch and export plans to .ics:**

```bash
./build/install/wahoo-cal/bin/wahoo-cal --range now
```

**Specify date range:**

```bash
# Using shorthand
./build/install/wahoo-cal/bin/wahoo-cal --range 1m
./build/install/wahoo-cal/bin/wahoo-cal --range 2w

# Using explicit dates (max 2 months)
wahoo-cal --from 2026-03-01 --to 2026-05-01
```

**Use custom config path:**

```bash
wahoo-cal --config /path/to/custom/config
```

### Docker

**Basic usage (2-week default range):**

```bash
docker run -it --rm \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)/output":/app/output \
  wahoo-cal:latest

docker run -it --rm \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)/output":/app/output \
  wahoo-cal:latest \
  --from 2026-03-01 --to 2026-04-01

docker run -it --rm \
  -v ~/.config/wahoo-cal/config:/app/config:ro \
  -v "$(pwd)/output":/app/output \
  -w /app \
  wahoo-cal:latest
```

The `.ics` file will be saved to the directory specified in the `config file` or a given `--out` option. 

**Email the .ics file (if configured):**

> [!WARNING]  
> This is not yet tested.

Enable email in config, then run normally. The .ics will be emailed; if SMTP fails, it saves to disk as backup.

## What You Get

A `.ics` file with all your workouts as calendar events:
- **Event Title:** Workout name + sport emoji + duration (e.g., "🚴 Sweet Spot Build 45 min")
- **Date:** All-day event (you can drag to preferred time in your calendar)
- **Type:** RFC 5545 VEVENT format (compatible with all major calendar apps)

## Development

### Run tests

```bash
./gradlew test
```
