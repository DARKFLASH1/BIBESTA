package com.example.BIBESTA.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Clé secrète pour signer les tokens, injectée depuis application.properties
    // (elle-même lue depuis la variable d'env JWT_SECRET). Ne jamais commiter
    // une vraie valeur : voir .env.example.
    private final String SECRET;

    // Durée de validité du token, configurable via app.jwt.expiration (ms).
    // Défaut : 60 minutes — pas de "remember me" donc TTL court (P2.5).
    private final long EXPIRATION;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:3600000}") long expiration) {
        this.SECRET = secret;
        this.EXPIRATION = expiration;
    }

    // Génère la clé de signature
    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // GÉNÈRE un token JWT
    public String genererToken(String identifiant, String role, Integer id,
            String nom, String prenom) {
        return Jwts.builder()
                .setSubject(identifiant)
                .claim("role", role)
                .claim("id", id) // ← ajoute l'id numérique
                .claim("nom", nom)
                .claim("prenom", prenom)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    // Parse le token en UNE SEULE FOIS et renvoie les claims.
    // Lève JwtException / IllegalArgumentException si le token est invalide.
    // Méthode unique utilisée par JwtFilter : on évite de parser 4× le même
    // token (estValide + extraireIdentifiant + extraireRole + extraireId),
    // et on re-vérifie le statut du compte en base plutôt que de se fier aux
    // claims figés (P2.7).
    public Claims extraireClaims(String token) {
        return getClaims(token);
    }

    // Méthode interne : extrait toutes les données du token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}