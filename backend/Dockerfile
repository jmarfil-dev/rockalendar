# ---- Build ----
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
# Descargar dependencias primero para aprovechar la caché de capas
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package spring-boot:repackage -DskipTests -q && \
    cp $(find target -name "*.jar" -not -name "*.jar.original") target/app.jar

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
