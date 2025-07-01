package tn.esprit.pi.services;

import tn.esprit.pi.entities.Participant;
import tn.esprit.pi.repositories.ParticipantRepository;
import tn.esprit.pi.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;

    @Mock
    private ParticipantRepository participantRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAjouterParticipant() {
        Participant p = new Participant();
        p.setNom("John Doe");
        when(participantRepository.save(p)).thenReturn(p);

        Participant result = participantService.ajouterParticipant(p);
        assertThat(result).isEqualTo(p);
        verify(participantRepository).save(p);
    }

    @Test
    void testAjouterParticipant_NullParticipant() {
        assertThatThrownBy(() -> participantService.ajouterParticipant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Participant invalide");
    }

    @Test
    void testGetAllParticipants() {
        List<Participant> participants = List.of(new Participant(), new Participant());
        when(participantRepository.findAll()).thenReturn(participants);

        List result = participantService.getAllParticipants();
        assertThat(result).hasSize(2);
        verify(participantRepository).findAll();
    }

    @Test
    void testGetAllParticipants_EmptyList() {
        when(participantRepository.findAll()).thenReturn(Collections.emptyList());

        List result = participantService.getAllParticipants();
        assertThat(result).isEmpty();
        verify(participantRepository).findAll();
    }

    @Test
    void testGetParticipantById_WhenFound() {
        Participant p = new Participant();
        p.setId(1L);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(p));

        Participant result = participantService.getParticipantById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testGetParticipantById_WhenNotFound() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participantService.getParticipantById(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Participant non trouvé");
    }

    @Test
    void testGetParticipantById_NullId() {
        assertThatThrownBy(() -> participantService.getParticipantById(null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Participant non trouvé avec ID");
    }

    @Test
    void testDeleteParticipant_WhenFound() {
        Participant p = new Participant();
        p.setId(1L);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(p));
        doNothing().when(participantRepository).deleteById(1L);

        participantService.deleteParticipant(1L);
        verify(participantRepository).deleteById(1L);
    }

    @Test
    void testDeleteParticipant_WhenNotFound() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participantService.deleteParticipant(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Participant non trouvé");
    }

    @Test
    void testDeleteParticipant_NullId() {
        assertThatThrownBy(() -> participantService.deleteParticipant(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Participant non trouvé avec l'ID");
    }

    @Test
    void testUpdateParticipant_WhenFound() {
        Participant existing = new Participant();
        existing.setId(1L);
        existing.setNom("Ancien nom");
        existing.setEmail("ancien@mail.com");

        Participant updated = new Participant();
        updated.setNom("Nouveau nom");
        updated.setEmail("nouveau@mail.com");
        updated.setUser(new User());

        when(participantRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(participantRepository.save(any(Participant.class))).thenReturn(existing);

        Participant result = participantService.updateParticipant(1L, updated);

        assertThat(result.getNom()).isEqualTo("Nouveau nom");
        assertThat(result.getEmail()).isEqualTo("nouveau@mail.com");
        assertThat(result.getUser()).isNotNull();
        verify(participantRepository).save(existing);
    }

    @Test
    void testUpdateParticipant_WhenNotFound() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participantService.updateParticipant(1L, new Participant()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Participant non trouvé");
    }

    @Test
    void testUpdateParticipant_KeepExistingFieldsIfUpdatedIsNull() {
        Participant existing = new Participant();
        existing.setId(1L);
        existing.setNom("NomExistant");
        existing.setEmail("email@existant.com");

        Participant updated = new Participant(); // tous les champs null

        when(participantRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(participantRepository.save(any())).thenReturn(existing);

        Participant result = participantService.updateParticipant(1L, updated);

        assertThat(result.getNom()).isEqualTo("NomExistant");
        assertThat(result.getEmail()).isEqualTo("email@existant.com");
    }

    @Test
    void testUpdateParticipant_NullId() {
        Participant p = new Participant();
        assertThatThrownBy(() -> participantService.updateParticipant(null, p))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Participant non trouvé avec ID");
    }
}