# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first (layer cache — only re-downloads deps if pom changes)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the final jar from build stage
COPY --from=build /app/target/finance-dashboard.jar app.jar

# Render sets PORT automatically; fallback to 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
