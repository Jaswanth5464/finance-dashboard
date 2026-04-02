# Finance Dashboard Backend

> Role-based access control API for managing financial records and dashboard analytics.

---

## 🔗 Live Links

| | Link |
|---|---|
| 🟢 **Swagger UI** | https://finance-dashboard-p6h7.onrender.com/swagger-ui.html |
| 📦 **GitHub Repo** | https://github.com/Jaswanth5464/finance-dashboard |
| 🌐 **Base API URL** | https://finance-dashboard-p6h7.onrender.com |

> **Note:** Hosted on Render free tier. First request after inactivity may take 30 seconds to wake up.

---

## ⚡ Quick Start

No setup needed. The API is live and pre-loaded with test data.

**1. Open Swagger UI:**
```
https://finance-dashboard-p6h7.onrender.com/swagger-ui.html
```

**2. Login — click `POST /api/auth/login` → Try it out → paste this:**
```json
{ "email": "admin@finance.com", "password": "admin123" }
```

**3. Copy the `token` from the response → click 🔒 Authorize (top right) → paste it**

**4. Now test any endpoint:**
- `POST /api/records` — create a record (ADMIN only)
- `GET /api/dashboard/summary` — analytics (ADMIN / ANALYST)
- Login as `viewer@finance.com / viewer123` → try dashboard → get 403 ✅

---

## 🚀 Key Highlights

- **Role-based access control** — ADMIN, ANALYST, VIEWER with different permissions enforced at method level
- **JWT stateless authentication** — no sessions, token carries identity
- **Advanced dashboard analytics** — income totals, expense totals, net balance, category breakdown, monthly trends
- **Filtering + pagination** — filter records by type, category, date range, keyword; paginated with sort control
- **Soft delete** — records are never physically removed, preserving audit history
- **Rate limiting** — 10 requests/minute per IP using Bucket4j
- **Clean layered architecture** — Controller → Service → Repository → Database, one responsibility per layer
- **Fully documented** — Swagger UI auto-generated, all endpoints testable from browser

---

## 🧠 Design Decision

This project focuses on **clarity, maintainability, and real-world backend practices** rather than over-engineering. Every design choice — from `BigDecimal` for money to soft delete for records — has a deliberate reason, documented in the Architecture section below.

```
Client Request
     │
     ▼
 Controller        ← Handles HTTP only (no business logic)
     │
     ▼
  Service          ← All business rules and data processing
     │
     ▼
 Repository        ← Database queries only
     │
     ▼
  Database         ← MySQL on Aiven (SSL encrypted)
```

---

## 🛠 Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.13 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Database (local) | MySQL 8 |
| Database (production) | **Aiven MySQL** (free cloud-hosted MySQL) |
| ORM | Spring Data JPA + Hibernate |
| Utilities | Lombok |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Rate Limiting | Bucket4j |
| Testing | JUnit 5 + Mockito |
| App Hosting | Render (Docker, free plan) |
| DB Hosting | Aiven (free plan, no credit card) |

---

## ☁️ Deployment Stack

The live API runs on two free cloud services:

| Service | What it hosts | Free plan |
|---|---|---|
| **Render** | Spring Boot application (via Docker) | ✅ No credit card |
| **Aiven** | MySQL 8 database | ✅ No credit card |

**How it connects:**
- The app on Render reads 5 environment variables: `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`
- Aiven requires **SSL** for all connections — the JDBC URL uses `ssl-mode=REQUIRED`
- The Docker image is built from the included `Dockerfile` using a multi-stage build (Maven build stage + lightweight JRE runtime stage)

---

## 🧠 Technology Choices

### Why Java + Spring Boot?

I chose Java with Spring Boot because it provides a strong foundation for building scalable and maintainable backend systems.

- **Structured Architecture** — Spring Boot naturally encourages a layered design (Controller → Service → Repository), which keeps concerns cleanly separated and the codebase easy to navigate.
- **Built-in Security** — Spring Security with JWT makes role-based access control straightforward to implement and reason about.
- **Production Readiness** — Spring Boot is widely used in enterprise-grade systems. Choosing it shows I understand what real-world backend development looks like.
- **Rich Ecosystem** — Spring Data JPA removes boilerplate database code, Bucket4j plugs in as middleware for rate limiting, and SpringDoc generates Swagger UI automatically. Each tool solved a real problem without reinventing the wheel.
- **MySQL for Persistence** — Relational data with clear relationships (User → FinancialRecord) is a natural fit for SQL. MySQL is battle-tested, free, and well-supported by JPA/Hibernate.

