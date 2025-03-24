package esprit.example.pi.services;

import esprit.example.pi.entities.Evaluation;

import java.util.List;

public interface IEvaluationService {

    Evaluation saveEvaluation(Evaluation evaluation); // Ajouter une évaluation
    Evaluation getEvaluationById(Long id); // Récupérer une évaluation par ID
    List<Evaluation> getAllEvaluations(); // Obtenir toutes les évaluations
    List<Evaluation> getEvaluationsByProjet(Long projetId); // Obtenir les évaluations d'un projet
    void deleteEvaluation(Long id); // Supprimer une évaluation

    Evaluation addEvaluationToProjet(Long projetId, Evaluation evaluation);

    Evaluation updateEvaluation(Long id, Evaluation updatedEvaluation);


}
