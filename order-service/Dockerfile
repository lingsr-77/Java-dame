# ============================================
# 多阶段构建 Dockerfile
# ============================================
# 阶段 1：构建（需要 JDK + Maven）
# 阶段 2：运行（只需要 JRE，镜像更小）
# ============================================

# ---- Stage 1: 用 Maven 编译打包 ----
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 先复制 POM 文件（利用 Docker 缓存层：不常变）
COPY pom.xml .
COPY settings-docker.xml /root/.m2/settings.xml
COPY common/pom.xml common/
COPY order-service/pom.xml order-service/
COPY user-service/pom.xml user-service/
COPY gateway/pom.xml gateway/

# 下载所有依赖（这个层缓存后，改代码不用重新下载依赖）
RUN mvn dependency:go-offline -B -q

# 再复制源码（经常变化的部分）
COPY common/src common/src/
COPY order-service/src order-service/src/

# -pl order-service：只编译这个模块
# -am（also-make）：自动先编译它的依赖（common）
# -DskipTests：跳过测试，加速构建
RUN mvn clean package -pl order-service -am -DskipTests -q

# ---- Stage 2: 运行时（JRE 镜像，更轻量）----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/order-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
