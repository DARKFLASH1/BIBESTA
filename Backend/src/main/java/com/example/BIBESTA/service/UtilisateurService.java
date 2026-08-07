package com.example.BIBESTA.service;

import com.example.BIBESTA.dto.utilisateur.UtilisateurRequest;
import com.example.BIBESTA.exception.BusinessException;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder; // pour hasher le mot de passe

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> findById(Integer id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> findByIdentifiant(String identifiant) {
        return utilisateurRepository.findByIdentifiant(identifiant);
    }

    // Crée un utilisateur depuis le DTO
    public Utilisateur save(UtilisateurRequest request) {

        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }

        if (utilisateurRepository.existsByIdentifiant(request.identifiant())) {
            throw new BusinessException("Cet identifiant est déjà pris");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(request.email());
        utilisateur.setIdentifiant(request.identifiant());
        utilisateur.setContact(request.contact());
        utilisateur.setDateNaissance(request.dateNaissance());
        utilisateur.setSexe(request.sexe());
        utilisateur.setRole(request.role());

        // Hash BCrypt du mot de passe avant sauvegarde
        utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));

        return utilisateurRepository.save(utilisateur);
    }

    // Met à jour un utilisateur existant
    public Utilisateur update(Integer id, UtilisateurRequest request) {

        Utilisateur existant = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        existant.setNom(request.nom());
        existant.setPrenom(request.prenom());
        existant.setEmail(request.email());
        existant.setContact(request.contact());
        existant.setRole(request.role());

        // Si un nouveau mot de passe est fourni → on le hashe et met à jour
        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            existant.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        }

        return utilisateurRepository.save(existant);
    }

    public Page<Utilisateur> findAllPagines(Pageable pageable) {
        // JpaRepository fournit findAll(Pageable) automatiquement
        return utilisateurRepository.findAll(pageable);
    }

    public void deleteById(Integer id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Désactivation logique au lieu d'une suppression physique (RG12)
        // Préserve l'historique des emprunts et évite les erreurs de clé étrangère
        utilisateur.setStatut(Utilisateur.Statut.DESACTIVE);
        utilisateurRepository.save(utilisateur);
    }

    // Retourne une page d'utilisateurs
    // Appelée par GET /utilisateurs/page?page=0&size=10
    public Page<Utilisateur> findAllPagines(Pageable pageable) {
        return utilisateurRepository.findAll(pageable);
    }
}