# E-Learning Project

A student management web application built with Spring Boot, featuring user authentication and role-based authorization via Spring Security.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| Framework | Spring Boot |
| Security | Spring Security + BCrypt |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL |
| Template Engine | Thymeleaf |
| Frontend | Bootstrap 5, Tabler Icons, HTML/CSS |
| Build Tool | Maven |

---

## Features

- User registration & login (Spring Security)
- View full student list
- Add new student
- Update student information
- Delete student
- Search student by name
- Role-based access (ADMIN / USER)

---

## Project Structure

```
src/main/java/com/example/demo/
├── Config/
│   ├── SecurityConfig.java            # Spring Security configuration
│   └── CustomUserDetailsService.java  # Load user from database
├── Controller/
│   └── SinhVien_Controller.java       # HTTP request handler
├── Model/
│   └── SinhVien.java                  # Entity mapped to Student table
├── Repository/
│   └── SinhVienRepository.java        # Database queries
└── Service/
    └── SinhVien_Service.java          # Business logic

src/main/resources/
├── templates/
│   ├── layout/
│   │   ├── header.html                # Shared header
│   │   └── footer.html                # Shared footer
│   ├── auth/
│   │   ├── login.html                 # Login page
│   │   ├── register.html              # Register page
│   │   └── home.html                  # Public home page
│   ├── Home.html                      # Student list
│   ├── Create.html                    # Add student form
│   ├── Update.html                    # Edit student form
│   ├── TimKiem.html                   # Search page
│   └── TimKiem2.html                  # Search result page
└── application.properties             # App & database config
```

---

## Getting Started

### Prerequisites

- Java 17+
- MySQL running locally
- Maven

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/CaoDucManh1611/E_LEARNING.git
cd E_LEARNING
```

**2. Create the database**
```sql
CREATE DATABASE e_learning;
```

**3. Configure database connection**

Edit `demo/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/e_learning
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

**4. Run the application**
```bash
cd demo
mvnw spring-boot:run
```

**5. Open in browser**
```
http://localhost:8080/Home
```

---

## API Routes

| Method | URL | Description | Auth Required |
|--------|-----|-------------|---------------|
| GET | `/Home` | Student list | Yes |
| GET | `/login` | Login page | No |
| GET | `/register` | Register page | No |
| GET | `/Create` | Add student form | Yes |
| POST | `/Create` | Save new student | Yes |
| GET | `/Update/user/{id}` | Edit student form | Yes |
| POST | `/Update` | Save updated student | Yes |
| POST | `/Delete/user/{id}` | Delete student | Yes |
| GET | `/TimKiem` | Search page | Yes |
| POST | `/TimKiem` | Search by name | Yes |

---

## Author

**Cao Duc Manh** — [github.com/CaoDucManh1611](https://github.com/CaoDucManh1611)
