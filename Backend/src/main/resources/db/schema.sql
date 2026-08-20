-- Script de création du schéma de la base de données BIBESTA
-- Compatible MySQL 8.0+

-- Suppression des tables si elles existent (ordre inverse des dépendances)
DROP TABLE IF EXISTS historique;
DROP TABLE IF EXISTS paiement;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS amende;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS emprunt;
DROP TABLE IF EXISTS abonnement;
DROP TABLE IF EXISTS exemplaire;
DROP TABLE IF EXISTS livre;
DROP TABLE IF EXISTS utilisateur;

-- ================================================
-- Table: utilisateur
-- ================================================
CREATE TABLE utilisateur (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    sexe VARCHAR(25) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    identifiant VARCHAR(50) NOT NULL UNIQUE,
    contact VARCHAR(20),
    motDePasse VARCHAR(255) NOT NULL,
    role ENUM('BIBLIOTHECAIRE', 'ETUDIANT', 'ENSEIGNANT', 'PUBLIC') NOT NULL,
    statut ENUM('ACTIF', 'SUSPENDU', 'DESACTIVE') NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: livre
-- ================================================
CREATE TABLE livre (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    editeur VARCHAR(255),
    annee_publication INT,
    categorie VARCHAR(100),
    resume TEXT,
    nbPages INT,
    langue VARCHAR(50) DEFAULT 'Français',
    disponible BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: exemplaire
-- ================================================
CREATE TABLE exemplaire (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    numExemplaire VARCHAR(255) NOT NULL,
    etat_physique ENUM('BON_ETAT', 'USAGE', 'ENDOMMAGE', 'PERDU') NOT NULL DEFAULT 'BON_ETAT',
    statut_disponibilite ENUM('DISPONIBLE', 'EMPRUNTE', 'RESERVE', 'EN_REPARATION') NOT NULL DEFAULT 'DISPONIBLE',
    livre_id INT NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_exemplaire_livre FOREIGN KEY (livre_id) REFERENCES livre(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: abonnement
-- ================================================
CREATE TABLE abonnement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL,
    dateDebut DATE NOT NULL,
    dateFin DATE NOT NULL,
    statutPaiement ENUM('EN_ATTENTE', 'PAYE', 'EXPIRE') NOT NULL DEFAULT 'EN_ATTENTE',
    montant DECIMAL(10,2) NOT NULL,
    utilisateur_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_abonnement_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: emprunt
-- ================================================
CREATE TABLE emprunt (
    id INT PRIMARY KEY AUTO_INCREMENT,
    utilisateur_id INT NOT NULL,
    exemplaire_id BIGINT NOT NULL,
    dateDebut DATE NOT NULL,
    dateRetourPrevue DATE NOT NULL,
    dateRetourReelle DATE,
    statut ENUM('EN_COURS', 'RETOURNE', 'EN_RETARD') NOT NULL DEFAULT 'EN_COURS',
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emprunt_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE RESTRICT,
    CONSTRAINT fk_emprunt_exemplaire FOREIGN KEY (exemplaire_id) REFERENCES exemplaire(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: reservation
-- ================================================
CREATE TABLE reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    utilisateur_id INT NOT NULL,
    livre_id INT NOT NULL,
    dateReservation DATETIME NOT NULL,
    dateExpiration DATETIME,
    statut ENUM('EN_ATTENTE', 'CONFIRMEE', 'ANNULEE', 'EXPIREE') NOT NULL DEFAULT 'EN_ATTENTE',
    positionFile INT,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_livre FOREIGN KEY (livre_id) REFERENCES livre(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: amende
-- ================================================
CREATE TABLE amende (
    id INT PRIMARY KEY AUTO_INCREMENT,
    emprunt_id INT NOT NULL,
    montant DECIMAL(10,2) NOT NULL,
    dateCreation DATE NOT NULL,
    dateLimite DATE,
    statut ENUM('EN_ATTENTE', 'PAYEE', 'ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE',
    motif VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_amende_emprunt FOREIGN KEY (emprunt_id) REFERENCES emprunt(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: paiement
-- ================================================
CREATE TABLE paiement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    amende_id INT,
    abonnement_id INT,
    montant DECIMAL(10,2) NOT NULL,
    datePaiement DATETIME NOT NULL,
    moyenPaiement ENUM('ESPECES', 'CARTE', 'MOBILE_MONEY', 'VIREMENT') NOT NULL,
    referencePaiement VARCHAR(100),
    statut ENUM('REUSSI', 'ECHEC', 'EN_ATTENTE') NOT NULL DEFAULT 'EN_ATTENTE',
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_paiement_amende FOREIGN KEY (amende_id) REFERENCES amende(id) ON DELETE SET NULL,
    CONSTRAINT fk_paiement_abonnement FOREIGN KEY (abonnement_id) REFERENCES abonnement(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: notification
-- ================================================
CREATE TABLE notification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    utilisateur_id INT NOT NULL,
    type ENUM('EMPRUNT', 'RETOUR', 'RETARD', 'RESERVATION', 'AMENDE', 'ABONNEMENT', 'GENERAL') NOT NULL,
    message TEXT NOT NULL,
    lu BOOLEAN DEFAULT FALSE,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: historique
-- ================================================
CREATE TABLE historique (
    id INT PRIMARY KEY AUTO_INCREMENT,
    utilisateur_id INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    emprunt_id INT,
    livre_id INT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historique_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    CONSTRAINT fk_historique_emprunt FOREIGN KEY (emprunt_id) REFERENCES emprunt(id) ON DELETE SET NULL,
    CONSTRAINT fk_historique_livre FOREIGN KEY (livre_id) REFERENCES livre(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Index pour améliorer les performances
-- ================================================
CREATE INDEX idx_utilisateur_email ON utilisateur(email);
CREATE INDEX idx_utilisateur_role ON utilisateur(role);
CREATE INDEX idx_livre_titre ON livre(titre);
CREATE INDEX idx_livre_isbn ON livre(isbn);
CREATE INDEX idx_exemplaire_livre ON exemplaire(livre_id);
CREATE INDEX idx_exemplaire_statut ON exemplaire(statut_disponibilite);
CREATE INDEX idx_emprunt_utilisateur ON emprunt(utilisateur_id);
CREATE INDEX idx_emprunt_statut ON emprunt(statut);
CREATE INDEX idx_reservation_utilisateur ON reservation(utilisateur_id);
CREATE INDEX idx_reservation_livre ON reservation(livre_id);
CREATE INDEX idx_amende_emprunt ON amende(emprunt_id);
CREATE INDEX idx_amende_statut ON amende(statut);
