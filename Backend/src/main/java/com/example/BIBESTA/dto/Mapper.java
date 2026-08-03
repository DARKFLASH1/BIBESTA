package com.example.BIBESTA.dto;

import com.example.BIBESTA.dto.utilisateur.UtilisateurResponse;
import com.example.BIBESTA.dto.emprunt.EmpruntResponse;
import com.example.BIBESTA.dto.reservation.ReservationResponse;
import com.example.BIBESTA.dto.livre.LivreResponse;
import com.example.BIBESTA.model.*;
import org.springframework.stereotype.Component;

@Component // Spring gère cet objet
public class Mapper {

    // Convertit Utilisateur → UtilisateurResponse
    public UtilisateurResponse toUtilisateurResponse(Utilisateur u) {
        return new UtilisateurResponse(
                u.getId(),
                u.getNom(),
                u.getPrenom(),
                u.getEmail(),
                u.getIdentifiant(),
                u.getContact(),
                u.getDateNaissance(),
                u.getSexe(),
                u.getRole());
    }

    // Convertit Emprunt → EmpruntResponse
    public EmpruntResponse toEmpruntResponse(Emprunt e) {
        // Récupère le livre via l'exemplaire
        Livre livre = e.getExemplaire().getLivre();

        return new EmpruntResponse(
                e.getId(),
                e.getDateDebut(),
                e.getDateRetourPrevue(),
                e.getDateRetourReelle(),
                e.getStatut(),
                e.getUtilisateur().getId(),
                e.getUtilisateur().getNom(),
                e.getUtilisateur().getPrenom(),
                livre.getId(),
                livre.getTitre(),
                livre.getAuteur(),
                e.getExemplaire().getNumExemplaire());
    }

    // Convertit Reservation → ReservationResponse
    public ReservationResponse toReservationResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getDateReservation(),
                r.getStatut(),
                r.getUtilisateur().getId(),
                r.getUtilisateur().getNom(),
                r.getUtilisateur().getPrenom(),
                r.getLivre().getId(),
                r.getLivre().getTitre());
    }

    // Convertit Livre → LivreResponse
    public LivreResponse toLivreResponse(Livre l, long exemplairesDiponibles) {
        return new LivreResponse(
                l.getId(),
                l.getTitre(),
                l.getAuteur(),
                l.getEdition(),
                l.getIsbn(),
                l.getCategorie(),
                l.getGenre(),
                l.getLangue(),
                l.getAnneePublication(),
                l.getNombrePages(),
                exemplairesDiponibles);
    }
}