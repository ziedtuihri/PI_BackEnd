# Étape 1 : Build de l'application Java
FROM maven:3.9.1-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image de runtime
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app

# Copier le .jar généré
COPY --from=build /app/target/*.jar app.jar

# Ajouter le script wait-for-it (assurez-vous qu'il est bien présent à la racine du projet)
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Exposer le port 8081 (comme défini dans application.properties)
EXPOSE 8082

# Point d'entrée (sans timeout)
ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "-t", "0", "--", "java", "-jar", "/app/app.jar"]