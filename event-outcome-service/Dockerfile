FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY event-outcome-service/build.gradle event-outcome-service/
COPY event-outcome-service/src/ event-outcome-service/src/
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew :event-outcome-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/event-outcome-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
