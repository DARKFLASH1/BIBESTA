package com.example.BIBESTA.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;





import java.time.LocalDate;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emprunt")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dateDebut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "dateRetourPrevue", nullable = false)
    private LocalDate dateRetourPrevue;

    @Column(name = "dateRetourReelle")
    // Nullable : pas encore retourné au moment de l'emprunt
    private LocalDate dateRetourReelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private Statut statut = Statut.EN_COURS;

    // Relation avec Utilisateur
    // "Plusieurs emprunts appartiennent à un seul utilisateur"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Relation avec Exemplaire
    // "Plusieurs emprunts concernent un seul exemplaire"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exemplaire_id", nullable = false)
    private Exemplaire exemplaire;

    public enum Statut {
        EN_COURS,
        RETOURNE,
        EN_RETARD
    }
}