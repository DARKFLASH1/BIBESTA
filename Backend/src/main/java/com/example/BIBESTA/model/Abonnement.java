package com.example.BIBESTA.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "abonnement")
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // Ex: "MENSUEL", "ANNUEL", "TRIMESTRIEL"

    @Column(name = "dateDebut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "dateFin", nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutPaiement")
    private StatutPaiement statutPaiement = StatutPaiement.EN_ATTENTE;

    @Column(name = "montant", nullable = false, precision = 10, scale = 2)
    // precision = nombre total de chiffres, scale = chiffres après la virgule
    // Ex: 2000.00 → precision=10, scale=2
    private BigDecimal montant;

    // Relation avec Utilisateur
    // "Plusieurs abonnements appartiennent à un seul utilisateur"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Les 3 statuts possibles d'un paiement d'abonnement
    public enum StatutPaiement {
        EN_ATTENTE,
        PAYE,
        EXPIRE
    }
}