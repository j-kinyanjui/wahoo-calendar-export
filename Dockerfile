# Build stage:
FROM gradle:9.4.0-jdk21-alpine AS builder

WORKDIR /app
COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY src/ src/

RUN gradle clean installDist -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-ubi9-minimal

WORKDIR /app

COPY --from=builder /app/build/install/wahoo-cal/ /app/wahoo-cal/
ENTRYPOINT ["/app/wahoo-cal/bin/wahoo-cal"]
CMD ["--range", "2w"]
