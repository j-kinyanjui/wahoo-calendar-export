# Wahoo Plan to Calendar

A lightweight CLI tool that fetches your Wahoo Systm training plans and exports an .ics files for any calendar app.

## Installation

### Option 1: Local Build

**Prerequisites:**
- Java 17+
- Gradle 9.4+

**Build:**

```bash
./gradlew build
./gradlew installDist
```

The CLI is available in `build/install/wahoo-cli/bin/wahoo-cli`.

### Option 2: Docker

**Build the Docker image:**

```bash
docker build -t wahoo-cli:latest .
```

**Run with Docker:**

```bash
docker run --rm wahoo-cli:latest --help 
```

```bash
# Fetch plans from the past 2 weeks and save to current directory
docker run -it --rm \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest

# Custom date range
docker run -it --rm \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest \
  --from 2026-03-01 --to 2026-04-01

# Using a config file
docker run -it --rm \
  -v ~/.config/wahoo-cli/config:/app/config:ro \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest
```

The `.ics` file will be saved to your current directory (`$(pwd)`).

### Option 3: Nix Flake

```bash
# Build the CLI
nix build ./nix

# Run directly
nix run ./nix

# Run with arguments
nix run ./nix -- --range 1m

# Show flake outputs
nix flake show ./nix
```

## Configuration

Create a config file at `~/.config/wahoo-cli/config`:

```toml
[credentials]
username = "your-systm-email@example.com"
password = "your-systm-password"

[output]
ics_save_path = "."

```

Or use environment variables to override:

```bash
export SYSTM_USER="your-email@example.com"
export SYSTM_PASSWORD="your-password"
```

## Usage

### Local Installation

**Fetch and export plans to .ics:**

```bash
wahoo-cli
# Exports to: workouts_2w_<date>.ics (past 7 days + next 14 days)
```

**Specify date range:**

```bash
# Using shorthand
wahoo-cli --range 1m    # Past 1 month
wahoo-cli --range 2w    # Past 2 weeks

# Using explicit dates (max 2 months)
wahoo-cli --from 2026-03-01 --to 2026-05-01
```

**Use custom config path:**

```bash
wahoo-cli --config /path/to/custom/config
```

### Docker

**Basic usage (2-week default range):**

```bash
docker run -it \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest
```

**Custom date range:**

```bash
docker run -it \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest \
  --from 2026-03-01 --to 2026-04-01
```

**With config file:**

```bash
docker run -it \
  -v ~/.config/wahoo-cli/config:/app/config:ro \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest
```

**Email the .ics file (if configured):**

Enable email in config, then run normally. The .ics will be emailed; if SMTP fails, it saves to disk as backup.

## What You Get

A `.ics` file with all your workouts as calendar events:
- **Event Title:** Workout name + sport emoji + duration (e.g., "🚴 Sweet Spot Build 45 min")
- **Date:** All-day event (you can drag to preferred time in your calendar)
- **Type:** RFC 5545 VEVENT format (compatible with all major calendar apps)

## Examples

### Apple Calendar (Local)

```bash
./wahoo-cli
open workouts_*.ics
```

### Apple Calendar (Docker)

```bash
docker run -it \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest

open workouts_*.ics
```

### Google Calendar (Local)

```bash
./wahoo-cli --from 2026-03-01 --to 2026-04-01
# Go to Google Calendar → Settings → Import & Export → Upload workouts_*.ics
```

### Google Calendar (Docker)

```bash
docker run -it \
  -e SYSTM_USER="your-email@example.com" \
  -e SYSTM_PASSWORD="your-password" \
  -v "$(pwd)":/app/output \
  wahoo-cli:latest \
  --from 2026-03-01 --to 2026-04-01
```

### Command Line

```bash
# Local:
./wahoo-cli && xdg-open workouts_*.ics

# Docker:
docker run -it -e SYSTM_USER="..." -e SYSTM_PASSWORD="..." -v "$(pwd)":/app/output wahoo-cli:latest && xdg-open workouts_*.ics
```

## Development

### Run tests

```bash
./gradlew test
```

### Build and run locally

```bash
./gradlew run --args="--from 2026-03-01 --to 2026-03-31"
```
