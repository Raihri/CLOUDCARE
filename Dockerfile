# Use Java 17 with Maven
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src src

# Build the JAR
RUN mvn clean package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/cloud-care-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]