import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { Livre } from '../../../../core/models/entities.model';

@Injectable({
  providedIn: 'root' // disponible partout dans l'application
})
export class LivreService {

  private http = inject(HttpClient);

  // L'URL de base : http://localhost:8080/api/livres
  // (le backend utilise /livres, pas /books)
  private apiUrl = `${environment.apiUrl}/livres`;

  // ── Récupère tous les livres ──────────────────────
  getTousLesLivres(): Observable<Livre[]> {
    return this.http.get<Livre[]>(this.apiUrl);
  }

  // ── Recherche avec filtres optionnels ─────────────
  // Les paramètres absents (undefined) ne sont pas envoyés à Spring Boot
  rechercher(filtres: {
    titre?: string;
    auteur?: string;
    isbn?: string;
    genre?: string;
    langue?: string;
    categorie?: string;
  }): Observable<Livre[]> {
    // HttpParams construit l'URL : /livres/search?titre=Java&auteur=Martin
    let params = new HttpParams();
    if (filtres.titre)     params = params.set('titre',     filtres.titre);
    if (filtres.auteur)    params = params.set('auteur',    filtres.auteur);
    if (filtres.isbn)      params = params.set('isbn',      filtres.isbn);
    if (filtres.genre)     params = params.set('genre',     filtres.genre);
    if (filtres.langue)    params = params.set('langue',    filtres.langue);
    if (filtres.categorie) params = params.set('categorie', filtres.categorie);

    return this.http.get<Livre[]>(`${this.apiUrl}/search`, { params });
  }

  // ── Crée un nouveau livre (bibliothécaire uniquement) ──
  creer(livre: Livre): Observable<Livre> {
    return this.http.post<Livre>(this.apiUrl, livre);
  }

  // ── Modifie un livre existant ─────────────────────
  modifier(id: number, livre: Livre): Observable<Livre> {
    return this.http.put<Livre>(`${this.apiUrl}/${id}`, livre);
  }

  // ── Supprime un livre ─────────────────────────────
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}