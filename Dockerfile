# products-service/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY application/target/application-0.0.1-SNAPSHOT.jar products-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "products-service.jar"]