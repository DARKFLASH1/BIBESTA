import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
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
  private apiUrl = `${environment.apiUrl}/dashboard`;

  // ⚠️ MOCK DATA - À remplacer par de vrais appels API quand le backend sera prêt
  
  getStats(): Observable<DashboardStats> {
    // return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
    return of({
      totalLivres: 245,
      totalUtilisateurs: 89,
      empruntsEnCours: 34,
      amendesImpayees: 7,
      livresDisponibles: 187,
      reservationsEnAttente: 12
    });
  }

  getActiviteRecente(): Observable<ActiviteRecente[]> {
    // return this.http.get<ActiviteRecente[]>(`${this.apiUrl}/activite-recente`);
    return of([
      {
        id: 1,
        type: 'EMPRUNT',
        description: 'Emprunt enregistré',
        date: '2026-08-03T14:30:00',
        utilisateur: 'Marie Dupont',
        livre: 'Le Petit Prince'
      },
      {
        id: 2,
        type: 'RETOUR',
        description: 'Livre retourné',
        date: '2026-08-03T11:15:00',
        utilisateur: 'Jean Martin',
        livre: '1984 - George Orwell'
      },
      {
        id: 3,
        type: 'INSCRIPTION',
        description: 'Nouvel utilisateur inscrit',
        date: '2026-08-03T09:45:00',
        utilisateur: 'Sophie Bernard'
      },
      {
        id: 4,
        type: 'RESERVATION',
        description: 'Réservation confirmée',
        date: '2026-08-02T16:20:00',
        utilisateur: 'Pierre Leroy',
        livre: 'L\'Étranger - Camus'
      }
    ]);
  }

  getEmpruntsEnRetard(): Observable<EmpruntRetard[]> {
    // return this.http.get<EmpruntRetard[]>(`${this.apiUrl}/emprunts-retard`);
    return of([
      {
        id: 1,
        utilisateur: 'Alice Moreau',
        livre: 'Les Misérables',
        dateRetourPrevue: '2026-07-28',
        joursRetard: 6
      },
      {
        id: 2,
        utilisateur: 'Thomas Petit',
        livre: 'Madame Bovary',
        dateRetourPrevue: '2026-07-30',
        joursRetard: 4
      },
      {
        id: 3,
        utilisateur: 'Julie Roux',
        livre: 'Germinal',
        dateRetourPrevue: '2026-08-01',
        joursRetard: 2
      }
    ]);
  }

  getStatistiquesMensuelles(): Observable<StatistiqueMensuelle[]> {
    // return this.http.get<StatistiqueMensuelle[]>(`${this.apiUrl}/stats-mensuelles`);
    return of([
      { mois: 'Jan', emprunts: 45, retours: 42 },
      { mois: 'Fév', emprunts: 52, retours: 48 },
      { mois: 'Mar', emprunts: 61, retours: 58 },
      { mois: 'Avr', emprunts: 49, retours: 51 },
      { mois: 'Mai', emprunts: 58, retours: 55 },
      { mois: 'Jun', emprunts: 67, retours: 63 }
    ]);
  }

  getLivresPopulaires(): Observable<LivrePopulaire[]> {
    // return this.http.get<LivrePopulaire[]>(`${this.apiUrl}/livres-populaires`);
    return of([
      { id: 1, titre: 'Le Petit Prince', auteur: 'Saint-Exupéry', nombreEmprunts: 23 },
      { id: 2, titre: '1984', auteur: 'George Orwell', nombreEmprunts: 19 },
      { id: 3, titre: 'L\'Étranger', auteur: 'Albert Camus', nombreEmprunts: 17 },
      { id: 4, titre: 'Les Misérables', auteur: 'Victor Hugo', nombreEmprunts: 15 },
      { id: 5, titre: 'Madame Bovary', auteur: 'Gustave Flaubert', nombreEmprunts: 12 }
    ]);
  }
}