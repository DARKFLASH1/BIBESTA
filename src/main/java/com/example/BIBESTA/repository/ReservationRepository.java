package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Reservation;
import com.example.BIBESTA.model.Reservation.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // Toutes les réservations d'un utilisateur
    List<Reservation> findByUtilisateurId(Integer utilisateurId);

    // Réservations d'un utilisateur par statut
    List<Reservation> findByUtilisateurIdAndStatut(
            Integer utilisateurId,
            Statut statut);

    // Réservations EN_ATTENTE pour un livre
    // Utile quand un exemplaire est rendu :
    // on cherche qui attend ce livre
    List<Reservation> findByLivreIdAndStatut(
            Integer livreId,
            Statut statut);

    // Vérifie si un utilisateur a déjà réservé ce livre
    boolean existsByUtilisateurIdAndLivreIdAndStatut(
            Integer utilisateurId,
            Integer livreId,
            Statut statut);

    // Toutes les réservations par statut
    List<Reservation> findByStatut(Statut statut);
}