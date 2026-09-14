// ==========================================
// ENUMS (Basés sur les ENUM de MySQL)
// ==========================================
export enum Role {
  BIBLIOTHECAIRE = 'BIBLIOTHECAIRE',
  ENSEIGNANT = 'ENSEIGNANT',
  ETUDIANT = 'ETUDIANT',
  PUBLIC = 'PUBLIC'
}

export enum EtatPhysique {
  BON_ETAT = 'BON_ETAT',
  USAGE = 'USAGE',
  ENDOMMAGE = 'ENDOMMAGE',
  PERDU = 'PERDU'
}

export enum StatutDisponibilite {
  DISPONIBLE = 'DISPONIBLE',
  EMPRUNTE = 'EMPRUNTE',
  RESERVE = 'RESERVE',
  EN_REPARATION = 'EN_REPARATION'
}

export enum StatutEmprunt {
  EN_COURS = 'EN_COURS',
  EN_RETARD = 'EN_RETARD',
  RETOURNE = 'RETOURNE',
  A_RENDRE_BIENTOT = 'A_RENDRE_BIENTOT'
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
  EMPRUNT = 'EMPRUNT',
  RETOUR = 'RETOUR',
  RETARD = 'RETARD',
  RESERVATION = 'RESERVATION',
  RESERVATION_DISPONIBLE = 'RESERVATION_DISPONIBLE',
  RESERVATION_EXPIREE = 'RESERVATION_EXPIREE',
  AMENDE = 'AMENDE',
  PAIEMENT = 'PAIEMENT',
  ANNULATION = 'ANNULATION',
  RAPPEL_RETOUR = 'RAPPEL_RETOUR',
  ABONNEMENT_EXPIRE = 'ABONNEMENT_EXPIRE',
  RAPPEL_ABONNEMENT = 'RAPPEL_ABONNEMENT'
}

// ==========================================
// SOUS-TYPES RÉUTILISÉS (pour DTOs imbriqués)
// ==========================================
export interface UtilisateurInfo {
  id: number;
  nom: string;
  prenom: string;
  identifiant: string;
}

export interface LivreInfo {
  id: number;
  titre: string;
  auteur: string;
}

// ==========================================
// INTERFACES (Entités — reflètent la sérialisation Jackson des entités JPA)
// ==========================================
export interface Utilisateur {
  id?: number;
  nom: string;
  prenom: string;
  dateNaissance: string;
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
  livre?: Livre;
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

export interface AmendeEmpruntInfo {
  id: number;
  utilisateur: { id: number; nom: string; prenom: string };
  exemplaire?: { numExemplaire: string; livre?: { titre: string } };
}

export interface Amende {
  id?: number;
  montant: number;
  raison?: string;
  date: string;
  statut: StatutAmende;
  emprunt: AmendeEmpruntInfo;
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
  utilisateur?: UtilisateurInfo;
}

export interface Notification {
  id?: number;
  type: TypeNotification;
  contenu?: string;
  date: string;
  statut: 'LU' | 'NON_LU';
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

// ==========================================
// DTOs (types des réponses API — une seule source de vérité dans core)
// ==========================================
export interface EmpruntResponse {
  id: number;
  dateDebut: string;
  dateRetourPrevue: string;
  dateRetourReelle: string | null;
  statut: StatutEmprunt;
  utilisateurId: number;
  utilisateurNom: string;
  utilisateurPrenom: string;
  livreId: number;
  livreTitre: string;
  livreAuteur: string;
  exemplaireNumero: string;
}

export interface EmpruntRequest {
  utilisateurId: number;
  exemplaireId: number;
}

export interface ReservationResponse {
  id: number;
  dateReservation: string;
  dateConfirmation: string | null;
  statut: StatutReservation;
  utilisateur: UtilisateurInfo;
  livre: LivreInfo;
}
