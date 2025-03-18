package esprit.example.pi.services;

import esprit.example.pi.entities.Evaluation;
import esprit.example.pi.entities.Note;
import esprit.example.pi.repositories.EvaluationRepo;
import esprit.example.pi.repositories.NoteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImpl implements INoteService {

    @Autowired
    private NoteRepo noteRepo;

    @Autowired
    private EvaluationRepo evaluationRepo; //  Correction : Injection du repo EvaluationRepo

    // ✅ Ajouter une note
    @Override
    public Note saveNote(Note note) {
        return noteRepo.save(note);
    }

    // ✅ Récupérer une note par ID
    @Override
    public Note getNoteById(Long id) {
        return noteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Note introuvable avec l'ID : " + id));
    }

    // ✅ Récupérer toutes les notes
    @Override
    public List<Note> getAllNotes() {
        return noteRepo.findAll();
    }

    // ✅ Récupérer les notes d'une évaluation spécifique
    @Override
    public List<Note> getNotesByEvaluation(Long evaluationId) {
        return noteRepo.findByEvaluationIdEvaluation(evaluationId);
    }

    // ✅ Mettre à jour une note existante
    @Override
    public Note updateNote(Long id, Note updatedNote) {
        Note existingNote = noteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Note introuvable avec l'ID : " + id));

        existingNote.setValeur(updatedNote.getValeur()); // Mettre à jour la valeur de la note
        return noteRepo.save(existingNote);
    }

    // ✅ Supprimer une note par ID
    @Override
    public void deleteNote(Long id) {
        if (!noteRepo.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Note introuvable avec l'ID : " + id);
        }
        noteRepo.deleteById(id);
    }

    // ✅ Ajouter une note à une évaluation existante
    @Override
    public Note addNoteToEvaluation(Long evaluationId, Note note) {
        // Récupérer l'évaluation, sinon exception
        Evaluation evaluation = evaluationRepo.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation introuvable avec l'ID : " + evaluationId));

        // Associer la note à l'évaluation et sauvegarder
        note.setEvaluation(evaluation);
        return noteRepo.save(note);
    }
}
