package com.example.BIBESTA.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "livre")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Livre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Column(name = "auteur", nullable = false, length = 255)
    private String auteur;

    @Column(name = "edition", length = 255)
    private String edition;

    @Column(name = "isbn", unique = true, length = 255)
    private String isbn;

    @Column(name = "categorie", length = 100)
    private String categorie;

    @Column(name = "langue", length = 50)
    private String langue;

    @Column(name = "annee_publication")
    private Integer anneePublication;

    @Column(name = "genre", length = 100)
    private String genre;

    @Column(name = "nombre_pages")
    private Integer nombrePages;

}
