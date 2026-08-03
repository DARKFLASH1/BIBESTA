package com.example.BIBESTA.dto.reservation;

// Ce qu'Angular envoie pour créer une réservation
public record ReservationRequest(
        Integer utilisateurId,
        Integer livreId) {
}