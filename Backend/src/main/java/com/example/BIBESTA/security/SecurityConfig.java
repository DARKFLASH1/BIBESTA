package com.example.BIBESTA.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtFilter jwtFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/livres/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/emprunts", "/emprunts/{id}",
                                                                "/emprunts/en-retard")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/emprunts/utilisateur/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/reservations", "/reservations/{id}")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/reservations/utilisateur/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/amendes", "/amendes/{id}")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/amendes/utilisateur/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/paiements", "/paiements/{id}")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/paiements/utilisateur/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/abonnements", "/abonnements/{id}")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/abonnements/utilisateur/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/utilisateurs", "/utilisateurs/{id}")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/historique", "/historique/**")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/statistiques/**")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/notifications",
                                                                "/notifications/{id}")
                                                .hasRole("BIBLIOTHECAIRE")
                                                .requestMatchers(HttpMethod.GET, "/notifications/utilisateur/**")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                .addFilterBefore(
                                                jwtFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // Autorise Angular
                config.setAllowedOrigins(List.of("http://localhost:4200"));

                // Autorise toutes les méthodes HTTP
                config.setAllowedMethods(List.of(
                                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

                // Autorise tous les en-têtes
                config.setAllowedHeaders(List.of("*"));

                // Autorise les credentials (token JWT)
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}