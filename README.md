# 💰 ExpenseX

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Security-6DB33F?logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication-black?logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black)

A full-stack **personal expense management application** built with **Java 21, Spring Boot, PostgreSQL, Spring Security, JWT, Thymeleaf, and Docker**.

ExpenseX allows users to securely manage **expenses, income, budgets, categories, authentication, analytics, and financial reports** through a web-based interface and REST APIs.

---

## 📌 About

ExpenseX is designed as a practical full-stack application demonstrating backend development, authentication, database management, REST API design, reporting, and containerization.

The application provides:

- 🔐 Secure authentication
- 💸 Expense management
- 💰 Budget management
- 🏷️ Category management
- 📊 Financial dashboards
- 📈 Analytics
- 📄 PDF & Excel reports
- 📧 Email OTP verification
- 🔑 JWT access & refresh tokens
- 📱 SMS transaction integration
- 🐳 Docker deployment
- 📚 Swagger API documentation

---

# 🚀 Features

## 🔐 Authentication & Security

- User registration
- Email OTP verification
- Login OTP verification
- JWT authentication
- Access and refresh tokens
- Forgot password
- Password reset
- Change password
- Account activation/deactivation
- Spring Security
- CORS configuration
- Environment-based secrets

## 💸 Expense Management

- Add expenses
- View expenses
- Edit expenses
- Delete expenses
- Category-based filtering
- Date-range filtering
- Monthly expense calculation
- Total expense calculation
- Expense categorization

## 💰 Budget Management

- Create budgets
- View budgets
- Update budgets
- Delete budgets
- User-specific budgets
- Monthly budget management

## 🏷️ Category Management

- Create categories
- View categories
- Update categories
- Delete categories

## 📊 Dashboard & Analytics

- Expense dashboard
- Monthly summaries
- Financial summaries
- Category-wise analysis
- User-specific analytics
- Monthly and yearly statistics

## 📄 Reports

Generate financial reports in:

- PDF
- Excel

Reports include:

- Monthly summary
- Yearly summary
- Category-wise summary
- Expense analytics

## 📧 Email & OTP

Email functionality is implemented using Gmail SMTP.

Supported operations:

- Registration OTP
- Login OTP
- Password reset
- OTP verification
- OTP resend

> ⚠️ Never commit email passwords, Google App Passwords, JWT secrets, database credentials, or API keys to GitHub.

---

# 📱 SMS Expense Integration

ExpenseX includes a webhook endpoint designed for integration with a future Android application that can read bank transaction SMS messages.

```http
POST /api/webhook/sms/{webhookToken}
```

### Architecture

```text
Bank SMS
   ↓
Android Application
   ↓
SMS Parser
   ↓
ExpenseX REST API
   ↓
Spring Boot
   ↓
PostgreSQL
   ↓
Dashboard / Reports
```

This provides a foundation for automatically detecting and recording bank transactions.

---

# 🛠️ Technology Stack

## Backend

- Java 21
- Spring Boot 3.5.4
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- Spring Validation
- Spring Mail
- Thymeleaf

## Database

- PostgreSQL
- PostgreSQL JDBC Driver

## Authentication

- JWT
- Spring Security
- Refresh Tokens
- OTP Verification

## Libraries

- Lombok
- ModelMapper
- JJWT
- Jackson Java 8 Date/Time
- iText PDF
- Apache POI

## API Documentation

- Springdoc OpenAPI
- Swagger UI

## DevOps

- Docker
- Docker Compose
- Maven

---

# 🏗️ Application Architecture

```text
                    ┌──────────────────┐
                    │      User        │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │   Thymeleaf UI   │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │ Spring Security  │
                    │   JWT + OTP      │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │   Controllers    │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │    Services      │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │ Repository / JPA │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │   PostgreSQL     │
                    └──────────────────┘
```

---

# 📂 Project Structure

