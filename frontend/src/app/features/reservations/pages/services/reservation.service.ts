import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ReservationResponse } from '../../../../core/models/entities.model';

export type Reservation = ReservationResponse;

@Injectable({ providedIn: 'root' })
export class ReservationService {

  private http   = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reservations`;

  getTous(): Observable<ReservationResponse[]> {
    return this.http.get<ReservationResponse[]>(this.apiUrl);
  }

  getParUtilisateur(utilisateurId: number): Observable<ReservationResponse[]> {
    return this.http.get<ReservationResponse[]>(
      `${this.apiUrl}/utilisateur/${utilisateurId}`
    );
  }

  creer(utilisateurId: number, livreId: number): Observable<ReservationResponse> {
    const params = new HttpParams()
      .set('utilisateurId', utilisateurId)
      .set('livreId', livreId);
    return this.http.post<ReservationResponse>(this.apiUrl, null, { params });
  }

  annuler(id: number): Observable<ReservationResponse> {
    return this.http.put<ReservationResponse>(`${this.apiUrl}/${id}/annuler`, {});
  }

  confirmer(livreId: number): Observable<unknown> {
    return this.http.put(`${this.apiUrl}/confirmer/${livreId}`, {});
  }
}
