package com.example.BIBESTA.service;

import com.example.BIBESTA.exception.BusinessException;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.Abonnement;
import com.example.BIBESTA.model.Abonnement.StatutPaiement;
import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.AbonnementRepository;
import com.example.BIBESTA.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final UtilisateurRepository utilisateurRepository;

    // Retourne tous les abonnements
    public List<Abonnement> findAll() {
        return abonnementRepository.findAll();
    }

    // Retourne un abonnement par son id
    public Optional<Abonnement> findById(Integer id) {
        return abonnementRepository.findById(id);
    }

    // Retourne tous les abonnements d'un utilisateur
    public List<Abonnement> findByUtilisateurId(Integer utilisateurId) {
        return abonnementRepository.findByUtilisateurId(utilisateurId);
    }

    // Vérifie si un utilisateur a un abonnement actif
    public boolean hasAbonnementActif(Integer utilisateurId) {
        return abonnementRepository
                .existsByUtilisateurIdAndStatutPaiementAndDateFinAfter(
                        utilisateurId,
                        StatutPaiement.PAYE,
                        LocalDate.now() // aujourd'hui
                );
    }

    // Crée un nouvel abonnement pour un utilisateur
    @Transactional
    public Abonnement save(Integer utilisateurId, Abonnement abonnement) {

        // Vérifie que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé avec l'id : " + utilisateurId));

        // Vérifie que la date de fin est après la date de début
        if (abonnement.getDateFin().isBefore(abonnement.getDateDebut())) {
            throw new BusinessException(
                    "La date de fin doit être après la date de début");
        }

        // Associe l'abonnement à l'utilisateur
        abonnement.setUtilisateur(utilisateur);

        return abonnementRepository.save(abonnement);
    }

    // Met à jour le statut de paiement d'un abonnement
    @Transactional
    public Abonnement updateStatut(Integer id, StatutPaiement nouveauStatut) {

        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement non trouvé"));

        abonnement.setStatutPaiement(nouveauStatut);
        return abonnementRepository.save(abonnement);
    }

    // Expire automatiquement les abonnements dont la date de fin est dépassée
    @Transactional
    public void expireAbonnementsDepasses() {

        // Trouve tous les abonnements dont la date de fin est dépassée
        List<Abonnement> expirés = abonnementRepository
                .findByDateFinBefore(LocalDate.now());

        // Pour chacun, change le statut en EXPIRE
        for (Abonnement a : expirés) {
            if (a.getStatutPaiement() == StatutPaiement.PAYE) {
                a.setStatutPaiement(StatutPaiement.EXPIRE);
                abonnementRepository.save(a);
            }
        }
    }

    // Supprime un abonnement
    @Transactional
    public void deleteById(Integer id) {
        if (!abonnementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Abonnement non trouvé");
        }
        abonnementRepository.deleteById(id);
    }
}