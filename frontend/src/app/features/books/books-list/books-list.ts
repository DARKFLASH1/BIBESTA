import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideSearch,
  LucideBookOpen,
  LucidePlus,
  LucidePencil,
  LucideX,
  LucideSlidersHorizontal
} from '@lucide/angular';
import { LivreService } from './service/livre.service';
import { AuthService } from '../../../core/services/auth.service';
import { Livre } from '../../../core/models/entities.model';
import { LivreCardComponent } from '../../../shared/components/livre-card/livre-card';
@Component({
  selector: 'app-books-list',
  standalone: true,
  imports: [LivreCardComponent,
    CommonModule,
    FormsModule,
    LucideSearch,
    LucideBookOpen,
    LucidePlus,
    LucidePencil,
    LucideX,
    LucideSlidersHorizontal
  ],
  templateUrl: 'books-list.html',
  styleUrl: 'books-list.scss'
})
export class BooksListPage implements OnInit {

  private livreService = inject(LivreService);
  private authService  = inject(AuthService);

  // ── État de la page ───────────────────────────────
  livres         = signal<Livre[]>([]);    // tous les livres chargés
  loading        = signal(true);           // afficher le skeleton ?
  erreur         = signal('');             // message d'erreur réseau
  afficherFiltres = signal(false);         // panneau filtres visible ?

  // ── Formulaire de recherche ───────────────────────
  recherche = {
    titre: '',
    auteur: '',
    isbn: '',
    genre: '',
    langue: '',
    categorie: ''
  };

  // ── Modal ajout/modification ──────────────────────
  erreurModal = signal(''); 
  modalOuvert  = signal(false);
  modeEdition  = signal(false);           // false = ajout, true = modif
  livreEnCours = signal<Livre>({          // livre dans le formulaire modal
    titre: '', auteur: '', edition: '',
    categorie: '', genre: '', anneePublication: undefined,
    langue: '', isbn: '', nombrePages: undefined
  });
  sauvegarde   = signal(false);           // bouton désactivé pendant l'enregistrement

  // ── Rôle ─────────────────────────────────────────
  estBibliothecaire = this.authService.isBibliothecaire();

  // ── Statistiques rapides ──────────────────────────
  totalLivres = computed(() => this.livres().length);

  // ────────────────────────────────────────────────
  ngOnInit(): void {
    this.chargerTousLesLivres();
  }

  // Charge la liste complète au démarrage
  chargerTousLesLivres(): void {
    this.loading.set(true);
    this.erreur.set('');

    this.livreService.getTousLesLivres().subscribe({
      next: (data) => {
        this.livres.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de contacter le serveur. Vérifiez que Spring Boot est démarré.');
        this.loading.set(false);
      }
    });
  }

  // Lance la recherche avec les filtres saisis
  lancerRecherche(): void {
    // Si tous les champs sont vides → recharge tout
    const vide = Object.values(this.recherche).every(v => !v);
    if (vide) { this.chargerTousLesLivres(); return; }

    this.loading.set(true);
    this.livreService.rechercher(this.recherche).subscribe({
      next: (data) => {
        this.livres.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.erreur.set('Erreur lors de la recherche.');
        this.loading.set(false);
      }
    });
  }

  // Réinitialise les filtres
  reinitialiser(): void {
    this.recherche = { titre: '', auteur: '', isbn: '', genre: '', langue: '', categorie: '' };
    this.chargerTousLesLivres();
  }

  // ── Modal ─────────────────────────────────────────
  ouvrirModalAjout(): void {
    this.erreurModal.set(''); 
    this.modeEdition.set(false);
    this.livreEnCours.set({
      titre: '', auteur: '', edition: '',
      categorie: '', genre: '', anneePublication: undefined,
      langue: '', isbn: '', nombrePages: undefined
    });
    this.modalOuvert.set(true);
  }

  ouvrirModalModif(livre: Livre): void {
    this.erreurModal.set(''); 
    this.modeEdition.set(true);
    // Copie le livre pour ne pas modifier la liste directement
    this.livreEnCours.set({ ...livre });
    this.modalOuvert.set(true);
  }

  fermerModal(): void {
    this.modalOuvert.set(false);
  }

  sauvegarder(): void {
  const livre = this.livreEnCours();
  if (!livre.titre || !livre.auteur) {
    this.erreurModal.set('Le titre et l\'auteur sont obligatoires.');
    return;
  }

  this.sauvegarde.set(true);
  this.erreurModal.set('');

  if (this.modeEdition() && livre.id) {
    this.livreService.modifier(livre.id, livre).subscribe({
      next: (updated) => {
        this.livres.update(list => list.map(l => l.id === updated.id ? updated : l));
        this.sauvegarde.set(false);
        this.fermerModal();
      },
      error: (err) => {
        this.sauvegarde.set(false);
        this.erreurModal.set(err.error?.message || 'Erreur lors de la modification.');
      }
    });
  } else {
    this.livreService.creer(livre).subscribe({
      next: (nouveau) => {
        this.livres.update(list => [nouveau, ...list]);
        this.sauvegarde.set(false);
        this.fermerModal();
      },
      error: (err) => {
        this.sauvegarde.set(false);
        // err.error.message = ce que Spring Boot renvoie maintenant grâce au fix ci-dessus
        this.erreurModal.set(err.error?.message || 'Erreur lors de la création du livre.');
      }
    });
  }
}

  supprimer(livre: Livre): void {
    if (!livre.id) return;
    if (!confirm(`Supprimer "${livre.titre}" ?`)) return;

    this.livreService.supprimer(livre.id).subscribe({
      next: () => {
        this.livres.update(list => list.filter(l => l.id !== livre.id));
      }
    });
  }
}