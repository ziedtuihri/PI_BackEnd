# Étape 1 : Build de l'application Java
FROM maven:3.9.1-eclipse-temurin-20 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build de l'application sans les tests pour accélérer la construction Docker
RUN mvn clean package -DskipTests

# Étape 2 : Image de runtime
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app

# Copier le .jar généré depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Ajouter le script wait-for-it (assurez-vous qu'il est bien présent à la racine du projet)
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Exposer le port sur lequel l'application Spring Boot tourne
EXPOSE 8081

# Point d'entrée de la conteneurisation, attend que MySQL soit prêt
ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "-t", "0", "--", "java", "-jar", "app.jar"]
