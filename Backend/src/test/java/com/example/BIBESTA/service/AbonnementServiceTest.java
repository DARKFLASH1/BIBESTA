package com.example.BIBESTA.service;

import com.example.BIBESTA.model.*;
import com.example.BIBESTA.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        utilisateur.setId(1L);
        utilisateur.setNom("Test");
        utilisateur.setEmail("test@example.com");

        abonnement = new Abonnement();
        abonnement.setId(1L);
        abonnement.setUtilisateur(utilisateur);
        abonnement.setDateDebut(LocalDate.now());
        abonnement.setDateFin(LocalDate.now().plusMonths(12));
        abonnement.setStatut(Abonnement.Statut.ACTIF);
    }

    @Test
    void testCreerAbonnement() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(abonnementRepository.save(any(Abonnement.class))).thenReturn(abonnement);

        Abonnement result = abonnementService.creerAbonnement(1L, 12);

        assertNotNull(result);
        assertEquals(utilisateur, result.getUtilisateur());
        assertEquals(Abonnement.Statut.ACTIF, result.getStatut());
        verify(abonnementRepository, times(1)).save(any(Abonnement.class));
    }

    @Test
    void testRenouvelerAbonnement() {
        when(abonnementRepository.findById(1L)).thenReturn(Optional.of(abonnement));
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(i -> i.getArguments()[0]);

        abonnementService.renouvelerAbonnement(1L, 6);

        LocalDate nouvelleDateFin = LocalDate.now().plusMonths(6);
        assertTrue(abonnement.getDateFin().isAfter(nouvelleDateFin.minusDays(5)));
        assertEquals(Abonnement.Statut.ACTIF, abonnement.getStatut());
    }

    @Test
    void testVerifierAbonnementValide_Actif() {
        when(abonnementRepository.findByUtilisateurAndStatut(utilisateur, Abonnement.Statut.ACTIF))
                .thenReturn(List.of(abonnement));

        boolean result = abonnementService.verifierAbonnementValide(utilisateur);

        assertTrue(result);
    }

    @Test
    void testVerifierAbonnementValide_Inactif() {
        when(abonnementRepository.findByUtilisateurAndStatut(utilisateur, Abonnement.Statut.ACTIF))
                .thenReturn(List.of());

        boolean result = abonnementService.verifierAbonnementValide(utilisateur);

        assertFalse(result);
    }

    @Test
    void testDesactiverAbonnement() {
        when(abonnementRepository.findById(1L)).thenReturn(Optional.of(abonnement));
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(i -> i.getArguments()[0]);

        abonnementService.desactiverAbonnement(1L);

        assertEquals(Abonnement.Statut.EXPIRE, abonnement.getStatut());
        verify(abonnementRepository, times(1)).save(abonnement);
    }

    @Test
    void testGetAbonnementsActifs() {
        when(abonnementRepository.findByStatut(Abonnement.Statut.ACTIF))
                .thenReturn(List.of(abonnement));

        List<Abonnement> result = abonnementService.getAbonnementsActifs();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
