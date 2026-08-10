import { inject } from '@angular/core';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // La route définit maintenant une LISTE de rôles autorisés :
  // data: { roles: ['BIBLIOTHECAIRE'] }
  const rolesAutorises: string[] = route.data['roles'] ?? [];
  const roleActuel = authService.getRole();

  if (roleActuel && rolesAutorises.includes(roleActuel)) {
    return true;
  }

  // Mauvais rôle → renvoie vers une page qui existe réellement
  // (l'ancienne version redirigeait vers '/dashboard', qui n'existe pas dans tes routes)
  router.navigate(['/books']);
  return false;
};