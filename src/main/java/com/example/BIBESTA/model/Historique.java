package com.example.BIBESTA.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historique")
public class Historique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dateMouvement", nullable = false)
    // LocalDateTime = date ET heure (ex: 2024-01-15 14:30:00)
    private LocalDateTime dateMouvement;

    @Column(name = "type", nullable = false, length = 50)
    // Types possibles : EMPRUNT | RETOUR | RESERVATION
    // | ANNULATION | PAIEMENT | CONNEXION
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Relation obligatoire avec Utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Relations optionnelles
    // Un historique peut tracer un emprunt, un livre, ou une réservation
    // mais pas forcément tous en même temps

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emprunt_id", nullable = true)
    private Emprunt emprunt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livre_id", nullable = true)
    private Livre livre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true)
    private Reservation reservation;
}