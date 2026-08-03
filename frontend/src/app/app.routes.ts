import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [

  // Page de connexion
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then(m => m.login)  
  },

  // Dashboard bibliothécaire
  {
    path: 'bibliothecaire/dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/bibliothecaire/dashboard/dashboard')
        .then(m => m.DashboardComponent)
  },

  // Dashboard lecteur (on créera après)
  /*{
    path: 'lecteur/dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/lecteur/dashboard/dashboard')
        .then(m => m.DashboardLecteurComponent)
  },*/
  
  // Par défaut → login
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  // Route inconnue → login
  {
    path: '**',
    redirectTo: 'login'
  }
];