```text
ExpenseTracker/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/expensetracker/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       └── util/
│   │   │
│   │   └── resources/
│   │       ├── templates/
│   │       ├── static/
│   │       └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── docker-compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```

---

# ⚙️ Requirements

Install the following before running the project:

- Java 21
- Maven
- PostgreSQL
- Git
- Docker Desktop *(optional)*

Check your installation:

```bash
java -version
mvn -version
psql --version
docker --version
```

---

# 🗄️ Database Setup

Create the database using `psql` or pgAdmin:

```sql
CREATE DATABASE expense_tracker;
```

| Configuration | Value |
|---|---|
| Database | `expense_tracker` |
| Port | `5432` |
| Default user | `postgres` |

The application uses:

```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Hibernate will automatically create/update the required tables during development.

Maven dependency for the PostgreSQL driver:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

# 🔑 Environment Configuration

Use environment variables for sensitive configuration.

Example:

```env
DB_URL=jdbc:postgresql://localhost:5432/expense_tracker
DB_USERNAME=postgres
DB_PASSWORD=YOUR_DATABASE_PASSWORD

JWT_SECRET=YOUR_LONG_RANDOM_JWT_SECRET
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

MAIL_USERNAME=your-email@gmail.com
MAIL_APP_PASSWORD=YOUR_GOOGLE_APP_PASSWORD
```

### Never commit

```text
.env
Database passwords
JWT secrets
Gmail App Passwords
API keys
Webhook tokens
Production credentials
```

If a secret has already been pushed to GitHub, **rotate it immediately**.

---

# ▶️ Run Locally

## 1. Clone Repository

```bash
git clone https://github.com/Lokesh-github07/ExpenseX.git
cd ExpenseX
```

## 2. Create Database

```sql
CREATE DATABASE expense_tracker;
```

## 3. Configure Environment Variables

Configure your database, JWT, and email credentials.

## 4. Build

```bash
mvn clean install
```

Windows:

```bash
mvnw.cmd clean install
```

## 5. Start Application

```bash
mvn spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

Application:

```text
http://localhost:8080
```

---

# 🌐 Web Pages

Main application pages include:

```text
/
├── /login
├── /register
├── /terms
├── /forgot-password
├── /dashboard
├── /add-expense
├── /edit-expense
├── /profile
└── /analytics
```

---

# 🐳 Docker

ExpenseX can also run using Docker Compose.

### Services

```text
┌──────────────────────┐
│   expense-tracker-app│
│       :8080          │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────────┐
│ expense-tracker-postgres │
│          :5432           │
└──────────────────────────┘
```

Example PostgreSQL service in `docker-compose.yaml`:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: expense-tracker-postgres
    environment:
      POSTGRES_DB: expense_tracker
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

In Docker, the application connects using the service name as host:

```env
DB_URL=jdbc:postgresql://postgres:5432/expense_tracker
```

## Start

Create `.env`:

```env
DB_PASSWORD=YOUR_DATABASE_PASSWORD
JWT_SECRET=YOUR_LONG_RANDOM_SECRET
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

MAIL_USERNAME=your-email@gmail.com
MAIL_APP_PASSWORD=YOUR_GOOGLE_APP_PASSWORD
```

Run:

```bash
docker compose up --build
```

Run in background:

```bash
docker compose up --build -d
```

Check containers:

```bash
docker ps
```

Open:

```text
http://localhost:8080
```

Stop:

```bash
docker compose down
```

Remove containers and database volume:

```bash
docker compose down -v
```

> ⚠️ `docker compose down -v` removes the PostgreSQL Docker volume and its stored data.

---

# 📚 API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger allows you to:

- View APIs
- Test endpoints
- Inspect requests
- Inspect responses
- Test protected endpoints

---

# 🔐 API Overview

### Authentication

```text
POST /api/auth/register
POST /api/auth/verify-otp
POST /api/auth/resend-otp
POST /api/auth/login
POST /api/auth/login/verify-otp
POST /api/auth/refresh-token
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/auth/health
```

