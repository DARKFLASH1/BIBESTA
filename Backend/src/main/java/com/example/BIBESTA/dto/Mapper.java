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
    // Appeler .getUtilisateur().getNom() ici force Hibernate à vraiment
    // charger les données (au lieu de laisser passer le proxy technique
    // "ByteBuddyInterceptor" que Jackson ne sait pas transformer en JSON).
    public ReservationResponse toReservationResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getDateReservation(),
                r.getDateConfirmation(),
                r.getStatut(),
                new ReservationResponse.UtilisateurInfo(
                        r.getUtilisateur().getId(),
                        r.getUtilisateur().getNom(),
                        r.getUtilisateur().getPrenom(),
                        r.getUtilisateur().getIdentifiant()),
                new ReservationResponse.LivreInfo(
                        r.getLivre().getId(),
                        r.getLivre().getTitre(),
                        r.getLivre().getAuteur()));
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