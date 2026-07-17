# 🎓 EduRecommend - Hệ Thống E-Learning & Gợi Ý Lộ Trình Học Tập Tối Ưu Bằng AI

**EduRecommend** là một giải pháp công nghệ toàn diện kết hợp giữa **Khai phá dữ liệu (Data Mining)**, **Học máy (Machine Learning)** và **Kỹ nghệ phần mềm (Software Engineering)**. Nền tảng không chỉ quản lý và kinh doanh khóa học lập trình trực tuyến (E-Learning) chuyên nghiệp mà còn đóng vai trò như một cố vấn học tập ảo – phân tích các chỉ số học tập, sức khỏe và sinh hoạt của từng học viên để đề xuất lộ trình khóa học tối ưu thông qua mô hình dự báo AI.

---

## 🌐 Multilingual Readme
*   [English Version (Bản tiếng Anh)](./README_EN.md)

---

## 🛠️ CÔNG NGHỆ SỬ DỤNG (TECH STACK)

Để xây dựng một hệ thống hoàn chỉnh từ xử lý dữ liệu, chạy mô hình AI đến triển khai ứng dụng thực tế, các công nghệ sau đã được áp dụng:

### 1. Phân Hệ Backend Web & Cơ Sở Dữ Liệu
*   **Java 26.0.1 (Amazon Corretto)**: Phiên bản Java mới nhất để tối ưu hóa hiệu năng thực thi hệ thống.
*   **Spring Boot 4.0.6**: Framework cốt lõi để xây dựng cấu trúc phần mềm ổn định, nhanh chóng.
*   **Spring Security**: Thiết lập bộ lọc bảo mật, xử lý đăng nhập, quản lý session và phân quyền người dùng (Student/Teacher/Admin).
*   **Spring Data JPA & Hibernate**: Đơn giản hóa việc thực hiện các câu truy vấn và tự động ánh xạ đối tượng xuống database.
*   **MySQL 8.0**: Hệ quản trị cơ sở dữ liệu quan hệ lưu trữ thông tin tài khoản, khóa học, đơn hàng, hóa đơn, coupon và tiến độ học tập.
*   **Thymeleaf**: Công nghệ Template Engine xử lý kết xuất giao diện HTML động phía Server.

### 2. Phân Hệ Trí Tuệ Nhân Tạo & Khai Phá Dữ Liệu
*   **Python 3.12**: Ngôn ngữ chính dùng cho xử lý số liệu và học máy.
*   **Flask API**: Framework Web Python dùng để đóng gói mô hình ML thành REST API, được deploy độc lập trên cloud **Render** (24/7).
*   **Pandas & NumPy**: Thư viện xử lý dữ liệu dạng bảng, xử lý các giá trị khuyết thiếu và định dạng kiểu dữ liệu.
*   **Matplotlib & Seaborn**: Thư viện vẽ biểu đồ phân tích dữ liệu trực quan (Data Visualization).
*   **Scikit-learn**: Thư viện huấn luyện thuật toán **K-Nearest Neighbors (KNN)** phân loại nhóm học lực học sinh.
*   **Mlxtend**: Thư viện chạy thuật toán **Apriori** khai phá luật kết hợp để gợi ý khóa học tối ưu.
*   **Gemini 2.5 Flash API**: Mô hình ngôn ngữ lớn (LLM) của Google tích hợp làm widget AI chatbot tư vấn tự động.

### 3. Phân Hệ Frontend
*   **HTML5, CSS3, JavaScript**: Xây dựng giao diện web phản hồi (Responsive) và xử lý tương tác động.
*   **Font chữ Inter**: Được chuẩn hóa toàn bộ trên tất cả các trang giao diện, đem lại cảm giác hiện đại, dễ đọc cho giao diện tiếng Việt.
*   **SweetAlert2 & Animate.css**: Thư viện tạo hộp thoại popup thông báo động và hiệu ứng chuyển động mượt mà cho trải nghiệm người dùng.

---

## 🗺️ TỔNG QUAN KỊCH BẢN PHÁT TRIỂN DỰ ÁN

