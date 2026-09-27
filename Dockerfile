FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline
# Added --mount=type=cache: Maven dependencies are now cached by BuildKit
# instead of being written into a permanent image layer — this significantly
# reduces the amount of data written to layer blobs during the build

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests
# Same cache mount applied here for the package step

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]