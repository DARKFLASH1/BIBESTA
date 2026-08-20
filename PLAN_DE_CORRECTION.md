# 📋 Plan de Correction - BIBESTA

> Document de suivi des corrections et améliorations du projet BIBESTA
> Date de création: 2025-01-XX

---

## 🎯 État des Lieux Initial (AUDIT COMPLET)

### Backend (Spring Boot)
| Élément | État | Détails |
|---------|------|---------|
| **Version Spring Boot** | ✅ 3.3.5 | Version STABLE recommandée pour portfolio |
| **Version Java** | ✅ 17 | Compatible avec Spring Boot 3.3.x |
| **Scripts SQL** | ✅ Présents | `schema.sql` + `data.sql` dans `/src/main/resources/db/` |
| **Configuration DB** | ✅ MySQL | `application.properties` configuré |
| **Tests unitaires** | ✅ 13 tests | `EmpruntServiceTest.java` couvre règles métier critiques |
| **Context path** | ⚠️ `/api` | Défini dans `application.properties` |
| **DTOs** | ✅ Présents | Dossier `/dto` avec Mapper |
| **Security** | ✅ JWT | `SecurityConfig.java` avec CORS configuré |
| **Controllers** | ✅ 12 controllers | Tous endpoints documentés |

### Frontend (Angular)
| Élément | État | Détails |
|---------|------|---------|
| **Version Angular** | ✅ 22.1.2 | Récente mais stable |
| **Services HTTP** | ✅ 6 services | `AuthService`, `LivreService`, `EmpruntService`, `ReservationService`, `StatistiqueService`, `DashboardService` |
| **Auth Interceptor** | ✅ Présent | `auth.interceptor.ts` fonctionnel |
| **Environment** | ✅ Configuré | `apiUrl: 'http://localhost:8080/api'` |
| **Proxy** | ❌ Manquant | Aucun proxy configuré dans `angular.json` |
| **Guards** | ✅ Présents | `auth.guard.ts` et `role.guard.ts` |
| **Modèles TypeScript** | ⚠️ Partiel | `entities.model.ts` incomplet (manque Reservation, Amende, etc.) |
| **Mock Data** | ✅ Aucun | Tous les composants utilisent déjà HttpClient |

### Services Frontend MANQUANTS à créer
- [ ] `UtilisateurService` (utilisé directement via HttpClient dans users-list.page.ts)
- [ ] `AmendeService` 
- [ ] `PaiementService`
- [ ] `AbonnementService`
- [ ] `ExemplaireService`
- [ ] `NotificationService`

---

## 📅 PHASE 1 : FIABILISATION & CORRECTION CRITIQUE (Jours 1-3)

### ✅ P0.1 - Vérification Spring Boot & Java
**Statut**: ✅ COMPLÉTÉ
- Version Spring Boot: 3.3.5 (stable, recommandée pour portfolio)
- Version Java: 17 (compatible)
- **Action requise**: Aucune - configuration déjà optimale

### ✅ P0.2 - Scripts SQL d'initialisation
**Statut**: ✅ COMPLÉTÉ
- `schema.sql`: Présent et complet (10 tables + index)
- `data.sql`: Présent avec données de test (4 utilisateurs, 8 livres, 14 exemplaires)
- **Action requise**: Vérifier que Hibernate n'exécute pas schema.sql automatiquement

### ⏳ P0.3 - Configuration application.properties
**Statut**: ⚠️ CONFIGURATION ACTUELLE
```properties
# Configuration actuelle
spring.datasource.url=jdbc:mysql://localhost:3306/bibliotheque?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull
spring.datasource.username=${DB_USERNAME:darkflash1}
spring.datasource.password=${DB_PASSWORD:yellowflash}
spring.jpa.hibernate.ddl-auto=update
server.servlet.context-path=/api
app.jwt.secret=${JWT_SECRET:changez_moi_en_dev_uniquement_valeur_temporaire}
```
**Actions requises**:
- [ ] Ajouter profil `dev` avec H2 pour tests rapides
- [ ] Vérifier que `schema.sql` ne s'exécute pas en production
- [ ] Ajouter configuration pour logs structurés

