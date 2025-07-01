package tn.esprit.pi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.pi.entities.EvenementDTO;
import tn.esprit.pi.entities.ReservationSalle;
import tn.esprit.pi.entities.Reunion;
import tn.esprit.pi.entities.Salle;
import tn.esprit.pi.repositories.ReservationSalleRepository;
import tn.esprit.pi.repositories.ReunionRepository;
import tn.esprit.pi.user.User;

@ExtendWith(MockitoExtension.class)
public class ReunionServiceUnitTest {

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private ReservationSalleRepository reservationSalleRepository;

    @InjectMocks  // Injecte automatiquement les mocks dans le service
    private ReunionService reunionService;

    @Test
    void testGetAllReunions() {
        Reunion r1 = new Reunion();
        r1.setId(1L);
        Reunion r2 = new Reunion();
        r2.setId(2L);

        when(reunionRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        List<Reunion> reunions = reunionService.getAllReunions();

        assertThat(reunions).hasSize(2);
        verify(reunionRepository, times(1)).findAll();
    }

    @Test
    void testCreateReunion_EnLigne_Success() {
        Reunion reunion = new Reunion();
        reunion.setTitre("Réunion en ligne");
        reunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);
        reunion.setLienZoom("https://zoom.us/test");
        reunion.setCreateur(new User());

        when(reunionRepository.save(any(Reunion.class))).thenAnswer(i -> i.getArguments()[0]);

        Reunion saved = reunionService.createReunion(reunion);

        assertThat(saved).isNotNull();
        assertThat(saved.getTitre()).isEqualTo("Réunion en ligne");
        verify(reunionRepository, times(1)).save(any(Reunion.class));
    }

    @Test
    void testDeleteReunion_Success() {
        Long reunionId = 1L;
        Reunion reunion = new Reunion();
        reunion.setId(reunionId);

        List<ReservationSalle> reservations = List.of();

        when(reunionRepository.existsById(reunionId)).thenReturn(true);
        when(reunionRepository.findById(reunionId)).thenReturn(Optional.of(reunion));
        when(reservationSalleRepository.findByReunion(reunion)).thenReturn(reservations);

        doAnswer(invocation -> {
            when(reunionRepository.existsById(reunionId)).thenReturn(false);
            return null;
        }).when(reunionRepository).deleteById(reunionId);

        reunionService.deleteReunion(reunionId);

        verify(reservationSalleRepository).deleteAll(reservations);
        verify(reunionRepository).deleteById(reunionId);
        verify(reunionRepository, times(2)).existsById(reunionId);
    }

    @Test
    void testVerifierDisponibiliteSalle_Disponible() {
        Salle salle = new Salle();
        salle.setId(1L);
        when(reunionRepository.findBySalleId(1L)).thenReturn(List.of());

        boolean dispo = reunionService.verifierDisponibiliteSalle(salle, "2025-07-01", "10:00", "60", null);

        assertThat(dispo).isTrue();
    }

    @Test
    void testGetEvenements_ShouldReturnOnlyFutureEvents() {
        Reunion r1 = new Reunion();
        r1.setId(1L);
        r1.setTitre("Future Meeting");
        r1.setDate(LocalDateTime.now().plusDays(1).toLocalDate().toString());
        r1.setHeure("10:00");
        r1.setDuree("60");
        r1.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);

        Reunion r2 = new Reunion();
        r2.setId(2L);
        r2.setTitre("Past Meeting");
        r2.setDate(LocalDateTime.now().minusDays(1).toLocalDate().toString());
        r2.setHeure("10:00");
        r2.setDuree("60");
        r2.setType(tn.esprit.pi.entities.enumeration.TypeReunion.PRESENTIEL);

        when(reunionRepository.findAll()).thenReturn(List.of(r1, r2));

