# Usamos la distribución oficial de Eclipse Temurin para Java 25
FROM eclipse-temurin:25-jdk-alpine

WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]