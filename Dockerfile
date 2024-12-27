FROM openjdk:17-jdk-alpine

RUN apk add --no-cache maven

COPY pom.xml .
COPY src ./src



EXPOSE 8087
CMD ["java", "-jar", "/app/presence-0.0.1-SNAPSHOT.jar"]