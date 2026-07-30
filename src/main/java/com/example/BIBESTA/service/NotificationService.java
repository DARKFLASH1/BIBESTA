package com.example.BIBESTA.service;

import com.example.BIBESTA.model.Notification;
import com.example.BIBESTA.model.Notification.Statut;
import com.example.BIBESTA.model.Utilisateur;
import com.example.BIBESTA.repository.NotificationRepository;
import com.example.BIBESTA.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    // Retourne toutes les notifications d'un utilisateur
    public List<Notification> findByUtilisateurId(Integer utilisateurId) {
        return notificationRepository.findByUtilisateurId(utilisateurId);
    }

    // Retourne les notifications non lues d'un utilisateur
    public List<Notification> findNonLues(Integer utilisateurId) {
        return notificationRepository.findByUtilisateurIdAndStatut(
                utilisateurId,
                Statut.NON_LU);
    }

    // Compte les notifications non lues (pour le badge dans Angular)
    public long countNonLues(Integer utilisateurId) {
        return notificationRepository.countByUtilisateurIdAndStatut(
                utilisateurId,
                Statut.NON_LU);
    }

    // Retourne une notification par son id
    public Optional<Notification> findById(Integer id) {
        return notificationRepository.findById(id);
    }

    // Crée une nouvelle notification pour un utilisateur
    // Cette méthode sera appelée automatiquement par les autres services
    // Ex: EmpruntService appellera cette méthode en cas de retard
    public Notification creer(Integer utilisateurId, String type, String contenu) {

        // Vérifie que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé avec l'id : " + utilisateurId));

        // Crée la notification
        Notification notification = new Notification();
        notification.setUtilisateur(utilisateur);
        notification.setType(type);
        notification.setContenu(contenu);
        notification.setDate(LocalDate.now()); // date d'aujourd'hui
        notification.setStatut(Statut.NON_LU); // non lue par défaut

        return notificationRepository.save(notification);
    }

    // Marque une notification comme lue
    public Notification marquerCommeLue(Integer id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        notification.setStatut(Statut.LU);
        return notificationRepository.save(notification);
    }

    // Marque toutes les notifications d'un utilisateur comme lues
    public void marquerToutesCommeLues(Integer utilisateurId) {

        List<Notification> nonLues = notificationRepository
                .findByUtilisateurIdAndStatut(utilisateurId, Statut.NON_LU);

        // Pour chaque notification non lue → on la marque comme lue
        for (Notification n : nonLues) {
            n.setStatut(Statut.LU);
            notificationRepository.save(n);
        }
    }

    // Supprime une notification
    public void deleteById(Integer id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification non trouvée");
        }
        notificationRepository.deleteById(id);
    }
}