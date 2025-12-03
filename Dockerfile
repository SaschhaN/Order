FROM eclipse-temurin:21
LABEL authors="Sascha Niederhauser, Alex Burri"

WORKDIR /app
COPY target/order-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]