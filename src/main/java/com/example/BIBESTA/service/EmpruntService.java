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

@Service
@RequiredArgsConstructor
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final NotificationService notificationService;
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
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé"));

        // 2. Vérifie que l'exemplaire existe
        Exemplaire exemplaire = exemplaireRepository.findById(exemplaireId)
                .orElseThrow(() -> new RuntimeException(
                        "Exemplaire non trouvé"));

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
        // Durée standard : 14 jours
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(14));
        emprunt.setStatut(Statut.EN_COURS);

        // 6. Change l'état de l'exemplaire → EMPRUNTE
        exemplaire.setEtat(Etat.EMPRUNTE);
        exemplaireRepository.save(exemplaire);

        // 7. Sauvegarde l'emprunt
        Emprunt saved = empruntRepository.save(emprunt);

        // 8. Envoie une notification de confirmation
        notificationService.creer(
                utilisateurId,
                "EMPRUNT",
                "Vous avez emprunté un livre. Date de retour prévue : "
                        + emprunt.getDateRetourPrevue());

        return saved;
    }

    // ENREGISTRER UN RETOUR
    public Emprunt enregistrerRetour(Integer empruntId) {

        // 1. Vérifie que l'emprunt existe
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        // 2. Vérifie que l'emprunt est bien EN_COURS
        if (emprunt.getStatut() != Statut.EN_COURS) {
            throw new RuntimeException(
                    "Cet emprunt est déjà clôturé : " + emprunt.getStatut());
        }

        // 3. Enregistre la date de retour réelle
        emprunt.setDateRetourReelle(LocalDate.now());

        // 4. Change le statut → RETOURNE
        emprunt.setStatut(Statut.RETOURNE);

        // 5. Remet l'exemplaire → DISPONIBLE
        Exemplaire exemplaire = emprunt.getExemplaire();
        exemplaire.setEtat(Etat.DISPONIBLE);
        exemplaireRepository.save(exemplaire);

        // 6. Envoie notification de confirmation
        notificationService.creer(
                emprunt.getUtilisateur().getId(),
                "RETOUR",
                "Retour enregistré avec succès. Merci !");

        return empruntRepository.save(emprunt);
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