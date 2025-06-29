package com.internship.platform.services;

import com.internship.platform.entities.Entreprise;
import com.internship.platform.entities.StatutEntreprise;
import com.internship.platform.repositories.EntrepriseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntrepriseServiceTest {

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @InjectMocks
    private EntrepriseService entrepriseService;

    private Entreprise entreprise;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        entreprise = new Entreprise();
        entreprise.setId(1L);
        entreprise.setNom("OpenAI");
        entreprise.setSecteurActivite("IA");
        entreprise.setTaille(200);
        entreprise.setAdresse("California");
        entreprise.setEmail("contact@openai.com");
        entreprise.setTelephone("123456789");
        entreprise.setSiteWeb("https://openai.com");
        entreprise.setContactRH("John Doe");
        entreprise.setStatut(StatutEntreprise.EN_ATTENTE);
    }

    @Test
    void testCreateEntreprise() {
        when(entrepriseRepository.save(any(Entreprise.class))).thenReturn(entreprise);

        Entreprise result = entrepriseService.createEntreprise(entreprise);

        assertEquals(StatutEntreprise.EN_ATTENTE, result.getStatut());
        verify(entrepriseRepository, times(1)).save(entreprise);
    }

    @Test
    void testGetAllEntreprises() {
        List<Entreprise> list = Arrays.asList(entreprise);
        when(entrepriseRepository.findAll()).thenReturn(list);

        List<Entreprise> result = entrepriseService.getAllEntreprises();

        assertEquals(1, result.size());
        verify(entrepriseRepository).findAll();
    }

    @Test
    void testGetEntrepriseById_Found() {
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entreprise));

        Entreprise result = entrepriseService.getEntrepriseById(1L);

        assertNotNull(result);
        assertEquals("OpenAI", result.getNom());
    }

    @Test
    void testGetEntrepriseById_NotFound() {
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            entrepriseService.getEntrepriseById(1L);
        });

        assertTrue(exception.getMessage().contains("Entreprise not found"));
    }

    @Test
    void testUpdateEntreprise() {
        Entreprise updated = new Entreprise();
        updated.setNom("Nouvelle entreprise");
        updated.setSecteurActivite("Tech");
        updated.setTaille(100);
        updated.setAdresse("Tunis");
        updated.setEmail("new@email.com");
        updated.setTelephone("999999999");
        updated.setSiteWeb("https://new.com");
        updated.setContactRH("Jane Doe");

        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entreprise));
        when(entrepriseRepository.save(any(Entreprise.class))).thenReturn(entreprise);

        Entreprise result = entrepriseService.updateEntreprise(1L, updated);

        assertEquals("Nouvelle entreprise", result.getNom());
        verify(entrepriseRepository).save(any(Entreprise.class));
    }

    @Test
    void testDeleteEntreprise() {
        doNothing().when(entrepriseRepository).deleteById(1L);

        entrepriseService.deleteEntreprise(1L);

        verify(entrepriseRepository).deleteById(1L);
    }

    @Test
    void testValidateEntreprise() {
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entreprise));
        when(entrepriseRepository.save(any(Entreprise.class))).thenReturn(entreprise);

        Entreprise result = entrepriseService.validateEntreprise(1L);

        assertEquals(StatutEntreprise.VALIDE, result.getStatut());
        verify(entrepriseRepository).save(entreprise);
    }

    @Test
    void testRefuseEntreprise() {
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entreprise));
        when(entrepriseRepository.save(any(Entreprise.class))).thenReturn(entreprise);

        Entreprise result = entrepriseService.refuseEntreprise(1L);

        assertEquals(StatutEntreprise.REFUSE, result.getStatut());
        verify(entrepriseRepository).save(entreprise);
    }
}
