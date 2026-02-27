# Hướng Dẫn Chạy Dự Án

Dự án này là một ứng dụng Spring Boot backend, sử dụng PostgreSQL và Redis.

## Yêu Cầu

- **Java Development Kit (JDK) 21**
- **Docker** & **Docker Compose** (để chạy Database và Redis)

## Thiết Lập Môi Trường

Để đơn giản hóa việc cài đặt database, dự án hỗ trợ chạy PostgreSQL và Redis thông qua Docker.

### 1. Khởi động Database và Redis

Mở terminal tại thư mục gốc của dự án và chạy lệnh sau:

```bash
docker-compose up -d
```

Lệnh này sẽ tải và khởi chạy 2 container:
- **PostgreSQL**: Chạy trên port `5432`
  - Database: `security`
- **Redis**: Chạy trên port `6379`

### 2. Chạy Ứng Dụng

Sau khi các container đã khởi động xong, bạn có thể chạy ứng dụng Spring Boot bằng lệnh:

**Đối với macOS / Linux:**

```bash
./gradlew bootRun
```

**Đối với Windows:**

```cmd
gradlew.bat bootRun
```

Nếu gặp lỗi "Permission denied" trên macOS/Linux, hãy cấp quyền thực thi cho file `gradlew`:

```bash
chmod +x gradlew
```

## Kiểm Tra Hoạt Động

Khi ứng dụng đã chạy thành công (thông báo `Started DemoApplication...`), bạn có thể truy cập:

- **Swagger UI (Tài liệu API)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **API Endpoint ví dụ**: [http://localhost:8080/api/v1/...](http://localhost:8080/api/v1/...)

## Xử Lý Sự Cố

- **Lỗi cổng (Port conflict)**: Đảm bảo không có dịch vụ nào khác đang chạy trên port `5432` (Postgres) hoặc `6379` (Redis).
- **Lỗi kết nối Db**: Đảm bảo container Docker đang chạy (`docker ps`).

## Xem Database

Bạn có thể xem dữ liệu trong database bằng một trong các các cách sau:

### Cách 1: Sử dụng Công cụ quản lý có sẵn (Adminer)

Mình đã tích hợp sẵn Adminer vào Docker Compose. Bạn chỉ cần truy cập:

- **URL**: [http://localhost:8081](http://localhost:8081)
- **Hệ quản trị**: PostgreSQL
- **Máy chủ**: `postgres` (hoặc `security_postgres`)
- **Tên đăng nhập**: `postgres`
- **Mật khẩu**: `0407`
- **Database**: `security`

### Cách 2: Sử dụng phần mềm bên ngoài (DBeaver, PGAdmin...)

Nếu bạn muốn dùng phần mềm cài trên máy, hãy kết nối với thông tin:

- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `security`
- **User**: `postgres`
- **Password**: `0407`
