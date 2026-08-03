package com.example.BIBESTA.dto.emprunt;

import com.example.BIBESTA.model.Emprunt.Statut;
import java.time.LocalDate;

// Ce qu'Angular reçoit pour un emprunt
public record EmpruntResponse(
        Integer id,
        LocalDate dateDebut,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourReelle,
        Statut statut,
        // Infos utilisateur (pas l'objet entier)
        Integer utilisateurId,
        String utilisateurNom,
        String utilisateurPrenom,
        // Infos livre (pas l'objet entier)
        Integer livreId,
        String livreTitre,
        String livreAuteur,
        String exemplaireNumero) {
}