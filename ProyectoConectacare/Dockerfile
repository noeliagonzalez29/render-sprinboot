# Etapa 1: Construcción del .jar
FROM maven:3.9.5-amazoncorretto-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final ligera
FROM amazoncorretto:21
COPY --from=build /app/target/ProyectoConectacare-0.0.1-SNAPSHOT.jar /api-v1.jar
ENTRYPOINT ["java", "-jar", "/api-v1.jar"]
