import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideBookmark, LucideClock, LucideCheckCircle2,
  LucideAlertTriangle, LucideCalendar, LucideUndo2,
  LucidePlus, LucideX, LucideSearch, LucideRefreshCw
} from '@lucide/angular';
import { EmpruntService, EmpruntResponse } from './services/emprunt.service';
import { LivreService } from '../../books/books-list/service/livre.service';
import { Livre } from '../../../core/models/entities.model';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';

interface Exemplaire {
  id: number;
  numExemplaire: string;
  etat: string;
}

interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  identifiant: string;
  role: string;
}

@Component({
  selector: 'app-manage-loans',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    LucideBookmark, LucideClock, LucideCheckCircle2,
    LucideAlertTriangle, LucideCalendar, LucideUndo2,
    LucidePlus, LucideX, LucideSearch, LucideRefreshCw
  ],
  templateUrl: './manage-loans.page.html',
  styleUrl: './manage-loans.page.scss'
})
export class ManageLoansPage implements OnInit {

  private empruntService = inject(EmpruntService);
  private livreService   = inject(LivreService);
  private http           = inject(HttpClient);

  // ── État principal ────────────────────────────────
  emprunts  = signal<EmpruntResponse[]>([]);
  loading   = signal(true);
  erreur    = signal('');

  // ── Filtre onglets ────────────────────────────────
  filtreActif = signal<string>('tous');

  empruntsFiltres = computed(() => {
    const filtre = this.filtreActif();
    if (filtre === 'tous') return this.emprunts();
    if (filtre === 'en_retard') return this.emprunts().filter(e => e.statut === 'EN_RETARD');
    if (filtre === 'en_cours')  return this.emprunts().filter(e => e.statut === 'EN_COURS');
    if (filtre === 'retourne')  return this.emprunts().filter(e => e.statut === 'RETOURNE');
    return this.emprunts();
  });

  nbEnCours  = computed(() => this.emprunts().filter(e => e.statut === 'EN_COURS').length);
  nbEnRetard = computed(() => this.emprunts().filter(e => e.statut === 'EN_RETARD').length);
  nbRetournes = computed(() => this.emprunts().filter(e => e.statut === 'RETOURNE').length);

  // ── Modal création emprunt ────────────────────────
  modalOuvert   = signal(false);
  sauvegarde    = signal(false);
  retourEnCours = signal<number | null>(null);

  // Données pour le formulaire
  livres       = signal<Livre[]>([]);
  exemplaires  = signal<Exemplaire[]>([]);
  utilisateurs = signal<Utilisateur[]>([]);

  // Valeurs sélectionnées dans le formulaire
  livreSelectionne      = signal<number | null>(null);
  exemplaireSelectionne = signal<number | null>(null);
  utilisateurSelectionne = signal<number | null>(null);

  ngOnInit(): void {
    this.charger();
    this.chargerUtilisateurs();
    this.chargerLivres();
  }

  charger(): void {
    this.loading.set(true);
    this.empruntService.getTous().subscribe({
      next: (data) => { this.emprunts.set(data); this.loading.set(false); },
      error: () => { this.erreur.set('Impossible de charger les emprunts.'); this.loading.set(false); }
    });
  }

  chargerUtilisateurs(): void {
    this.http.get<Utilisateur[]>(`${environment.apiUrl}/utilisateurs`).subscribe({
      next: (data) => this.utilisateurs.set(data)
    });
  }

  chargerLivres(): void {
    this.livreService.getTousLesLivres().subscribe({
      next: (data) => this.livres.set(data)
    });
  }

  // Quand un livre est sélectionné → charge ses exemplaires disponibles
  onLivreChange(livreId: number): void {
    this.livreSelectionne.set(livreId);
    this.exemplaireSelectionne.set(null);
    this.exemplaires.set([]);

    if (!livreId) return;

    this.http.get<Exemplaire[]>(
      `${environment.apiUrl}/exemplaires/livre/${livreId}/disponibles`
    ).subscribe({
      next: (data) => this.exemplaires.set(data)
    });
  }

  ouvrirModal(): void {
    this.livreSelectionne.set(null);
    this.exemplaireSelectionne.set(null);
    this.utilisateurSelectionne.set(null);
    this.exemplaires.set([]);
    this.modalOuvert.set(true);
  }

  fermerModal(): void { this.modalOuvert.set(false); }

  creerEmprunt(): void {
    const utilisateurId = this.utilisateurSelectionne();
    const exemplaireId  = this.exemplaireSelectionne();

    if (!utilisateurId || !exemplaireId) return;

    this.sauvegarde.set(true);
    this.empruntService.creer({ utilisateurId, exemplaireId }).subscribe({
      next: (nouveau) => {
        this.emprunts.update(list => [nouveau, ...list]);
        this.sauvegarde.set(false);
        this.fermerModal();
      },
      error: (err) => {
        this.erreur.set(err.error?.message || 'Erreur lors de la création.');
        this.sauvegarde.set(false);
      }
    });
  }

  retourner(emprunt: EmpruntResponse): void {
    if (!confirm(`Confirmer le retour de "${emprunt.livreTitre}" ?`)) return;
    this.retourEnCours.set(emprunt.id);
    this.empruntService.enregistrerRetour(emprunt.id).subscribe({
      next: (updated) => {
        this.emprunts.update(list => list.map(e => e.id === updated.id ? updated : e));
        this.retourEnCours.set(null);
      },
      error: () => this.retourEnCours.set(null)
    });
  }

  mettreAJourRetards(): void {
    this.empruntService.mettreAJourRetards().subscribe({
      next: () => this.charger()
    });
  }

  joursRestants(dateRetourPrevue: string): number {
    const today = new Date();
    const retour = new Date(dateRetourPrevue);
    return Math.ceil((retour.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'EN_COURS':  return 'badge-warning';
      case 'EN_RETARD': return 'badge-danger';
      case 'RETOURNE':  return 'badge-success';
      default:          return 'badge-system';
    }
  }

  libelleStatut(statut: string): string {
    switch (statut) {
      case 'EN_COURS':  return 'En cours';
      case 'EN_RETARD': return 'En retard';
      case 'RETOURNE':  return 'Retourné';
      default:          return statut;
    }
  }
}