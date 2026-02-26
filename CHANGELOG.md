# Journal des modifications

Toutes les modifications notables de MasterAnnonce sont documentées dans ce fichier.

Format basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/).

## [1.3.0] — 2026-02-25

### Ajouté
- **SonarQube** : dockerisation complète (LTS Community + PostgreSQL dédié) — `docker-compose.yml`
- **`sonar-project.properties`** : configuration projet avec import JaCoCo XML
- **`sonar-maven-plugin`** (4.0.0) dans `pom.xml`
- **Collection Postman** : `MasterAnnonce.postman_collection.json` (20 requêtes + auto-token)
- **Profil dev** : `application-dev.yml` (SQL verbose, TRACE params, DEBUG security)
- **Swagger `@ApiResponse`** : codes HTTP documentés sur les 14 endpoints (3 controllers)
- **`AuthServiceTest`** : 7 tests unitaires Mockito (login, register, refresh)

### Corrigé
- **JWT secret** dans `docker-compose.yml` aligné sur la clé 528 bits
- **JaCoCo** : génération rapport XML en plus du HTML (requis par SonarQube)

## [1.2.0] — 2026-02-25

### Ajouté
- **V3 seed** : 20 annonces réalistes (Immobilier, Automobile, Emploi, Électronique, Services) + 3 utilisateurs
- **Traduction française** : tous les commentaires, Javadoc, `@DisplayName` et messages traduits en français

### Corrigé
- **Secret JWT** : clé passée de 488 à 704 bits (conformité HS512 — RFC 7518)
- **`data.sql` obsolète** supprimé (Flyway gère le seeding via `V2` et `V3`)

## [1.1.0] — 2026-02-25

### Ajouté
- **Flyway** : migrations de schéma (`V1__init_schema.sql`, `V2__seed_data.sql`) — remplace `ddl-auto: update`
- **CORS** pour intégration frontend (`WebConfig.java`)
- **Rate limiting** sur `/api/v1/auth/login` — 5 tentatives/min par IP (`RateLimitFilter.java`)
- **Robustesse du mot de passe** : min 8 caractères, 1 majuscule, 1 chiffre
- **Profil production** (`application-prod.yml`) avec `ddl-auto: validate`
- **Versionnement API** — tous les endpoints sous `/api/v1/`
- **`PageResponse<T>`** — DTO de réponse paginée sans dépendance framework
- **Prometheus** — `/actuator/prometheus` via Micrometer
- **HATEOAS** — liens hypermedia (`_links`) sur les réponses unitaires d'annonces
- **Checkstyle** Google Java Style
- **JaCoCo** couverture ≥ 50% obligatoire
- **5 classes de tests** : `JwtServiceTest`, `UserServiceTest`, `CategoryServiceTest`, `GlobalExceptionHandlerTest`, `AnnonceServiceTest`
- **CHANGELOG.md**

### Modifié
- `application.yml` : `ddl-auto: update` → `validate`, Flyway activé
- `RegisterRequest` : longueur min du mot de passe 4 → 8, ajout de `@Pattern` regex
- `SecurityConfig` : patterns d'URL mis à jour vers `/api/v1/`
- Tous les contrôleurs : `@RequestMapping` de `/api/` vers `/api/v1/`
- `AnnonceController.listAnnonces()` retourne `PageResponse<AnnonceDTO>` au lieu de `Page<AnnonceDTO>`
- `AnnonceController` — réponses unitaires enrichies avec `HateoasResponse<AnnonceDTO>`
- `springdoc-openapi` mis à jour de 2.8.6 vers **2.8.15** (compatibilité Spring Boot 3.5.5)

### Sécurité
- Protection brute-force via rate limiting sur le login
- CORS correctement configuré (pas de wildcard)
- Exigences de mot de passe fort imposées au niveau validation

## [1.0.0] — 2026-02-24

### Ajouté
- Migration initiale Spring Boot 3.5.5 depuis JAX-RS/Jersey
- Authentification JWT (tokens d'accès + de rafraîchissement)
- Spring Security stateless avec RBAC
- Spring Data JPA avec Specifications pour recherche multi-critères
- Mapper MapStruct pour Annonce et Category
- Logging AOP avec Correlation ID
- Modèle de domaine riche avec méthodes métier (cycle de vie des statuts)
- Tests d'intégration Testcontainers
- SpringDoc OpenAPI (Swagger UI)
- Spring Boot Actuator (health/info)
- Dockerfile + docker-compose
- GitHub Actions CI (matrice Java 17 + 21)
- Manifests Kubernetes (8 fichiers YAML)
