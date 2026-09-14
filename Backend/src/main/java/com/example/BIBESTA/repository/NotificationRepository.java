package com.example.BIBESTA.repository;

import com.example.BIBESTA.model.Notification;
import com.example.BIBESTA.model.Notification.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // ── Fetch join : notification sérialisée avec son utilisateur ────────────
    @EntityGraph(attributePaths = "utilisateur")
    List<Notification> findByUtilisateurId(Integer utilisateurId);

    @Override
    @EntityGraph(attributePaths = "utilisateur")
    Optional<Notification> findById(Integer id);

    // Notifications non lues d'un utilisateur
    // SELECT * FROM notification WHERE utilisateur_id = ? AND statut = 'NON_LU'
    @EntityGraph(attributePaths = "utilisateur")
    List<Notification> findByUtilisateurIdAndStatut(
            Integer utilisateurId,
            Statut statut);

    // Compte les notifications non lues d'un utilisateur
    // Utile pour afficher un badge "3 notifications" dans Angular
    long countByUtilisateurIdAndStatut(Integer utilisateurId, Statut statut);

    // Notifications par type
    @EntityGraph(attributePaths = "utilisateur")
    List<Notification> findByType(String type);
}