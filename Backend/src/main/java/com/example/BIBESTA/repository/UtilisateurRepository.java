package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // Dit à Spring : "ce composant gère l'accès à la base de données"
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    // On ajoute nos propres méthodes de recherche :

    // Spring comprend "findByEmail" et génère automatiquement :
    // SELECT * FROM utilisateur WHERE email = ?
    Optional<Utilisateur> findByEmail(String email);

    // SELECT * FROM utilisateur WHERE identifiant = ?
    Optional<Utilisateur> findByIdentifiant(String identifiant);

    // Vérifie si un email existe déjà (pour éviter les doublons)
    // SELECT COUNT(*) > 0 FROM utilisateur WHERE email = ?
    boolean existsByEmail(String email);

    // Vérifie si un identifiant existe déjà
    boolean existsByIdentifiant(String identifiant);

    // Compte les utilisateurs actifs par rôle
    long countByRoleAndStatut(Utilisateur.Role role, Utilisateur.Statut statut);
}