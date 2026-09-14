import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // La page de connexion peut être prérendue (publique).
  {
    path: 'login',
    renderMode: RenderMode.Prerender
  },
  // Toutes les autres routes sont protégées par authGuard (JWT côté client) :
  // un prérendu générerait la page de login pour chaque URL. On les rend
  // côté client — le serveur renvoie l'index.html, l'app se charge dans le
  // navigateur avec le token.
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
