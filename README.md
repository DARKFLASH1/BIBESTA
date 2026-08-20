# 📚 BIBESTA - Système de Gestion de Bibliothèque

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-22.1.2-red.svg)](https://angular.io/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

Application complète de gestion de bibliothèque avec authentification JWT, rôles utilisateurs et suivi des emprunts.

## 🏗️ Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Angular   │────▶│ Spring Boot  │────▶│   MySQL     │
│  (Port 80)  │     │  (Port 8080) │     │ (Port 3306) │
│   Frontend  │◀────│   Backend    │◀────│  Database   │
└─────────────┘     └──────────────┘     └─────────────┘
```

### Technologies Utilisées

**Backend:**
- Spring Boot 3.3.5 (version stable recommandée pour portfolio)
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8.0
- Maven
- Java 17

**Frontend:**
- Angular 22.1.2
- RxJS
- HttpClient
- Guards (Auth & Role)
- Interceptors (Auth & Error)

**DevOps:**
- Docker & Docker Compose
- Nginx (production frontend)

## 🚀 Démarrage Rapide

### Option 1: Avec Docker (Recommandé)

```bash
# Lancer tous les services (MySQL, Backend, Frontend)
docker-compose up --build

# Ou en arrière-plan
docker-compose up -d --build
```

L'application sera accessible sur:
- **Frontend:** http://localhost:80
- **Backend API:** http://localhost:8080/api
- **MySQL:** localhost:3306

### Option 2: Manuellement (Développement)

#### Prérequis
- Java 17+
- Node.js 20+
- Maven 3.8+
- MySQL 8.0

#### 1. Base de données
```sql
CREATE DATABASE bibliotheque;
-- Les tables et données seront créées automatiquement au démarrage
```

#### 2. Backend
```bash
cd Backend
./mvnw spring-boot:run
# ou
mvn spring-boot:run
```

Variables d'environnement optionnelles:
```bash
export DB_USERNAME=votre_user
export DB_PASSWORD=votre_password
export JWT_SECRET=votre_cle_secrete
```

#### 3. Frontend
```bash
cd frontend
npm install
npm start -- --proxy-config proxy.conf.json
```

L'application sera accessible sur http://localhost:4200

## 👥 Identifiants de Test

| Rôle | Email | Mot de passe | Permissions |
|------|-------|--------------|-------------|
| Administrateur | admin@bibesta.com | admin123 | Accès complet |
| Bibliothécaire | biblio@bibesta.com | biblio123 | Gestion livres, emprunts, utilisateurs |
| Étudiant | etudiant@bibesta.com | user123 | Emprunter, consulter historique |
| Enseignant | prof@bibesta.com | prof123 | Emprunter (quota étendu) |
| Public | public@bibesta.com | public123 | Emprunter (quota limité) |

## 📋 Fonctionnalités Implémentées

### Backend
- ✅ Authentification JWT avec refresh token
- ✅ Gestion des rôles (ADMIN, BIBLIOTHECAIRE, ETUDIANT, ENSEIGNANT, PUBLIC)
- ✅ CRUD complet: Livres, Exemplaires, Utilisateurs, Emprunts, Réservations, Amendes, Paiements, Abonnements, Notifications
- ✅ Règles métier: quotas d'emprunts, amendes automatiques, file d'attente de réservations
- ✅ Scripts SQL d'initialisation (schema.sql + data.sql)
- ✅ Tests unitaires (JUnit 5 + Mockito)
- ✅ CORS configuré pour localhost:4200
- ✅ Endpoint statistiques dashboard
- ✅ Endpoint emprunts récents

### Frontend
- ✅ Services HTTP pour toutes les entités
- ✅ Auth Interceptor (ajout automatique du token JWT)
- ✅ Error Interceptor (gestion globale des erreurs)
- ✅ Guards: auth.guard.ts, role.guard.ts
- ✅ Composants: Livres, Emprunts, Réservations, Amendes, Utilisateurs, Dashboard
- ✅ Pas de MOCK DATA - Tous les appels API sont réels
- ✅ Proxy configuration pour développement
- ✅ Typage fort TypeScript (interfaces pour toutes les réponses API)

## 📁 Structure du Projet

```
/workspace
├── README.md                 # Ce fichier
├── docker-compose.yml        # Orchestration Docker
├── Backend/
│   ├── Dockerfile           # Image Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/BIBESTA/
│       │   ├── controller/   # 12 contrôleurs REST
│       │   ├── service/      # Logique métier
│       │   ├── repository/   # Spring Data JPA
│       │   ├── model/        # Entités JPA
│       │   ├── dto/          # Data Transfer Objects
│       │   ├── security/     # JWT, SecurityConfig
│       │   └── exception/    # Gestion d'erreurs
│       └── resources/
│           ├── application.properties
│           └── db/
│               ├── schema.sql
│               └── data.sql
└── frontend/
    ├── Dockerfile           # Image Angular + Nginx
    ├── nginx.conf           # Configuration Nginx
    ├── proxy.conf.json      # Proxy dev vers backend
    ├── angular.json
    └── src/app/
        ├── core/
        │   ├── guards/      # auth.guard, role.guard
        │   ├── interceptors/# auth.interceptor, error.interceptor
        │   ├── services/    # 12 services HTTP
        │   └── models/      # Interfaces TypeScript
        ├── features/        # Modules fonctionnels
        │   ├── auth/
        │   ├── books/
        │   ├── loans/
        │   ├── reservations/
        │   ├── fines/
        │   ├── users/
        │   ├── subscriptions/
        │   ├── notifications/
        │   └── bibliothecaire/
        │       └── dashboard/
        └── shared/
            └── components/
```

## 🔧 Commandes Utiles

### Backend
```bash
# Compiler
./mvnw clean package

# Exécuter les tests
./mvnw test

# Lancer en dev
./mvnw spring-boot:run

# Vérifier les dépendances
./mvnw dependency:tree
```

### Frontend
```bash
# Installer les dépendances
npm install

# Lancer en dev avec proxy
npm start -- --proxy-config proxy.conf.json

# Build production
npm run build -- --configuration production

# Tests unitaires
npm test
```

### Docker
```bash
# Tout lancer
docker-compose up --build

# Arrêter
docker-compose down

# Voir les logs
docker-compose logs -f

# Reconstruire un service
docker-compose build backend
docker-compose up -d backend
```

## 🧪 Tests

### Backend
Les tests unitaires couvrent les règles métier critiques:
- Quota d'emprunts par rôle
- Calcul automatique des amendes
- Gestion de la file d'attente des réservations
- Validation des abonnements actifs

Exécuter: `./mvnw test`

### Frontend
Tests à implémenter pour les composants et services.

## 🛠️ Dépannage

### Erreur de connexion MySQL
```bash
# Vérifier que MySQL tourne
docker ps | grep mysql

# Ou en local
mysql -u root -p -e "SHOW DATABASES;"
```

### Erreur CORS
Vérifier que `SecurityConfig.java` autorise `http://localhost:4200`

### Token JWT expiré
Le frontend gère automatiquement le refresh via l'interceptor.

### Port déjà utilisé
```bash
# Changer le port backend dans application.properties
server.port=8081

# Changer le port frontend dans angular.json
"serve": {
  "options": {
    "port": 4201
  }
}
```

## 📊 Endpoints API Principaux

| Méthode | Endpoint | Description | Auth Requise |
|---------|----------|-------------|--------------|
| POST | /api/auth/login | Connexion utilisateur | ❌ |
| GET | /api/livres | Liste des livres | ❌ |
| GET | /api/emprunts/utilisateur/{id}/en-cours | Emprunts en cours | ✅ Propriétaire |
| POST | /api/emprunts | Créer un emprunt | ✅ BIBLIOTHECAIRE |
| GET | /api/emprunts/en-retard | Emprunts en retard | ✅ BIBLIOTHECAIRE |
| GET | /api/emprunts/recent?size=5 | Derniers emprunts | ✅ BIBLIOTHECAIRE |
| PUT | /api/emprunts/{id}/retour | Retourner un livre | ✅ BIBLIOTHECAIRE |
| GET | /api/statistiques/dashboard | Stats dashboard | ✅ BIBLIOTHECAIRE |
| GET | /api/reservations | Toutes réservations | ✅ BIBLIOTHECAIRE |
| POST | /api/reservations | Réserver un livre | ✅ Utilisateur connecté |
| GET | /api/amendes | Amendes utilisateur | ✅ Propriétaire |
| POST | /api/paiements | Payer une amende | ✅ Propriétaire |

## 📝 Notes de Développement

- **Version Spring Boot 3.3.5**: Version stable recommandée pour un portfolio. Évite les versions 4.x encore expérimentales.
- **JWT Secret**: Toujours utiliser une variable d'environnement en production.
- **Scripts SQL**: `schema.sql` et `data.sql` s'exécutent automatiquement au premier démarrage.
- **Proxy Frontend**: Le fichier `proxy.conf.json` redirige `/api` vers `http://localhost:8080`.

## 🎯 Prochaines Améliorations Possibles

- [ ] Tests d'intégration avec Testcontainers
- [ ] Swagger/OpenAPI documentation
- [ ] Refresh token automatique côté frontend
- [ ] Pagination serveur pour les grandes listes
- [ ] Websockets pour notifications en temps réel
- [ ] Export PDF des rapports
- [ ] Recherche full-text Elasticsearch

## 📄 Licence

Projet académique - Usage libre pour apprentissage.

---

**Développé avec ❤️ pour démontrer les compétences Full-Stack Java/Angular**
