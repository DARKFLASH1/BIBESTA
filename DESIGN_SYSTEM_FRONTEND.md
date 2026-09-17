# DESIGN SYSTEM — BIBESTA Frontend

> v1.0 — 2026-09-17
> Ce document définit les règles visuelles et de construction du frontend Angular de BIBESTA
> (application de gestion de bibliothèque). Il est la **référence unique** : tout composant,
> page ou token de l'interface doit s'y conformer.

---

## 1. Direction artistique

**Concept : « Bibliothèque de papier et d'encre »**

L'application évoque une bibliothèque classique, chaleureuse et moderne à la fois : le papier
ancien (fond ivoire), l'encre d'imprimerie (textes brun-noir), le laiton des étagères et des
dorures (accent). Les statuts sémantiques (disponible, en retard, amendes) restent lisibles
mais adoucis, comme des encres de couleur sur du papier.

- **Lisible avant esthétique** : contrastes AA, taille de texte confortable.
- **Chaleureux sans être vintage dépassé** : coins arrondis modérés, ombres douces, transitions courtes.
- **Domaine bibliothèque** : métaphores livresques (couverture = carte, étagère = grille, filet de livre = dégradé).

---

## 2. Palette de couleurs (tokens)

Toutes les couleurs sont déclarées en tokens CSS dans `:root` de `src/styles.scss`.
**Interdiction d'utiliser une couleur en dur (`#hex`) ailleurs que dans ce fichier.**

### 2.1 Fond & surfaces — « le papier »

| Token | Valeur | Usage |
|-------|--------|-------|
| `--bg-primary` | `#FAF6EE` | Fond global de l'application (ivoire chaud) |
| `--bg-secondary` | `#F1EADA` | En-têtes de tableaux, panneaux de filtres, zones secondaires |
| `--surface` | `#FFFFFF` | Cartes, modales, inputs, tableaux |
| `--surface-sunken` | `#F5F0E6` | Zones creuses : inputs désactivés, skeletons, fond d'application |

### 2.2 Textes — « l'encre »

| Token | Valeur | Usage | Ratio AA |
|-------|--------|-------|----------|
| `--text-main` | `#241F16` | Texte principal (sur fonds ivoire/blanc) | > 12:1 |
| `--text-muted` | `#6B5F4E` | Secondaire : auteurs, labels, sous-titres | ≈ 6:1 |
| `--text-light` | `#A69985` | **DÉCORATIF UNIQUEMENT** : placeholders, icônes inactives. Jamais pour du texte à lire | — |

### 2.3 Bordures

| Token | Valeur | Usage |
|-------|--------|-------|
| `--border-color` | `#E6DFCE` | Bordures par défaut (discrètes) |
| `--border-strong` | `#D2C6AC` | Séparation marquée, hover, drag & drop |
| `--border-focus` | `#8A6A2F` | Focus actif des inputs (laiton) |

### 2.4 Accent — « le laiton » (RÉSERVÉ aux actions)

| Token | Valeur | Usage |
|-------|--------|-------|
| `--accent` | `#8A6A2F` | Bouton primaire, lien actif, sélection active |
| `--accent-hover` | `#6E5324` | Hover du bouton primaire |
| `--accent-light` | `#F1E8D2` | Fond pastel de l'accent (badge, compteur, hover secondaire) |
| `--accent-gold` | `#C1A466` | Dégradés dorés, barres de progression, ornement |

⚠️ **Règle stricte** : le laiton est réservé aux *actions* et à l'*identité*. Jamais pour un statut.
Les statuts ont leurs propres couleurs sémantiques (§ 2.5).

### 2.5 Couleurs sémantiques — « les statuts et alertes »

| Token | Valeur | Fond (token) | Usage |
|-------|--------|--------------|-------|
| `--success` / `--success-bg` | `#2E7D46` / `#E8F5EC` | ✓ | Disponible, retourné, payé, confirmé |
| `--warning` / `--warning-bg` | `#C1690D` / `#FBEEE0` | ✓ | À rendre bientôt, en attente, abonnement expirant |
| `--danger` / `--danger-bg` | `#B23A2E` / `#FBEAE7` | ✓ | En retard, amende, annulé, erreur |
| `--info` / `--info-bg` | `#2A5C8A` / `#EAF1F8` | ✓ | En cours, notifications neutres |

**Règle** : un statut s'utilise **toujours** avec sa paire texte + fond pastel
(`color: var(--success)` sur fond `var(--success-bg)`). Jamais de couleur pure sur blanc pour un statut.

### 2.6 Sidebar et héros (zones sombres) — « l'étagère en bois »

| Token | Valeur | Usage |
|-------|--------|-------|
| `--ink-deep` | `#241F16` | Fond de la sidebar, fond du panneau login |
| `--ink-raised` | `#3D3425` | Éléments surélevés / dégradé login |
| `--ink-muted` | `#A69985` | Texte/icônes secondaires sur fond sombre |
| `--ink-light` | `#EDE3D0` | Texte principal clair sur fond sombre |
| `--ink-accent` | `#C1A466` | Icône/logo doré sur fond sombre |

