# PLAN DE CORRECTIONS — BIBESTA v3

> Audit DevOps senior — 2026-09-17
> Base de travail : analyse du code, des configs, de l'historique git et de la doc.
> Les plans v1/v2 (`PLAN_DE_CORRECTION.md`, `PLAN_CORRECTIONS_AUDIT.md`) couvrent le code applicatif ; ce v3 traite **sécurité, infrastructure, CI/CD, design** et les points résiduels.

---

## PHASE 0 — Sécurité critique (à faire aujourd'hui)

| Ref. | Action | Localisation |
|------|--------|--------------|
| **P0.1** | Retirer les secrets en clair de `docker-compose.yml` (MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD, JWT_SECRET, DB_USERNAME) → variables d'environnement via un fichier `.env` non versionné + placeholder `${...}` | `docker-compose.yml:9,12,37-41` |
| **P0.2** | Ajouter `.env` aux `.gitignore` (déjà partiellement couvert par `*.env.*`) + créer un `.env.example` versionné | racine |
| **P0.3** | **Rotation immédiate** des secrets exposés dans git history : `yellowflash`, `changez_moi_en_dev...`, noyau git. S'ils ont été utilisés hors dev local, révoquer. | historique commits `456690c`→`fbd34f2` |
| **P0.4** | Supprimer les credentials commités de `application-dev.properties` et le basculer sur `${ENV}` + `.env` dev uniquement | `application-dev.properties:7-10` |
| **P0.5** | Ne plus publier le port MySQL sur l'hôte : retirer `ports: 3306:3306` (accès uniquement via réseau Docker interne) | `docker-compose.yml:14` |
| **P0.6** | Réparer le healthcheck backend : ajouter `spring-boot-starter-actuator` au pom + corriger `curl` (utiliser `wget`/`sh -c` comme l'image alpine n'a pas `curl`) | `pom.xml`, `docker-compose.yml:47` |

**Livrable P0 :** `docker compose config` ne doit exposer aucune secret ; `docker compose up` doit montrer le backend `healthy`.

---

## PHASE 1 — CI/CD & Build (1-2 jours)

