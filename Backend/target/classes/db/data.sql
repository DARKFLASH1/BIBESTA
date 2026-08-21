-- Script de données de test pour BIBESTA
-- Données fictives pour démarrer rapidement en développement

-- ================================================
-- Utilisateurs de test (mot de passe: "password123")
-- Hash BCrypt généré pour "password123"
-- ================================================
INSERT INTO utilisateur (nom, prenom, date_naissance, sexe, email, identifiant, contact, motDePasse, role, statut) VALUES
('Bibliothécaire', 'Admin', '1985-05-15', 'MASCULIN', 'admin@bibesta.com', 'ADMIN001', '+243999999999', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAtmZ1EHsMwmzrOpoGy7qLpIYjW6', 'BIBLIOTHECAIRE', 'ACTIF'),
('Etudiant', 'Jean', '2002-03-20', 'MASCULIN', 'jean.etudiant@univ.cd', 'ETU001', '+243811111111', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAtmZ1EHsMwmzrOpoGy7qLpIYjW6', 'ETUDIANT', 'ACTIF'),
('Enseignant', 'Marie', '1978-11-08', 'FEMININ', 'marie.prof@univ.cd', 'ENS001', '+243822222222', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAtmZ1EHsMwmzrOpoGy7qLpIYjW6', 'ENSEIGNANT', 'ACTIF'),
('Public', 'Pierre', '1995-07-25', 'MASCULIN', 'pierre.public@email.com', 'PUB001', '+243833333333', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAtmZ1EHsMwmzrOpoGy7qLpIYjW6', 'PUBLIC', 'ACTIF');

-- ================================================
-- Livres de test
-- ================================================
INSERT INTO livre (titre, auteur, isbn, editeur, annee_publication, categorie, resume, nbPages, langue) VALUES
('Introduction à Java', 'John Smith', '978-2-123456-01-0', 'Editions Tech', 2022, 'Programmation', 'Un guide complet pour apprendre Java', 450, 'Français'),
('Algorithmes et Structures de Données', 'Robert Martin', '978-2-123456-02-7', 'Informatique Plus', 2021, 'Programmation', 'Les fondamentaux des algorithmes', 520, 'Français'),
('Base de Données Relationnelles', 'Sophie Durand', '978-2-123456-03-4', 'DataPress', 2023, 'Base de données', 'Maîtrisez SQL et la modélisation', 380, 'Français'),
('Développement Web avec Angular', 'Thomas Anderson', '978-2-123456-04-1', 'WebDev Editions', 2022, 'Web', 'Créez des applications web modernes', 600, 'Français'),
('Spring Boot en Action', 'Emily Johnson', '978-2-123456-05-8', 'Java Press', 2023, 'Programmation', 'Développez avec Spring Boot', 480, 'Français'),
('Intelligence Artificielle', 'Alan Turing Jr', '978-2-123456-06-5', 'AI Publications', 2024, 'IA', 'Introduction au Machine Learning', 550, 'Français'),
('Sécurité Informatique', 'David Brown', '978-2-123456-07-2', 'SecureBooks', 2023, 'Sécurité', 'Protégez vos systèmes', 420, 'Français'),
('Architecture Logicielle', 'Martin Fowler Jr', '978-2-123456-08-9', 'ArchPress', 2022, 'Architecture', 'Les patterns essentiels', 500, 'Français');

-- ================================================
-- Exemplaires (plusieurs exemplaires par livre)
-- ================================================
INSERT INTO exemplaire (numExemplaire, etat_physique, statut_disponibilite, livre_id) VALUES
('EX-JAVA-001', 'BON_ETAT', 'DISPONIBLE', 1),
('EX-JAVA-002', 'BON_ETAT', 'DISPONIBLE', 1),
('EX-JAVA-003', 'USAGE', 'DISPONIBLE', 1),
('EX-ALGO-001', 'BON_ETAT', 'DISPONIBLE', 2),
('EX-ALGO-002', 'BON_ETAT', 'DISPONIBLE', 2),
('EX-BD-001', 'BON_ETAT', 'DISPONIBLE', 3),
('EX-BD-002', 'ENDOMMAGE', 'EN_REPARATION', 3),
('EX-ANGULAR-001', 'BON_ETAT', 'DISPONIBLE', 4),
('EX-ANGULAR-002', 'BON_ETAT', 'DISPONIBLE', 4),
('EX-SPRING-001', 'BON_ETAT', 'DISPONIBLE', 5),
('EX-SPRING-002', 'BON_ETAT', 'DISPONIBLE', 5),
('EX-IA-001', 'BON_ETAT', 'DISPONIBLE', 6),
('EX-SECURITE-001', 'BON_ETAT', 'DISPONIBLE', 7),
('EX-ARCHI-001', 'BON_ETAT', 'DISPONIBLE', 8);

-- ================================================
-- Abonnements actifs
-- ================================================
INSERT INTO abonnement (type, dateDebut, dateFin, statutPaiement, montant, utilisateur_id) VALUES
('ANNUEL', DATE_SUB(CURDATE(), INTERVAL 6 MONTH), DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 'PAYE', 50.00, 2), -- Étudiant
('ANNUEL', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 9 MONTH), 'PAYE', 75.00, 3), -- Enseignant
('MENSUEL', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 1 MONTH), 'PAYE', 10.00, 4); -- Public

-- ================================================
-- Notifications de bienvenue
-- ================================================
INSERT INTO notification (utilisateur_id, type, message, lu) VALUES
(2, 'GENERAL', 'Bienvenue dans votre bibliothèque ! Votre abonnement est actif.'),
(3, 'GENERAL', 'Bienvenue cher enseignant. Profitez de nos services.'),
(4, 'GENERAL', 'Bienvenue ! N''oubliez pas de renouveler votre abonnement mensuel.');

-- ================================================
-- Historique initial
-- ================================================
INSERT INTO historique (utilisateur_id, action, details) VALUES
(1, 'CREATION_COMPTE', 'Compte bibliothécaire créé'),
(2, 'CREATION_COMPTE', 'Compte étudiant créé'),
(3, 'CREATION_COMPTE', 'Compte enseignant créé'),
(4, 'CREATION_COMPTE', 'Compte public créé');
