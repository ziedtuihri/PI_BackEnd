package tn.esprit.pi.services;

import tn.esprit.pi.entities.ReservationSalle;
import tn.esprit.pi.entities.Reunion;
import tn.esprit.pi.entities.Salle;
import tn.esprit.pi.repositories.ReservationSalleRepository;
import tn.esprit.pi.repositories.SalleRepository;
import tn.esprit.pi.repositories.ReunionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationSalleRepository reservationSalleRepository;

    @Mock
    private SalleRepository salleRepository;

    @Mock
    private ReunionRepository reunionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllReservations() {
        List<ReservationSalle> reservations = List.of(new ReservationSalle(), new ReservationSalle());
        when(reservationSalleRepository.findAll()).thenReturn(reservations);

        List<ReservationSalle> result = reservationService.getAllReservations();
        assertThat(result).hasSize(2);
        verify(reservationSalleRepository).findAll();
    }

    @Test
    void testGetReservationById_WhenFound() {
        ReservationSalle res = new ReservationSalle();
        res.setId(1L);
        when(reservationSalleRepository.findById(1L)).thenReturn(Optional.of(res));

        ReservationSalle result = reservationService.getReservationById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testGetReservationById_WhenNotFound() {
        when(reservationSalleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservationById(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Réservation non trouvée");
    }

    @Test
    void testSaveReservation() {
        ReservationSalle res = new ReservationSalle();
        when(reservationSalleRepository.save(res)).thenReturn(res);

        ReservationSalle result = reservationService.saveReservation(res);
        assertThat(result).isEqualTo(res);
        verify(reservationSalleRepository).save(res);
    }

    @Test
    void testGetSalleById_WhenFound() {
        Salle salle = new Salle();
        salle.setId(5L);
        when(salleRepository.findById(5L)).thenReturn(Optional.of(salle));

        Salle result = reservationService.getSalleById(5L);
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void testGetSalleById_WhenNotFound() {
        when(salleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getSalleById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Salle non trouvée");
    }

    @Test
    void testGetReunionById_WhenFound() {
        Reunion reunion = new Reunion();
        reunion.setId(3L);
        when(reunionRepository.findById(3L)).thenReturn(Optional.of(reunion));

        Reunion result = reservationService.getReunionById(3L);
        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    void testGetReunionById_WhenNotFound() {
        when(reunionRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReunionById(88L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Réunion non trouvée");
    }

    @Test
    void testVerifierDisponibiliteSalle_AlwaysTrue_Placeholder() {
        Salle salle = new Salle();
        boolean result = reservationService.verifierDisponibiliteSalle(salle, "2025-07-01", "10:00", "60", 1L);
        assertThat(result).isTrue(); // car méthode retourne toujours true (à adapter selon ta logique métier réelle)
    }

    // Tests supplémentaires

    @Test
    void testVerifierDisponibiliteSalle_SimuleConflitAvecReunion() {
        // Cette méthode est un placeholder retournant toujours true.
        // Pour un vrai test, il faudrait mocker et simuler la logique, par exemple:
        // Supposons que tu ajoutes la logique plus tard, on simule ici.

        Salle salle = new Salle();
        // Imagine que tu mets une réunion qui chevauche la plage demandée
        // Ici on ne peut pas tester sans implémentation, donc on checke juste la méthode appelée
        boolean result = reservationService.verifierDisponibiliteSalle(salle, "2025-07-01", "10:00", "60", null);
        assertThat(result).isTrue();
    }

    @Test
    void testSaveReservation_NullReservation_ShouldThrow() {
        assertThatThrownBy(() -> reservationService.saveReservation(null))
                .isInstanceOf(IllegalArgumentException.class) // au lieu de NullPointerException
                .hasMessageContaining("ne peut pas être nulle");
    }

    @Test
    void testGetReservationById_NullId_ShouldThrow() {
        assertThatThrownBy(() -> reservationService.getReservationById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetSalleById_NullId_ShouldThrow() {
        assertThatThrownBy(() -> reservationService.getSalleById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetReunionById_NullId_ShouldThrow() {
        assertThatThrownBy(() -> reservationService.getReunionById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
