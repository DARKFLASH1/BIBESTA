package com.example.BIBESTA.dto.utilisateur;

import com.example.BIBESTA.model.Utilisateur.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Ce qu'Angular envoie pour créer/modifier un utilisateur
public record UtilisateurRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,
        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,
        @NotBlank(message = "L'identifiant est obligatoire")
        @Size(min = 3, max = 50, message = "L'identifiant doit contenir entre 3 et 50 caractères")
        String identifiant,
        // Pas de @NotBlank : à l'édition, le frontend envoie motDePasse=''
        // pour ne PAS changer le mot de passe (vérifié explicitement en création).
        String motDePasse, // reçu en clair → hashé dans le service
        String contact,
        LocalDate dateNaissance,
        String sexe,
        Role role) {
}