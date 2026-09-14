package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Paiement;
import com.example.BIBESTA.model.Paiement.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    // ── Fetch joins : sérialisation complète abonnement ET amende ────────────
    // Un paiement peut être lié à un abonnement (→utilisateur) ou à une amende
    // (→emprunt→exemplaire→livre). On pré-charge les deux branches en une requête.
    @EntityGraph(attributePaths = {
            "abonnement",
            "abonnement.utilisateur",
            "amende",
            "amende.emprunt",
            "amende.emprunt.utilisateur",
            "amende.emprunt.exemplaire",
            "amende.emprunt.exemplaire.livre"})
    List<Paiement> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "abonnement",
            "abonnement.utilisateur",
            "amende",
            "amende.emprunt",
            "amende.emprunt.utilisateur",
            "amende.emprunt.exemplaire",
            "amende.emprunt.exemplaire.livre"})
    Optional<Paiement> findById(Integer id);

    // Paiements d'un abonnement
    @EntityGraph(attributePaths = {"abonnement", "abonnement.utilisateur"})
    List<Paiement> findByAbonnementId(Integer abonnementId);

    // Paiement d'une amende
    @EntityGraph(attributePaths = {
            "amende",
            "amende.emprunt",
            "amende.emprunt.utilisateur",
            "amende.emprunt.exemplaire",
            "amende.emprunt.exemplaire.livre"})
    Optional<Paiement> findByAmendeId(Integer amendeId);

    // Paiements par statut
    @EntityGraph(attributePaths = {
            "abonnement",
            "abonnement.utilisateur",
            "amende",
            "amende.emprunt",
            "amende.emprunt.utilisateur",
            "amende.emprunt.exemplaire",
            "amende.emprunt.exemplaire.livre"})
    List<Paiement> findByStatut(Statut statut);

    // Vérifie si une amende a déjà été payée
    boolean existsByAmendeId(Integer amendeId);

    // Paiements d'un utilisateur via son abonnement
    @EntityGraph(attributePaths = {"abonnement", "abonnement.utilisateur"})
    List<Paiement> findByAbonnementUtilisateurId(Integer utilisateurId);

    // Retourne TOUS les paiements d'un utilisateur :
    // ceux liés à ses abonnements + ceux liés à ses amendes
    // @Query = requête JPQL personnalisée car on cherche dans deux relations
    // différentes. LEFT JOIN FETCH = toutes les relations lazy sont chargées
    // dans la même requête SQL (pas de N+1 à la sérialisation).
    @Query("SELECT p FROM Paiement p "
            + "LEFT JOIN FETCH p.abonnement a "
            + "LEFT JOIN FETCH a.utilisateur au "
            + "LEFT JOIN FETCH p.amende am "
            + "LEFT JOIN FETCH am.emprunt e "
            + "LEFT JOIN FETCH e.utilisateur eu "
            + "LEFT JOIN FETCH e.exemplaire ex "
            + "LEFT JOIN FETCH ex.livre l "
            + "WHERE (p.abonnement IS NOT NULL AND p.abonnement.utilisateur.id = :utilisateurId) "
            + "OR (p.amende IS NOT NULL AND p.amende.emprunt.utilisateur.id = :utilisateurId)")
    List<Paiement> findAllByUtilisateurId(
            @Param("utilisateurId") Integer utilisateurId);
}