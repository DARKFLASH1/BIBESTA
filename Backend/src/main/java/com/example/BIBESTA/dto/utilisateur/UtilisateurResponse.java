package com.example.BIBESTA.dto.utilisateur;

import com.example.BIBESTA.model.Utilisateur.Role;
import java.time.LocalDate;

// Ce qu'Angular reçoit — sans mot de passe
public record UtilisateurResponse(
        Integer id,
        String nom,
        String prenom,
        String email,
        String identifiant,
        String contact,
        LocalDate dateNaissance,
        String sexe,
        Role role) {
}