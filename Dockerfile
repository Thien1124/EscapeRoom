# ===== STAGE 1: Build =====
FROM maven:3.9.9-eclipse-temurin-22 AS builder
WORKDIR /app

# Copy pom.xml và download dependencies trước (tận dụng Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== STAGE 2: Runtime =====
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

# Tạo user không phải root để bảo mật
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR từ build stage
COPY --from=builder /app/target/*.jar app.jar

# Đổi ownership
RUN chown spring:spring app.jar
USER spring

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Chạy ứng dụng với profile MySQL
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=mysql", "-Dserver.port=80", "app.jar"]
