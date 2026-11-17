# 💰 ExpenseX

[![Work in Progress](https://img.shields.io/badge/Status-Work%20in%20Progress-orange?style=for-the-badge)](https://github.com/Lokesh-github07/ExpenseX)

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Security-6DB33F?logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication-black?logo=jsonwebtokens&logoColor=white)
![Brevo](https://img.shields.io/badge/Email-Brevo%20SMTP-0B996E?logo=brevo&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black)

A full-stack **personal expense management application** built with **Java 21, Spring Boot, PostgreSQL, Spring Security, JWT, Thymeleaf, and Docker**.

ExpenseX lets users securely manage **expenses, income, budgets, categories, analytics, and financial reports** through a web interface and REST APIs. Transactional emails (OTP verification, password reset) are delivered through **Brevo SMTP**.

---

## 📌 About

ExpenseX is a practical full-stack project demonstrating backend development, authentication, database management, REST API design, reporting, email delivery, and containerization.

- 🔐 Secure authentication (JWT + email OTP)
- 💸 Expense management
- 💰 Budget management
- 🏷️ Category management
- 📊 Financial dashboards & analytics
- 📄 PDF & Excel reports
- 📧 Email OTP delivery via Brevo SMTP
- 📱 SMS transaction webhook (for a future Android app)
- 🐳 Docker deployment
- 📚 Swagger API documentation
- ☁️ Render deployment support

## 🖥️ Web Interface & Implementation Status

- **Public pages:** `/` (landing page), `/login`, `/register`, `/forgot-password`, `/terms`.
- **Theme:** A single permanent dark theme with an electric-orange accent (colors are CSS variables in `static/css/style.css`). There is no theme switching.
- **Sidebar:** Dashboard, Transactions & Expenses, Income & Budget, Bills/Loans/EMIs, Savings & CIBIL, Reports & Analysis, Financial Calendar. The profile link is pinned at the bottom.
- **Profile:** Personal details and Logout. **Security & Privacy:** change password, sign out of this browser, privacy preferences and account deletion.

| Area | Status |
| ---- | ------ |
| Authentication, email OTP (Brevo SMTP), JWT, expenses, categories, dashboard, analytics, PDF/Excel reports, calendar, profile, password change, account deletion | ✅ Implemented (server-side) |
| Income, Budget page, Bills, Loans & EMIs, Savings goals, CIBIL log | 🟡 Partial: the pages save to browser `localStorage` only (a budget REST API exists but the page does not use it yet) |
| Privacy preferences | 🟡 Browser-only; not enforced by the server |
| Sign out everywhere | ❌ Not implemented: tokens are not revoked server-side |
| SMS webhook / Android app | 🟡 Webhook endpoint exists; the Android client is not built yet |
| AI Finance Assistant | 🗓️ **Planned** (shown as "Coming Soon" in the sidebar; no route or functionality yet) |

> Protected pages are rendered without a server-side check and guarded in the browser by JavaScript; all `/api/**` data endpoints require a valid JWT.

---

# 🚀 Features

## 🔐 Authentication & Security

- User registration with email OTP verification
- Login OTP verification
- JWT authentication with access and refresh tokens
- Forgot password / password reset / change password
- Account activation/deactivation
- Spring Security with CORS configuration
- Environment-based secrets

## 💸 Expense Management

- Add, view, edit and delete expenses
- Category-based and date-range filtering
- Monthly and total expense calculation

## 💰 Budget Management

- Create, view, update and delete budgets (REST API)
- User-specific, monthly budgets
- _The Budget web page currently uses browser `localStorage`; see the status table above._

## 🏷️ Category Management

- Create, view, update and delete categories

## 📊 Dashboard & Analytics

- Expense dashboard and monthly/yearly summaries
- Category-wise and user-specific analysis

## 📄 Reports

Generate financial reports in **PDF** and **Excel**, including monthly summary, yearly summary, category-wise summary and expense analytics.

---

# 📧 Email & OTP – Brevo SMTP

ExpenseX sends all transactional email through **Brevo SMTP** (it no longer uses Gmail or any personal mailbox).

Supported operations:

- Registration OTP
- Login OTP
- Password reset
- OTP verification / resend

### SMTP settings

| Setting | Value |
| ------- | ----- |
| Provider | Brevo |
| Host | `smtp-relay.brevo.com` |
| Port | `2525` |
| Authentication | Enabled |
| STARTTLS | Enabled |
| Username | Brevo SMTP login (`BREVO_SMTP_LOGIN`) |
| Password | Brevo SMTP key (`BREVO_SMTP_KEY`) |
| From address | Verified Brevo sender (`MAIL_FROM`) |

Reference Spring configuration (adjust to match your `application.properties`):

```properties
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=2525
spring.mail.username=${BREVO_SMTP_LOGIN}
spring.mail.password=${BREVO_SMTP_KEY}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

> The SMTP **login** is not your sender address. Set the sender separately with `MAIL_FROM` and make sure it is verified in Brevo.

### Getting Brevo credentials

1. Sign in at [https://app.brevo.com/](https://app.brevo.com/).
2. Go to **Settings → SMTP & API → SMTP**.
3. Copy your **SMTP login**.
4. Generate an **SMTP key**.
5. Verify your **sender email/domain** under Senders.
6. Put the values in your environment variables (see [Environment Configuration](#-environment-configuration)).

> ⚠️ Use the Brevo **SMTP key**, not the Brevo API key, and not your Gmail password.
>
> ⚠️ Never commit SMTP credentials, JWT secrets, database passwords, or API keys to GitHub.

### Email troubleshooting

| Symptom | Likely cause |
| ------- | ------------ |
| Authentication failed | Wrong SMTP login/key, or the API key was used instead of the SMTP key |
| Mail is accepted but never arrives | `MAIL_FROM` is not a verified sender; check Brevo transactional logs |
| Connection timeout | Outbound port blocked; keep using port `2525` |
| No email sent at all | `MAIL_ENABLED=false` |
| OTP only appears in logs | `MAIL_CONSOLE_FALLBACK=true` (development only) |

---

# 📱 SMS Expense Integration

ExpenseX includes a webhook endpoint designed for a future Android application that reads bank transaction SMS messages.

```http
POST /api/webhook/sms/{webhookToken}
```

```text
Bank SMS → Android App → SMS Parser → ExpenseX REST API
        → Spring Boot → PostgreSQL → Dashboard / Reports
```

This is the foundation for automatically detecting and recording bank transactions.

---

# 🛠️ Technology Stack

| Layer | Technologies |
| ----- | ------------ |
| Backend | Java 21, Spring Boot 3.5.4, Spring Web, Spring Security, Spring Data JPA, Hibernate, Spring Validation, Spring Mail, Thymeleaf |
| Database | PostgreSQL, PostgreSQL JDBC driver |
| Authentication | JWT (JJWT), refresh tokens, OTP verification |
| Email | Brevo SMTP, Spring Mail, STARTTLS |
| Libraries | Lombok, ModelMapper, Jackson Java 8 Date/Time, iText PDF, Apache POI |
| API docs | Springdoc OpenAPI, Swagger UI |
| DevOps | Docker, Docker Compose, Maven, Render |

---

# 🏗️ Application Architecture

```text
                    ┌──────────────────┐
                    │      User        │
                    └────────┬─────────┘
                             ▼
                    ┌──────────────────┐
                    │   Thymeleaf UI   │
                    └────────┬─────────┘
                             ▼
                    ┌──────────────────┐
                    │ Spring Security  │
                    │   JWT + OTP      │
                    └────────┬─────────┘
                             ▼
                    ┌──────────────────┐
                    │   Controllers    │
                    └────────┬─────────┘
                             ▼
                    ┌──────────────────┐
                    │    Services      │──────► Brevo SMTP (OTP / email)
                    └────────┬─────────┘
                             ▼
                    ┌──────────────────┐
                    │ Repository / JPA │
                    └────────┬─────────┘
                             ▼
                    ┌──────────────────┐
                    │   PostgreSQL     │
                    └──────────────────┘
```

---

# 📂 Project Structure

```text
ExpenseX/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/expensetracker/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── templates/
│   │       ├── static/
│   │       └── application.properties
│   └── test/
│
├── Dockerfile
├── docker-compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .env.example
├── .gitignore
└── README.md
```

---

# ⚙️ Requirements

- Java 21
- Maven (or the included Maven wrapper)
- PostgreSQL
- Git
- A Brevo account (free tier is enough) for email OTP
- Docker Desktop (optional)

```bash
java -version
mvn -version
psql --version
docker --version
```

---

# 🗄️ Database Setup

```sql
CREATE DATABASE expense_tracker;
```

| Configuration | Local | Docker |
| ------------- | ----- | ------ |
| Database | `expense_tracker` | `expense_tracker` |
| PostgreSQL host | `localhost` | `postgres` |
| PostgreSQL port | `2004` | `5432` |
| Default user | `postgres` | `postgres` |

```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
```

Hibernate creates/updates tables automatically during development. For production, consider `validate` with a migration tool such as Flyway or Liquibase. Hibernate 6 detects the PostgreSQL dialect automatically, so `spring.jpa.database-platform` is no longer needed.

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

# 🔑 Environment Configuration

ExpenseX keeps sensitive configuration in environment variables.

## 1. Create `.env`

Copy `.env.example` to `.env` in the project root (next to `pom.xml`).

Windows:

```cmd
copy .env.example .env
```

Linux / macOS:

```bash
cp .env.example .env
```

## 2. Fill in the values

```properties
# PostgreSQL
DB_URL=jdbc:postgresql://localhost:2004/expense_tracker
DB_USERNAME=postgres
DB_PASSWORD=YOUR_DATABASE_PASSWORD

# JWT
JWT_SECRET=YOUR_LONG_RANDOM_JWT_SECRET
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Brevo SMTP
BREVO_SMTP_LOGIN=YOUR_BREVO_SMTP_LOGIN
BREVO_SMTP_KEY=YOUR_BREVO_SMTP_KEY
MAIL_FROM=YOUR_VERIFIED_SENDER_EMAIL

# Email settings
MAIL_ENABLED=true
MAIL_CONSOLE_FALLBACK=false

# Application settings
JPA_SHOW_SQL=false
THYMELEAF_CACHE=true

# FRED Financial Data API (optional)
FINANCIAL_API_KEY=YOUR_FRED_API_KEY
```

| Variable | Purpose |
| -------- | ------- |
| `BREVO_SMTP_LOGIN` | Your Brevo SMTP login |
| `BREVO_SMTP_KEY` | Your generated Brevo SMTP key |
| `MAIL_FROM` | Your verified sender email address |
| `MAIL_ENABLED` | `true` to send real emails |
| `MAIL_CONSOLE_FALLBACK` | `true` prints OTPs to the console for local development only; keep `false` in production |
| `FINANCIAL_API_KEY` | Optional FRED API key for financial data |

> Generate a strong JWT secret, for example: `openssl rand -base64 64`

## 3. Never commit

```text
.env
Database passwords
JWT secrets
Brevo SMTP keys
API keys
Webhook tokens
Production credentials
```

If a secret has already been pushed to GitHub, rotate it immediately.

---

# ▶️ Run Locally

## 1. Clone

```bash
git clone https://github.com/Lokesh-github07/ExpenseX.git
cd ExpenseX
```

## 2. Create the database

```sql
CREATE DATABASE expense_tracker;
```

The default local configuration expects PostgreSQL on port `2004`.

## 3. Configure environment

Copy `.env.example` to `.env` and set your PostgreSQL credentials, JWT secret, Brevo SMTP credentials, verified sender email and (optionally) the financial API key.

The application imports the root `.env` file with:

```properties
spring.config.import=optional:file:./.env[.properties]
```

Real process environment variables take precedence over `.env` values.

## 4. Build

```bash
mvn clean install
```

Windows: `mvnw.cmd clean install`

## 5. Start

```bash
mvn spring-boot:run
```

Windows: `mvnw.cmd spring-boot:run`

Open `http://localhost:8080`.

## IntelliJ IDEA

1. **Run → Edit Configurations**.
2. Select your Spring Boot application.
3. Add the variables from `.env` (including the Brevo ones).
4. Set the working directory to the project root.
5. Apply and run.

Restart the application after changing environment variables.

---

# 🌐 Web Pages

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

Pages behind login are guarded client-side; the data behind them is protected by JWT on `/api/**`.

---

# 🐳 Docker

```text
┌──────────────────────┐
│  expense-tracker-app │
│        :8080         │
└──────────┬───────────┘
           ▼
┌──────────────────────────┐
│ expense-tracker-postgres │
│          :5432           │
└──────────────────────────┘
```

PostgreSQL is exposed on local port `2004`, while the application reaches it internally on `5432`.

### Environment passed by `docker-compose.yaml`

```yaml
environment:
  DB_URL: jdbc:postgresql://postgres:5432/expense_tracker
  DB_USERNAME: postgres
  DB_PASSWORD: ${DB_PASSWORD}

  JWT_SECRET: ${JWT_SECRET}
  JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
  JWT_REFRESH_EXPIRATION: ${JWT_REFRESH_EXPIRATION:-604800000}

  BREVO_SMTP_LOGIN: ${BREVO_SMTP_LOGIN}
  BREVO_SMTP_KEY: ${BREVO_SMTP_KEY}
  MAIL_FROM: ${MAIL_FROM}

  MAIL_ENABLED: ${MAIL_ENABLED:-true}
  MAIL_CONSOLE_FALLBACK: ${MAIL_CONSOLE_FALLBACK:-false}
```

> `FINANCIAL_API_KEY`, `JPA_SHOW_SQL` and `THYMELEAF_CACHE` are not passed by default. Add them to the `environment:` block if you need them in Docker.

### Commands

```bash
docker compose up --build          # build and start
docker compose up --build -d       # run in background
docker ps                          # list containers
docker compose logs -f app         # application logs
docker compose logs -f postgres    # database logs
docker compose down                # stop
docker compose up -d --build --force-recreate app   # apply config changes
docker compose down -v             # stop and delete database volume
```

> ⚠️ `docker compose down -v` deletes the PostgreSQL volume and all stored data.

---

# ☁️ Deployment – Render

ExpenseX can be deployed with **Render Web Services, Render PostgreSQL, and Brevo SMTP**.

```text
             User
               │
               ▼
        Render Web Service
          (Spring Boot)
               │
       ┌───────┴────────┐
       ▼                ▼
  PostgreSQL         Brevo SMTP
   Database          Email / OTP
```

## 1. Create the PostgreSQL database

1. Sign in to [Render](https://render.com/) and create a PostgreSQL database.
2. Copy the connection details.
3. Use the **Internal Database URL** when the web service is in the same region.

## 2. Create the web service

1. Connect your GitHub repository and select ExpenseX.
2. Create a Web Service.

**Build command**

```bash
./mvnw clean package -DskipTests
```

**Start command**

```bash
java -jar target/expense-tracker-0.0.1-SNAPSHOT.jar
```

Use the actual JAR name generated by Maven.

## 3. Environment variables

**Render Dashboard → ExpenseX Web Service → Environment**

| Variable | Value |
| -------- | ----- |
| `DB_URL` | Render PostgreSQL JDBC URL |
| `DB_USERNAME` | Render PostgreSQL username |
| `DB_PASSWORD` | Render PostgreSQL password |
| `JWT_SECRET` | Strong production secret |
| `JWT_EXPIRATION` | `86400000` |
| `JWT_REFRESH_EXPIRATION` | `604800000` |
| `BREVO_SMTP_LOGIN` | Brevo SMTP login |
| `BREVO_SMTP_KEY` | Brevo SMTP key |
| `MAIL_FROM` | Verified Brevo sender email |
| `MAIL_ENABLED` | `true` |
| `MAIL_CONSOLE_FALLBACK` | `false` |
| `JPA_SHOW_SQL` | `false` |
| `THYMELEAF_CACHE` | `true` |
| `FINANCIAL_API_KEY` | _(optional)_ FRED API key |

Render provides `PORT` automatically:

```properties
server.port=${PORT:8080}
```

> Render's free tier may block outbound SMTP on ports 25, 465 and 587. Brevo's port `2525` is the recommended choice here.

## 4. Verify

1. Check the Render logs and confirm the app starts.
2. Confirm PostgreSQL connectivity.
3. Test registration OTP and login OTP.
4. Check **Brevo → Transactional → Logs** for delivery status.

> A successful deploy does not guarantee email delivery. The app must connect to Brevo, authenticate and submit the message; check the Brevo logs if an email does not arrive.

---

# 📚 API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

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

### Webhook

```text
POST /api/webhook/sms/{webhookToken}
```

---

# 🧪 Testing

Tools: JUnit, Mockito, Postman, Swagger UI, Selenium, Docker.

```text
Unit → Integration → API → Manual → UI Automation → Docker → Deployment
```

Tip: while developing, set `MAIL_CONSOLE_FALLBACK=true` to read OTPs from the console, and switch it back to `false` before deploying.

---

# 📦 Build JAR

```bash
mvn clean package -DskipTests
java -jar target/expense-tracker-0.0.1-SNAPSHOT.jar
```

---

# 🔒 Security

ExpenseX uses Spring Security, JWT with refresh tokens, OTP verification, password reset, account activation/deactivation, authenticated SMTP (Brevo) and environment-based secrets.

### Production recommendations

- Use HTTPS.
- Use strong, unique JWT secrets.
- Secure database credentials and keep regular backups.
- Configure restrictive CORS.
- Protect and rotate webhook tokens.
- Disable unnecessary debug logging.
- Keep `.env` out of Git and never commit secrets.
- Keep `MAIL_CONSOLE_FALLBACK=false` in production.
- Never print OTPs or SMTP credentials in production logs.
- Use a verified Brevo sender address (ideally an authenticated domain with SPF/DKIM).
- Consider adding server-side route protection and token revocation (see the status table).

---

# 🔮 Future Improvements

- 📱 Android application
- 🏦 Automatic bank SMS transaction detection
- 💳 UPI transaction detection
- 🔔 Push notifications
- 🔄 Recurring expenses
- 🎯 Server-side savings goals, bills, loans/EMIs and income (currently browser-only)
- 🔌 Connect the Budget page to the existing budget REST API
- 🚪 Server-side token revocation ("sign out everywhere")
- 📈 Investment tracking
- 💱 Multiple currencies
- 🤖 AI Finance Assistant and AI-powered expense categorization
- 🔄 CI/CD pipeline
- 🧪 Automated integration and API testing
- 📋 Jira QA workflow
- 👨‍💼 Admin dashboard

---

# 📊 Project Highlights

```text
Java 21 · Spring Boot · Spring Security · JWT · REST APIs
PostgreSQL · JPA / Hibernate · Thymeleaf
OTP Authentication · Brevo SMTP Integration
PDF & Excel Reports · SMS Webhook
Docker · Docker Compose · Render Deployment
Swagger / OpenAPI · API Testing
```

---

# 👨‍💻 Author

**Lokesh Pande**

Java Full Stack Developer

Spring Boot • REST APIs • PostgreSQL • Docker • Testing

🔗 GitHub: [https://github.com/Lokesh-github07](https://github.com/Lokesh-github07)

---

## ⭐ Project

If you find this project useful, consider giving it a ⭐ on GitHub.

## 📜 License

This project is intended for educational, portfolio, and development purposes.