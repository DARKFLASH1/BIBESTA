package com.example.BIBESTA.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// Protection brute-force sur /api/auth/login (P2.4) :
// compteur en mémoire par IP, fenêtre + blocage temporaire configurables.
// Simple (sans Redis, sans dépendance) : suffisant tant que l'app est mono-instance.
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxAttempts;
    private final long windowMs;
    private final long blockMs;

    private static class Compteur {
        final long debutFenetre;
        int tentatives;

        Compteur(long debutFenetre) {
            this.debutFenetre = debutFenetre;
            this.tentatives = 1;
        }
    }

    // IP → état du compteur
    private final ConcurrentHashMap<String, Compteur> compteurs = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${app.rate-limit.window-ms:60000}") long windowMs,
            @Value("${app.rate-limit.block-ms:300000}") long blockMs) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
        this.blockMs = blockMs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (!request.getMethod().equals("POST") || !uri.endsWith("/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = ipClient(request);
        long maintenant = System.currentTimeMillis();

        Compteur état = compteurs.compute(ip, (cle, existant) -> {
            if (existant == null || maintenant - existant.debutFenetre > windowMs + blockMs) {
                return new Compteur(maintenant);
            }
            if (maintenant - existant.debutFenetre > windowMs) {
                // Fenêtre expirée mais blocage en cours → on compte tout de même,
                // seule la nouvelle fenêtre civilisée réinitialise.
                existant.tentatives++;
                return existant;
            }
            existant.tentatives++;
            return existant;
        });

        if (état.tentatives > maxAttempts) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"erreur\":\"Trop de tentatives. Réessayez plus tard.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String ipClient(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !xff.equalsIgnoreCase("unknown")) {
            return xff.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return (ip != null) ? ip : "inconnu";
    }
}