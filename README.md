# MasterAnnonce — Spring Boot API

API REST de gestion d'annonces, migrée de JAX-RS/Jersey vers **Spring Boot 3.5.5**.

## 🏗️ Architecture

Architecture en couches (Clean Architecture pragmatique) :

```
adapter/rest/           ← Controllers REST (@RestController)
application/dto/        ← DTOs (Java Records)
application/mapper/     ← MapStruct mappers
application/service/    ← Services métier (@Service, @Transactional)
domain/model/           ← Entités JPA + logique métier
domain/exception/       ← Exceptions domaine
infrastructure/
├── aop/               ← Logging AOP + Correlation ID
├── config/            ← OpenAPI + CORS config
├── persistence/       ← Spring Data JPA repositories + Specifications
└── security/          ← JWT, Spring Security, Rate limiting
```

## 🛠️ Technologies

| Technologie | Version | Usage |
|-------------|---------|-------|
| Spring Boot | 3.5.5 | Framework principal |
| Spring Data JPA | 3.5.x | Repositories + Specifications |
| Spring Security | 6.x | Authentification JWT stateless |
| Flyway | 10.x | Migrations de schéma DB |
| MapStruct | 1.6.3 | Mapping DTO ↔ Entity |
| jjwt | 0.12.6 | Génération/validation JWT |
| SpringDoc OpenAPI | 2.8.15 | Documentation Swagger |
| Micrometer + Prometheus | — | Monitoring & métriques |
| SonarQube | LTS Community | Analyse qualité de code |
| PostgreSQL | 16 | Base de données |
| JaCoCo | 0.8.12 | Couverture de code (≥ 50%) |
| Testcontainers | — | Tests d'intégration |
| Docker | Multi-stage | Containerisation |

## 🚀 Démarrage rapide

### Avec Docker Compose (recommandé)
```bash
docker-compose up -d
# L'API est accessible sur http://localhost:8080
# Swagger UI : http://localhost:8080/swagger-ui.html
```

### Sans Docker
```bash
# Prérequis : PostgreSQL sur localhost:5433, DB "MasterAnnonce"
mvn spring-boot:run
```

### Utilisateurs de test
| Username | Password | Rôle |
|----------|----------|------|
| `admin` | `Password1` | ROLE_ADMIN |
| `user1` | `Password1` | ROLE_USER |
| `sophie` | `Password1` | ROLE_USER |
| `karim` | `Password1` | ROLE_USER |
| `claire` | `Password1` | ROLE_USER |

## 📡 API Endpoints

### Authentification (`/api/v1/auth`)
| Méthode | URI | Description |
|---------|-----|-------------|
| POST | `/api/v1/auth/login` | Connexion (retourne JWT) |
| POST | `/api/v1/auth/register` | Inscription |
| POST | `/api/v1/auth/refresh` | Rafraîchir le token |

### Annonces (`/api/v1/annonces`) — HATEOAS

Chaque réponse unitaire contient des liens `_links` (self, collection, publish, archive).

| Méthode | URI | Description | Auth |
|---------|-----|-------------|------|
| GET | `/api/v1/annonces` | Liste paginée + filtres | Public |
| GET | `/api/v1/annonces/{id}` | Détail (+ liens HATEOAS) | Public |
| POST | `/api/v1/annonces` | Création (→ DRAFT) | USER |
| PUT | `/api/v1/annonces/{id}` | Mise à jour complète | Auteur |
| PATCH | `/api/v1/annonces/{id}` | Mise à jour partielle | Auteur |
| DELETE | `/api/v1/annonces/{id}` | Suppression (si ARCHIVED) | Auteur |
| PATCH | `/api/v1/annonces/{id}/publish` | Publier (DRAFT → PUBLISHED) | Auteur |
| PATCH | `/api/v1/annonces/{id}/archive` | Archiver (→ ARCHIVED) | ADMIN |

### Filtres disponibles
```
GET /api/v1/annonces?q=paris&status=PUBLISHED&categoryId=1&authorId=2&fromDate=2025-01-01&toDate=2025-12-31&page=0&size=10&sort=createdAt,desc
```

