package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.UtilisateurRepository;
import com.example.BIBESTA.security.HashUtil;
import com.example.BIBESTA.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import com.example.BIBESTA.dto.auth.LoginResponse;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtUtil jwtUtil;

    // Objet qui représente les données de connexion envoyées par Angular
    public record LoginRequest(String identifiant, String motDePasse) {
    }
    // "record" = classe simple avec juste des données (Java 16+)

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. Cherche l'utilisateur par identifiant
        Utilisateur utilisateur = utilisateurRepository
                .findByIdentifiant(request.identifiant())
                .orElse(null);

        // 2. Vérifie que l'utilisateur existe
        if (utilisateur == null) {
            return ResponseEntity.status(401)
                    .body("Identifiant ou mot de passe incorrect");
        }

        // 3. Vérifie le mot de passe
        // Pour l'instant on compare directement le hash
        // (on améliorera avec BCrypt après)
        // compare les hashs SHA-256
        String motDePasseHashe = HashUtil.sha256(request.motDePasse());
        if (!utilisateur.getMotDePasse().equals(motDePasseHashe)) {
            return ResponseEntity.status(401)
                    .body("Identifiant ou mot de passe incorrect");
        }

        // 4. Génère le token JWT
        String token = jwtUtil.genererToken(
                utilisateur.getIdentifiant(),
                utilisateur.getRole().name());

        // 5. Retourne le token et les infos de l'utilisateur
        LoginResponse reponse = new LoginResponse(
                token,
                utilisateur.getRole().name(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getIdentifiant(),
                utilisateur.getId());
        return ResponseEntity.ok(reponse);
    }
}