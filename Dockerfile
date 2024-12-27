FROM openjdk:17-jdk-alpine

RUN apk add --no-cache maven

COPY pom.xml .
COPY src ./src



EXPOSE 8087
CMD ["java", "-jar", "/app/mail-0.0.1-SNAPSHOT.jar"]