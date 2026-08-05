import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
  // "providedIn: root" = disponible partout dans l'app
})
export class AuthService {

  // URL de connexion Spring Boot
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(
    private http: HttpClient,  // pour parler à Spring Boot
    private router: Router     // pour naviguer entre pages
  ) {}

  // CONNEXION
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      request
    ).pipe(
      // "pipe + tap" = exécute du code quand la réponse arrive
      tap(response => {
        // Stocke le token et les infos dans localStorage
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('nom', response.nom);
        localStorage.setItem('prenom', response.prenom);
        localStorage.setItem('id', response.id.toString());
      })
    );
  }

  // DÉCONNEXION
  logout(): void {
    // Supprime tout du localStorage
    localStorage.clear();
    // Redirige vers la page de connexion
    this.router.navigate(['/login']);
  }

  // VÉRIFIE SI L'UTILISATEUR EST CONNECTÉ
  isLoggedIn(): boolean {
  if (typeof window === 'undefined') return false;
  
  const token = localStorage.getItem('token');
  if (!token) return false;

  // Décode la partie centrale du token JWT (sans librairie externe)
  // Un token JWT = 3 parties séparées par des points : header.payload.signature
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    // "exp" = timestamp d'expiration en secondes
    // Date.now() est en millisecondes → on divise par 1000
    const estExpire = payload.exp * 1000 < Date.now();
    
    if (estExpire) {
      localStorage.clear(); // nettoie automatiquement
      return false;
    }
    return true;
  } catch {
    // Token malformé → on déconnecte
    localStorage.clear();
    return false;
  }
}
getCurrentUserId(): number {
  const token = this.getToken();
  if (!token) return 0;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.id || 0;
  } catch {
    return 0;
  }
}

  // RÉCUPÈRE LE TOKEN
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  // RÉCUPÈRE LE RÔLE
  getRole(): string | null {
    return localStorage.getItem('role');
  }

  // RÉCUPÈRE L'ID
  getId(): number {
    return parseInt(localStorage.getItem('id') || '0');
  }

  // VÉRIFIE SI C'EST UN BIBLIOTHÉCAIRE
  isBibliothecaire(): boolean {
    return this.getRole() === 'BIBLIOTHECAIRE';
  }

  // RÉCUPÈRE LE NOM COMPLET
  getNomComplet(): string {
    const nom = localStorage.getItem('nom') || '';
    const prenom = localStorage.getItem('prenom') || '';
    return `${prenom} ${nom}`;
  }
  getCurrentUserNom(): string {
  const token = this.getToken();
  if (!token) return '';
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.nom || payload.sub || '';
  } catch { return ''; }
}

getCurrentUserRole(): string {
  const token = this.getToken();
  if (!token) return '';
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || '';
  } catch { return ''; }
}
}