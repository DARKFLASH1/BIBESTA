package com.example.BIBESTA.controller;

import com.example.BIBESTA.dto.statistique.StatistiqueResponse;
import com.example.BIBESTA.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    // GET /statistiques/dashboard → toutes les stats en un seul appel
    // Réservé au bibliothécaire : un lecteur n'a pas à voir les statistiques
    // globales de la bibliothèque (chiffre d'affaires, tous les utilisateurs...)
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<StatistiqueResponse> getDashboard() {
        return ResponseEntity.ok(statistiqueService.getDashboard());
    }
}