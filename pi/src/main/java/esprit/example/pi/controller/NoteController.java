package esprit.example.pi.controller;

import esprit.example.pi.entities.Note;
import esprit.example.pi.services.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
public class NoteController {


    @Autowired
    private INoteService noteService; //  Injection du service

    //  Ajouter une note à une évaluation liée à un projet
    @PostMapping("/add_note/{evaluationId}")
    public Note addNoteToEvaluation(@PathVariable Long evaluationId, @RequestBody Note note) {
        return noteService.addNoteToEvaluation(evaluationId, note); //  Utilisation correcte de l'instance
    }

}
