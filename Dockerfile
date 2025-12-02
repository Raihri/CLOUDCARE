# Use Java 17 image with Maven
FROM maven:3.9.3-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper, pom.xml, and source code
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn
COPY src src

# Make mvnw executable
RUN chmod +x mvnw

# Build the JAR (skip tests to speed up)
RUN ./mvnw clean package -DskipTests

# Use a lightweight Java runtime for running the app
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/cloud-care-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]