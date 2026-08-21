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
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private PaiementService paiementService;

    private Utilisateur utilisateur;
    private Amende amende;
    private Paiement paiement;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setNom("Test");
        utilisateur.setEmail("test@example.com");

        amende = new Amende();
        amende.setId(1L);
        amende.setMontant(new BigDecimal("10.00"));
        amende.setStatut(Amende.Statut.NON_PAYEE);
        amende.setUtilisateur(utilisateur);

        paiement = new Paiement();
        paiement.setId(1L);
        paiement.setMontant(new BigDecimal("10.00"));
        paiement.setStatut(Paiement.Statut.VALIDE);
    }

    @Test
    void testPayerAmende() {
        when(amendeRepository.findById(1L)).thenReturn(Optional.of(amende));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiement);
        when(amendeRepository.save(any(Amende.class))).thenAnswer(i -> i.getArguments()[0]);

        Paiement result = paiementService.payerAmende(1L, "CARTE", "REF123");

        assertNotNull(result);
        assertEquals(Paiement.Statut.VALIDE, result.getStatut());
        assertEquals(Amende.Statut.PAYEE, amende.getStatut());
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    void testPayerAbonnement() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiement);

        Paiement result = paiementService.payerAbonnement(1L, "CARTE", "REF456");

        assertNotNull(result);
        assertEquals(Paiement.Statut.VALIDE, result.getStatut());
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    void testGetPaiementsParUtilisateur() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(paiementRepository.findByUtilisateurAndStatut(utilisateur, Paiement.Statut.VALIDE))
                .thenReturn(List.of(paiement));

        List<Paiement> result = paiementService.getPaiementsParUtilisateur(1L, Paiement.Statut.VALIDE);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testRembourserPaiement() {
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(paiement));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(i -> i.getArguments()[0]);

        paiementService.rembourserPaiement(1L);

        assertEquals(Paiement.Statut.REMBOURSE, paiement.getStatut());
        verify(paiementRepository, times(1)).save(paiement);
    }
}