> ⚠️ Remplacer l'ancienne palette Material de la sidebar (`#3E2723`, `#8D6E63`, `#D7CCC8`,
> `#A1887F`, `#C9BBA0`) par ces tokens (§ P4.1 du plan).

---

## 3. Typographie

Chargée via Google Fonts dans `index.html` (préconnect + `display=swap` optimisé par le build).

| Rôle | Police | Poids | Usage |
|------|--------|-------|-------|
| Display | **Outfit** | 500–700 | Titres `h1`–`h6`, boutons, badges, chiffres (KPI), nav |
| Corps | **Plus Jakarta Sans** | 400–600 | Texte courant, paragraphes, tableaux |
| Code | `ui-monospace / monospace` | — | ISBN, codes, données techniques |

Déclaration recommandée dans `styles.scss` :
```scss
--font-display: 'Outfit', system-ui, sans-serif;
--font-body: 'Plus Jakarta Sans', system-ui, sans-serif;
--font-mono: 'SFMono-Regular', Consolas, 'Liberation Mono', monospace;
```

### Échelle (tokens existants)

| Token | Taille | Usage |
|-------|--------|-------|
| `--text-xs` | 0.75rem | métadonnées, badges (non critique) |
| `--text-sm` | 0.875rem | labels, sous-titres, tableaux, boutons |
| `--text-base` | 1rem | texte courant, inputs |
| `--text-lg` | 1.125rem | h2 de section |
| `--text-xl` | 1.5rem | h1 de page |
| `--text-2xl` | 2rem | titres de dashboard |

**Tailles minimales** : 0.75rem jamais pour du texte à lire ; 0.875rem mini pour le corps.

---

## 4. Espacement, rayons, ombres

### 4.1 Grille d'espacement (base 4px)

`--space-1` 4 · `--space-2` 8 · `--space-3` 12 · `--space-4` 16 · `--space-6` 24 · `--space-8` 32 · `--space-12` 48

Cibles tactiles **min 44×44px** (boutons, icônes, liens nav).

### 4.2 Rayons

`--radius-sm` 8 (boutons, inputs, badges) · `--radius-md` 12 (cartes, modales) · `--radius-lg` 16 (grandes cartes)

Badges : `border-radius: 100px` (pill) comme aujourd'hui.

### 4.3 Ombres

`--shadow-sm` (cartes au repos) · `--shadow-md` (hover, modales) · `--shadow-lg`/`--shadow-xl` (modales empilées)

Une seule direction : bas ou centré, jamais top. Pas d'ambiance "singkraft".

---

## 5. Composants & éléments

### 5.1 Boutons — « actions »

| Variante | Style | Usage |
|----------|-------|-------|
| `btn-primary` | fond `--accent`, texte blanc | Action principale d'une vue / formulaire |
| `btn-secondary` | fond `--bg-secondary`, bord `--border-color` | Action alternative |
| `btn-outline` | transparent, bord `--border-color`, hover laiton | Actions fantômes, filtres |
| `btn-danger` | fond `--danger-bg`, texte `--danger`, hover danger plein | Suppression, actions destructives |
| `btn-ghost` / `btn-icon` | pas de fond, icône `--text-muted` | Actions intègues (icône seule), taille 44px |

Règles :
- Un bouton = une action. Pas de bouton avec action de navigation sauf `routerLink` stylé en `btn`.
- État désactivé : `opacity: 0.6`, `cursor: not-allowed`.
- Chargement : dots animés dans le bouton (pas de spinner girafe).
- Famille `--font-display`, `font-weight: 500`, `min-height: 44px`.

### 5.2 Badges — « statut »

`badge` + modifier : `badge-success`, `badge-warning`, `badge-danger`, `badge-info`, `badge-system` (laiton, pour concepts système : "Biblio").
Fond pastel + texte couleur de la paire sémantique. Pill 100px. Icône Lucide optionnelle avant le texte (gap 0.35rem).

### 5.3 Cartes

`card` : fond `--surface`, bord `--border-color`, `--radius-md`, `--shadow-sm`, hover → `translateY(-2px)` + `--shadow-md`.
Carte livre : ajout d'une **barre d'accent dorée** `linear-gradient(90deg, var(--accent), var(--accent-gold))` en haut — métaphore de la reliure.

### 5.4 Formulaires

- Labels : `--font-display`, `--text-xs`, uppercase, `letter-spacing: 0.05em`, couleur `--text-muted`.
- `form-control` : fond `--surface`, bord `--border-color`, focus → `--border-focus` + ring `0 0 0 3px rgba(138,106,47,.18)`.
- Erreur : `.field-error` `--danger`, `--text-sm`, sous le champ. Bannière erreur : `--danger-bg` + texte `--danger` + bord translucide.

### 5.5 Tableaux

- En-tête : fond `--bg-secondary`, `--font-display` 0.85rem uppercase, `letter-spacing .05em`, couleur `--text-muted`.
- Lignes : alternance via `hover td { background: var(--bg-primary) }`.
- Zone surlignée (focus ligne) : `--accent-light`.
- Pas de cellules débordantes : `white-space: nowrap` + `text-overflow: ellipsis` pour les longs contenus (titres).

