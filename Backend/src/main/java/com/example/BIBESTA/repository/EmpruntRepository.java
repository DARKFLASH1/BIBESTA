package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Emprunt;
import com.example.BIBESTA.model.Emprunt.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {

        // ── Fetch joins : chargement eagerly des relations lazy nécessaires ───
        // Evite les N+1 pour les listes sérialisées par Jackson (EmpruntResponse
        // lit e.getUtilisateur(), e.getExemplaire().getLivre()). Les appels
        // @EntityGraph déclenchent des LEFT JOIN  en une seule requête SQL
        // au lieu d'une requête par ligne × relation.
        @EntityGraph(attributePaths = {"utilisateur", "exemplaire", "exemplaire.livre"})
        List<Emprunt> findAll();

        @Override
        @EntityGraph(attributePaths = {"utilisateur", "exemplaire", "exemplaire.livre"})
        Optional<Emprunt> findById(Integer id);

        @EntityGraph(attributePaths = {"utilisateur", "exemplaire", "exemplaire.livre"})
        List<Emprunt> findByUtilisateurId(Integer utilisateurId);

        @EntityGraph(attributePaths = {"utilisateur", "exemplaire", "exemplaire.livre"})
        List<Emprunt> findByUtilisateurIdAndStatut(
                        Integer utilisateurId,
                        Statut statut);

        @EntityGraph(attributePaths = {"utilisateur", "exemplaire", "exemplaire.livre"})
        List<Emprunt> findByStatut(Statut statut);

        @EntityGraph(attributePaths = {"utilisateur", "exemplaire", "exemplaire.livre"})
        List<Emprunt> findByStatutAndDateRetourPrevueBefore(
                        Statut statut,
                        LocalDate date);

        // Vérifie si un exemplaire est actuellement emprunté
        boolean existsByExemplaireIdAndStatut(
                        Integer exemplaireId,
                        Statut statut);

        // ── Requête unique : existe-t-il un emprunt EN_COURS pour ce livre ? ─
        // Remplace la boucle N+1 dans LivreService.isLivreEmprunte.
        @Query("SELECT COUNT(e) > 0 FROM Emprunt e "
                        + "JOIN e.exemplaire x "
                        + "WHERE x.livre.id = :livreId AND e.statut = :statut")
        boolean existsByLivreIdAndStatut(
                        @Param("livreId") Integer livreId,
                        @Param("statut") Statut statut);

        // Compte les emprunts par statut (raccourci plus léger que
        // findByStatut().size())
        long countByStatut(Statut statut);
        
        // Récupère les derniers emprunts triés par date décroissante
        // JOIN FETCH = charge utilisateur + exemplaire.livre dans la même requête
        // (au lieu d'une requête par emprunt × relation = N+1).
        @Query("SELECT e FROM Emprunt e "
                + "JOIN FETCH e.utilisateur u "
                + "JOIN FETCH e.exemplaire x "
                + "JOIN FETCH x.livre l "
                + "ORDER BY e.dateDebut DESC")
        List<Emprunt> findTopByOrderByDateDebutDesc(Pageable pageable);

        // ── Agrégations JPQL (StatistiqueService) ─────────────────────────────
        // Groupe par mois les emprunts démarrés depuis une date donnée.
        // Renvoie des lignes [year (int), month (int), count (long)].
        // Fonctionne sur H2 (tests) et MySQL (prod) grâce à YEAR()/MONTH() standards.
        @Query("SELECT YEAR(e.dateDebut), MONTH(e.dateDebut), COUNT(e) "
                + "FROM Emprunt e "
                + "WHERE e.dateDebut >= :debut "
                + "GROUP BY YEAR(e.dateDebut), MONTH(e.dateDebut)")
        List<Object[]> countEmpruntsParMois(
                @Param("debut") LocalDate debut);

        // Top 5 des titres les plus empruntés (tous statuts confondus).
        // Pageable permet de borner avec setMaxResults(5) → top 5.
        @Query("SELECT l.titre, COUNT(e) FROM Emprunt e "
                + "JOIN e.exemplaire x "
                + "JOIN x.livre l "
                + "GROUP BY l.titre "
                + "ORDER BY COUNT(e) DESC")
        List<Object[]> findTopLivresEmpruntes(Pageable pageable);
}