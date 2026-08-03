package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Exemplaire;
import com.example.BIBESTA.model.Exemplaire.Etat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExemplaireRepository extends JpaRepository<Exemplaire, Integer> {

    // Tous les exemplaires d'un livre
    // SELECT * FROM exemplaire WHERE livre_id = ?
    List<Exemplaire> findByLivreId(Integer livreId);

    // Tous les exemplaires disponibles d'un livre
    // SELECT * FROM exemplaire WHERE livre_id = ? AND etat = 'DISPONIBLE'
    List<Exemplaire> findByLivreIdAndEtat(Integer livreId, Etat etat);

    // Compte les exemplaires disponibles d'un livre
    long countByLivreIdAndEtat(Integer livreId, Etat etat);

    // Tous les exemplaires par état
    List<Exemplaire> findByEtat(Etat etat);
}