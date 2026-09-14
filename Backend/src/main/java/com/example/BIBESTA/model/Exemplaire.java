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

    // P2.13 : l'ancien enum composite Etat (DISPONIBLE/EMPRUNTE/RESERVE/
    // EN_REPARATION/BON_ETAT/MAUVAIS_ETAT) et les shims getEtat()/setEtat()
    // ont été supprimés. Le modèle utilise désormais deux enums séparées :
    //   EtatPhysique      → état physique (bon état, usage, endommagé…)
    //   StatutDisponibilite → disponibilité (disponible, emprunté, réservé…)
    // Cela clarifie la sémantique et supprime les coercions silencieuses
    // (setEtat(BON_ETAT) → DISPONIBLE).

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;
}
