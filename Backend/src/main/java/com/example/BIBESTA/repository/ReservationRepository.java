package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Reservation;
import com.example.BIBESTA.model.Reservation.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

        // ── Fetch joins : charger utilisateur + livre en une requête ─────────
        // Mapper.toReservationResponse lit r.getUtilisateur() et r.getLivre() :
        // sans ça, une requête SQL par ligne serait nécessaire (N+1).
        @EntityGraph(attributePaths = {"utilisateur", "livre"})
        List<Reservation> findAll();

        @Override
        @EntityGraph(attributePaths = {"utilisateur", "livre"})
        Optional<Reservation> findById(Integer id);

        @EntityGraph(attributePaths = {"utilisateur", "livre"})
        List<Reservation> findByUtilisateurId(Integer utilisateurId);

        @EntityGraph(attributePaths = {"utilisateur", "livre"})
        List<Reservation> findByUtilisateurIdAndStatut(
                        Integer utilisateurId,
                        Statut statut);

        @EntityGraph(attributePaths = {"utilisateur", "livre"})
        List<Reservation> findByLivreIdAndStatut(
                        Integer livreId,
                        Statut statut);

        @EntityGraph(attributePaths = {"utilisateur", "livre"})
        List<Reservation> findByLivreIdAndStatutOrderByDateReservationAsc(
                        Integer livreId,
                        Statut statut);

        // Vérifie si un utilisateur a déjà réservé ce livre
        boolean existsByUtilisateurIdAndLivreIdAndStatut(
                        Integer utilisateurId,
                        Integer livreId,
                        Statut statut);

        @EntityGraph(attributePaths = {"utilisateur", "livre"})
        List<Reservation> findByStatut(Statut statut);

        // Compte les réservations par statut
        long countByStatut(Statut statut);
}