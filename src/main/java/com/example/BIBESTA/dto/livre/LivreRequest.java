package com.example.BIBESTA.dto.livre;

// Ce qu'Angular envoie pour créer/modifier un livre
public record LivreRequest(
        String titre,
        String auteur,
        String edition,
        String isbn,
        String categorie,
        String genre,
        String langue,
        Integer anneePublication,
        Integer nombrePages) {
}