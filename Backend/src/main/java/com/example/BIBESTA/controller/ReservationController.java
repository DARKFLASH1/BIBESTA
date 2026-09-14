package com.example.BIBESTA.controller;

import com.example.BIBESTA.dto.Mapper;
import com.example.BIBESTA.dto.reservation.ReservationResponse;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.Reservation;
import com.example.BIBESTA.security.SecurityUtils; // ← import ajouté
import com.example.BIBESTA.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    // Mapper : transforme les entités JPA (avec leurs relations LAZY)
    // en DTOs "à plat" que Jackson peut sérialiser sans planter.
    private final Mapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        List<ReservationResponse> reservations = reservationService.findAll()
                .stream()
                .map(mapper::toReservationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<ReservationResponse> findById(@PathVariable Integer id) {
        return reservationService.findById(id)
                .map(mapper::toReservationResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Toutes les réservations d'un utilisateur
    // Accessible à l'utilisateur lui-même OU au bibliothécaire
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<ReservationResponse>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        List<ReservationResponse> reservations = reservationService
                .findByUtilisateurId(utilisateurId)
                .stream()
                .map(mapper::toReservationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservations);
    }

    // Réservations en attente d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/en-attente")
    public ResponseEntity<List<ReservationResponse>> findEnAttente(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        List<ReservationResponse> reservations = reservationService
                .findEnAttenteByUtilisateurId(utilisateurId)
                .stream()
                .map(mapper::toReservationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> creerReservation(
            @RequestParam Integer utilisateurId,
            @RequestParam Integer livreId) {
        // Un lecteur ne peut réserver que pour lui-même
        SecurityUtils.verifierAccesPropriete(utilisateurId);
        Reservation reservation = reservationService
                .creerReservation(utilisateurId, livreId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toReservationResponse(reservation));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<ReservationResponse> annuler(@PathVariable Integer id) {
        // Retrouve la réservation et vérifie que l'utilisateur peut y accéder
        Reservation reservation = reservationService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));
        SecurityUtils.verifierAccesPropriete(reservation.getUtilisateur().getId());
        Reservation updated = reservationService.annuler(id);
        return ResponseEntity.ok(mapper.toReservationResponse(updated));
    }

    @PutMapping("/confirmer/{livreId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<String> confirmer(@PathVariable Integer livreId) {
        reservationService.confirmerReservationsSiDisponible(livreId);
        return ResponseEntity.ok("Réservations vérifiées et confirmées");
    }
}