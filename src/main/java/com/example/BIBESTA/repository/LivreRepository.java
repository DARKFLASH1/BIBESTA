package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Integer> {

    // Recherche par titre (contient le mot recherché, insensible à la casse)
    List<Livre> findByTitreContainingIgnoreCase(String titre);

    // Recherche par auteur (contient le mot recherché, insensible à la casse)
    List<Livre> findByAuteurContainingIgnoreCase(String auteur);

    // Recherche par ISBN
    Optional<Livre> findByIsbn(String isbn);

    // Recherche par genre (insensible à la casse)
    List<Livre> findByGenreContainingIgnoreCase(String genre);

    // Recherche par année de publication
    List<Livre> findByAnneePublication(Integer anneePublication);

    // Recherche par langue (insensible à la casse)
    List<Livre> findByLangueContainingIgnoreCase(String langue);

    // Recherche combinée
    List<Livre> findByTitreContainingIgnoreCaseAndAuteurContainingIgnoreCase(String titre, String auteur);

    List<Livre> findByCategorieContainingIgnoreCase(String categorie);
}
