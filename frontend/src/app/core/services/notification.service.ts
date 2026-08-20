import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/entities.model';

export interface CreateNotificationRequest {
  type: string;
  contenu?: string;
  utilisateurId: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/notifications`;

  // Récupère toutes les notifications (bibliothécaire)
  getToutes(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  // Récupère les notifications d'un utilisateur
  getByUtilisateur(utilisateurId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/utilisateur/${utilisateurId}`);
  }

  // Récupère une notification par son ID
  getById(id: number): Observable<Notification> {
    return this.http.get<Notification>(`${this.apiUrl}/${id}`);
  }

  // Crée une nouvelle notification
  creer(notification: CreateNotificationRequest): Observable<Notification> {
    return this.http.post<Notification>(this.apiUrl, notification);
  }

  // Marque une notification comme lue
  marquerLue(id: number): Observable<Notification> {
    return this.http.patch<Notification>(`${this.apiUrl}/${id}/lue`, {});
  }

  // Marque toutes les notifications d'un utilisateur comme lues
  marquerToutesLues(utilisateurId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/utilisateur/${utilisateurId}/lues`, {});
  }

  // Supprime une notification
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Envoie une notification de retard
  envoyerNotificationRetard(utilisateurId: number): Observable<Notification> {
    return this.http.post<Notification>(`${this.apiUrl}/retard/${utilisateurId}`, {});
  }

  // Envoie une notification de réservation disponible
  envoyerNotificationReservation(utilisateurId: number, livreId: number): Observable<Notification> {
    const params = new HttpParams()
      .set('utilisateurId', utilisateurId)
      .set('livreId', livreId);
    return this.http.post<Notification>(`${this.apiUrl}/reservation`, null, { params });
  }
}
