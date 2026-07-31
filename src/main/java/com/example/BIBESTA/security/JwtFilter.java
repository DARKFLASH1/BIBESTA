package com.example.BIBESTA.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = ce filtre s'exécute UNE FOIS par requête

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Récupère l'en-tête Authorization
        // Ex: "Bearer eyJhbGci...."
        String authHeader = request.getHeader("Authorization");

        // 2. Vérifie que l'en-tête est présent et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Pas de token → on laisse passer
            // SecurityConfig décidera si la route est accessible
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrait le token (enlève "Bearer ")
        String token = authHeader.substring(7);

        // 4. Vérifie que le token est valide
        if (!jwtUtil.estValide(token)) {
            // Token invalide → on laisse passer sans authentifier
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extrait l'identifiant et le rôle du token
        String identifiant = jwtUtil.extraireIdentifiant(token);
        String role = jwtUtil.extraireRole(token);

        // 6. Crée l'objet d'authentification Spring Security
        // "ROLE_" est un préfixe obligatoire pour Spring Security
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                identifiant,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        // 7. Enregistre l'authentification dans le contexte Spring
        // Maintenant Spring Security sait qui fait la requête
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // 8. Passe à la suite (Controller)
        filterChain.doFilter(request, response);
    }
}