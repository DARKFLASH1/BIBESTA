package com.example.BIBESTA.service;

import com.example.BIBESTA.exception.BusinessException;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.Emprunt.Statut;
import com.example.BIBESTA.model.Exemplaire;
import com.example.BIBESTA.model.Livre;
import com.example.BIBESTA.repository.EmpruntRepository;
import com.example.BIBESTA.repository.ExemplaireRepository;
import com.example.BIBESTA.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class LivreService {

    private final LivreRepository livreRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final EmpruntRepository empruntRepository;

    @Autowired
    public LivreService(LivreRepository livreRepository,
            ExemplaireRepository exemplaireRepository,
            EmpruntRepository empruntRepository) {
        this.livreRepository = livreRepository;
        this.exemplaireRepository = exemplaireRepository;
        this.empruntRepository = empruntRepository;
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
            throw new BusinessException("Un livre avec cet ISBN existe déjà");
        }

        // Valider l'unicité de l'ISBN (10 ou 13 chiffres)
        if (livre.getIsbn() != null && !isValidIsbn(livre.getIsbn())) {
            throw new BusinessException("ISBN invalide. Format accepté : 10 ou 13 chiffres.");
        }

        return livreRepository.save(livre);
    }

    // Retourne tous les livres ACTIFS (exclut les désactivés)
    public List<Livre> getAllLivres() {
        return livreRepository.findByActifTrue();
    }

    // Liste paginée — actifs seulement
    public Page<Livre> getAllLivresPagines(Pageable pageable) {
        return livreRepository.findByActifTrue(pageable);
    }

    // Recherche paginée — actifs seulement
    public Page<Livre> searchLivresPagines(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return livreRepository.findByActifTrue(pageable);
        }
        return livreRepository
                .findByActifTrueAndTitreContainingIgnoreCaseOrActifTrueAndAuteurContainingIgnoreCaseOrActifTrueAndCategorieContainingIgnoreCase(
                        query, query, query, pageable);
    }

    // Recherche multicritère — actifs seulement
    public List<Livre> searchLivres(String titre, String auteur, String isbn,
            String genre, String langue, String categorie) {

        if (titre == null && auteur == null && isbn == null &&
                genre == null && langue == null && categorie == null) {
            return getAllLivres(); // déjà filtré
        }
        if (titre != null && !titre.isEmpty()) {
            return livreRepository.findByTitreContainingIgnoreCaseAndActifTrue(titre);
        }
        if (auteur != null && !auteur.isEmpty()) {
            return livreRepository.findByAuteurContainingIgnoreCaseAndActifTrue(auteur);
        }
        if (isbn != null && !isbn.isEmpty()) {
            return livreRepository.findByIsbnAndActifTrue(isbn)
                    .map(List::of).orElse(List.of());
        }
        if (genre != null && !genre.isEmpty()) {
            return livreRepository.findByGenreContainingIgnoreCaseAndActifTrue(genre);
        }
        if (categorie != null && !categorie.isEmpty()) {
            return livreRepository.findByCategorieContainingIgnoreCaseAndActifTrue(categorie);
        }
        if (langue != null && !langue.isEmpty()) {
            return livreRepository.findByLangueContainingIgnoreCaseAndActifTrue(langue);
        }
        return List.of();
    }

    /**
     * Récupère tous les livres
     * 
     * @return Liste de tous les livres
     */

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
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé avec l'ID : " + id));

        // Vérifier si le livre est emprunté
        if (isLivreEmprunte(livre)) {
            throw new BusinessException("Impossible de supprimer le livre : il est actuellement emprunté");
        }

        // Désactivation logique au lieu d'une suppression physique (RG12)
        // Préserve l'historique des emprunts et évite les erreurs de clé étrangère
        livre.setActif(false);
        livreRepository.save(livre);
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
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé avec l'ID : " + id));

        // Valider que l'ISBN n'est pas changé ou est unique
        if (livre.getIsbn() != null && !livre.getIsbn().equals(livreExistant.getIsbn())) {
            if (livreRepository.findByIsbn(livre.getIsbn()).isPresent()) {
                throw new BusinessException("Un autre livre avec cet ISBN existe déjà");
            }
            // Valider le format du nouvel ISBN
            if (!isValidIsbn(livre.getIsbn())) {
                throw new BusinessException("ISBN invalide. Format accepté : 10 ou 13 chiffres.");
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

    /**
     * Vérifie si un livre est emprunté
     * 
     * @param livre Le livre à vérifier
     * @return true si le livre est emprunté, false sinon
     */
    private boolean isLivreEmprunte(Livre livre) {
        // Un livre est "emprunté" si au moins un de ses exemplaires a un emprunt
        // au statut EN_COURS.
        List<Exemplaire> exemplaires = exemplaireRepository.findByLivreId(livre.getId());
        for (Exemplaire exemplaire : exemplaires) {
            if (empruntRepository.existsByExemplaireIdAndStatut(
                    exemplaire.getId().intValue(), Statut.EN_COURS)) {
                return true;
            }
        }
        return false;
    }

}