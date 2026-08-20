package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Emprunt;
import com.example.BIBESTA.model.Emprunt.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {

        // Tous les emprunts d'un utilisateur
        List<Emprunt> findByUtilisateurId(Integer utilisateurId);

        // Emprunts d'un utilisateur par statut
        // Ex: tous les emprunts EN_COURS de Bachar
        List<Emprunt> findByUtilisateurIdAndStatut(
                        Integer utilisateurId,
                        Statut statut);

        // Tous les emprunts par statut
        List<Emprunt> findByStatut(Statut statut);

        // Emprunts en retard :
        // EN_COURS et dont la date de retour prévue est dépassée
        List<Emprunt> findByStatutAndDateRetourPrevueBefore(
                        Statut statut,
                        LocalDate date);

        // Vérifie si un exemplaire est actuellement emprunté
        boolean existsByExemplaireIdAndStatut(
                        Integer exemplaireId,
                        Statut statut);

        // Compte les emprunts par statut (raccourci plus léger que
        // findByStatut().size())
        long countByStatut(Statut statut);
        
        // Récupère les derniers emprunts triés par date décroissante
        @Query("SELECT e FROM Emprunt e ORDER BY e.dateDebut DESC")
        List<Emprunt> findTopByOrderByDateDebutDesc(Pageable pageable);
}