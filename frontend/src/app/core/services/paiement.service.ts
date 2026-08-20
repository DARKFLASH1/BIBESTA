import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Paiement } from '../models/entities.model';

export interface CreatePaiementRequest {
  montant: number;
  methodePaiement: 'ESPECES' | 'MOBILE_MONEY' | 'CARTE_BANCAIRE';
}

@Injectable({ providedIn: 'root' })
export class PaiementService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/paiements`;

  // Récupère tous les paiements (bibliothécaire)
  getTous(): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(this.apiUrl);
  }

  // Récupère les paiements d'un utilisateur
  getByUtilisateur(utilisateurId: number): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(`${this.apiUrl}/utilisateur/${utilisateurId}`);
  }

  // Récupère un paiement par son ID
  getById(id: number): Observable<Paiement> {
    return this.http.get<Paiement>(`${this.apiUrl}/${id}`);
  }

  // Crée un paiement pour une amende
  payerAmende(amendeId: number, methodePaiement: string): Observable<Paiement> {
    const params = new HttpParams().set('methodePaiement', methodePaiement);
    return this.http.post<Paiement>(`${this.apiUrl}/amende/${amendeId}`, null, { params });
  }

  // Crée un paiement pour un abonnement
  payerAbonnement(abonnementId: number, methodePaiement: string): Observable<Paiement> {
    const params = new HttpParams().set('methodePaiement', methodePaiement);
    return this.http.post<Paiement>(`${this.apiUrl}/abonnement/${abonnementId}`, null, { params });
  }

  // Annule un paiement
  annuler(id: number): Observable<Paiement> {
    return this.http.patch<Paiement>(`${this.apiUrl}/${id}/annuler`, {});
  }
}