### Catégories (`/api/v1/categories`)
| Méthode | URI | Description | Auth |
|---------|-----|-------------|------|
| GET | `/api/v1/categories` | Liste | Public |
| GET | `/api/v1/categories/{id}` | Détail | Public |
| POST | `/api/v1/categories` | Création | ADMIN |

### Métadonnées (`/api/v1/meta`)
| Méthode | URI | Description |
|---------|-----|-------------|
| GET | `/api/v1/meta/annonces` | Champs filtrables/triables (introspection) |

## 🔒 Sécurité

- **JWT** signé avec HMAC-SHA512, expiration configurable
- **Refresh Token** pour renouveler sans re-login
- **BCrypt** pour le hashage des mots de passe
- **Password strength** : min 8 caractères, 1 majuscule, 1 chiffre
- **Rôles** : `ROLE_USER`, `ROLE_ADMIN` avec `@PreAuthorize`
- **CORS** configuré (`WebConfig.java`) — origins : 3000, 5173, 4200
- **Rate limiting** : 5 tentatives/min par IP sur `/api/v1/auth/login` → 429
- **Stateless** : pas de session serveur

## 📊 Logging & Observabilité

- **AOP** : logging automatique entrée/sortie/durée/exceptions sur les services
- **Correlation ID** : tracé via header `X-Correlation-Id` + MDC SLF4J
- **Actuator** : `/actuator/health`, `/actuator/info`, `/actuator/metrics`
- **Prometheus** : `/actuator/prometheus` — métriques JVM, HTTP, et applicatives scrappables par Grafana

## 🧪 Tests

### Exécution
```bash
# Tous les tests (unitaires + intégration)
mvn clean verify

# Rapport JaCoCo
# → target/site/jacoco/index.html
```

### Stratégie
| Type | Framework | Fichiers |
|------|-----------|----------|
| Unitaires | Mockito + JUnit 5 | `JwtServiceTest`, `UserServiceTest`, `CategoryServiceTest`, `AnnonceServiceTest`, `AuthServiceTest`, `GlobalExceptionHandlerTest` |
| Intégration REST | MockMvc + @SpringBootTest | `AuthControllerIT`, `AnnonceControllerIT` |
| Base de données | **Testcontainers** (PostgreSQL) | Via JDBC URL `jdbc:tc:postgresql:...` |
| Couverture | JaCoCo (≥ 50%) | Enforcement automatique à la phase `verify` |

**Total : 50 tests** (39 unitaires + 11 intégration) — 8 classes de tests.

### Choix Testcontainers (justification)
Nous avons choisi **Testcontainers** plutôt que H2 car :
- PostgreSQL réel = même comportement qu'en production
- Automatique en CI (aucun service PostgreSQL à déclarer)
- Reproductible et isolé
- Conforme à une approche "industrialisable"

## 🐳 Docker

### Dockerfile
Build multi-stage :
1. **Build** : Maven compile + package sur JDK 17 Alpine
2. **Runtime** : JRE 17 Alpine (image légère ~200MB)

### Services (`docker-compose.yml`)
| Service | Image | Port | Description |
|---------|-------|------|-------------|
| `postgres` | postgres:16-alpine | 5433 | Base de données applicative |
| `app` | build local | 8080 | Application Spring Boot |
| `sonarqube` | sonarqube:lts-community | 9000 | Analyse de qualité de code |
| `sonarqube-db` | postgres:16-alpine | — | Base dédiée SonarQube |

### Commande unique
```bash
docker-compose up -d
```

## 🔍 SonarQube (Qualité de code)

### Lancement
```bash
# 1. Démarrer SonarQube
docker-compose up -d sonarqube

# 2. Attendre que SonarQube soit prêt (http://localhost:9000)
#    Login par défaut : admin / admin

# 3. Lancer l'analyse Maven
mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=<VOTRE_TOKEN>
```

### Configuration
- **Fichier** : `sonar-project.properties`
- **Couverture** : JaCoCo (rapport XML importé automatiquement)
- **Exclusions** : DTOs, modèles, config, classes générées

## 🔄 CI — GitHub Actions

Fichier : `.github/workflows/ci.yml`

