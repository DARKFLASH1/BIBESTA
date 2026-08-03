package com.example.BIBESTA.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                // Désactive CSRF car on utilise JWT (pas de cookies)
                .csrf(csrf -> csrf.disable())

                // Pas de session : JWT gère l'authentification
                // STATELESS = chaque requête est indépendante
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Règles d'accès par URL et rôle
                .authorizeHttpRequests(auth -> auth

                        // Connexion : tout le monde
                        .requestMatchers("/auth/**").permitAll()

                        // Lecture des livres : tout le monde connecté
                        .requestMatchers(HttpMethod.GET, "/livres/**")
                        .permitAll()

                        // Modification des livres : bibliothécaire uniquement
                        .requestMatchers(HttpMethod.POST, "/livres/**")
                        .hasRole("BIBLIOTHECAIRE")
                        .requestMatchers(HttpMethod.PUT, "/livres/**")
                        .hasRole("BIBLIOTHECAIRE")
                        .requestMatchers(HttpMethod.DELETE, "/livres/**")
                        .hasRole("BIBLIOTHECAIRE")

                        // Gestion des utilisateurs : bibliothécaire uniquement
                        .requestMatchers("/utilisateurs/**")
                        .hasRole("BIBLIOTHECAIRE")

                        // Exemplaires : bibliothécaire uniquement
                        .requestMatchers(HttpMethod.POST, "/exemplaires/**")
                        .hasRole("BIBLIOTHECAIRE")
                        .requestMatchers(HttpMethod.DELETE, "/exemplaires/**")
                        .hasRole("BIBLIOTHECAIRE")

                        // Emprunts : authentifié
                        .requestMatchers("/emprunts/**")
                        .authenticated()

                        // Réservations : authentifié
                        .requestMatchers("/reservations/**")
                        .authenticated()

                        // Amendes : authentifié
                        .requestMatchers("/amendes/**")
                        .authenticated()

                        // Paiements : authentifié
                        .requestMatchers("/paiements/**")
                        .authenticated()

                        // Notifications : authentifié
                        .requestMatchers("/notifications/**")
                        .authenticated()

                        // Historique : bibliothécaire uniquement
                        .requestMatchers("/historique/**")
                        .hasRole("BIBLIOTHECAIRE")

                        // Tout le reste : authentifié
                        .anyRequest().authenticated())

                // Ajoute notre filtre JWT AVANT le filtre Spring par défaut
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt : algorithme de hashage des mots de passe
    // Ex: "monmdp" → "$2a$10$xyz...."
    // Impossible de retrouver le mot de passe original
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}