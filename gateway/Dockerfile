# Gateway Dockerfile（多阶段构建）
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY settings-docker.xml /root/.m2/settings.xml
COPY common/pom.xml common/
COPY order-service/pom.xml order-service/
COPY user-service/pom.xml user-service/
COPY gateway/pom.xml gateway/

RUN mvn dependency:go-offline -B -q

COPY common/src common/src/
COPY gateway/src gateway/src/

RUN mvn clean package -pl gateway -am -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
