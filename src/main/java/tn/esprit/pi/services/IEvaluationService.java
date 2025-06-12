package tn.esprit.pi.services;
import tn.esprit.pi.entities.Evaluation;

import java.time.LocalDate;
import java.util.List;

public interface IEvaluationService {

    Evaluation saveEvaluation(Evaluation evaluation); // Ajouter une évaluation
    Evaluation getEvaluationById(Long id); // Récupérer une évaluation par ID
    List<Evaluation> getAllEvaluations(); // Obtenir toutes les évaluations
    List<Evaluation> getEvaluationsByProjet(Long projetId); // Obtenir les évaluations d'un projet

    List<Evaluation> getEvaluationsBySprint(Long projetId);
    void deleteEvaluation(Long id); // Supprimer une évaluation

    /*  Evaluation addEvaluationToProjet(Long projetId, Evaluation evaluation);*/





    /* Evaluation addEvaluationToSprint(Long sprintId, Evaluation evaluation);*/

    Evaluation updateEvaluation(Long id, Evaluation updatedEvaluation);
    Evaluation addEvaluationToProjet(Long projetId, String titre, String description,
                                     LocalDate dateEvaluation, double coef, Long sprintId);


}