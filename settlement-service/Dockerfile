FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY settlement-service/build.gradle settlement-service/
COPY settlement-service/src/ settlement-service/src/
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew :settlement-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/settlement-service/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