        List<EvenementDTO> events = reunionService.getEvenements();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Future Meeting");
    }


    @Test
    void testGetReunionById_Success() {
        Long reunionId = 1L;
        Reunion reunion = new Reunion();
        reunion.setId(reunionId);
        reunion.setTitre("Test Reunion");

        when(reunionRepository.findById(reunionId)).thenReturn(Optional.of(reunion));

        Reunion result = reunionService.getReunionById(reunionId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reunionId);
        assertThat(result.getTitre()).isEqualTo("Test Reunion");
        verify(reunionRepository, times(1)).findById(reunionId);
    }

    @Test
    void testGetReunionById_NotFound() {
        Long reunionId = 999L;

        when(reunionRepository.findById(reunionId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            reunionService.getReunionById(reunionId);
        });

        verify(reunionRepository, times(1)).findById(reunionId);
    }

    @Test
    void testCreateReunion_EnLigne_WithoutZoomLink_ShouldThrowException() {
        Reunion reunion = new Reunion();
        reunion.setTitre("Réunion en ligne");
        reunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);
        reunion.setLienZoom(null); // Pas de lien Zoom
        reunion.setCreateur(new User());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.createReunion(reunion);
        });

        assertThat(exception.getMessage()).isEqualTo("Le lien Zoom ne peut pas être nul pour une réunion en ligne");
        verify(reunionRepository, never()).save(any(Reunion.class));
    }

    @Test
    void testCreateReunion_WithoutCreateur_ShouldThrowException() {
        Reunion reunion = new Reunion();
        reunion.setTitre("Test Reunion");
        reunion.setCreateur(null); // Pas de créateur

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.createReunion(reunion);
        });

        assertThat(exception.getMessage()).isEqualTo("Le créateur de la réunion ne peut pas être nul");
        verify(reunionRepository, never()).save(any(Reunion.class));
    }

    @Test
    void testCreateReunion_Presentiel_WithoutSalle_ShouldThrowException() {
        Reunion reunion = new Reunion();
        reunion.setTitre("Réunion présentiel");
        reunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.PRESENTIEL);
        reunion.setSalle(null); // Pas de salle
        reunion.setCreateur(new User());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.createReunion(reunion);
        });

        assertThat(exception.getMessage()).isEqualTo("La salle ne peut pas être nulle pour une réunion en présentiel");
        verify(reunionRepository, never()).save(any(Reunion.class));
    }

    @Test
    void testDeleteReunion_NotFound_ShouldThrowException() {
        Long reunionId = 999L;

        when(reunionRepository.existsById(reunionId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.deleteReunion(reunionId);
        });

        assertThat(exception.getMessage()).isEqualTo("La réunion avec cet ID n'existe pas.");
        verify(reunionRepository, never()).deleteById(reunionId);
    }

    @Test
    void testDeleteReunion_WithReservations_Success() {
        Long reunionId = 1L;
        Reunion reunion = new Reunion();
        reunion.setId(reunionId);

        ReservationSalle reservation = new ReservationSalle();
        List<ReservationSalle> reservations = List.of(reservation);

        when(reunionRepository.existsById(reunionId)).thenReturn(true);
        when(reunionRepository.findById(reunionId)).thenReturn(Optional.of(reunion));
        when(reservationSalleRepository.findByReunion(reunion)).thenReturn(reservations);

        // Simule la suppression réussie
        doAnswer(invocation -> {
            when(reunionRepository.existsById(reunionId)).thenReturn(false);
            return null;
        }).when(reunionRepository).deleteById(reunionId);

        reunionService.deleteReunion(reunionId);

        verify(reservationSalleRepository).deleteAll(reservations);
        verify(reunionRepository).deleteById(reunionId);
    }

    @Test
    void testVerifierDisponibiliteSalle_Occupee() {
        Salle salle = new Salle();
        salle.setId(1L);

        Reunion reunionExistante = new Reunion();
        reunionExistante.setId(2L);
        reunionExistante.setDate("2025-07-01");
        reunionExistante.setHeure("10:00");
        reunionExistante.setDuree("120"); // 2 heures

        when(reunionRepository.findBySalleId(1L)).thenReturn(List.of(reunionExistante));

        // Test avec chevauchement (11:00-12:00 chevauche avec 10:00-12:00)
        boolean dispo = reunionService.verifierDisponibiliteSalle(salle, "2025-07-01", "11:00", "60", null);

        assertThat(dispo).isFalse();
    }

    @Test
    void testVerifierDisponibiliteSalle_ExcludeCurrentReunion() {
        Salle salle = new Salle();
        salle.setId(1L);

        Long currentReunionId = 2L;
        Reunion reunionExistante = new Reunion();
        reunionExistante.setId(currentReunionId);
        reunionExistante.setDate("2025-07-01");
        reunionExistante.setHeure("10:00");
        reunionExistante.setDuree("120");

        when(reunionRepository.findBySalleId(1L)).thenReturn(List.of(reunionExistante));

        // Test avec la même réunion (doit être exclue)
        boolean dispo = reunionService.verifierDisponibiliteSalle(salle, "2025-07-01", "11:00", "60", currentReunionId);

        assertThat(dispo).isTrue(); // Disponible car la réunion actuelle est exclue
    }

    @Test
    void testUpdateReunion_Success() {
        Long reunionId = 1L;
        Reunion existingReunion = new Reunion();
        existingReunion.setId(reunionId);
        existingReunion.setTitre("Ancien titre");
        existingReunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);

        Reunion updatedReunion = new Reunion();
        updatedReunion.setTitre("Nouveau titre");
        updatedReunion.setDate("2025-07-01");
        updatedReunion.setHeure("14:00");
        updatedReunion.setDuree("90");
        updatedReunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);
        updatedReunion.setDescription("Nouvelle description");
        updatedReunion.setLienZoom("https://newzoom.com");
        updatedReunion.setCreateur(new User());

        when(reunionRepository.findById(reunionId)).thenReturn(Optional.of(existingReunion));
        when(reunionRepository.save(any(Reunion.class))).thenAnswer(i -> i.getArguments()[0]);

        Reunion result = reunionService.updateReunion(reunionId, updatedReunion);

        assertThat(result.getTitre()).isEqualTo("Nouveau titre");
        assertThat(result.getDate()).isEqualTo("2025-07-01");
        assertThat(result.getHeure()).isEqualTo("14:00");
        assertThat(result.getDuree()).isEqualTo("90");
        assertThat(result.getLienZoom()).isEqualTo("https://newzoom.com");

        verify(reunionRepository, times(1)).findById(reunionId);
        verify(reunionRepository, times(1)).save(any(Reunion.class));
    }

    @Test
    void testUpdateReunion_NotFound_ShouldThrowException() {
        Long reunionId = 999L;
        Reunion updatedReunion = new Reunion();

        when(reunionRepository.findById(reunionId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.updateReunion(reunionId, updatedReunion);
        });

        assertThat(exception.getMessage()).contains("Réunion non trouvée avec l'ID : " + reunionId);
        verify(reunionRepository, never()).save(any(Reunion.class));
    }

    @Test
    void testUpdateReunion_EnLigne_WithoutZoomLink_ShouldThrowException() {
        Long reunionId = 1L;
        Reunion existingReunion = new Reunion();
        existingReunion.setId(reunionId);
        existingReunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);

        Reunion updatedReunion = new Reunion();
        updatedReunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);
        updatedReunion.setLienZoom(null); // Pas de lien Zoom

        when(reunionRepository.findById(reunionId)).thenReturn(Optional.of(existingReunion));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.updateReunion(reunionId, updatedReunion);
        });

        assertThat(exception.getMessage()).isEqualTo("Le lien Zoom ne peut pas être nul pour une réunion en ligne");
        verify(reunionRepository, never()).save(any(Reunion.class));
    }

    @Test
    void testGetEvenements_EmptyList() {
        when(reunionRepository.findAll()).thenReturn(List.of());

        List<EvenementDTO> events = reunionService.getEvenements();

        assertThat(events).isEmpty();
        verify(reunionRepository, times(1)).findAll();
    }

    @Test
    void testGetEvenements_WithPastAndFutureEvents() {
        Reunion pastReunion = new Reunion();
        pastReunion.setId(1L);
        pastReunion.setTitre("Past Meeting");
        pastReunion.setDate(LocalDateTime.now().minusDays(1).toLocalDate().toString());
        pastReunion.setHeure("10:00");
        pastReunion.setDuree("60");
        pastReunion.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);

        Reunion futureReunion1 = new Reunion();
        futureReunion1.setId(2L);
        futureReunion1.setTitre("Future Meeting 1");
        futureReunion1.setDate(LocalDateTime.now().plusDays(1).toLocalDate().toString());
        futureReunion1.setHeure("10:00");
        futureReunion1.setDuree("90");
        futureReunion1.setType(tn.esprit.pi.entities.enumeration.TypeReunion.PRESENTIEL);

        Reunion futureReunion2 = new Reunion();
        futureReunion2.setId(3L);
        futureReunion2.setTitre("Future Meeting 2");
        futureReunion2.setDate(LocalDateTime.now().plusDays(2).toLocalDate().toString());
        futureReunion2.setHeure("14:00");
        futureReunion2.setDuree("120");
        futureReunion2.setType(tn.esprit.pi.entities.enumeration.TypeReunion.EN_LIGNE);
        futureReunion2.setLienZoom("https://zoom.com/test");

        when(reunionRepository.findAll()).thenReturn(List.of(pastReunion, futureReunion1, futureReunion2));

        List<EvenementDTO> events = reunionService.getEvenements();

        assertThat(events).hasSize(2);
        assertThat(events).extracting(EvenementDTO::getTitle)
                .containsExactlyInAnyOrder("Future Meeting 1", "Future Meeting 2");
    }

    @Test
    void testVerifierDisponibiliteSalle_InvalidDateFormat_ShouldThrowException() {
        Salle salle = new Salle();
        salle.setId(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.verifierDisponibiliteSalle(salle, "invalid-date", "10:00", "60", null);
        });

        assertThat(exception.getMessage()).contains("Erreur lors de la vérification de la disponibilité de la salle");
    }

    @Test
    void testVerifierDisponibiliteSalle_InvalidDureeFormat_ShouldThrowException() {
        Salle salle = new Salle();
        salle.setId(1L);

        // Pas de mock nécessaire car l'exception est lancée avant d'atteindre findBySalleId

        // Teste avec une durée complètement non numérique (pas de chiffres)
        // Cela devrait lever une exception car après replaceAll, on obtient une chaîne vide
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reunionService.verifierDisponibiliteSalle(salle, "2025-07-01", "10:00", "abc", null);
        });

        assertThat(exception.getMessage()).contains("Erreur lors de la vérification de la disponibilité de la salle");
    }

    @Test
    void testVerifierDisponibiliteSalle_MixedDureeFormat() {
        Salle salle = new Salle();
        salle.setId(1L);

        when(reunionRepository.findBySalleId(1L)).thenReturn(List.of());

        // Teste avec une durée contenant des chiffres et des lettres
        // Le service devrait extraire "60" de "60min"
        boolean dispo = reunionService.verifierDisponibiliteSalle(salle, "2025-07-01", "10:00", "60min", null);

        assertThat(dispo).isTrue();
        verify(reunionRepository, times(1)).findBySalleId(1L);
    }
}