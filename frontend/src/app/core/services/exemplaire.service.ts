import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Exemplaire } from '../models/entities.model';

export interface CreateExemplaireRequest {
  numExemplaire: string;
  etatPhysique: string;
  statutDisponibilite: string;
  livreId: number;
}

@Injectable({ providedIn: 'root' })
export class ExemplaireService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/exemplaires`;

  // Récupère tous les exemplaires
  getTous(): Observable<Exemplaire[]> {
    return this.http.get<Exemplaire[]>(this.apiUrl);
  }

  // Récupère un exemplaire par son ID
  getById(id: number): Observable<Exemplaire> {
    return this.http.get<Exemplaire>(`${this.apiUrl}/${id}`);
  }

  // Récupère les exemplaires d'un livre
  getByLivre(livreId: number): Observable<Exemplaire[]> {
    return this.http.get<Exemplaire[]>(`${this.apiUrl}/livre/${livreId}`);
  }

  // Crée un nouvel exemplaire
  creer(exemplaire: CreateExemplaireRequest): Observable<Exemplaire> {
    return this.http.post<Exemplaire>(this.apiUrl, exemplaire);
  }

  // Met à jour un exemplaire existant
  modifier(id: number, exemplaire: CreateExemplaireRequest): Observable<Exemplaire> {
    return this.http.put<Exemplaire>(`${this.apiUrl}/${id}`, exemplaire);
  }

  // Supprime un exemplaire
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Change le statut de disponibilité d'un exemplaire
  updateStatut(id: number, statut: string): Observable<Exemplaire> {
    const params = new HttpParams().set('statut', statut);
    return this.http.patch<Exemplaire>(`${this.apiUrl}/${id}/statut`, null, { params });
  }

  // Change l'état physique d'un exemplaire
  updateEtat(id: number, etat: string): Observable<Exemplaire> {
    const params = new HttpParams().set('etat', etat);
    return this.http.patch<Exemplaire>(`${this.apiUrl}/${id}/etat`, null, { params });
  }
}
