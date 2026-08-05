import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard'; // ← protection JWT

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard], // ← toutes les pages enfants sont protégées
    children: [
      { path: '', redirectTo: 'books', pathMatch: 'full' },
      {
        path: 'books',
        loadComponent: () => import('./features/books/books-list/books-list').then(m => m.BooksListPage)
      },
      {
        path: 'loans',
        loadComponent: () => import('./features/loans/pages/my-loans.page').then(m => m.MyLoansPage)
      },
      {
        path: 'loans/manage',
        loadComponent: () => import('./features/loans/pages/manage-loans.page').then(m => m.ManageLoansPage)
      },
      {
        path: 'reservations',
        loadComponent: () => import('./features/reservations/pages/reservations-list.page').then(m => m.ReservationsListPage)
      },
      {
        path: 'users',
        loadComponent: () => import('./features/users/pages/users-list.page').then(m => m.UsersListPage)
      },
      {
        path: 'fines',
        loadComponent: () => import('./features/fines/pages/fines-list.page').then(m => m.FinesListPage)
      },
      {
        path: 'subscriptions',
        loadComponent: () => import('./features/subscriptions/pages/subscriptions-list.page').then(m => m.SubscriptionsListPage)
      },
      {
        path: 'notifications',
        loadComponent: () => import('./features/notifications/pages/notifications-list.page').then(m => m.NotificationsListPage)
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/reports/pages/reports-dashboard.page').then(m => m.ReportsDashboardPage)
      }
    ]
  },
  {
    // ← route login accessible SANS être connecté
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent)
  },
  { path: '**', redirectTo: 'books' }
];