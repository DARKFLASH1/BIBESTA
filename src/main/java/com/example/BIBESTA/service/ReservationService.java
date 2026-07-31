package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.model.Reservation.Statut;
import com.example.BIBESTA.model.Exemplaire.Etat;
import com.example.BIBESTA.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivreRepository livreRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;

    // Toutes les réservations
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    // Une réservation par id
    public Optional<Reservation> findById(Integer id) {
        return reservationRepository.findById(id);
    }

    // Réservations d'un utilisateur
    public List<Reservation> findByUtilisateurId(Integer utilisateurId) {
        return reservationRepository.findByUtilisateurId(utilisateurId);
    }

    // Réservations EN_ATTENTE d'un utilisateur
    public List<Reservation> findEnAttenteByUtilisateurId(Integer utilisateurId) {
        return reservationRepository.findByUtilisateurIdAndStatut(
                utilisateurId,
                Statut.EN_ATTENTE);
    }

    // CRÉER UNE RÉSERVATION
    public Reservation creerReservation(Integer utilisateurId, Integer livreId) {

        // 1. Vérifie que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Vérifie que le livre existe
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        // 3. Vérifie que l'utilisateur n'a pas déjà réservé ce livre
        if (reservationRepository.existsByUtilisateurIdAndLivreIdAndStatut(
                utilisateurId, livreId, Statut.EN_ATTENTE)) {
            throw new RuntimeException(
                    "Vous avez déjà une réservation en attente pour ce livre");
        }

        // 4. Crée la réservation
        Reservation reservation = new Reservation();
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDate.now());
        reservation.setStatut(Statut.EN_ATTENTE);

        Reservation saved = reservationRepository.save(reservation);

        // 5. Notification de confirmation
        notificationService.creer(
                utilisateurId,
                "RESERVATION",
                "Votre réservation pour '" + livre.getTitre() +
                        "' est enregistrée. Nous vous préviendrons dès qu'un exemplaire est disponible.");

        // Enregistre dans l'historique
        historiqueService.enregistrerReservation(
                utilisateurId,
                saved.getId(),
                livreId);

        // Notification de confirmation
        notificationService.creer(
                utilisateurId,
                "RESERVATION",
                "Votre réservation pour '" + livre.getTitre() +
                        "' est enregistrée. Nous vous préviendrons dès qu'un exemplaire est disponible.");

        return saved;
    }

    // CONFIRMER UNE RÉSERVATION
    // Appelée automatiquement quand un exemplaire devient disponible
    public void confirmerReservationsSiDisponible(Integer livreId) {

        // Cherche les exemplaires disponibles du livre
        List<Exemplaire> disponibles = exemplaireRepository
                .findByLivreIdAndEtat(livreId, Etat.DISPONIBLE);

        if (disponibles.isEmpty()) {
            // Pas d'exemplaire disponible → on ne fait rien
            return;
        }

        // Cherche la première réservation EN_ATTENTE pour ce livre
        List<Reservation> enAttente = reservationRepository
                .findByLivreIdAndStatut(livreId, Statut.EN_ATTENTE);

        if (enAttente.isEmpty()) {
            // Personne n'attend ce livre
            return;
        }

        // Confirme la première réservation en attente
        Reservation premiere = enAttente.get(0);
        premiere.setStatut(Statut.CONFIRMEE);
        reservationRepository.save(premiere);

        // Notifie l'utilisateur
        notificationService.creer(
                premiere.getUtilisateur().getId(),
                "RESERVATION_DISPONIBLE",
                "Bonne nouvelle ! Le livre '" + premiere.getLivre().getTitre() +
                        "' est disponible. Venez le récupérer dans les 48h.");
    }

    // ANNULER UNE RÉSERVATION
    public Reservation annuler(Integer reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // On ne peut annuler que les réservations EN_ATTENTE
        if (reservation.getStatut() != Statut.EN_ATTENTE) {
            throw new RuntimeException(
                    "Impossible d'annuler une réservation " + reservation.getStatut());
        }

        reservation.setStatut(Statut.ANNULEE);

        // Notifie l'utilisateur
        notificationService.creer(
                reservation.getUtilisateur().getId(),
                "ANNULATION",
                "Votre réservation pour '" +
                        reservation.getLivre().getTitre() + "' a été annulée.");

        return reservationRepository.save(reservation);
    }
}