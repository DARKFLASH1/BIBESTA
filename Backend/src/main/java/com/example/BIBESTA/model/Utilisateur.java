package com.example.BIBESTA.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*; // outils pour parler à la base de données
import lombok.Data; // génère automatiquement getters/setters
import lombok.NoArgsConstructor; // génère un constructeur vide
import lombok.AllArgsConstructor; // génère un constructeur avec tous les champs
import java.time.LocalDate; // pour gérer les dates

@Data // Lombok : génère getters, setters, toString automatiquement
@NoArgsConstructor // Lombok : génère Utilisateur() { }
@AllArgsConstructor // Lombok : génère Utilisateur(nom, prenom, ...) { }
@Entity // Dit à Hibernate : "cette classe = une table MySQL"
@Table(name = "utilisateur") // Précise le nom exact de la table dans MySQL
public class Utilisateur {

    @Id // Dit que ce champ est la clé primaire (PRIMARY KEY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // AUTO_INCREMENT : MySQL génère l'id automatiquement (1, 2, 3...)
    private Integer id;

    @Column(name = "nom", nullable = false, length = 100)
    // nullable = false → ce champ est obligatoire (NOT NULL dans MySQL)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance; // LocalDate = date sans heure (2004-01-01)

    @Column(name = "sexe", nullable = false, length = 25)
    private String sexe;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    // unique = true → deux utilisateurs ne peuvent pas avoir le même email
    private String email;

    @Column(name = "identifiant", nullable = false, unique = true, length = 50)
    private String identifiant;

    @Column(name = "contact", length = 20)
    // pas de nullable = false → ce champ est optionnel
    private String contact;
    @JsonIgnore
    @Column(name = "motDePasse", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    // @Enumerated = ce champ est une liste de valeurs fixes
    // EnumType.STRING = on stocke le texte ("ETUDIANT") pas un nombre
    @Column(name = "role", nullable = false)
    private Role role;

    // Enum = une liste de valeurs autorisées, rien d'autre n'est possible
    // Correspond au ENUM dans ta table MySQL
    public enum Role {
        BIBLIOTHECAIRE,
        ETUDIANT,
        ENSEIGNANT,
        PUBLIC
    }
}