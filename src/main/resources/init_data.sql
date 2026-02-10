-- =============================================
-- Script d'initialisation des données
-- MasterAnnonce - TP 02 JPA/Hibernate
-- =============================================

-- Note : Hibernate avec hbm2ddl.auto=update crée les tables automatiquement.
-- Ce script sert uniquement à insérer des données de base.

-- Catégories
INSERT INTO category (label) VALUES ('Immobilier') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Emploi') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Véhicules') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Services') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Électronique') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Loisirs') ON CONFLICT (label) DO NOTHING;

-- Utilisateur de test (mot de passe : test)
INSERT INTO user_account (username, email, password, created_at)
VALUES ('admin', 'admin@masterannonce.fr', 'admin', NOW())
ON CONFLICT (username) DO NOTHING;
