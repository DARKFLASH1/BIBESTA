import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { EmpruntResponse, EmpruntRequest } from '../../../../core/models/entities.model';

@Injectable({ providedIn: 'root' })
export class EmpruntService {

  private http   = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/emprunts`;

  getTous(): Observable<EmpruntResponse[]> {
    return this.http.get<EmpruntResponse[]>(this.apiUrl);
  }

  getParUtilisateur(utilisateurId: number): Observable<EmpruntResponse[]> {
    return this.http.get<EmpruntResponse[]>(
      `${this.apiUrl}/utilisateur/${utilisateurId}`
    );
  }

  getEnRetard(): Observable<EmpruntResponse[]> {
    return this.http.get<EmpruntResponse[]>(`${this.apiUrl}/en-retard`);
  }

  creer(request: EmpruntRequest): Observable<EmpruntResponse> {
    return this.http.post<EmpruntResponse>(this.apiUrl, request);
  }

  enregistrerRetour(id: number): Observable<EmpruntResponse> {
    return this.http.put<EmpruntResponse>(`${this.apiUrl}/${id}/retour`, {});
  }

  mettreAJourRetards(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/retards/update`, {});
  }
}
