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

@RestController
@RequestMapping("/emprunts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EmpruntController {
    private final Mapper mapper;
    private final EmpruntService empruntService;

    // GET /api/emprunts → tous les emprunts
    @GetMapping
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
    public ResponseEntity<Emprunt> findById(@PathVariable Integer id) {
        return empruntService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/emprunts/utilisateur/2 → emprunts d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Emprunt>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                empruntService.findByUtilisateurId(utilisateurId));
    }

    // GET /api/emprunts/utilisateur/2/en-cours
    // Emprunts en cours d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/en-cours")
    public ResponseEntity<List<Emprunt>> findEnCours(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                empruntService.findEnCoursByUtilisateurId(utilisateurId));
    }

    // GET /api/emprunts/en-retard → tous les emprunts en retard
    // Réservé au bibliothécaire
    @GetMapping("/en-retard")
    public ResponseEntity<List<Emprunt>> findEnRetard() {
        return ResponseEntity.ok(empruntService.findEnRetard());
    }

    // POST /api/emprunts?utilisateurId=2&exemplaireId=1
    // Crée un nouvel emprunt
    @PostMapping
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
    public ResponseEntity<?> mettreAJourRetards() {
        try {
            empruntService.mettreAJourRetards();
            return ResponseEntity.ok("Retards mis à jour avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}