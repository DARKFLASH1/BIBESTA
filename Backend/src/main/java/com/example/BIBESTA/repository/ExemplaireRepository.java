package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Exemplaire;
import com.example.BIBESTA.model.Exemplaire.StatutDisponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExemplaireRepository extends JpaRepository<Exemplaire, Integer> {

        // ── Fetch join : l'exemplaire est sérialisé avec son livre ────────────
        // (frontend : e.livre.titre).
        @EntityGraph(attributePaths = "livre")
        List<Exemplaire> findAll();

        @Override
        @EntityGraph(attributePaths = "livre")
        Optional<Exemplaire> findById(Integer id);

        // Verrou pessimiste : sérialise l'emprunt d'un même exemplaire (TOCTOU).
        // Deux requêtes concurrentes sur le même exemplaire se mettent en file ;
        // la seconde voit l'état mis à jour par la première (PESSIMISTIC_WRITE).
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT x FROM Exemplaire x WHERE x.id = :id")
        Optional<Exemplaire> findByIdVerrouille(@Param("id") Integer id);

        // Tous les exemplaires d'un livre
        // SELECT * FROM exemplaire WHERE livre_id = ?
        @EntityGraph(attributePaths = "livre")
        List<Exemplaire> findByLivreId(Integer livreId);

        // Tous les exemplaires disponibles d'un livre
        // SELECT * FROM exemplaire WHERE livre_id = ? AND statut_disponibilite =
        // 'DISPONIBLE'
        @EntityGraph(attributePaths = "livre")
        List<Exemplaire> findByLivreIdAndStatutDisponibilite(
                        Integer livreId, StatutDisponibilite statutDisponibilite);

        // Compte les exemplaires disponibles d'un livre
        long countByLivreIdAndStatutDisponibilite(
                        Integer livreId, StatutDisponibilite statutDisponibilite);

        // Tous les exemplaires par statut de disponibilité
        @EntityGraph(attributePaths = "livre")
        List<Exemplaire> findByStatutDisponibilite(StatutDisponibilite statutDisponibilite);

        // Compte les exemplaires par statut de disponibilité (toutes exemplaires
        // confondues)
        long countByStatutDisponibilite(StatutDisponibilite statutDisponibilite);
}