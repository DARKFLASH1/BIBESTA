package com.example.BIBESTA.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "exemplaire")
public class Exemplaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numExemplaire", nullable = false, length = 255)
    private String numExemplaire;

    // État physique de l'exemplaire (BON_ETAT, USAGE, ENDOMMAGE, PERDU)
    @Enumerated(EnumType.STRING)
    @Column(name = "etat_physique", nullable = false)
    private EtatPhysique etatPhysique = EtatPhysique.BON_ETAT;

    // Statut de disponibilité (DISPONIBLE, EMPRUNTE, RESERVE, EN_REPARATION)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_disponibilite", nullable = false)
    private StatutDisponibilite statutDisponibilite = StatutDisponibilite.DISPONIBLE;

    // Enum pour l'état physique
    public enum EtatPhysique {
        BON_ETAT,
        USAGE,
        ENDOMMAGE,
        PERDU
    }

    // Enum pour le statut de disponibilité
    public enum StatutDisponibilite {
        DISPONIBLE,
        EMPRUNTE,
        RESERVE,
        EN_REPARATION
    }

    // Compatibilité : ancien champ "etat" → getter/setter
    // Pour ne pas casser le code existant qui utilise getEtat()/setEtat()
    @Deprecated
    public Etat getEtat() {
        return switch (statutDisponibilite) {
            case DISPONIBLE -> Etat.DISPONIBLE;
            case EMPRUNTE -> Etat.EMPRUNTE;
            case RESERVE -> Etat.RESERVE;
            case EN_REPARATION -> Etat.EN_REPARATION;
        };
    }

    @Deprecated
    public void setEtat(Etat etat) {
        this.statutDisponibilite = switch (etat) {
            case DISPONIBLE -> StatutDisponibilite.DISPONIBLE;
            case EMPRUNTE -> StatutDisponibilite.EMPRUNTE;
            case RESERVE -> StatutDisponibilite.RESERVE;
            case EN_REPARATION -> StatutDisponibilite.EN_REPARATION;
            case BON_ETAT -> StatutDisponibilite.DISPONIBLE;
            case MAUVAIS_ETAT -> StatutDisponibilite.EN_REPARATION;
        };
    }

    // Ancien enum conservé pour compatibilité
    public enum Etat {
        DISPONIBLE,
        EMPRUNTE,
        RESERVE,
        EN_REPARATION,
        BON_ETAT,
        MAUVAIS_ETAT
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;
}