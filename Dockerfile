#build the app
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY pom.xml .
COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests


#rur the app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app


RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

