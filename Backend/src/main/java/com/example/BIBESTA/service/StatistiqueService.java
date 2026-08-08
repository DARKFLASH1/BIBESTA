package com.example.BIBESTA.service;

import com.example.BIBESTA.dto.statistique.StatistiqueResponse;
import com.example.BIBESTA.dto.statistique.StatistiqueResponse.EmpruntsParMois;
import com.example.BIBESTA.dto.statistique.StatistiqueResponse.LivrePopulaire;
import com.example.BIBESTA.model.Amende;
import com.example.BIBESTA.model.Emprunt;
import com.example.BIBESTA.model.Exemplaire.StatutDisponibilite;
import com.example.BIBESTA.model.Reservation;
import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok génère le constructeur avec tous les "final" ci-dessous
public class StatistiqueService {

    // On injecte tous les repositories dont on a besoin, comme dans tes
    // autres services (EmpruntService, ReservationService...)
    private final LivreRepository livreRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final EmpruntRepository empruntRepository;
    private final ReservationRepository reservationRepository;
    private final AmendeRepository amendeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public StatistiqueResponse getDashboard() {

        return new StatistiqueResponse(
                // Livres & exemplaires
                livreRepository.findByActifTrue().size(),
                exemplaireRepository.count(),
                exemplaireRepository.countByStatutDisponibilite(StatutDisponibilite.DISPONIBLE),
                exemplaireRepository.countByStatutDisponibilite(StatutDisponibilite.EMPRUNTE),
                exemplaireRepository.countByStatutDisponibilite(StatutDisponibilite.RESERVE),
                exemplaireRepository.countByStatutDisponibilite(StatutDisponibilite.EN_REPARATION),

                // Emprunts
                empruntRepository.countByStatut(Emprunt.Statut.EN_COURS),
                empruntRepository.countByStatut(Emprunt.Statut.EN_RETARD),
                empruntRepository.countByStatut(Emprunt.Statut.RETOURNE),

                // Réservations
                reservationRepository.countByStatut(Reservation.Statut.EN_ATTENTE),

                // Amendes
                amendeRepository.sommeMontantParStatut(Amende.Statut.EN_ATTENTE),
                amendeRepository.sommeMontantParStatut(Amende.Statut.PAYEE),

                // Utilisateurs actifs par rôle
                utilisateurRepository.countByRoleAndStatut(Utilisateur.Role.ETUDIANT, Utilisateur.Statut.ACTIF),
                utilisateurRepository.countByRoleAndStatut(Utilisateur.Role.ENSEIGNANT, Utilisateur.Statut.ACTIF),
                utilisateurRepository.countByRoleAndStatut(Utilisateur.Role.PUBLIC, Utilisateur.Statut.ACTIF),
                utilisateurRepository.countByRoleAndStatut(Utilisateur.Role.BIBLIOTHECAIRE, Utilisateur.Statut.ACTIF),

                // Graphiques
                calculerEmpruntsParMois(),
                calculerTopLivres());
    }

    // ── Graphique 1 : nombre d'emprunts démarrés, mois par mois ──────────
    private List<EmpruntsParMois> calculerEmpruntsParMois() {
        LocalDate ilYA6Mois = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        // On récupère TOUS les emprunts puis on les regroupe en mémoire.
        // Sur un petit projet académique, c'est largement suffisant et
        // beaucoup plus lisible qu'une requête SQL de groupement par date.
        Map<String, Long> comptageParMois = empruntRepository.findAll().stream()
                .filter(e -> !e.getDateDebut().isBefore(ilYA6Mois))
                .collect(Collectors.groupingBy(
                        e -> cleMois(e.getDateDebut()),
                        Collectors.counting()));

        // On force l'affichage des 6 derniers mois même s'il y a 0 emprunt
        // certains mois (sinon un mois sans emprunt disparaîtrait du graphique)
        return java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> ilYA6Mois.plusMonths(i))
                .map(date -> new EmpruntsParMois(
                        libelleMois(date),
                        comptageParMois.getOrDefault(cleMois(date), 0L)))
                .toList();
    }

    // "2026-08" → utilisé comme clé de regroupement (unique et triable)
    private String cleMois(LocalDate date) {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
    }

    // "Août" → utilisé pour l'affichage dans le graphique
    private String libelleMois(LocalDate date) {
        return date.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
    }

    // ── Graphique 2 : les 5 livres les plus empruntés (tous statuts) ─────
    private List<LivrePopulaire> calculerTopLivres() {
        return empruntRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getExemplaire().getLivre().getTitre(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new LivrePopulaire(entry.getKey(), entry.getValue()))
                .toList();
    }
}