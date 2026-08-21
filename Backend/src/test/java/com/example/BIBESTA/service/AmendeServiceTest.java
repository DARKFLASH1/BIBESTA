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
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EmpruntRepository empruntRepository;

    @Mock
    private PaiementService paiementService;

    @InjectMocks
    private AmendeService amendeService;

    private Utilisateur utilisateur;
    private Emprunt emprunt;
    private Amende amende;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setNom("Test");
        utilisateur.setEmail("test@example.com");

        emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setUtilisateur(utilisateur);
        emprunt.setDateRetourPrevu(LocalDate.now().minusDays(5));

        amende = new Amende();
        amende.setId(1L);
        amende.setMontant(new BigDecimal("10.00"));
        amende.setStatut(Amende.Statut.NON_PAYEE);
        amende.setUtilisateur(utilisateur);
    }

    @Test
    void testCreerAmendePourRetard() {
        when(empruntRepository.findById(1L)).thenReturn(Optional.of(emprunt));
        when(amendeRepository.save(any(Amende.class))).thenReturn(amende);

        Amende result = amendeService.creerAmendePourRetard(1L);

        assertNotNull(result);
        assertEquals(utilisateur, result.getUtilisateur());
        assertTrue(result.getMontant().compareTo(BigDecimal.ZERO) > 0);
        verify(amendeRepository, times(1)).save(any(Amende.class));
    }

    @Test
    void testMarquerCommePayee() {
        when(amendeRepository.findById(1L)).thenReturn(Optional.of(amende));
        when(amendeRepository.save(any(Amende.class))).thenAnswer(i -> i.getArguments()[0]);

        amendeService.marquerCommePayee(1L);

        assertEquals(Amende.Statut.PAYEE, amende.getStatut());
        verify(amendeRepository, times(1)).save(amende);
    }

    @Test
    void testGetAmendesParUtilisateur() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(amendeRepository.findByUtilisateurAndStatut(utilisateur, Amende.Statut.NON_PAYEE))
                .thenReturn(List.of(amende));

        List<Amende> result = amendeService.getAmendesParUtilisateur(1L, Amende.Statut.NON_PAYEE);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCalculerTotalAmendesImpayees() {
        when(amendeRepository.findByUtilisateurAndStatut(utilisateur, Amende.Statut.NON_PAYEE))
                .thenReturn(List.of(amende));

        BigDecimal total = amendeService.calculerTotalAmendesImpayees(utilisateur);

        assertEquals(new BigDecimal("10.00"), total);
    }
}
