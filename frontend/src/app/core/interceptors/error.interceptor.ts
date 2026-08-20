import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Intercepte les erreurs HTTP et gère globalement les cas 401 et 500
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Une erreur inconnue est survenue.';

      if (error.status === 401) {
        // Token invalide ou expiré → déconnexion automatique
        errorMessage = 'Session expirée. Veuillez vous reconnecter.';
        authService.logout();
        router.navigate(['/login']);
      } else if (error.status === 403) {
        // Accès interdit
        errorMessage = 'Accès non autorisé à cette ressource.';
      } else if (error.status === 404) {
        // Ressource non trouvée
        errorMessage = 'Ressource non trouvée.';
      } else if (error.status === 500) {
        // Erreur serveur
        errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
      } else if (error.error instanceof ErrorEvent) {
        // Erreur côté client (network, CORS, etc.)
        errorMessage = 'Erreur de connexion au serveur.';
      } else {
        // Erreur métier du backend
        errorMessage = error.error?.message || error.message || errorMessage;
      }

      console.error('HTTP Error:', error);
      return throwError(() => new Error(errorMessage));
    })
  );
};
