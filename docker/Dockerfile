# Build and run in a single, small JRE image (multi-stage not needed since Maven build is outside Docker here)
FROM eclipse-temurin:24-jre-alpine

# Create non-root user
RUN addgroup -S app && adduser -S app -G app

# Set workdir and copy artifact
WORKDIR /app
COPY target/*.jar app.jar

# JVM settings optimized for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Spring profile for containers
ENV SPRING_PROFILES_ACTIVE=docker

# Expose HTTP port
EXPOSE 8080

# Run as non-root and use read-only FS with tmpfs mounted at runtime if desired
USER app

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]