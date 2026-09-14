package com.example.BIBESTA.security;

import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.UtilisateurRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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

    private final JwtUtil jwtUtil;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Parse le token UNE SEULE FOIS (P2.7) : avant, on le parsait 4 fois
        // (estValide + extraireIdentifiant + extraireRole + extraireId).
        Claims claims;
        try {
            claims = jwtUtil.extraireClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            // Token invalide / expiré → pas d'authentification
            filterChain.doFilter(request, response);
            return;
        }

        Integer userId = claims.get("id", Integer.class);

        // Re-vérifie le statut du compte en BASE (P2.7) : le rôle et le statut
        // figurants dans le token peuvent être périmés (compte désactivé,
        // rôle changé…) jusqu'à l'expiration du token. On rejette les comptes
        // supprimés ou non-ACTIF, et on reprend le rôle réel depuis la base.
        Utilisateur utilisateur = userId == null
                ? null
                : utilisateurRepository.findById(userId).orElse(null);

        if (utilisateur == null || utilisateur.getStatut() != Utilisateur.Statut.ACTIF) {
            filterChain.doFilter(request, response);
            return;
        }

        // principal = id numérique (comme avant) ; credentials = identifiant ;
        // authorities = rôle réel lu en base (frais, plus fiable que le token)
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                utilisateur.getId(),
                utilisateur.getIdentifiant(),
                List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}