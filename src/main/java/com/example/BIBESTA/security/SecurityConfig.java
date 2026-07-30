package com.example.BIBESTA.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Dit à Spring : "ce fichier contient de la configuration"
@EnableWebSecurity // Active la gestion de la sécurité web
public class SecurityConfig {

    @Bean
    // @Bean = Spring gère cet objet, il sera disponible partout dans l'app
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Désactive la protection CSRF (on la réactivera avec JWT)

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                // Pour l'instant : tout le monde peut accéder à tout
                // On restreindra par rôle plus tard
                );

        return http.build();
    }
}