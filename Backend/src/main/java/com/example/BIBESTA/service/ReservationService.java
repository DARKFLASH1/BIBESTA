package com.example.BIBESTA.service;

import com.example.BIBESTA.exception.BusinessException;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.*;
import com.example.BIBESTA.model.Reservation.Statut;
import com.example.BIBESTA.model.Exemplaire.Etat;
import com.example.BIBESTA.model.Exemplaire.StatutDisponibilite;
import com.example.BIBESTA.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

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
        @Transactional
        public Reservation creerReservation(Integer utilisateurId, Integer livreId) {

                // 1. Vérifie que l'utilisateur existe
                Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

                // 1.1 Vérifie que le compte est ACTIF (RG5)
                if (utilisateur.getStatut() != Utilisateur.Statut.ACTIF) {
                        throw new BusinessException(
                                        "Ce compte est " + utilisateur.getStatut().name().toLowerCase() +
                                                        ". Il ne peut pas réserver.");
                }

                // 2. Vérifie que le livre existe
                Livre livre = livreRepository.findById(livreId)
                                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé"));

                // 3. Vérifie que l'utilisateur n'a pas déjà réservé ce livre
                if (reservationRepository.existsByUtilisateurIdAndLivreIdAndStatut(
                                utilisateurId, livreId, Statut.EN_ATTENTE)) {
                        throw new BusinessException(
                                        "Vous avez déjà une réservation en attente pour ce livre");
                }

                // 4. Crée la réservation
                Reservation reservation = new Reservation();
                reservation.setUtilisateur(utilisateur);
                reservation.setLivre(livre);
                reservation.setDateReservation(LocalDate.now());
                reservation.setStatut(Statut.EN_ATTENTE);

                Reservation saved = reservationRepository.save(reservation);

                // 5. Notification de confirmation (une seule fois)
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

                return saved;
        }

        // CONFIRMER UNE RÉSERVATION
        // Appelée automatiquement quand un exemplaire devient disponible
        @Transactional
        public void confirmerReservationsSiDisponible(Integer livreId) {

                // Cherche les exemplaires disponibles du livre
                List<Exemplaire> disponibles = exemplaireRepository
                                .findByLivreIdAndStatutDisponibilite(livreId, StatutDisponibilite.DISPONIBLE);

                if (disponibles.isEmpty()) {
                        // Pas d'exemplaire disponible → on ne fait rien
                        return;
                }

                // Cherche la première réservation EN_ATTENTE pour ce livre
                // Triée par date de réservation (FIFO - premier arrivé, premier servi)
                List<Reservation> enAttente = reservationRepository
                                .findByLivreIdAndStatutOrderByDateReservationAsc(livreId, Statut.EN_ATTENTE);

                if (enAttente.isEmpty()) {
                        // Personne n'attend ce livre
                        return;
                }

                // Confirme la première réservation en attente
                Reservation premiere = enAttente.get(0);
                premiere.setStatut(Statut.CONFIRMEE);
                premiere.setDateConfirmation(LocalDate.now());
                reservationRepository.save(premiere);

                // Verrouille l'exemplaire disponible → RESERVE
                // pour que personne d'autre ne puisse l'emprunter entre-temps
                Exemplaire exemplaire = disponibles.get(0);
                exemplaire.setEtat(Etat.RESERVE);
                exemplaireRepository.save(exemplaire);

                // Notifie l'utilisateur
                notificationService.creer(
                                premiere.getUtilisateur().getId(),
                                "RESERVATION_DISPONIBLE",
                                "Bonne nouvelle ! Le livre '" + premiere.getLivre().getTitre() +
                                                "' est disponible. Venez le récupérer dans les 48h.");
        }

        // EXPIRER LES RÉSERVATIONS CONFIRMÉES NON RETIRÉES APRÈS 48H
        // Appelée automatiquement chaque jour par ScheduledTasks
        @Transactional
        public void expirerReservationsConfirmees() {

                // Trouve toutes les réservations confirmées
                List<Reservation> confirmees = reservationRepository.findByStatut(Statut.CONFIRMEE);

                for (Reservation reservation : confirmees) {
                        // Si la réservation a plus de 48h (2 jours) → elle expire
                        if (reservation.getDateConfirmation() == null)
                                continue;

                        if (reservation.getDateConfirmation().plusDays(2).isBefore(LocalDate.now())) {
                                reservation.setStatut(Statut.ANNULEE);
                                reservationRepository.save(reservation);

                                // Libère l'exemplaire réservé → DISPONIBLE
                                // et relance la réservation suivante dans la file
                                List<Exemplaire> reserves = exemplaireRepository
                                                .findByLivreIdAndStatutDisponibilite(
                                                                reservation.getLivre().getId(),
                                                                StatutDisponibilite.RESERVE);
                                for (Exemplaire exemplaire : reserves) {
                                        exemplaire.setEtat(Etat.DISPONIBLE);
                                        exemplaireRepository.save(exemplaire);
                                }

                                // Notifie l'utilisateur
                                notificationService.creer(
                                                reservation.getUtilisateur().getId(),
                                                "RESERVATION_EXPIREE",
                                                "Votre réservation pour '" + reservation.getLivre().getTitre() +
                                                                "' a expiré. Vous ne l'avez pas récupérée dans les 48h.");

                                // Relance la confirmation pour le lecteur suivant
                                confirmerReservationsSiDisponible(reservation.getLivre().getId());
                        }
                }
        }

        // ANNULER UNE RÉSERVATION
        @Transactional
        public Reservation annuler(Integer reservationId) {

                Reservation reservation = reservationRepository.findById(reservationId)
                                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

                // On ne peut annuler que les réservations EN_ATTENTE
                if (reservation.getStatut() != Statut.EN_ATTENTE) {
                        throw new BusinessException(
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