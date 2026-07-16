# Spec kỹ thuật: Website bán khóa học tích hợp AI gợi ý

## 1. Tổng quan dự án

Chuyển đổi đồ án khai phá dữ liệu (hệ thống gợi ý khóa học bằng Apriori + KNN) thành 1
website bán khóa học đầy đủ chức năng, giữ nguyên AI gợi ý làm điểm khác biệt.

**Stack hiện có (không đổi):**
- Backend: Spring Boot (Java), MySQL, Thymeleaf
- AI service: Flask (Apriori + KNN), hiện chạy trên Colab + ngrok — cần deploy ổn định sau
- Code hiện có: `FlaskApiService.java` gọi Flask qua REST (`/recommend`, `/health`, `/skills`, `/eda`)
- Model classes có sẵn: `StudentRequest` (17 đặc trưng học tập), `CourseResult`, `RecommendResponse`, `StudentInfo`

## 2. Mô hình nghiệp vụ đã chốt

**Quyết định cốt lõi:** hệ thống có 2 loại "khóa học" tách biệt, không được gộp chung:

| Loại | Vai trò | Có bán không? |
|---|---|---|
| `EXTERNAL_COURSES` | Catalog Coursera (809 khóa, crawl sẵn) — chỉ dùng làm dữ liệu cho AI gợi ý kỹ năng | Không, chỉ đọc/tham khảo |
| `COURSES` | Khóa học tự tạo (nội dung riêng, video YouTube embed) | Có, đây là sản phẩm được bán qua Order/Payment |

Lý do: không có quyền bán lại khóa học Coursera. `EXTERNAL_COURSES` chỉ nuôi AI (dựa vào
811 khóa + 2237 kỹ năng để hiểu xu hướng thị trường), còn `COURSES` là nơi áp dụng toàn bộ
nghiệp vụ bán hàng thật (giỏ hàng, thanh toán, coupon, hoàn tiền, chứng chỉ).

**Quyết định phụ:**
- Không có vai trò Giảng viên (mô hình 1 chủ, Admin quản lý toàn bộ `COURSES`)
- Thanh toán: Payment Simulator (giả lập kết quả thành công/thất bại), không tích hợp cổng thật
- Video bài học: nhúng YouTube (unlisted), không tự lưu trữ/stream

## 3. Tác nhân & nhiệm vụ

**Khách (chưa đăng nhập)**
- Xem `COURSES`, `CATEGORIES`, `REVIEWS` (chỉ đọc)
- Đăng ký tài khoản → tạo dòng mới trong `USERS`

**Học viên (`USERS.role = student`)**
- Tạo `ORDERS` + `ORDER_ITEMS` (giỏ hàng)
- Áp `COUPONS` vào đơn hàng
- Kích hoạt `PAYMENTS` (qua Payment Simulator)
- Khi `PAYMENTS.trang_thai = success` → hệ thống tự tạo `ENROLLMENTS`
- Cập nhật tiến độ học trong `ENROLLMENTS` (đánh dấu hoàn thành bài học)
- Khi đủ điều kiện (xem mục 5) → hệ thống tự sinh `CERTIFICATES`
- Viết `REVIEWS` cho khóa đã có `ENROLLMENTS`
- Nhập `STUDENT_PROFILE` (form khảo sát 17 đặc trưng) để dùng tính năng gợi ý AI
- Có thể yêu cầu hoàn tiền (tạo yêu cầu refund gắn với `ORDERS`)

**Admin (`USERS.role = admin`)**
- CRUD `CATEGORIES`, `COURSES` (bao gồm `LESSONS` — bài học trong khóa)
- CRUD `COUPONS`, theo dõi `so_luong`/`da_dung` để không vượt quota
- Duyệt/từ chối yêu cầu hoàn tiền → cập nhật `ORDERS.trang_thai`
- Kiểm duyệt `REVIEWS` (ẩn/xóa spam)
- Xem dashboard: doanh thu, khóa bán chạy, tỷ lệ hoàn thành (tổng hợp từ `ORDERS`, `ENROLLMENTS`)
- Theo dõi trạng thái AI service (health check tới Flask)

