import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Amende } from '../models/entities.model';

export interface AmendeRequest {
  empruntId: number;
  montant: number;
  raison?: string;
}

@Injectable({ providedIn: 'root' })
export class AmendeService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/amendes`;

  // Récupère toutes les amendes (bibliothécaire)
  getToutes(): Observable<Amende[]> {
    return this.http.get<Amende[]>(this.apiUrl);
  }

  // Récupère les amendes d'un utilisateur
  getByUtilisateur(utilisateurId: number): Observable<Amende[]> {
    return this.http.get<Amende[]>(`${this.apiUrl}/utilisateur/${utilisateurId}`);
  }

  // Récupère une amende par son ID
  getById(id: number): Observable<Amende> {
    return this.http.get<Amende>(`${this.apiUrl}/${id}`);
  }

  // Crée une nouvelle amende
  creer(amende: AmendeRequest): Observable<Amende> {
    return this.http.post<Amende>(this.apiUrl, amende);
  }

  // Annule une amende
  annuler(id: number): Observable<Amende> {
    return this.http.patch<Amende>(`${this.apiUrl}/${id}/annuler`, {});
  }

  // Met à jour le statut d'une amende
  updateStatut(id: number, statut: string): Observable<Amende> {
    const params = new HttpParams().set('nouveauStatut', statut);
    return this.http.patch<Amende>(`${this.apiUrl}/${id}/statut`, null, { params });
  }
}
