package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Livre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Integer> {

    // ── Listes complètes (actifs uniquement) ──────────────────────────────

    // Tous les livres actifs (exclut les livres désactivés)
    // Spring génère : SELECT * FROM livre WHERE actif = true
    List<Livre> findByActifTrue();

    // ── Recherches (actifs uniquement) ────────────────────────────────────

    // Recherche par titre — livres actifs seulement
    List<Livre> findByTitreContainingIgnoreCaseAndActifTrue(String titre);

    // Recherche par auteur — livres actifs seulement
    List<Livre> findByAuteurContainingIgnoreCaseAndActifTrue(String auteur);

    // Recherche par ISBN — livres actifs seulement
    Optional<Livre> findByIsbnAndActifTrue(String isbn);

    // Recherche par ISBN sans filtre actif (pour vérifier les doublons à la
    // création)
    Optional<Livre> findByIsbn(String isbn);

    // Recherche par genre — livres actifs seulement
    List<Livre> findByGenreContainingIgnoreCaseAndActifTrue(String genre);

    // Recherche par catégorie — livres actifs seulement
    List<Livre> findByCategorieContainingIgnoreCaseAndActifTrue(String categorie);

    // Recherche par langue — livres actifs seulement
    List<Livre> findByLangueContainingIgnoreCaseAndActifTrue(String langue);

    // Recherche par année — livres actifs seulement
    List<Livre> findByAnneePublicationAndActifTrue(Integer anneePublication);

    // ── Pagination (actifs uniquement) ────────────────────────────────────

    // Liste paginée des livres actifs
    Page<Livre> findByActifTrue(Pageable pageable);

    // Recherche paginée multi-champs (titre OU auteur OU catégorie) — actifs
    // seulement
    Page<Livre> findByActifTrueAndTitreContainingIgnoreCaseOrActifTrueAndAuteurContainingIgnoreCaseOrActifTrueAndCategorieContainingIgnoreCase(
            String titre, String auteur, String categorie, Pageable pageable);

    // Recherche combinée multi-critères (P2.11) : TOUS les critères fournis
    // s'appliquent (ET). Un critère null / vide est simplement ignoré.
    // Avant : searchLivres n'appliquait que le premier critère non vide.
    @Query("SELECT l FROM Livre l WHERE l.actif = true "
            + "AND (:titre IS NULL OR :titre = '' OR LOWER(l.titre) LIKE LOWER(CONCAT('%', :titre, '%'))) "
            + "AND (:auteur IS NULL OR :auteur = '' OR LOWER(l.auteur) LIKE LOWER(CONCAT('%', :auteur, '%'))) "
            + "AND (:isbn IS NULL OR :isbn = '' OR l.isbn = :isbn) "
            + "AND (:genre IS NULL OR :genre = '' OR LOWER(l.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) "
            + "AND (:langue IS NULL OR :langue = '' OR LOWER(l.langue) LIKE LOWER(CONCAT('%', :langue, '%'))) "
            + "AND (:categorie IS NULL OR :categorie = '' OR LOWER(l.categorie) LIKE LOWER(CONCAT('%', :categorie, '%')))")
    List<Livre> searchMulticriteres(
            @Param("titre") String titre,
            @Param("auteur") String auteur,
            @Param("isbn") String isbn,
            @Param("genre") String genre,
            @Param("langue") String langue,
            @Param("categorie") String categorie);
}