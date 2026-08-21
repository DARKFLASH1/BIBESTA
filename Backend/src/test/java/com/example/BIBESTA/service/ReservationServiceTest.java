package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @InjectMocks
    private ReservationService reservationService;

    private Utilisateur utilisateur;
    private Livre livre;
    private Exemplaire exemplaire;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setNom("Test");
        utilisateur.setPrenom("User");
        utilisateur.setEmail("test@example.com");

        livre = new Livre();
        livre.setId(1L);
        livre.setTitre("Test Livre");
        livre.setAuteur("Auteur Test");
        livre.setIsbn("1234567890");

        exemplaire = new Exemplaire();
        exemplaire.setId(1L);
        exemplaire.setLivre(livre);
        exemplaire.setEtat(Exemplaire.Etat.DISPONIBLE);
    }

    @Test
    void testCreerReservation() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        when(exemplaireRepository.findByLivreAndEtat(livre, Exemplaire.Etat.DISPONIBLE))
                .thenReturn(List.of(exemplaire));
        
        Reservation reservation = new Reservation();
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDate.now());
        
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Reservation result = reservationService.creerReservation(1L, 1L);

        assertNotNull(result);
        assertEquals(utilisateur, result.getUtilisateur());
        assertEquals(livre, result.getLivre());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testAnnulerReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatut(Reservation.Statut.EN_ATTENTE);
        
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        reservationService.annulerReservation(1L);

        assertEquals(Reservation.Statut.ANNULEE, reservation.getStatut());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void testTraiterReservation_ExemplaireDisponible() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatut(Reservation.Statut.EN_ATTENTE);
        reservation.setLivre(livre);
        
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(exemplaireRepository.findByLivreAndEtat(livre, Exemplaire.Etat.DISPONIBLE))
                .thenReturn(List.of(exemplaire));
        when(exemplaireRepository.save(any(Exemplaire.class))).thenAnswer(i -> i.getArguments()[0]);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        reservationService.traiterReservation(1L);

        assertEquals(Reservation.Statut.TRAITEE, reservation.getStatut());
        assertEquals(Exemplaire.Etat.RESERVE, exemplaire.getEtat());
    }

    @Test
    void testGetReservationsParUtilisateur() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findByUtilisateurAndStatut(utilisateur, Reservation.Statut.EN_ATTENTE))
                .thenReturn(List.of(new Reservation()));

        List<Reservation> result = reservationService.getReservationsParUtilisateur(1L, Reservation.Statut.EN_ATTENTE);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
