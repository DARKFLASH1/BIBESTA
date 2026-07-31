-- phpMyAdmin SQL Dump — VERSION CORRIGÉE
-- Corrections apportées par rapport au dump original (bibliotheque.sql) :
--   1. paiement.abonnement_id rendu NULLABLE (un paiement peut régler une amende sans abonnement)
--   2. CHECK ajouté sur paiement : exactement un des deux (abonnement_id XOR amende_id) doit être renseigné
--   3. amende.emprunt_id passé en UNIQUE (un emprunt a au plus une amende, cohérent avec le MCD 0..1/1..1)
--   4. historique.livre_id ajouté (troisième relation prévue par le MCD, absente du dump original)
--   5. exemplaire.livre_id passé en ON DELETE RESTRICT (cohérence avec emprunt.exemplaire_id en RESTRICT :
--      évite qu'une suppression de livre parte en cascade puis échoue sur un exemplaire encore emprunté)
--   6. utilisateur.email passé en UNIQUE (évite les doublons de compte)
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */
;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */
;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */
;
/*!40101 SET NAMES utf8mb4 */
;
-- --------------------------------------------------------
--
-- Structure de la table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
CREATE TABLE IF NOT EXISTS `utilisateur` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `date_naissance` date NOT NULL,
  `sexe` varchar(25) NOT NULL,
  `email` varchar(255) NOT NULL,
  `identifiant` varchar(50) NOT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `motDePasse` varchar(255) NOT NULL,
  `role` enum(
    'BIBLIOTHECAIRE',
    'ETUDIANT',
    'ENSEIGNANT',
    'PUBLIC'
  ) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `identifiant` (`identifiant`),
  UNIQUE KEY `email` (`email`)
) ENGINE = InnoDB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
INSERT INTO `utilisateur` (
    `id`,
    `nom`,
    `prenom`,
    `date_naissance`,
    `sexe`,
    `email`,
    `identifiant`,
    `contact`,
    `motDePasse`,
    `role`
  )
VALUES (
    1,
    'Admin',
    'Bibliotheque',
    '1990-01-01',
    'M',
    'admin@bibliotheque.com',
    'admin',
    '+226 00 00 00 00',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'BIBLIOTHECAIRE'
  ),
  (
    2,
    'TINGUIERI',
    'Bachar',
    '2004-01-01',
    'masculin',
    'mohamad@gmail.com',
    'yellowflash',
    '+226 06 78 27 25',
    '2d93cad3b769a719805864cd676d62f66c96f33a7393f285fb5c285bfe099bc1',
    'ETUDIANT'
  );
-- --------------------------------------------------------
--
-- Structure de la table `livre`
--

