package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Notification;
import com.example.BIBESTA.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications/utilisateur/2
    // Toutes les notifications d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Notification>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                notificationService.findByUtilisateurId(utilisateurId));
    }

    // GET /api/notifications/utilisateur/2/non-lues
    // Notifications non lues d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public ResponseEntity<List<Notification>> findNonLues(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                notificationService.findNonLues(utilisateurId));
    }

    // GET /api/notifications/utilisateur/2/count
    // Nombre de notifications non lues (pour le badge Angular)
    @GetMapping("/utilisateur/{utilisateurId}/count")
    public ResponseEntity<Long> countNonLues(
            @PathVariable Integer utilisateurId) {
        return ResponseEntity.ok(
                notificationService.countNonLues(utilisateurId));
    }

    // POST /api/notifications/utilisateur/2
    // Crée une notification manuellement
    @PostMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<?> creer(
            @PathVariable Integer utilisateurId,
            @RequestParam String type,
            @RequestParam String contenu) {
        try {
            Notification saved = notificationService.creer(
                    utilisateurId, type, contenu);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/notifications/1/lue → marque une notification comme lue
    @PatchMapping("/{id}/lue")
    public ResponseEntity<?> marquerCommeLue(@PathVariable Integer id) {
        try {
            Notification updated = notificationService.marquerCommeLue(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/notifications/utilisateur/2/toutes-lues
    // Marque toutes les notifications comme lues
    @PatchMapping("/utilisateur/{utilisateurId}/toutes-lues")
    public ResponseEntity<?> marquerToutesCommeLues(
            @PathVariable Integer utilisateurId) {
        try {
            notificationService.marquerToutesCommeLues(utilisateurId);
            return ResponseEntity.ok("Toutes les notifications marquées comme lues");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/notifications/1 → supprime une notification
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            notificationService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}