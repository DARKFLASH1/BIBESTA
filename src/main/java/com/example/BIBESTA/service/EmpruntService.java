package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.model.Emprunt.Statut;
import com.example.BIBESTA.model.Exemplaire.Etat;
import com.example.BIBESTA.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.example.BIBESTA.service.AmendeService;
import com.example.BIBESTA.service.ReservationService;
import com.example.BIBESTA.service.HistoriqueService;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final NotificationService notificationService;
    private final AmendeService amendeService; // ← nouveau
    private final ReservationService reservationService; // ← nouveau
    private final HistoriqueService historiqueService;
    // NotificationService : on envoie des notifications automatiques

    // Retourne tous les emprunts
    public List<Emprunt> findAll() {
        return empruntRepository.findAll();
    }

    // Retourne un emprunt par son id
    public Optional<Emprunt> findById(Integer id) {
        return empruntRepository.findById(id);
    }

    // Retourne tous les emprunts d'un utilisateur
    public List<Emprunt> findByUtilisateurId(Integer utilisateurId) {
        return empruntRepository.findByUtilisateurId(utilisateurId);
    }

    // Retourne les emprunts en cours d'un utilisateur
    public List<Emprunt> findEnCoursByUtilisateurId(Integer utilisateurId) {
        return empruntRepository.findByUtilisateurIdAndStatut(
                utilisateurId,
                Statut.EN_COURS);
    }

    // Retourne tous les emprunts en retard
    public List<Emprunt> findEnRetard() {
        return empruntRepository.findByStatutAndDateRetourPrevueBefore(
                Statut.EN_COURS,
                LocalDate.now());
    }

    // CRÉER UN EMPRUNT
    public Emprunt creerEmprunt(Integer utilisateurId, Integer exemplaireId) {

        // 1. Vérifie que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Vérifie que l'exemplaire existe
        Exemplaire exemplaire = exemplaireRepository.findById(exemplaireId)
                .orElseThrow(() -> new RuntimeException("Exemplaire non trouvé"));

        // 3. Vérifie que l'exemplaire est disponible
        if (exemplaire.getEtat() != Etat.DISPONIBLE) {
            throw new RuntimeException(
                    "Cet exemplaire n'est pas disponible : " + exemplaire.getEtat());
        }

        // 4. Vérifie que l'exemplaire n'est pas déjà emprunté
        if (empruntRepository.existsByExemplaireIdAndStatut(
                exemplaireId, Statut.EN_COURS)) {
            throw new RuntimeException(
                    "Cet exemplaire est déjà en cours d'emprunt");
        }

        // 5. Crée l'emprunt
        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(utilisateur);
        emprunt.setExemplaire(exemplaire);
        emprunt.setDateDebut(LocalDate.now());
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(14));
        emprunt.setStatut(Statut.EN_COURS);

        // 6. Change l'état de l'exemplaire → EMPRUNTE
        exemplaire.setEtat(Etat.EMPRUNTE);
        exemplaireRepository.save(exemplaire);

        // 7. Sauvegarde l'emprunt
        Emprunt saved = empruntRepository.save(emprunt);

        // 8. Enregistre dans l'historique
        historiqueService.enregistrerEmprunt(
                utilisateurId,
                saved.getId(),
                exemplaire.getLivre().getId());

        // 9. Notification de confirmation
        notificationService.creer(
                utilisateurId,
                "EMPRUNT",
                "Vous avez emprunté '" + exemplaire.getLivre().getTitre() +
                        "'. Date de retour prévue : " + emprunt.getDateRetourPrevue());

        return saved;
    }

    // ENREGISTRER UN RETOUR
    public Emprunt enregistrerRetour(Integer empruntId) {

        // 1. Vérifie que l'emprunt existe
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        // 2. Vérifie que l'emprunt est EN_COURS
        if (emprunt.getStatut() != Statut.EN_COURS &&
                emprunt.getStatut() != Statut.EN_RETARD) {
            throw new RuntimeException(
                    "Cet emprunt est déjà clôturé : " + emprunt.getStatut());
        }

        // 3. Enregistre la date de retour réelle
        emprunt.setDateRetourReelle(LocalDate.now());
        emprunt.setStatut(Statut.RETOURNE);

        // 4. Remet l'exemplaire → DISPONIBLE
        Exemplaire exemplaire = emprunt.getExemplaire();
        exemplaire.setEtat(Etat.DISPONIBLE);
        exemplaireRepository.save(exemplaire);

        // 5. Sauvegarde l'emprunt
        Emprunt saved = empruntRepository.save(emprunt);

        // 6. Vérifie s'il y a un retard
        long joursRetard = ChronoUnit.DAYS.between(
                emprunt.getDateRetourPrevue(),
                LocalDate.now());

        if (joursRetard > 0) {
            // Retard détecté → crée une amende automatiquement
            try {
                amendeService.creerAmende(empruntId);
            } catch (RuntimeException e) {
                // Si amende déjà existante → on ignore
                System.out.println("Amende déjà existante : " + e.getMessage());
            }
        } else {
            // Pas de retard → notification de retour normal
            notificationService.creer(
                    emprunt.getUtilisateur().getId(),
                    "RETOUR",
                    "Retour enregistré avec succès. Merci !");
        }

        // 7. Vérifie s'il y a une réservation en attente pour ce livre
        Integer livreId = exemplaire.getLivre().getId();
        reservationService.confirmerReservationsSiDisponible(livreId);

        // 8. Enregistre dans l'historique
        historiqueService.enregistrerRetour(
                emprunt.getUtilisateur().getId(),
                empruntId,
                livreId);

        return saved;
    }

    // METTRE À JOUR LES EMPRUNTS EN RETARD
    // Cette méthode sera appelée automatiquement chaque jour
    public void mettreAJourRetards() {

        List<Emprunt> enRetard = findEnRetard();

        for (Emprunt emprunt : enRetard) {
            // Change le statut → EN_RETARD
            emprunt.setStatut(Statut.EN_RETARD);
            empruntRepository.save(emprunt);

            // Envoie une notification de retard
            notificationService.creer(
                    emprunt.getUtilisateur().getId(),
                    "RETARD",
                    "Votre emprunt est en retard ! " +
                            "Date limite dépassée : " + emprunt.getDateRetourPrevue());
        }
    }
}