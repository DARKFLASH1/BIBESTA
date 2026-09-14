package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.model.Abonnement.StatutPaiement;
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
class AbonnementServiceTest {

    @Mock
    private AbonnementRepository abonnementRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AbonnementService abonnementService;

    private Utilisateur utilisateur;
    private Abonnement abonnement;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setNom("Test");
        utilisateur.setEmail("test@example.com");

        abonnement = new Abonnement();
        abonnement.setId(1);
        abonnement.setUtilisateur(utilisateur);
        abonnement.setType("MENSUEL");
        abonnement.setMontant(new BigDecimal("5000.00"));
        abonnement.setDateDebut(LocalDate.now());
        abonnement.setDateFin(LocalDate.now().plusMonths(1));
        abonnement.setStatutPaiement(StatutPaiement.EN_ATTENTE);
    }

    @Test
    void testSaveAbonnement() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(abonnementRepository.save(any(Abonnement.class))).thenReturn(abonnement);

        Abonnement result = abonnementService.save(1, abonnement);

        assertNotNull(result);
        assertEquals(utilisateur, result.getUtilisateur());
        verify(abonnementRepository, times(1)).save(any(Abonnement.class));
    }

    @Test
    void testHasAbonnementActif_Vrai() {
        when(abonnementRepository.existsByUtilisateurIdAndStatutPaiementAndDateFinAfter(
                1, StatutPaiement.PAYE, LocalDate.now())).thenReturn(true);

        boolean result = abonnementService.hasAbonnementActif(1);

        assertTrue(result);
    }

    @Test
    void testHasAbonnementActif_Faux() {
        when(abonnementRepository.existsByUtilisateurIdAndStatutPaiementAndDateFinAfter(
                1, StatutPaiement.PAYE, LocalDate.now())).thenReturn(false);

        boolean result = abonnementService.hasAbonnementActif(1);

        assertFalse(result);
    }

    @Test
    void testUpdateStatut() {
        when(abonnementRepository.findById(1)).thenReturn(Optional.of(abonnement));
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(i -> i.getArguments()[0]);

        Abonnement result = abonnementService.updateStatut(1, StatutPaiement.PAYE);

        assertEquals(StatutPaiement.PAYE, result.getStatutPaiement());
        verify(abonnementRepository, times(1)).save(abonnement);
    }

    @Test
    void testExpireAbonnementsDepasses() {
        abonnement.setDateFin(LocalDate.now().minusDays(1));
        abonnement.setStatutPaiement(StatutPaiement.PAYE);

        when(abonnementRepository.findByDateFinBefore(any(LocalDate.class)))
                .thenReturn(List.of(abonnement));
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(i -> i.getArguments()[0]);

        abonnementService.expireAbonnementsDepasses();

        assertEquals(StatutPaiement.EXPIRE, abonnement.getStatutPaiement());
        verify(abonnementRepository, times(1)).save(abonnement);
    }

    @Test
    void testFindByUtilisateurId() {
        when(abonnementRepository.findByUtilisateurId(1)).thenReturn(List.of(abonnement));

        List<Abonnement> result = abonnementService.findByUtilisateurId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
