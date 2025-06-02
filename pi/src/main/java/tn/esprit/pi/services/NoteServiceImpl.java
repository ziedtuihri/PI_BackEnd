package tn.esprit.pi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.dto.NoteDisplayDto;
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
        // ❌ Vérifie si une note existe déjà pour ce sprint et utilisateur
        Optional<Note> existingNote = noteRepository.findByUser_IdAndSprint_IdSprint(userId, sprintId);
        if (existingNote.isPresent()) {
            throw new RuntimeException("❌ Une note a déjà été affectée à cet utilisateur pour ce sprint.");
        }

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
        List<Note> notes = noteRepository.findByEvaluation_Projet_IdProjetAndUser_Id(projetId, userId);

        double totalWeightedValue = 0;
        double totalCoefficient = 0;

        for (Note note : notes) {
            // Ensure evaluation exists before accessing its properties
            if (note.getEvaluation() != null) {
                double coefficient = note.getEvaluation().getCoef();
                totalWeightedValue += note.getValeur() * coefficient;
                totalCoefficient += coefficient;
            }
        }
        // Avoid division by zero
        return totalCoefficient != 0 ? totalWeightedValue / totalCoefficient : 0;
    }

    @Override
    public double calculerMoyenneGeneraleUtilisateur(Integer userId) {
        List<Note> notes = noteRepository.findByUser_Id(userId); // Changed to find by user ID directly

        // Group notes by project
        Map<Long, List<Note>> notesPerProject = notes.stream()
                .filter(n -> n.getEvaluation() != null && n.getEvaluation().getProjet() != null)
                .collect(Collectors.groupingBy(n -> n.getEvaluation().getProjet().getIdProjet()));

        double totalOverallAverage = 0;
        int numberOfProjects = 0;

        for (List<Note> projectNotes : notesPerProject.values()) {
            double projectWeightedSum = 0;
            double projectTotalCoef = 0;

            for (Note note : projectNotes) {
                double coefficient = note.getEvaluation().getCoef();
                projectWeightedSum += note.getValeur() * coefficient;
                projectTotalCoef += coefficient;
            }

            if (projectTotalCoef != 0) {
                double projectAverage = projectWeightedSum / projectTotalCoef;
                totalOverallAverage += projectAverage;
                numberOfProjects++;
            }
        }
        return numberOfProjects > 0 ? totalOverallAverage / numberOfProjects : 0;
    }

    @Override
    public Map<User, Double> calculerMoyenneGeneraleTousUtilisateurs() {
        List<User> users = userRepository.findAll();
        // Calculate the general average for each user and collect into a map
        return users.stream()
                .collect(Collectors.toMap(u -> u, u -> calculerMoyenneGeneraleUtilisateur(u.getId())));
    }

    @Override
    public List<NoteDisplayDto> getNoteDisplayList() {
        return noteRepository.findAll().stream()
                .filter(note -> note.getEvaluation() != null && note.getEvaluation().getProjet() != null && note.getUser() != null && note.getSprint() != null)
                .map(note -> new NoteDisplayDto(
                        note.getUser().getId(),
                        note.getUser().fullName(), // Assuming User has a fullName() method
                        note.getSprint().getIdSprint(),
                        note.getSprint().getNom(),
                        note.getValeur(),
                        note.getEvaluation().getProjet().getIdProjet(),
                        note.getEvaluation().getProjet().getNom()
                )).collect(Collectors.toList());
    }
}