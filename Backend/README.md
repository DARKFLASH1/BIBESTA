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

### Champs et statuts requis pour un usage réel en bibliothèque

Le MCD initial est correct sur le plan structurel, mais insuffisant pour représenter le fonctionnement réel d'une bibliothèque physique. Les ajouts suivants sont nécessaires (voir aussi l'audit §14) :

| Entité | Ajout nécessaire | Justification métier |
|---|---|---|
| **Utilisateur** | `Statut` (`ACTIF` / `SUSPENDU` / `DESACTIVE`) | Un bibliothécaire doit pouvoir bloquer un lecteur (amendes impayées, livre perdu, comportement abusif) sans supprimer son historique. Le design frontend prévoit déjà un badge "Compte désactivé" (`LucideCircleOff`) — le champ backend correspondant n'existe pas encore. |
| **Utilisateur / Rôle** | Quota d'emprunts simultanés par rôle (ex. 3 pour `PUBLIC`, 5 pour `ETUDIANT`, 10 pour `ENSEIGNANT`) | Sans plafond, un même lecteur peut vider un rayon entier. Un vrai SIGB (Système Intégré de Gestion de Bibliothèque) applique toujours une limite par profil. |
| **Exemplaire** | Séparer **état physique** (`BON_ETAT`, `USAGE`, `ENDOMMAGE`, `PERDU`) de **statut de disponibilité** (`DISPONIBLE`, `EMPRUNTE`, `RESERVE`, `EN_REPARATION`) | Un exemplaire "endommagé" doit pouvoir rester "emprunté" jusqu'à son retour — ces deux notions ne peuvent pas cohabiter dans un seul enum sans créer des états invalides. |
| **Emprunt** | Compteur/plafond de renouvellements (`nombreRenouvellements`, max. généralement 2) | Un emprunt doit pouvoir être prolongé une ou deux fois, sauf si le livre est réservé par quelqu'un d'autre — fonctionnalité standard en bibliothèque, absente du modèle actuel. |
| **Reservation** | `datePeremption` (ex. +48h après confirmation) | Une réservation "confirmée" doit expirer si le lecteur ne vient pas récupérer le livre, sinon l'exemplaire reste bloqué indéfiniment pour les autres lecteurs. |
| **Amende** | Distinction `raisonType` (`RETARD` / `DOMMAGE` / `PERTE`) en plus du texte libre `raison` | Le calcul et le montant d'une amende pour un livre perdu (remplacement) n'ont rien à voir avec un simple retard ; les mélanger dans un champ texte empêche tout reporting fiable. |
| **Paiement** | Contrainte `CHECK` SQL (`abonnement_id` XOR `amende_id`) | La règle "un paiement règle soit un abonnement, soit une amende" n'est aujourd'hui vérifiée que côté service Java — rien n'empêche une insertion directe en base ou un bug futur de violer la règle. |

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

## Règles de gestion métier (Business Rules)

Cette section documente les règles indispensables au fonctionnement réel d'une bibliothèque. Certaines sont déjà implémentées dans le backend, d'autres sont **manquantes dans le code actuel** (voir la colonne "Statut" et le détail en §14 — Audit).

