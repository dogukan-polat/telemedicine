FROM maven:4.0.0-rc-4-eclipse-temurin-25-alpine

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]