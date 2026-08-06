import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideUsers, LucidePlus, LucideX,
  LucidePencil, LucideSearch, LucideUser
} from '@lucide/angular';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  identifiant: string;
  contact: string;
  dateNaissance: string;
  sexe: string;
  role: 'BIBLIOTHECAIRE' | 'ETUDIANT' | 'ENSEIGNANT' | 'PUBLIC';
}

interface UtilisateurForm {
  nom: string;
  prenom: string;
  email: string;
  identifiant: string;
  motDePasse: string;
  contact: string;
  dateNaissance: string;
  sexe: string;
  role: string;
}

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    LucideUsers, LucidePlus, LucideX,
    LucidePencil, LucideSearch, LucideUser
  ],
  templateUrl: './users-list.page.html',
  styleUrl: './users-list.page.scss'
})
export class UsersListPage implements OnInit {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/utilisateurs`;

  // ── État ──────────────────────────────────────────
  utilisateurs  = signal<Utilisateur[]>([]);
  loading       = signal(true);
  erreur        = signal('');
  recherche     = signal('');

  // ── Filtre par rôle ───────────────────────────────
  filtreRole = signal<string>('tous');

  utilisateursFiltres = computed(() => {
    let liste = this.utilisateurs();
    const r = this.recherche().toLowerCase();
    const role = this.filtreRole();

    if (r) {
      liste = liste.filter(u =>
        u.nom.toLowerCase().includes(r) ||
        u.prenom.toLowerCase().includes(r) ||
        u.email.toLowerCase().includes(r) ||
        u.identifiant.toLowerCase().includes(r)
      );
    }

    if (role !== 'tous') {
      liste = liste.filter(u => u.role === role);
    }

    return liste;
  });

  // Compteurs par rôle
  nbEtudiants   = computed(() => this.utilisateurs().filter(u => u.role === 'ETUDIANT').length);
  nbEnseignants = computed(() => this.utilisateurs().filter(u => u.role === 'ENSEIGNANT').length);
  nbPublic      = computed(() => this.utilisateurs().filter(u => u.role === 'PUBLIC').length);
  nbBiblios     = computed(() => this.utilisateurs().filter(u => u.role === 'BIBLIOTHECAIRE').length);

  // ── Modal ─────────────────────────────────────────
  modalOuvert  = signal(false);
  modeEdition  = signal(false);
  sauvegarde   = signal(false);
  userEnCours  = signal<number | null>(null); // id en cours de modif

  form: UtilisateurForm = {
    nom: '', prenom: '', email: '', identifiant: '',
    motDePasse: '', contact: '', dateNaissance: '', sexe: 'M', role: 'ETUDIANT'
  };

  motDePasseVisible = false;

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.loading.set(true);
    this.http.get<Utilisateur[]>(this.apiUrl).subscribe({
      next: (data) => { this.utilisateurs.set(data); this.loading.set(false); },
      error: () => { this.erreur.set('Impossible de charger les utilisateurs.'); this.loading.set(false); }
    });
  }

  ouvrirModalAjout(): void {
    this.modeEdition.set(false);
    this.userEnCours.set(null);
    this.form = {
      nom: '', prenom: '', email: '', identifiant: '',
      motDePasse: '', contact: '', dateNaissance: '', sexe: 'M', role: 'ETUDIANT'
    };
    this.modalOuvert.set(true);
  }

  ouvrirModalModif(u: Utilisateur): void {
    this.modeEdition.set(true);
    this.userEnCours.set(u.id);
    this.form = {
      nom: u.nom,
      prenom: u.prenom,
      email: u.email,
      identifiant: u.identifiant,
      motDePasse: '', // on ne pré-remplit jamais le mot de passe
      contact: u.contact || '',
      dateNaissance: u.dateNaissance,
      sexe: u.sexe,
      role: u.role
    };
    this.modalOuvert.set(true);
  }

  fermerModal(): void { this.modalOuvert.set(false); }

  sauvegarder(): void {
    if (!this.form.nom || !this.form.prenom || !this.form.email) return;

    this.sauvegarde.set(true);

    if (this.modeEdition() && this.userEnCours()) {
      // Modification
      this.http.put<Utilisateur>(
        `${this.apiUrl}/${this.userEnCours()}`, this.form
      ).subscribe({
        next: (updated) => {
          this.utilisateurs.update(list =>
            list.map(u => u.id === updated.id ? updated : u)
          );
          this.sauvegarde.set(false);
          this.fermerModal();
        },
        error: (err) => {
          this.erreur.set(err.error?.message || 'Erreur lors de la modification.');
          this.sauvegarde.set(false);
        }
      });
    } else {
      // Création
      this.http.post<Utilisateur>(this.apiUrl, this.form).subscribe({
        next: (nouveau) => {
          this.utilisateurs.update(list => [nouveau, ...list]);
          this.sauvegarde.set(false);
          this.fermerModal();
        },
        error: (err) => {
          this.erreur.set(err.error?.message || 'Erreur lors de la création.');
          this.sauvegarde.set(false);
        }
      });
    }
  }

  supprimer(u: Utilisateur): void {
    if (!confirm(`Supprimer "${u.prenom} ${u.nom}" ?`)) return;
    this.http.delete(`${this.apiUrl}/${u.id}`).subscribe({
      next: () => this.utilisateurs.update(list => list.filter(x => x.id !== u.id)),
      error: (err) => this.erreur.set(err.error?.message || 'Erreur lors de la suppression.')
    });
  }

  badgeRole(role: string): string {
    switch (role) {
      case 'BIBLIOTHECAIRE': return 'badge-accent';
      case 'ETUDIANT':       return 'badge-info';
      case 'ENSEIGNANT':     return 'badge-success';
      case 'PUBLIC':         return 'badge-system';
      default:               return 'badge-system';
    }
  }

  libelleRole(role: string): string {
    switch (role) {
      case 'BIBLIOTHECAIRE': return 'Bibliothécaire';
      case 'ETUDIANT':       return 'Étudiant';
      case 'ENSEIGNANT':     return 'Enseignant';
      case 'PUBLIC':         return 'Public';
      default:               return role;
    }
  }
}