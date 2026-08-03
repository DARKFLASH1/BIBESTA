package com.example.BIBESTA.dto.utilisateur;

import com.example.BIBESTA.model.Utilisateur.Role;
import java.time.LocalDate;

// Ce qu'Angular envoie pour créer/modifier un utilisateur
public record UtilisateurRequest(
        String nom,
        String prenom,
        String email,
        String identifiant,
        String motDePasse, // reçu en clair → hashé dans le service
        String contact,
        LocalDate dateNaissance,
        String sexe,
        Role role) {
}