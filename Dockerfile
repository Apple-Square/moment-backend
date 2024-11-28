# 1. OpenJDK 런타임 이미지를 기반으로 컨테이너 생성
FROM openjdk:17-jdk-slim

# 2. 컨테이너 내부에서 작업 디렉토리를 설정
WORKDIR /app

# 3. Gradle 또는 Maven으로 빌드된 JAR 파일을 컨테이너로 복사
COPY *SNAPSHOT.jar app.jar

# 4. Spring Boot 애플리케이션에서 사용하는 포트 개방
EXPOSE 8080

# 5. JAR 파일 실행 명령어 설정
ENTRYPOINT ["java", "-jar", "app.jar"]
