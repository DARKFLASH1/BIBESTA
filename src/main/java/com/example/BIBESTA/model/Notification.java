package com.example.BIBESTA.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type", nullable = false, length = 50)
    // Types possibles définis dans ton SQL :
    // RETARD | RESERVATION_DISPONIBLE | AMENDE | RAPPEL_RETOUR | ABONNEMENT_EXPIRE
    private String type;

    @Column(name = "contenu", columnDefinition = "TEXT")
    // columnDefinition = "TEXT" → texte long (pas limité à 255 caractères)
    private String contenu;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private Statut statut = Statut.NON_LU; // Par défaut : NON_LU

    // Relation avec Utilisateur
    // "Plusieurs notifications appartiennent à un seul utilisateur"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    public enum Statut {
        LU,
        NON_LU
    }
}