package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Exemplaire;
import com.example.BIBESTA.model.Exemplaire.StatutDisponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
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