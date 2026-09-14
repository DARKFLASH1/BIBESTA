package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Historique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Integer> {

    // ── Fetch joins : tous les champs lazy sérialisés d'un Historique ────────
    // L'entité Historique expose utilisateur, emprunt, livre et réservation :
    // on pré-charge le tout en une seule requête (colonnes propres + LEFT JOIN).
    @EntityGraph(attributePaths = {
            "utilisateur",
            "emprunt",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre",
            "livre",
            "reservation"})
    List<Historique> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "utilisateur",
            "emprunt",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre",
            "livre",
            "reservation"})
    Optional<Historique> findById(Integer id);

    // Tout l'historique d'un utilisateur
    // SELECT * FROM historique WHERE utilisateur_id = ?
    // ORDER BY dateMouvement DESC
    @EntityGraph(attributePaths = {
            "utilisateur",
            "emprunt",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre",
            "livre",
            "reservation"})
    List<Historique> findByUtilisateurIdOrderByDateMouvementDesc(
            Integer utilisateurId);

    // Historique par type d'action
    // Ex: tous les EMPRUNT, tous les RETOUR...
    @EntityGraph(attributePaths = {
            "utilisateur",
            "emprunt",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre",
            "livre",
            "reservation"})
    List<Historique> findByTypeOrderByDateMouvementDesc(String type);

    // Historique d'un livre précis
    @EntityGraph(attributePaths = {
            "utilisateur",
            "emprunt",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre",
            "livre",
            "reservation"})
    List<Historique> findByLivreIdOrderByDateMouvementDesc(Integer livreId);

    // Historique entre deux dates
    @EntityGraph(attributePaths = {
            "utilisateur",
            "emprunt",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre",
            "livre",
            "reservation"})
    List<Historique> findByDateMouvementBetweenOrderByDateMouvementDesc(
            LocalDateTime debut,
            LocalDateTime fin);

    // Historique d'un utilisateur par type
    @EntityGraph(attributePaths = {
            "utilisateur",
            "emprunt",
            "emprunt.exemplaire",
            "emprunt.exemplaire.livre",
            "livre",
            "reservation"})
    List<Historique> findByUtilisateurIdAndTypeOrderByDateMouvementDesc(
            Integer utilisateurId,
            String type);
}