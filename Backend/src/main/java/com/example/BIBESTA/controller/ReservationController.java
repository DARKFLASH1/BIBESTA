package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Reservation;
import com.example.BIBESTA.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // GET /api/reservations → toutes les réservations
    @GetMapping
    public ResponseEntity<List<Reservation>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    // GET /api/reservations/1 → une réservation par id
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById(@PathVariable Integer id) {
        return reservationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/reservations/utilisateur/2
    // Toutes les réservations d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Reservation>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                reservationService.findByUtilisateurId(utilisateurId));
    }

    // GET /api/reservations/utilisateur/2/en-attente
    // Réservations en attente d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/en-attente")
    public ResponseEntity<List<Reservation>> findEnAttente(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                reservationService.findEnAttenteByUtilisateurId(utilisateurId));
    }

    // POST /api/reservations?utilisateurId=2&livreId=1
    // Crée une réservation
    @PostMapping
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

    // PUT /api/reservations/1/annuler → annule une réservation
    @PutMapping("/{id}/annuler")
    public ResponseEntity<?> annuler(@PathVariable Integer id) {
        try {
            Reservation reservation = reservationService.annuler(id);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/reservations/confirmer/1
    // Vérifie et confirme les réservations en attente pour un livre
    @PutMapping("/confirmer/{livreId}")
    public ResponseEntity<?> confirmer(@PathVariable Integer livreId) {
        try {
            reservationService.confirmerReservationsSiDisponible(livreId);
            return ResponseEntity.ok("Réservations vérifiées et confirmées");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}