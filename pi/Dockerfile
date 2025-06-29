# Étape 1 : builder l'application
FROM maven:3.9.1-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : exécuter l'application avec JRE
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app

# Copier le jar buildé
COPY --from=build /app/target/*.jar app.jar

# Copier wait-for-it.sh dans l'image
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8084

# Lancer wait-for-it avant le jar (ici on attend mysql:3306)
ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]
