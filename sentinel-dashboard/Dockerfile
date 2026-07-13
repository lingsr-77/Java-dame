FROM openjdk:21-jre-slim

WORKDIR /app

# 下载 Sentinel Dashboard 1.8.6（与 spring-cloud-alibaba 2023.0.1.0 兼容）
ADD https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar app.jar

EXPOSE 8858

# JAVA_OPTS 由 docker-compose 环境变量传入
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} \
  -Dserver.port=8858 \
  -Dcsp.sentinel.dashboard.server=localhost:8858 \
  -Dproject.name=sentinel-dashboard \
  -jar app.jar"]
