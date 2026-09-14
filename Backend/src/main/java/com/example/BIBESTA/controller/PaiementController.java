package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Paiement;
import com.example.BIBESTA.security.SecurityUtils; // ← import ajouté
import com.example.BIBESTA.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<Paiement>> findAll() {
        return ResponseEntity.ok(paiementService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Paiement> findById(@PathVariable Integer id) {
        return paiementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Paiements d'un utilisateur
    // Accessible à l'utilisateur lui-même OU au bibliothécaire
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Paiement>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                paiementService.findByUtilisateurId(utilisateurId));
    }

    @PostMapping("/abonnement/{abonnementId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Paiement> payerAbonnement(
            @PathVariable Integer abonnementId,
            @RequestParam String methodePaiement) {
        Paiement paiement = paiementService
                .payerAbonnement(abonnementId, methodePaiement);
        return ResponseEntity.status(HttpStatus.CREATED).body(paiement);
    }

    @PostMapping("/amende/{amendeId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Paiement> payerAmende(
            @PathVariable Integer amendeId,
            @RequestParam String methodePaiement) {
        Paiement paiement = paiementService
                .payerAmende(amendeId, methodePaiement);
        return ResponseEntity.status(HttpStatus.CREATED).body(paiement);
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Paiement> annuler(@PathVariable Integer id) {
        return ResponseEntity.ok(paiementService.annuler(id));
    }
}