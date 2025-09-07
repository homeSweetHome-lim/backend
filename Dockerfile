# 멀티스테이지 빌드를 위한 베이스 이미지
FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /workspace

# Gradle Wrapper 및 빌드 스크립트 먼저 복사 (의존성 캐시 최적화)
COPY gradlew settings.gradle build.gradle /workspace/
COPY gradle /workspace/gradle
RUN chmod +x gradlew

# 소스 복사 후 빌드 (테스트 제외)
COPY src /workspace/src
RUN ./gradlew --no-daemon -x test clean bootJar


FROM openjdk:21-jdk-slim AS runtime

WORKDIR /app

# 빌드 산출물만 복사
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
