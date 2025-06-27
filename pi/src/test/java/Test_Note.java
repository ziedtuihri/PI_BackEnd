import tn.esprit.pi.services.NoteServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tn.esprit.pi.dto.NoteDisplayDto;
import tn.esprit.pi.email.EmailService;
import tn.esprit.pi.entities.*;
import tn.esprit.pi.repositories.*;
import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Test_Note {

    @InjectMocks
    private NoteServiceImpl noteService;

    @Mock
    private NoteRepo noteRepo;

    @Mock
    private EvaluationRepo evaluationRepo;

    @Mock
    private SprintRepo sprintRepo;

    @Mock
    private ProjetRepo projetRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void testAffecterNoteAUtilisateur_alreadyExists() {
        when(noteRepo.findByUser_IdAndSprint_IdSprint(1, 1L))
                .thenReturn(Optional.of(new Note()));
        assertThrows(RuntimeException.class, () -> {
            noteService.affecterNoteAUtilisateur(1L, 1L, 1, 10);
        });
    }

    @Test
    void testCalculerMoyenneProjet_success() {
        Evaluation eval = new Evaluation();
        eval.setCoef(2.0);

        Projet projet = new Projet();
        projet.setIdProjet(1L);
        projet.setNom("Projet Test");

        User user = new User();
        user.setId(1);
        user.setFirstName("Mehdi");
        user.setLastName("Younsi");
        user.setEmail("mehdi@test.com");

        Note note = new Note();
        note.setValeur(14.0);
        note.setEvaluation(eval);

        when(noteRepo.findByEvaluation_Projet_IdProjetAndUser_Id(1L, 1)).thenReturn(List.of(note));
        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(projetRepo.findById(1L)).thenReturn(Optional.of(projet));

        double moyenne = noteService.calculerMoyenneProjet(1L, 1);
        assertEquals(14.0, moyenne);
    }

    @Test
    void testCalculerMoyenneProjet_zeroCoef() {
        Evaluation eval = new Evaluation();
        eval.setCoef(0.0);

        Note note = new Note();
        note.setValeur(18.0);
        note.setEvaluation(eval);

        when(noteRepo.findByEvaluation_Projet_IdProjetAndUser_Id(1L, 1)).thenReturn(List.of(note));

        double moyenne = noteService.calculerMoyenneProjet(1L, 1);
        assertEquals(0.0, moyenne);
    }



    @Test
    void testGetNoteDisplayList() {
        User user = new User();
        user.setId(1);
        user.setFirstName("Mehdi");
        user.setLastName("Younsi");
        user.setEmail("mehdi@test.com");

        Sprint sprint = new Sprint();
        sprint.setIdSprint(5L);
        sprint.setNom("Sprint 5");

        Projet projet = new Projet();
        projet.setIdProjet(8L);
        projet.setNom("Projet A");

        Evaluation eval = new Evaluation();
        eval.setProjet(projet);

        Note note = new Note();
        note.setUser(user);
        note.setSprint(sprint);
        note.setEvaluation(eval);
        note.setValeur(17.0);

        when(noteRepo.findAll()).thenReturn(List.of(note));

        List<NoteDisplayDto> result = noteService.getNoteDisplayList();
        assertEquals(1, result.size());

        NoteDisplayDto dto = result.get(0);
        assertEquals(1, dto.getUserId());
        assertEquals("Mehdi Younsi", dto.getUserName());
        assertEquals(5L, dto.getSprintId());
        assertEquals("Sprint 5", dto.getSprintNom());
        assertEquals(17.0, dto.getValeur());
        assertEquals(8L, dto.getProjetId());
        assertEquals("Projet A", dto.getProjetNom());
    }

    @Test
    void testCalculerMoyenneGeneraleTousUtilisateurs() {
        User user1 = new User();
        user1.setId(1);

        User user2 = new User();
        user2.setId(2);

        when(userRepo.findAll()).thenReturn(List.of(user1, user2));
        when(noteRepo.findBySprint_User_Id(anyInt())).thenReturn(Collections.emptyList());

        Map<User, Double> moyennes = noteService.calculerMoyenneGeneraleTousUtilisateurs();
        assertEquals(2, moyennes.size());
        assertTrue(moyennes.values().stream().allMatch(val -> val == 0.0));
    }
}
