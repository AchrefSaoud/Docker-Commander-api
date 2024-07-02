FROM openjdk:17-jdk-alpine

VOLUME /tmp

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

EXPOSE 8080

#run this commands to build the image and run a docker container
#./mvnw package -DskipTests
#docker build -t spring-boot-app .
#docker run -d -p 8080:8080 spring-boot-app
