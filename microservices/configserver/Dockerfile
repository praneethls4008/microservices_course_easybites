# --- Stage 1: Build Stage ---
# Use Maven with JDK 21 for the compilation phase
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy pom.xml to cache dependencies (Efficiency Tip!)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime Stage ---
# Use the lightweight JRE 21 image for the final container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 3. Create a non-root user for security (Interview Gold Star!)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 4. Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# 5. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]