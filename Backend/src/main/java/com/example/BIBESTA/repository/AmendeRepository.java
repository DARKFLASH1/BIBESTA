package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Amende;
import com.example.BIBESTA.model.Amende.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmendeRepository extends JpaRepository<Amende, Integer> {

    // Toutes les amendes d'un utilisateur via son emprunt
    List<Amende> findByEmpruntUtilisateurId(Integer utilisateurId);

    // Amendes par statut
    List<Amende> findByStatut(Statut statut);

    // Amendes EN_ATTENTE d'un utilisateur
    List<Amende> findByEmpruntUtilisateurIdAndStatut(
            Integer utilisateurId,
            Statut statut);

    // Vérifie si un emprunt a déjà une amende
    boolean existsByEmpruntId(Integer empruntId);

    // Trouve l'amende d'un emprunt précis
    Optional<Amende> findByEmpruntId(Integer empruntId);

    // Somme des montants des amendes, filtrée par statut
    // JPQL = requête écrite en "langage objet" au lieu de SQL brut
    // On l'utilise ici car Spring Data ne sait pas générer un SUM() tout seul
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.montant), 0) FROM Amende a WHERE a.statut = :statut")
    java.math.BigDecimal sommeMontantParStatut(
            @org.springframework.data.repository.query.Param("statut") Statut statut);
}