import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideBadgeCheck, LucidePlus, LucideX,
  LucideCheckCircle2, LucideClock, LucideAlertTriangle
} from '@lucide/angular';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';

interface Abonnement {
  id: number;
  type: string;
  dateDebut: string;
  dateFin: string;
  montant: number;
  statutPaiement: 'PAYE' | 'EN_ATTENTE' | 'EXPIRE';
  utilisateur: {
    id: number;
    nom: string;
    prenom: string;
    identifiant: string;
  };
}

interface AbonnementForm {
  type: string;
  dateDebut: string;
  dateFin: string;
  montant: number;
  statutPaiement: string;
}

interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  identifiant: string;
}

@Component({
  selector: 'app-subscriptions-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    CommonModule, FormsModule,
    LucideBadgeCheck, LucidePlus, LucideX,
    LucideCheckCircle2, LucideClock, LucideAlertTriangle
  ],
  templateUrl: './subscriptions-list.page.html',
  styleUrl: './subscriptions-list.page.scss'
})
export class SubscriptionsListPage implements OnInit {

  private http        = inject(HttpClient);
  private authService = inject(AuthService);

  estBibliothecaire = this.authService.isBibliothecaire();
  utilisateurId     = this.authService.getCurrentUserId();

  abonnements  = signal<Abonnement[]>([]);
  loading      = signal(true);
  erreur       = signal('');
  filtreActif  = signal<string>('tous');
  abonnementASupprimer = signal<Abonnement | null>(null);

  abonnementsFiltres = computed(() => {
    const filtre = this.filtreActif();
    if (filtre === 'tous') return this.abonnements();
    return this.abonnements().filter(
      a => a.statutPaiement.toLowerCase() === filtre
    );
  });

  nbPayes    = computed(() => this.abonnements().filter(a => a.statutPaiement === 'PAYE').length);
  nbAttente  = computed(() => this.abonnements().filter(a => a.statutPaiement === 'EN_ATTENTE').length);
  nbExpires  = computed(() => this.abonnements().filter(a => a.statutPaiement === 'EXPIRE').length);

  // Modal
  modalOuvert           = signal(false);
  sauvegarde            = signal(false);
  utilisateurs          = signal<Utilisateur[]>([]);
  utilisateurSelectionne = signal<number | null>(null);

  form: AbonnementForm = {
    type: 'MENSUEL',
    dateDebut: new Date().toISOString().split('T')[0],
    dateFin: '',
    montant: 5000,
    statutPaiement: 'EN_ATTENTE'
  };

  ngOnInit(): void {
    this.charger();
    if (this.estBibliothecaire) this.chargerUtilisateurs();
  }

  charger(): void {
    this.loading.set(true);
    const url = this.estBibliothecaire
      ? `${environment.apiUrl}/abonnements`
      : `${environment.apiUrl}/abonnements/utilisateur/${this.utilisateurId}`;

    this.http.get<Abonnement[]>(url).subscribe({
      next: (data) => { this.abonnements.set(data); this.loading.set(false); },
      error: () => { this.erreur.set('Impossible de charger les abonnements.'); this.loading.set(false); }
    });
  }

  chargerUtilisateurs(): void {
    this.http.get<Utilisateur[]>(`${environment.apiUrl}/utilisateurs`).subscribe({
      next: (data) => this.utilisateurs.set(data)
    });
  }

  // Calcule automatiquement la date de fin selon le type
  onTypeChange(): void {
    const debut = new Date(this.form.dateDebut);
    if (this.form.type === 'MENSUEL') {
      debut.setMonth(debut.getMonth() + 1);
    } else if (this.form.type === 'ANNUEL') {
      debut.setFullYear(debut.getFullYear() + 1);
    } else if (this.form.type === 'TRIMESTRIEL') {
      debut.setMonth(debut.getMonth() + 3);
      this.form.montant = 12000;
    }
    if (this.form.type === 'MENSUEL') this.form.montant = 5000;
    if (this.form.type === 'ANNUEL')  this.form.montant = 15000;
    this.form.dateFin = debut.toISOString().split('T')[0];
  }

  ouvrirModal(): void {
    this.utilisateurSelectionne.set(
      this.estBibliothecaire ? null : this.utilisateurId
    );
    this.form = {
      type: 'MENSUEL',
      dateDebut: new Date().toISOString().split('T')[0],
      dateFin: '',
      montant: 5000,
      statutPaiement: 'EN_ATTENTE'
    };
    this.onTypeChange();
    this.modalOuvert.set(true);
  }

  fermerModal(): void { this.modalOuvert.set(false); }

  sauvegarder(): void {
    const uid = this.utilisateurSelectionne();
    if (!uid) return;

    this.sauvegarde.set(true);
    this.http.post<Abonnement>(
      `${environment.apiUrl}/abonnements/utilisateur/${uid}`,
      this.form
    ).subscribe({
      next: (nouveau) => {
        this.abonnements.update(list => [nouveau, ...list]);
        this.sauvegarde.set(false);
        this.fermerModal();
      },
      error: (err) => {
        this.erreur.set(err.error?.message || 'Erreur lors de la création.');
        this.sauvegarde.set(false);
      }
    });
  }

  payerAbonnement(abonnement: Abonnement): void {
    const params = new HttpParams().set('nouveauStatut', 'PAYE');
    this.http.patch<Abonnement>(
      `${environment.apiUrl}/abonnements/${abonnement.id}/statut`,
      null, { params }
    ).subscribe({
      next: (updated) => {
        this.abonnements.update(list =>
          list.map(a => a.id === updated.id ? updated : a)
        );
      },
      error: (err) => this.erreur.set(err.error?.message || 'Erreur.')
    });
  }

  supprimer(abonnement: Abonnement): void {
    this.abonnementASupprimer.set(abonnement);
  }

  onConfirmationSupprimer(confirme: boolean): void {
    const abonnement = this.abonnementASupprimer();
    this.abonnementASupprimer.set(null);
    if (!confirme || !abonnement) return;
    this.http.delete(`${environment.apiUrl}/abonnements/${abonnement.id}`).subscribe({
      next: () => this.abonnements.update(list => list.filter(a => a.id !== abonnement.id)),
      error: (err) => this.erreur.set(err.error?.message || 'Erreur.')
    });
  }

  // Vérifie si l'abonnement est actif (date de fin dans le futur)
  estActif(abonnement: Abonnement): boolean {
    return new Date(abonnement.dateFin) > new Date()
      && abonnement.statutPaiement === 'PAYE';
  }

  joursRestants(dateFin: string): number {
    return Math.ceil(
      (new Date(dateFin).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)
    );
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'PAYE':       return 'badge-success';
      case 'EN_ATTENTE': return 'badge-warning';
      case 'EXPIRE':     return 'badge-danger';
      default:           return 'badge-system';
    }
  }

  libelleStatut(statut: string): string {
    switch (statut) {
      case 'PAYE':       return 'Payé';
      case 'EN_ATTENTE': return 'En attente';
      case 'EXPIRE':     return 'Expiré';
      default:           return statut;
    }
  }
}