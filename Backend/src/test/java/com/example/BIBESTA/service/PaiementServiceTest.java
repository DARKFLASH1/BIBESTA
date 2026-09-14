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
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private AmendeRepository amendeRepository;

    @Mock
    private AbonnementRepository abonnementRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AmendeService amendeService;

    @Mock
    private HistoriqueService historiqueService;

    @InjectMocks
    private PaiementService paiementService;

    private Utilisateur utilisateur;
    private Amende amende;
    private Abonnement abonnement;
    private Paiement paiement;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setNom("Test");
        utilisateur.setEmail("test@example.com");

        amende = new Amende();
        amende.setId(1);
        amende.setMontant(new BigDecimal("10.00"));
        amende.setStatut(Amende.Statut.EN_ATTENTE);

        abonnement = new Abonnement();
        abonnement.setId(1);
        abonnement.setMontant(new BigDecimal("5000.00"));
        abonnement.setType("MENSUEL");
        abonnement.setDateDebut(LocalDate.now());
        abonnement.setDateFin(LocalDate.now().plusMonths(1));
        abonnement.setStatutPaiement(Abonnement.StatutPaiement.EN_ATTENTE);
        abonnement.setUtilisateur(utilisateur);

        paiement = new Paiement();
        paiement.setId(1);
        paiement.setMontant(new BigDecimal("10.00"));
        paiement.setStatut(Paiement.Statut.EFFECTUE);
    }

    @Test
    void testPayerAmende() {
        when(amendeRepository.findById(1)).thenReturn(Optional.of(amende));
        when(paiementRepository.existsByAmendeId(1)).thenReturn(false);
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiement);
        when(amendeService.marquerPayee(1)).thenReturn(amende);

        Paiement result = paiementService.payerAmende(1, "ESPECES");

        assertNotNull(result);
        assertEquals(Paiement.Statut.EFFECTUE, result.getStatut());
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    void testPayerAbonnement() {
        when(abonnementRepository.findById(1)).thenReturn(Optional.of(abonnement));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiement);
        when(notificationService.creer(any(), any(), any())).thenReturn(new Notification());
        doNothing().when(historiqueService).enregistrerPaiement(any(), any());

        Paiement result = paiementService.payerAbonnement(1, "MOBILE_MONEY");

        assertNotNull(result);
        assertEquals(Paiement.Statut.EFFECTUE, result.getStatut());
        verify(paiementRepository, times(1)).save(any(Paiement.class));
        assertEquals(Abonnement.StatutPaiement.PAYE, abonnement.getStatutPaiement());
    }

    @Test
    void testAnnulerPaiement() {
        when(paiementRepository.findById(1)).thenReturn(Optional.of(paiement));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(i -> i.getArguments()[0]);

        Paiement result = paiementService.annuler(1);

        assertEquals(Paiement.Statut.ANNULE, result.getStatut());
        verify(paiementRepository, times(1)).save(paiement);
    }

    @Test
    void testFindByUtilisateurId() {
        when(paiementRepository.findAllByUtilisateurId(1)).thenReturn(List.of(paiement));

        List<Paiement> result = paiementService.findByUtilisateurId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
