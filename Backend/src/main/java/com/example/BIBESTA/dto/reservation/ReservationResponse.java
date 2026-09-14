package com.example.BIBESTA.dto.reservation;

import com.example.BIBESTA.model.Reservation.Statut;
import java.time.LocalDate;

// Ce qu'Angular reçoit pour une réservation.
// Structure IMBRIQUÉE (et non des champs plats comme "utilisateurNom")
// car c'est cette forme que reservation.service.ts attend déjà
// (ex: r.utilisateur.nom, r.livre.titre dans le template HTML).
public record ReservationResponse(
                Integer id,
                LocalDate dateReservation,
                // dateConfirmation : null tant que la réservation est EN_ATTENTE.
                // Nécessaire pour que le template affiche "Disponible depuis le ...".
                LocalDate dateConfirmation,
                Statut statut,
                UtilisateurInfo utilisateur,
                LivreInfo livre) {

        // Sous-fiche "utilisateur" : uniquement les champs utiles à l'affichage,
        // jamais l'entité Utilisateur complète (qui contient le mot de passe haché).
        public record UtilisateurInfo(
                        Integer id,
                        String nom,
                        String prenom,
                        String identifiant) {
        }

        // Sous-fiche "livre" : uniquement les champs utiles à l'affichage.
        public record LivreInfo(
                        Integer id,
                        String titre,
                        String auteur) {
        }
}