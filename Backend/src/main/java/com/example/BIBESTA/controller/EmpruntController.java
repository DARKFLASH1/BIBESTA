package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Emprunt;
import com.example.BIBESTA.service.EmpruntService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.BIBESTA.dto.Mapper;
import com.example.BIBESTA.dto.emprunt.EmpruntResponse;
import com.example.BIBESTA.dto.emprunt.EmpruntRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.BIBESTA.security.SecurityUtils;

@RestController
@RequestMapping("/emprunts")
@RequiredArgsConstructor
public class EmpruntController {
    private final Mapper mapper;
    private final EmpruntService empruntService;

    // GET /api/emprunts → tous les emprunts
    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<EmpruntResponse>> findAll() {
        List<EmpruntResponse> emprunts = empruntService.findAll()
                .stream()
                // Convertit chaque Emprunt en EmpruntResponse
                .map(mapper::toEmpruntResponse)
                .toList();
        return ResponseEntity.ok(emprunts);
    }

    // GET /api/emprunts/1 → un emprunt par id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Emprunt> findById(@PathVariable Integer id) {
        return empruntService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/emprunts/utilisateur/2 → emprunts d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<EmpruntResponse>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId);
        List<EmpruntResponse> emprunts = empruntService.findByUtilisateurId(utilisateurId)
                .stream()
                .map(mapper::toEmpruntResponse)
                .toList();
        return ResponseEntity.ok(emprunts);
    }

    // GET /api/emprunts/utilisateur/2/en-cours
    // Emprunts en cours d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/en-cours")
    public ResponseEntity<List<EmpruntResponse>> findEnCours(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId);
        List<EmpruntResponse> emprunts = empruntService.findEnCoursByUtilisateurId(utilisateurId)
                .stream()
                .map(mapper::toEmpruntResponse)
                .toList();
        return ResponseEntity.ok(emprunts);
    }

    // GET /api/emprunts/en-retard → tous les emprunts en retard
    // Réservé au bibliothécaire
    @GetMapping("/en-retard")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<EmpruntResponse>> findEnRetard() {
        List<EmpruntResponse> emprunts = empruntService.findEnRetard()
                .stream()
                .map(mapper::toEmpruntResponse)
                .toList();
        return ResponseEntity.ok(emprunts);
    }

    // GET /api/emprunts/recent?size=5 → les derniers emprunts
    // Réservé au bibliothécaire
    @GetMapping("/recent")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<EmpruntResponse>> findRecent(
            @RequestParam(defaultValue = "5") int size) {
        List<Emprunt> emprunts = empruntService.findRecent(size);
        List<EmpruntResponse> responses = emprunts.stream()
                .map(mapper::toEmpruntResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // POST /api/emprunts?utilisateurId=2&exemplaireId=1
    // Crée un nouvel emprunt
    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> creerEmprunt(
            @RequestBody EmpruntRequest request) {
        try {
            Emprunt emprunt = empruntService.creerEmprunt(
                    request.utilisateurId(),
                    request.exemplaireId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapper.toEmpruntResponse(emprunt));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/emprunts/1/retour → enregistre le retour d'un livre
    @PutMapping("/{id}/retour")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> enregistrerRetour(@PathVariable Integer id) {
        try {
            Emprunt emprunt = empruntService.enregistrerRetour(id);
            return ResponseEntity.ok(emprunt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/emprunts/retards/update
    // Met à jour tous les emprunts en retard
    @PutMapping("/retards/update")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> mettreAJourRetards() {
        try {
            empruntService.mettreAJourRetards();
            return ResponseEntity.ok("Retards mis à jour avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}