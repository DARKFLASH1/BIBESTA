package com.example.BIBESTA.dto.reservation;

import com.example.BIBESTA.model.Reservation.Statut;
import java.time.LocalDate;

// Ce qu'Angular reçoit pour une réservation
public record ReservationResponse(
        Integer id,
        LocalDate dateReservation,
        Statut statut,
        // Infos utilisateur
        Integer utilisateurId,
        String utilisateurNom,
        String utilisateurPrenom,
        // Infos livre
        Integer livreId,
        String livreTitre) {
}