| Ref. | Action |
|------|--------|
| **P1.1** | Créer `.github/workflows/ci.yml` : jobs `backend` (mvn verify + tests H2), `frontend` (npm ci, build prod + lint), triggers push/PR branch `dev` et `main` |
| **P1.2** | Sortir `-DskipTests` du Dockerfile : les tests s'exécutent en CI ; le Dockerfile rebuild avec `mvn package` (tests déjà validés) |
| **P1.3** | Ajouter `.dockerignore` (`Backend/` et `frontend/`) pour exclure `target/`, `node_modules/`, `dist/`, `.git`, logs |
| **P1.4** | Épingler les images : `mysql:8.4` (LTS), `maven:3.9-eclipse-temurin-17` → digest ou tag à patch fixe, `node:22-alpine` → `node:22-bookworm-slim` ou digest, `nginx:alpine` → version patchée |
| **P1.5** | Activer le scan de vulnérabilités (Trivy via action GitHub) sur les images avant push registry |
| **P1.6** | Configurer un registry privé (GHCR) + tag `${{ github.sha }}` + déploiement `docker compose` sur prod (jeu d'env dédié) |

---

## PHASE 2 — Backend (sécurité & robustesse)

| Ref. | Action | Localisation |
|------|--------|--------------|
| **P2.1** | `spring.jpa.show-sql=false` et `format_sql=false` dans `application.properties` (activer uniquement en profil dev) | `application.properties:29-32` |
| **P2.2** | `spring.jpa.open-in-view=false` + audit des entités retournées en `@EntityGraph`/JOIN FETCH pour éviter les `LazyInitializationException` | `application.properties` |
| **P2.3** | En prod : `spring.jpa.hibernate.ddl-auto=validate` (ou mieux Flyway/Liquibase). **Plan B minimal :** garder `update` et documenter le risque | `application.properties:26` |
| **P2.4** | Rate limiting / protection brute-force sur `/api/auth/login` (Bucket4j, Spring Security, ou simple compteur Redis/DB + lock) — item P1.6 en attente | `SecurityConfig`, `AuthController` |
| **P2.5** | Refresh token côté backend (rotation, stockage hashé, révocation) ou réduire le TTL JWT à 30-60 min sans "remember me" | `JwtUtil.java:19` |
| **P2.6** | Désactiver l'oracle "compte inactif" au login (réponse 403 distincte) → code 401 uniforme, ou activer seulement après 2e facteur | `AuthController.java:78-82` |
| **P2.7** | Remplacer le dummy-bcrypt hardcodé dans la mitigation timing par un hachage pré-brûlé généré à l'init | `AuthController.java:66-68` |
| **P2.8** | CORS : rendre les origines configurables par env (`app.cors.allowed-origins`), ne pas hardcoder `localhost:4200` | `SecurityConfig.java:81` |
| **P2.9** | JDBC URL : `useSSL` et `allowPublicKeyRetrieval` via variables d'env, pas en dur | `application.properties:9` |
| **P2.10** | Uniformiser le modèle DTO : ne jamais retourner d'entité JPA brute (EmpruntController GET/{id} & PUT/{id}/retour → `EmpruntResponse`). Éviter la fuite de données liées et les lazy-loads | `EmpruntController` |
| **P2.11** | `/@Data` sur entités avec relations lazy → `@Getter/@Setter` uniquement + `@ToString(onlyExplicitlyIncluded=true)`, éviter equals/hashCode sur collections | 9 entités |
| **P2.12** | `HashUtil`: spécifier `StandardCharsets.UTF_8` (eviter dépendance au charset par défaut) | `HashUtil.java:13` |
| **P2.13** | Ajouter `spring-boot-starter-test` + quelques tests intégration (auth, emprunts) dans le Dockerfile runtime ? Non — les tests restent en CI uniquement. Point clos |

---

## PHASE 3 — Frontend (qualité & sécurité)

### Sécurité / fonctionnel
| Ref. | Action |
|------|--------|
| **P3.1** | Stocker le JWT hors `localStorage` (cookie `HttpOnly`+`Secure`+`SameSite`, ou en mémoire + refresh). Test XSS requis si localStorage conservé |
| **P3.2** | Corriger le décodage JWT base64url (`atob` ne gère pas `-`/`_`) → implémentation propre ou lib (e.g. `@panva/jose`) | `auth.service.ts:44,64` |
| **P3.3** | Ajouter `roleGuard` sur `/loans` (mes emprunts) et vérifier les guards sur toutes les routes | `app.routes.ts:24-26` |
| **P3.4** | Route 404 dédiée + redirect post-login selon rôle (biblio → dashboard, user → books) | `app.routes.ts:67`, `auth.guard.ts:14` |
| **P3.5** | Supprimer la promesse "refresh token" du README (ou implémenter réellement P2.5) | `README.md:109,255` |

### Qualité / build
| Ref. | Action |
|------|--------|
| **P3.6** | Installer et configurer ESLint + `@angular-eslint` (plan `lint` dans angular.json) — item P1.14 en attente |
| **P3.7** | Ajouter le runner de tests front (vitest ou karma+jasmin) + tests d'un premier composant (login), car `node test` existe mais aucune dépendance ne permet de l'exécuter |
| **P3.8** | Ajouter `ngx-toastr` CSS dans `styles` de `angular.json` si la lib est utilisée (sinon la retirer) |
| **P3.9** | Supprimer commentaires de debug `// ← ajouté`, imports inutiles, `console.error` du production interceptor, code mort (`noImplicitDefaults`, DTOs inutilisés) |
| **P3.10** | `<title>` descriptif : « BIBESTA — Gestion de bibliothèque » + meta description | `index.html:5` |
| **P3.11** | `nomiImplicitDefaults` : supprimer les DTO inutilisés (`LivreRequest`, `ReservationRequest`, `dto.auth.LoginRequest`) |
| **P3.12** | Corriger le chemin d'import `'.././../../../../environments/environment'` | `reservation.service.ts:4` |

---

## PHASE 4 — Design system & UI cohérence

> Voir le fichier `DESIGN_SYSTEM_FRONTEND.md` pour les spécifications complètes.

| Ref. | Action |
|------|--------|
| **P4.1** | Refonte de la sidebar : remplacer la palette Material (marron `#3E2723`, rose-beige `#8D6E63`, `#D7CCC8`, `#A1887F`) par le thème "encre + laiton" du design system tokens | `main-layout.component.scss` |
| **P4.2** | Centraliser les couleurs hex hardcodées des composants en tokens CSS (`#C1A466`, `#F5F2EA`, `#EFEBE0`, stripes...) | tous les `.scss` |
| **P4.3** | Définir les 3 variantes de police en tokens (`--font-display`, `--font-body`, `--font-mono`) avec fallback système |
| **P4.4** | Composants à unifier : boutons (primary/secondary/danger/outline/ghost), badges, tables, modales, skeletons, empty-states, stripes cartes emprunt — via tokens uniquement |
| **P4.5** | Supprimer le CSS dupliqué : `.btn`, `.badge`, `.modal`, `.skeleton` sont rédéfinis dans chaque page → les extraire en classes globales `styles.scss` |
| **P4.6** | Ajouter les headers de sécurité nginx (CSP, X-Frame-Options, HSTS, nosniff) + `Cache-Control` pour les assets hashés | `nginx.conf` |
| **P4.7** | Autoload police : `font-display: swap` + préconnect déjà présent — vérifier `Optimization fonts` du build prod |

---

## PHASE 5 — Docs & hygiène

| Ref. | Action |
|------|--------|
| **P5.1** | Corriger le README : Angular 22 (pas 19), Java 17 (pas 21), frontend sur `http://localhost:4200` (pas 80), supprimer les références à `schema.sql`/`data.sql` supprimés, retirer la promesse refresh-token |
| **P5.2** | `Backend/README.md` : pipeline de run basé en utilisant `.env` (P0.2), pas de credentials en dur dans `application.properties` |
| **P5.3** | Créer `docs/CONTRIBUTING.md` : conventions de commit, workflows Git Flow, checklist PR (tests, lint, secret scan) |
| **P5.4** | Uniformiser le nommage Maven `com.bibliotheque:bibliotheque` vs package Java `com.example.BIBESTA` (P3 backlog) |
| **P5.5** | Nettoyer la duplication `PLAN_*` : fusionner le contenu résiduel dans ce v3 ou archiver les anciens (une seule source de vérité) |

---

## ANNEXE — Matrice de priorisation

| Priorité | Critère | Échéance conseillée |
|----------|---------|---------------------|
| P0 | Fuite de données / accès non autorisé | Immédiat |
| P1 | Build & déploiement non reproductibles | 1-2 j |
| P2 | Robustesse API & sécurité applicative | 1 sem. |
| P3 | Qualité frontend & garde-fous | 1 sem. |
| P4 | Cohérence visuelle | 2-3 j |
| P5 | Documentation | Continu |