# Dockerfile for ProductoService - Java 8 Compatible

# Etapa 1: build de la aplicación
FROM maven:3.6.3-openjdk-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: imagen liviana para ejecutar
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