Quy trình phát triển dự án được thiết kế nghiêm ngặt theo mô hình chuyển tiếp từ nghiên cứu dữ liệu đến ứng dụng thực tiễn:

```
[Dữ liệu Khảo sát Thô] 
       │ (Pandas, NumPy, re, unicodedata)
       ▼
[GIAI ĐOẠN 1: TIỀN XỬ LÝ & LÀM SẠCH] (Điền khuyết, Chuẩn hóa, Khai thác thuộc tính kỹ năng)
       │
       ▼
[TRỰC QUAN HÓA & PHÂN TÍCH DỮ LIỆU] (Vẽ biểu đồ tương quan Heatmap, Biểu đồ phân phối Histogram/KDE)
       │
       ▼
[GIAI ĐOẠN 2: HUẤN LUYỆN MÔ HÌNH AI] (Train mô hình KNN & Apriori, Deploy Flask API trên Cloud)
       │
       ▼
[GIAI ĐOẠN 3: PHẦN MỀM THỰC TẾ] (Spring Boot Web App, Phân quyền Security, Widget AI Gemini Chat)
```

---

## 📊 GIAI ĐOẠN 1: KHAI PHÁ & TIỀN XỬ LÝ DỮ LIỆU (DATA MINING & CLEANING)

Dữ liệu thô ban đầu được khai thác từ hai nguồn chính và được xử lý trong các file Jupyter Notebook (`Data_Cleaning.ipynb` và `Clean_Data_for_MySQL.ipynb`):

### 1. Nguồn Dữ Liệu Ban Đầu
*   **Student Dataset (10,000 dòng khảo sát):** Chứa thông tin chi tiết về thói quen học tập và đời sống của học sinh. Các cột thuộc tính bao gồm: `Hours_Studied` (Giờ tự học), `Attendance` (Chuyên cần), `Previous_Scores` (Điểm kỳ trước), `Sleep_Hours` (Giờ ngủ), `Tutoring_Sessions` (Số buổi học thêm), `Extracurricular_Activities` (Hoạt động ngoại khóa), `Physical_Activity` (Giờ thể thao), `Parental_Education_Level` (Trình độ học vấn cha mẹ), `Distance_from_Home` (Khoảng cách từ nhà đến trường).
*   **Coursera Dataset (Hàng trăm khóa học công nghệ):** Chứa thông tin về tên khóa học, cấp độ (Beginner, Intermediate, Advanced), số lượng học viên đã đăng ký, và chuỗi văn bản danh sách kỹ năng bổ trợ (`skills`).

### 2. Chi Tiết Kỹ Thuật Làm Sạch Dữ Liệu (Python - Pandas & NumPy)
Dữ liệu thô chứa nhiều nhiễu, giá trị khuyết thiếu và sai định dạng đã được xử lý qua các bước:
*   **Xử lý dữ liệu khuyết thiếu (Missing Data Imputation):**
    *   **Đối với thuộc tính phân loại (Categorical):** Dùng giá trị xuất hiện nhiều nhất (Mode). Ví dụ: `Parental_Education_Level` rỗng được điền là `'High School'`; `Distance_from_Home` rỗng được điền là `'Near'`.
    *   **Đối với thuộc tính số (Numerical):** Dùng giá trị trung vị (Median) của cột để tránh làm lệch phân phối dữ liệu gốc khi có các điểm dị biệt (outliers).
*   **Làm sạch dữ liệu định dạng số lượng học viên:**
    *   Cột `course_students_enrolled` ban đầu chứa dữ liệu dạng văn bản (ví dụ: `120k` hoặc `1.2m`).
    *   Đã sử dụng biểu thức chính quy (Regular Expression) để lọc lấy phần số, nhân tương ứng với hệ số (nhân `1,000` cho `k` và `1,000,000` cho `m`) để chuyển đổi thành kiểu số nguyên (`Integer/Long`) đồng bộ phục vụ sắp xếp, thống kê.
