
FROM openjdk:17-jdk-alpine
ARG ./pi/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]