# Aegis Sentinel Backend

Enterprise Cloud Incident Detection & Response Platform Backend Service built with Java 21, Spring Boot 4, Spring Security, PostgreSQL, Redis, and Apache Kafka.

---

## Technical Features & Domains

- **Identity & Access Management (IAM)**: Organization & Workspace multi-tenancy, JWT access token authentication, refresh token rotation, Google OAuth2 federation.
- **RBAC & Fine-Grained Authorization**: Role-based access control and method-level tenant permission evaluation.
- **Security Audit Logging**: Comprehensive security audit trail logging.
- **OpenAPI / Swagger Documentation**: Interactive API testing and schemas via SpringDoc OpenAPI.

---

## Quick Documentation Links

- [Developer Setup Guide](docs/developer-setup.md)
- [API Architecture & Design Specification](docs/api-architecture.md)
- [Postman v2.1 Collection](docs/postman/aegis-sentinel-v1.postman_collection.json)
- [HTTP Client Request Suite](docs/aegis-sentinel.http)

---

## Interactive API Documentation

When running locally (`./gradlew bootRun`):
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Quickstart Commands

```bash
# 1. Start PostgreSQL, Redis, Kafka via Docker
docker compose up -d

# 2. Run backend application
./gradlew bootRun

# 3. Run automated tests
./gradlew test

# 4. Check & apply Spotless formatting
./gradlew spotlessApply
```