*   **Chuẩn hóa cấu trúc CSDL quan hệ:**
    *   Cột `skills` chứa danh sách kỹ năng dạng mảng chuỗi trong file CSV.
    *   Tiến hành phân tách (flatten) danh sách kỹ năng này thành bảng danh mục kỹ năng độc lập (`skills.csv`) và bảng trung gian liên kết khóa học - kỹ năng (`external_course_skills.csv`) để import trực tiếp vào cấu trúc cơ sở dữ liệu MySQL, đảm bảo chuẩn hóa dạng chuẩn 3 (3NF).

---

## 📈 TRỰC QUAN HÓA & KHAI PHÁ DỮ LIỆU (DATA VISUALIZATION)

Để hiểu rõ cấu trúc dữ liệu trước khi đưa vào mô hình học máy, các biểu đồ trực quan hóa dữ liệu được vẽ trong các Notebook bằng thư viện **Matplotlib** và **Seaborn**:

1.  **Biểu Đồ Nhiệt Tương Quan (Correlation Heatmap):**
    *   Sử dụng `sns.heatmap` hiển thị ma trận tương quan giữa các biến số. 
    *   Kết quả phân tích chỉ ra rằng **Hours_Studied** (Số giờ tự học), **Attendance** (Tỷ lệ chuyên cần), và **Previous_Scores** (Điểm số cũ) là 3 thuộc tính có hệ số tương quan tuyến tính Pearson cao nhất đối với kết quả học tập đầu ra của học sinh. Đây là cơ sở để tăng trọng số đặc trưng (feature weighting) trong mô hình KNN.
2.  **Biểu Đồ Phân Phối (Distribution Plots - Histograms/KDE):**
    *   Vẽ biểu đồ phân phối tần suất của cột điểm số cũ (`Previous_Scores`) và số giờ tự học (`Hours_Studied`).
    *   Giúp kiểm tra xem dữ liệu có bị lệch (skewed) hay tuân theo phân phối chuẩn để quyết định phương pháp chuẩn hóa dữ liệu.
3.  **Biểu Đồ Luật Kết Hợp (Apriori Rules Scatter Plot):**
    *   Trực quan hóa tập hợp các luật kết hợp tìm được từ thuật toán Apriori dựa trên hai trục tọa độ: Độ hỗ trợ (Support) và Độ tin cậy (Confidence).
    *   Giúp bộ lọc lọc bỏ những luật kết hợp có độ tin cậy thấp, chỉ giữ lại các luật mạnh để đề xuất lộ trình khóa học chính xác nhất.

---

## 🤖 GIAI ĐOẠN 2: HUẤN LUYỆN MÔ HÌNH HỌC MÁY (MODEL TRAINING & AI SERVER)

Sau khi dữ liệu được làm sạch và phân tích trực quan, mô hình học máy được xây dựng và triển khai độc lập làm API gợi ý lộ trình học tập:

### 1. Thuật Toán Sử Dụng (`Data_Mining_Train_Project.ipynb`)
*   **K-Nearest Neighbors (KNN Classifier):**
    *   Học viên được phân vào các nhóm năng lực học tập dựa trên các thuộc tính đầu vào.
    *   Dữ liệu được chuẩn hóa thông qua **StandardScaler** để các đặc trưng có thang đo lớn không lấn át các đặc trưng khác.
    *   **Trọng số đặc trưng cá nhân hóa (Feature Weighting):** Tiến hành nhân trọng số **x3** đối với 3 đặc trưng quan trọng nhất: **Hours_Studied** (Số giờ tự học), **Attendance** (Tỷ lệ chuyên cần), và **Previous_Scores** (Điểm số cũ). Việc này giúp tăng cường độ chính xác khi phân loại nhóm học tập.
*   **Apriori (Luật kết hợp - Association Rules):**
    *   Sử dụng để tìm các luật kết hợp giữa nhóm năng lực học tập của học viên và các khóa học công nghệ thông tin tương ứng.
    *   Hệ thống sẽ gợi ý: "Học sinh thuộc nhóm năng lực học tập A sẽ cần học lộ trình gồm khóa học Beginner X -> Intermediate Y -> Advanced Z".

