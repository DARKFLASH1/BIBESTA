import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

// Correspond exactement au EmpruntResponse du backend
export interface EmpruntResponse {
  id: number;
  dateDebut: string;
  dateRetourPrevue: string;
  dateRetourReelle: string | null;
  statut: 'EN_COURS' | 'RETOURNE' | 'EN_RETARD' | 'A_RENDRE_BIENTOT';
  utilisateurId: number;
  utilisateurNom: string;
  utilisateurPrenom: string;
  livreId: number;
  livreTitre: string;
  livreAuteur: string;
  exemplaireNumero: string;
}

// Correspond au EmpruntRequest du backend
export interface EmpruntRequest {
  utilisateurId: number;
  exemplaireId: number;
}

@Injectable({ providedIn: 'root' })
export class EmpruntService {

  private http   = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/emprunts`;

  // Tous les emprunts (bibliothécaire)
  getTous(): Observable<EmpruntResponse[]> {
    return this.http.get<EmpruntResponse[]>(this.apiUrl);
  }

  // Emprunts d'un utilisateur
  getParUtilisateur(utilisateurId: number): Observable<EmpruntResponse[]> {
    return this.http.get<EmpruntResponse[]>(
      `${this.apiUrl}/utilisateur/${utilisateurId}`
    );
  }

  // Emprunts en retard (bibliothécaire)
  getEnRetard(): Observable<EmpruntResponse[]> {
    return this.http.get<EmpruntResponse[]>(`${this.apiUrl}/en-retard`);
  }

  // Créer un emprunt
  creer(request: EmpruntRequest): Observable<EmpruntResponse> {
    return this.http.post<EmpruntResponse>(this.apiUrl, request);
  }

  // Enregistrer le retour d'un livre
  enregistrerRetour(id: number): Observable<EmpruntResponse> {
    return this.http.put<EmpruntResponse>(`${this.apiUrl}/${id}/retour`, {});
  }

  // Mettre à jour les statuts de retard
  mettreAJourRetards(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/retards/update`, {});
  }
}