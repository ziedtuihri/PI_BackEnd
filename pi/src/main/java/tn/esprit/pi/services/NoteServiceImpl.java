package tn.esprit.pi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.entities.*;
import tn.esprit.pi.repositories.EvaluationRepo;
import tn.esprit.pi.repositories.NoteRepo;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {

    private final NoteRepo noteRepository;
    private final UserRepository userRepository;
    private final SprintRepo sprintRepo;
    private final EvaluationRepo evaluationRepo;

    @Override
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }



    @Override
    public Note affecterNoteAUtilisateur(Long evaluationId, Long sprintId, Integer userId, double valeur) {
        Note note = new Note();

        note.setValeur(valeur);

        Evaluation evaluation = evaluationRepo.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation introuvable"));
        Sprint sprint = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint introuvable"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        note.setEvaluation(evaluation);
        note.setSprint(sprint);
        note.setUser(user);

        return noteRepository.save(note);
    }



    @Override
    public double calculerMoyenneProjet(Long projetId, Integer userId) {
        List<Note> notes = noteRepository.findByEvaluation_Projet_IdProjetAndSprint_User_Id(projetId, userId);

        double total = 0;
        double totalCoef = 0;

        for (Note note : notes) {
            double coef = note.getEvaluation().getCoef();
            total += note.getValeur() * coef;
            totalCoef += coef;
        }

        return totalCoef != 0 ? total / totalCoef : 0;
    }

    @Override
    public double calculerMoyenneGeneraleUtilisateur(Integer userId) {
        List<Note> notes = noteRepository.findBySprint_User_Id(userId);

        // Grouper les notes par projet
        Map<Long, List<Note>> notesParProjet = notes.stream()
                .filter(n -> n.getEvaluation() != null && n.getEvaluation().getProjet() != null)
                .collect(Collectors.groupingBy(n -> n.getEvaluation().getProjet().getIdProjet()));

        double total = 0;
        int nbProjets = 0;

        for (List<Note> notesProjet : notesParProjet.values()) {
            double sommePonderee = 0;
            double totalCoef = 0;

            for (Note note : notesProjet) {
                double coef = note.getEvaluation().getCoef();
                sommePonderee += note.getValeur() * coef;
                totalCoef += coef;
            }

            if (totalCoef != 0) {
                double moyenneProjet = sommePonderee / totalCoef;
                total += moyenneProjet;
                nbProjets++;
            }
        }

        return nbProjets > 0 ? total / nbProjets : 0;
    }

    @Override
    public Map<User, Double> calculerMoyenneGeneraleTousUtilisateurs() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .collect(Collectors.toMap(u -> u, u -> calculerMoyenneGeneraleUtilisateur(u.getId())));
    }
}
