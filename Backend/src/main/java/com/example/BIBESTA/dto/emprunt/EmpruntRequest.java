package com.example.BIBESTA.dto.emprunt;

// Ce qu'Angular envoie pour créer un emprunt
public record EmpruntRequest(
        Integer utilisateurId,
        Integer exemplaireId) {
}