    # Dockerfile (giữ nguyên như phiên bản đã tối ưu)

# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-slim
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/be.jar app.jar

# The port should be configurable through environment variable
EXPOSE ${SERVER_PORT:-8080}

# Add entrypoint to properly pass Spring profile
ENTRYPOINT ["java", "-jar", "app.jar"]

