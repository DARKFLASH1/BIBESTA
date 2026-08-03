package com.example.BIBESTA.dto.auth;

// Ce que Spring Boot renvoie après connexion réussie
public record LoginResponse(
        String token, // le token JWT
        String role, // "BIBLIOTHECAIRE", "ETUDIANT"...
        String nom, // "Admin"
        String prenom, // "Bibliotheque"
        String identifiant, // "admin"
        Integer id // 1
) {
}