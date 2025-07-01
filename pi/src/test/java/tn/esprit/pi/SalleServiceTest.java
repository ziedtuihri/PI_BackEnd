package tn.esprit.pi.services;

import tn.esprit.pi.entities.Reunion;
import tn.esprit.pi.entities.ReservationSalle;
import tn.esprit.pi.entities.Salle;
import tn.esprit.pi.repositories.ReservationSalleRepository;
import tn.esprit.pi.repositories.ReunionRepository;
import tn.esprit.pi.repositories.SalleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalleServiceTest {

    @InjectMocks
    private SalleService salleService;

    @Mock
    private SalleRepository salleRepository;

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private ReservationSalleRepository reservationSalleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Tests existants

    @Test
    void testAjouterSalle() {
        Salle salle = new Salle();
        salle.setNom("Salle 1");
        when(salleRepository.save(salle)).thenReturn(salle);

        Salle result = salleService.ajouterSalle(salle);
        assertThat(result).isEqualTo(salle);
        verify(salleRepository).save(salle);
    }

    @Test
    void testGetSalleById_WhenExists() {
        Salle salle = new Salle();
        salle.setId(1L);
        when(salleRepository.findById(1L)).thenReturn(Optional.of(salle));

        Salle result = salleService.getSalleById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testGetSalleById_WhenNotFound() {
        when(salleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> salleService.getSalleById(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Salle non trouvée");
    }

    @Test
    void testGetAllSalleDisponible() {
        Salle salle1 = new Salle();
        salle1.setDisponible(true);
        Salle salle2 = new Salle();
        salle2.setDisponible(false);
        when(salleRepository.findAll()).thenReturn(List.of(salle1, salle2));

        List<Salle> disponibles = salleService.getAllSalleDisponible();
        assertThat(disponibles).containsExactly(salle1);
    }

    @Test
    void testGetSallesDisponiblesPourReunion_Success() {
        Salle salle = new Salle();
        salle.setDisponible(true);
        salle.setReunions(new ArrayList<>());
        salle.setReservations(new ArrayList<>());

        when(salleRepository.findAll()).thenReturn(List.of(salle));

        List<Salle> result = salleService.getSallesDisponiblesPourReunion(
                "2025-06-30", "10:00", "60");

        assertThat(result).containsExactly(salle);
    }

    @Test
    void testUpdateSalle_WhenExists() {
        Salle existing = new Salle();
        existing.setId(1L);
        existing.setNom("Ancienne Salle");

        Salle updated = new Salle();
        updated.setNom("Nouvelle Salle");
        updated.setCapacite(100);
        updated.setDisponible(true);

        when(salleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(salleRepository.saveAndFlush(any(Salle.class))).thenReturn(existing);

        Salle result = salleService.updateSalle(1L, updated);

        assertThat(result.getNom()).isEqualTo("Nouvelle Salle");
        assertThat(result.getCapacite()).isEqualTo(100);
    }

    @Test
    void testDeleteSalle_WhenSalleWithoutReunionsOrReservations() {
        Salle salle = new Salle();
        salle.setId(1L);
        salle.setReunions(new ArrayList<>());
        salle.setReservations(new ArrayList<>());

        when(salleRepository.findById(1L)).thenReturn(Optional.of(salle));
        doNothing().when(salleRepository).delete(salle);

        salleService.deleteSalle(1L);
        verify(salleRepository).delete(salle);
    }

    @Test
    void testDeleteSalle_WithReunion_ShouldThrowException() {
        Salle salle = new Salle();
        salle.setId(1L);
        salle.setReunions(List.of(new Reunion()));

        when(salleRepository.findById(1L)).thenReturn(Optional.of(salle));

        assertThatThrownBy(() -> salleService.deleteSalle(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Impossible de supprimer la salle");
    }

    // Tests supplémentaires

    @Test
    void testGetSallesDisponiblesPourReunion_WithConflictingReunion_ShouldExcludeSalle() {
        Reunion reunion = new Reunion();
        reunion.setDate("2025-06-30");
        reunion.setHeure("10:30");
        reunion.setDuree("60");

        Salle salle = new Salle();
        salle.setReunions(List.of(reunion));
        salle.setReservations(new ArrayList<>());

        when(salleRepository.findAll()).thenReturn(List.of(salle));

        List<Salle> result = salleService.getSallesDisponiblesPourReunion("2025-06-30", "10:00", "60");

        assertThat(result).isEmpty(); // conflit, salle non disponible
    }

    @Test
    void testGetSallesDisponiblesPourReunion_WithConflictingReservation_ShouldExcludeSalle() {
        Salle salle = new Salle();

        ReservationSalle reservation = new ReservationSalle();
        reservation.setDate("2025-06-30");
        reservation.setHeure("10:30");
        reservation.setDuree("60");

        salle.setReservations(List.of(reservation));
        salle.setReunions(new ArrayList<>());

        when(salleRepository.findAll()).thenReturn(List.of(salle));

        List<Salle> result = salleService.getSallesDisponiblesPourReunion("2025-06-30", "10:00", "60");

        assertThat(result).isEmpty();
    }

    @Test
    void testUpdateReunionSalle_Success() {
        Reunion reunion = new Reunion();
        reunion.setId(1L);
        Salle ancienneSalle = new Salle();
        ancienneSalle.setId(10L);
        ancienneSalle.setDisponible(false);
        reunion.setSalle(ancienneSalle);

        Salle nouvelleSalle = new Salle();
        nouvelleSalle.setId(20L);
        nouvelleSalle.setDisponible(true);

        when(reunionRepository.findById(1L)).thenReturn(Optional.of(reunion));
        when(salleRepository.findById(20L)).thenReturn(Optional.of(nouvelleSalle));
        when(reservationSalleRepository.findByReunion(reunion)).thenReturn(new ArrayList<>());
        when(salleRepository.save(any(Salle.class))).thenReturn(nouvelleSalle);
        when(reunionRepository.save(any(Reunion.class))).thenReturn(reunion);
        when(reservationSalleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        salleService.updateReunionSalle(1L, 20L);

        assertThat(ancienneSalle.isDisponible()).isTrue();
        assertThat(nouvelleSalle.isDisponible()).isFalse();

        verify(salleRepository, times(2)).save(any(Salle.class));
        verify(reunionRepository).save(reunion);
        verify(reservationSalleRepository).save(any());
    }

    @Test
    void testUpdateReunionSalle_NewSalleNotAvailable_ShouldThrow() {
        Reunion reunion = new Reunion();
        reunion.setId(1L);

        Salle nouvelleSalle = new Salle();
        nouvelleSalle.setId(20L);
        nouvelleSalle.setDisponible(false);

        when(reunionRepository.findById(1L)).thenReturn(Optional.of(reunion));
        when(salleRepository.findById(20L)).thenReturn(Optional.of(nouvelleSalle));

        assertThatThrownBy(() -> salleService.updateReunionSalle(1L, 20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("n'est pas disponible");
    }

    @Test
    void testUpdateSalle_NotFound_ShouldThrow() {
        when(salleRepository.findById(1L)).thenReturn(Optional.empty());

        Salle updated = new Salle();
        assertThatThrownBy(() -> salleService.updateSalle(1L, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Salle non trouvée");
    }

    @Test
    void testDeleteSalle_NotFound_ShouldThrow() {
        when(salleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salleService.deleteSalle(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Salle non trouvée");
    }

}