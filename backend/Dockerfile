FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY gradle gradle
COPY gradlew .
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p uploads && \
    apt-get update && apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r spring && useradd -r -g spring spring && \
    chown -R spring:spring /app

USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]