# MasterAnnonce – API REST Java EE avec JAX-RS / Jersey

## Architecture

L'application suit une architecture en **4 couches** avec exposition REST :

```
┌──────────────────────────────────────┐
│    REST Layer (JAX-RS / Jersey)      │  ← Endpoints REST, DTOs, Filtres
├──────────────────────────────────────┤
│    Service Layer                     │  ← Logique métier, transactions
├──────────────────────────────────────┤
│    Repository Layer (DAO JPA)        │  ← Accès données via JPQL
├──────────────────────────────────────┤
│    JPA / Hibernate → PostgreSQL      │  ← ORM
└──────────────────────────────────────┘
```

### Structure des packages

```
org.j2e
├── bean/                # Entités JPA (User, Category, Annonce, AnnonceStatus)
├── dao/                 # Repositories JPA (JPQL, pagination)
├── service/             # Services métier avec gestion des transactions
├── rest/
│   ├── JaxRsApplication.java    # Point d'entrée JAX-RS (@ApplicationPath("/api"))
│   ├── resource/        # Resources REST (AnnonceResource, AuthResource, HelloResource)
│   ├── dto/             # DTOs Request/Response (AnnonceDTO, LoginDTO, PaginatedResponse...)
│   ├── mapper/          # Mapping Entity ↔ DTO (pattern Builder)
│   ├── filter/          # Filtre de sécurité JAX-RS (@Secured, SecurityFilter)
│   └── exception/       # ExceptionMappers centralisés (400, 403, 404, 409, 500)
├── security/            # TokenStore (authentification stateless)
├── servlet/             # Servlets legacy (Login, Register, etc.)
├── filter/              # Filtre Servlet legacy (AuthFilter)
└── util/                # Utilitaires (JPAUtil)
```

## Technologies utilisées

| Technologie | Version | Usage |
|---|---|---|
| Java | 17 | Langage |
| Jersey | 2.41 | Implémentation JAX-RS (javax.ws.rs) |
| Jackson | via Jersey | Sérialisation JSON |
| Hibernate ORM | 5.6.15 | JPA (javax.persistence) |
| Hibernate Validator | 6.2.5 | Bean Validation |
| PostgreSQL | 42.6.0 | Base de données |
| SLF4J + Logback | 2.0.9 / 1.4.14 | Logging structuré |
| Swagger / OpenAPI | 2.2.19 | Documentation API |
| JUnit 5 | 5.13.2 | Tests |
| Mockito | 5.11 | Mocks |
| H2 | 2.2.224 | Base de données de test |
| Jersey Test Framework | 2.41 | Tests d'intégration REST |

## API REST – Endpoints

### Authentification
| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/login` | Connexion → retourne un token | ❌ |
| POST | `/api/register` | Inscription | ❌ |

### Annonces
| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/annonces?page=1&size=10` | Liste paginée | ❌ |
| GET | `/api/annonces/{id}` | Détail d'une annonce | ❌ |
| POST | `/api/annonces` | Créer une annonce (→ DRAFT) | ✅ |
| PUT | `/api/annonces/{id}` | Mise à jour complète | ✅ |
| PATCH | `/api/annonces/{id}` | Mise à jour partielle | ✅ |
| DELETE | `/api/annonces/{id}` | Supprimer (archivée uniquement) | ✅ |
| POST | `/api/annonces/{id}/publish` | Publier (DRAFT → PUBLISHED) | ✅ |
| POST | `/api/annonces/{id}/archive` | Archiver (→ ARCHIVED) | ✅ |

### Utilitaires
| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/helloWorld` | Test de fonctionnement |
| GET | `/api/params?name=xxx` | Démo @QueryParam |
| GET | `/api/params/{id}` | Démo @PathParam |
| GET | `/api/openapi.json` | Documentation OpenAPI |

## Flux de sécurité (Stateless)

```
1. POST /api/login { username, password }
   → Vérification credentials via UserService
   → Génération UUID token → stockage en mémoire (TokenStore)
   → Réponse : { token, username, userId }

2. Requêtes protégées :
   Header : Authorization: Bearer <token>
   → SecurityFilter intercepte (@Secured)
   → Validation token via TokenStore
   → Injection userId dans SecurityContext
   → Si invalide : 401 Unauthorized

