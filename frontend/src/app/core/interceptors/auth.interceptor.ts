import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

// Intercepte chaque requête HTTP et ajoute le token JWT
export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const token = authService.getToken();

  // Si token présent → l'ajoute dans l'en-tête
  if (token) {
    const reqAvecToken = req.clone({
      headers: req.headers.set(
        'Authorization',
        `Bearer ${token}`  // Spring Boot attend ce format
      )
    });
    return next(reqAvecToken);
  }

  // Pas de token → envoie la requête sans modification
  return next(req);
};