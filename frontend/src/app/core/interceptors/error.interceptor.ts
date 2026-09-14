import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

// Intercepte les erreurs HTTP : gère globalement le cas 401 et
// retransmet l'erreur ORIGINALE (HttpErrorResponse) aux pages.
// Important : ne PAS remplacer l'erreur par un simple Error(), sinon
// `err.error?.message` (le message métier du backend) n'est plus lisible
// par les pages.
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token invalide ou expiré → déconnexion automatique (logout navigue déjà)
        authService.logout();
      }

      console.error('HTTP Error:', error);
      return throwError(() => error);
    })
  );
};