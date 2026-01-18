# [1단계: 빌드 단계] - 여기서 소스 코드를 빌드해서 jar를 만듭니다.
FROM amazoncorretto:17 AS builder
WORKDIR /app
COPY . .
# 윈도우에서 넘어온 파일일 경우 실행 권한 문제 방지
RUN chmod +x ./mvnw
# 테스트는 건너뛰고 빌드 실행
RUN ./mvnw clean package -DskipTests

# [2단계: 실행 단계] - 1단계에서 만든 jar 파일만 쏙 가져와서 실행합니다.
FROM amazoncorretto:17
WORKDIR /app
# 1단계(builder)의 target 폴더에서 jar 파일을 가져옴
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/app/app.jar"]