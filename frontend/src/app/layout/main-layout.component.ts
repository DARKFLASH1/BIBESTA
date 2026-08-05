import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { 
  LucideBookOpen, LucideBookmark, LucideCalendarClock, 
  LucideUsers, LucideCreditCard, LucideBadgeCheck, 
  LucideBell, LucideBarChart3, LucideLibrary,
  LucideMenu, LucideX, LucideUser, LucideLogOut
} from '@lucide/angular';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    LucideBookOpen, LucideBookmark, LucideCalendarClock,
    LucideUsers, LucideCreditCard, LucideBadgeCheck,
    LucideBell, LucideBarChart3, LucideLibrary,
    LucideMenu, LucideX, LucideUser, LucideLogOut
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {

  private authService = inject(AuthService);
  private router      = inject(Router);

  isSidebarOpen = signal(false);
  isMobile      = signal(false);

  // Infos utilisateur connecté pour affichage dans la sidebar
  nomUtilisateur  = this.authService.getCurrentUserNom();
  roleUtilisateur = this.authService.getCurrentUserRole();

  menuItems = [
    { label: 'Catalogue',        route: '/books',         icon: 'bookOpen',    roles: ['BIBLIOTHECAIRE', 'ENSEIGNANT', 'ETUDIANT', 'PUBLIC'] },
    { label: 'Mes emprunts',     route: '/loans',         icon: 'bookmark',    roles: ['ENSEIGNANT', 'ETUDIANT', 'PUBLIC'] },
    { label: 'Emprunts',         route: '/loans/manage',  icon: 'bookmark',    roles: ['BIBLIOTHECAIRE'] },
    { label: 'Réservations',     route: '/reservations',  icon: 'calendarClock', roles: ['BIBLIOTHECAIRE', 'ENSEIGNANT', 'ETUDIANT', 'PUBLIC'] },
    { label: 'Utilisateurs',     route: '/users',         icon: 'users',       roles: ['BIBLIOTHECAIRE'] },
    { label: 'Amendes',          route: '/fines',         icon: 'creditCard',  roles: ['BIBLIOTHECAIRE', 'ENSEIGNANT', 'ETUDIANT', 'PUBLIC'] },
    { label: 'Abonnements',      route: '/subscriptions', icon: 'badgeCheck',  roles: ['BIBLIOTHECAIRE', 'ENSEIGNANT', 'ETUDIANT', 'PUBLIC'] },
    { label: 'Notifications',    route: '/notifications', icon: 'bell',        roles: ['BIBLIOTHECAIRE', 'ENSEIGNANT', 'ETUDIANT', 'PUBLIC'] },
    { label: 'Statistiques',     route: '/reports',       icon: 'barChart3',   roles: ['BIBLIOTHECAIRE'] }
  ];

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  toggleSidebar(): void { this.isSidebarOpen.update(v => !v); }
  closeSidebar(): void  { if (this.isMobile()) this.isSidebarOpen.set(false); }
}