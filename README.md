# Système de Gestion de Bibliothèque

Application de gestion de bibliothèque

## Objectif du projet

Remplacer la gestion manuelle (Excel, papier) d'une bibliothèque par une solution digitale sécurisée et évolutive, couvrant la gestion des collections, des emprunts, des retours et des utilisateurs, avec des outils de recherche et de suivi pour bibliothécaires et lecteurs.

## Stack technique

| Couche | Technologie |
|---|---|
| Frontend | Angular |
| Backend | Spring Boot (Java) |
| Base de données | MySQL |
| Communication | API REST (JSON) |
| Sécurité | Spring Security + JWT |
| Build backend | Maven |
| Build frontend | Angular CLI / npm |

## Architecture

```

bibliotheque/
├── backend/                  # API Spring Boot
│   ├── src/main/java/
│   │   └── com/esta/bibliotheque/
│   │       ├── controller/   # Endpoints REST
│   │       ├── service/      # Logique métier
│   │       ├── repository/   # Accès données (Spring Data JPA)
│   │       ├── model/        # Entités JPA
│   │       ├── dto/          # Objets de transfert
│   │       ├── security/     # Config JWT / rôles
│   │       └── exception/    # Gestion centralisée des erreurs
│   └── src/main/resources/
│       └── application.properties
│
└── frontend/                 # Application Angular
    └── src/app/
        ├── core/              # Services, guards, intercepteurs
        ├── shared/            # Composants réutilisables
        └── features/
            ├── auth/
            ├── users/
            ├── books/
            ├── loans/
            ├── search/
            ├── notifications/
            └── reports/
```

## Modèle de données (MCD)

Modèle conceptuel de données du groupe, 10 entités.

### Entités

| Entité | Attributs |
|---|---|
| **Utilisateur** | Nom, Prenom, Date_naissance, Sexe, Email, Role, Identifiant, Contact, MotDePasse |
| **Livre** | Titre, Auteur, Edition, Categorie, Annee_publication, Langue, ISBN (`String`) |
| **Exemplaire** | Num_exemplaire, Etat |
| **Emprunt** | Date_debut, Date_retour_prevue, Date_retour_reelle, Statut |
| **Reservation** | Date_reserv, Statut |
| **Amende** | Montant, Raison, Date, Statut |
| **Abonnement** | Type, Date_debut, Date_fin, Montant, StatutPaiement |
| **Paiement** | Montant, Date, MethodePaiement, Statut |
| **Notification** | Type, Contenue, Date_envoi, Statut |
| **Historique** | Date_mouv, Description, Type |

### Associations

| Classe A | Card. A | Card. B | Classe B | Remarque |
|---|:---:|:---:|---|---|
| Livre | 1..* | 1..1 | Exemplaire | Un livre a au moins un exemplaire |
| Utilisateur | 0..* | 1..1 | Emprunt | Un utilisateur peut avoir plusieurs emprunts |
| Exemplaire | 0..* | 1..1 | Emprunt | Un exemplaire peut être emprunté plusieurs fois |
| Emprunt | 0..1 | 1..1 | Amende | Un emprunt a au plus une amende ; une amende concerne exactement un emprunt |
| Livre | 1..1 | 0..* | Reservation | Un livre peut avoir plusieurs réservations |
| Utilisateur | 0..* | 1..1 | Reservation | Un utilisateur peut faire plusieurs réservations |
| Utilisateur | 1..1 | 0..* | Abonnement | Un utilisateur peut avoir plusieurs abonnements |
| Abonnement | 1..1 | 0..* | Paiement | Un abonnement peut avoir 0, 1 ou plusieurs paiements (échelonnement, renouvellement) |
| Amende | 1..1 | 0..1 | Paiement | Une amende peut avoir au plus un paiement qui la règle |
| Utilisateur | 0..* | 1..1 | Notification | Un utilisateur peut recevoir plusieurs notifications |
| Historique | 0..* | 1..1 | Utilisateur | L'historique est associé à un utilisateur |
| Historique | 0..* | 0..1 | Emprunt | L'historique peut tracer des emprunts |
| Historique | 0..* | 0..1 | Livre | L'historique peut tracer des mouvements de livres |
| Historique | 0..* | 0..1 | Reservation | L'historique peut tracer des réservations |

**Contrainte applicative (non modélisable en MCD)** : un `Paiement` référence soit un `Abonnement`, soit une `Amende`, jamais les deux et jamais aucun des deux. Le schéma relationnel ne peut pas l'imposer nativement — à valider par un `CHECK` en base ou par la couche service Spring Boot.

## Modules fonctionnels

### 1. Gestion des utilisateurs

- CRUD comptes utilisateurs avec rôles
- Suivi des emprunts et retards par utilisateur

### 2. Gestion des livres

