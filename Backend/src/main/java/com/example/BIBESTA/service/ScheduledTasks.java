package com.example.BIBESTA.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.example.BIBESTA.repository.EmpruntRepository;
import com.example.BIBESTA.repository.AbonnementRepository;
import com.example.BIBESTA.service.NotificationService;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final EmpruntService empruntService;
    private final AbonnementService abonnementService;
    private final ReservationService reservationService;
    private final EmpruntRepository empruntRepository;
    private final AbonnementRepository abonnementRepository;
    private final NotificationService notificationService;
    // Exécute chaque jour à 1h du matin

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void tachesQuotidiennes() {

        // 1. Met à jour les emprunts en retard
        empruntService.mettreAJourRetards();

        // 2. Expire les abonnements dépassés
        abonnementService.expireAbonnementsDepasses();

        // 3. Expire les réservations confirmées non retirées après 48h
        reservationService.expirerReservationsConfirmees();

        // 4. Rappels J-3 avant date de retour
        LocalDate dans3jours = LocalDate.now().plusDays(3);
        empruntRepository
                .findByStatutAndDateRetourPrevueBefore(
                        com.example.BIBESTA.model.Emprunt.Statut.EN_COURS,
                        dans3jours.plusDays(1)) // avant J+4 = inclut J+3
                .stream()
                .filter(e -> e.getDateRetourPrevue().isEqual(dans3jours))
                .forEach(e -> notificationService.creer(
                        e.getUtilisateur().getId(),
                        "RAPPEL_RETOUR",
                        "Rappel : le livre '" + e.getExemplaire().getLivre().getTitre() +
                                "' doit être retourné dans 3 jours (le " + e.getDateRetourPrevue() + ")."));

        // 5. Rappels J-7 avant expiration d'abonnement
        LocalDate dans7jours = LocalDate.now().plusDays(7);
        abonnementRepository
                .findByDateFinBefore(dans7jours.plusDays(1))
                .stream()
                .filter(a -> a.getDateFin().isEqual(dans7jours))
                .filter(a -> a.getStatutPaiement() == com.example.BIBESTA.model.Abonnement.StatutPaiement.PAYE)
                .forEach(a -> notificationService.creer(
                        a.getUtilisateur().getId(),
                        "RAPPEL_ABONNEMENT",
                        "Votre abonnement expire dans 7 jours (le " + a.getDateFin() +
                                "). Pensez à le renouveler."));
    }
}