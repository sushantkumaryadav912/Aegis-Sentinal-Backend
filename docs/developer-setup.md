# Developer Setup & Contribution Guide

Welcome to the **Aegis Sentinel Backend** developer guide. This document provides setup instructions, architectural guidelines, environment configurations, and testing protocols for local development.

---

## 1. Prerequisites

Ensure your development machine has the following tools installed:

- **JDK 21** (Eclipse Temurin or Amazon Corretto recommended)
- **Gradle 8.x** (Included via Gradle Wrapper `./gradlew`)
- **Docker** & **Docker Compose** (For running infrastructure services locally)
- **Git**

---

## 2. Infrastructure Services (Docker Compose)

Aegis Sentinel relies on PostgreSQL, Redis, and Apache Kafka for identity persistence, session token management, rate-limiting, and security audit events.

Start local infrastructure dependencies:

```bash
docker compose up -d
```

### Infrastructure Port Mapping:
- **PostgreSQL 16**: `localhost:5433` (`aegis` / `aegis_dev`)
- **Redis**: `localhost:6379`
- **Kafka**: `localhost:9092`

---

## 3. Environment Variables

The following environment variables can be configured in your local environment or `.env` file:

| Variable Name | Default / Sample Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring environment profile (`dev`, `prod`, `docker`) |
| `AEGIS_JWT_SECRET` | `dGhpcy1pcy1hLXZlcnktc2VjdXJlLWRldmVsb3BtZW50LWp3dC1zZWNyZXQta2V5LTMyLWJ5dGVzLW1pbg==` | Base64-encoded secret key for signing JWT tokens |
| `GOOGLE_CLIENT_ID` | `google-client-id-placeholder` | OAuth2 Google Client ID |
| `GOOGLE_CLIENT_SECRET` | `google-client-secret-placeholder` | OAuth2 Google Client Secret |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/aegis` | PostgreSQL database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `aegis` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `aegis_dev` | Database password |

---

## 4. Running the Application

### 4.1 Development Mode
Run the Spring Boot server using the Gradle wrapper:

```bash
./gradlew bootRun
```
The backend server will start on `http://localhost:8080`.

---

## 5. API Documentation & OpenAPI

Interactive API documentation and schema specifications are exposed via SpringDoc OpenAPI:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### API Test Collections:
- **Postman Collection**: [docs/postman/aegis-sentinel-v1.postman_collection.json](file:///home/sushant/Projects/btech_project/Aegis-Sentinel-Backend/docs/postman/aegis-sentinel-v1.postman_collection.json)
- **HTTP Client File**: [docs/aegis-sentinel.http](file:///home/sushant/Projects/btech_project/Aegis-Sentinel-Backend/docs/aegis-sentinel.http)

---

## 6. Testing & Code Quality

### 6.1 Running Test Suites
Execute the full automated test suite:

```bash
./gradlew test
```

### 6.2 Code Style & Formatting (Spotless)
Aegis Sentinel enforces Google Java Format via Spotless.

Check format compliance:
```bash
./gradlew spotlessCheck
```

Apply formatting fixes automatically:
```bash
./gradlew spotlessApply
```

### 6.3 Static Analysis (Checkstyle)
Verify static analysis and lint rules:

```bash
./gradlew checkstyleMain
```

### 6.4 Complete Build Verification
Run full clean build with tests and linters:

```bash
./gradlew clean build
```
