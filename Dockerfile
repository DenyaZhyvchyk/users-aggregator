FROM maven:latest AS builder

COPY src /usr/local/app/src
COPY pom.xml /usr/local/app/pom.xml

WORKDIR /usr/local/app

RUN mvn clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /usr/local/app/target/*.jar app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]