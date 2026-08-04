# 🎓 EduRecommend - Hệ thống Bán khóa học & Gợi ý Lộ trình Học tập Tích hợp AI

![Build Status](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-brightgreen?logo=github-actions)
![Java Version](https://img.shields.io/badge/Java-26-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?logo=springboot)
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D?logo=vuedotjs)
![Python Flask](https://img.shields.io/badge/Flask-AI%20Engine-blue?logo=flask)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker)

---

## 📌 1. Giới thiệu tổng quan

**EduRecommend** là một nền tảng thương mại điện tử chuyên cung cấp các khóa học trực tuyến (E-Learning) kết hợp với **Hệ thống gợi ý AI thông minh (Apriori + KNN)** và **Trợ lý tư vấn AI Chatbot (Google Gemini API)**.

Dự án giúp tối ưu hóa lộ trình học tập dựa trên 17 đặc trưng học vấn của học viên (thời gian học, điểm số, điểm chuyên cần, mục tiêu kỹ năng...) kết hợp khai phá dữ liệu từ xu hướng thị trường (từ catalog 800+ khóa học Coursera).

---

## 🏗️ 2. Kiến trúc hệ thống

Hệ thống được thiết kế theo kiến trúc Microservices & RESTful API chuẩn hóa:

```
                  ┌─────────────────────────────────────┐
                  │          Frontend (Vue 3)           │
                  │   Vite + SPA + Responsive Layout    │
                  └──────────────────┬──────────────────┘
                                     │ REST API
                                     ▼
                  ┌─────────────────────────────────────┐
                  │       Backend (Spring Boot 4)       │
                  │   Java 26 + Spring Security + Data  │
                  └─────────┬─────────────────┬─────────┘
                            │                 │
             ┌──────────────┴──────┐   ┌──────┴───────────────┐
             ▼                     ▼   ▼                      ▼
  ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
  │   Database       │   │  AI Service      │   │  Google Gemini   │
  │   MySQL / H2     │   │  Flask (Python)  │   │  API Chatbot     │
  │   JPA / Hibernate│   │  Apriori + KNN   │   │  (Generative AI) │
  └──────────────────┘   └──────────────────┘   └──────────────────┘
```

---

## 📸 3. Giao diện ứng dụng (Screenshots)

| Trang chủ & Danh sách khóa học | Hệ thống Gợi ý Lộ trình AI |
|:---:|:---:|
| *(Thêm ảnh docs/screenshots/home.png)* | *(Thêm ảnh docs/screenshots/recommend.png)* |

| Trợ lý AI Chatbot (Gemini API) | Quản trị Dashboard (Admin) |
|:---:|:---:|
| *(Thêm ảnh docs/screenshots/chat.png)* | *(Thêm ảnh docs/screenshots/admin.png)* |

---

## 🔥 4. Chức năng chính

### 👨‍🎓 Học viên (Student)
- **Khám phá khóa học**: Tìm kiếm, lọc khóa học theo danh mục, giá, đánh giá.
- **Tư vấn AI Recommendation**: Điền form 17 thông số học tập để AI phân tích và đề xuất lộ trình kỹ năng phù hợp.
- **Giỏ hàng & Thanh toán**: Áp mã giảm giá (Coupon), thanh toán đơn hàng (Payment Simulator).
- **Học tập trực tuyến**: Xem video bài học nhúng (YouTube Unlisted), theo dõi tiến độ hoàn thành.
- **Chứng chỉ & Đánh giá**: Tự động nhận chứng chỉ hoàn thành khóa học và viết đánh giá sao (Review).
- **AI Chatbot trợ lý**: Hỏi đáp trực tiếp về chính sách hoàn tiền, khóa học phù hợp, tư vấn lộ trình.

### 🛡️ Quản trị viên (Admin)
- **Quản lý khóa học (CRUD)**: Tạo, sửa, xóa khóa học và các bài học (Lessons).
- **Quản lý danh mục & Coupon**: Tạo mã giảm giá, giới hạn lượt dùng.
- **Duyệt hoàn tiền (Refund)**: Xử lý yêu cầu hoàn tiền của học viên theo quy định.
- **Thống kê doanh thu**: Dashboard trực quan về doanh thu, số học viên, khóa học bán chạy.
- **Giám sát AI Engine**: Kiểm tra trạng thái kết nối tới Flask AI Service.

---

## 🛠️ 5. Công nghệ sử dụng

| Phân loại | Công nghệ / Thư viện |
|---|---|
| **Backend Core** | Java 26, Spring Boot 4.0.6, Spring Data JPA, Spring Security |
| **Database** | MySQL (Production), H2 Database (Testing) |
| **Frontend** | Vue 3, Vite, JavaScript ES6+, Vanilla CSS (Custom Design Token) |
| **AI / Data Mining** | Python 3.12, Flask, Pandas, Scikit-learn (KNN), Mlxtend (Apriori) |
| **AI Chatbot** | Google Gemini API (REST client) |
| **Testing** | JUnit 5, Mockito, AssertJ |
| **Container & CI/CD** | Docker, Docker Buildx, GitHub Actions |

---

## 📁 6. Cấu trúc thư mục dự án

```
E_LEARNING/
├── .github/
│   └── workflows/
│       └── ci.yml             # Cấu hình GitHub Actions CI/CD Pipeline
├── Edu_Recommend/
│   └── doan/                  # Spring Boot 4 Backend Project
│       ├── src/
│       │   ├── main/          # Mã nguồn Backend (Controllers, Services, Models, Repositories)
│       │   └── test/          # Unit Tests (JUnit 5 + Mockito)
│       ├── mvnw / mvnw.cmd    # Maven Wrapper
│       └── pom.xml            # Cấu hình Maven dependencies
├── flask_api/                 # Python Flask AI Engine (Apriori + KNN)
│   ├── app.py
│   └── requirements.txt
├── frontend-vue/              # Vue 3 Single Page Application
│   ├── src/
│   │   ├── components/        # UI Components (Nav, ChatWidget, CourseCard...)
│   │   ├── views/             # Các trang (Home, Courses, Recommend, Admin...)
│   │   └── services/          # Gọi API Backend
│   └── package.json
├── Dockerfile                 # Multi-stage Docker build file (Amazon Corretto 26)
└── README.md                  # Tài liệu hướng dẫn dự án
```

---

## 🚀 7. Hướng dẫn chạy cục bộ (Local Setup)

### Yêu cầu tiên quyết:
- **JDK 26** trở lên
- **Node.js** v18+ & **npm**
- **Python** 3.10+
- **MySQL** 8.0+

### Bước 1: Khởi chạy Backend (Spring Boot)
```bash
cd Edu_Recommend/doan

# Chạy bằng Maven Wrapper
./mvnw spring-boot:run
```
*(Backend sẽ khởi chạy tại port `8080`)*

### Bước 2: Khởi chạy Frontend (Vue 3)
```bash
cd frontend-vue

# Cài đặt dependencies
npm install

# Chạy môi trường dev
npm run dev
```
*(Frontend sẽ khởi chạy tại `http://localhost:5173`)*

### Bước 3: Khởi chạy AI Service (Flask)
```bash
cd flask_api

# Cài đặt thư viện Python
pip install -r requirements.txt

# Chạy Flask app
python app.py
```
*(Flask AI Engine sẽ khởi chạy tại port `5000`)*

---

## 🐳 8. Chạy bằng Docker

Ứng dụng hỗ trợ đóng gói Docker siêu nhẹ bằng Multi-stage Build:

```bash
# Build Docker image
docker build -t edurecommend-backend:latest .

# Chạy container
docker run -d -p 8080:8080 --name backend edurecommend-backend:latest
```

---

## ⚙️ 9. Quyền trình CI/CD (GitHub Actions)

Dự án được tích hợp tự động hóa quy trình phát triển và triển khai:

```
[ Git Push / Pull Request ]
            │
            ▼
 🧪 CI Job (Chạy trên mọi Branch)
    ├── Checkout Code
    ├── Set up JDK 26 (Amazon Corretto)
    └── Run Unit Tests (./mvnw clean test)
            │
            ├── (Nếu FAILED) ──► ❌ Dừng workflow & Báo lỗi
            │
            ▼ (Nếu PASSED)
 🐳 CD Job (Chỉ chạy khi Push/Merge vào nhánh 'main')
    ├── Set up Docker Buildx
    ├── Log in to Docker Hub
    ├── Build Docker Image
    └── Push to Docker Hub (caoducmanh1611/edurecommend-backend:latest)
```

---

## 📄 10. Giấy phép & Bản quyền

Dự án được phát triển phục vụ mục đích nghiên cứu, học tập và làm đồ án khai phá dữ liệu / phát triển ứng dụng Web.
