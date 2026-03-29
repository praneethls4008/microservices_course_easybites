# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Install curl
RUN apt-get update && apt-get install -y curl

# Download OpenTelemetry agent
RUN curl -L -o opentelemetry-javaagent.jar \
https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.22.0/opentelemetry-javaagent.jar

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


# --- Stage 2: Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/libs \
 && addgroup -S spring \
 && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/opentelemetry-javaagent.jar /app/libs/opentelemetry-javaagent.jar

RUN chown -R spring:spring /app

USER spring:spring
ENTRYPOINT ["java","-jar","app.jar"]