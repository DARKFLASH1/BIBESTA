// ==========================================
// ENUMS (Basés sur les ENUM de MySQL)
// ==========================================
export enum Role {
  BIBLIOTHECAIRE = 'BIBLIOTHECAIRE',
  ENSEIGNANT = 'ENSEIGNANT',
  ETUDIANT = 'ETUDIANT',
  PUBLIC = 'PUBLIC'
}

// État physique de l'exemplaire (indépendant de sa disponibilité)
export enum EtatPhysique {
  BON_ETAT = 'BON_ETAT',
  USAGE = 'USAGE',
  ENDOMMAGE = 'ENDOMMAGE',
  PERDU = 'PERDU'
}

// Statut de disponibilité de l'exemplaire (indépendant de son état physique)
export enum StatutDisponibilite {
  DISPONIBLE = 'DISPONIBLE',
  EMPRUNTE = 'EMPRUNTE',
  RESERVE = 'RESERVE',
  EN_REPARATION = 'EN_REPARATION'
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
  etatPhysique: EtatPhysique;
  statutDisponibilite: StatutDisponibilite;
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

export interface Reservation {
  id?: number;
  dateReservation: string;
  dateConfirmation?: string;
  statut: StatutReservation;
  utilisateurId: number;
  livreId: number;
  utilisateur?: Utilisateur;
  livre?: Livre;
}

export interface Amende {
  id?: number;
  montant: number;
  raison?: string;
  date: string;
  statut: StatutAmende;
  empruntId: number;
}

export interface Paiement {
  id?: number;
  montant: number;
  datePaiement: string;
  methodePaiement: 'ESPECES' | 'MOBILE_MONEY' | 'CARTE_BANCAIRE';
  statut: 'EFFECTUE' | 'ANNULE' | 'EN_ATTENTE';
  abonnementId?: number;
  amendeId?: number;
}

export interface Abonnement {
  id?: number;
  type: string;
  dateDebut: string;
  dateFin: string;
  statutPaiement: 'EN_ATTENTE' | 'PAYE' | 'EXPIRE';
  montant: number;
  utilisateurId: number;
  utilisateur?: Utilisateur;
}

export interface Notification {
  id?: number;
  type: TypeNotification;
  contenu?: string;
  date: string;
  statut: 'LU' | 'NON_LU';
  utilisateurId: number;
  utilisateur?: Utilisateur;
}

export interface Historique {
  id?: number;
  dateMouvement: string;
  type: 'EMPRUNT' | 'RETOUR' | 'RESERVATION' | 'ANNULATION' | 'PAIEMENT' | 'CONNEXION';
  description?: string;
  utilisateurId: number;
  utilisateur?: Utilisateur;
  empruntId?: number;
  livreId?: number;
  reservationId?: number;
}