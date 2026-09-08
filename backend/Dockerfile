# Stage 8 — Infrastructure: backend container image
#
# Multi-stage build: compile with Maven under JDK 25 in the builder stage, then run the
# packaged jar on a minimal JRE 25 Alpine image. Nothing from the builder (Maven, sources,
# the local ~/.m2 cache) ships in the runtime image.

# ---- Builder -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# ---- Runtime -------------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Run as a non-root, unprivileged user.
RUN addgroup -S syncboard && adduser -S syncboard -G syncboard

COPY --from=builder /build/target/*.jar app.jar
RUN chown syncboard:syncboard app.jar

USER syncboard

EXPOSE 8080

# Container-aware JVM defaults. SPRING_PROFILES_ACTIVE and the SYNCBOARD_*/GOOGLE_*
# variables are supplied at run time (see .env.example); nothing is baked into the image.
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
