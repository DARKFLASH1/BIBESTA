import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService, DashboardStats, ActiviteRecente, EmpruntRetard, StatistiqueMensuelle, LivrePopulaire } from './services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardPage implements OnInit {
  private dashboardService = inject(DashboardService);
  private authService = inject(AuthService);
  private router = inject(Router);

  // Infos utilisateur
  nomComplet = this.authService.getNomComplet();
  role = this.authService.getRole() ?? 'Bibliothécaire';

  // États de chargement
  loading = signal(true);

  // Données
  stats = signal<DashboardStats | null>(null);
  activiteRecente = signal<ActiviteRecente[]>([]);
  empruntsRetard = signal<EmpruntRetard[]>([]);
  statsMensuelles = signal<StatistiqueMensuelle[]>([]);
  livresPopulaires = signal<LivrePopulaire[]>([]);

  // Computed pour les statistiques dérivées
  tauxDisponibilite = computed(() => {
    const s = this.stats();
    if (!s || s.totalLivres === 0) return 0;
    return Math.round((s.livresDisponibles / s.totalLivres) * 100);
  });

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees(): void {
    this.loading.set(true);

    this.dashboardService.getStats().subscribe(data => {
      this.stats.set(data);
    });

    this.dashboardService.getActiviteRecente().subscribe(data => {
      this.activiteRecente.set(data);
    });

    this.dashboardService.getEmpruntsEnRetard().subscribe(data => {
      this.empruntsRetard.set(data);
    });

    this.dashboardService.getStatistiquesMensuelles().subscribe(data => {
      this.statsMensuelles.set(data);
      this.loading.set(false);
    });

    this.dashboardService.getLivresPopulaires().subscribe(data => {
      this.livresPopulaires.set(data);
    });
  }

  logout(): void {
    this.authService.logout();
  }

  naviguer(route: string): void {
    this.router.navigate([route]);
  }

  formaterDate(date: string): string {
    const d = new Date(date);
    const maintenant = new Date();
    const diffMs = maintenant.getTime() - d.getTime();
    const diffHeures = Math.floor(diffMs / (1000 * 60 * 60));

    if (diffHeures < 1) return "Il y a moins d'une heure";
    if (diffHeures < 24) return `Il y a ${diffHeures}h`;
    const diffJours = Math.floor(diffHeures / 24);
    return `Il y a ${diffJours}j`;
  }

  getIconeActivite(type: string): string {
    switch (type) {
      case 'EMPRUNT': return '📖';
      case 'RETOUR': return '✅';
      case 'RESERVATION': return '🔖';
      case 'INSCRIPTION': return '👤';
      default: return '🔔';
    }
  }

  getCouleurActivite(type: string): string {
    switch (type) {
      case 'EMPRUNT': return 'warning';
      case 'RETOUR': return 'success';
      case 'RESERVATION': return 'system';
      case 'INSCRIPTION': return 'success';
      default: return 'system';
    }
  }

  calculerMaxEmprunts(): number {
    const max = Math.max(...this.statsMensuelles().map(s => Math.max(s.emprunts, s.retours)));
    return max || 1;
  }

  calculerMaxLivresPopulaires(): number {
    const max = Math.max(...this.livresPopulaires().map(l => l.nombreEmprunts));
    return max || 1;
  }
}