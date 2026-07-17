# 🎓 EduRecommend - E-Learning & AI-Powered Course Recommendation System

**EduRecommend** is a comprehensive educational technology solution combining **Data Mining**, **Machine Learning**, and **Software Engineering**. The platform not only manages and sells online programming courses (E-Learning) but also serves as an AI academic advisor. It analyzes students' study habits, academic history, and daily routines to recommend personalized course learning paths through predictive modeling.

---

## 🌐 Multilingual Readme
*   [Tiếng Việt (Vietnamese Version)](./README.md)

---

## 🛠️ TECHNOLOGIES USED (TECH STACK)

A complete end-to-end stack was implemented to handle data processing, model training, AI serving, and web platform hosting:

### 1. Web Backend & Database
*   **Java 26.0.1 (Amazon Corretto)**: Leveraging the latest Java runtime features for optimized execution.
*   **Spring Boot 4.0.6**: Core framework providing a robust, production-ready web application architecture.
*   **Spring Security**: Standard security filter chain managing secure login, session cookies, remember-me tokens, and CSRF protection.
*   **Spring Data JPA & Hibernate**: Object-Relational Mapping (ORM) framework to simplify database persistence operations.
*   **MySQL 8.0**: Relational Database Management System (RDBMS) storing user profiles, courses, orders, invoices, coupons, and progress logs.
*   **Thymeleaf**: Server-side Template Engine for rendering dynamic HTML views.

### 2. Artificial Intelligence & Data Mining
*   **Python 3.12**: Primary programming language utilized for data science and model training.
*   **Flask API**: Lightweight Python web framework wrapping the ML model into REST endpoints, deployed on **Render** (24/7).
*   **Pandas & NumPy**: Data manipulation libraries used for data cleaning, handling missing values, and data type casting.
*   **Matplotlib & Seaborn**: Libraries used for data visualization and exploratory data analysis (EDA).
*   **Scikit-learn**: Machine learning library used to train the **K-Nearest Neighbors (KNN)** student classifier.
*   **Mlxtend**: Library executing the **Apriori Association Rules** algorithm to generate personalized course recommendations.
*   **Gemini 2.5 Flash API**: Large Language Model (LLM) integrated into the frontend as an interactive AI assistant widget.

### 3. Frontend & UX
*   **HTML5, CSS3, JavaScript**: Responsive layout styling and client-side dynamic behaviors.
*   **Inter Typography**: System-wide font standardization to ensure clean, readable, and modern UI text rendering.
*   **SweetAlert2 & Animate.css**: Libraries creating dynamic popup alerts and smooth animations.

---

## 🗺️ DEVELOPMENT LIFECYCLE ROADMAP

The development pipeline transitions logically from raw data research to production software:

```
[Raw Survey Data] 
       │ (Pandas, NumPy, re, unicodedata)
       ▼
[PHASE 1: PREPROCESSING & CLEANING] (Imputation, Normalization, Skill set parsing)
       │
       ▼
[DATA VISUALIZATION & ANALYSIS] (Correlation Heatmaps, Distribution Histograms)
       │
       ▼
[PHASE 2: MACHINE LEARNING TRAINING] (KNN & Apriori rules training, Flask API deployment)
       │
       ▼
[PHASE 3: SOFTWARE APPLICATION] (Spring Boot Web App, Security Role Filter, Gemini Chat)
```

---

## 📊 PHASE 1: DATA MINING & PREPROCESSING (CLEANING)

Raw data is extracted from two sources and cleaned in Jupyter Notebooks (`Data_Cleaning.ipynb` and `Clean_Data_for_MySQL.ipynb`):

### 1. Data Sources
*   **Student Dataset (10,000 survey records):** Contains detailed student attributes, including academic metrics (`Hours_Studied`, `Attendance`, `Previous_Scores`, `Tutoring_Sessions`) and daily habits (`Sleep_Hours`, `Extracurricular_Activities`, `Physical_Activity`, `Parental_Education_Level`, `Distance_from_Home`).
*   **Coursera Dataset (Hundreds of tech courses):** Contains course metadata including titles, difficulty levels, enrollment count, and comma-separated skills taught.

### 2. Preprocessing & Cleaning (Pandas & NumPy)
*   **Missing Value Imputation:**
    *   **Categorical columns:** Missing values filled with the mode. (e.g., `Parental_Education_Level` set to `'High School'`, `Distance_from_Home` set to `'Near'`).
    *   **Numerical columns:** Missing values filled with the median to prevent outliers from skewing the model distribution.
*   **Numeric Conversion:** Standardized the `course_students_enrolled` column from abbreviations (e.g., `120k` converted to `120000`, `1.2m` converted to `1200000`) into clean integer formats for indexing.
*   **Database Normalization (3NF):** Extracted the skills column from Coursera data to create a standalone skills registry (`skills.csv`) and a lookup table (`external_course_skills.csv`) for a clean relational database schema in MySQL.

---

## 📈 DATA VISUALIZATION & EDA

To understand feature distributions and verify statistical integrity, visualizations were generated using **Matplotlib** and **Seaborn**:

