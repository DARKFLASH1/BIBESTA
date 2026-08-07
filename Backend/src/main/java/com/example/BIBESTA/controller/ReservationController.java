package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Reservation;
import com.example.BIBESTA.security.SecurityUtils; // ← import ajouté
import com.example.BIBESTA.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<Reservation>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Reservation> findById(@PathVariable Integer id) {
        return reservationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Toutes les réservations d'un utilisateur
    // Accessible à l'utilisateur lui-même OU au bibliothécaire
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Reservation>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                reservationService.findByUtilisateurId(utilisateurId));
    }

    // Réservations en attente d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/en-attente")
    public ResponseEntity<List<Reservation>> findEnAttente(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                reservationService.findEnAttenteByUtilisateurId(utilisateurId));
    }

    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> creerReservation(
            @RequestParam Integer utilisateurId,
            @RequestParam Integer livreId) {
        try {
            Reservation reservation = reservationService
                    .creerReservation(utilisateurId, livreId);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> annuler(@PathVariable Integer id) {
        try {
            Reservation reservation = reservationService.annuler(id);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/confirmer/{livreId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> confirmer(@PathVariable Integer livreId) {
        try {
            reservationService.confirmerReservationsSiDisponible(livreId);
            return ResponseEntity.ok("Réservations vérifiées et confirmées");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}