3. Règles métier :
   - Seul l'auteur peut modifier/supprimer/publier/archiver
   - Une annonce PUBLISHED ne peut plus être modifiée
   - Suppression uniquement si ARCHIVED
   - Optimistic locking via @Version
```

## Gestion des erreurs

Format JSON normalisé pour toutes les erreurs :
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "Annonce introuvable (id=42)",
    "details": [],
    "timestamp": "2026-02-12T14:30:00"
}
```

| Code | ExceptionMapper | Cas d'usage |
|---|---|---|
| 400 | ValidationExceptionMapper | Bean Validation (champs invalides) |
| 401 | SecurityFilter | Token absent ou invalide |
| 403 | ForbiddenExceptionMapper | Non-auteur tente modification |
| 404 | NotFoundExceptionMapper | Ressource introuvable |
| 409 | BusinessExceptionMapper | Conflit métier (annonce publiée, non archivée) |
| 500 | GenericExceptionMapper | Erreur interne (filet de sécurité) |

## Tests

```
mvnw.cmd test           # Tests unitaires uniquement (*Test.java)
mvnw.cmd verify         # Tests unitaires + intégration (*IT.java)
```

| Suite | Fichier | Tests | Description |
|---|---|---|---|
| Unit | AnnonceRepositoryTest | 8 | CRUD repository avec H2 |
| Unit | AnnonceServiceTest | 12 | Logique métier |
| Unit | WebServletTest | 9 | Servlets et filtres (Mockito) |
| Unit | AnnonceResourceTest | 15 | DTOs, Mapper, Exceptions, TokenStore |
| Intégration | IntegrationTest | 3 | Workflow complet services |
| Intégration | AnnonceApiIT | 15 | Workflow REST API complet |

**Total : 62 tests (47 unitaires + 15 intégration)**

## Problèmes rencontrés et solutions

### 1. LazyInitializationException
**Problème** : Accès aux relations `@ManyToOne(fetch = LAZY)` après fermeture de l'EntityManager.  
**Solution** : `JOIN FETCH` dans les requêtes JPQL du repository.

### 2. javax vs jakarta
**Problème** : Jersey 3.x utilise `jakarta.ws.rs`, incompatible avec `javax.persistence` (Hibernate 5).  
**Solution** : Utilisation de Jersey 2.41 qui reste sur le namespace `javax.ws.rs`.

### 3. Sécurité stateless
**Problème** : Les sessions HTTP ne conviennent pas pour une API REST.  
**Solution** : Authentification par token Bearer + `@Secured` NameBinding + `ContainerRequestFilter`.

### 4. Séparation tests unitaires / intégration
**Problème** : Maven exécute tous les tests ensemble.  
**Solution** : Convention `*Test.java` (Surefire) / `*IT.java` (Failsafe).

### 5. Concurrence optimiste (Optimistic Locking)
**Problème** : Modifications concurrentes d'une même annonce.  
**Solution** : `@Version` sur l'entité Annonce, vérifié automatiquement par JPA.

## Lancement

1. PostgreSQL doit tourner sur `localhost:5433` avec une base `MasterAnnonce`
2. `.\mvnw.cmd clean compile` pour compiler
3. Déployer le WAR sur Tomcat
4. API accessible à `http://localhost:8080/J2E/api/`
5. Documentation OpenAPI à `http://localhost:8080/J2E/api/openapi.json`

## Collection Postman

Le fichier `MasterAnnonce.postman_collection.json` à la racine du projet contient **17 requêtes** organisées par section :
- **Hello World** : test des endpoints Ex.1 (helloWorld, QueryParam, PathParam)
- **Authentification** : register, login (succès + échec)
- **CRUD Annonces** : création, listing, détail, mise à jour PUT/PATCH, cas d'erreur (401, 400, 404)
- **Workflow Statut** : publish → modification interdite (409) → suppression sans archivage (409) → archive → suppression (204)
- **Documentation** : accès OpenAPI

> **Astuce** : les scripts Postman sauvegardent automatiquement le `token` et l'`annonceId` dans les variables de collection.
