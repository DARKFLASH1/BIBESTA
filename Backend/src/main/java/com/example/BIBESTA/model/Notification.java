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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    // Types possibles définis dans ton SQL :
    // RETARD | RESERVATION_DISPONIBLE | AMENDE | RAPPEL_RETOUR | ABONNEMENT_EXPIRE
    private Type type;

    public enum Type {
        // Types envoyés automatiquement par les services
        EMPRUNT, // confirmation d'un nouvel emprunt
        RETOUR, // retour enregistré sans retard
        RETARD, // emprunt passé en retard
        RESERVATION, // confirmation de création d'une réservation
        RESERVATION_DISPONIBLE, // exemplaire disponible → venir récupérer
        RESERVATION_EXPIREE, // réservation confirmée non retirée après 48h
        AMENDE, // amende créée automatiquement
        PAIEMENT, // paiement d'amende ou d'abonnement confirmé
        ANNULATION, // réservation annulée
        // Types planifiés (à implémenter dans ScheduledTasks)
        RAPPEL_RETOUR, // rappel J-3 avant date de retour
        ABONNEMENT_EXPIRE, // abonnement arrivé à expiration
        RAPPEL_ABONNEMENT // rappel J-7 avant expiration abonnement
    }

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