# GIAI ĐOẠN 1: Build file JAR tự động bên trong Docker sử dụng JDK 26
FROM amazoncorretto:26-alpine AS builder
WORKDIR /app

# Sao chép các file cấu hình Maven Wrapper của dự án
COPY Edu_Recommend/doan/.mvn/ .mvn/
COPY Edu_Recommend/doan/mvnw mvnw
COPY Edu_Recommend/doan/mvnw.cmd mvnw.cmd
COPY Edu_Recommend/doan/pom.xml pom.xml

# Cấp quyền thực thi cho file chạy mvnw
RUN chmod +x mvnw

# Sao chép mã nguồn chính
COPY Edu_Recommend/doan/src/ src/

# Tiến hành biên dịch và đóng gói thành file jar
RUN ./mvnw package -DskipTests

# GIAI ĐOẠN 2: Runtime image chạy ứng dụng siêu nhẹ
FROM amazoncorretto:26-alpine
WORKDIR /app

# Sao chép file jar đã build xong từ Giai đoạn 1 sang
COPY --from=builder /app/target/doan-0.0.1-SNAPSHOT.jar app.jar

# Tạo thư mục uploads lưu trữ file tải lên
RUN mkdir -p uploads

# Cấu hình các biến môi trường kết nối database mặc định đến máy host
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/doan_khai_pha?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=123456

# Cấu hình cổng 8080
EXPOSE 8080
ENV APP_UPLOAD_DIR=/app/uploads/

ENTRYPOINT ["java", "-jar", "app.jar"]
