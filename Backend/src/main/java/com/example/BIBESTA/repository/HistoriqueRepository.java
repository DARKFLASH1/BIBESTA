package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Historique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Integer> {

    // Tout l'historique d'un utilisateur
    // SELECT * FROM historique WHERE utilisateur_id = ?
    // ORDER BY dateMouvement DESC
    List<Historique> findByUtilisateurIdOrderByDateMouvementDesc(
            Integer utilisateurId);

    // Historique par type d'action
    // Ex: tous les EMPRUNT, tous les RETOUR...
    List<Historique> findByTypeOrderByDateMouvementDesc(String type);

    // Historique d'un livre précis
    List<Historique> findByLivreIdOrderByDateMouvementDesc(Integer livreId);

    // Historique entre deux dates
    List<Historique> findByDateMouvementBetweenOrderByDateMouvementDesc(
            LocalDateTime debut,
            LocalDateTime fin);

    // Historique d'un utilisateur par type
    List<Historique> findByUtilisateurIdAndTypeOrderByDateMouvementDesc(
            Integer utilisateurId,
            String type);
}