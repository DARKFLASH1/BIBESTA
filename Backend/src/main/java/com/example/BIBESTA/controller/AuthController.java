package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.UtilisateurRepository;
import com.example.BIBESTA.security.HashUtil;
import com.example.BIBESTA.security.JwtUtil;
import com.example.BIBESTA.dto.auth.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

        private final UtilisateurRepository utilisateurRepository;
        private final JwtUtil jwtUtil;

        // Objet qui représente les données de connexion
        public record LoginRequest(String identifiant, String motDePasse) {
        }

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
                String motDePasseHashe = HashUtil.sha256(request.motDePasse());

                System.out.println("Hash reçu    : " + motDePasseHashe);
                System.out.println("Hash en base : " + utilisateur.getMotDePasse());

                if (!utilisateur.getMotDePasse().equals(motDePasseHashe)) {
                        return ResponseEntity.status(401)
                                        .body("Identifiant ou mot de passe incorrect");
                }

                // 4. Génère le token JWT
                String token = jwtUtil.genererToken(
                                utilisateur.getIdentifiant(),
                                utilisateur.getRole().name());

                // 5. Retourne le token et les infos
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