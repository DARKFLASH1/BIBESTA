import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

// P3.1 : le JWT n'est PLUS jamais stocké dans localStorage (exposition XSS).
// Il vit uniquement en mémoire, attaché au service (header Authorization).
// Conséquence voulue : rafraîchir la page = reconnexion (pas de session persistée).
@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;
  private token: string | null = null;

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
        // Stockage en mémoire uniquement (P3.1). Jamais dans localStorage.
        if (this.isBrowser()) this.token = response.token;
      })
    );
  }

  logout(): void {
    this.token = null;
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.token && !!this.decodePayload();
  }

  getToken(): string | null {
    return this.token;
  }

  // Décode un segment JWT encodé en base64url (RFC 4648 §5).
  // `atob` ne gère PAS les caractères '-' et '_' du base64url → remplacement
  // propre par l'alphabet base64 standard avant décodage (P3.2).
  private decodeSegment(segment: string): string | null {
    try {
      let s = segment.replace(/-/g, '+').replace(/_/g, '/');
      const pad = s.length % 4;
      if (pad === 2) s += '==';
      else if (pad === 3) s += '=';
      const json = atob(s);
      // Vérifie que c'est bien du JSON (évite les payloads invalides)
      const payload = JSON.parse(json);
      return payload;
    } catch {
      return null;
    }
  }

  // Décode le payload JWT en UNE fois. Retourne null si absent/invalide.
  private decodePayload(): any {
    const token = this.getToken();
    if (!token) return null;
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    return this.decodeSegment(parts[1]);
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
