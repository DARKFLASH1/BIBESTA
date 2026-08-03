package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Historique;
import com.example.BIBESTA.service.HistoriqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/historique")
@RequiredArgsConstructor
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    // GET /api/historique → tout l'historique
    // Réservé au bibliothécaire
    @GetMapping
    public ResponseEntity<List<Historique>> findAll() {
        return ResponseEntity.ok(historiqueService.findAll());
    }

    // GET /api/historique/1 → un historique par id
    @GetMapping("/{id}")
    public ResponseEntity<Historique> findById(@PathVariable Integer id) {
        return historiqueService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/historique/utilisateur/2
    // Historique complet d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Historique>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                historiqueService.findByUtilisateurId(utilisateurId));
    }

    // GET /api/historique/livre/1
    // Historique d'un livre
    @GetMapping("/livre/{livreId}")
    public ResponseEntity<List<Historique>> findByLivre(
            @PathVariable Integer livreId) {
        return ResponseEntity.ok(
                historiqueService.findByLivreId(livreId));
    }

    // GET /api/historique/type/EMPRUNT
    // Historique par type d'action
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Historique>> findByType(
            @PathVariable String type) {
        return ResponseEntity.ok(
                historiqueService.findByType(type));
    }
}