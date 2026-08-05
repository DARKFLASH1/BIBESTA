import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '.././../../../../environments/environment';

export interface Reservation {
  id: number;
  dateReservation: string;
  statut: 'EN_ATTENTE' | 'DISPONIBLE' | 'ANNULEE' | 'EXPIREE';
  utilisateur: {
    id: number;
    nom: string;
    prenom: string;
    identifiant: string;
  };
  livre: {
    id: number;
    titre: string;
    auteur: string;
  };
}

@Injectable({ providedIn: 'root' })
export class ReservationService {

  private http   = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reservations`;

  // Toutes les réservations (bibliothécaire)
  getTous(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.apiUrl);
  }

  // Réservations d'un utilisateur
  getParUtilisateur(utilisateurId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(
      `${this.apiUrl}/utilisateur/${utilisateurId}`
    );
  }

  // Créer une réservation
  creer(utilisateurId: number, livreId: number): Observable<Reservation> {
    const params = new HttpParams()
      .set('utilisateurId', utilisateurId)
      .set('livreId', livreId);
    return this.http.post<Reservation>(this.apiUrl, null, { params });
  }

  // Annuler une réservation
  annuler(id: number): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.apiUrl}/${id}/annuler`, {});
  }

  // Confirmer les réservations d'un livre
  confirmer(livreId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/confirmer/${livreId}`, {});
  }
}