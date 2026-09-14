package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Abonnement;
import com.example.BIBESTA.model.Abonnement.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Integer> {

    // ── Fetch join : l'abonnement est sérialisé avec son utilisateur ─────────
    // (frontend : a.utilisateur.nom / a.utilisateur.prenom).
    @EntityGraph(attributePaths = "utilisateur")
    List<Abonnement> findAll();

    @Override
    @EntityGraph(attributePaths = "utilisateur")
    Optional<Abonnement> findById(Integer id);

    // Tous les abonnements d'un utilisateur
    // SELECT * FROM abonnement WHERE utilisateur_id = ?
    @EntityGraph(attributePaths = "utilisateur")
    List<Abonnement> findByUtilisateurId(Integer utilisateurId);

    // Abonnements par statut (EN_ATTENTE, PAYE, EXPIRE)
    @EntityGraph(attributePaths = "utilisateur")
    List<Abonnement> findByStatutPaiement(StatutPaiement statut);

    // Abonnements actifs d'un utilisateur
    // (payés et dont la date de fin est après aujourd'hui)
    @EntityGraph(attributePaths = "utilisateur")
    List<Abonnement> findByUtilisateurIdAndStatutPaiement(
            Integer utilisateurId,
            StatutPaiement statut);

    // Abonnements qui expirent avant une date donnée
    // Utile pour envoyer des rappels avant expiration
    @EntityGraph(attributePaths = "utilisateur")
    List<Abonnement> findByDateFinBefore(LocalDate date);

    // Vérifie si un utilisateur a un abonnement actif
    boolean existsByUtilisateurIdAndStatutPaiementAndDateFinAfter(
            Integer utilisateurId,
            StatutPaiement statut,
            LocalDate date);

    // Vérifie si un abonnement PAYE reste actif après la date de début d'un nouvel
    // abonnement (dateFin >= dateDebut du nouveau) → chevauchement détecté en P2.10.
    boolean existsByUtilisateurIdAndStatutPaiementAndDateFinGreaterThanEqual(
            Integer utilisateurId,
            StatutPaiement statut,
            LocalDate dateDebut);
}