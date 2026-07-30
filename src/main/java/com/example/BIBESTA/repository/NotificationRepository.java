package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Notification;
import com.example.BIBESTA.model.Notification.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Toutes les notifications d'un utilisateur
    // SELECT * FROM notification WHERE utilisateur_id = ?
    List<Notification> findByUtilisateurId(Integer utilisateurId);

    // Notifications non lues d'un utilisateur
    // SELECT * FROM notification WHERE utilisateur_id = ? AND statut = 'NON_LU'
    List<Notification> findByUtilisateurIdAndStatut(
            Integer utilisateurId,
            Statut statut);

    // Compte les notifications non lues d'un utilisateur
    // Utile pour afficher un badge "3 notifications" dans Angular
    long countByUtilisateurIdAndStatut(Integer utilisateurId, Statut statut);

    // Notifications par type
    List<Notification> findByType(String type);
}