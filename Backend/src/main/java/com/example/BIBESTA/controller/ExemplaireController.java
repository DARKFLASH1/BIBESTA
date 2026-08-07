package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Exemplaire;
import com.example.BIBESTA.model.Exemplaire.Etat;
import com.example.BIBESTA.service.ExemplaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/exemplaires")
@RequiredArgsConstructor
public class ExemplaireController {

    private final ExemplaireService exemplaireService;

    // GET /api/exemplaires → tous les exemplaires
    @GetMapping
    public ResponseEntity<List<Exemplaire>> findAll() {
        return ResponseEntity.ok(exemplaireService.findAll());
    }

    // GET /api/exemplaires/1 → un exemplaire par id
    @GetMapping("/{id}")
    public ResponseEntity<Exemplaire> findById(@PathVariable Integer id) {
        return exemplaireService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/exemplaires/livre/1 → tous les exemplaires d'un livre
    @GetMapping("/livre/{livreId}")
    public ResponseEntity<List<Exemplaire>> findByLivreId(@PathVariable Integer livreId) {
        return ResponseEntity.ok(exemplaireService.findByLivreId(livreId));
    }

    // GET /api/exemplaires/livre/1/disponibles → exemplaires disponibles d'un livre
    @GetMapping("/livre/{livreId}/disponibles")
    public ResponseEntity<List<Exemplaire>> findDisponibles(@PathVariable Integer livreId) {
        return ResponseEntity.ok(exemplaireService.findDisponiblesByLivreId(livreId));
    }

    // POST /api/exemplaires/livre/1 → crée un exemplaire pour le livre 1
    @PostMapping("/livre/{livreId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> save(
            @PathVariable Integer livreId,
            @RequestBody Exemplaire exemplaire) {
        try {
            Exemplaire saved = exemplaireService.save(livreId, exemplaire);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/exemplaires/1/etat?nouvelEtat=EMPRUNTE → change l'état
    @PatchMapping("/{id}/etat")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    // @PatchMapping = modification partielle (juste l'état, pas tout l'objet)
    public ResponseEntity<?> updateEtat(
            @PathVariable Integer id,
            @RequestParam Etat nouvelEtat) {
        try {
            Exemplaire updated = exemplaireService.updateEtat(id, nouvelEtat);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/exemplaires/1 → supprime un exemplaire
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            exemplaireService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}