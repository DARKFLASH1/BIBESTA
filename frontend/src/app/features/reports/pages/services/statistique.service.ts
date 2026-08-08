import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

// Correspond exactement au StatistiqueResponse (record) du backend.
// Chaque "record" imbriqué Java devient une interface TypeScript.
export interface EmpruntsParMois {
  mois: string;
  total: number;
}

export interface LivrePopulaire {
  titre: string;
  nombreEmprunts: number;
}

export interface StatistiqueResponse {
  totalLivres: number;
  totalExemplaires: number;
  exemplairesDisponibles: number;
  exemplairesEmpruntes: number;
  exemplairesReserves: number;
  exemplairesEnReparation: number;

  empruntsEnCours: number;
  empruntsEnRetard: number;
  empruntsRetournes: number;

  reservationsEnAttente: number;

  montantAmendesEnAttente: number;
  montantAmendesPayees: number;

  utilisateursEtudiants: number;
  utilisateursEnseignants: number;
  utilisateursPublic: number;
  utilisateursBibliothecaires: number;

  empruntsParMois: EmpruntsParMois[];
  topLivres: LivrePopulaire[];
}

@Injectable({ providedIn: 'root' })
export class StatistiqueService {

  private http   = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/statistiques`;

  // Un seul appel : GET /statistiques/dashboard
  getDashboard(): Observable<StatistiqueResponse> {
    return this.http.get<StatistiqueResponse>(`${this.apiUrl}/dashboard`);
  }
}