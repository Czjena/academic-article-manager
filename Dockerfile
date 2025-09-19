# Etap 1: Budowanie aplikacji
FROM maven:3-openjdk-17-slim AS build

WORKDIR /app

# Kopiowanie plików projektu
COPY pom.xml .
COPY src ./src

# Budowanie aplikacji
RUN mvn clean package -DskipTests

# Etap 2: Tworzenie obrazu produkcyjnego
FROM openjdk:17-jdk-slim

WORKDIR /app

# Kopiowanie zbudowanego JARa
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
