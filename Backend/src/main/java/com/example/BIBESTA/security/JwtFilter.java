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

    private final JwtUtil jwtUtil;

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

        if (!jwtUtil.estValide(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String identifiant = jwtUtil.extraireIdentifiant(token);
        String role = jwtUtil.extraireRole(token);
        Integer userId = jwtUtil.extraireId(token); // ← on extrait l'id

        // On crée un objet qui porte les infos de l'utilisateur connecté
        // principal = "qui est-ce ?" → on y met l'id numérique
        // credentials = "son mot de passe" → null (déjà vérifié via JWT)
        // authorities = "ses droits" → son rôle
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, // ← l'id est maintenant le "principal"
                identifiant, // ← l'identifiant en credentials (pour info)
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}