### 5.6 Modales

- Backdrop : `rgba(36,31,22,.45)` + `backdrop-filter: blur(6px)`.
- Surface : blanc ~98%, `--radius-lg`, `--shadow-xl`, animation `slideUp` 0.3s.
- 3 zones : header / body / footer, footer **toujours** aligné à droite avec actions.
- Fermeture : icône Lucide en haut à droite + `Escape` + clic backdrop ; focus trap et retour focus à la fermeture.

### 5.7 Sidebar

Fond `--ink-deep`, texte `--ink-light`. Menu :
- item normal : texte `--ink-muted`, icône `--ink-muted` ;
- hover : fond `rgba(193,164,102,.12)`, texte `--ink-light` ;
- actif : fond `--ink-raised`, **bordure gauche 3px `--accent-gold`**, texte `--ink-light`, icône dorée.

> Confirme "l'étagère en bois" chaleureuse et élimine la palette Material incohérente.

### 5.8 Loading (skeleton)

Gradient `linear-gradient(90deg, #F1EADA 25%, #E6DFCE 50%, #F1EADA 75%)` + shimmer 1.5s.
Respecter `prefers-reduced-motion` (animation coupée, fond statique).

### 5.9 Stripes (cartes d'emprunt / réservation / amende)

Dégradés verticaux par statut, dérivés des tokens sémantiques :

| Statut | Dégradé |
|--------|---------|
| En cours / info | `#2A5C8A → #5B87AD` |
| À rendre bientôt / warning | `#C1690D → #E2924B` |
| Retourné / success | `#2E7D46 → #66A97D` |
| En retard / danger | `#B23A2E → #D9695D` |
| Annulé / system | `#6B5F4E → #A69985` |

Ces dégradés sont **décoratifs** (bordure latérale de carte), le statut est toujours répété en badge explicite à côté.

### 5.10 États vides et erreurs

- Vide : icône Lucide `--border-focus`, titre `--font-display`, message `--text-muted`, CTA éventuel.
- Une icône + 2 lignes max + 1 action max.
- Erreur de chargement : même structure, variante danger.

### 5.11 Icônes

**Lucide** (`@lucide/angular`, déjà en dépendance) = bibliothèque unique. Règles :
- taille standard 20px (navigation), 18px (dense), 24px (mobile header), icônes décoratives plus grandes.
- `stroke-width` par défaut (2), pas de remplissage sauf avatar.
- Accessibilité : `[aria-hidden]` pour icônes décoratives, `title`/`aria-label` pour icônes informatives.

---

## 6. Règles d'accessibilité (WCAG AA)

| Règle | Exigence |
|-------|----------|
| Contraste texte sur fond | ≥ 4.5:1 (texte normal), ≥ 3:1 (texte large / icônes UTC) |
| Cible tactile | min 44×44px |
| Focus visible | `outline: 2px solid var(--accent)`, `outline-offset: 2px` via `:focus-visible` |
| Mouvement réduit | `prefers-reduced-motion` → coupe animations/transitions |
| Formulaires | `<label for>` liés aux inputs, `aria-describedby` sur erreurs, `autocomplete` adapté |
| Modales | focus trap, `aria-modal`, `Escape` pour fermer |
| Couleurs seules | jamais de couleur seule pour informer → toujours icône/texte associé |

---

## 7. Règles d'implémentation (Angular)

1. **Tokens uniquement** : aucune valeur hex en dur dans un composant — tout passe par les variables CSS de `styles.scss`.
2. **Styles globaux vs locaux** : boutons, badges, modales, skeletons, card → dans `styles.scss` (une seule définition). Pas de redéfinition locale par page.
3. **View encapsulation** : `styleUrl` Angular par composant pour les styles *spécifiques à la page* uniquement.
4. **Ne pas réinventer** : réutiliser les classes globales (`.btn`, `.badge`, `.form-control`, `.card`, `.modal`, `.skeleton`).
5. **Responsive** : breakpoints existants — 1024px (sidebar off-canvas) et 768px (empilement cartes/formulaires). Mobile-first pour tout nouveau composant.
6. **Signal & standalone** : nouveau code conforme (signals pour état, composants standalone — déjà en place).

---

## 8. Audit d'écart actuel (à corriger — lié au plan v3 P4)

| Fichier | Écart |
|---------|-------|
| `main-layout.component.scss` | Sidebar en palette Material (`#3E2723`, `#8D6E63`, `#D7CCC8`, `#A1887F`, `#C9BBA0`) → tokens §2.6 |
| `books-list.scss`, `reports-dashboard.page.scss` | `#C1A466`, dégradés en dur → `--accent-gold` |
| tous les `.scss` | gradients skeleton `#F5F2EA`/`#EFEBE0` → tokens §2.6 ou `--bg-secondary` |
| plusieurs pages | `.btn`, `.badge`, `.modal`, `.skeleton` dupliqués → extraire dans `styles.scss` (P4.5) |
| `index.html:5` | `<title>Frontend</title>` → « BIBESTA — Gestion de bibliothèque » |