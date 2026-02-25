# ===== Build stage =====
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Télécharge les dépendances et build le JAR (sans tests pour le build Docker)
RUN apk add --no-cache maven && \
    mvn -B clean package -DskipTests

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copie le JAR depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Port exposé
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
