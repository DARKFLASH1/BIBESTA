package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Abonnement;
import com.example.BIBESTA.model.Abonnement.StatutPaiement;
import com.example.BIBESTA.service.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService abonnementService;

    // GET /api/abonnements → tous les abonnements
    @GetMapping
    public ResponseEntity<List<Abonnement>> findAll() {
        return ResponseEntity.ok(abonnementService.findAll());
    }

    // GET /api/abonnements/1 → un abonnement par id
    @GetMapping("/{id}")
    public ResponseEntity<Abonnement> findById(@PathVariable Integer id) {
        return abonnementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/abonnements/utilisateur/2 → abonnements d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Abonnement>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                abonnementService.findByUtilisateurId(utilisateurId));
    }

    // GET /api/abonnements/utilisateur/2/actif → vérifie si abonnement actif
    @GetMapping("/utilisateur/{utilisateurId}/actif")
    public ResponseEntity<Boolean> hasAbonnementActif(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                abonnementService.hasAbonnementActif(utilisateurId));
    }

    // POST /api/abonnements/utilisateur/2 → crée un abonnement
    @PostMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<?> save(
            @PathVariable Integer utilisateurId,
            @RequestBody Abonnement abonnement) {
        try {
            Abonnement saved = abonnementService.save(utilisateurId, abonnement);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/abonnements/1/statut?nouveauStatut=PAYE → change le statut
    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> updateStatut(
            @PathVariable Integer id,
            @RequestParam StatutPaiement nouveauStatut) {
        try {
            Abonnement updated = abonnementService.updateStatut(id, nouveauStatut);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/abonnements/1 → supprime un abonnement
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            abonnementService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}