### Pipeline
1. **Checkout** : `actions/checkout@v4`
2. **Setup Java** : Matrice Java 17 + 21, cache Maven
3. **Checkstyle** : `mvn checkstyle:check` (Google Java Style)
4. **Build & Tests** : `mvn -B clean verify` (tests jamais skippés)
5. **JaCoCo** : couverture ≥ 50% (enforcement)
6. **Artifacts** : JAR (`master-annonce-jar`) + rapport JaCoCo
7. **Docker** (main uniquement) : build + save de l'image
8. **Trivy** : scan de vulnérabilités de l'image Docker (CRITICAL, HIGH)

### Artifact produit
- Nom : `master-annonce-jar`
- Contenu : fichier `.jar` exécutable

## ☸️ Kubernetes (Minikube)

### Déploiement
```bash
minikube start
minikube addons enable ingress
eval $(minikube docker-env)        # ou PowerShell : minikube docker-env | Invoke-Expression
docker build -t masterannonce:1.0 .
kubectl apply -f k8s/
```

### Manifests (`k8s/`)
| Fichier | Description |
|---------|-------------|
| `postgres-deployment.yaml` | PostgreSQL avec probes |
| `postgres-service.yaml` | Service ClusterIP |
| `postgres-pvc.yaml` | Persistent Volume Claim |
| `postgres-secret.yaml` | Credentials base64 |
| `app-configmap.yaml` | Configuration Spring externalisée |
| `app-deployment.yaml` | App avec `replicas: 2`, readiness/liveness probes |
| `app-service.yaml` | NodePort (30080) |
| `ingress.yaml` | Nginx Ingress |

### Vérification
```bash
kubectl get pods          # → 2 pods masterannonce READY 1/1
kubectl get svc           # → NodePort 30080
minikube service masterannonce --url    # → URL d'accès
curl $(minikube service masterannonce --url)/api/v1/annonces
```

## 🗄️ Flyway (Migrations DB)

La base de données est gérée par **Flyway** (et non `ddl-auto: update`) :
- `V1__init_schema.sql` — Création des tables avec index
- `V2__seed_data.sql` — Données initiales (catégories + 2 utilisateurs)
- `V3__seed_annonces.sql` — 20 annonces réalistes + 3 utilisateurs supplémentaires

En production (`application-prod.yml`) : `ddl-auto: validate` pour sécurité maximale.

## ⚠️ Problèmes rencontrés & Solutions

### 1. Migration javax → jakarta (2h de débogage)
**Problème** : En passant de JAX-RS/Jersey à Spring Boot 3.x, **tous les imports `javax.*` sont devenus `jakarta.*`** (Jakarta EE 10). Le compilateur ne donnait pas d'erreur claire — juste des `ClassNotFoundException` au runtime.
**Solution** : Remplacement systématique dans tout le projet. La difficulté était surtout de ne rien oublier (`javax.persistence`, `javax.validation`, `javax.servlet`...).

### 2. LazyInitializationException avec Hibernate
**Problème** : En chargeant une annonce, l'accès à `annonce.getAuthor().getUsername()` lançait `LazyInitializationException` parce que la session Hibernate était déjà fermée. C'était très frustrant car le code marchait parfois (quand `open-in-view` était activé) et pas d'autres fois.
**Solution** : Désactiver `spring.jpa.open-in-view: false` (anti-pattern en production) et utiliser `@Query("SELECT a FROM Annonce a JOIN FETCH a.author JOIN FETCH a.category")` dans le repository pour charger les relations en une seule requête.

### 3. Conflit Flyway : deux fichiers V3
**Problème** : J'avais créé `V3__update_passwords.sql` pour corriger les hash BCrypt, puis plus tard `V3__seed_annonces.sql` pour les données de test. Flyway refusait de démarrer avec l'erreur `Found more than one migration with version 3`. Le message était clair mais j'ai d'abord cru que c'était un problème de cache.
**Solution** : Supprimé l'ancien `V3__update_passwords.sql` (devenu inutile car V2 contenait déjà les bons hash) et gardé le seed.