- CRUD livres et exemplaires
- Historique des mouvements (emprunts, réservations, retours)

### 3. Gestion des emprunts et retours

- Création d'emprunt avec dates
- Gestion automatique des retards
- Réservation sur livre indisponible
- Historique par utilisateur / par livre

### 4. Recherche et consultation

- Recherche par titre, auteur, ISBN, catégorie, mots-clés
- Filtres avancés : disponibilité, langue, année

### 5. Notifications et alertes

- Rappels avant date limite
- Alerte disponibilité sur réservation
- Alerte retard / amende

### 6. Abonnements et paiement

- Suivi des cotisations/abonnements des utilisateurs (à spécifier selon les besoins du groupe)

### 7. Statistiques et reporting

- Emprunts les plus fréquents, livres les plus populaires
- Suivi des retards et amendes
- Export PDF / Excel

## Endpoints API (aperçu)

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Authentification |
| GET | `/api/users` | Liste des utilisateurs |
| POST | `/api/users` | Création d'un utilisateur |
| GET | `/api/books?search=&category=&lang=` | Recherche de livres |
| POST | `/api/books` | Ajout d'un livre |
| POST | `/api/loans` | Créer un emprunt |
| PUT | `/api/loans/{id}/return` | Enregistrer un retour |
| POST | `/api/reservations` | Réserver un livre |
| GET | `/api/reports/most-borrowed` | Statistiques d'emprunts |
| GET | `/api/reports/export?format=pdf` | Export rapport |

## Déroulement logique du développement

L'ordre suit les dépendances réelles du MCD : une entité ne peut être développée avant celles dont elle dépend par clé étrangère.

### Étape 0 — Préparation

- Convertir le MCD corrigé en MLD (tables, clés primaires/étrangères) et créer le schéma MySQL
- Mettre en place les squelettes Spring Boot (Maven, dépendances JPA/Security/Web) et Angular (CLI, routing, modules)

### Étape 1 — Entités racines (sans dépendance)

- `Utilisateur` : entité JPA, repository, CRUD, gestion des rôles
- `Livre` : entité JPA, repository, CRUD

### Étape 2 — Entités dépendantes de niveau 1

- `Exemplaire` (dépend de `Livre`, 1..*) : un livre doit exister avant de créer un exemplaire
- `Abonnement` (dépend de `Utilisateur`, 1..1)
- `Notification` (dépend de `Utilisateur`, 1..1)

### Étape 3 — Entités transactionnelles (dépendent de deux entités)

- `Emprunt` (dépend de `Utilisateur` + `Exemplaire`) : implémenter en premier car `Amende` et `Historique` en dépendent
- `Reservation` (dépend de `Utilisateur` + `Livre`)

### Étape 4 — Entités de règlement (dépendent des transactions)

- `Amende` (dépend de `Emprunt`, au plus une par emprunt) : logique de calcul à définir (retard, dommage)
- `Paiement` (référence `Abonnement` en 0..* ou `Amende` en 0..1) : imposer par code que chaque paiement règle soit un abonnement, soit une amende — jamais les deux, jamais aucun des deux

### Étape 5 — Historisation (dépend de tout ce qui précède)

- `Historique` (dépend de `Utilisateur`, et optionnellement de `Emprunt` ou `Livre`) : implémenter en dernier côté backend, via des écouteurs d'événements ou déclenchements explicites à chaque mouvement (emprunt, retour, réservation)

### Étape 6 — API REST

- Exposer les contrôleurs dans le même ordre (Utilisateur → Livre → Exemplaire → Emprunt/Reservation → Amende/Paiement → Historique/Notification)
- Sécuriser les endpoints par rôle (JWT + Spring Security)

### Étape 7 — Frontend Angular

- Modules dans le même ordre de dépendance : auth/utilisateurs → catalogue livres → emprunts/réservations → amendes/paiements → notifications/historique
- Chaque module frontend n'est développable qu'une fois son endpoint backend disponible

### Étape 8 — Fonctionnalités transverses

- Recherche/filtrage sur `Livre` (titre, auteur, ISBN, catégorie, langue, année)
- Notifications automatiques (rappel avant échéance, alerte réservation disponible, alerte retard)
- Statistiques et exports PDF/Excel (dépendent de `Emprunt` et `Historique` déjà peuplés)

### Étape 9 — Tests et cas limites

- Double réservation sur un même livre, exemplaire déjà emprunté, paiement sur abonnement expiré
- Cohérence des statuts (`Emprunt.Statut`, `Reservation.Statut`, `Amende.Statut`, `Paiement.Statut`)

## Installation

### Prérequis

- JDK 17+
- Node.js 18+ / npm
- MySQL 8+
- Maven

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
ng serve
```

### Base de données

```sql
CREATE DATABASE bibliotheque;
```

Configurer les identifiants dans `backend/src/main/resources/application.properties`.
