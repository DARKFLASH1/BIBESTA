import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  // ── Aide centrale : sommes-nous dans un vrai navigateur ? ──
  // Toutes les méthodes qui touchent localStorage passent par ici.
  // Ça évite d'oublier la protection sur l'une d'elles (l'erreur qu'on vient de corriger).
  private isBrowser(): boolean {
    return typeof window !== 'undefined';
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        if (!this.isBrowser()) return; // le login n'a de sens que côté navigateur
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('nom', response.nom);
        localStorage.setItem('prenom', response.prenom);
        localStorage.setItem('id', response.id.toString());
      })
    );
  }

  logout(): void {
    if (this.isBrowser()) localStorage.clear();
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    if (!this.isBrowser()) return false;

    const token = localStorage.getItem('token');
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const estExpire = payload.exp * 1000 < Date.now();
      if (estExpire) { localStorage.clear(); return false; }
      return true;
    } catch {
      localStorage.clear();
      return false;
    }
  }

  getToken(): string | null {
    if (!this.isBrowser()) return null;
    return localStorage.getItem('token');
  }

  getRole(): string | null {
    if (!this.isBrowser()) return null;
    return localStorage.getItem('role');
  }

  getId(): number {
    if (!this.isBrowser()) return 0;
    return parseInt(localStorage.getItem('id') || '0');
  }

  isBibliothecaire(): boolean {
    return this.getRole() === 'BIBLIOTHECAIRE';
  }

  getNomComplet(): string {
    if (!this.isBrowser()) return '';
    const nom = localStorage.getItem('nom') || '';
    const prenom = localStorage.getItem('prenom') || '';
    return `${prenom} ${nom}`;
  }

  private decodePayload(): any {
    const token = this.getToken(); // déjà protégé, donc null si SSR
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  getCurrentUserId(): number {
    return this.decodePayload()?.id ?? 0;
  }

  getCurrentUserNom(): string {
    const payload = this.decodePayload();
    return payload?.nom || payload?.sub || '';
  }

  getCurrentUserRole(): string {
    return this.decodePayload()?.role || '';
  }
}