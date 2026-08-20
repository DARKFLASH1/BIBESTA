import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideCreditCard, LucideAlertTriangle, LucideCheckCircle2,
  LucideX, LucideInfo
} from '@lucide/angular';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';

interface Amende {
  id: number;
  montant: number;
  raison: string;
  date: string;
  statut: 'EN_ATTENTE' | 'PAYEE' | 'ANNULEE';
  emprunt: {
    id: number;
    utilisateur: {
      id: number;
      nom: string;
      prenom: string;
    };
    livre?: {
      titre: string;
    };
    exemplaire?: {
      numExemplaire: string;
      livre?: { titre: string };
    };
  };
}

interface Paiement {
  id: number;
  montant: number;
  date: string;
  methodePaiement: string;
  statut: string;
}

@Component({
  selector: 'app-fines-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    CommonModule, FormsModule,
    LucideCreditCard, LucideAlertTriangle,
    LucideCheckCircle2, LucideX, LucideInfo
  ],
  templateUrl: './fines-list.page.html',
  styleUrl: './fines-list.page.scss'
})
export class FinesListPage implements OnInit {

  private http        = inject(HttpClient);
  private authService = inject(AuthService);

  estBibliothecaire = this.authService.isBibliothecaire();
  utilisateurId     = this.authService.getCurrentUserId();

  // ── Amendes ───────────────────────────────────────
  amendes     = signal<Amende[]>([]);
  loadingAmendes = signal(true);
  erreur      = signal('');
  filtreActif = signal<string>('tous');

  amendesFiltrees = computed(() => {
    const filtre = this.filtreActif();
    if (filtre === 'tous') return this.amendes();
    return this.amendes().filter(a => a.statut.toLowerCase() === filtre);
  });

  nbEnAttente = computed(() => this.amendes().filter(a => a.statut === 'EN_ATTENTE').length);
  nbPayees    = computed(() => this.amendes().filter(a => a.statut === 'PAYEE').length);
  totalDu     = computed(() =>
    this.amendes()
      .filter(a => a.statut === 'EN_ATTENTE')
      .reduce((sum, a) => sum + a.montant, 0)
  );

  // ── Modal paiement ────────────────────────────────
  modalPaiementOuvert = signal(false);
  amendeSelectionnee  = signal<Amende | null>(null);
  methodePaiement     = signal<string>('ESPECES');

  // ── Dialog confirmation annulation ────────────────
  amendeAAnnuler = signal<Amende | null>(null);
  paiementEnCours     = signal(false);

  ngOnInit(): void {
    this.chargerAmendes();
  }

  chargerAmendes(): void {
    this.loadingAmendes.set(true);
    const url = this.estBibliothecaire
      ? `${environment.apiUrl}/amendes`
      : `${environment.apiUrl}/amendes/utilisateur/${this.utilisateurId}`;

    this.http.get<Amende[]>(url).subscribe({
      next: (data) => { this.amendes.set(data); this.loadingAmendes.set(false); },
      error: () => { this.erreur.set('Impossible de charger les amendes.'); this.loadingAmendes.set(false); }
    });
  }

  // Ouvre le modal pour payer une amende
  ouvrirModalPaiement(amende: Amende): void {
    this.amendeSelectionnee.set(amende);
    this.methodePaiement.set('ESPECES');
    this.modalPaiementOuvert.set(true);
  }

  fermerModal(): void { this.modalPaiementOuvert.set(false); }

  payerAmende(): void {
    const amende = this.amendeSelectionnee();
    if (!amende) return;

    this.paiementEnCours.set(true);
    const params = new HttpParams()
      .set('methodePaiement', this.methodePaiement());

    this.http.post<Paiement>(
      `${environment.apiUrl}/paiements/amende/${amende.id}`,
      null,
      { params }
    ).subscribe({
      next: () => {
        // Met à jour le statut de l'amende localement
        this.amendes.update(list =>
          list.map(a => a.id === amende.id ? { ...a, statut: 'PAYEE' as const } : a)
        );
        this.paiementEnCours.set(false);
        this.fermerModal();
      },
      error: (err) => {
        this.erreur.set(err.error?.message || 'Erreur lors du paiement.');
        this.paiementEnCours.set(false);
      }
    });
  }

  annulerAmende(amende: Amende): void {
    this.amendeAAnnuler.set(amende);
  }

  onConfirmationAnnulation(confirme: boolean): void {
    const amende = this.amendeAAnnuler();
    this.amendeAAnnuler.set(null);
    if (!confirme || !amende) return;

    this.http.patch<Amende>(
      `${environment.apiUrl}/amendes/${amende.id}/annuler`, {}
    ).subscribe({
      next: (updated) => {
        this.amendes.update(list =>
          list.map(a => a.id === updated.id ? updated : a)
        );
      },
      error: (err) => this.erreur.set(err.error?.message || 'Erreur.')
    });
  }

  // Récupère le titre du livre depuis l'emprunt
  titreLivre(amende: Amende): string {
    return amende.emprunt?.exemplaire?.livre?.titre
      || amende.emprunt?.livre?.titre
      || 'Livre inconnu';
  }

  nomUtilisateur(amende: Amende): string {
    const u = amende.emprunt?.utilisateur;
    return u ? `${u.prenom} ${u.nom}` : '—';
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'badge-danger';
      case 'PAYEE':      return 'badge-success';
      case 'ANNULEE':    return 'badge-system';
      default:           return 'badge-system';
    }
  }

  libelleStatut(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'Impayée';
      case 'PAYEE':      return 'Payée';
      case 'ANNULEE':    return 'Annulée';
      default:           return statut;
    }
  }
}