package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Livre;
import com.example.BIBESTA.service.LivreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/livres")
@RequiredArgsConstructor
public class LivreController {

    private final LivreService livreService;

    // ─────────────────────────────────────────
    // ENDPOINTS PAGINÉS (nouveaux — à utiliser côté Angular)
    // ─────────────────────────────────────────

    // GET /livres/page?page=0&size=10&sort=titre
    // Retourne une page de livres avec les infos de pagination
    // Exemple de réponse : { content: [...], totalPages: 5, totalElements: 48 }
    @GetMapping("/page")
    public ResponseEntity<Page<Livre>> getAllLivresPagines(
            // @RequestParam(defaultValue = "0") = si le paramètre est absent, on prend 0
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "titre") String sort) {

        // PageRequest.of = crée l'objet Pageable avec page + taille + tri
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return ResponseEntity.ok(livreService.getAllLivresPagines(pageable));
    }

    // GET /livres/search/page?query=java&page=0&size=10
    // Recherche paginée dans titre, auteur et catégorie
    @GetMapping("/search/page")
    public ResponseEntity<Page<Livre>> searchLivresPagines(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("titre").ascending());
        return ResponseEntity.ok(livreService.searchLivresPagines(query, pageable));
    }

    // ─────────────────────────────────────────
    // ENDPOINTS EXISTANTS (conservés pour compatibilité)
    // ─────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Livre>> getAllLivres() {
        return ResponseEntity.ok(livreService.getAllLivres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livre> getLivreById(@PathVariable Integer id) {
        return livreService.getLivreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Livre> createLivre(@RequestBody Livre livre) {
        try {
            return ResponseEntity.ok(livreService.saveLivre(livre));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Livre> updateLivre(
            @PathVariable Integer id, @RequestBody Livre livreDetails) {
        try {
            return ResponseEntity.ok(livreService.updateLivre(id, livreDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> deleteLivre(@PathVariable Integer id) {
        try {
            livreService.deleteLivre(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Livre>> searchLivres(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String auteur,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String langue,
            @RequestParam(required = false) String categorie) {
        return ResponseEntity.ok(
                livreService.searchLivres(titre, auteur, isbn, genre, langue, categorie));
    }
}