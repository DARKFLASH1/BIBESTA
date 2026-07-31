package com.example.BIBESTA.dto.livre;

// Ce qu'Angular reçoit pour un livre
public record LivreResponse(
        Integer id,
        String titre,
        String auteur,
        String edition,
        String isbn,
        String categorie,
        String genre,
        String langue,
        Integer anneePublication,
        Integer nombrePages,
        // Nombre d'exemplaires disponibles
        // calculé dynamiquement dans le service
        long exemplairesDiponibles) {
}