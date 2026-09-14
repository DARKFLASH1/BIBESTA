package com.example.BIBESTA.controller;

import com.example.BIBESTA.dto.auth.LoginRequest;
import com.example.BIBESTA.dto.auth.LoginResponse;
import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.UtilisateurRepository;
import com.example.BIBESTA.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        // POST /api/auth/login
        // Sécurité : on ne distingue PAS "identifiant inconnu" de "mot de passe faux"
        // (même statut 401, même message, temps de réponse comparable) pour éviter
        // l'énumération des comptes.
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {

                // 1. Cherche l'utilisateur par identifiant
                Utilisateur utilisateur = utilisateurRepository
                                .findByIdentifiant(request.identifiant())
                                .orElse(null);

                // 1.1 Hash factice comparé même si l'utilisateur n'existe pas :
                // égalise le temps de réponse et évite le "timing side-channel".
                String hashEnBase = (utilisateur != null) ? utilisateur.getMotDePasse() : null;
                boolean utilisateurTrouve = utilisateur != null;

                // 2. Vérifie le mot de passe
                boolean motDePasseValide;
                if (estAncienHashSha256(hashEnBase)) {
                        // Migration transparente : dernière vérification avec l'ancien algo,
                        // puis re-hash immédiat en BCrypt. Après ce login, plus de SHA-256 en base
                        // pour ce compte.
                        motDePasseValide = ancienHashSha256Valide(request.motDePasse(), hashEnBase);
                        if (utilisateurTrouve && motDePasseValide) {
                                utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
                                utilisateurRepository.save(utilisateur);
                        }
                } else if (utilisateurTrouve) {
                        motDePasseValide = passwordEncoder.matches(request.motDePasse(), hashEnBase);
                } else {
                        motDePasseValide = passwordEncoder.matches(
                                        request.motDePasse(),
                                        "$2a$10$abcdefghijklmnopqrstuuabcdefghijklmnopqrstuuabcdefghijkl");
                }

                if (!utilisateurTrouve || !motDePasseValide) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body("Identifiant ou mot de passe incorrect");
                }

                // 2.1 Vérifie que le compte est ACTIF — après vérification du mot de passe,
                // donc sans réveller l'existence du compte à un inconnu (RG5)
                if (utilisateur.getStatut() != Utilisateur.Statut.ACTIF) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body("Ce compte est " + utilisateur.getStatut().name().toLowerCase() +
                                                        ". Contactez le bibliothécaire.");
                }

                // 3. Génère le token JWT
                String token = jwtUtil.genererToken(
                                utilisateur.getIdentifiant(),
                                utilisateur.getRole().name(),
                                utilisateur.getId());

                // 4. Retourne le token et les infos
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