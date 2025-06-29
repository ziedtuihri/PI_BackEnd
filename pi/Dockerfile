# Étape 1 : build avec Maven
FROM maven:3.9.1-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : image finale JRE + app
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8081

ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "-t", "60", "--", "java", "-jar", "app.jar"]