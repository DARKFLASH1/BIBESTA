// ==========================================
// ENUMS (Basés sur les ENUM de MySQL)
// ==========================================
export enum Role {
  BIBLIOTHECAIRE = 'BIBLIOTHECAIRE',
  ENSEIGNANT = 'ENSEIGNANT',
  ETUDIANT = 'ETUDIANT',
  PUBLIC = 'PUBLIC'
}

export enum EtatExemplaire {
  BON_ETAT = 'BON_ETAT',
  DISPONIBLE = 'DISPONIBLE',
  EMPRUNTE = 'EMPRUNTE',
  EN_REPARATION = 'EN_REPARATION',
  MAUVAIS_ETAT = 'MAUVAIS_ETAT',
  RESERVE = 'RESERVE'
}

export enum StatutEmprunt {
  EN_COURS = 'EN_COURS',
  EN_RETARD = 'EN_RETARD',
  RETOURNE = 'RETOURNE'
}

export enum StatutReservation {
  ANNULEE = 'ANNULEE',
  CONFIRMEE = 'CONFIRMEE',
  EN_ATTENTE = 'EN_ATTENTE'
}

export enum StatutAmende {
  ANNULEE = 'ANNULEE',
  EN_ATTENTE = 'EN_ATTENTE',
  PAYEE = 'PAYEE'
}

export enum TypeNotification {
  ABONNEMENT_EXPIRE = 'ABONNEMENT_EXPIRE',
  AMENDE = 'AMENDE',
  RAPPEL_RETOUR = 'RAPPEL_RETOUR',
  RESERVATION_DISPONIBLE = 'RESERVATION_DISPONIBLE',
  RETARD = 'RETARD'
}

// ==========================================
// INTERFACES (Entités)
// ==========================================
export interface Utilisateur {
  id?: number;
  nom: string;
  prenom: string;
  dateNaissance: string; // Format YYYY-MM-DD
  sexe: string;
  email: string;
  identifiant: string;
  contact?: string;
  role: Role;
}

export interface Livre {
  id?: number;
  titre: string;
  auteur: string;
  edition?: string;
  categorie?: string;
  genre?: string;
  anneePublication?: number;
  langue?: string;
  isbn?: string;
  nombrePages?: number;
}

export interface Exemplaire {
  id?: number;
  numExemplaire: string;
  etat: EtatExemplaire;
  livreId: number;
  livre?: Livre; // Pour l'affichage frontend
}

export interface Emprunt {
  id?: number;
  dateDebut: string;
  dateRetourPrevue: string;
  dateRetourReelle?: string;
  statut: StatutEmprunt;
  exemplaireId: number;
  utilisateurId: number;
}