### 2. Triển Khai AI Server (Flask API)
*   Mô hình được lưu lại (serialize) và đóng gói thành một REST API sử dụng **Flask (Python)**.
*   Ứng dụng được deploy trực tuyến trên nền tảng đám mây **Render** tại địa chỉ: `https://flask-recommend-api.onrender.com`.
*   Spring Boot kết nối tới Flask API thông qua `RestTemplate` với thời gian Timeout được cấu hình tối ưu để xử lý độ trễ phản hồi của Cloud Server.

---

## 💻 GIAI ĐOẠN 3: PHẦN MỀM ỨNG DỤNG THỰC TẾ (SPRING BOOT PLATFORM)

Nền tảng phần mềm được phát triển bằng **Spring Boot** đóng vai trò là hệ thống tương tác chính với người dùng và quản lý toàn bộ nghiệp vụ E-Learning:

### 1. Kiến Trúc Bảo Mật & Phân Quyền (Spring Security)
*   Phân quyền chi tiết dựa trên vai trò: **ROLE_STUDENT** (Học viên), **ROLE_TEACHER** (Giảng viên), **ROLE_ADMIN** (Quản trị viên).
*   Đăng nhập bằng Form Login bảo mật, tích hợp tính năng tự động ghi nhớ đăng nhập (Remember Me) bằng token tồn tại trong 14 ngày.
*   Vô hiệu hóa tấn công CSRF cho các yêu cầu API, cấu hình trang báo lỗi phân quyền 403 (`/access-deny`) và giới hạn tối đa 1 session đăng nhập trên mỗi tài khoản để tăng tính bảo mật.
*   Mở quyền truy cập công khai không cần đăng nhập cho API chatbot AI (`/api/ai/**`) để khách truy cập trang chủ vẫn có thể được tư vấn.

### 2. Các Phân Hệ Nghiệp Vụ Của Hệ Thống

#### A. Phân Hệ Học Viên (Student)
*   **Gợi Ý Lộ Trình AI:** Nhập các thông số cá nhân, hệ thống gửi yêu cầu REST API tới Flask Server và kết xuất lộ trình khóa học phù hợp trực tiếp lên giao diện.
*   **Thanh Toán & Hóa Đơn (Invoice):** Áp dụng mã giảm giá (Coupon), xử lý giỏ hàng, thực hiện thanh toán giả lập. Khi đơn hàng chuyển trạng thái "Đã thanh toán" (Paid), hệ thống tự động xuất hóa đơn điện tử chứa mã vạch giao dịch độc nhất (UUID).
*   **Học Trực Tuyến & Cấp Chứng Chỉ:** Học viên xem video bài giảng, trao đổi bài học qua bình luận cây phân cấp (Lesson Comment). Khi tiến độ học tập đạt 100%, hệ thống tự động sinh file PDF Chứng chỉ (Certificate) chứa tên học viên, tên khóa học và ngày hoàn thành.
*   **Yêu Cầu Hoàn Tiền (Refund Flow):** Học viên có thể gửi yêu cầu hoàn tiền nếu khóa học mua dưới 7 ngày và tiến độ học dưới 20%.

#### B. Phân Hệ Giảng Viên (Teacher)
*   **Quản Lý Khóa Học & Bài Giảng:** Tạo khóa học, bài học mới và tải lên video không giới hạn kích thước file.
*   **Báo Cáo Doanh Thu:** Thống kê số lượng học viên thực tế, tiến độ học của từng em, và số tiền thực nhận sau khi khấu trừ hoa hồng của hệ thống (Commission).

#### C. Phân Hệ Quản Trị Viên (Admin)
*   **Bảng Điều Khiển Doanh Thu (Dashboard):** Biểu đồ trực quan hóa doanh thu theo thời gian, quản lý danh sách người dùng.
*   **Phê Duyệt Khóa Học & Hoàn Tiền:** Duyệt khóa học mới của giáo viên trước khi public; duyệt yêu cầu hoàn tiền of học viên (hệ thống tự động thu hồi quyền học và trừ hoa hồng of giáo viên tương ứng).

