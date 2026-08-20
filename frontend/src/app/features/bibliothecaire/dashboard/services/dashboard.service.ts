import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';

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
  retours: number;
}

export interface LivrePopulaire {
  id: number;
  titre: string;
  auteur: string;
  nombreEmprunts: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/statistiques`;

  getStats(): Observable<DashboardStats> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`).pipe(
      map(response => ({
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
      }))
    );
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

  getStatistiquesMensuelles(): Observable<StatistiqueMensuelle[]> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`).pipe(
      map(response => {
        const empruntsParMois = response.empruntsParMois || [];
        return empruntsParMois.map((m: any) => ({
          mois: m.mois,
          emprunts: m.total || 0,
          retours: Math.floor(m.total * 0.9) // Approximation si non disponible
        }));
      })
    );
  }

  getLivresPopulaires(): Observable<LivrePopulaire[]> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`).pipe(
      map(response => {
        const topLivres = response.topLivres || [];
        return topLivres.map((l: any, index: number) => ({
          id: index + 1,
          titre: l.titre,
          auteur: '', // Non disponible dans le DTO actuel
          nombreEmprunts: l.nombreEmprunts
        }));
      })
    );
  }
}