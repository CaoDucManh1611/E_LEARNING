-- =====================================================================
-- KỊCH BẢN KHỞI TẠO CƠ SỞ DỮ LIỆU MYSQL (DDL SCHEMA)
-- Đồ án: Website bán khóa học tích hợp AI gợi ý (EduRecommend)
-- Phong cách code: Tương thích cơ chế Spring Security & JPA của E_LEARNING-main
-- File: src/main/resources/init_schema.sql
-- =====================================================================

-- Tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS doan_khai_pha CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE doan_khai_pha;

-- 1. BẢNG NGƯỜI DÙNG (USERS) - Tương thích Spring Security
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ho_ten VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'student',  -- student | teacher | admin
    so_dien_thoai VARCHAR(20),
    is_locked BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. BẢNG DANH MỤC KHÓA HỌC (CATEGORIES)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ten_danh_muc VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- 3. BẢNG KHÓA HỌC BÁN (COURSES) - Khóa học nội bộ tự quản lý
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT,
    teacher_id BIGINT,                           -- Giảng viên sở hữu khóa học
    ten_khoa_hoc VARCHAR(500) NOT NULL,
    mo_ta TEXT,
    gia DECIMAL(12,2) NOT NULL,
    cap_do VARCHAR(50),          -- Beginner/Intermediate/Advanced
    hinh_anh VARCHAR(500),
    trang_thai VARCHAR(20) DEFAULT 'active',  -- active | hidden | pending_review
    commission_rate INT NOT NULL DEFAULT 70,  -- Tỷ lệ hoa hồng (VD: 70%)
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 3.5 BẢNG HOA HỒNG GIẢNG VIÊN (INSTRUCTOR_EARNINGS)
CREATE TABLE IF NOT EXISTS instructor_earnings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    tong_tien DECIMAL(12,2) NOT NULL,
    tien_nhan DECIMAL(12,2) NOT NULL,
    thoi_gian DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. BẢNG DANH SÁCH KỸ NĂNG (SKILLS) - Dùng chung cho cả AI và khóa học bán
CREATE TABLE IF NOT EXISTS skills (
    skill_id INT PRIMARY KEY,
    ten_ky_nang VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- 5. BẢNG QUAN HỆ KHÓA HỌC TỰ BÁN - KỸ NĂNG (COURSE_SKILLS)
CREATE TABLE IF NOT EXISTS course_skills (
    course_id BIGINT,
    skill_id INT,
    PRIMARY KEY (course_id, skill_id),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. BẢNG BÀI HỌC TRONG KHÓA HỌC (LESSONS)
CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    tieu_de VARCHAR(500) NOT NULL,
    video_url VARCHAR(500),       -- Link YouTube embed
    thu_tu INT NOT NULL,
    thoi_luong_phut INT,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. BẢNG MÃ GIẢM GIÁ (COUPONS)
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ma_code VARCHAR(50) NOT NULL UNIQUE,
    loai_giam VARCHAR(20) NOT NULL,   -- percent | fixed
    gia_tri DECIMAL(12,2) NOT NULL,
    so_luong INT NOT NULL,
    da_dung INT NOT NULL DEFAULT 0,
    ngay_het_han DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 8. BẢNG ĐƠN HÀNG (ORDERS)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT,
    tong_tien DECIMAL(12,2) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'pending', 
    -- pending -> paid -> (refund_requested -> refunded) | cancelled
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 9. BẢNG CHI TIẾT ĐƠN HÀNG (ORDER_ITEMS)
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    gia DECIMAL(12,2) NOT NULL,   -- Lưu giá tại thời điểm mua
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 10. BẢNG GIAO DỊCH THANH TOÁN (PAYMENTS) - Giả lập
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    phuong_thuc VARCHAR(50) DEFAULT 'bank_transfer',
    trang_thai VARCHAR(20) NOT NULL,  -- success | failed
    ma_giao_dich VARCHAR(100),
    so_tien DECIMAL(12,2) NOT NULL,   -- Số tiền giao dịch (khớp với Payment.java)
    pay_date DATETIME DEFAULT CURRENT_TIMESTAMP,  -- Thời gian giao dịch (khớp với Payment.java)
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. BẢNG HÓA ĐƠN XUẤT (INVOICES)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    so_hoa_don VARCHAR(50) NOT NULL UNIQUE,
    ngay_xuat DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. BẢNG ĐĂNG KÝ HỌC (ENROLLMENTS)
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    tien_do_percent INT DEFAULT 0,
    trang_thai VARCHAR(20) DEFAULT 'in_progress',  -- in_progress | completed
    ngay_dang_ky DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngay_hoan_thanh DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 13. BẢNG TIẾN ĐỘ BÀI HỌC (LESSON_PROGRESS)
CREATE TABLE IF NOT EXISTS lesson_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    hoan_thanh BOOLEAN DEFAULT FALSE,
    hoan_thanh_at DATETIME,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. BẢNG CHỨNG CHỈ HOÀN THÀNH (CERTIFICATES)
CREATE TABLE IF NOT EXISTS certificates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id BIGINT NOT NULL UNIQUE,
    ma_xac_thuc VARCHAR(100) NOT NULL UNIQUE,
    ngay_cap DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 15. BẢNG YÊU CẦU HOÀN TIỀN (REFUND_REQUESTS)
CREATE TABLE IF NOT EXISTS refund_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    ly_do TEXT,
    trang_thai VARCHAR(20) DEFAULT 'requested',  -- requested | approved | rejected
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    xu_ly_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 16. BẢNG ĐÁNH GIÁ KHÓA HỌC (REVIEWS)
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    so_sao INT NOT NULL,   -- 1-5
    noi_dung TEXT,
    trang_thai VARCHAR(20) DEFAULT 'visible',  -- visible | hidden
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- CÁC BẢNG DÀNH RIÊNG CHO MÔ HÌNH HỌC MÁY / AI (READ-ONLY TRÊN WEB)
-- =====================================================================

-- 17. BẢNG KHÓA HỌC NGOÀI THAM KHẢO (EXTERNAL_COURSES) - Dữ liệu AI từ Coursera
CREATE TABLE IF NOT EXISTS external_courses (
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
) ENGINE=InnoDB;

-- 18. BẢNG QUAN HỆ KHÓA HỌC NGOÀI - KỸ NĂNG (EXTERNAL_COURSE_SKILLS)
CREATE TABLE IF NOT EXISTS external_course_skills (
    external_course_id BIGINT,
    skill_id INT,
    PRIMARY KEY (external_course_id, skill_id),
    FOREIGN KEY (external_course_id) REFERENCES external_courses(external_course_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 19. BẢNG HỒ SƠ HỌC TẬP SINH VIÊN (STUDENT_PROFILE) - Lưu 17 đặc trưng học tập
CREATE TABLE IF NOT EXISTS student_profile (
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
    social_media_usage VARCHAR(20), -- Low/Medium/High
    group_label VARCHAR(50),       -- Kết quả dự đoán từ KNN: Yếu/Trung bình/Khá/Giỏi
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 20. BẢNG THÔNG BÁO HỎI ĐÁP (NOTIFICATIONS)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sender_id BIGINT,
    tieu_de VARCHAR(255) NOT NULL,
    noi_dung TEXT NOT NULL,
    url VARCHAR(500) NOT NULL,
    da_doc BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 21. BẢNG CHI TIẾT GIỎ HÀNG (CART_ITEMS)
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_course (user_id, course_id)
) ENGINE=InnoDB;
