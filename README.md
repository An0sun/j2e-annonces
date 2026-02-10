# MasterAnnonce – Application Web Java EE avec JPA / Hibernate

## Architecture

L'application suit une architecture en **3 couches** :

```
┌──────────────────────────────────┐
│    Web Layer (Servlets + JSP)    │  ← Gestion HTTP, affichage
├──────────────────────────────────┤
│    Service Layer                 │  ← Logique métier, transactions
├──────────────────────────────────┤
│    Repository Layer (DAO JPA)    │  ← Accès données via JPQL
├──────────────────────────────────┤
│    JPA / Hibernate → PostgreSQL  │  ← ORM
└──────────────────────────────────┘
```

### Structure des packages

```
org.j2e
├── bean/            # Entités JPA (User, Category, Annonce, AnnonceStatus)
├── dao/             # Repositories JPA (AnnonceRepository, UserRepository, CategoryRepository)
├── service/         # Services métier avec gestion des transactions
├── servlet/         # Servlets (Login, Register, Annonce CRUD, etc.)
├── filter/          # Filtre de sécurité (AuthFilter)
└── util/            # Utilitaires (JPAUtil - EntityManagerFactory singleton)
```

## Technologies utilisées

- **Java 17** (Adoptium)
- **JPA 2.2** (javax.persistence)
- **Hibernate ORM 5.6.15** (implémentation JPA)
- **Hibernate Validator 6.2.5** (Bean Validation)
- **PostgreSQL 42.6.0** (driver JDBC)
- **Servlets / JSP / JSTL** (javax.servlet 4.0)
- **Maven** (build tool)

## Modèle de données

| Entité   | Table          | Relations                        |
|----------|----------------|----------------------------------|
| User     | user_account   | OneToMany → Annonce              |
| Category | category       | OneToMany → Annonce              |
| Annonce  | annonce        | ManyToOne → User, ManyToOne → Category |

### Enum AnnonceStatus
`DRAFT` → `PUBLISHED` → `ARCHIVED`

## Problèmes rencontrés

### 1. LazyInitializationException
**Problème** : Accès aux relations `@ManyToOne(fetch = LAZY)` après fermeture de l'EntityManager.  
**Solution** : Utilisation de `JOIN FETCH` dans les requêtes JPQL du repository pour charger les relations nécessaires en une seule requête.

### 2. Table "user" réservée en PostgreSQL
**Problème** : Le mot `user` est réservé en SQL.  
**Solution** : Nommage de la table `user_account` via `@Table(name = "user_account")`.

### 3. Transactions dans les Servlets
**Problème** : Tentation de gérer les transactions dans les servlets.  
**Solution** : Toute gestion de transactions est dans la couche Service. Les servlets appellent uniquement les méthodes du service.

### 4. N+1 Queries
**Problème** : Chargement de N annonces + 1 requête par relation (author, category).  
**Solution** : `JOIN FETCH` dans les requêtes de listing pour tout charger en une seule requête SQL.

### 5. javax vs jakarta
**Problème** : Mélange de namespaces `javax.servlet` et `jakarta.ee`.  
**Solution** : Unification sur `javax.persistence` (Hibernate 5) et `javax.servlet` (Servlet API 4.0).

## Solutions apportées

- **JPAUtil** : Singleton `EntityManagerFactory` initialisé au chargement de la classe, avec méthode `close()` pour l'arrêt propre.
- **AuthFilter** : `@WebFilter("/*")` interceptant toutes les requêtes, avec whitelist pour les pages publiques (login, register, ressources statiques).
- **Pagination** : Utilisation de `setFirstResult()` / `setMaxResults()` dans les requêtes JPQL.
- **Bean Validation** : Annotations `@NotNull`, `@Size`, `@Email` sur toutes les entités pour la validation côté serveur.

## Lancement

1. PostgreSQL doit tourner sur `localhost:5433` avec une base `MasterAnnonce`
2. `mvnw.cmd clean compile` pour compiler
3. Déployer le WAR sur Tomcat
4. Accéder à `http://localhost:8080/J2E/`