**Hệ thống tự động (business logic, không phải actor người dùng)**
- Payment Simulator → ghi kết quả vào `PAYMENTS`
- Trigger tạo `ENROLLMENTS` khi `PAYMENTS` thành công
- Trigger tạo `CERTIFICATES` khi điều kiện hoàn thành đạt
- Trigger sinh `INVOICES` khi `PAYMENTS` thành công
- Recommendation Engine (Flask) → đọc `STUDENT_PROFILE`, đối chiếu `EXTERNAL_COURSES` + `SKILLS`, trả gợi ý

## 4. Database schema đầy đủ

### 4.1 Nhóm dữ liệu tham khảo AI (đã có sẵn file CSV, xem mục 6)

```sql
CREATE TABLE external_courses (
    external_course_id BIGINT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    organization VARCHAR(255),
    certificate_type VARCHAR(100),
    duration VARCHAR(50),
    rating DECIMAL(3,2),
    reviews_num INT,
    difficulty VARCHAR(50),
    external_url VARCHAR(500),
    students_enrolled INT
);

CREATE TABLE skills (
    skill_id INT PRIMARY KEY,
    ten_ky_nang VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE external_course_skills (
    external_course_id BIGINT,
    skill_id INT,
    PRIMARY KEY (external_course_id, skill_id),
    FOREIGN KEY (external_course_id) REFERENCES external_courses(external_course_id),
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id)
);

-- 17 đặc trưng học tập, khớp đúng StudentRequest.java hiện có
CREATE TABLE student_profile (
    user_id BIGINT PRIMARY KEY,
    hours_studied INT,
    attendance INT,
    previous_scores INT,
    sleep_hours INT,
    tutoring_sessions INT,
    physical_activity INT,
    parental_involvement INT,      -- 0=Low,1=Medium,2=High
    access_to_resources INT,       -- 0=Low,1=Medium,2=High
    extracurricular_activities INT,-- 0=No,1=Yes
    motivation_level INT,          -- 0=Low,1=Medium,2=High
    internet_access INT,           -- 0=No,1=Yes
    family_income INT,             -- 0=Low,1=Medium,2=High
    peer_influence INT,            -- 0=Negative,1=Neutral,2=Positive
    learning_disabilities INT,     -- 0=No,1=Yes
    parental_education_level INT,  -- 0=High School,1=College,2=Postgraduate
    distance_from_home INT,        -- 0=Far,1=Moderate,2=Near
    gender INT,                    -- 0=Female,1=Male
    group_label VARCHAR(50),       -- kết quả model: Yếu/Trung bình/Khá/Giỏi
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.2 Nhóm dữ liệu nghiệp vụ (khóa học tự bán)

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ho_ten VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'student',  -- student | admin
    so_dien_thoai VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ten_danh_muc VARCHAR(255) NOT NULL
);

CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT,
    ten_khoa_hoc VARCHAR(500) NOT NULL,
    mo_ta TEXT,
    gia DECIMAL(12,2) NOT NULL,
    cap_do VARCHAR(50),          -- Beginner/Intermediate/Advanced
    hinh_anh VARCHAR(500),
    trang_thai VARCHAR(20) DEFAULT 'active',  -- active | hidden
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE course_skills (
    course_id BIGINT,
    skill_id INT,
    PRIMARY KEY (course_id, skill_id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id)
);

CREATE TABLE lessons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    tieu_de VARCHAR(500) NOT NULL,
    video_url VARCHAR(500),       -- link YouTube
    thu_tu INT NOT NULL,
    thoi_luong_phut INT,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ma_code VARCHAR(50) NOT NULL UNIQUE,
    loai_giam VARCHAR(20) NOT NULL,   -- percent | fixed
    gia_tri DECIMAL(12,2) NOT NULL,
    so_luong INT NOT NULL,
    da_dung INT NOT NULL DEFAULT 0,
    ngay_het_han DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT,
    tong_tien DECIMAL(12,2) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'pending',
    -- pending -> paid -> (refund_requested -> refunded) | cancelled
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id)
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    gia DECIMAL(12,2) NOT NULL,   -- lưu giá tại thời điểm mua
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    phuong_thuc VARCHAR(50) DEFAULT 'simulator',
    trang_thai VARCHAR(20) NOT NULL,  -- success | failed
    ma_giao_dich VARCHAR(100),
    thoi_gian DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    so_hoa_don VARCHAR(50) NOT NULL UNIQUE,
    ngay_xuat DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    tien_do_percent INT DEFAULT 0,
    trang_thai VARCHAR(20) DEFAULT 'in_progress',  -- in_progress | completed
    ngay_dang_ky DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngay_hoan_thanh DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE lesson_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    hoan_thanh BOOLEAN DEFAULT FALSE,
    hoan_thanh_at DATETIME,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

CREATE TABLE certificates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id BIGINT NOT NULL UNIQUE,
    ma_xac_thuc VARCHAR(100) NOT NULL UNIQUE,
    ngay_cap DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id)
);

CREATE TABLE refund_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    ly_do TEXT,
    trang_thai VARCHAR(20) DEFAULT 'requested',  -- requested | approved | rejected
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    xu_ly_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    so_sao INT NOT NULL,   -- 1-5
    noi_dung TEXT,
    trang_thai VARCHAR(20) DEFAULT 'visible',  -- visible | hidden
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

## 5. Quy tắc nghiệp vụ quan trọng (business rules)

**Vòng đời đơn hàng:** `pending → paid → (refund_requested → refunded)` hoặc `pending → cancelled`.
Chỉ tạo `enrollments` khi đơn chuyển sang `paid`.

**Coupon:** kiểm tra `da_dung < so_luong` VÀ `ngay_het_han >= ngày hiện tại` trước khi áp dụng.
Khi áp dụng thành công, tăng `da_dung` lên 1 (cần transaction để tránh vượt quota khi nhiều
người dùng cùng lúc).

**Điều kiện hoàn tiền:** chỉ được yêu cầu nếu `enrollments.tien_do_percent < 20` VÀ đơn hàng
`paid` trong vòng 7 ngày trở lại.

**Điều kiện cấp chứng chỉ:** `enrollments.tien_do_percent = 100` (tất cả lesson trong khóa đã
`hoan_thanh = true`) → tự động tạo `certificates` với `ma_xac_thuc` là UUID hoặc mã ngẫu nhiên
để tra cứu công khai.

**Review:** chỉ cho phép nếu tồn tại `enrollments` của `user_id` + `course_id` đó.

## 6. Dữ liệu đã chuẩn bị sẵn (không cần làm lại)

- `external_courses.csv` — 809 dòng, đã tách khỏi cột skills gốc
- `skills.csv` — 2237 kỹ năng duy nhất, đã dọn lỗi ký tự
- `external_course_skills.csv` — 4482 dòng quan hệ N-N
- `students_cleaned_new.csv` — 6608 dòng, dùng để hiểu format `student_profile` (17 cột đúng thứ tự `StudentRequest.java`)

## 7. Lộ trình triển khai (thứ tự bắt buộc, không đảo được)

### GIAI ĐOẠN 1: Hoàn thiện Website cốt lõi (Mô hình "1 chủ" - Admin)
*Mô hình này giúp hoàn thiện nhanh hệ thống nghiệp vụ cơ bản, chạy thử an toàn và tránh rủi ro dở dang dự án.*

1. Tạo toàn bộ bảng ở mục 4 trong MySQL, import 3 file CSV vào `external_courses`/`skills`/`external_course_skills`
2. Auth: đăng ký/đăng nhập, phân quyền `student`/`admin` (Spring Security Session Form Login, KHÔNG dùng JWT, theo phong cách E_LEARNING-main)
3. CRUD `categories`, `courses`, `lessons` (trang Admin) + trang danh sách/chi tiết khóa học (public)
4. Giỏ hàng + `orders`/`order_items` + Payment Simulator + `payments`
5. `coupons` — áp dụng vào đơn hàng lúc thanh toán, kiểm tra quota (Trừ quota coupon chỉ khi thanh toán thành công)
6. `enrollments` + `lesson_progress` — trang học tập, đánh dấu hoàn thành bài
7. `certificates` — tự động cấp khi đủ điều kiện
8. `refund_requests` — học viên yêu cầu, Admin duyệt
9. `invoices` — tự sinh khi thanh toán thành công
10. `reviews` — ràng buộc phải có enrollment mới được review
11. `student_profile` — form khảo sát 17 đặc trưng + gọi lại Flask API gợi ý (đổi từ nhập tay mỗi lần sang lưu profile)
12. Deploy Flask API ổn định (bỏ Colab/ngrok), cập nhật `flask.api.base-url`
13. Admin Dashboard: thống kê doanh thu tổng, khóa bán chạy, tỷ lệ hoàn thành (query từ `orders`, `enrollments`)
14. Polish UI + Dockerize + deploy public (bước cuối cùng của Giai đoạn 1)

### GIAI ĐOẠN 2: Nâng cấp phân quyền Giảng viên (Teacher & Commission Upgrade)
*Nâng cấp sau khi Giai đoạn 1 đã chạy ổn định để đưa vào CV tăng tính chiều sâu.*

15. Mở rộng CSDL: Thêm cột hoa hồng (`commission_rate`), bảng doanh thu (`instructor_earnings`), trạng thái kiểm duyệt khóa học (`draft`/`pending_review`/`published`)
16. Phân quyền Security: Cấu hình phân vùng `/teacher/**` cho role `teacher`
17. Teacher Dashboard: CRUD khóa học/bài học của riêng giảng viên, kiểm soát IDOR tầng Service
18. Thống kê doanh thu giảng viên (Trừ hoa hồng hệ thống) và yêu cầu rút tiền giả lập.

## 8. Ghi chú cho AI code theo spec này

- Giữ nguyên các model class Java đã có (`StudentRequest`, `CourseResult`, `RecommendResponse`,
  `FlaskApiService`) — không sửa cấu trúc 17 đặc trưng, chỉ thêm entity/repository mới cho các
  bảng ở mục 4.
- Tuân thủ tuyệt đối phong cách viết code của dự án mẫu `E_LEARNING-main`:
  - **Tên các package phải viết hoa chữ cái đầu**: `Config`, `Controller`, `Model`, `Repository`, `Service`.
  - **Đặt tên Class & Method**: Sử dụng PascalCase hoặc Snake_Case tương ứng (ví dụ: `SinhVien_Service`, `SinhVien_Controller`, các hàm nghiệp vụ như `Get_SinhVien()`, `Create()`, `Get_Id()`, `Update_SinhVien()`, `TimKiem()`, `DangKy()`).
  - **Cơ chế Bảo mật**: Sử dụng Spring Security Form Login truyền thống (Session Cookie), không sử dụng JWT. Cấu hình `SecurityConfig` và `CustomUserDetailsService` tương thích để phân quyền người dùng.
  - **Database Entity**: Khai báo thủ công constructor mặc định và constructor đầy đủ tham số, viết đầy đủ Getters/Setters và `toString` (hạn chế dùng Lombok trừ khi bắt buộc để đồng bộ phong cách code của bạn).
- Không tạo entity `EXTERNAL_COURSES`/`SKILLS`/`EXTERNAL_COURSE_SKILLS` trùng lặp — dữ liệu
  này chỉ đọc (read-only), phục vụ AI, không có API CRUD.
- Toàn bộ số tiền dùng `DECIMAL`, không dùng `FLOAT`/`DOUBLE`, để tránh sai số khi tính toán tiền.
- Coupon và Order cần xử lý trong 1 transaction (`@Transactional`) để tránh race condition khi
  nhiều người dùng cùng áp 1 coupon giới hạn số lượng.
