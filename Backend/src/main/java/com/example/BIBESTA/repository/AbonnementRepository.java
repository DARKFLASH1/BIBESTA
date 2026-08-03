package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Abonnement;
import com.example.BIBESTA.model.Abonnement.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Integer> {

    // Tous les abonnements d'un utilisateur
    // SELECT * FROM abonnement WHERE utilisateur_id = ?
    List<Abonnement> findByUtilisateurId(Integer utilisateurId);

    // Abonnements par statut (EN_ATTENTE, PAYE, EXPIRE)
    List<Abonnement> findByStatutPaiement(StatutPaiement statut);

    // Abonnements actifs d'un utilisateur
    // (payés et dont la date de fin est après aujourd'hui)
    List<Abonnement> findByUtilisateurIdAndStatutPaiement(
            Integer utilisateurId,
            StatutPaiement statut);

    // Abonnements qui expirent avant une date donnée
    // Utile pour envoyer des rappels avant expiration
    List<Abonnement> findByDateFinBefore(LocalDate date);

    // Vérifie si un utilisateur a un abonnement actif
    boolean existsByUtilisateurIdAndStatutPaiementAndDateFinAfter(
            Integer utilisateurId,
            StatutPaiement statut,
            LocalDate date);
}