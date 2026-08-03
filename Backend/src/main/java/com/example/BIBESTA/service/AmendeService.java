package com.example.BIBESTA.service;

import com.example.BIBESTA.exception.BusinessException;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.Amende;
import com.example.BIBESTA.model.Amende.Statut;
import com.example.BIBESTA.model.Emprunt;
import com.example.BIBESTA.repository.AmendeRepository;
import com.example.BIBESTA.repository.EmpruntRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AmendeService {

        private final AmendeRepository amendeRepository;
        private final EmpruntRepository empruntRepository;
        private final NotificationService notificationService;

        // Prix par jour de retard (en FCFA)
        private static final BigDecimal TARIF_PAR_JOUR = new BigDecimal("100");

        // Toutes les amendes
        public List<Amende> findAll() {
                return amendeRepository.findAll();
        }

        // Une amende par id
        public Optional<Amende> findById(Integer id) {
                return amendeRepository.findById(id);
        }

        // Amendes d'un utilisateur
        public List<Amende> findByUtilisateurId(Integer utilisateurId) {
                return amendeRepository.findByEmpruntUtilisateurId(utilisateurId);
        }

        // Amendes EN_ATTENTE d'un utilisateur
        public List<Amende> findEnAttenteByUtilisateurId(Integer utilisateurId) {
                return amendeRepository.findByEmpruntUtilisateurIdAndStatut(
                                utilisateurId,
                                Statut.EN_ATTENTE);
        }

        // CRÉER UNE AMENDE AUTOMATIQUEMENT
        // Appelée par EmpruntService quand un retour est en retard
        public Amende creerAmende(Integer empruntId) {

                // 1. Vérifie que l'emprunt existe
                Emprunt emprunt = empruntRepository.findById(empruntId)
                                .orElseThrow(() -> new ResourceNotFoundException("Emprunt non trouvé"));

                // 2. Vérifie qu'il n'y a pas déjà une amende pour cet emprunt
                if (amendeRepository.existsByEmpruntId(empruntId)) {
                        throw new ResourceNotFoundException(
                                        "Une amende existe déjà pour cet emprunt");
                }

                // 3. Calcule le nombre de jours de retard
                LocalDate dateRetourPrevue = emprunt.getDateRetourPrevue();
                LocalDate dateRetourReelle = emprunt.getDateRetourReelle() != null
                                ? emprunt.getDateRetourReelle()
                                : LocalDate.now();

                long joursRetard = ChronoUnit.DAYS.between(
                                dateRetourPrevue,
                                dateRetourReelle);

                if (joursRetard <= 0) {
                        throw new ResourceNotFoundException(
                                        "Pas de retard détecté pour cet emprunt");
                }

                // 4. Calcule le montant
                // Ex: 5 jours de retard × 100 FCFA = 500 FCFA
                BigDecimal montant = TARIF_PAR_JOUR
                                .multiply(new BigDecimal(joursRetard));

                // 5. Crée l'amende
                Amende amende = new Amende();
                amende.setEmprunt(emprunt);
                amende.setMontant(montant);
                amende.setDate(LocalDate.now());
                amende.setStatut(Statut.EN_ATTENTE);
                amende.setRaison(
                                "Retard de " + joursRetard + " jour(s). " +
                                                "Tarif : " + TARIF_PAR_JOUR + " FCFA/jour.");

                Amende saved = amendeRepository.save(amende);

                // 6. Notifie l'utilisateur
                notificationService.creer(
                                emprunt.getUtilisateur().getId(),
                                "AMENDE",
                                "Une amende de " + montant + " FCFA a été créée pour " +
                                                joursRetard + " jour(s) de retard.");

                return saved;
        }

        // MARQUER UNE AMENDE COMME PAYÉE
        public Amende marquerPayee(Integer amendeId) {

                Amende amende = amendeRepository.findById(amendeId)
                                .orElseThrow(() -> new ResourceNotFoundException("Amende non trouvée"));

                if (amende.getStatut() != Statut.EN_ATTENTE) {
                        throw new BusinessException(
                                        "Cette amende ne peut pas être marquée payée : "
                                                        + amende.getStatut());
                }

                amende.setStatut(Statut.PAYEE);

                // Notifie l'utilisateur
                notificationService.creer(
                                amende.getEmprunt().getUtilisateur().getId(),
                                "PAIEMENT",
                                "Votre amende de " + amende.getMontant() +
                                                " FCFA a été réglée. Merci !");

                return amendeRepository.save(amende);
        }

        // ANNULER UNE AMENDE (bibliothécaire uniquement)
        public Amende annuler(Integer amendeId) {

                Amende amende = amendeRepository.findById(amendeId)
                                .orElseThrow(() -> new ResourceNotFoundException("Amende non trouvée"));

                if (amende.getStatut() == Statut.PAYEE) {
                        throw new BusinessException(
                                        "Impossible d'annuler une amende déjà payée");
                }

                amende.setStatut(Statut.ANNULEE);
                return amendeRepository.save(amende);
        }
}