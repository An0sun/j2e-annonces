-- ===== Schema MasterAnnonce =====

-- Table des catégories
CREATE TABLE IF NOT EXISTS category (
    id          BIGSERIAL PRIMARY KEY,
    label       VARCHAR(64) NOT NULL UNIQUE
);

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS user_account (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des annonces
CREATE TABLE IF NOT EXISTS annonce (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    address     VARCHAR(64),
    mail        VARCHAR(64),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    author_id   BIGINT REFERENCES user_account(id),
    category_id BIGINT REFERENCES category(id),
    version     BIGINT DEFAULT 0
);

-- Index pour les recherches fréquentes
CREATE INDEX IF NOT EXISTS idx_annonce_status ON annonce(status);
CREATE INDEX IF NOT EXISTS idx_annonce_author ON annonce(author_id);
CREATE INDEX IF NOT EXISTS idx_annonce_category ON annonce(category_id);
CREATE INDEX IF NOT EXISTS idx_annonce_created_at ON annonce(created_at);
