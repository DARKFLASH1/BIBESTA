import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

// Source unique de vérité : le token JWT (id, rôle, nom, prénom).
// localStorage ne stocke QUE le token ; plus aucune clé "role"/"id"/"nom"
// dupliquée qu'on pourrait désynchroniser (P2.16).
@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  private isBrowser(): boolean {
    return typeof window !== 'undefined';
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        if (!this.isBrowser()) return;
        localStorage.setItem('token', response.token);
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

  // Décode le token JWT en UNE fois. Retourne null si absent/invalide.
  private decodePayload(): any {
    const token = this.getToken();
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  getId(): number {
    return this.decodePayload()?.id ?? 0;
  }

  getRole(): string | null {
    return this.decodePayload()?.role ?? null;
  }

  getCurrentUserId(): number {
    return this.getId();
  }

  getCurrentUserRole(): string {
    return this.decodePayload()?.role || '';
  }

  isBibliothecaire(): boolean {
    return this.getRole() === 'BIBLIOTHECAIRE';
  }

  getCurrentUserNom(): string {
    const payload = this.decodePayload();
    return payload?.nom || payload?.sub || '';
  }

  getNomComplet(): string {
    const payload = this.decodePayload();
    if (!payload) return '';
    const nom    = payload?.prenom || '';
    const prenom = payload?.nom || '';
    const complet = `${nom} ${prenom}`.trim();
    return complet || payload?.sub || '';
  }
}