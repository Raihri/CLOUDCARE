# Use a Java image that supports ARM (Apple Silicon)
FROM eclipse-temurin:17-jdk

# Set working directory inside container
WORKDIR /app

# Copy your jar
COPY target/cloud-care-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]