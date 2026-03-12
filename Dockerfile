# Multi-stage build: compile in Gradle container, then run in minimal JRE
FROM gradle:8.4-jdk17 AS builder

WORKDIR /app
COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
COPY src/ src/

RUN gradle build -x test --no-daemon

# Runtime stage: use lightweight JRE image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built application from builder stage
COPY --from=builder /app/build/install/wahoo-cli /app/wahoo-cli

# Create directory for config and output files
RUN mkdir -p /app/config /app/output

# Set the entrypoint to the CLI
ENTRYPOINT ["/app/wahoo-cli/bin/wahoo-cli"]

# Default command: fetch and export workouts with 2-week range
CMD ["--range", "2w"]