For this assignment, Spring Boot let me focus entirely on business logic, access control, and data design — instead of spending time on low-level plumbing.

---

## 🏗 Architecture and Design Principles

### Layered Architecture
The project follows a strict **Controller → Service → Repository → Database** pattern.

- **Controller** — Handles HTTP requests and responses only. No business logic here.
- **Service** — Contains all business rules (who can do what, how data is processed).
- **Repository** — Talks to the database. Only SQL/JPA queries live here.
- **Database** — MySQL stores the actual data.

This separation means each class has exactly one reason to change. If the database changes, only the Repository changes. If a business rule changes, only the Service changes.

### DTO Pattern
Entities (like `User`, `FinancialRecord`) are never sent directly to the client. Instead, **Data Transfer Objects** (DTOs) like `RecordResponse` are used.

This matters because:
- You control exactly what fields get exposed (no leaking password hashes)
- The response format stays stable even if the database model changes

The `RecordResponse.from(record)` static method handles the Entity → DTO conversion cleanly.

### Soft Delete
Records are never physically deleted from the database. Instead, a `deleted` boolean flag is set to `true`.

Why? Financial data is sensitive. If a record is accidentally deleted, you want to be able to recover it. All queries include a `WHERE deleted = false` filter so soft-deleted records are invisible to users but still exist in the database.

### BigDecimal for Money
All financial amounts use `BigDecimal`, not `float` or `double`.

Float and double store numbers in binary format which causes rounding errors. For example, `0.1 + 0.2` in float gives `0.30000000000000004`. For money, even a single paisa error is unacceptable. `BigDecimal` is mathematically exact.

### @PreAuthorize Role Enforcement
Every protected endpoint uses `@PreAuthorize("hasRole('ADMIN')")` or similar.

This annotation runs **before** the method executes. If the user's role does not match, Spring Security immediately returns a 403 response without even entering the method body.

### JWT Stateless Authentication
No sessions or cookies are used. When a user logs in, the server creates a signed JWT token containing the user's email. The client sends this token in every request header.

The server verifies the signature to confirm the token is valid and reads the email from it. This is stateless — the server stores nothing between requests, which makes it easy to scale horizontally.

### Transactional Write Operations
Write operations (create, update, delete) are annotated with `@Transactional`. If any step fails midway, the entire operation is rolled back automatically. This prevents partial writes that would leave the database in an inconsistent state.

### Generic ApiResponse Wrapper
Every endpoint returns the same structure:
```json
{
  "success": true,
  "message": "Record created successfully",
  "data": { ... }
}
```
This makes it predictable for any frontend to consume. The `ApiResponse<T>` generic class handles all types with one implementation.

### Custom Exceptions
Instead of letting Spring return generic 500 errors, specific exceptions like `ResourceNotFoundException` are thrown. A `GlobalExceptionHandler` catches these and returns the correct HTTP status code with a clear message — 404 for not found, 400 for bad input, 403 for wrong role, etc.

---

## 👥 Role Permissions

| Action | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| View financial records | ✅ | ✅ | ✅ |
| Get record by ID | ✅ | ✅ | ✅ |
| View dashboard summary | ❌ | ✅ | ✅ |
| Create financial records | ❌ | ❌ | ✅ |
| Update financial records | ❌ | ❌ | ✅ |
| Delete financial records | ❌ | ❌ | ✅ |
| View all users | ❌ | ❌ | ✅ |
| Toggle user active/inactive | ❌ | ❌ | ✅ |

---

## 📡 API Endpoints

### Authentication — Public (no token required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user and receive a JWT token |
| POST | `/api/auth/login` | Login and receive a JWT token |

---

### Financial Records — JWT Required

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/records` | ALL | Get paginated list of records with optional filters |
| POST | `/api/records` | ADMIN | Create a new financial record |
| GET | `/api/records/{id}` | ALL | Get a single record by its ID |
| PUT | `/api/records/{id}` | ADMIN | Update an existing record |
| DELETE | `/api/records/{id}` | ADMIN | Soft delete a record |

**Filter and pagination parameters for `GET /api/records`:**

| Parameter | Type | Example | Description |
|---|---|---|---|
| `type` | enum | `INCOME` or `EXPENSE` | Filter by record type |
| `category` | enum | `SALARY`, `FOOD` | Filter by category |
| `startDate` | date | `2024-01-01` | Records from this date |
| `endDate` | date | `2024-03-31` | Records up to this date |
| `keyword` | string | `salary` | Search keyword in notes |
| `page` | int | `0` | Page number (0-indexed) |
| `size` | int | `10` | Records per page |
| `sortBy` | string | `date`, `amount` | Field to sort by |
| `sortDir` | string | `asc`, `desc` | Sort direction |

---

### Dashboard Summary — JWT Required

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary` | ADMIN, ANALYST | Returns full analytics summary |

