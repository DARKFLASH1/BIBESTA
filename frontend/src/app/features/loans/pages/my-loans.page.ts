import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideBookmark, LucideClock, LucideCheckCircle2,
  LucideAlertTriangle, LucideCalendar, LucideUndo2
} from '@lucide/angular';
import { EmpruntService, EmpruntResponse } from './services/emprunt.service';
import { AuthService } from '../../../core/services/auth.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-my-loans',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    CommonModule,
    LucideBookmark, LucideClock, LucideCheckCircle2,
    LucideAlertTriangle, LucideCalendar, LucideUndo2
  ],
  templateUrl: './my-loans.page.html',
  styleUrl: './my-loans.page.scss'
})
export class MyLoansPage implements OnInit {

  private empruntService = inject(EmpruntService);
  private authService    = inject(AuthService);

  emprunts  = signal<EmpruntResponse[]>([]);
  loading   = signal(true);
  erreur    = signal('');
  retourEnCours = signal<number | null>(null); // id de l'emprunt en cours de retour
  empruntARetourner = signal<EmpruntResponse | null>(null);

  // Filtre actif : 'tous' | 'en_cours' | 'retourne' | 'en_retard'
  filtreActif = signal<string>('tous');

  // Emprunts filtrés selon l'onglet actif
  empruntsFiltres = computed(() => {
    const tous = this.emprunts();
    const filtre = this.filtreActif();
    if (filtre === 'tous') return tous;
    return tous.filter(e => e.statut.toLowerCase() === filtre);
  });

  // Compteurs pour les onglets
  nbEnCours  = computed(() => this.emprunts().filter(e => e.statut === 'EN_COURS').length);
  nbRetard   = computed(() => this.emprunts().filter(e => e.statut === 'EN_RETARD').length);
  nbRetournes = computed(() => this.emprunts().filter(e => e.statut === 'RETOURNE').length);

  // ID de l'utilisateur connecté
  private utilisateurId = this.authService.getCurrentUserId();

  // Seul le bibliothécaire peut enregistrer un retour (backend : hasRole BIBLIOTHECAIRE)
  estBibliothecaire = this.authService.isBibliothecaire();

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.empruntService.getParUtilisateur(this.utilisateurId).subscribe({
      next: (data) => {
        this.emprunts.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger vos emprunts.');
        this.loading.set(false);
      }
    });
  }

  retourner(emprunt: EmpruntResponse): void {
    this.empruntARetourner.set(emprunt);
  }

  onConfirmationRetour(confirme: boolean): void {
    const emprunt = this.empruntARetourner();
    this.empruntARetourner.set(null);
    if (!confirme || !emprunt) return;

    this.retourEnCours.set(emprunt.id);
    this.empruntService.enregistrerRetour(emprunt.id).subscribe({
      next: (updated) => {
        this.emprunts.update(list =>
          list.map(e => e.id === updated.id ? updated : e)
        );
        this.retourEnCours.set(null);
      },
      error: () => this.retourEnCours.set(null)
    });
  }

  // Calcule les jours restants ou de retard
  joursRestants(dateRetourPrevue: string): number {
    const today = new Date();
    const retour = new Date(dateRetourPrevue);
    return Math.ceil((retour.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  // Classe CSS du badge selon le statut
  badgeClass(statut: string): string {
    switch (statut) {
      case 'EN_COURS':        return 'badge-warning';
      case 'EN_RETARD':       return 'badge-danger';
      case 'RETOURNE':        return 'badge-success';
      case 'A_RENDRE_BIENTOT': return 'badge-warning';
      default:                return 'badge-system';
    }
  }

  // Libellé du statut en français
  libelleStatut(statut: string): string {
    switch (statut) {
      case 'EN_COURS':         return 'En cours';
      case 'EN_RETARD':        return 'En retard';
      case 'RETOURNE':         return 'Retourné';
      case 'A_RENDRE_BIENTOT': return 'À rendre bientôt';
      default:                 return statut;
    }
  }
}