# syntax=docker/dockerfile:1

# ===== Build Stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache dependencies first (faster rebuilds)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ===== Runtime Stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN groupadd --system lottery && useradd --system --gid lottery lottery

# Copy the built jar
COPY --from=build /build/target/*.jar app.jar

RUN chown -R lottery:lottery /app
USER lottery

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]