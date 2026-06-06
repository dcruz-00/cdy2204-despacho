# Etapa 1: compilar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
COPY wallet ./wallet
RUN mvn clean package -DskipTests

# Etapa 2: ejecutar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/wallet ./wallet
ENTRYPOINT ["java", "-jar", "app.jar"]