### Expenses

```text
POST   /api/expenses
GET    /api/expenses
GET    /api/expenses/{id}
PUT    /api/expenses/{id}
DELETE /api/expenses/{id}
GET    /api/expenses/category/{categoryId}
GET    /api/expenses/date-range
GET    /api/expenses/total
GET    /api/expenses/monthly-total
```

### Budgets

```text
POST   /api/budgets
GET    /api/budgets
GET    /api/budgets/{id}
PUT    /api/budgets/{id}
DELETE /api/budgets/{id}
GET    /api/budgets/user/{userId}
GET    /api/budgets/month
```

### Categories

```text
POST   /api/categories
GET    /api/categories
GET    /api/categories/{id}
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

### Dashboard

```text
GET /api/dashboard
GET /api/dashboard/user/{userId}
GET /api/dashboard/monthly
GET /api/dashboard/summary
```

### Reports

```text
GET /api/reports/analytics
GET /api/reports/pdf
GET /api/reports/excel
GET /api/reports/monthly-summary
GET /api/reports/yearly-summary
GET /api/reports/category-summary
```

### Users

```text
GET    /api/users
GET    /api/users/{id}
GET    /api/users/email/{email}
PUT    /api/users/{id}/profile
PUT    /api/users/change-password
DELETE /api/users/account
DELETE /api/users/{id}
PUT    /api/users/{id}/activate
PUT    /api/users/{id}/deactivate
```

---

# 🧪 Testing

The application can be tested using:

- JUnit
- Mockito
- Postman
- Swagger UI
- Selenium
- Docker

### Testing Flow

```text
Unit Testing
     ↓
Integration Testing
     ↓
API Testing
     ↓
Manual Testing
     ↓
UI Automation
     ↓
Docker Testing
     ↓
Deployment Testing
```

---

# 📦 Build JAR

```bash
mvn clean package -DskipTests
```

Generated JAR:

```text
target/expense-tracker-0.0.1-SNAPSHOT.jar
```

Run:

```bash
java -jar target/expense-tracker-0.0.1-SNAPSHOT.jar
```

---

# 🔒 Security

ExpenseX uses:

- Spring Security
- JWT authentication
- Refresh tokens
- OTP verification
- Password reset
- Account activation/deactivation
- SMTP authentication
- Environment-based secrets

For production:

- Use HTTPS
- Use strong JWT secrets
- Secure database credentials
- Configure restrictive CORS
- Protect webhook tokens
- Disable unnecessary debug logging
- Maintain database backups
- Never commit secrets

---

# 🔮 Future Improvements

- 📱 Android application
- 🏦 Automatic bank SMS transaction detection
- 💳 UPI transaction detection
- 🔔 Push notifications
- 🔄 Recurring expenses
- 🎯 Savings goals
- 📈 Investment tracking
- 💱 Multiple currencies
- 🤖 AI-powered expense categorization
- ☁️ Cloud deployment
- 🔄 CI/CD pipeline
- 🧪 Automated integration testing
- 🧪 Automated API testing
- 📋 Jira QA workflow
- 👨‍💼 Admin dashboard

---

# 📊 Project Highlights

```text
Java 21
Spring Boot
Spring Security
JWT
REST APIs
PostgreSQL
JPA / Hibernate
Thymeleaf
OTP Authentication
Email Integration
PDF Reports
Excel Reports
Docker
Docker Compose
Swagger / OpenAPI
API Testing
SMS Integration
```

---

# 👨‍💻 Author

**Lokesh Pande**

Java Full Stack Developer  
Spring Boot • REST APIs • PostgreSQL • Docker • Testing

🔗 GitHub:  
https://github.com/Lokesh-github07

---

## ⭐ Project

If you find this project useful, consider giving it a ⭐ on GitHub.

---

## 📜 License

This project is intended for educational, portfolio, and development purposes.