### ⏳ P0.4 - Nettoyage du code backend
**Actions requises**:
- [ ] Supprimer imports inutilisés (vérifier avec IDE)
- [ ] Uniformiser la gestion des exceptions (`@ControllerAdvice` déjà présent)
- [ ] Ajouter logs pertinents dans les Services (pas Controllers)

---

## 🔗 PHASE 2 : INTÉGRATION FRONTEND-BACKEND (Jours 4-7)

### ⏳ P1.1 - Configuration Proxy/CORS
**Backend (SecurityConfig.java)**:
```java
// Déjà configuré ✅
config.setAllowedOrigins(List.of("http://localhost:4200"));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
config.setAllowCredentials(true);
```

**Frontend (À FAIRE)**:
- [ ] Créer `proxy.conf.json` à la racine du frontend
- [ ] OU configurer proxy dans `angular.json`
- [ ] Tester la connexion sans CORS errors

### ⏳ P1.2 - Services HTTP Angular
**État actuel**:
- ✅ `LivreService` - Complet avec toutes les méthodes CRUD
- ✅ `AuthService` - Login/logout avec stockage JWT
- ✅ `EmpruntService` - Complet avec interfaces typées
- ✅ `ReservationService` - Complet avec interfaces typées
- ✅ `StatistiqueService` - Présent
- ✅ `DashboardService` - Présent
- ❌ `UtilisateurService` - MANQUANT (code inline dans users-list.page.ts)
- ❌ `AmendeService` - MANQUANT
- ❌ `PaiementService` - MANQUANT
- ❌ `AbonnementService` - MANQUANT
- ❌ `ExemplaireService` - MANQUANT
- ❌ `NotificationService` - MANQUANT

**Services à créer**:
- [ ] `UtilisateurService` - Extraire HttpClient de users-list.page.ts
- [ ] `AmendeService` 
- [ ] `PaiementService`
- [ ] `AbonnementService`
- [ ] `ExemplaireService`
- [ ] `NotificationService`

### ⏳ P1.3 - Authentification complète
**État actuel**:
- ✅ Backend: `/api/auth/login` fonctionnel avec JWT
- ✅ Frontend: `AuthService` avec stockage localStorage
- ✅ Frontend: `authInterceptor` ajoute le token
- ✅ Frontend: Guards présents (`auth.guard.ts`, `role.guard.ts`)

**Actions requises**:
- [ ] Tester login avec vrais identifiants (admin/password123)
- [ ] Vérifier redirection selon rôle
- [ ] Implémenter logout automatique à l'expiration

### ⏳ P1.4 - Modèles TypeScript incomplets
**État actuel**: `entities.model.ts` contient seulement:
- ✅ Enums: Role, EtatPhysique, StatutDisponibilite, StatutEmprunt, StatutReservation, StatutAmende, TypeNotification
- ✅ Interfaces: Utilisateur, Livre, Exemplaire, Emprunt

**Interfaces manquantes à ajouter**:
- [ ] Reservation
- [ ] Amende
- [ ] Paiement
- [ ] Abonnement
- [ ] Notification
- [ ] Historique
- [ ] Statistiques (Dashboard)

---

## 🧪 PHASE 3 : QUALITÉ INDUSTRIELLE & TESTS (Jours 8-12)

### ⏳ P2.1 - Tests unitaires backend
**État actuel**: 
- ✅ `EmpruntServiceTest.java` - 13 tests (création, quota, retards, retours)
- ⚠️ Autres services non testés

