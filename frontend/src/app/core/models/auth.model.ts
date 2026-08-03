// auth.model.ts
export interface LoginRequest {
  identifiant: string;
  motDePasse: string;
}

export interface LoginResponse {
  token: string;
  role: string;
  nom: string;
  prenom: string;
  identifiant: string;
  id: number;
}