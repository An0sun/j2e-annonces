-- ===== Seed data =====

-- Catégories
INSERT INTO category (label) VALUES ('Immobilier') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Automobile') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Emploi') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Électronique') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Services') ON CONFLICT (label) DO NOTHING;

-- Utilisateurs (BCrypt hash de "Password1")
INSERT INTO user_account (username, email, password, role, created_at)
VALUES ('admin', 'admin@masterannonce.com', '$2a$10$HYSO6LqrVVRo0jthxkodLOJPk3DR1.0bjZZ1pCpha/rbtlOjBuH0y', 'ROLE_ADMIN', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_account (username, email, password, role, created_at)
VALUES ('user1', 'user1@masterannonce.com', '$2a$10$HYSO6LqrVVRo0jthxkodLOJPk3DR1.0bjZZ1pCpha/rbtlOjBuH0y', 'ROLE_USER', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;
