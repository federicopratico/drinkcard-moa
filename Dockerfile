# syntax=docker/dockerfile:1.7
# Enables BuildKit features (e.g. --mount=type=cache) used below.

# ---- Build stage ----
# Use a full Maven + JDK 21 image to compile and package the app.
# This stage is discarded after the build; only the resulting jar is kept.
FROM maven:3.9-eclipse-temurin-21 AS build

# All subsequent commands run inside /app in the image.
WORKDIR /app

# Copy only pom.xml first so Docker can cache the dependency layer.
# If pom.xml doesn't change, the expensive `go-offline` step is reused.
COPY pom.xml .

# Pre-download all Maven dependencies into the local repo.
# --mount=type=cache keeps ~/.m2 across builds so deps aren't re-downloaded every time.
# -B = batch (non-interactive), -q = quiet output.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# Now copy the actual source code. Done after deps so source changes
# don't invalidate the dependency cache layer above.
COPY src ./src

# Compile and package the application into a jar under target/.
# -DskipTests skips tests to keep image builds fast (tests run in CI separately).
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
# Use a minimal JRE-only Alpine image for the final runtime — smaller and
# more secure than shipping the full JDK + Maven from the build stage.
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user/group so the app doesn't run as root inside the container.
# -S creates a system user/group (no password, no home setup overhead).
RUN addgroup -S app && adduser -S app -G app

# Copy the built jar from the build stage and rename it to a stable name.
# The wildcard handles version suffixes (e.g. drinkcard-moa-0.0.1-SNAPSHOT.jar).
COPY --from=build /app/target/*.jar app.jar

# Ensure the non-root user owns the jar so it can read/execute it.
RUN chown app:app app.jar

# Drop privileges: everything from here on runs as the unprivileged `app` user.
USER app

# Document that the container listens on 8080 (Spring Boot's default).
# This is metadata only — actual port publishing happens with `docker run -p`.
EXPOSE 8080

# Allow callers to pass extra JVM flags (heap size, GC tuning, etc.) without
# rebuilding the image, e.g. `docker run -e JAVA_OPTS="-Xmx512m" ...`.
ENV JAVA_OPTS=""

# Run the jar via `sh -c` so $JAVA_OPTS gets expanded by the shell.
# `exec` replaces the shell process with the Java process, so the JVM
# becomes PID 1 and receives signals (SIGTERM) directly for graceful shutdown.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
