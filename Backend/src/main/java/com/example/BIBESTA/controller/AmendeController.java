package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Amende;
import com.example.BIBESTA.service.AmendeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/amendes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AmendeController {

    private final AmendeService amendeService;

    // GET /api/amendes → toutes les amendes
    @GetMapping
    public ResponseEntity<List<Amende>> findAll() {
        return ResponseEntity.ok(amendeService.findAll());
    }

    // GET /api/amendes/1 → une amende par id
    @GetMapping("/{id}")
    public ResponseEntity<Amende> findById(@PathVariable Integer id) {
        return amendeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/amendes/utilisateur/2 → amendes d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Amende>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                amendeService.findByUtilisateurId(utilisateurId));
    }

    // GET /api/amendes/utilisateur/2/en-attente
    // Amendes non payées d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/en-attente")
    public ResponseEntity<List<Amende>> findEnAttente(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                amendeService.findEnAttenteByUtilisateurId(utilisateurId));
    }

    // POST /api/amendes/emprunt/1 → crée une amende pour un emprunt
    @PostMapping("/emprunt/{empruntId}")
    public ResponseEntity<?> creerAmende(@PathVariable Integer empruntId) {
        try {
            Amende amende = amendeService.creerAmende(empruntId);
            return ResponseEntity.status(HttpStatus.CREATED).body(amende);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/amendes/1/payee → marque une amende comme payée
    @PatchMapping("/{id}/payee")
    public ResponseEntity<?> marquerPayee(@PathVariable Integer id) {
        try {
            Amende amende = amendeService.marquerPayee(id);
            return ResponseEntity.ok(amende);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/amendes/1/annuler → annule une amende
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<?> annuler(@PathVariable Integer id) {
        try {
            Amende amende = amendeService.annuler(id);
            return ResponseEntity.ok(amende);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}