package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Amende;
import com.example.BIBESTA.security.SecurityUtils; // ← import ajouté
import com.example.BIBESTA.service.AmendeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/amendes")
@RequiredArgsConstructor
public class AmendeController {

    private final AmendeService amendeService;

    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<Amende>> findAll() {
        return ResponseEntity.ok(amendeService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Amende> findById(@PathVariable Integer id) {
        return amendeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Amendes d'un utilisateur
    // Accessible à l'utilisateur lui-même OU au bibliothécaire
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Amende>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                amendeService.findByUtilisateurId(utilisateurId));
    }

    // Amendes non payées d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/en-attente")
    public ResponseEntity<List<Amende>> findEnAttente(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                amendeService.findEnAttenteByUtilisateurId(utilisateurId));
    }

    @PostMapping("/emprunt/{empruntId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Amende> creerAmende(@PathVariable Integer empruntId) {
        Amende amende = amendeService.creerAmende(empruntId);
        return ResponseEntity.status(HttpStatus.CREATED).body(amende);
    }

    @PatchMapping("/{id}/payee")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Amende> marquerPayee(@PathVariable Integer id) {
        return ResponseEntity.ok(amendeService.marquerPayee(id));
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Amende> annuler(@PathVariable Integer id) {
        return ResponseEntity.ok(amendeService.annuler(id));
    }
}