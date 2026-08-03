package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Livre;
import com.example.BIBESTA.service.LivreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livres") // Toutes les URL de ce contrôleur commenceront par /livres
@RequiredArgsConstructor
public class LivreController {

    private final LivreService livreService;

    // =====================
    // LIRE
    // =====================

    // GET /livres → retourne tous les livres
    @GetMapping
    public ResponseEntity<List<Livre>> getAllLivres() {
        List<Livre> livres = livreService.getAllLivres();
        return ResponseEntity.ok(livres);
    }

    // GET /livres/123 → retourne un livre par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Livre> getLivreById(@PathVariable Integer id) {
        return livreService.getLivreById(id)
                .map(ResponseEntity::ok) // Si trouvé → ResponseEntity.ok(livre)
                .orElse(ResponseEntity.notFound().build()); // Si pas trouvé → 404
    }

    // =====================
    // CRÉER
    // =====================

    // POST /livres → crée un nouveau livre
    @PostMapping
    public ResponseEntity<Livre> createLivre(@RequestBody Livre livre) {
        try {
            Livre savedLivre = livreService.saveLivre(livre);
            return ResponseEntity.ok(savedLivre);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // En cas d'erreur → 400 Bad Request
        }
    }

    // =====================
    // METTRE À JOUR
    // =====================

    // PUT /livres/123 → met à jour le livre avec l'ID 123
    @PutMapping("/{id}")
    public ResponseEntity<Livre> updateLivre(@PathVariable Integer id, @RequestBody Livre livreDetails) {
        try {
            Livre updatedLivre = livreService.updateLivre(id, livreDetails);
            return ResponseEntity.ok(updatedLivre);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // =====================
    // SUPPRIMER
    // =====================

    // DELETE /livres/123 → supprime le livre avec l'ID 123
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLivre(@PathVariable Integer id) {
        try {
            livreService.deleteLivre(id);
            return ResponseEntity.ok().build(); // 200 OK sans corps
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // =====================
    // RECHERCHE
    // =====================

    // GET /livres/search?titre=Java&auteur=Smith
    // GET /livres/search?titre=Java
    // GET /livres/search?auteur=Smith
    @GetMapping("/search")
    public ResponseEntity<List<Livre>> searchLivres(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String auteur,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String langue,
            @RequestParam(required = false) String categorie) {

        List<Livre> livres = livreService.searchLivres(titre, auteur, isbn, genre, langue, categorie);
        return ResponseEntity.ok(livres);
    }
}
