import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';

// Correspond au DTO StatistiqueResponse du backend
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
  montantAmendesEnAttente: string;
  montantAmendesPayees: string;
  utilisateursEtudiants: number;
  utilisateursEnseignants: number;
  utilisateursPublic: number;
  utilisateursBibliothecaires: number;
  empruntsParMois: { mois: string; total: number }[];
  topLivres: { titre: string; nombreEmprunts: number }[];
}

export interface DashboardStats {
  totalLivres: number;
  totalUtilisateurs: number;
  empruntsEnCours: number;
  amendesImpayees: number;
  livresDisponibles: number;
  reservationsEnAttente: number;
}

export interface ActiviteRecente {
  id: number;
  type: 'EMPRUNT' | 'RETOUR' | 'RESERVATION' | 'INSCRIPTION';
  description: string;
  date: string;
  utilisateur?: string;
  livre?: string;
}

export interface EmpruntRetard {
  id: number;
  utilisateur: string;
  livre: string;
  dateRetourPrevue: string;
  joursRetard: number;
}

export interface StatistiqueMensuelle {
  mois: string;
  emprunts: number;
}

export interface LivrePopulaire {
  titre: string;
  nombreEmprunts: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/statistiques`;

  getDashboard(): Observable<StatistiqueResponse> {
    // Un seul appel HTTP, utilisé par tous les getters dérivés
    return this.http.get<StatistiqueResponse>(`${this.apiUrl}/dashboard`);
  }

  getStats(): Observable<DashboardStats> {
    return this.getDashboard().pipe(map(response => ({
      totalLivres: response.totalLivres || 0,
      totalUtilisateurs: (response.utilisateursEtudiants || 0) +
        (response.utilisateursEnseignants || 0) +
        (response.utilisateursPublic || 0) +
        (response.utilisateursBibliothecaires || 0),
      empruntsEnCours: response.empruntsEnCours || 0,
      amendesImpayees: response.montantAmendesEnAttente ?
        parseFloat(response.montantAmendesEnAttente) : 0,
      livresDisponibles: response.exemplairesDisponibles || 0,
      reservationsEnAttente: response.reservationsEnAttente || 0
    })));
  }

  getStatistiquesMensuelles(): Observable<StatistiqueMensuelle[]> {
    return this.getDashboard().pipe(map(response =>
      (response.empruntsParMois || []).map(m => ({ mois: m.mois, emprunts: m.total || 0 }))
    ));
  }

  getLivresPopulaires(): Observable<LivrePopulaire[]> {
    return this.getDashboard().pipe(map(response => response.topLivres || []));
  }

  getActiviteRecente(): Observable<ActiviteRecente[]> {
    // Le backend ne fournit pas encore d'endpoint dédié pour l'activité récente
    // On utilise les derniers emprunts et réservations comme activité
    return this.http.get<any[]>(`${environment.apiUrl}/emprunts/recent`, {
      params: { size: '5' }
    }).pipe(
      map(emprunts => emprunts.map((e: any, index: number) => ({
        id: e.id || index,
        type: e.statut === 'EN_COURS' ? 'EMPRUNT' : 'RETOUR' as 'EMPRUNT' | 'RETOUR',
        description: e.statut === 'EN_COURS' ? 'Emprunt enregistré' : 'Livre retourné',
        date: e.dateDebut || e.dateFin,
        utilisateur: e.utilisateur?.nom || 'Inconnu',
        livre: e.exemplaire?.livre?.titre || 'Inconnu'
      })))
    );
  }

  getEmpruntsEnRetard(): Observable<EmpruntRetard[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/emprunts/en-retard`).pipe(
      map(emprunts => emprunts.map(e => ({
        id: e.id,
        utilisateur: e.utilisateur?.nom || 'Inconnu',
        livre: e.exemplaire?.livre?.titre || 'Inconnu',
        dateRetourPrevue: e.dateRetourPrevue,
        joursRetard: e.joursRetard || 0
      })))
    );
  }
}