#### D. Trợ Lý AI Gemini Chatbot
*   Tích hợp trực tiếp **Gemini 2.5 Flash API** để chat trực tuyến, giải đáp thắc mắc về khóa học, lộ trình học và chính sách hoàn tiền.
*   Tích hợp sẵn bộ phản hồi **Offline thông minh** để tự động trả lời tư vấn dựa trên từ khóa nếu hệ thống mất kết nối API key.

---

## 📁 CẤU TRÚC THƯ MỤC DỰ ÁN (PROJECT STRUCTURE)

```
Edu_Recommend/
├── Edu_Recommend_Data/                 # Bộ dữ liệu thô, dữ liệu sạch và Notebooks ML
│   ├── Data_Cleaning.ipynb             # Notebook tiền xử lý dữ liệu khảo sát
│   ├── Clean_Data_for_MySQL.ipynb      # Notebook làm sạch và chuẩn hóa DB khóa học
│   ├── Data_Mining_Train_Project.ipynb # Notebook huấn luyện mô hình KNN & Apriori
│   └── *.csv                           # Các tệp dữ liệu CSV trước và sau khi làm sạch
├── doan/                               # Mã nguồn Backend Spring Boot
│   ├── src/main/java/com/example/doan/
│   │   ├── Config/                     # Cấu hình Web, Security (permitAll API AI), RestTemplate
│   │   ├── Controller/
│   │   │   ├── admin/                  # Quản lý Coupon, Doanh thu, Phê duyệt khóa học
│   │   │   ├── student/                # Xem khóa học, Gợi ý AI, Yêu cầu hoàn tiền, Chứng chỉ
│   │   │   ├── teacher/                # Đăng bài học, xem báo cáo doanh thu, tiến độ học viên
│   │   │   └── shared/                 # PageController, CartController, AIChatController (Gemini)
│   │   ├── Service/                    # Chứa logic nghiệp vụ phân tầng rõ ràng theo domain
│   │   │   ├── course/, order/, user/, enrollment/, review/, refund/, notification/, common/
│   │   ├── Model/                      # Thực thể JPA ánh xạ DB (StudentInfo, Order, Invoice, Course...)
│   │   │   ├── course/, order/, user/, enrollment/, review/, refund/, notification/, recommend/
│   │   ├── Repository/                 # Tầng giao tiếp database Spring Data JPA (chia theo domain)
│   │   └── DoanApplication.java        # Main class khởi động Spring Boot
│   └── src/main/resources/
│       ├── templates/                  # Giao diện Thymeleaf HTML/CSS/JS (đã đồng bộ font Inter)
│       └── application.properties       # Cấu hình Spring Boot, Kết nối DB, Gemini API Key
```

---

## 🛠️ HƯỚNG DẪN KHỞI CHẠY DỰ ÁN

### 1. Khởi Tạo Cơ Sở Dữ Liệu MySQL
Tạo cơ sở dữ liệu mới trong MySQL Command Line hoặc Workbench:
```sql
CREATE DATABASE doan_khai_pha CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Cấu Hình Kết Nối
Mở file `doan/src/main/resources/application.properties` và sửa thông tin kết nối MySQL của bạn:
```properties
spring.datasource.username=username_cua_ban
spring.datasource.password=password_cua_ban
```

### 3. Biên Dịch & Chạy Server
Mở Terminal tại thư mục `doan/` và chạy các lệnh:
```bash
# Biên dịch dự án
./mvnw clean compile

# Chạy ứng dụng Spring Boot
./mvnw spring-boot:run
```
Truy cập ứng dụng tại địa chỉ: `http://localhost:8080/`

*(Lưu ý: Mật khẩu đăng nhập mặc định của tất cả người dùng trong hệ thống dữ liệu mẫu đã được seed sẵn là `123456`)*

---

## 👥 TÁC GIẢ & THÀNH VIÊN THỰC HIỆN

*   **Cao Đức Mạnh**
*   **Nguyễn Nhật Toàn**
*   *Đồ án tốt nghiệp / Đồ án môn học Khai phá dữ liệu & Học máy ứng dụng.*
