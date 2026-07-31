package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.model.Paiement.Statut;
import com.example.BIBESTA.model.Abonnement.StatutPaiement;
import com.example.BIBESTA.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final AbonnementRepository abonnementRepository;
    private final AmendeRepository amendeRepository;
    private final NotificationService notificationService;
    private final AmendeService amendeService;
    private final HistoriqueService historiqueService;

    // Tous les paiements
    public List<Paiement> findAll() {
        return paiementRepository.findAll();
    }

    // Un paiement par id
    public Optional<Paiement> findById(Integer id) {
        return paiementRepository.findById(id);
    }

    // Paiements d'un utilisateur
    public List<Paiement> findByUtilisateurId(Integer utilisateurId) {
        return paiementRepository
                .findByAbonnementUtilisateurId(utilisateurId);
    }

    // PAYER UN ABONNEMENT
    public Paiement payerAbonnement(
            Integer abonnementId,
            String methodePaiement) {

        // 1. Vérifie que l'abonnement existe
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException(
                        "Abonnement non trouvé"));

        // 2. Vérifie que l'abonnement n'est pas déjà payé
        if (abonnement.getStatutPaiement() == StatutPaiement.PAYE) {
            throw new RuntimeException(
                    "Cet abonnement est déjà payé");
        }

        // 3. Crée le paiement
        // RÈGLE : abonnement renseigné, amende = null
        Paiement paiement = new Paiement();
        paiement.setAbonnement(abonnement);
        paiement.setAmende(null); // jamais les deux à la fois
        paiement.setMontant(abonnement.getMontant());
        paiement.setDatePaiement(LocalDate.now());
        paiement.setMethodePaiement(methodePaiement);
        paiement.setStatut(Statut.EFFECTUE);

        // 4. Met à jour le statut de l'abonnement → PAYE
        abonnement.setStatutPaiement(StatutPaiement.PAYE);
        abonnementRepository.save(abonnement);

        Paiement saved = paiementRepository.save(paiement);
        // Enregistre dans l'historique
        historiqueService.enregistrerPaiement(
                abonnement.getUtilisateur().getId(),
                "Paiement abonnement : " + abonnement.getMontant() + " FCFA");
        // 5. Notifie l'utilisateur
        notificationService.creer(
                abonnement.getUtilisateur().getId(),
                "PAIEMENT",
                "Votre abonnement a été payé avec succès. " +
                        "Montant : " + abonnement.getMontant() + " FCFA. " +
                        "Valable jusqu'au : " + abonnement.getDateFin());

        return saved;
    }

    // PAYER UNE AMENDE
    public Paiement payerAmende(
            Integer amendeId,
            String methodePaiement) {

        // 1. Vérifie que l'amende existe
        Amende amende = amendeRepository.findById(amendeId)
                .orElseThrow(() -> new RuntimeException(
                        "Amende non trouvée"));

        // 2. Vérifie que l'amende n'est pas déjà payée
        if (paiementRepository.existsByAmendeId(amendeId)) {
            throw new RuntimeException(
                    "Cette amende a déjà été payée");
        }

        // 3. Vérifie que l'amende n'est pas annulée
        if (amende.getStatut() == Amende.Statut.ANNULEE) {
            throw new RuntimeException(
                    "Impossible de payer une amende annulée");
        }

        // 4. Crée le paiement
        // RÈGLE : amende renseignée, abonnement = null
        Paiement paiement = new Paiement();
        paiement.setAmende(amende);
        paiement.setAbonnement(null); // jamais les deux à la fois
        paiement.setMontant(amende.getMontant());
        paiement.setDatePaiement(LocalDate.now());
        paiement.setMethodePaiement(methodePaiement);
        paiement.setStatut(Statut.EFFECTUE);

        // 5. Marque l'amende comme payée
        amendeService.marquerPayee(amendeId);

        return paiementRepository.save(paiement);
    }

    // ANNULER UN PAIEMENT
    public Paiement annuler(Integer paiementId) {

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException(
                        "Paiement non trouvé"));

        if (paiement.getStatut() != Statut.EFFECTUE) {
            throw new RuntimeException(
                    "Impossible d'annuler ce paiement : "
                            + paiement.getStatut());
        }

        paiement.setStatut(Statut.ANNULE);
        return paiementRepository.save(paiement);
    }
}