**Services à tester**:
- [ ] `ReservationService` (file d'attente FIFO, expiration 48h)
- [ ] `AmendeService` (calcul 100 FCFA/jour)
- [ ] `PaiementService` (règle: soit amende soit abonnement)
- [ ] `AbonnementService` (vérification statut actif)
- [ ] Objectif: 5-6 tests critiques minimum

### ⏳ P2.2 - Tests d'intégration
**Actions requises**:
- [ ] Créer test avec `@SpringBootTest`
- [ ] Utiliser base H2 en mémoire pour tests
- [ ] Tester flux complet: Login → Emprunt → Retour

### ⏳ P2.3 - Qualité Frontend
**Actions requises**:
- [ ] Vérifier typage fort (pas de `any`)
- [ ] Interfaces TypeScript pour toutes les réponses API
- [ ] Implémenter `CanActivate` guards pour routes protégées
- [ ] Guards par rôle (ADMIN vs USER)

---

## 🐳 PHASE 4 : DEVOPS & DÉPLOIEMENT (Jours 13-15)

### ⏳ P3.1 - Dockerisation
**Fichiers à créer**:
- [ ] `Backend/Dockerfile` (Java/Maven)
- [ ] `frontend/Dockerfile` (Nginx pour static files)
- [ ] `docker-compose.yml` à la racine

**docker-compose.yml doit lancer**:
- [ ] Conteneur MySQL
- [ ] Conteneur Backend (wait-for-it.sh pour DB)
- [ ] Conteneur Frontend (optionnel)

### ⏳ P3.2 - Documentation finale
**README.md à mettre à jour**:
- [ ] Schéma d'architecture
- [ ] Commandes de lancement (avec et sans Docker)
- [ ] Identifiants de test par défaut
- [ ] Liste des fonctionnalités implémentées vs en cours

---

## 📊 RÉSUMÉ DES PRIORITÉS

| Priorité | Tâche | Estimation | Statut | Jours restants |
|----------|-------|------------|--------|----------------|
| P0 | Vérifier/Stabiliser version Spring Boot & Java | 2h | ✅ FAIT | Jour 1 |
| P0 | Script SQL d'init DB + Config application.properties | 3h | ✅ FAIT | Jour 1 |
| P0 | Nettoyer imports et uniformiser exceptions | 2h | ⏳ À faire | Jour 2 |
| P1 | Connecter Frontend au Backend (Auth + API) | 8h | ⏳ À faire | Jours 3-5 |
| P1 | Créer/Angulariser tous les services HTTP | 6h | ⏳ À faire | Jours 4-5 |
| P2 | Écrire 5 tests unitaires Backend (Règles métiers) | 4h | ⏳ Partiel | Jour 6 |
| P2 | Tests d'intégration + Guards frontend | 4h | ⏳ À faire | Jour 7 |
| P3 | Dockeriser l'application (Compose) | 3h | ⏳ À faire | Jour 8 |
| P3 | Documentation finale README.md | 2h | ⏳ À faire | Jour 9 |

---

## 🔧 COMMANDES UTILES

### Backend
```bash
# Build et tests
cd Backend
mvn clean install
mvn test

# Lancement en dev
mvn spring-boot:run

# Lancement avec profil spécifique
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend
```bash
# Installation dépendances
cd frontend
npm install

# Lancement dev server
ng serve

# Lancement avec proxy
ng serve --proxy-config proxy.conf.json

# Build production
ng build --configuration production
```

### Docker (après implémentation)
```bash
# Lancement complet
docker-compose up -d

# Arrêt
docker-compose down

# Logs
docker-compose logs -f
```

---

## 📝 NOTES D'IMPLÉMENTATION

### Points d'attention identifiés
1. **Context path**: Le backend utilise `/api` comme context path - vérifier cohérence avec frontend
2. **CORS**: Déjà configuré côté backend pour `http://localhost:4200`
3. **JWT Secret**: Utiliser variable d'environnement en production
4. **Schema.sql**: S'assurer qu'il ne s'exécute pas automatiquement en prod

### Décisions techniques
- Garder Spring Boot 3.3.5 (stable, bonne compatibilité)
- Utiliser H2 pour les tests d'intégration
- Dockeriser avec docker-compose pour simplicité
- Conserver architecture actuelle (standalone Angular 19+)

---

*Document généré automatiquement - Dernière mise à jour: $(date)*
