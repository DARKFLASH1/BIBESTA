package com.example.BIBESTA.service;

import com.example.BIBESTA.model.Livre;
import com.example.BIBESTA.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LivreService {

    private final LivreRepository livreRepository;

    @Autowired
    public LivreService(LivreRepository livreRepository) {
        this.livreRepository = livreRepository;
    }

    /**
     * Sauvegarde un nouveau livre
     * 
     * @param livre Le livre à sauvegarder
     * @return Le livre sauvegardé
     * @throws IllegalArgumentException si l'ISBN existe déjà
     */
    public Livre saveLivre(Livre livre) {
        // Valider que l'ISBN est unique
        if (livre.getIsbn() != null && livreRepository.findByIsbn(livre.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Un livre avec cet ISBN existe déjà");
        }

        // Valider l'unicité de l'ISBN (10 ou 13 chiffres)
        if (livre.getIsbn() != null && !isValidIsbn(livre.getIsbn())) {
            throw new IllegalArgumentException("ISBN invalide. Format accepté : 10 ou 13 chiffres.");
        }

        return livreRepository.save(livre);
    }

    /**
     * Récupère tous les livres
     * 
     * @return Liste de tous les livres
     */
    public List<Livre> getAllLivres() {
        return livreRepository.findAll();
    }

    /**
     * Récupère un livre par son ID
     * 
     * @param id L'ID du livre
     * @return Le livre trouvé ou Optional vide
     */
    public Optional<Livre> getLivreById(int id) {
        return livreRepository.findById(id);
    }

    /**
     * Supprime un livre par son ID
     * 
     * @param id L'ID du livre à supprimer
     * @throws IllegalArgumentException si le livre n'est pas trouvé
     * @throws RuntimeException         si le livre est emprunté
     */
    public void deleteLivre(int id) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé avec l'ID : " + id));

        // Vérifier si le livre est emprunté
        if (isLivreEmprunte(livre)) {
            throw new RuntimeException("Impossible de supprimer le livre : il est actuellement emprunté");
        }

        livreRepository.delete(livre);
    }

    /**
     * Met à jour un livre existant
     * 
     * @param id    L'ID du livre à mettre à jour
     * @param livre Les nouvelles données
     * @return Le livre mis à jour
     * @throws IllegalArgumentException si le livre n'est pas trouvé ou si l'ISBN
     *                                  existe déjà pour un autre livre
     */
    public Livre updateLivre(int id, Livre livre) {
        Livre livreExistant = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé avec l'ID : " + id));

        // Valider que l'ISBN n'est pas changé ou est unique
        if (livre.getIsbn() != null && !livre.getIsbn().equals(livreExistant.getIsbn())) {
            if (livreRepository.findByIsbn(livre.getIsbn()).isPresent()) {
                throw new IllegalArgumentException("Un autre livre avec cet ISBN existe déjà");
            }
            // Valider le format du nouvel ISBN
            if (!isValidIsbn(livre.getIsbn())) {
                throw new IllegalArgumentException("ISBN invalide. Format accepté : 10 ou 13 chiffres.");
            }
        }

        // Mettre à jour les champs
        livreExistant.setTitre(livre.getTitre() != null ? livre.getTitre() : livreExistant.getTitre());
        livreExistant.setAuteur(livre.getAuteur() != null ? livre.getAuteur() : livreExistant.getAuteur());
        livreExistant.setEdition(livre.getEdition() != null ? livre.getEdition() : livreExistant.getEdition());
        livreExistant.setIsbn(livre.getIsbn() != null ? livre.getIsbn() : livreExistant.getIsbn());
        livreExistant.setLangue(livre.getLangue() != null ? livre.getLangue() : livreExistant.getLangue());
        livreExistant.setAnneePublication(livre.getAnneePublication() != null ? livre.getAnneePublication()
                : livreExistant.getAnneePublication());
        livreExistant.setNombrePages(livre.getNombrePages() != null ? livre.getNombrePages()
                : livreExistant.getNombrePages());
        livreExistant.setGenre(livre.getGenre() != null ? livre.getGenre() : livreExistant.getGenre());

        return livreRepository.save(livreExistant);
    }

    private boolean isValidIsbn(String isbn) {
        // Supprime tous les tirets et espaces
        String cleaned = isbn.replaceAll("[- ]", "");
        // Un ISBN valide contient 10 ou 13 chiffres uniquement
        return cleaned.matches("\\d{10}|\\d{13}");
    }

    /**
     * Recherche des livres selon plusieurs critères
     * 
     * @param titre  Titre du livre (optionnel)
     * @param auteur Auteur du livre (optionnel)
     * @param isbn   ISBN du livre (optionnel)
     * @param genre  Genre du livre (optionnel)
     * @param langue Langue du livre (optionnel)
     * @return Liste des livres correspondant aux critères
     */
    public List<Livre> searchLivres(String titre, String auteur, String isbn,
            String genre, String langue, String categorie) {

        if (titre == null && auteur == null && isbn == null &&
                genre == null && langue == null && categorie == null) {
            return getAllLivres();
        }

        if (titre != null && !titre.isEmpty()) {
            return livreRepository.findByTitreContainingIgnoreCase(titre);
        }
        if (auteur != null && !auteur.isEmpty()) {
            return livreRepository.findByAuteurContainingIgnoreCase(auteur);
        }
        if (isbn != null && !isbn.isEmpty()) {
            return livreRepository.findByIsbn(isbn)
                    .map(List::of).orElse(List.of());
        }
        if (genre != null && !genre.isEmpty()) {
            return livreRepository.findByGenreContainingIgnoreCase(genre);
        }
        if (categorie != null && !categorie.isEmpty()) {
            return livreRepository.findByCategorieContainingIgnoreCase(categorie);
        }
        if (langue != null && !langue.isEmpty()) {
            return livreRepository.findByLangueContainingIgnoreCase(langue);
        }

        return List.of();
    }

    /**
     * Vérifie si un livre est emprunté
     * 
     * @param livre Le livre à vérifier
     * @return true si le livre est emprunté, false sinon
     */
    private boolean isLivreEmprunte(Livre livre) {
        // Cette méthode devrait interroger la table emprunt pour vérifier si le
        // livre a des emprunts en cours
        // Pour l'instant, retourne false
        return false;
    }

}