**Response includes:**
- Total income, total expenses, net balance
- Category-wise totals (e.g., SALARY: 255000, FOOD: 3200)
- Monthly income trends
- Last 5 recent records
- Total, income, and expense record counts

---

### User Management — JWT Required

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/users` | ADMIN | Get list of all users |
| PATCH | `/api/users/{id}/status` | ADMIN | Toggle user active/inactive |

---

### System

| Method | Endpoint | Description |
|---|---|---|
| GET | `/actuator/health` | Returns `{"status":"UP"}` for health checks |

---

## 🚀 How to Run Locally

### Prerequisites
- Java 17+
- MySQL 8+
- Maven (or use the included `mvnw` wrapper)

### Step 1 — Create the database
```sql
CREATE DATABASE finance_db;
```

### Step 2 — Clone the repo
```bash
git clone https://github.com/Jaswanth5464/finance-dashboard.git
cd finance-dashboard
```

### Step 3 — Configure application.properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finance_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE
```

### Step 4 — Run
```bash
./mvnw spring-boot:run
```

### Step 5 — Open Swagger
```
http://localhost:8080/swagger-ui.html
```

Tables are created automatically by Hibernate. Sample data (3 users + 10 records) is seeded on first run.

---

## 🔑 Test Credentials (Pre-seeded)

These accounts are automatically created when the app starts for the first time.

| Email | Password | Role | What they can do |
|---|---|---|---|
| `admin@finance.com` | `admin123` | ADMIN | Full access — create, update, delete, manage users |
| `analyst@finance.com` | `analyst123` | ANALYST | View records + dashboard analytics |
| `viewer@finance.com` | `viewer123` | VIEWER | Read-only access to records |

---

## 📋 Sample Request Bodies

### Register a new user
```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secure123",
  "role": "ANALYST"
}
```

### Login
```json
POST /api/auth/login
{
  "email": "admin@finance.com",
  "password": "admin123"
}
```

### Create a financial record (ADMIN only)
```json
POST /api/records
Authorization: Bearer <your_token_here>

{
  "amount": 50000.00,
  "type": "INCOME",
  "category": "SALARY",
  "date": "2024-04-01",
  "notes": "April salary credit"
}
```

---

## ✅ Validation Rules

| Field | Rule |
|---|---|
| `amount` | Required. Must be greater than 0. |
| `type` | Required. Must be `INCOME` or `EXPENSE`. |
| `category` | Required. Must be one of the valid enum values. |
| `date` | Required. Cannot be a future date. |
| `notes` | Optional. Maximum 500 characters. |
| `email` | Required. Must be valid email format. Unique per user. |
| `password` | Required. Minimum 6 characters. |
| `role` | Required on register. Must be `ADMIN`, `ANALYST`, or `VIEWER`. |

---

## ⚠️ Error Handling

| HTTP Status | When it happens | Example |
|---|---|---|
| `400 Bad Request` | Invalid input or validation failure | Amount is negative, missing required field |
| `401 Unauthorized` | No token or token is expired | Calling `/api/records` without logging in |
| `403 Forbidden` | Valid token but wrong role | VIEWER trying to create a record |
| `404 Not Found` | Resource does not exist | `GET /api/records/999` with no such record |
| `429 Too Many Requests` | Rate limit exceeded (10/min per IP) | Sending requests too fast |
| `500 Internal Server Error` | Unexpected server-side error | Logged internally, generic message returned to client |