| # | Règle | Statut dans le code actuel |
|---|---|---|
| RG1 | Un emprunt ne peut être créé que si l'exemplaire est `DISPONIBLE`. | ✅ Implémenté (`EmpruntService.creerEmprunt`) |
| RG2 | Un emprunt ne peut être créé que si l'utilisateur a un **abonnement actif et payé**. | ❌ Non implémenté — aucune vérification de l'abonnement lors de l'emprunt |
| RG3 | Un emprunt ne peut être créé que si l'utilisateur n'a **aucune amende impayée** au-delà d'un certain seuil. | ❌ Non implémenté |
| RG4 | Un emprunt ne peut être créé que si l'utilisateur n'a pas atteint son **quota d'emprunts simultanés**. | ❌ Non implémenté — aucune limite de nombre d'emprunts en cours |
| RG5 | Un compte `SUSPENDU`/`DESACTIVE` ne peut ni emprunter, ni réserver, ni se connecter. | ❌ Non implémenté — le champ `Statut` du compte n'existe pas |
| RG6 | Quand une réservation est confirmée, l'exemplaire concerné doit être **verrouillé** (`RESERVE`) pour que personne d'autre ne puisse l'emprunter entre-temps. | ❌ Non implémenté — l'exemplaire reste `DISPONIBLE`, voir audit §14.3 |
| RG7 | Une réservation confirmée non retirée après un délai (48h) doit expirer et libérer l'exemplaire pour le lecteur suivant dans la file d'attente. | ❌ Non implémenté — aucune tâche planifiée, aucun champ de péremption |
| RG8 | La file de réservations sur un même livre doit être servie dans l'ordre d'arrivée (FIFO). | ⚠️ Partiel — aucun tri explicite (`ORDER BY dateReservation`) n'est appliqué en base |
| RG9 | Le passage automatique d'un emprunt en retard (`EN_RETARD`), le calcul des amendes, l'expiration des abonnements et les rappels avant échéance doivent s'exécuter **chaque jour sans intervention humaine**. | ❌ Non implémenté — les méthodes existent (`mettreAJourRetards`, `expireAbonnementsDepasses`) mais **aucune tâche planifiée (`@Scheduled`) ne les déclenche** |
| RG10 | Un paiement référence exactement une `Amende` ou un `Abonnement`, jamais les deux, jamais aucun. | ⚠️ Partiel — imposé côté service Java uniquement, pas de contrainte en base |
| RG11 | Seul un `BIBLIOTHECAIRE` peut créer/modifier/supprimer un livre, un exemplaire, gérer les utilisateurs, annuler une amende ou un paiement. | ❌ Non implémenté au niveau API — voir audit §14.1 (faille critique) |
| RG12 | La suppression d'un utilisateur ou d'un livre ayant un historique d'emprunts ne doit jamais être une suppression physique (perte de traçabilité légale et de l'historique des lecteurs). | ❌ Non implémenté — suppression physique (`deleteById`) sans désactivation logique |

## Endpoints API (aperçu)

> ⚠️ Le tableau ci-dessous reflète les **routes réellement exposées dans le code** (`@RequestMapping` des contrôleurs Spring Boot), qui diffèrent de la version précédente de ce README : il n'y a **pas de préfixe `/api`**, et les noms de ressources sont en français.

| Méthode | Endpoint | Description | Accès requis (souhaité) |
|---|---|---|---|
| POST | `/auth/login` | Authentification, retourne un JWT | Public |
| GET | `/utilisateurs` | Liste des utilisateurs | Bibliothécaire uniquement |
| POST | `/utilisateurs` | Création d'un utilisateur (le rôle est fourni dans le corps de la requête) | Bibliothécaire uniquement — **actuellement ouvert à tout utilisateur authentifié, voir audit §14.1** |
| GET | `/livres` / `GET /livres/search?titre=&auteur=&isbn=&genre=&langue=&categorie=` | Catalogue / recherche de livres | Public (lecture) |
| POST | `/livres` | Ajout d'un livre | Bibliothécaire uniquement — **non appliqué actuellement** |
| POST | `/emprunts` | Créer un emprunt | Bibliothécaire (guichet) |
| PUT | `/emprunts/{id}/retour` | Enregistrer un retour | Bibliothécaire (guichet) |
| POST | `/reservations` | Réserver un livre | Lecteur authentifié |
| GET | `/amendes`, `/paiements`, `/abonnements` | Gestion financière | Bibliothécaire (écriture) / Lecteur (lecture de son propre solde) |
| GET | `/historique` | Historique des mouvements | Bibliothécaire |
| GET | `/notifications` | Notifications d'un utilisateur | Lecteur authentifié (les siennes uniquement) |

Le module de reporting/export (statistiques, export PDF/Excel) décrit en §7 n'a pas encore de contrôleur dédié dans le code actuel.

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

---

## 14. Audit du code existant — Constats d'un expert

