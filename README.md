# Job Tracker — Backend API

REST API for tracking job applications, with AI-powered matching between your resume and each job description.

Upload your resume (PDF) once. Every application you create with a job description is then analyzed by Google Gemini, which returns a **match score (0–100)** and up to **5 missing key skills**.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Web, Data JPA, Security, Validation) |
| Database | PostgreSQL |
| AI | Spring AI 1.1 + Google Gemini |
| Auth | JWT (jjwt 0.12) via HttpOnly cookie or `Bearer` header |
| PDF parsing | Apache Tika 2.9 |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper included) |

## Getting started

### Prerequisites

- JDK 21
- A PostgreSQL instance
- A [Google Gemini API key](https://aistudio.google.com/apikey)

### 1. Start PostgreSQL

The quickest way is with Docker:

```bash
docker run -d --name job-db \
  -e POSTGRES_DB=job_db -e POSTGRES_USER=job_user -e POSTGRES_PASSWORD=change-me \
  -p 5434:5432 postgres:17
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Then fill in `.env`. Git ignores this file. Real environment variables take precedence over it.

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | JDBC URL of the database | — |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials | — |
| `GEMINI_API_KEY` | Google Gemini API key | — |
| `GEMINI_MODEL` | Gemini model to use | `gemini-3.8-flash` |
| `JWT_SECRET_KEY` | Base64 key of at least 256 bits (`openssl rand -base64 32`) | — |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds | `86400000` (24 h) |
| `JWT_COOKIE_SECURE` | Mark the auth cookie `Secure` (must be `true` in production) | `false` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | `http://localhost:3000` |

> ⚠️ Never commit `.env` or real secrets. Only `.env.example` belongs in the repository.

### 3. Run the API

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`. Hibernate creates the database schema automatically.

Swagger UI: **http://localhost:8080/swagger-ui/index.html**

### 4. Run the tests

```bash
./mvnw test
```

## Authentication

1. Create an account with `POST /api/users`.
2. Log in with `POST /api/auth/login`. The response sets an **HttpOnly** `jwt_token` cookie, which the browser sends automatically. This is the flow for the Next.js frontend.
3. Other clients can send the token in an `Authorization: Bearer <token>` header instead.
4. `POST /api/auth/logout` clears the cookie.

## API endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/users` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Log in and receive the JWT cookie |
| `POST` | `/api/auth/logout` | Public | Clear the JWT cookie |
| `GET` | `/api/users/me` | ✅ | Get the current user's profile |
| `POST` | `/api/users/me/resume` | ✅ | Upload a resume (multipart `file`) and extract its text |
| `GET` | `/api/applications` | ✅ | List the current user's applications |
| `POST` | `/api/applications` | ✅ | Create an application (runs the AI analysis when `jobDescription` is set) |
| `GET` | `/api/applications/{id}` | ✅ | Get one application |
| `PUT` | `/api/applications/{id}` | ✅ | Update an application |
| `PATCH` | `/api/applications/{id}/status` | ✅ | Update the status only |
| `DELETE` | `/api/applications/{id}` | ✅ | Delete an application |

Users can only access their own applications.

### Example: create an application

```http
POST /api/applications
Content-Type: application/json

{
  "companyName": "Acme",
  "jobTitle": "Backend Developer",
  "status": "APPLIED",
  "jobDescription": "We are looking for a Java / Spring Boot developer..."
}
```

The response includes `resumeMatchScore` and `missingSkills`, both computed by Gemini from your uploaded resume.

## Project structure

```
src/main/java/com/portfolio/jobtracker/
├── config/       # Security, JWT filter, CORS, Swagger
├── controller/   # REST controllers + global exception handler
├── dto/          # Request/response records with validation
├── entity/       # JPA entities (Users, JobApplication)
├── exception/    # Custom exceptions
├── repository/   # Spring Data JPA repositories
└── service/      # Business logic, JWT, PDF parsing, AI analysis
```
