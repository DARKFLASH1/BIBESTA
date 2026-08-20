package com.example.BIBESTA.service;

import com.example.BIBESTA.exception.BusinessException;
import com.example.BIBESTA.exception.ResourceNotFoundException;
import com.example.BIBESTA.model.*;
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
class EmpruntServiceTest {

    @Mock
    private EmpruntRepository empruntRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ExemplaireRepository exemplaireRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AmendeService amendeService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private HistoriqueService historiqueService;

    @Mock
    private AbonnementService abonnementService;

    @Mock
    private AmendeRepository amendeRepository;

    @InjectMocks
    private EmpruntService empruntService;

    private Utilisateur utilisateur;
    private Exemplaire exemplaire;
    private Livre livre;
    private Abonnement abonnement;

    @BeforeEach
    void setUp() {
        // Création d'un livre de test
        livre = new Livre();
        livre.setId(1);
        livre.setTitre("Test Livre");
        livre.setAuteur("Auteur Test");
        livre.setIsbn("1234567890");

        // Création d'un exemplaire de test
        exemplaire = new Exemplaire();
        exemplaire.setId(1);
        exemplaire.setLivre(livre);
        exemplaire.setEtat(Exemplaire.Etat.DISPONIBLE);

        // Création d'un utilisateur de test
        utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setNom("Test");
        utilisateur.setPrenom("User");
        utilisateur.setEmail("test@example.com");
        utilisateur.setRole(Utilisateur.Role.ETUDIANT);
        utilisateur.setStatut(Utilisateur.Statut.ACTIF);

        // Création d'un abonnement actif
        abonnement = new Abonnement();
        abonnement.setId(1);
        abonnement.setUtilisateur(utilisateur);
        abonnement.setStatutPaiement(Abonnement.StatutPaiement.PAYE);
        abonnement.setDateDebut(LocalDate.now().minusMonths(1));
        abonnement.setDateFin(LocalDate.now().plusMonths(11));
    }

    @Test
    void testFindAll() {
        List<Emprunt> emprunts = List.of(new Emprunt(), new Emprunt());
        when(empruntRepository.findAll()).thenReturn(emprunts);

        List<Emprunt> result = empruntService.findAll();

        assertEquals(2, result.size());
        verify(empruntRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        Emprunt emprunt = new Emprunt();
        when(empruntRepository.findById(1)).thenReturn(Optional.of(emprunt));

        Optional<Emprunt> result = empruntService.findById(1);

        assertTrue(result.isPresent());
        verify(empruntRepository, times(1)).findById(1);
    }

    @Test
    void testCreerEmprunt_Success() {
        // Mock des dépendances
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(exemplaireRepository.findById(1)).thenReturn(Optional.of(exemplaire));
        when(abonnementService.hasAbonnementActif(1)).thenReturn(true);
        when(amendeRepository.findByEmpruntUtilisateurIdAndStatut(1, Amende.Statut.EN_ATTENTE))
                .thenReturn(List.of());
        when(empruntRepository.findByUtilisateurIdAndStatut(1, Emprunt.Statut.EN_COURS))
                .thenReturn(List.of());
        when(exemplaireRepository.save(any(Exemplaire.class))).thenReturn(exemplaire);
        when(empruntRepository.save(any(Emprunt.class))).thenAnswer(i -> {
            Emprunt e = (Emprunt) i.getArguments()[0];
            e.setId(1);
            return e;
        });

        // Appel du service
        Emprunt result = empruntService.creerEmprunt(1, 1);

        // Vérifications
        assertNotNull(result);
        assertEquals(Emprunt.Statut.EN_COURS, result.getStatut());
        verify(notificationService, times(1)).creer(anyInt(), anyString(), anyString());
        verify(historiqueService, times(1)).enregistrerEmprunt(eq(1), anyInt(), eq(1));
    }

    @Test
    void testCreerEmprunt_UtilisateurInactif() {
        utilisateur.setStatut(Utilisateur.Statut.SUSPENDU);
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> empruntService.creerEmprunt(1, 1));

        assertTrue(exception.getMessage().contains("suspendu"));
    }

    @Test
    void testCreerEmprunt_ExemplaireNonDisponible() {
        exemplaire.setEtat(Exemplaire.Etat.EMPRUNTE);
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(exemplaireRepository.findById(1)).thenReturn(Optional.of(exemplaire));

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> empruntService.creerEmprunt(1, 1));