Cette section a été rédigée après lecture du code source réel (`backend/` et `frontend/`), pas seulement de la conception. Les constats sont classés par gravité. L'objectif n'est pas de critiquer le travail déjà fourni — la base est solide (couches propres, DTO, gestion d'erreurs centralisée, hachage BCrypt, JWT, historisation) — mais de lister objectivement ce qui empêche aujourd'hui une mise en production dans une vraie bibliothèque.

### 14.1 Critique — Sécurité (à corriger avant tout déploiement)

**Aucune autorisation par rôle côté API.** `SecurityConfig` n'exige que `authenticated()` sur toutes les routes hors `/auth/**` et `GET /livres/**`. Il n'y a **aucune annotation `@PreAuthorize` ni `hasRole(...)`** dans tout le backend. Concrètement, un compte `PUBLIC` ou `ETUDIANT` authentifié peut aujourd'hui, via un simple appel API (Postman, curl, ou son propre token JWT) :
- lister tous les utilisateurs de la bibliothèque (`GET /utilisateurs`), y compris emails et contacts ;
- **créer un utilisateur avec le rôle `BIBLIOTHECAIRE`** (`POST /utilisateurs`, le champ `role` est fourni librement dans le corps de la requête) — c'est une escalade de privilèges complète ;
- supprimer n'importe quel livre, utilisateur, ou annuler n'importe quelle amende.

Le `roleGuard` Angular (`frontend/src/app/core/guards/role.guard.ts`) est purement cosmétique : il masque des liens dans l'interface, mais **ne protège rien côté serveur**. La sécurité doit toujours être imposée par le backend, jamais uniquement par le frontend.

*Recommandation :* ajouter `@PreAuthorize("hasRole('BIBLIOTHECAIRE')")` sur les endpoints d'écriture sensibles (gestion des utilisateurs, livres, exemplaires, annulation d'amendes/paiements), et restreindre les endpoints de lecture personnelle (`/emprunts`, `/notifications`, `/amendes`) à l'utilisateur concerné ou au bibliothécaire.

### 14.2 Critique — Aucune tâche planifiée (cron)

Les méthodes `EmpruntService.mettreAJourRetards()`, `AbonnementService.expireAbonnementsDepasses()` existent et sont fonctionnellement correctes, mais **rien ne les appelle automatiquement** (aucun `@Scheduled`, aucun `@EnableScheduling` sur `BibestaApplication`). En l'état, un emprunt ne passera jamais en retard, une amende ne sera jamais générée automatiquement, et un abonnement expiré restera marqué `PAYE`, **sauf si un bibliothécaire déclenche ces actions manuellement** — ce qui n'est réaliste pour aucune bibliothèque.

*Recommandation :* ajouter `@EnableScheduling` et exécuter ces méthodes via `@Scheduled(cron = "0 0 1 * * *")` (une fois par jour, nuit), ainsi qu'une tâche pour les rappels avant échéance (module 5 du README) et l'expiration des réservations confirmées (§14.3).

### 14.3 Important — La réservation ne verrouille pas l'exemplaire

Dans `ReservationService.confirmerReservationsSiDisponible()`, quand un exemplaire redevient disponible, la réservation passe en `CONFIRMEE` et une notification est envoyée ("venez le récupérer dans les 48h"), **mais l'exemplaire reste à l'état `DISPONIBLE`**. Rien n'empêche un autre lecteur d'emprunter ce même exemplaire avant que la personne qui a réservé ne se présente au guichet. La réservation, dans son état actuel, n'a donc aucune valeur contraignante — c'est une notification, pas un blocage.

*Recommandation :* faire passer l'exemplaire à un état `RESERVE` (distinct de `DISPONIBLE`) à la confirmation, et prévoir la tâche planifiée qui le repasse en `DISPONIBLE` (et relance la réservation suivante dans la file) si le lecteur ne s'est pas présenté après le délai.

Par ailleurs, `ReservationRepository.findByLivreIdAndStatut(...)` n'est trié par aucune date : "la première réservation en attente" (`enAttente.get(0)`) n'est donc pas garantie être la plus ancienne. Pour respecter le principe premier-arrivé/premier-servi attendu dans une bibliothèque, la requête doit être triée par `dateReservation ASC`.

### 14.4 Important — Emprunt sans vérification d'éligibilité du lecteur

`EmpruntService.creerEmprunt()` vérifie uniquement la disponibilité de l'exemplaire. Il ne vérifie ni :
- que l'utilisateur a un abonnement actif et payé (`AbonnementService.hasAbonnementActif` existe déjà mais n'est jamais appelé depuis `EmpruntService`) ;
- que l'utilisateur n'a pas d'amende impayée bloquante ;
- qu'il n'a pas dépassé un plafond d'emprunts simultanés ;
- que son compte n'est pas suspendu (le champ n'existe même pas encore, voir §14.6).