### 4. JWT — secret trop court pour HS512
**Problème** : Au runtime, un warning apparaissait : *"The signing key's size is 488 bits which is not secure enough for the HS512 algorithm"*. L'application fonctionnait quand même, mais ce n'était pas sécurisé.
**Solution** : Généré une clé de 528 bits (66 octets) encodée en Base64. Il fallait aussi penser à mettre à jour la clé dans **3 fichiers** (`application.yml`, `application-test.yml`, `JwtServiceTest.java`) sinon les tests échouaient avec des erreurs cryptiques de signature.

### 5. Testcontainers — Docker pas démarré
**Problème** : Les tests d'intégration échouaient silencieusement avec `Could not connect to Ryuk at localhost:xxxx`. Il m'a fallu un moment pour comprendre que **Docker Desktop doit tourner** pour que Testcontainers puisse lancer le conteneur PostgreSQL de test.
**Solution** : Lancer Docker Desktop **avant** `mvn verify`. J'ai aussi ajouté un commentaire dans le README pour les futurs lecteurs.

### 6. MapStruct + Spring : injection impossible
**Problème** : MapStruct générait des implémentations avec `new XxxMapperImpl()` au lieu de beans Spring, donc impossible de les `@Autowired`. L'erreur était `NoSuchBeanDefinitionException` et j'ai d'abord suspecté un problème de scan de packages.
**Solution** : Ajouter `-Amapstruct.defaultComponentModel=spring` dans la configuration du `maven-compiler-plugin`. Sans ça, MapStruct ignore complètement le contexte Spring.

### 7. Sécurité stateless vs JAAS
**Problème** : Le TP3 utilisait JAAS avec des sessions serveur. En passant à une API REST stateless, il a fallu repenser complètement l'authentification. Le plus dur était de comprendre comment injecter l'utilisateur courant dans les endpoints protégés.
**Solution** : Filtre JWT custom (`OncePerRequestFilter`) qui parse le token à chaque requête + `@AuthenticationPrincipal AuthenticatedUser user` comme paramètre de méthode. C'est plus propre que JAAS mais la courbe d'apprentissage de Spring Security est raide.

### 8. HATEOAS — implémentation manuelle
**Problème** : Spring HATEOAS avec `EntityModel<T>` ne marchait pas bien avec les records Java (immutables). Les liens étaient mal sérialisés.
**Solution** : Créé un `HateoasResponse<T>` custom avec un builder pattern. Plus de contrôle sur la structure JSON et compatible avec les records.

### 9. Pagination + Specifications JPA
**Problème** : Combiner les filtres dynamiques (statut, catégorie, recherche textuelle, dates) avec la pagination Spring (`Pageable`) n'était pas trivial. Les premières tentatives avec des `@Query` JPQL étaient illisibles et non maintenables.
**Solution** : Utilisation de `JpaSpecificationExecutor<Annonce>` avec un builder de `Specification<Annonce>` qui chaîne les critères via `and()`. Le code est beaucoup plus lisible et chaque filtre est isolé.

### 10. Optimistic Locking (409 Conflict)
**Problème** : En test, une mise à jour d'annonce échouait de temps en temps avec `ObjectOptimisticLockingFailureException`. J'ai mis du temps à comprendre que c'était lié au champ `@Version` qui n'était pas envoyé dans le DTO de mise à jour.
**Solution** : Ajouté le champ `version` dans `AnnonceUpdateDTO` et le transmettre dans le PUT. Le client doit envoyer la version qu'il a reçue — si elle ne correspond plus, c'est qu'un autre utilisateur a modifié entre-temps.


## 📬 Postman

Collection disponible : `MasterAnnonce.postman_collection.json`

- **20 requêtes** couvrant tous les endpoints (auth, annonces, catégories, méta, actuator)
- **Auto-token** : le login sauvegarde automatiquement le JWT dans une variable `{{token}}`
- **Auth Bearer** : configuré au niveau collection, appliqué à toutes les requêtes protégées
- **Filtres** : exemples de recherche textuelle, filtre par statut, par catégorie

### Import
1. Ouvrir Postman → **Import** → sélectionner `MasterAnnonce.postman_collection.json`
2. Exécuter "Login (admin)" en premier
3. Les autres requêtes utilisent automatiquement le token
