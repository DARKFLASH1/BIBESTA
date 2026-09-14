package com.example.BIBESTA.dto.emprunt;

import jakarta.validation.constraints.NotNull;

// Ce qu'Angular envoie pour créer un emprunt
public record EmpruntRequest(
        @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
        Integer utilisateurId,
        @NotNull(message = "L'identifiant de l'exemplaire est obligatoire")
        Integer exemplaireId) {
}