FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
# Copy files to container
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
FROM openjdk:17-oracle
WORKDIR /app
# Copy jar file
COPY --from=build /app/target/CricketCare-0.0.1-SNAPSHOT.jar .
# Run application
CMD ["java", "-jar", "CricketCare-0.0.1-SNAPSHOT.jar"]