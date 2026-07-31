package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoriqueService {

    private final HistoriqueRepository historiqueRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;
    private final LivreRepository livreRepository;
    private final ReservationRepository reservationRepository;

    // Tout l'historique
    public List<Historique> findAll() {
        return historiqueRepository.findAll();
    }

    // Un historique par id
    public Optional<Historique> findById(Integer id) {
        return historiqueRepository.findById(id);
    }

    // Historique d'un utilisateur
    public List<Historique> findByUtilisateurId(Integer utilisateurId) {
        return historiqueRepository
                .findByUtilisateurIdOrderByDateMouvementDesc(utilisateurId);
    }

    // Historique d'un livre
    public List<Historique> findByLivreId(Integer livreId) {
        return historiqueRepository
                .findByLivreIdOrderByDateMouvementDesc(livreId);
    }

    // Historique par type
    public List<Historique> findByType(String type) {
        return historiqueRepository
                .findByTypeOrderByDateMouvementDesc(type);
    }

    // ENREGISTRER UN MOUVEMENT
    // Méthode centrale appelée par tous les autres services
    public Historique enregistrer(
            Integer utilisateurId,
            String type,
            String description,
            Integer empruntId,
            Integer livreId,
            Integer reservationId) {

        // 1. Vérifie que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository
                .findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé"));

        // 2. Crée l'entrée historique
        Historique historique = new Historique();
        historique.setUtilisateur(utilisateur);
        historique.setType(type);
        historique.setDescription(description);
        historique.setDateMouvement(LocalDateTime.now());

        // 3. Associe les entités optionnelles si elles sont fournies
        if (empruntId != null) {
            empruntRepository.findById(empruntId)
                    .ifPresent(historique::setEmprunt);
        }

        if (livreId != null) {
            livreRepository.findById(livreId)
                    .ifPresent(historique::setLivre);
        }

        if (reservationId != null) {
            reservationRepository.findById(reservationId)
                    .ifPresent(historique::setReservation);
        }

        return historiqueRepository.save(historique);
    }

    // Raccourcis pour les cas les plus fréquents

    // Enregistre un emprunt
    public void enregistrerEmprunt(
            Integer utilisateurId,
            Integer empruntId,
            Integer livreId) {
        enregistrer(
                utilisateurId,
                "EMPRUNT",
                "Emprunt du livre id=" + livreId,
                empruntId,
                livreId,
                null);
    }

    // Enregistre un retour
    public void enregistrerRetour(
            Integer utilisateurId,
            Integer empruntId,
            Integer livreId) {
        enregistrer(
                utilisateurId,
                "RETOUR",
                "Retour du livre id=" + livreId,
                empruntId,
                livreId,
                null);
    }

    // Enregistre une réservation
    public void enregistrerReservation(
            Integer utilisateurId,
            Integer reservationId,
            Integer livreId) {
        enregistrer(
                utilisateurId,
                "RESERVATION",
                "Réservation du livre id=" + livreId,
                null,
                livreId,
                reservationId);
    }

    // Enregistre une connexion
    public void enregistrerConnexion(Integer utilisateurId) {
        enregistrer(
                utilisateurId,
                "CONNEXION",
                "Connexion de l'utilisateur",
                null,
                null,
                null);
    }

    // Enregistre un paiement
    public void enregistrerPaiement(
            Integer utilisateurId,
            String description) {
        enregistrer(
                utilisateurId,
                "PAIEMENT",
                description,
                null,
                null,
                null);
    }
}