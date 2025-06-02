package tn.esprit.pi.services;

import tn.esprit.pi.dto.NoteDisplayDto;
import tn.esprit.pi.entities.Note;
import tn.esprit.pi.user.User;

import java.util.List;
import java.util.Map;

public interface INoteService {

    Note affecterNoteAUtilisateur(Long evaluationId, Long sprintId, Integer userId, double valeur);

    List<Note> getAllNotes();

    double calculerMoyenneProjet(Long projetId, Integer userId);

    double calculerMoyenneGeneraleUtilisateur(Integer userId);

    Map<User, Double> calculerMoyenneGeneraleTousUtilisateurs();
    List<NoteDisplayDto> getNoteDisplayList();
}