import { inject } from '@angular/core';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Récupère le rôle requis défini dans les routes
  const roleRequis = route.data['role'];
  const roleActuel = authService.getRole();

  if (roleActuel === roleRequis) {
    return true;  // bon rôle → accès autorisé
  }

  // Mauvais rôle → redirige vers dashboard
  router.navigate(['/dashboard']);
  return false;
};