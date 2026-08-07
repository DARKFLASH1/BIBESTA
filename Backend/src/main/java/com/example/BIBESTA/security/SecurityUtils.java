package com.example.BIBESTA.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.example.BIBESTA.exception.BusinessException;

public class SecurityUtils {

    // Retourne l'id de l'utilisateur connecté
    // Utilisation : Integer monId = SecurityUtils.getIdConnecte();
    public static Integer getIdConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Aucun utilisateur connecté");
        }
        // Le principal est l'id qu'on a mis dans JwtFilter
        return (Integer) auth.getPrincipal();
    }

    // Retourne true si l'utilisateur connecté est bibliothécaire
    public static boolean estBibliothecaire() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_BIBLIOTHECAIRE"));
    }

    // Vérifie que l'utilisateur connecté EST bien cet utilisateurId
    // OU qu'il est bibliothécaire (le biblio peut tout voir)
    // Sinon → exception
    public static void verifierAccesPropriete(Integer utilisateurId) {
        if (!estBibliothecaire() && !getIdConnecte().equals(utilisateurId)) {
            throw new BusinessException(
                    "Accès refusé : vous ne pouvez consulter que vos propres données");
        }
    }
}