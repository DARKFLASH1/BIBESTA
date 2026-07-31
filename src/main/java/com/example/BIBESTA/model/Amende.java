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
@Table(name = "amende")
public class Amende {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "montant", nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(name = "raison", length = 255)
    private String raison;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private Statut statut = Statut.EN_ATTENTE;

    // Relation avec Emprunt
    // "Une amende concerne exactement un emprunt"
    @OneToOne(fetch = FetchType.LAZY)
    // @OneToOne = un emprunt a au maximum une amende
    @JoinColumn(name = "emprunt_id", nullable = false)
    private Emprunt emprunt;

    public enum Statut {
        EN_ATTENTE, // amende créée mais pas encore payée
        PAYEE, // amende réglée
        ANNULEE // amende annulée par le bibliothécaire
    }
}