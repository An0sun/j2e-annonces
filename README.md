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
├── config/            ← OpenAPI config
├── persistence/       ← Spring Data JPA repositories + Specifications
└── security/          ← JWT, Spring Security, UserDetailsService
```

## 🛠️ Technologies

| Technologie | Version | Usage |
|-------------|---------|-------|
| Spring Boot | 3.5.5 | Framework principal |
| Spring Data JPA | 3.5.x | Repositories + Specifications |
| Spring Security | 6.x | Authentification JWT stateless |
| MapStruct | 1.6.3 | Mapping DTO ↔ Entity |
| jjwt | 0.12.6 | Génération/validation JWT |
| SpringDoc OpenAPI | 2.8.6 | Documentation Swagger |
| PostgreSQL | 16 | Base de données |
| JaCoCo | 0.8.12 | Couverture de code |
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
| `admin` | `password` | ROLE_ADMIN |
| `user1` | `password` | ROLE_USER |

## 📡 API Endpoints

### Authentification (`/api/auth`)
| Méthode | URI | Description |
|---------|-----|-------------|
| POST | `/api/auth/login` | Connexion (retourne JWT) |
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/refresh` | Rafraîchir le token |

### Annonces (`/api/annonces`)
| Méthode | URI | Description | Auth |
|---------|-----|-------------|------|
| GET | `/api/annonces` | Liste paginée + filtres | Public |
| GET | `/api/annonces/{id}` | Détail | Public |
| POST | `/api/annonces` | Création (→ DRAFT) | USER |
| PUT | `/api/annonces/{id}` | Mise à jour complète | Auteur |
| PATCH | `/api/annonces/{id}` | Mise à jour partielle | Auteur |
| DELETE | `/api/annonces/{id}` | Suppression (si ARCHIVED) | Auteur |
| PATCH | `/api/annonces/{id}/publish` | Publier (DRAFT → PUBLISHED) | Auteur |
| PATCH | `/api/annonces/{id}/archive` | Archiver (→ ARCHIVED) | ADMIN |

### Filtres disponibles
```
GET /api/annonces?q=paris&status=PUBLISHED&categoryId=1&authorId=2&fromDate=2025-01-01&toDate=2025-12-31&page=0&size=10&sort=createdAt,desc
```

### Catégories (`/api/categories`)
| Méthode | URI | Description | Auth |
|---------|-----|-------------|------|
| GET | `/api/categories` | Liste | Public |
| GET | `/api/categories/{id}` | Détail | Public |
| POST | `/api/categories` | Création | ADMIN |

### Métadonnées (`/api/meta`)
| Méthode | URI | Description |
|---------|-----|-------------|
| GET | `/api/meta/annonces` | Champs filtrables/triables (introspection) |

## 🔒 Sécurité

- **JWT** signé avec HMAC-SHA512, expiration configurable
- **Refresh Token** pour renouveler sans re-login
- **BCrypt** pour le hashage des mots de passe
- **Rôles** : `ROLE_USER`, `ROLE_ADMIN` avec `@PreAuthorize`
- **Stateless** : pas de session serveur

## 📊 Logging & Observabilité

- **AOP** : logging automatique entrée/sortie/durée/exceptions sur les services
- **Correlation ID** : tracé via header `X-Correlation-Id` + MDC SLF4J
- **Actuator** : `/actuator/health`, `/actuator/info`

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
| Unitaires | Mockito + JUnit 5 | `AnnonceServiceTest` |
| Intégration REST | MockMvc + @SpringBootTest | `AuthControllerIT`, `AnnonceControllerIT` |
| Base de données | **Testcontainers** (PostgreSQL) | Via JDBC URL `jdbc:tc:postgresql:...` |

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

### Commande unique
```bash
docker-compose up -d
```

## 🔄 CI — GitHub Actions

Fichier : `.github/workflows/ci.yml`

### Pipeline
1. **Checkout** : `actions/checkout@v4`
2. **Setup Java** : Matrice Java 17 + 21, cache Maven
3. **Build & Tests** : `mvn -B clean verify` (tests jamais skippés)
4. **Artifacts** : JAR (`master-annonce-jar`) + rapport JaCoCo
5. **Docker** (main uniquement) : build + save de l'image

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
curl $(minikube service masterannonce --url)/api/annonces
```

## ⚠️ Problèmes rencontrés & Solutions

### 1. Migration javax → jakarta
**Problème** : Spring Boot 3.x utilise Jakarta EE 10 (`jakarta.*`), pas `javax.*`.
**Solution** : Remplacement systématique de tous les imports.

### 2. LazyInitializationException
**Problème** : Accès aux relations lazy (author, category) hors session Hibernate.
**Solution** : `@Query` avec `JOIN FETCH` dans le repository + `open-in-view: false`.

### 3. MapStruct + Spring
**Problème** : MapStruct doit utiliser le modèle Spring pour l'injection.
**Solution** : Configurer `mapstruct.defaultComponentModel=spring` dans le `maven-compiler-plugin`.

### 4. Sécurité stateless avec JWT
**Problème** : JAAS du TP3 n'est plus adapté avec Spring Security.
**Solution** : Filtre JWT personnalisé (`OncePerRequestFilter`) + `SessionCreationPolicy.STATELESS`.

### 5. Gestion des transactions
**Problème** : Transactions manuelles dans les DAO (TP3).
**Solution** : `@Transactional` au niveau service, géré automatiquement par Spring.

## 📬 Postman

Collection mise à jour disponible — les endpoints sont identiques au TP3 avec :
- Nouveau header `Authorization: Bearer <token>`
- Nouveau endpoint `POST /api/auth/refresh`
- Nouveau endpoint `PATCH /api/annonces/{id}` (mise à jour partielle)
