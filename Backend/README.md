# BIBESTA — Système de Gestion de Bibliothèque

> Application web complète de gestion de bibliothèque, conçue pour remplacer la gestion manuelle (Excel, papier) par une solution digitale sécurisée. Elle couvre la gestion des collections, des emprunts, des retours, des réservations, des abonnements, des amendes et des utilisateurs.

---

## Table des matières

1. [Présentation générale](#1-présentation-générale)
2. [Stack technique](#2-stack-technique)
3. [Architecture du projet](#3-architecture-du-projet)
4. [Modèle de données](#4-modèle-de-données)
5. [Règles de gestion métier](#5-règles-de-gestion-métier)
6. [Endpoints API](#6-endpoints-api)
7. [Sécurité](#7-sécurité)
8. [Automatisations planifiées](#8-automatisations-planifiées)
9. [Installation et démarrage](#9-installation-et-démarrage)
---

## 1. Présentation générale

BIBESTA est une application de gestion de bibliothèque à deux interfaces :

- **Le bibliothécaire** gère les livres, les exemplaires, les utilisateurs, les emprunts, les retours, les amendes, les abonnements et les réservations.
- **Le lecteur** consulte le catalogue, suit ses emprunts, ses réservations et ses notifications depuis son espace personnel.

L'application est divisée en deux parties distinctes qui communiquent via une API :

- Le **backend** (serveur) : reçoit les demandes, applique les règles métier, accède à la base de données.
- Le **frontend** (interface web) : ce que l'utilisateur voit et utilise dans son navigateur.

---

## 2. Stack technique

| Couche | Technologie | Rôle |
|---|---|---|
| Frontend | Angular 19 (Standalone + Signals) | Interface utilisateur dans le navigateur |
| Backend | Spring Boot 3 (Java 21) | Serveur, logique métier, API REST |
| Base de données | MySQL 8 | Stockage persistant des données |
| Communication | API REST (JSON) | Échange de données entre frontend et backend |
| Sécurité | Spring Security + JWT | Authentification et autorisation |
| Build backend | Maven | Compilation et gestion des dépendances Java |
| Build frontend | Angular CLI / npm | Compilation et gestion des dépendances TypeScript |
| ORM | Hibernate / Spring Data JPA | Mapping automatique Java ↔ MySQL |
| Utilitaires Java | Lombok | Génération automatique de getters/setters/constructeurs |

---

## 3. Architecture du projet

```
BIBESTA/
├── backend/                          # Serveur Spring Boot
│   └── src/main/java/com/example/BIBESTA/
│       ├── BibestaApplication.java   # Point d'entrée + @EnableScheduling
│       ├── controller/               # Reçoit les requêtes HTTP (endpoints REST)
│       ├── service/                  # Logique métier (règles, calculs, validations)
│       ├── repository/               # Accès base de données (Spring Data JPA)
│       ├── model/                    # Entités JPA = tables MySQL
│       ├── dto/                      # Objets de transfert (ce qu'on envoie/reçoit)
│       │   └── Mapper.java           # Convertit entités ↔ DTOs
│       ├── security/                 # JWT, filtre d'authentification, utilitaires
│       │   ├── JwtUtil.java          # Génère et valide les tokens JWT
│       │   ├── JwtFilter.java        # Intercepte chaque requête pour vérifier le token
│       │   ├── SecurityConfig.java   # Règles d'accès par route et par rôle
│       │   └── SecurityUtils.java    # Utilitaire : récupère l'id de l'utilisateur connecté
│       └── exception/                # Gestion centralisée des erreurs
│           ├── GlobalExceptionHandler.java
│           ├── BusinessException.java     # Règle métier violée → HTTP 400
│           └── ResourceNotFoundException.java # Ressource introuvable → HTTP 404
│
└── frontend/                         # Application Angular 19
    └── src/app/
        ├── core/                     # Services globaux, guards, intercepteurs
        ├── shared/                   # Composants réutilisables
        └── features/
            ├── auth/                 # Connexion / déconnexion
            ├── users/                # Gestion des utilisateurs
            ├── books/                # Catalogue et gestion des livres
            ├── loans/                # Emprunts et retours
            ├── reservations/         # Réservations
            ├── notifications/        # Centre de notifications
            └── reports/              # Statistiques (prévu)
```

### Comment les couches communiquent

```
Navigateur (Angular)
      ↓  requête HTTP (GET /livres, POST /emprunts...)
Controller (reçoit et délègue)
      ↓
Service (applique les règles métier)
      ↓
Repository (parle à MySQL via JPA)
      ↓
Base de données MySQL
```

---

## 4. Modèle de données

### Les 10 entités et leurs champs

| Entité | Champs | Notes |
|---|---|---|
| **Utilisateur** | id, nom, prenom, dateNaissance, sexe, email, identifiant, contact, motDePasse, role, statut | `role` : BIBLIOTHECAIRE / ETUDIANT / ENSEIGNANT / PUBLIC. `statut` : ACTIF / SUSPENDU / DESACTIVE |
| **Livre** | id, titre, auteur, edition, isbn, categorie, genre, langue, anneePublication, nombrePages, actif | `actif = false` = livre désactivé (suppression logique) |
| **Exemplaire** | id, numExemplaire, etatPhysique, statutDisponibilite, livre | `etatPhysique` : BON_ETAT / USAGE / ENDOMMAGE / PERDU. `statutDisponibilite` : DISPONIBLE / EMPRUNTE / RESERVE / EN_REPARATION |
| **Emprunt** | id, dateDebut, dateRetourPrevue, dateRetourReelle, statut, utilisateur, exemplaire | `statut` : EN_COURS / RETOURNE / EN_RETARD. Durée standard : 14 jours |
| **Reservation** | id, dateReservation, statut, utilisateur, livre | `statut` : EN_ATTENTE / CONFIRMEE / ANNULEE. On réserve un livre, pas un exemplaire précis |
| **Amende** | id, montant, raison, date, statut, emprunt | `statut` : EN_ATTENTE / PAYEE / ANNULEE. Tarif : 100 FCFA/jour de retard |
| **Abonnement** | id, type, dateDebut, dateFin, montant, statutPaiement, utilisateur | `statutPaiement` : EN_ATTENTE / PAYE / EXPIRE |
| **Paiement** | id, montant, datePaiement, methodePaiement, statut, abonnement, amende | Règle : soit `abonnement`, soit `amende` — jamais les deux. `methodePaiement` : ESPECES / MOBILE_MONEY / CARTE_BANCAIRE |
| **Notification** | id, type, contenu, date, statut, utilisateur | `statut` : LU / NON_LU |
| **Historique** | id, dateMouvement, type, description, utilisateur, emprunt, livre, reservation | Trace automatique de toutes les actions. `type` : EMPRUNT / RETOUR / RESERVATION / ANNULATION / PAIEMENT / CONNEXION |

### Relations entre entités

| Entité A | Relation | Entité B | Signification |
|---|:---:|---|---|
| Livre | 1 → N | Exemplaire | Un livre a un ou plusieurs exemplaires physiques |
| Utilisateur | 1 → N | Emprunt | Un utilisateur peut avoir plusieurs emprunts |
| Exemplaire | 1 → N | Emprunt | Un exemplaire peut être emprunté plusieurs fois (pas en même temps) |
| Emprunt | 1 → 0..1 | Amende | Un emprunt peut avoir au plus une amende |
| Utilisateur | 1 → N | Reservation | Un utilisateur peut avoir plusieurs réservations |
| Livre | 1 → N | Reservation | Un livre peut avoir plusieurs réservations en file d'attente |
| Utilisateur | 1 → N | Abonnement | Un utilisateur peut avoir plusieurs abonnements (renouvellements) |
| Abonnement | 1 → N | Paiement | Un abonnement peut être payé en plusieurs fois |
| Amende | 1 → 0..1 | Paiement | Une amende est réglée par au plus un paiement |
| Utilisateur | 1 → N | Notification | Un utilisateur reçoit plusieurs notifications |
| Utilisateur | 1 → N | Historique | Toutes les actions d'un utilisateur sont tracées |

---

## 5. Règles de gestion métier

Ce sont les règles que le système applique automatiquement. Elles sont toutes implémentées dans la couche service.

### Règles sur l'emprunt

| Règle | Description | Où c'est implémenté |
|---|---|---|
| RG1 | L'exemplaire doit être `DISPONIBLE` | `EmpruntService.creerEmprunt()` |
| RG2 | L'utilisateur doit avoir un abonnement actif et payé | `EmpruntService` → `AbonnementService.hasAbonnementActif()` |
| RG3 | L'utilisateur ne doit avoir aucune amende impayée | `EmpruntService.creerEmprunt()` |
| RG4 | Quota d'emprunts simultanés par rôle : PUBLIC=3, ETUDIANT=5, ENSEIGNANT=10, BIBLIOTHECAIRE=10 | `EmpruntService.creerEmprunt()` |
| RG5 | Le compte utilisateur doit être `ACTIF` | `EmpruntService` et `ReservationService` |
| RG6 | Durée standard d'un emprunt : 14 jours | `EmpruntService.creerEmprunt()` |
| RG7 | Si retour en retard → amende créée automatiquement (100 FCFA/jour) | `EmpruntService.enregistrerRetour()` → `AmendeService.creerAmende()` |

### Règles sur les réservations

| Règle | Description | Où c'est implémenté |
|---|---|---|
| RG8 | File d'attente FIFO : premier réservé = premier servi | `ReservationRepository.findByLivreIdAndStatutOrderByDateReservationAsc()` |
| RG9 | Quand un exemplaire devient disponible, la première réservation est confirmée et l'exemplaire est verrouillé (`RESERVE`) | `ReservationService.confirmerReservationsSiDisponible()` |
| RG10 | Une réservation confirmée expire après 48h si non retirée | `ScheduledTasks` → `ReservationService.expirerReservationsConfirmees()` |
| RG11 | Un utilisateur ne peut pas réserver deux fois le même livre | `ReservationRepository.existsByUtilisateurIdAndLivreIdAndStatut()` |

### Règles sur les paiements

| Règle | Description | Où c'est implémenté |
|---|---|---|
| RG12 | Un paiement règle soit un abonnement, soit une amende — jamais les deux | `PaiementService.payerAbonnement()` et `payerAmende()` |

### Règles sur les suppressions

| Règle | Description | Où c'est implémenté |
|---|---|---|
| RG13 | Les utilisateurs et livres ne sont jamais supprimés physiquement — désactivation logique uniquement | `UtilisateurService.deleteById()` → `statut = DESACTIVE`. `LivreService.deleteLivre()` → `actif = false` |
| RG14 | Un livre avec un emprunt en cours ne peut pas être désactivé | `LivreService.isLivreEmprunte()` |
| RG15 | Un exemplaire emprunté ne peut pas être supprimé | `ExemplaireService.deleteById()` |

---

## 6. Endpoints API

**Base URL :** `http://localhost:8080`

> Les routes sont en français, sans préfixe `/api`.

### Authentification

| Méthode | Route | Description | Accès |
|---|---|---|---|
| POST | `/auth/login` | Connexion — retourne un token JWT | Public |

### Livres

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/livres` | Tous les livres | Public |
| GET | `/livres/{id}` | Un livre par id | Public |
| GET | `/livres/search?titre=&auteur=&isbn=&genre=&langue=&categorie=` | Recherche multicritère | Public |
| GET | `/livres/page?page=0&size=10&sort=titre` | Liste paginée | Public |
| GET | `/livres/search/page?query=java&page=0&size=10` | Recherche paginée | Public |
| POST | `/livres` | Créer un livre | Bibliothécaire |
| PUT | `/livres/{id}` | Modifier un livre | Bibliothécaire |
| DELETE | `/livres/{id}` | Désactiver un livre | Bibliothécaire |

### Exemplaires

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/exemplaires` | Tous les exemplaires | Authentifié |
| GET | `/exemplaires/{id}` | Un exemplaire | Authentifié |
| GET | `/exemplaires/livre/{livreId}` | Exemplaires d'un livre | Authentifié |
| GET | `/exemplaires/livre/{livreId}/disponibles` | Exemplaires disponibles | Authentifié |
| POST | `/exemplaires/livre/{livreId}` | Créer un exemplaire | Bibliothécaire |
| PATCH | `/exemplaires/{id}/etat` | Changer l'état | Bibliothécaire |
| DELETE | `/exemplaires/{id}` | Supprimer un exemplaire | Bibliothécaire |

### Utilisateurs

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/utilisateurs` | Tous les utilisateurs | Bibliothécaire |
| GET | `/utilisateurs/page?page=0&size=10` | Liste paginée | Bibliothécaire |
| GET | `/utilisateurs/{id}` | Un utilisateur | Bibliothécaire |
| POST | `/utilisateurs` | Créer un utilisateur | Bibliothécaire |
| PUT | `/utilisateurs/{id}` | Modifier un utilisateur | Bibliothécaire |
| DELETE | `/utilisateurs/{id}` | Désactiver un utilisateur | Bibliothécaire |

### Emprunts

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/emprunts` | Tous les emprunts | Bibliothécaire |
| GET | `/emprunts/{id}` | Un emprunt | Bibliothécaire |
| GET | `/emprunts/en-retard` | Emprunts en retard | Bibliothécaire |
| GET | `/emprunts/utilisateur/{id}` | Emprunts d'un utilisateur | Propriétaire ou Bibliothécaire |
| GET | `/emprunts/utilisateur/{id}/en-cours` | Emprunts en cours | Propriétaire ou Bibliothécaire |
| POST | `/emprunts` | Créer un emprunt | Bibliothécaire |
| PUT | `/emprunts/{id}/retour` | Enregistrer un retour | Bibliothécaire |
| PUT | `/emprunts/retards/update` | Forcer la mise à jour des retards | Bibliothécaire |

### Réservations

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/reservations` | Toutes les réservations | Bibliothécaire |
| GET | `/reservations/{id}` | Une réservation | Bibliothécaire |
| GET | `/reservations/utilisateur/{id}` | Réservations d'un utilisateur | Propriétaire ou Bibliothécaire |
| GET | `/reservations/utilisateur/{id}/en-attente` | Réservations en attente | Propriétaire ou Bibliothécaire |
| POST | `/reservations?utilisateurId=&livreId=` | Créer une réservation | Bibliothécaire |
| PUT | `/reservations/{id}/annuler` | Annuler une réservation | Bibliothécaire |
| PUT | `/reservations/confirmer/{livreId}` | Confirmer manuellement | Bibliothécaire |

### Amendes

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/amendes` | Toutes les amendes | Bibliothécaire |
| GET | `/amendes/{id}` | Une amende | Bibliothécaire |
| GET | `/amendes/utilisateur/{id}` | Amendes d'un utilisateur | Propriétaire ou Bibliothécaire |
| GET | `/amendes/utilisateur/{id}/en-attente` | Amendes impayées | Propriétaire ou Bibliothécaire |
| POST | `/amendes/emprunt/{empruntId}` | Créer une amende manuellement | Bibliothécaire |
| PATCH | `/amendes/{id}/payee` | Marquer comme payée | Bibliothécaire |
| PATCH | `/amendes/{id}/annuler` | Annuler une amende | Bibliothécaire |

### Paiements

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/paiements` | Tous les paiements | Bibliothécaire |
| GET | `/paiements/{id}` | Un paiement | Bibliothécaire |
| GET | `/paiements/utilisateur/{id}` | Paiements d'un utilisateur | Propriétaire ou Bibliothécaire |
| POST | `/paiements/abonnement/{id}?methodePaiement=ESPECES` | Payer un abonnement | Bibliothécaire |
| POST | `/paiements/amende/{id}?methodePaiement=MOBILE_MONEY` | Payer une amende | Bibliothécaire |
| PATCH | `/paiements/{id}/annuler` | Annuler un paiement | Bibliothécaire |

### Abonnements

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/abonnements` | Tous les abonnements | Bibliothécaire |
| GET | `/abonnements/{id}` | Un abonnement | Bibliothécaire |
| GET | `/abonnements/utilisateur/{id}` | Abonnements d'un utilisateur | Propriétaire ou Bibliothécaire |
| GET | `/abonnements/utilisateur/{id}/actif` | A un abonnement actif ? | Propriétaire ou Bibliothécaire |
| POST | `/abonnements/utilisateur/{id}` | Créer un abonnement | Bibliothécaire |
| PATCH | `/abonnements/{id}/statut?nouveauStatut=PAYE` | Changer le statut | Bibliothécaire |
| DELETE | `/abonnements/{id}` | Supprimer un abonnement | Bibliothécaire |

### Notifications

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/notifications/utilisateur/{id}` | Notifications d'un utilisateur | Propriétaire ou Bibliothécaire |
| GET | `/notifications/utilisateur/{id}/non-lues` | Notifications non lues | Propriétaire ou Bibliothécaire |
| GET | `/notifications/utilisateur/{id}/count` | Nombre non lues (badge) | Propriétaire ou Bibliothécaire |
| POST | `/notifications/utilisateur/{id}` | Créer une notification | Bibliothécaire |
| PATCH | `/notifications/{id}/lue` | Marquer comme lue | Authentifié |
| PATCH | `/notifications/utilisateur/{id}/toutes-lues` | Tout marquer comme lu | Propriétaire ou Bibliothécaire |
| DELETE | `/notifications/{id}` | Supprimer une notification | Bibliothécaire |

### Historique

| Méthode | Route | Description | Accès |
|---|---|---|---|
| GET | `/historique` | Tout l'historique | Bibliothécaire |
| GET | `/historique/{id}` | Une entrée | Bibliothécaire |
| GET | `/historique/utilisateur/{id}` | Historique d'un utilisateur | Bibliothécaire |
| GET | `/historique/livre/{id}` | Historique d'un livre | Bibliothécaire |
| GET | `/historique/type/{type}` | Par type d'action | Bibliothécaire |

---

## 7. Sécurité

### Authentification par JWT

**JWT (JSON Web Token)** = un badge numérique. Quand un utilisateur se connecte, le serveur lui remet un token chiffré contenant son identifiant, son rôle et son id. Ce token est joint à chaque requête suivante dans l'en-tête HTTP.

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Le token expire après **24 heures**. Après expiration, l'utilisateur doit se reconnecter.

### Niveaux d'accès

| Niveau | Description |
|---|---|
| **Public** | Accessible sans connexion (catalogue de livres, login) |
| **Authentifié** | Accessible à tout utilisateur connecté |
| **Propriétaire ou Bibliothécaire** | Accessible à l'utilisateur concerné (ses propres données) ou au bibliothécaire |
| **Bibliothécaire** | Réservé au rôle BIBLIOTHECAIRE uniquement |

### Vérification des droits

- **Niveau route** : `SecurityConfig.java` définit qui peut accéder à quelles routes.
- **Niveau méthode** : `@PreAuthorize("hasRole('BIBLIOTHECAIRE')")` sur chaque endpoint sensible.
- **Niveau propriété** : `SecurityUtils.verifierAccesPropriete()` vérifie qu'un utilisateur ne consulte que ses propres données.

### Mots de passe

Les mots de passe sont hachés avec **BCrypt** avant stockage. Le mot de passe en clair n'est jamais sauvegardé ni retourné par l'API (`@JsonIgnore`).

---

## 8. Automatisations planifiées

Le serveur exécute automatiquement des tâches chaque nuit à **1h du matin** (`ScheduledTasks.java`).

| Tâche | Déclencheur | Effet |
|---|---|---|
| Mise à jour des retards | Chaque nuit | Les emprunts dont la date de retour est dépassée passent au statut `EN_RETARD`. Une notification est envoyée à l'utilisateur. |
| Expiration des abonnements | Chaque nuit | Les abonnements dont la date de fin est dépassée passent de `PAYE` à `EXPIRE`. |
| Expiration des réservations | Chaque nuit | Les réservations `CONFIRMEE` non retirées après 48h passent à `ANNULEE`. L'exemplaire est libéré. La réservation suivante dans la file est confirmée automatiquement. |

---

## 9. Installation et démarrage

### Prérequis

- Java 21+
- Node.js 18+ et npm
- MySQL 8+
- Maven 3.8+

### Base de données

```sql
CREATE DATABASE bibliotheque CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Configuration backend

Dans `backend/src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bibliotheque
spring.datasource.username=TON_UTILISATEUR_MYSQL
spring.datasource.password=TON_MOT_DE_PASSE

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

app.jwt.secret=TON_SECRET_JWT_LONG_ET_ALEATOIRE
```

> Hibernate crée les tables automatiquement au premier démarrage (`ddl-auto=update`). Ne jamais créer les tables manuellement.

### Démarrage backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Le serveur démarre sur `http://localhost:8080`.

### Démarrage frontend

```bash
cd frontend
npm install
ng serve
```

L'interface est accessible sur `http://localhost:4200`.
