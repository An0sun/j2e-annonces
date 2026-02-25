-- ===== Données initiales =====

-- Catégories
INSERT INTO category (label) VALUES ('Immobilier') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Automobile') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Emploi') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Électronique') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Services') ON CONFLICT (label) DO NOTHING;

-- Utilisateurs (mots de passe hashés BCrypt : mdp = "password")
-- $2a$10$dXJ3SW6G7P50lGEBiH7DiOeKJnGYLc2G4/CwJWU.pVvQWQTrL3L.m = BCrypt("password")
INSERT INTO user_account (username, email, password, role, created_at)
VALUES ('admin', 'admin@masterannonce.com', '$2a$10$dXJ3SW6G7P50lGEBiH7DiOeKJnGYLc2G4/CwJWU.pVvQWQTrL3L.m', 'ROLE_ADMIN', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_account (username, email, password, role, created_at)
VALUES ('user1', 'user1@masterannonce.com', '$2a$10$dXJ3SW6G7P50lGEBiH7DiOeKJnGYLc2G4/CwJWU.pVvQWQTrL3L.m', 'ROLE_USER', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;
