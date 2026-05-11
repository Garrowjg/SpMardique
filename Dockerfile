# Usar Maven con Java 21 para compilar
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Usar una imagen ligera de Java 21 para ejecutar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/static /app/static
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]]