DROP TABLE IF EXISTS `livre`;
CREATE TABLE IF NOT EXISTS `livre` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titre` varchar(255) NOT NULL,
  `auteur` varchar(255) NOT NULL,
  `edition` varchar(100) DEFAULT NULL,
  `isbn` varchar(20) NOT NULL,
  `categorie` varchar(100) DEFAULT NULL,
  `langue` varchar(50) DEFAULT NULL,
  `annee_Publication` int DEFAULT NULL,
  `nombre_pages` int DEFAULT NULL,
  `genre` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `isbn` (`isbn`),
  KEY `idx_livre_titre` (`titre`),
  KEY `idx_livre_auteur` (`auteur`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
INSERT INTO `livre` (
    `id`,
    `titre`,
    `auteur`,
    `edition`,
    `isbn`,
    `categorie`,
    `langue`,
    `annee_Publication`
  )
VALUES (
    1,
    'Batifolages',
    'Jiraya',
    '1',
    '987-2873-234',
    'developement personnel',
    'francais',
    2004
  );
-- --------------------------------------------------------
--
-- Structure de la table `exemplaire`
--

DROP TABLE IF EXISTS `exemplaire`;
CREATE TABLE IF NOT EXISTS `exemplaire` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numero` varchar(50) NOT NULL,
  `etat` enum(
    'DISPONIBLE',
    'EMPRUNTE',
    'RESERVE',
    'EN_REPARATION'
  ) NOT NULL DEFAULT 'DISPONIBLE',
  `livre_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_exemplaire_livre` (`livre_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
-- --------------------------------------------------------
--
-- Structure de la table `emprunt`
--
 
DROP TABLE IF EXISTS `emprunt`;
CREATE TABLE IF NOT EXISTS `emprunt` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dateDebut` date NOT NULL,
  `dateRetourPrevue` date NOT NULL,
  `dateRetourReelle` date DEFAULT NULL,
  `statut` enum('EN_COURS', 'RETOURNE', 'EN_RETARD') DEFAULT 'EN_COURS',
  `utilisateur_id` int NOT NULL,
  `exemplaire_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_emprunt_utilisateur` (`utilisateur_id`),
  KEY `idx_emprunt_exemplaire` (`exemplaire_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
-- --------------------------------------------------------
--
-- Structure de la table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dateReservation` date NOT NULL,
  `statut` enum('EN_ATTENTE', 'CONFIRMEE', 'ANNULEE') DEFAULT 'EN_ATTENTE',
  `utilisateur_id` int NOT NULL,
  `livre_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reservation_utilisateur` (`utilisateur_id`),
  KEY `reservation_ibfk_livre` (`livre_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
INSERT INTO `reservation` (
    `id`,
    `dateReservation`,
    `statut`,
    `utilisateur_id`,
    `livre_id`
  )
VALUES (1, '2026-03-09', 'CONFIRMEE', 2, 1);
-- --------------------------------------------------------
--
-- Structure de la table `amende`
-- CORRECTION : emprunt_id passé en UNIQUE (au plus une amende par emprunt, cf. MCD 0..1/1..1)
--

DROP TABLE IF EXISTS `amende`;
CREATE TABLE IF NOT EXISTS `amende` (
  `id` int NOT NULL AUTO_INCREMENT,
  `montant` decimal(10, 2) NOT NULL,
  `raison` varchar(255) DEFAULT NULL,
  `date` date NOT NULL,
  `emprunt_id` int NOT NULL,
  `statut` enum('EN_ATTENTE', 'PAYEE', 'ANNULEE') DEFAULT 'EN_ATTENTE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_amende_emprunt` (`emprunt_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
-- --------------------------------------------------------
--
-- Structure de la table `abonnement`
--

DROP TABLE IF EXISTS `abonnement`;
CREATE TABLE IF NOT EXISTS `abonnement` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL,
  `dateDebut` date NOT NULL,
  `dateFin` date NOT NULL,
  `statutPaiement` enum('EN_ATTENTE', 'PAYE', 'EXPIRE') DEFAULT 'EN_ATTENTE',
  `utilisateur_id` int NOT NULL,
  `montant` float NOT NULL,
  PRIMARY KEY (`id`),
  KEY `utilisateur_id` (`utilisateur_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
INSERT INTO `abonnement` (
    `id`,
    `type`,
    `dateDebut`,
    `dateFin`,
    `statutPaiement`,
    `utilisateur_id`,
    `montant`
  )
VALUES (
    1,
    'MENSUEL',
    '2026-03-09',
    '2026-04-09',
    'PAYE',
    2,
    2000
  );
-- --------------------------------------------------------
--
-- Structure de la table `paiement`
-- CORRECTION : abonnement_id rendu NULLABLE + CHECK d'exclusivité avec amende_id
--

DROP TABLE IF EXISTS `paiement`;
CREATE TABLE IF NOT EXISTS `paiement` (
  `id` int NOT NULL AUTO_INCREMENT,
  `montant` decimal(10, 2) NOT NULL,
  `datePaiement` date NOT NULL,
  `statut` varchar(50) DEFAULT 'EFFECTUE',
  `abonnement_id` int DEFAULT NULL,
  `methode_paiement` varchar(255) NOT NULL,
  `amende_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `abonnement_id` (`abonnement_id`),
  KEY `paiement_ibfk_amende` (`amende_id`),
  CONSTRAINT `chk_paiement_cible_unique` CHECK (
    (
      `abonnement_id` IS NOT NULL
      AND `amende_id` IS NULL
    )
    OR (
      `abonnement_id` IS NULL
      AND `amende_id` IS NOT NULL
    )
  )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
-- --------------------------------------------------------
--
-- Structure de la table `notification`
--

DROP TABLE IF EXISTS `notification`;
CREATE TABLE IF NOT EXISTS `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL,
  `contenu` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `date` date NOT NULL,
  `utilisateur_id` int NOT NULL,
  `statut` enum('LU', 'NON_LU') NOT NULL DEFAULT 'NON_LU',
  PRIMARY KEY (`id`),
  KEY `idx_notification_utilisateur` (`utilisateur_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
-- --------------------------------------------------------
--
-- Structure de la table `historique`
-- CORRECTION : ajout de livre_id (troisième relation prévue par le MCD, absente du dump original)
--

DROP TABLE IF EXISTS `historique`;
CREATE TABLE IF NOT EXISTS `historique` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dateMouvement` datetime NOT NULL,
  `type` varchar(50) NOT NULL,
  `description` text,
  `utilisateur_id` int NOT NULL,
  `emprunt_id` int DEFAULT NULL,
  `reservation_id` int DEFAULT NULL,
  `livre_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `emprunt_id` (`emprunt_id`),
  KEY `reservation_id` (`reservation_id`),
  KEY `idx_historique_utilisateur` (`utilisateur_id`),
  KEY `idx_historique_livre` (`livre_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `abonnement`
--
ALTER TABLE `abonnement`
ADD CONSTRAINT `abonnement_ibfk_1` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`) ON DELETE RESTRICT;
--
-- Contraintes pour la table `amende`
--
ALTER TABLE `amende`
ADD CONSTRAINT `amende_ibfk_1` FOREIGN KEY (`emprunt_id`) REFERENCES `emprunt` (`id`) ON DELETE RESTRICT;
--
-- Contraintes pour la table `emprunt`
--
ALTER TABLE `emprunt`
ADD CONSTRAINT `emprunt_ibfk_1` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `emprunt_ibfk_2` FOREIGN KEY (`exemplaire_id`) REFERENCES `exemplaire` (`id`) ON DELETE RESTRICT;
--
-- Contraintes pour la table `exemplaire`
-- CORRECTION : CASCADE -> RESTRICT, cohérent avec emprunt.exemplaire_id en RESTRICT
-- (évite qu'une suppression de livre parte en cascade puis échoue sur un exemplaire encore emprunté)
--
ALTER TABLE `exemplaire`
ADD CONSTRAINT `exemplaire_ibfk_1` FOREIGN KEY (`livre_id`) REFERENCES `livre` (`id`) ON DELETE RESTRICT;
--
-- Contraintes pour la table `historique`
--
ALTER TABLE `historique`
ADD CONSTRAINT `historique_ibfk_1` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `historique_ibfk_2` FOREIGN KEY (`emprunt_id`) REFERENCES `emprunt` (`id`) ON DELETE
SET NULL,
  ADD CONSTRAINT `historique_ibfk_3` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE
SET NULL,
  ADD CONSTRAINT `historique_ibfk_4` FOREIGN KEY (`livre_id`) REFERENCES `livre` (`id`) ON DELETE
SET NULL;
--
-- Contraintes pour la table `notification`
--
ALTER TABLE `notification`
ADD CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE;
--
-- Contraintes pour la table `paiement`
--
ALTER TABLE `paiement`
ADD CONSTRAINT `paiement_ibfk_1` FOREIGN KEY (`abonnement_id`) REFERENCES `abonnement` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `paiement_ibfk_amende` FOREIGN KEY (`amende_id`) REFERENCES `amende` (`id`) ON DELETE RESTRICT;
--
-- Contraintes pour la table `reservation`
--
ALTER TABLE `reservation`
ADD CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `reservation_ibfk_livre` FOREIGN KEY (`livre_id`) REFERENCES `livre` (`id`) ON DELETE RESTRICT;
COMMIT;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */
;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */
;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */
;