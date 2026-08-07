package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Exemplaire;
import com.example.BIBESTA.model.Exemplaire.StatutDisponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExemplaireRepository extends JpaRepository<Exemplaire, Integer> {

    // Tous les exemplaires d'un livre
    // SELECT * FROM exemplaire WHERE livre_id = ?
    List<Exemplaire> findByLivreId(Integer livreId);

    // Tous les exemplaires disponibles d'un livre
    // SELECT * FROM exemplaire WHERE livre_id = ? AND statut_disponibilite =
    // 'DISPONIBLE'
    List<Exemplaire> findByLivreIdAndStatutDisponibilite(
            Integer livreId, StatutDisponibilite statutDisponibilite);

    // Compte les exemplaires disponibles d'un livre
    long countByLivreIdAndStatutDisponibilite(
            Integer livreId, StatutDisponibilite statutDisponibilite);

    // Tous les exemplaires par statut de disponibilité
    List<Exemplaire> findByStatutDisponibilite(StatutDisponibilite statutDisponibilite);
}