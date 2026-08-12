import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideCalendarClock, LucideClock, LucideCheckCircle2,
  LucideX, LucidePlus, LucideAlertTriangle, LucideBell
} from '@lucide/angular';
import { ReservationService, Reservation } from './services/reservation.service';
import { AuthService } from '../../../core/services/auth.service';
import { LivreService } from '../../books/books-list/service/livre.service';
import { Livre } from '../../../core/models/entities.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  identifiant: string;
}

@Component({
  selector: 'app-reservations-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    LucideCalendarClock, LucideClock, LucideCheckCircle2,
    LucideX, LucidePlus, LucideAlertTriangle, LucideBell
  ],
  templateUrl: './reservations-list.page.html',
  styleUrl: './reservations-list.page.scss'
})
export class ReservationsListPage implements OnInit {

  private reservationService = inject(ReservationService);
  private authService        = inject(AuthService);
  private livreService       = inject(LivreService);
  private http               = inject(HttpClient);

  reservations  = signal<Reservation[]>([]);
  loading       = signal(true);
  erreur        = signal('');
  filtreActif   = signal<string>('tous');
  confirmationEnCours = signal<number | null>(null); // livreId en cours de confirmation

  // Modal
  modalOuvert  = signal(false);
  sauvegarde   = signal(false);
  livres       = signal<Livre[]>([]);
  utilisateurs = signal<Utilisateur[]>([]);
  livreSelectionne      = signal<number | null>(null);
  utilisateurSelectionne = signal<number | null>(null);

  estBibliothecaire = this.authService.isBibliothecaire();
  utilisateurId     = this.authService.getCurrentUserId();

  // Filtres
  reservationsFiltrees = computed(() => {
    const filtre = this.filtreActif();
    if (filtre === 'tous') return this.reservations();
    return this.reservations().filter(
      r => r.statut.toLowerCase() === filtre
    );
  });

  nbEnAttente  = computed(() =>
    this.reservations().filter(r => r.statut === 'EN_ATTENTE').length);
  nbConfirmee = computed(() =>
    this.reservations().filter(r => r.statut === 'CONFIRMEE').length);
  nbAnnulee    = computed(() =>
    this.reservations().filter(r => r.statut === 'ANNULEE').length);

  ngOnInit(): void {
    this.charger();
    if (this.estBibliothecaire) {
      this.chargerUtilisateurs();
    }
    this.chargerLivres();
  }

  charger(): void {
    this.loading.set(true);
    const obs = this.estBibliothecaire
      ? this.reservationService.getTous()
      : this.reservationService.getParUtilisateur(this.utilisateurId);

    obs.subscribe({
      next: (data) => { this.reservations.set(data); this.loading.set(false); },
      error: () => { this.erreur.set('Impossible de charger les réservations.'); this.loading.set(false); }
    });
  }

  chargerLivres(): void {
    this.livreService.getTousLesLivres().subscribe({
      next: (data) => this.livres.set(data)
    });
  }

  chargerUtilisateurs(): void {
    this.http.get<Utilisateur[]>(`${environment.apiUrl}/utilisateurs`).subscribe({
      next: (data) => this.utilisateurs.set(data)
    });
  }

  ouvrirModal(): void {
    this.livreSelectionne.set(null);
    this.utilisateurSelectionne.set(
      this.estBibliothecaire ? null : this.utilisateurId
    );
    this.modalOuvert.set(true);
  }

  fermerModal(): void { this.modalOuvert.set(false); }

  creerReservation(): void {
    const utilisateurId = this.utilisateurSelectionne();
    const livreId       = this.livreSelectionne();
    if (!utilisateurId || !livreId) return;

    this.sauvegarde.set(true);
    this.reservationService.creer(utilisateurId, livreId).subscribe({
      next: (nouvelle) => {
        this.reservations.update(list => [nouvelle, ...list]);
        this.sauvegarde.set(false);
        this.fermerModal();
      },
      error: (err) => {
        this.erreur.set(err.error?.message || 'Erreur lors de la réservation.');
        this.sauvegarde.set(false);
      }
    });
  }

  annuler(reservation: Reservation): void {
    if (!confirm(`Annuler la réservation de "${reservation.livre.titre}" ?`)) return;
    this.reservationService.annuler(reservation.id).subscribe({
      next: (updated) => {
        this.reservations.update(list =>
          list.map(r => r.id === updated.id ? updated : r)
        );
      }
    });
  }

  confirmerDisponibilite(reservation: Reservation): void {
    if (!confirm(`Confirmer la disponibilité du livre "${reservation.livre.titre}" et notifier le lecteur ?`)) return;
    this.confirmationEnCours.set(reservation.livre.id);
    this.reservationService.confirmer(reservation.livre.id).subscribe({
      next: () => {
        this.confirmationEnCours.set(null);
        // Recharge la liste pour afficher le nouveau statut CONFIRMEE
        this.charger();
      },
      error: (err) => {
        this.erreur.set(err.error?.message || 'Erreur lors de la confirmation.');
        this.confirmationEnCours.set(null);
      }
    });
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE':  return 'badge-warning';
      case 'CONFIRMEE':   return 'badge-success';
      case 'ANNULEE':     return 'badge-system';
      default:            return 'badge-system';
    }
  }

  libelleStatut(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE':  return 'En attente';
      case 'CONFIRMEE':   return 'Disponible';
      case 'ANNULEE':     return 'Annulée';
      default:            return statut;
    }
  }
}