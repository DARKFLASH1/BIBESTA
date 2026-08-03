// utilisateur.model.ts
export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  identifiant: string;
  contact: string;
  role: 'BIBLIOTHECAIRE' | 'ETUDIANT' | 'ENSEIGNANT' | 'PUBLIC';
}