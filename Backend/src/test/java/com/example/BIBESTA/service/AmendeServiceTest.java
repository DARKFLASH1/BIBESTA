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
class AmendeServiceTest {

    @Mock
    private AmendeRepository amendeRepository;

    @Mock
    private EmpruntRepository empruntRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AmendeService amendeService;

    private Utilisateur utilisateur;
    private Emprunt emprunt;
    private Amende amende;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setNom("Test");
        utilisateur.setEmail("test@example.com");

        emprunt = new Emprunt();
        emprunt.setId(1);
        emprunt.setUtilisateur(utilisateur);
        emprunt.setDateRetourPrevue(LocalDate.now().minusDays(5));

        amende = new Amende();
        amende.setId(1);
        amende.setMontant(new BigDecimal("500.00")); // 5 jours × 100 FCFA
        amende.setStatut(Amende.Statut.EN_ATTENTE);
        amende.setEmprunt(emprunt);
    }

    @Test
    void testCreerAmende() {
        when(empruntRepository.findById(1)).thenReturn(Optional.of(emprunt));
        when(amendeRepository.existsByEmpruntId(1)).thenReturn(false);
        when(amendeRepository.save(any(Amende.class))).thenReturn(amende);
        when(notificationService.creer(any(), any(), any())).thenReturn(new Notification());

        Amende result = amendeService.creerAmende(1);

        assertNotNull(result);
        assertTrue(result.getMontant().compareTo(BigDecimal.ZERO) > 0);
        verify(amendeRepository, times(1)).save(any(Amende.class));
    }

    @Test
    void testMarquerPayee() {
        when(amendeRepository.findById(1)).thenReturn(Optional.of(amende));
        when(amendeRepository.save(any(Amende.class))).thenAnswer(i -> i.getArguments()[0]);
        when(notificationService.creer(any(), any(), any())).thenReturn(new Notification());

        amendeService.marquerPayee(1);

        assertEquals(Amende.Statut.PAYEE, amende.getStatut());
        verify(amendeRepository, times(1)).save(amende);
    }

    @Test
    void testAnnulerAmende() {
        when(amendeRepository.findById(1)).thenReturn(Optional.of(amende));
        when(amendeRepository.save(any(Amende.class))).thenAnswer(i -> i.getArguments()[0]);

        Amende result = amendeService.annuler(1);

        assertEquals(Amende.Statut.ANNULEE, result.getStatut());
        verify(amendeRepository, times(1)).save(amende);
    }

    @Test
    void testFindEnAttenteByUtilisateurId() {
        when(amendeRepository.findByEmpruntUtilisateurIdAndStatut(1, Amende.Statut.EN_ATTENTE))
                .thenReturn(List.of(amende));

        List<Amende> result = amendeService.findEnAttenteByUtilisateurId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Amende.Statut.EN_ATTENTE, result.get(0).getStatut());
    }
}
