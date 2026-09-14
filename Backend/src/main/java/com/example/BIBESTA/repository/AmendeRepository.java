package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Amende;
import com.example.BIBESTA.model.Amende.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmendeRepository extends JpaRepository<Amende, Integer> {

    // ── Fetch joins : l'amende est sérialisée avec sa chaîne emprunt/exemplaire ─
    // Le frontend lit a.emprunt.exemplaire.livre.titre et a.emprunt.utilisateur.nom :
    // sans @EntityGraph, chaque niveau RELance une requête (N+1 cascadé).
    @EntityGraph(attributePaths = {
            "emprunt",
            "emprunt.utilisateur",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre"})
    List<Amende> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "emprunt",
            "emprunt.utilisateur",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre"})
    Optional<Amende> findById(Integer id);

    // Toutes les amendes d'un utilisateur via son emprunt
    @EntityGraph(attributePaths = {
            "emprunt",
            "emprunt.utilisateur",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre"})
    List<Amende> findByEmpruntUtilisateurId(Integer utilisateurId);

    // Amendes par statut
    @EntityGraph(attributePaths = {
            "emprunt",
            "emprunt.utilisateur",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre"})
    List<Amende> findByStatut(Statut statut);

    // Amendes EN_ATTENTE d'un utilisateur
    @EntityGraph(attributePaths = {
            "emprunt",
            "emprunt.utilisateur",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre"})
    List<Amende> findByEmpruntUtilisateurIdAndStatut(
            Integer utilisateurId,
            Statut statut);

    // Vérifie si un emprunt a déjà une amende
    boolean existsByEmpruntId(Integer empruntId);

    // Trouve l'amende d'un emprunt précis
    @EntityGraph(attributePaths = {
            "emprunt",
            "emprunt.utilisateur",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre"})
    Optional<Amende> findByEmpruntId(Integer empruntId);

    // Somme des montants des amendes, filtrée par statut
    // JPQL = requête écrite en "langage objet" au lieu de SQL brut
    // On l'utilise ici car Spring Data ne sait pas générer un SUM() tout seul
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.montant), 0) FROM Amende a WHERE a.statut = :statut")
    java.math.BigDecimal sommeMontantParStatut(
            @org.springframework.data.repository.query.Param("statut") Statut statut);
}