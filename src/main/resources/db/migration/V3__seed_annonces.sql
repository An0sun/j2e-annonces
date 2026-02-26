-- ===== Données de démonstration : annonces diversifiées =====

-- Utilisateurs supplémentaires (BCrypt hash de "Password1")
INSERT INTO user_account (username, email, password, role, created_at)
VALUES ('sophie', 'sophie@email.com', '$2a$10$HYSO6LqrVVRo0jthxkodLOJPk3DR1.0bjZZ1pCpha/rbtlOjBuH0y', 'ROLE_USER', CURRENT_TIMESTAMP - INTERVAL '30 days')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_account (username, email, password, role, created_at)
VALUES ('karim', 'karim@email.com', '$2a$10$HYSO6LqrVVRo0jthxkodLOJPk3DR1.0bjZZ1pCpha/rbtlOjBuH0y', 'ROLE_USER', CURRENT_TIMESTAMP - INTERVAL '20 days')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_account (username, email, password, role, created_at)
VALUES ('claire', 'claire@email.com', '$2a$10$HYSO6LqrVVRo0jthxkodLOJPk3DR1.0bjZZ1pCpha/rbtlOjBuH0y', 'ROLE_USER', CURRENT_TIMESTAMP - INTERVAL '15 days')
ON CONFLICT (username) DO NOTHING;

-- ===============================
-- IMMOBILIER (category_id = 1)
-- ===============================

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Appartement T3 lumineux centre-ville',
        'Bel appartement 65m² avec balcon, parquet, cuisine équipée. Proche transports et commerces. 3e étage avec ascenseur.',
        '12 rue de la République, Lyon 2e',
        'sophie@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'sophie'), 1,
        CURRENT_TIMESTAMP - INTERVAL '25 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Maison 4 pièces avec jardin',
        'Maison familiale 95m², jardin 200m², garage double. Quartier calme, proche écoles et parc. Chauffage gaz récent.',
        '8 allée des Tilleuls, Villeurbanne',
        'karim@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'karim'), 1,
        CURRENT_TIMESTAMP - INTERVAL '18 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Studio meublé étudiant',
        'Studio 22m² meublé, idéal étudiant. Kitchenette, salle d''eau, internet inclus. Disponible immédiatement.',
        '45 avenue Jean Jaurès, Lyon 7e',
        'claire@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'claire'), 1,
        CURRENT_TIMESTAMP - INTERVAL '10 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Loft industriel rénové',
        'Ancien atelier 120m² rénové, mezzanine, grandes baies vitrées, parquet béton ciré. Coup de cœur assuré.',
        '3 impasse des Forges, Lyon 9e',
        'sophie@email.com', 'DRAFT',
        (SELECT id FROM user_account WHERE username = 'sophie'), 1,
        CURRENT_TIMESTAMP - INTERVAL '2 days', 0);

-- ===============================
-- AUTOMOBILE (category_id = 2)
-- ===============================

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Renault Clio V 2021 — 25 000 km',
        'Essence, boîte auto, GPS, caméra de recul, première main. Entretien Renault à jour. CT vierge.',
        'Garage Central, Bron 69500',
        'karim@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'karim'), 2,
        CURRENT_TIMESTAMP - INTERVAL '22 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Peugeot 3008 GT Line 2020',
        'Diesel 130ch, toit panoramique, sièges cuir, full options. 48 000 km, carnet entretien complet.',
        '15 boulevard Vivier-Merle, Lyon 3e',
        'sophie@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'sophie'), 2,
        CURRENT_TIMESTAMP - INTERVAL '14 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Vélo électrique Moustache 2023',
        'VTT électrique Moustache Samedi 27, 500Wh, très bon état. Idéal vélotaf ou loisirs.',
        'Place Bellecour, Lyon 2e',
        'claire@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'claire'), 2,
        CURRENT_TIMESTAMP - INTERVAL '7 days', 0);

