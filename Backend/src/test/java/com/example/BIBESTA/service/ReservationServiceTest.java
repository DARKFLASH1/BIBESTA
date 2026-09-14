package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.model.Exemplaire.StatutDisponibilite;
import com.example.BIBESTA.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ExemplaireRepository exemplaireRepository;

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HistoriqueService historiqueService;

    @InjectMocks
    private ReservationService reservationService;

    private Utilisateur utilisateur;
    private Livre livre;
    private Exemplaire exemplaire;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setNom("Test");
        utilisateur.setPrenom("User");
        utilisateur.setEmail("test@example.com");
        utilisateur.setStatut(Utilisateur.Statut.ACTIF);

        livre = new Livre();
        livre.setId(1);
        livre.setTitre("Test Livre");
        livre.setAuteur("Auteur Test");
        livre.setIsbn("1234567890");

        exemplaire = new Exemplaire();
        exemplaire.setId(1);
        exemplaire.setLivre(livre);
        exemplaire.setStatutDisponibilite(StatutDisponibilite.DISPONIBLE);
    }

    @Test
    void testCreerReservation() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(livreRepository.findById(1)).thenReturn(Optional.of(livre));
        when(reservationRepository.existsByUtilisateurIdAndLivreIdAndStatut(
                1, 1, Reservation.Statut.EN_ATTENTE)).thenReturn(false);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDate.now());
        reservation.setStatut(Reservation.Statut.EN_ATTENTE);
        reservation.setId(1);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(notificationService.creer(any(), any(), any())).thenReturn(new Notification());
        doNothing().when(historiqueService).enregistrerReservation(any(), any(), any());

        Reservation result = reservationService.creerReservation(1, 1);

        assertNotNull(result);
        assertEquals(utilisateur, result.getUtilisateur());
        assertEquals(livre, result.getLivre());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testAnnulerReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1);
        reservation.setStatut(Reservation.Statut.EN_ATTENTE);
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);
        when(notificationService.creer(any(), any(), any())).thenReturn(new Notification());

        Reservation result = reservationService.annuler(1);

        assertEquals(Reservation.Statut.ANNULEE, result.getStatut());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void testConfirmerReservationsSiDisponible() {
        Reservation reservation = new Reservation();
        reservation.setId(1);
        reservation.setStatut(Reservation.Statut.EN_ATTENTE);
        reservation.setLivre(livre);
        reservation.setUtilisateur(utilisateur);

        when(exemplaireRepository.findByLivreIdAndStatutDisponibilite(1, StatutDisponibilite.DISPONIBLE))
                .thenReturn(List.of(exemplaire));
        when(reservationRepository.findByLivreIdAndStatutOrderByDateReservationAsc(1, Reservation.Statut.EN_ATTENTE))
                .thenReturn(List.of(reservation));
        when(exemplaireRepository.save(any(Exemplaire.class))).thenAnswer(i -> i.getArguments()[0]);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);
        when(notificationService.creer(any(), any(), any())).thenReturn(new Notification());

        reservationService.confirmerReservationsSiDisponible(1);

        assertEquals(Reservation.Statut.CONFIRMEE, reservation.getStatut());
        assertEquals(StatutDisponibilite.RESERVE, exemplaire.getStatutDisponibilite());
    }

    @Test
    void testGetReservationsParUtilisateur() {
        when(reservationRepository.findByUtilisateurIdAndStatut(1, Reservation.Statut.EN_ATTENTE))
                .thenReturn(List.of(new Reservation()));

        List<Reservation> result = reservationService.findEnAttenteByUtilisateurId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
