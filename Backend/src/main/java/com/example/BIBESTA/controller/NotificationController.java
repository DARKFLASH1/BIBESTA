package com.example.BIBESTA.controller;

import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.Notification;
import com.example.BIBESTA.security.SecurityUtils; // ← import ajouté
import com.example.BIBESTA.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Toutes les notifications d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Notification>> findByUtilisateur(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                notificationService.findByUtilisateurId(utilisateurId));
    }

    // Notifications non lues d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public ResponseEntity<List<Notification>> findNonLues(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                notificationService.findNonLues(utilisateurId));
    }

    // Nombre de notifications non lues (badge Angular)
    @GetMapping("/utilisateur/{utilisateurId}/count")
    public ResponseEntity<Long> countNonLues(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        return ResponseEntity.ok(
                notificationService.countNonLues(utilisateurId));
    }

    @PostMapping("/utilisateur/{utilisateurId}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Notification> creer(
            @PathVariable Integer utilisateurId,
            @RequestParam String type,
            @RequestParam String contenu) {
        Notification saved = notificationService.creer(
                utilisateurId, type, contenu);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Marquer une notification comme lue
    // L'utilisateur marque ses propres notifications
    @PatchMapping("/{id}/lue")
    public ResponseEntity<Notification> marquerCommeLue(@PathVariable Integer id) {
        // IDOR : on vérifie que la notification appartient bien à l'utilisateur
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));
        SecurityUtils.verifierAccesPropriete(notification.getUtilisateur().getId());
        return ResponseEntity.ok(notificationService.marquerCommeLue(id));
    }

    // Marquer toutes les notifications comme lues
    @PatchMapping("/utilisateur/{utilisateurId}/toutes-lues")
    public ResponseEntity<String> marquerToutesCommeLues(
            @PathVariable Integer utilisateurId) {
        SecurityUtils.verifierAccesPropriete(utilisateurId); // ← ajouté
        notificationService.marquerToutesCommeLues(utilisateurId);
        return ResponseEntity.ok("Toutes les notifications marquées comme lues");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        notificationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}