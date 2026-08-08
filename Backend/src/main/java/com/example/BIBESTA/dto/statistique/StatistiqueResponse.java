package com.example.BIBESTA.dto.statistique;

import java.math.BigDecimal;
import java.util.List;

// "record" = un DTO en une ligne par champ, sans getters/setters à écrire :
// Java les génère automatiquement (comme Lombok, mais natif au langage).
// C'est la "feuille de calcul" complète renvoyée au frontend.
public record StatistiqueResponse(

        // ── Livres & exemplaires ──────────────────────────────
        long totalLivres,
        long totalExemplaires,
        long exemplairesDisponibles,
        long exemplairesEmpruntes,
        long exemplairesReserves,
        long exemplairesEnReparation,

        // ── Emprunts ───────────────────────────────────────────
        long empruntsEnCours,
        long empruntsEnRetard,
        long empruntsRetournes,

        // ── Réservations ───────────────────────────────────────
        long reservationsEnAttente,

        // ── Amendes ────────────────────────────────────────────
        BigDecimal montantAmendesEnAttente,
        BigDecimal montantAmendesPayees,

        // ── Utilisateurs actifs, groupés par rôle ──────────────
        long utilisateursEtudiants,
        long utilisateursEnseignants,
        long utilisateursPublic,
        long utilisateursBibliothecaires,

        // ── Graphique 1 : emprunts des 6 derniers mois ─────────
        List<EmpruntsParMois> empruntsParMois,

        // ── Graphique 2 : top 5 livres les plus empruntés ──────
        List<LivrePopulaire> topLivres) {

    // "record" imbriqué = un mini-DTO pour chaque point du graphique
    public record EmpruntsParMois(String mois, long total) {
    }

    public record LivrePopulaire(String titre, long nombreEmprunts) {
    }
}