        assertTrue(exception.getMessage().contains("pas disponible"));
    }

    @Test
    void testCreerEmprunt_SansAbonnementActif() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(exemplaireRepository.findById(1)).thenReturn(Optional.of(exemplaire));
        when(abonnementService.hasAbonnementActif(1)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> empruntService.creerEmprunt(1, 1));

        assertTrue(exception.getMessage().contains("abonnement actif"));
    }

    @Test
    void testCreerEmprunt_AvecAmendesImpayees() {
        Amende amende = new Amende();
        amende.setStatut(Amende.Statut.EN_ATTENTE);
        
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(exemplaireRepository.findById(1)).thenReturn(Optional.of(exemplaire));
        when(abonnementService.hasAbonnementActif(1)).thenReturn(true);
        when(amendeRepository.findByEmpruntUtilisateurIdAndStatut(1, Amende.Statut.EN_ATTENTE))
                .thenReturn(List.of(amende));

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> empruntService.creerEmprunt(1, 1));

        assertTrue(exception.getMessage().contains("amendes impayées"));
    }

    @Test
    void testCreerEmprunt_QuotaDepasse() {
        // Créer 5 emprunts en cours (quota ETUDIANT = 5)
        List<Emprunt> empruntsEnCours = List.of(
                new Emprunt(), new Emprunt(), new Emprunt(), 
                new Emprunt(), new Emprunt()
        );
        
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(exemplaireRepository.findById(1)).thenReturn(Optional.of(exemplaire));
        when(abonnementService.hasAbonnementActif(1)).thenReturn(true);
        when(amendeRepository.findByEmpruntUtilisateurIdAndStatut(1, Amende.Statut.EN_ATTENTE))
                .thenReturn(List.of());
        when(empruntRepository.findByUtilisateurIdAndStatut(1, Emprunt.Statut.EN_COURS))
                .thenReturn(empruntsEnCours);

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> empruntService.creerEmprunt(1, 1));

        assertTrue(exception.getMessage().contains("Quota d'emprunts simultanés atteint"));
    }

    @Test
    void testEnregistrerRetour_Success() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1);
        emprunt.setStatut(Emprunt.Statut.EN_COURS);
        emprunt.setUtilisateur(utilisateur);
        emprunt.setExemplaire(exemplaire);
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(5));

        when(empruntRepository.findById(1)).thenReturn(Optional.of(emprunt));
        when(exemplaireRepository.save(any(Exemplaire.class))).thenReturn(exemplaire);
        when(empruntRepository.save(any(Emprunt.class))).thenReturn(emprunt);

        Emprunt result = empruntService.enregistrerRetour(1);

        assertEquals(Emprunt.Statut.RETOURNE, result.getStatut());
        assertNotNull(result.getDateRetourReelle());
        verify(notificationService, times(1)).creer(anyInt(), eq("RETOUR"), anyString());
    }

    @Test
    void testEnregistrerRetour_AvecRetard() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1);
        emprunt.setStatut(Emprunt.Statut.EN_COURS);
        emprunt.setUtilisateur(utilisateur);
        emprunt.setExemplaire(exemplaire);
        emprunt.setDateRetourPrevue(LocalDate.now().minusDays(3)); // 3 jours de retard

        when(empruntRepository.findById(1)).thenReturn(Optional.of(emprunt));
        when(exemplaireRepository.save(any(Exemplaire.class))).thenReturn(exemplaire);
        when(empruntRepository.save(any(Emprunt.class))).thenReturn(emprunt);
        when(amendeService.creerAmende(1)).thenReturn(new Amende());

        Emprunt result = empruntService.enregistrerRetour(1);

        assertEquals(Emprunt.Statut.RETOURNE, result.getStatut());
        verify(amendeService, times(1)).creerAmende(1);
    }

    @Test
    void testEnregistrerRetour_EmpruntDejaCloture() {
        Emprunt emprunt = new Emprunt();
        emprunt.setStatut(Emprunt.Statut.RETOURNE);

        when(empruntRepository.findById(1)).thenReturn(Optional.of(emprunt));

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> empruntService.enregistrerRetour(1));

        assertTrue(exception.getMessage().contains("déjà clôturé"));
    }

    @Test
    void testMettreAJourRetards() {
        Emprunt emprunt1 = new Emprunt();
        emprunt1.setId(1);
        emprunt1.setStatut(Emprunt.Statut.EN_COURS);
        emprunt1.setDateRetourPrevue(LocalDate.now().minusDays(2));
        emprunt1.setUtilisateur(utilisateur);

        when(empruntRepository.findByStatutAndDateRetourPrevueBefore(
                Emprunt.Statut.EN_COURS, LocalDate.now()))
                .thenReturn(List.of(emprunt1));
        when(empruntRepository.save(any(Emprunt.class))).thenReturn(emprunt1);

        empruntService.mettreAJourRetards();

        assertEquals(Emprunt.Statut.EN_RETARD, emprunt1.getStatut());
        verify(notificationService, times(1)).creer(anyInt(), eq("RETARD"), anyString());
    }
}
