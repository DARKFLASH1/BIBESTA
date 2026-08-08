import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideBarChart3, LucideBookOpen, LucideBookmark, LucideUsers,
  LucideCreditCard, LucideCalendarClock, LucideAlertTriangle,
  LucideCheckCircle2, LucideClock, LucideBookX
} from '@lucide/angular';
import { StatistiqueService, StatistiqueResponse } from './services/statistique.service';

@Component({
  selector: 'app-reports-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    LucideBarChart3, LucideBookOpen, LucideBookmark, LucideUsers,
    LucideCreditCard, LucideCalendarClock, LucideAlertTriangle,
    LucideCheckCircle2, LucideClock, LucideBookX
  ],
  templateUrl: './reports-dashboard.page.html',
  styleUrl: './reports-dashboard.page.scss'
})
export class ReportsDashboardPage implements OnInit {

  private statistiqueService = inject(StatistiqueService);

  // ── État de la page (les 4 états obligatoires) ────────
  data    = signal<StatistiqueResponse | null>(null);
  loading = signal(true);
  erreur  = signal('');

  // ── Valeurs dérivées pour le graphique "emprunts par mois" ──
  // computed() recalcule automatiquement dès que data() change
  maxEmpruntsMois = computed(() => {
    const mois = this.data()?.empruntsParMois ?? [];
    // Math.max(...[]) vaudrait -Infinity : on protège avec 1 minimum
    return Math.max(1, ...mois.map(m => m.total));
  });

  // Hauteur de chaque barre en %, relative au mois le plus chargé
  hauteurBarre(total: number): number {
    return Math.round((total / this.maxEmpruntsMois()) * 100);
  }

  // Longueur de chaque barre du top livres, relative au livre le plus emprunté
  maxTopLivres = computed(() => {
    const livres = this.data()?.topLivres ?? [];
    return Math.max(1, ...livres.map(l => l.nombreEmprunts));
  });

  largeurBarre(nombreEmprunts: number): number {
    return Math.round((nombreEmprunts / this.maxTopLivres()) * 100);
  }

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.erreur.set('');

    this.statistiqueService.getDashboard().subscribe({
      next: (reponse) => {
        this.data.set(reponse);
        this.loading.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les statistiques. Vérifiez que le serveur est démarré.');
        this.loading.set(false);
      }
    });
  }
}