1.  **Correlation Heatmap (`sns.heatmap`):**
    *   Revealed that `Hours_Studied`, `Attendance`, and `Previous_Scores` have the highest Pearson correlation coefficients relative to student academic performance. This guided our feature scaling weights in model training.
2.  **Distribution Plots (Histograms/KDE):**
    *   Examined the distribution of study hours and test scores to ensure proper standardization and avoid model bias.
3.  **Apriori Association Rules Plot:**
    *   Visualized Support vs. Confidence to filter out weak rules, retaining only high-confidence rules for course path recommendation.

---

## 🤖 PHASE 2: MACHINE LEARNING & AI SERVICE

### 1. Model Training (`Data_Mining_Train_Project.ipynb`)
*   **K-Nearest Neighbors (KNN Classifier):**
    *   Groups students into performance cohorts based on their academic and lifestyle metrics.
    *   Input features are scaled using **StandardScaler** to ensure uniform variance.
    *   **Custom Feature Weighting:** Multiplied the values of the 3 key features (`Hours_Studied`, `Attendance`, `Previous_Scores`) by **x3** to emphasize their impact on the classification boundary.
*   **Apriori Association Rules:**
    *   Mined association rules linking performance cohorts to optimal sequence tracks of programming courses (e.g., Beginner X ➔ Intermediate Y ➔ Advanced Z).

### 2. AI Server Deployment (Flask API)
*   The trained models are serialized and hosted via a lightweight **Flask** web API.
*   Deployed on **Render** (`https://flask-recommend-api.onrender.com`).
*   Spring Boot queries this API asynchronously using `RestTemplate` to request recommendations based on real-time user inputs.

---

## 💻 PHASE 3: PRODUCTION SOFTWARE APPLICATION

The **Spring Boot** application handles all user operations and commercial e-learning flows:

### 1. Authentication & Security (Spring Security)
*   Role-based authorization: **ROLE_STUDENT**, **ROLE_TEACHER**, **ROLE_ADMIN**.
*   Form-based authentication with 14-day persistent Remember-Me tokens.
*   CSRF protection disabled for API requests, concurrent session management limited to 1 active session per user, and custom access-denied pages (`/access-deny`).
*   Publicly permit all requests to the AI chat endpoint (`/api/ai/**`) so guest users can chat with the assistant on landing pages.

### 2. Business Features

#### A. Student Features
*   **Course Discovery & Checkout**: Add courses to cart, apply coupon discounts, simulated checkout, and download dynamic invoices with transactional UUID codes.
*   **AI Path Suggestion**: Enter lifestyle/academic attributes, send REST requests to the Flask server, and render recommendations on the UI.
*   **Learning Area & Certificates**: Video lecture player, recursive comments for Q&A, and automatic PDF Certificate generation upon 100% course completion.
*   **Refund Requests**: Request refunds within 7 days of purchase if course progress is under 20%.

#### B. Teacher Features
*   **Course Management**: Upload course chapters and video lectures with no file size limitations.
*   **Earnings Report**: Track enrollment statistics, student study progress, and net revenue share after platform commission deductions.

#### C. Admin Features
*   **Earnings Dashboard**: Live charting of system-wide revenue over time.
*   **Approvals**: Approve/reject new teacher courses and process student refund requests (automatically revoking student access and adjusting teacher commissions).

#### D. Gemini AI Assistant
*   An overlay chatbot powered by the **Gemini 2.5 Flash API** to answer queries about courses and policies.
*   Includes a fallback offline rule-based mechanism that triggers automatically if the API key is not configured.

---

## 📁 PROJECT STRUCTURE

```
Edu_Recommend/
├── Edu_Recommend_Data/                 # Datasets & Jupyter Notebooks
├── doan/                               # Spring Boot codebase
│   ├── src/main/java/com/example/doan/
│   │   ├── Config/                     # Web configs & Security filters
│   │   ├── Controller/                 # Sub-divided REST/Page Controllers (admin, student, teacher, shared)
│   │   ├── Service/                    # Domain-driven service interfaces & classes
│   │   ├── Model/                      # JPA Entities (course, user, order, enrollment, etc.)
│   │   └── Repository/                 # Database JPA repositories
│   └── src/main/resources/
│       ├── templates/                  # Thymeleaf templates styled with Inter font
│       └── application.properties       # Database, server, and API configurations
```

---

## 🛠️ RUNNING THE PROJECT

### 1. Database Initialization
Create a MySQL database:
```sql
CREATE DATABASE doan_khai_pha CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Connection Settings
Modify `doan/src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
```

### 3. Build & Run
From the `doan/` directory, execute:
```bash
# Compile project
./mvnw clean compile

# Run Spring Boot app
./mvnw spring-boot:run
```
Access the application locally at: `http://localhost:8080/`

*(Note: Default password for seeded demo accounts is `123456`)*

---

## 👥 AUTHORS

*   **Cao Duc Manh**
*   **Nguyen Nhat Toan**
*   *Capstone Project / Applied Data Mining & Machine Learning course project.*
