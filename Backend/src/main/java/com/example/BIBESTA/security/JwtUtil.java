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
    // une vraie valeur : voir application.properties.example.
    private final String SECRET;

    // Durée de validité : 24h en millisecondes
    private final long EXPIRATION = 86400000;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        this.SECRET = secret;
    }

    // Génère la clé de signature
    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // GÉNÈRE un token JWT
    public String genererToken(String identifiant, String role, Integer id) {
        return Jwts.builder()
                .setSubject(identifiant)
                .claim("role", role)
                .claim("id", id) // ← ajoute l'id numérique
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    // EXTRAIT l'identifiant depuis un token
    public String extraireIdentifiant(String token) {
        return getClaims(token).getSubject();
    }

    // EXTRAIT le rôle depuis un token
    public String extraireRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // EXTRAIT l'ID depuis un token
    public Integer extraireId(String token) {
        return getClaims(token).get("id", Integer.class);
    }

    // VÉRIFIE si un token est valide
    public boolean estValide(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
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