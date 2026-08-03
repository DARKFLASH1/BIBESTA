package com.example.BIBESTA.controller;

import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.UtilisateurRepository;
import com.example.BIBESTA.security.JwtUtil;
import com.example.BIBESTA.dto.auth.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final UtilisateurRepository utilisateurRepository;
        private final JwtUtil jwtUtil;
        private final PasswordEncoder passwordEncoder;

        // Un hash SHA-256 hexadécimal fait toujours 64 caractères [0-9a-f].
        // Sert uniquement à détecter les anciens comptes pour migration transparente.
        private static boolean estAncienHashSha256(String hash) {
                return hash != null && hash.matches("[0-9a-f]{64}");
        }

        // Vérifie un mot de passe contre un ancien hash SHA-256 (rétrocompatibilité
        // le temps de la migration). À supprimer une fois tous les comptes migrés.
        private static boolean ancienHashSha256Valide(String motDePasseClair, String hashAttendu) {
                return com.example.BIBESTA.security.HashUtil.sha256(motDePasseClair).equals(hashAttendu);
        }

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
                String hashEnBase = utilisateur.getMotDePasse();
                boolean motDePasseValide;

                if (estAncienHashSha256(hashEnBase)) {
                        // Migration transparente : dernière vérification avec l'ancien algo,
                        // puis re-hash immédiat en BCrypt. Après ce login, plus de SHA-256 en base
                        // pour ce compte.
                        motDePasseValide = ancienHashSha256Valide(request.motDePasse(), hashEnBase);
                        if (motDePasseValide) {
                                utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
                                utilisateurRepository.save(utilisateur);
                        }
                } else {
                        motDePasseValide = passwordEncoder.matches(request.motDePasse(), hashEnBase);
                }

                if (!motDePasseValide) {
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