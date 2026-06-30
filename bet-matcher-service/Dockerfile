FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY bet-matcher-service/build.gradle bet-matcher-service/
COPY bet-matcher-service/src/ bet-matcher-service/src/
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew :bet-matcher-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/bet-matcher-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
