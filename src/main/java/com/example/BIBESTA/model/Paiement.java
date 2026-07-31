package com.example.BIBESTA.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paiement")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "montant", nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(name = "datePaiement", nullable = false)
    private LocalDate datePaiement;

    @Column(name = "methode_paiement", nullable = false, length = 50)
    // Ex: "ESPECES", "MOBILE_MONEY", "CARTE_BANCAIRE"
    private String methodePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private Statut statut = Statut.EFFECTUE;

    // Relation avec Abonnement (optionnelle)
    // nullable = true : peut être null si c'est un paiement d'amende
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonnement_id", nullable = true)
    private Abonnement abonnement;

    // Relation avec Amende (optionnelle)
    // nullable = true : peut être null si c'est un paiement d'abonnement
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amende_id", nullable = true)
    private Amende amende;

    public enum Statut {
        EFFECTUE, // paiement réussi
        ANNULE, // paiement annulé
        EN_ATTENTE // paiement en attente de confirmation
    }
}