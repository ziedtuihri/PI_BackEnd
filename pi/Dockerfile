# Stage 1: Build the Java application
FROM maven:3.9.1-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the application, skipping tests for faster build in Docker
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app

# Copy the generated .jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Add the wait-for-it script to the container
# Ensure wait-for-it.sh is in the same directory as your Dockerfile
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Expose the port that the Spring Boot application will run on
# This aligns with server.port=8081 in your application.properties
EXPOSE 8081

# Entrypoint for the container:
# It waits for the MySQL service (named 'mysql' in docker-compose) on port 3306.
# '-t 0' means it will wait indefinitely until the MySQL database is available.
# '--' separates wait-for-it.sh arguments from the command to execute (your Spring Boot app).
ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "-t", "0", "--", "java", "-jar", "app.jar"]