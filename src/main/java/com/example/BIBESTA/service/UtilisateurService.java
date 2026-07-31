package com.example.BIBESTA.service;

import com.example.BIBESTA.exception.BusinessException;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service // Dit à Spring : "ce composant contient la logique métier"
@RequiredArgsConstructor // Lombok : génère le constructeur avec les dépendances
public class UtilisateurService {

    // Spring injecte automatiquement le Repository ici
    // "injection de dépendance" = Spring crée l'objet et le fournit
    private final UtilisateurRepository utilisateurRepository;

    // =====================
    // LIRE
    // =====================

    // Retourne tous les utilisateurs
    // SELECT * FROM utilisateur
    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    // Retourne un utilisateur par son id
    // SELECT * FROM utilisateur WHERE id = ?
    public Optional<Utilisateur> findById(Integer id) {
        return utilisateurRepository.findById(id);
    }

    // Retourne un utilisateur par son identifiant (ex: "yellowflash")
    public Optional<Utilisateur> findByIdentifiant(String identifiant) {
        return utilisateurRepository.findByIdentifiant(identifiant);
    }

    // =====================
    // CRÉER / MODIFIER
    // =====================

    // Crée un nouvel utilisateur après vérifications
    public Utilisateur save(Utilisateur utilisateur) {

        // Vérifie que l'email n'est pas déjà utilisé
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }

        // Vérifie que l'identifiant n'est pas déjà pris
        if (utilisateurRepository.existsByIdentifiant(utilisateur.getIdentifiant())) {
            throw new BusinessException("Cet identifiant est déjà pris");
        }

        // Tout est ok → on sauvegarde dans MySQL
        return utilisateurRepository.save(utilisateur);
    }

    // Met à jour un utilisateur existant
    public Utilisateur update(Integer id, Utilisateur utilisateurModifie) {

        // Vérifie que l'utilisateur existe
        Utilisateur existant = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Met à jour les champs
        existant.setNom(utilisateurModifie.getNom());
        existant.setPrenom(utilisateurModifie.getPrenom());
        existant.setEmail(utilisateurModifie.getEmail());
        existant.setContact(utilisateurModifie.getContact());
        existant.setRole(utilisateurModifie.getRole());

        // Sauvegarde les modifications
        return utilisateurRepository.save(existant);
    }

    // =====================
    // SUPPRIMER
    // =====================

    public void deleteById(Integer id) {

        // Vérifie que l'utilisateur existe avant de supprimer
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé");
        }

        utilisateurRepository.deleteById(id);
    }
}