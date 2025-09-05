# High-Throughput Task Scheduler Dockerfile
# Multi-stage build for optimized production image

# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (layer caching optimization)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

# Install system dependencies and performance tools
RUN apk add --no-cache \
    curl \
    dumb-init \
    && rm -rf /var/cache/apk/*

# Create application user for security
RUN addgroup -g 1001 -S taskscheduler && \
    adduser -u 1001 -S taskscheduler -G taskscheduler

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/high-throughput-task-scheduler-*.jar app.jar

# Create directories for logs and configuration
RUN mkdir -p /app/logs /app/config && \
    chown -R taskscheduler:taskscheduler /app

# Switch to non-root user
USER taskscheduler

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables for JVM optimization
ENV JAVA_OPTS="-server \
    -Xms2g \
    -Xmx4g \
    -XX:+UseG1GC \
    -XX:G1HeapRegionSize=16m \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseCGroupMemoryLimitForHeap \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

# Application configuration
ENV SERVER_PORT=8080
ENV LOGGING_LEVEL_ROOT=INFO
ENV LOGGING_LEVEL_COM_TASKSCHEDULER=INFO

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Start the application
CMD ["sh", "-c", "exec java ${JAVA_OPTS} -jar app.jar"]

# Labels for metadata
LABEL \
    org.opencontainers.image.title="High-Throughput Task Scheduler" \
    org.opencontainers.image.description="Distributed high-throughput task scheduling system" \
    org.opencontainers.image.version="1.0.0" \
    org.opencontainers.image.vendor="Task Scheduler Team" \
    org.opencontainers.image.source="https://github.com/your-org/high-throughput-task-scheduler" \
    maintainer="taskscheduler-team@example.com"