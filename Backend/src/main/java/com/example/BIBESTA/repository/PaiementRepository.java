package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Paiement;
import com.example.BIBESTA.model.Paiement.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    // Paiements d'un abonnement
    List<Paiement> findByAbonnementId(Integer abonnementId);

    // Paiement d'une amende
    Optional<Paiement> findByAmendeId(Integer amendeId);

    // Paiements par statut
    List<Paiement> findByStatut(Statut statut);

    // Vérifie si une amende a déjà été payée
    boolean existsByAmendeId(Integer amendeId);

    // Paiements d'un utilisateur via son abonnement
    List<Paiement> findByAbonnementUtilisateurId(Integer utilisateurId);

    // Retourne TOUS les paiements d'un utilisateur :
    // ceux liés à ses abonnements + ceux liés à ses amendes
    // @Query = requête JPQL personnalisée car on cherche dans deux relations
    // différentes
    @Query("SELECT p FROM Paiement p WHERE " +
            "(p.abonnement IS NOT NULL AND p.abonnement.utilisateur.id = :utilisateurId) OR " +
            "(p.amende IS NOT NULL AND p.amende.emprunt.utilisateur.id = :utilisateurId)")
    List<Paiement> findAllByUtilisateurId(
            @org.springframework.data.repository.query.Param("utilisateurId") Integer utilisateurId);
}