All errors follow the same response format:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "amount": "must be greater than 0"
  }
}
```

---

## ⭐ Optional Features Implemented

- [x] JWT Authentication (register + login flow)
- [x] Pagination (page, size, sortBy, sortDir)
- [x] Keyword Search (filter records by notes content)
- [x] Date Range Filtering
- [x] Soft Delete (data preserved, not physically removed)
- [x] Rate Limiting (10 requests/minute per IP — Bucket4j)
- [x] Unit Tests (7 tests — UserService + FinancialRecordService)
- [x] API Documentation (Swagger UI — auto-generated)
- [x] Sample Data Seeder (runs on first startup only)
- [x] Docker deployment

---

## 💭 Assumptions Made

1. **ANALYST can view dashboard but cannot modify records.** The assignment said analysts access insights, so dashboard summary is allowed but write access is not.
2. **VIEWER is fully read-only.** They can list and view records but the dashboard summary is considered an analytics privilege, so it is restricted to ANALYST and ADMIN.
3. **Soft delete only — no hard delete.** Financial records should never be permanently lost. This matches how real finance systems work (audit trail requirement).
4. **Same error message for wrong email and wrong password.** Returning different messages for each would let an attacker know whether an email exists in the system. The endpoint intentionally says "Invalid email or password" for both cases.
5. **Date cannot be in the future.** A financial record can only be created for a past or current date. A future income is not yet realized.
6. **Monthly trends show INCOME only.** The dashboard trends chart is intended for tracking earning patterns. Expense trends can be added as a separate field if needed.

---

## 🔄 Tradeoffs and What I Would Improve

1. **Rate limiting is in-memory (per instance).** The current Bucket4j setup resets when the server restarts and does not work correctly if multiple instances run in parallel. For production, a Redis-based distributed rate limiter would be the right choice.

2. **Dashboard queries run on every request.** The `/api/dashboard/summary` endpoint executes aggregation queries against the database every single time it is called. At scale, this would be slow. Caching the result in Redis for 60 seconds (or invalidating on record write) would eliminate most of the load.

3. **No refresh token mechanism.** The current JWT expires after 24 hours and the user must log in again. A refresh token pattern (short-lived access token + long-lived refresh token) would provide a better user experience without sacrificing security.

4. **Pagination defaults to 10 records per page.** This is a reasonable default for a dashboard but should be configurable per client need. A maximum cap (e.g., 100) should also be enforced to prevent clients from requesting thousands of records in one call.

---

## 📂 Project Structure

```
src/main/java/com/finance/dashboard/
├── config/
│   ├── DataSeeder.java         # Seeds 3 users + 10 records on first run
│   ├── RateLimitConfig.java    # Bucket4j rate limit configuration
│   └── SwaggerConfig.java      # OpenAPI / Swagger setup
├── controller/
│   ├── AuthController.java     # POST /api/auth/register, /login
│   ├── FinancialRecordController.java  # CRUD for /api/records
│   ├── DashboardController.java        # GET /api/dashboard/summary
│   └── UserController.java     # GET /api/users, PATCH status
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── RecordRequest.java
│   ├── RecordResponse.java     # Includes RecordResponse.from(entity)
│   ├── AuthResponse.java
│   ├── DashboardSummaryResponse.java
│   └── ApiResponse.java        # Generic wrapper for all responses
├── exception/
│   ├── GlobalExceptionHandler.java     # Maps exceptions to HTTP responses
│   ├── ResourceNotFoundException.java  # Throws 404
│   └── AccessDeniedException.java
├── model/
│   ├── User.java
│   ├── FinancialRecord.java
│   ├── Role.java               # Enum: ADMIN, ANALYST, VIEWER
│   ├── RecordType.java         # Enum: INCOME, EXPENSE
│   └── Category.java           # Enum: SALARY, FOOD, TRANSPORT, etc.
├── repository/
│   ├── UserRepository.java
│   └── FinancialRecordRepository.java  # Custom JPQL queries for filters + aggregation
└── security/
    ├── JwtUtil.java            # Generate + validate JWT tokens
    ├── JwtFilter.java          # Reads token from header, sets auth context
    ├── RateLimitFilter.java    # Enforces request rate limit per IP
    └── SecurityConfig.java     # Defines which endpoints are public vs protected
```

---

## 👤 Author

**Jaswanth Kanamrlapudi**

| | |
|---|---|
| 📧 **Email** | [jaswanth5464@gmail.com](mailto:jaswanth5464@gmail.com) |
| 💼 **LinkedIn** | [linkedin.com/in/jaswanth-kanamrlapudi-a41197252](https://www.linkedin.com/in/jaswanth-kanamrlapudi-a41197252) |
| 🐙 **GitHub** | [github.com/Jaswanth5464](https://github.com/Jaswanth5464) |

---

*Built as part of the Finance Data Processing and Access Control Backend — Backend Developer Intern Assignment.*
