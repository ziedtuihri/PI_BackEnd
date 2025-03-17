package esprit.example.pi.services;

import esprit.example.pi.entities.Note;
import java.util.List;

public interface INoteService {

    //  Ajouter une note
    Note saveNote(Note note);

    //  Récupérer une note par ID
    Note getNoteById(Long id);

    //  Récupérer toutes les notes
    List<Note> getAllNotes();

    //  Récupérer les notes d'une évaluation spécifique
    List<Note> getNotesByEvaluation(Long evaluationId);

    //  Mettre à jour une note existante
    Note updateNote(Long id, Note updatedNote);

    //  Supprimer une note par ID
    void deleteNote(Long id);
}
