# Changelog

All notable changes to MasterAnnonce are documented in this file.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.1.0] — 2026-02-25

### Added
- **Flyway** schema migrations (`V1__init_schema.sql`, `V2__seed_data.sql`) — replaces `ddl-auto: update`
- **CORS** configuration for frontend integration (`WebConfig.java`)
- **Rate limiting** on `/api/v1/auth/login` — 5 attempts/min per IP (`RateLimitFilter.java`)
- **Password strength** validation: min 8 chars, 1 uppercase, 1 digit
- **Production profile** (`application-prod.yml`) with `ddl-auto: validate`
- **API versioning** — all endpoints under `/api/v1/`
- **`PageResponse<T>`** — framework-agnostic paginated response DTO
- **Checkstyle** Google Java Style integration
- **JaCoCo coverage enforcement** ≥ 50%
- **5 new test classes**: `JwtServiceTest`, `UserServiceTest`, `CategoryServiceTest`, `GlobalExceptionHandlerTest`, `AnnonceServiceTest`
- **CHANGELOG.md**

### Changed
- `application.yml`: `ddl-auto: update` → `validate`, Flyway enabled
- `RegisterRequest`: password min length 4 → 8, added `@Pattern` regex
- `SecurityConfig`: URL patterns updated to `/api/v1/`
- All controllers: `@RequestMapping` from `/api/` to `/api/v1/`
- `AnnonceController.listAnnonces()` returns `PageResponse<AnnonceDTO>` instead of `Page<AnnonceDTO>`

### Security
- Login brute-force protection via rate limiting
- CORS properly configured (no wildcard)
- Strong password requirements enforced at validation layer

## [1.0.0] — 2026-02-24

### Added
- Initial Spring Boot 3.5.5 migration from JAX-RS/Jersey
- JWT authentication (access + refresh tokens)
- Spring Security stateless with RBAC
- Spring Data JPA with Specifications for multi-criteria search
- MapStruct mapper for Annonce and Category
- AOP Logging with correlation ID
- Domain model with rich business methods (status lifecycle)
- Testcontainers integration tests
- SpringDoc OpenAPI (Swagger UI)
- Spring Boot Actuator (health/info)
- Dockerfile + docker-compose
- GitHub Actions CI (Java 17 + 21 matrix)
- Kubernetes manifests (8 YAML files)
