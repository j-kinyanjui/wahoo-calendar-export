# Build stage:
FROM gradle:9.4.0-jdk21-alpine AS builder

WORKDIR /app
COPY build.gradle.kts gradle.properties settings.gradle.kts ./
#COPY gradle/ gradle/
COPY src/ src/

# Build and install distribution (excludes tests for faster build)
RUN gradle clean installDist -x test --no-daemon

# Runtime stage:
FROM gcr.io/distroless/java17-debian13

WORKDIR /app

COPY --from=builder /app/build/install/wahoo-calendar-export/ /app/wahoo-calendar-export
RUN mkdir -p /app/config /app/output
ENTRYPOINT ["/app/wahoo-cli/bin/wahoo-calendar-export"]

# Default command: fetch and export workouts with 2-week range
CMD ["--range", "2w"]
