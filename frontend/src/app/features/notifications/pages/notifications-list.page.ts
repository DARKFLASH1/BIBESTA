import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideBell, LucideCheckCircle2, LucideX,
  LucideInfo, LucideAlertTriangle, LucideClock
} from '@lucide/angular';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';

// Correspond aux champs renvoyés par Notification.java
// (contenu / date / statut LU|NON_LU) et à l'enum Type du backend.
type TypeNotification =
  | 'EMPRUNT' | 'RETOUR' | 'RETARD' | 'RESERVATION'
  | 'RESERVATION_DISPONIBLE' | 'RESERVATION_EXPIREE'
  | 'AMENDE' | 'PAIEMENT' | 'ANNULATION'
  | 'RAPPEL_RETOUR' | 'ABONNEMENT_EXPIRE' | 'RAPPEL_ABONNEMENT';

interface Notification {
  id: number;
  type: TypeNotification;
  contenu: string;
  date: string;
  statut: 'LU' | 'NON_LU';
}

@Component({
  selector: 'app-notifications-list',
  standalone: true,
  imports: [
    CommonModule,
    LucideBell, LucideCheckCircle2, LucideX,
    LucideInfo, LucideAlertTriangle, LucideClock
  ],
  templateUrl: './notifications-list.page.html',
  styleUrl: './notifications-list.page.scss'
})
export class NotificationsListPage implements OnInit {

  private http        = inject(HttpClient);
  private authService = inject(AuthService);

  utilisateurId     = this.authService.getCurrentUserId();
  estBibliothecaire = this.authService.isBibliothecaire();
  notifications     = signal<Notification[]>([]);
  loading           = signal(true);
  erreur            = signal('');
  filtreActif       = signal<string>('tous');

  notificationsFiltrees = computed(() => {
    const filtre = this.filtreActif();
    if (filtre === 'tous')     return this.notifications();
    if (filtre === 'non_lues') return this.notifications().filter(n => n.statut === 'NON_LU');
    if (filtre === 'lues')     return this.notifications().filter(n => n.statut === 'LU');
    return this.notifications();
  });

  nbNonLues = computed(() =>
    this.notifications().filter(n => n.statut === 'NON_LU').length
  );

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.loading.set(true);
    this.http.get<Notification[]>(
      `${environment.apiUrl}/notifications/utilisateur/${this.utilisateurId}`
    ).subscribe({
      next: (data) => { this.notifications.set(data); this.loading.set(false); },
      error: () => { this.erreur.set('Impossible de charger les notifications.'); this.loading.set(false); }
    });
  }

  marquerLue(notification: Notification): void {
    if (notification.statut === 'LU') return;

    this.http.patch<Notification>(
      `${environment.apiUrl}/notifications/${notification.id}/lue`, {}
    ).subscribe({
      next: (updated) => {
        this.notifications.update(list =>
          list.map(n => n.id === updated.id ? updated : n)
        );
      }
    });
  }

  marquerToutesLues(): void {
    this.http.patch(
      `${environment.apiUrl}/notifications/utilisateur/${this.utilisateurId}/toutes-lues`,
      {}
    ).subscribe({
      next: () => {
        this.notifications.update(list =>
          list.map(n => ({ ...n, statut: 'LU' as const }))
        );
      }
    });
  }

  supprimer(notification: Notification): void {
    this.http.delete(
      `${environment.apiUrl}/notifications/${notification.id}`
    ).subscribe({
      next: () => {
        this.notifications.update(list =>
          list.filter(n => n.id !== notification.id)
        );
      }
    });
  }

  // Icône selon le type de notification (aligné sur l'enum Type du backend)
  iconeType(type: string): string {
    switch (type) {
      case 'AMENDE':               return 'alertTriangle';
      case 'RETARD':
      case 'RAPPEL_RETOUR':
      case 'RAPPEL_ABONNEMENT':
      case 'ABONNEMENT_EXPIRE':    return 'clock';
      case 'RETOUR':
      case 'PAIEMENT':
      case 'RESERVATION_DISPONIBLE': return 'checkCircle2';
      default:                     return 'info';
    }
  }

  // Classe CSS selon le type
  classeType(type: string): string {
    switch (type) {
      case 'AMENDE':               return 'type-danger';
      case 'RETARD':
      case 'RAPPEL_RETOUR':
      case 'RAPPEL_ABONNEMENT':
      case 'ABONNEMENT_EXPIRE':    return 'type-warning';
      case 'RETOUR':
      case 'PAIEMENT':
      case 'RESERVATION_DISPONIBLE': return 'type-success';
      default:                     return 'type-info';
    }
  }
}