-- ===============================
-- EMPLOI (category_id = 3)
-- ===============================

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Développeur Java/Spring — CDI',
        'Startup SaaS recrute dev Java 3+ ans. Stack : Spring Boot, PostgreSQL, Docker, CI/CD. Télétravail 3j/sem.',
        'La Part-Dieu, Lyon 3e',
        'admin@masterannonce.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'admin'), 3,
        CURRENT_TIMESTAMP - INTERVAL '20 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Stage Marketing Digital — 6 mois',
        'Agence digitale cherche stagiaire motivé(e). SEO, Google Ads, réseaux sociaux. Gratification légale + tickets resto.',
        '22 rue Mercière, Lyon 2e',
        'claire@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'claire'), 3,
        CURRENT_TIMESTAMP - INTERVAL '12 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Plombier expérimenté — CDD 6 mois',
        'Entreprise BTP recrute plombier qualifié pour chantiers neufs. Permis B requis. Salaire selon expérience.',
        'Zone industrielle, Vénissieux',
        'karim@email.com', 'DRAFT',
        (SELECT id FROM user_account WHERE username = 'karim'), 3,
        CURRENT_TIMESTAMP - INTERVAL '3 days', 0);

-- ===============================
-- ÉLECTRONIQUE (category_id = 4)
-- ===============================

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('MacBook Pro M2 14" — comme neuf',
        'MacBook Pro 2023, 16Go RAM, 512Go SSD, moins de 50 cycles batterie. Facture Apple, sous garantie.',
        'Confluence, Lyon 2e',
        'sophie@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'sophie'), 4,
        CURRENT_TIMESTAMP - INTERVAL '8 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('PlayStation 5 + 2 manettes',
        'PS5 Édition Standard, 2 manettes DualSense, 3 jeux inclus (Spider-Man 2, FC25, God of War). État impeccable.',
        '7 rue Victor Hugo, Lyon 2e',
        'karim@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'karim'), 4,
        CURRENT_TIMESTAMP - INTERVAL '5 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('iPhone 15 Pro 256Go',
        'iPhone 15 Pro Titane Naturel, débloqué tout opérateur, coque et chargeur MagSafe inclus. Aucune rayure.',
        '30 cours Charlemagne, Lyon 2e',
        'claire@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'claire'), 4,
        CURRENT_TIMESTAMP - INTERVAL '4 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Écran gaming 27" 165Hz',
        'Samsung Odyssey G5, 27 pouces, 1440p, 165Hz, 1ms. Parfait pour le gaming compétitif. Boîte d''origine.',
        'Centre commercial Carré de Soie',
        'sophie@email.com', 'DRAFT',
        (SELECT id FROM user_account WHERE username = 'sophie'), 4,
        CURRENT_TIMESTAMP - INTERVAL '1 day', 0);

-- ===============================
-- SERVICES (category_id = 5)
-- ===============================

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Cours particuliers de mathématiques',
        'Professeur certifié donne cours de maths du collège à la terminale. 25€/h. Résultats garantis, +3 points de moyenne.',
        'À domicile, Lyon et agglomération',
        'claire@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'claire'), 5,
        CURRENT_TIMESTAMP - INTERVAL '16 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Déménagement — camion + 2 déménageurs',
        'Service de déménagement complet : emballage, transport, montage meubles. Devis gratuit. Disponible le week-end.',
        'Lyon et périphérie (50 km)',
        'karim@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'karim'), 5,
        CURRENT_TIMESTAMP - INTERVAL '11 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Photographe événementiel',
        'Photographe pro pour mariages, anniversaires, événements corporate. Galerie en ligne sous 48h. Devis sur mesure.',
        'Toute la région Rhône-Alpes',
        'sophie@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'sophie'), 5,
        CURRENT_TIMESTAMP - INTERVAL '6 days', 0);

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Garde d''animaux à domicile',
        'Pet-sitter expérimentée : chiens, chats, NAC. Promenades, visites, hébergement possible. Références disponibles.',
        '10 rue Garibaldi, Lyon 6e',
        'claire@email.com', 'PUBLISHED',
        (SELECT id FROM user_account WHERE username = 'claire'), 5,
        CURRENT_TIMESTAMP - INTERVAL '3 days', 0);

-- ===============================
-- Annonce archivée (pour démo)
-- ===============================

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id, created_at, version)
VALUES ('Canapé d''angle cuir — VENDU',
        'Canapé d''angle en cuir véritable, 5 places, très bon état. Couleur taupe. Article vendu.',
        '55 rue de la Part-Dieu, Lyon 3e',
        'karim@email.com', 'ARCHIVED',
        (SELECT id FROM user_account WHERE username = 'karim'), 5,
        CURRENT_TIMESTAMP - INTERVAL '28 days', 0);
