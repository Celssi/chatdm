# Multi-stage build for ChatDM on Cloud Run
# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (no build)
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build (index built from local PDFs if present, else skip)
COPY src src
RUN ./mvnw package -DskipTests -B -q

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
USER appuser

# Copy built JAR
COPY --from=builder /app/target/*.jar app.jar

# Cloud Run expects port 8080
ENV PORT=8080
EXPOSE 8080

# Use cloud profile for GCS
ENV SPRING_PROFILES_ACTIVE=cloud

ENTRYPOINT ["java", "-jar", "app.jar"]