Dans une bibliothèque réelle, ce sont typiquement les premiers contrôles effectués au guichet avant même de regarder si un livre est disponible.

### 14.5 Modéré — Modélisation `Exemplaire.Etat`

L'énumération actuelle mélange deux notions différentes : la disponibilité (`DISPONIBLE`, `EMPRUNTE`, `RESERVE`, `EN_REPARATION`) et l'état physique (`BON_ETAT`, `MAUVAIS_ETAT`). Un même exemplaire peut pourtant être simultanément "emprunté" ET "en mauvais état" — ces deux informations doivent être portées par deux champs distincts, sinon certaines combinaisons légitimes deviennent impossibles à représenter.

### 14.6 Modéré — Aucune gestion de compte suspendu/désactivé

Le frontend prévoit déjà une icône et un badge "Compte désactivé / Bloquer" (`LucideCircleOff`, rapport de design §6), mais le modèle `Utilisateur` backend n'a aucun champ `statut`. Un bibliothécaire ne peut donc pas aujourd'hui bloquer un lecteur indélicat (livre perdu non remboursé, comportement abusif) sans supprimer purement et simplement son compte — ce qui détruit son historique.

### 14.7 Mineur — Suppression physique des données

`UtilisateurService.deleteById()` et `LivreService.deleteLivre()` effectuent une suppression physique (`repository.deleteById`). Pour une bibliothèque, la suppression d'un lecteur ou d'un livre ayant un historique d'emprunts pose un double problème : perte de traçabilité (utile en cas de litige sur un livre non rendu) et risque d'échec par contrainte de clé étrangère (`Emprunt`, `Historique`, `Amende` référencent ces entités). Une désactivation logique (`Statut = DESACTIVE` / champ `actif = false`) est préférable à une suppression réelle.

### 14.8 Mineur — Incohérences de documentation / doublons de code

- Le tableau "Endpoints API" original documentait des routes `/api/...` (ex. `/api/users`, `/api/books`) qui **n'existent pas dans le code** ; les routes réelles n'ont pas de préfixe `/api` et sont en français (`/utilisateurs`, `/livres`). Corrigé dans la section correspondante ci-dessus.
- `ReservationService.creerReservation()` envoie **deux fois** la même notification de confirmation (bloc dupliqué) — un lecteur reçoit deux notifications identiques pour une seule réservation.
- L'entité `Livre` contient deux champs (`genre`, `nombrePages`) absents du MCD documenté en §"Modèle de données" — à ajouter au MCD ou à retirer du modèle si non utilisés, pour que la documentation reste fidèle au code.
- Aucune pagination sur `GET /livres`, `GET /livres/search`, `GET /utilisateurs` : pour un catalogue de plusieurs milliers d'ouvrages, retourner la liste entière à chaque appel deviendra un problème de performance. Une pagination Spring Data (`Pageable`) est recommandée avant mise en production.

### 14.9 Synthèse — Priorités avant mise en service réelle

| Priorité | Action | Impact si non traité |
|---|---|---|
| P0 | Ajouter les vérifications de rôle (`@PreAuthorize`) sur les endpoints d'écriture | Faille de sécurité exploitable par n'importe quel compte lecteur |
| P0 | Activer `@Scheduled` pour retards, expiration d'abonnements, rappels | Le système ne détecte jamais un retard tout seul |
| P1 | Verrouiller l'exemplaire à la confirmation d'une réservation + expiration après délai | Les réservations n'ont aucune valeur contraignante |
| P1 | Vérifier abonnement actif + amendes impayées + quota avant de créer un emprunt | Le système ne peut pas faire respecter les règles de prêt d'une vraie bibliothèque |
| P2 | Ajouter le statut de compte (actif/suspendu) | Impossible de sanctionner un lecteur indélicat sans supprimer son compte |
| P2 | Séparer état physique / statut de disponibilité de l'exemplaire | États incohérents impossibles à représenter |
| P3 | Passer les suppressions physiques en désactivation logique | Perte d'historique, risques de contrainte FK |
| P3 | Pagination des listes, tri FIFO des réservations, corrections mineures listées en §14.8 | Dette technique, pas bloquant à court terme |
