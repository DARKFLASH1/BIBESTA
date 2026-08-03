package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
// Dit à Spring : "ce composant reçoit et répond aux requêtes HTTP"
// Les réponses sont automatiquement converties en JSON

@RequestMapping("/utilisateurs")
// Toutes les URLs de ce controller commencent par /api/utilisateurs
// (le /api vient de application.properties → context-path)

// Autorise Angular (sur un autre port) à appeler cette API
// "*" = accepte toutes les origines (à restreindre en production)

@RequiredArgsConstructor
// Lombok : génère le constructeur avec les dépendances
public class UtilisateurController {

    // Spring injecte automatiquement le Service
    private final UtilisateurService utilisateurService;

    // =====================
    // GET /api/utilisateurs
    // Retourne la liste de tous les utilisateurs
    // =====================
    @GetMapping
    public ResponseEntity<List<Utilisateur>> findAll() {
        List<Utilisateur> utilisateurs = utilisateurService.findAll();
        // ResponseEntity = la réponse HTTP complète (données + code HTTP)
        // HttpStatus.OK = code 200 → tout s'est bien passé
        return ResponseEntity.ok(utilisateurs);
    }

    // =====================
    // GET /api/utilisateurs/1
    // Retourne un utilisateur par son id
    // =====================
    @GetMapping("/{id}")
    // {id} = paramètre dynamique dans l'URL
    public ResponseEntity<Utilisateur> findById(@PathVariable Integer id) {
        // @PathVariable = récupère la valeur de {id} dans l'URL
        return utilisateurService.findById(id)
                .map(u -> ResponseEntity.ok(u))
                // Si trouvé → 200 OK avec l'utilisateur
                .orElse(ResponseEntity.notFound().build());
        // Si non trouvé → 404 Not Found
    }

    // =====================
    // POST /api/utilisateurs
    // Crée un nouvel utilisateur
    // =====================
    @PostMapping
    public ResponseEntity<?> save(@RequestBody Utilisateur utilisateur) {
        // @RequestBody = récupère les données JSON envoyées par Angular
        // "?" = peut retourner un Utilisateur ou un message d'erreur
        try {
            Utilisateur saved = utilisateurService.save(utilisateur);
            // 201 Created = ressource créée avec succès
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            // 400 Bad Request = la requête est invalide (email déjà pris...)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =====================
    // PUT /api/utilisateurs/1
    // Met à jour un utilisateur existant
    // =====================
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody Utilisateur utilisateur) {
        try {
            Utilisateur updated = utilisateurService.update(id, utilisateur);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =====================
    // DELETE /api/utilisateurs/1
    // Supprime un utilisateur
    // =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            utilisateurService.deleteById(id);
            // 204 No Content = suppression réussie, pas de données à retourner
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}