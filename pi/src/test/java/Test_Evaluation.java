

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tn.esprit.pi.entities.Evaluation;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.repositories.EvaluationRepo;
import tn.esprit.pi.repositories.ProjetRepo;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.services.EvaluationServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Test_Evaluation {

    @InjectMocks
    private EvaluationServiceImpl evaluationService;

    @Mock
    private EvaluationRepo evaluationRepo;

    @Mock
    private ProjetRepo projetRepo;

    @Mock
    private SprintRepo sprintRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveEvaluation() {
        Evaluation eval = new Evaluation();
        eval.setTitre("Test Evaluation");

        when(evaluationRepo.save(eval)).thenReturn(eval);

        Evaluation saved = evaluationService.saveEvaluation(eval);
        assertNotNull(saved);
        assertEquals("Test Evaluation", saved.getTitre());
    }

    @Test
    void testGetEvaluationById_found() {
        Evaluation eval = new Evaluation();
        eval.setIdEvaluation(1L);

        when(evaluationRepo.findById(1L)).thenReturn(Optional.of(eval));

        Evaluation result = evaluationService.getEvaluationById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getIdEvaluation());
    }

    @Test
    void testGetEvaluationById_notFound() {
        when(evaluationRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> evaluationService.getEvaluationById(1L));
    }

    @Test
    void testGetAllEvaluations() {
        when(evaluationRepo.findAll()).thenReturn(List.of(new Evaluation(), new Evaluation()));

        List<Evaluation> list = evaluationService.getAllEvaluations();
        assertEquals(2, list.size());
    }

    @Test
    void testGetEvaluationsByProjet() {
        when(evaluationRepo.findByProjetId(1L)).thenReturn(List.of(new Evaluation()));
        List<Evaluation> result = evaluationService.getEvaluationsByProjet(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetEvaluationsBySprint() {
        when(evaluationRepo.findBySprintId(1L)).thenReturn(List.of(new Evaluation()));
        List<Evaluation> result = evaluationService.getEvaluationsBySprint(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteEvaluation_success() {
        when(evaluationRepo.existsById(1L)).thenReturn(true);
        evaluationService.deleteEvaluation(1L);
        verify(evaluationRepo, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteEvaluation_notFound() {
        when(evaluationRepo.existsById(1L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> evaluationService.deleteEvaluation(1L));
    }

    @Test
    void testUpdateEvaluation_valid() {
        Projet projet = new Projet();
        projet.setDateFinPrevue(LocalDate.of(2024, 1, 1));

        Evaluation existing = new Evaluation();
        existing.setIdEvaluation(1L);
        existing.setProjet(projet);

        Evaluation updated = new Evaluation();
        updated.setTitre("Updated");
        updated.setDescription("New desc");
        updated.setCoef(1.5);
        updated.setDateEvaluation(LocalDate.of(2024, 2, 1));

        when(evaluationRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(evaluationRepo.save(any(Evaluation.class))).thenReturn(existing);

        Evaluation result = evaluationService.updateEvaluation(1L, updated);

        assertEquals("Updated", result.getTitre());
        assertEquals("New desc", result.getDescription());
    }

    @Test
    void testUpdateEvaluation_invalidDate() {
        Projet projet = new Projet();
        projet.setDateFinPrevue(LocalDate.of(2025, 1, 1));

        Evaluation existing = new Evaluation();
        existing.setIdEvaluation(1L);
        existing.setProjet(projet);

        Evaluation updated = new Evaluation();
        updated.setDateEvaluation(LocalDate.of(2024, 1, 1));

        when(evaluationRepo.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> evaluationService.updateEvaluation(1L, updated));
    }

    @Test
    void testAddEvaluationToProjet_validWithSprint() {
        Projet projet = new Projet();
        projet.setIdProjet(1L);

        Sprint sprint = new Sprint();
        sprint.setIdSprint(2L);

        // Création de l'évaluation attendue
        Evaluation expectedEvaluation = new Evaluation();
        expectedEvaluation.setTitre("Titre");
        expectedEvaluation.setDescription("Desc");
        expectedEvaluation.setDateEvaluation(LocalDate.now());
        expectedEvaluation.setCoef(2.0);
        expectedEvaluation.setProjet(projet);
        expectedEvaluation.setSprint(sprint);

        // Mock des repositories
        when(projetRepo.findById(1L)).thenReturn(Optional.of(projet));
        when(sprintRepo.findById(2L)).thenReturn(Optional.of(sprint));
        when(evaluationRepo.save(any(Evaluation.class))).thenReturn(expectedEvaluation); // <-- CORRECTION

        // Appel de la méthode
        Evaluation result = evaluationService.addEvaluationToProjet(
                1L, "Titre", "Desc", LocalDate.now(), 2.0, 2L
        );

        // Assertions
        assertNotNull(result);
        assertEquals("Titre", result.getTitre());
        assertEquals("Desc", result.getDescription());
        assertEquals(projet, result.getProjet());
        assertEquals(sprint, result.getSprint());
    }

    @Test
    void testAddEvaluationToProjet_invalidProjet() {
        when(projetRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                evaluationService.addEvaluationToProjet(999L, "Titre", "Desc", LocalDate.now(), 2.0, null));
    }

    @Test
    void testAddEvaluationToProjet_invalidSprint() {
        Projet projet = new Projet();
        when(projetRepo.findById(1L)).thenReturn(Optional.of(projet));
        when(sprintRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                evaluationService.addEvaluationToProjet(1L, "Titre", "Desc", LocalDate.now(), 2.0, 99L));
    }
}
