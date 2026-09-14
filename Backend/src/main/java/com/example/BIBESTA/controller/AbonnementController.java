package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Abonnement;
import com.example.BIBESTA.model.Abonnement.StatutPaiement;
import com.example.BIBESTA.security.SecurityUtils; // ← import ajouté
import com.example.BIBESTA.service.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService abonnementService;

    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<Abonnement>> findAll() {
        return ResponseEntity.ok(abonnementService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Abonnement> findById(@PathVariable Integer id) {
        return abonnementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Abonnements d'un utilisateur
    // Accessible à l'utilisateur lui-même OU au bibliothécaire
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Abonnement>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                abonnementService.findByUtilisateurId(utilisateurId));
    }

    // Vérifie si un utilisateur a un abonnement actif
    @GetMapping("/utilisateur/{utilisateurId}/actif")
    public ResponseEntity<Boolean> hasAbonnementActif(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                abonnementService.hasAbonnementActif(utilisateurId));
    }

    @PostMapping("/utilisateur/{utilisateurId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Abonnement> save(
            @PathVariable Integer utilisateurId,
            @RequestBody Abonnement abonnement) {
        Abonnement saved = abonnementService.save(utilisateurId, abonnement);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Abonnement> updateStatut(
            @PathVariable Integer id,
            @RequestParam StatutPaiement nouveauStatut) {
        return ResponseEntity.ok(abonnementService.updateStatut(id, nouveauStatut));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        abonnementService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}