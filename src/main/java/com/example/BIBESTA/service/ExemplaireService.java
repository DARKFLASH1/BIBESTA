package com.example.BIBESTA.service;

import com.example.BIBESTA.model.Exemplaire;
import com.example.BIBESTA.model.Exemplaire.Etat;
import com.example.BIBESTA.model.Livre;
import com.example.BIBESTA.repository.ExemplaireRepository;
import com.example.BIBESTA.repository.LivreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExemplaireService {

    private final ExemplaireRepository exemplaireRepository;
    private final LivreRepository livreRepository;
    // On a besoin de LivreRepository pour vérifier que le livre existe

    // Retourne tous les exemplaires
    public List<Exemplaire> findAll() {
        return exemplaireRepository.findAll();
    }

    // Retourne un exemplaire par son id
    public Optional<Exemplaire> findById(Integer id) {
        return exemplaireRepository.findById(id);
    }

    // Retourne tous les exemplaires d'un livre
    public List<Exemplaire> findByLivreId(Integer livreId) {
        return exemplaireRepository.findByLivreId(livreId);
    }

    // Retourne les exemplaires disponibles d'un livre
    public List<Exemplaire> findDisponiblesByLivreId(Integer livreId) {
        return exemplaireRepository.findByLivreIdAndEtat(livreId, Etat.DISPONIBLE);
    }

    // Vérifie si un livre a au moins un exemplaire disponible
    public boolean hasExemplaireDisponible(Integer livreId) {
        return exemplaireRepository.countByLivreIdAndEtat(livreId, Etat.DISPONIBLE) > 0;
    }

    // Crée un nouvel exemplaire pour un livre
    public Exemplaire save(Integer livreId, Exemplaire exemplaire) {

        // Vérifie que le livre existe
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'id : " + livreId));

        // Associe l'exemplaire au livre
        exemplaire.setLivre(livre);

        // Par défaut, un nouvel exemplaire est DISPONIBLE
        if (exemplaire.getEtat() == null) {
            exemplaire.setEtat(Etat.DISPONIBLE);
        }

        return exemplaireRepository.save(exemplaire);
    }

    // Change l'état d'un exemplaire (DISPONIBLE, EMPRUNTE, etc.)
    public Exemplaire updateEtat(Integer id, Etat nouvelEtat) {

        Exemplaire exemplaire = exemplaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaire non trouvé"));

        exemplaire.setEtat(nouvelEtat);
        return exemplaireRepository.save(exemplaire);
    }

    // Supprime un exemplaire
    public void deleteById(Integer id) {

        Exemplaire exemplaire = exemplaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaire non trouvé"));

        // On ne peut pas supprimer un exemplaire emprunté
        if (exemplaire.getEtat() == Etat.EMPRUNTE) {
            throw new RuntimeException("Impossible de supprimer un exemplaire emprunté");
        }

        exemplaireRepository.deleteById(id);
    }
}