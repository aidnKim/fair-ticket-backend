# 1. Base Image (자바 17 환경)
FROM amazoncorretto:17

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너 내부로 복사
# (주의: 미리 로컬에서 빌드했다고 가정합니다)
COPY target/*.jar app.jar

# 4. 실행 명령어 (지갑 경로는 docker-compose에서 주입할 예정)
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/app/app.jar"]