# Build stage
FROM ghcr.io/graalvm/graalvm-community:25 AS builder

WORKDIR /app

# Install Maven
RUN microdnf install -y maven && microdnf clean all

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build native image with production profile
RUN mvn -Pnative,production native:compile -DskipTests

# Runtime stage
FROM debian:bookworm-slim

WORKDIR /app

# Install required libraries for native binary
RUN apt-get update && apt-get install -y --no-install-recommends \
    libfreetype6 \
    libfontconfig1 \
    && rm -rf /var/lib/apt/lists/*

# Copy native binary from builder
COPY --from=builder /app/target/livechat /app/livechat

# Create non-root user
RUN useradd -r -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["/app/livechat"]
