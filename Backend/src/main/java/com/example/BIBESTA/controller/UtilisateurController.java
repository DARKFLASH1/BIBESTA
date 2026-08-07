package com.example.BIBESTA.controller;

import com.example.BIBESTA.dto.utilisateur.UtilisateurRequest;
import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // GET /utilisateurs/page?page=0&size=10
    // Liste paginée des utilisateurs — bibliothécaire uniquement
    @GetMapping("/page")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Page<Utilisateur>> findAllPagines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        return ResponseEntity.ok(utilisateurService.findAllPagines(pageable));
    }

    // Endpoints existants conservés
    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<Utilisateur>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Utilisateur> findById(@PathVariable Integer id) {
        return utilisateurService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> save(@RequestBody UtilisateurRequest request) {
        try {
            Utilisateur saved = utilisateurService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UtilisateurRequest request) {
        try {
            return ResponseEntity.ok(utilisateurService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            utilisateurService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}