import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Abonnement } from '../models/entities.model';

export interface CreateAbonnementRequest {
  type: string;
  dateDebut: string;
  dateFin: string;
  montant: number;
  statutPaiement: string;
}

@Injectable({ providedIn: 'root' })
export class AbonnementService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/abonnements`;

  // Récupère tous les abonnements (bibliothécaire)
  getTous(): Observable<Abonnement[]> {
    return this.http.get<Abonnement[]>(this.apiUrl);
  }

  // Récupère les abonnements d'un utilisateur
  getByUtilisateur(utilisateurId: number): Observable<Abonnement[]> {
    return this.http.get<Abonnement[]>(`${this.apiUrl}/utilisateur/${utilisateurId}`);
  }

  // Récupère un abonnement par son ID
  getById(id: number): Observable<Abonnement> {
    return this.http.get<Abonnement>(`${this.apiUrl}/${id}`);
  }

  // Crée un abonnement pour un utilisateur
  creerPourUtilisateur(utilisateurId: number, abonnement: CreateAbonnementRequest): Observable<Abonnement> {
    return this.http.post<Abonnement>(`${this.apiUrl}/utilisateur/${utilisateurId}`, abonnement);
  }

  // Met à jour le statut d'un abonnement
  updateStatut(id: number, nouveauStatut: string): Observable<Abonnement> {
    const params = new HttpParams().set('nouveauStatut', nouveauStatut);
    return this.http.patch<Abonnement>(`${this.apiUrl}/${id}/statut`, null, { params });
  }

  // Supprime un abonnement
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Vérifie si un utilisateur a un abonnement actif
  estActif(utilisateurId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/utilisateur/${utilisateurId}/actif`);
  }
}
