package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Paiement;
import com.example.BIBESTA.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/paiements")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    // GET /api/paiements → tous les paiements
    @GetMapping
    public ResponseEntity<List<Paiement>> findAll() {
        return ResponseEntity.ok(paiementService.findAll());
    }

    // GET /api/paiements/1 → un paiement par id
    @GetMapping("/{id}")
    public ResponseEntity<Paiement> findById(@PathVariable Integer id) {
        return paiementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/paiements/utilisateur/2
    // Paiements d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Paiement>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                paiementService.findByUtilisateurId(utilisateurId));
    }

    // POST /api/paiements/abonnement/1?methodePaiement=ESPECES
    // Payer un abonnement
    @PostMapping("/abonnement/{abonnementId}")
    public ResponseEntity<?> payerAbonnement(
            @PathVariable Integer abonnementId,
            @RequestParam String methodePaiement) {
        try {
            Paiement paiement = paiementService
                    .payerAbonnement(abonnementId, methodePaiement);
            return ResponseEntity.status(HttpStatus.CREATED).body(paiement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/paiements/amende/1?methodePaiement=MOBILE_MONEY
    // Payer une amende
    @PostMapping("/amende/{amendeId}")
    public ResponseEntity<?> payerAmende(
            @PathVariable Integer amendeId,
            @RequestParam String methodePaiement) {
        try {
            Paiement paiement = paiementService
                    .payerAmende(amendeId, methodePaiement);
            return ResponseEntity.status(HttpStatus.CREATED).body(paiement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/paiements/1/annuler → annule un paiement
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<?> annuler(@PathVariable Integer id) {
        try {
            Paiement paiement = paiementService.annuler(id);
            return ResponseEntity.ok(paiement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}