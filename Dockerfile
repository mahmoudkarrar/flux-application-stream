FROM openjdk:11-jre-slim
MAINTAINER "Mahmoud Karrar <mahmod.fathi@gmail.com>"
WORKDIR /app

COPY ./target/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 8080