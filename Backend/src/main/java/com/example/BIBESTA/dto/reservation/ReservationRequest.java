package com.example.BIBESTA.dto.reservation;

import jakarta.validation.constraints.NotNull;

// Ce qu'Angular envoie pour créer une réservation
public record ReservationRequest(
        @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
        Integer utilisateurId,
        @NotNull(message = "L'identifiant du livre est obligatoire")
        Integer livreId) {
}