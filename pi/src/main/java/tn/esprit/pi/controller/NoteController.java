package tn.esprit.pi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.NoteDisplayDto;
import tn.esprit.pi.dto.NoteRequestDto;
import tn.esprit.pi.entities.Note;
import tn.esprit.pi.services.INoteService;

import tn.esprit.pi.user.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private INoteService noteService; // Injection du service

    // ✅ Récupérer toutes les notes
    @GetMapping("/get")
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    // ✅ Calculer la moyenne d’un utilisateur pour un projet donné
    @GetMapping("/moyenne/projet")
    public double getMoyenneProjet(
            @RequestParam Long projetId,
            @RequestParam Integer userId) {
        return noteService.calculerMoyenneProjet(projetId, userId);
    }

    // ✅ Calculer la moyenne générale d’un utilisateur
    @GetMapping("/moyenne/utilisateur/{userId}")
    public double getMoyenneGeneraleUtilisateur(@PathVariable Integer userId) {
        return noteService.calculerMoyenneGeneraleUtilisateur(userId);
    }

    // ✅ Calculer la moyenne générale pour tous les utilisateurs
    @GetMapping("/moyenne/tous")
    public Map<User, Double> getMoyenneGeneraleTousUtilisateurs() {
        return noteService.calculerMoyenneGeneraleTousUtilisateurs();
    }


    @PostMapping("/affecter")
    public Note affecterNote(@RequestBody NoteRequestDto dto) {
        return noteService.affecterNoteAUtilisateur(
                dto.getEvaluationId(),
                dto.getSprintId(),
                dto.getUserId(),
                dto.getValeur()
        );
    }

    @GetMapping("/display")
    public List<NoteDisplayDto> getNoteDisplayList() {
        return noteService.getNoteDisplayList();
    }




}


