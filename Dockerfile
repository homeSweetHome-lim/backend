# 멀티스테이지 빌드를 위한 베이스 이미지
FROM openjdk:24-jdk AS builder

WORKDIR /workspace

# Gradle Wrapper 및 빌드 스크립트 먼저 복사 (의존성 캐시 최적화)
COPY gradlew settings.gradle build.gradle /workspace/
COPY gradle /workspace/gradle
RUN chmod +x gradlew

# Gradle Wrapper 실행에 필요한 findutils(xargs) 설치 (Oracle Linux 기반 이미지)
RUN microdnf -y install findutils && microdnf clean all

# 소스 복사 후 빌드 (테스트 제외)
COPY src /workspace/src
RUN ./gradlew --no-daemon -x test clean bootJar


FROM openjdk:24-jdk AS runtime

WORKDIR /app

# 빌드 산출물만 복사
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]