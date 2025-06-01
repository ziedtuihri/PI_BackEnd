package tn.esprit.pi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pi.entities.Evaluation;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.repositories.EvaluationRepo;
import tn.esprit.pi.repositories.ProjetRepo;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.services.IEvaluationService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements IEvaluationService {

    private final EvaluationRepo evaluationRepo;
    private final ProjetRepo projetRepo;
    private final SprintRepo sprintRepo;

    @Override
    public Evaluation saveEvaluation(Evaluation evaluation) {

        return evaluationRepo.save(evaluation);
    }

    @Override
    public Evaluation getEvaluationById(Long id) {
        return evaluationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Évaluation introuvable avec l'ID : " + id));
    }

    @Override
    public List<Evaluation> getAllEvaluations() {
        return evaluationRepo.findAll();
    }

    @Override
    public List<Evaluation> getEvaluationsByProjet(Long projetId) {
        return evaluationRepo.findByProjetId(projetId);
    }

    @Override
    public List<Evaluation> getEvaluationsBySprint(Long sprintId) {
        return evaluationRepo.findBySprintId(sprintId);
    }


    @Override
    public void deleteEvaluation(Long id) {
        if (!evaluationRepo.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Évaluation introuvable avec l'ID : " + id);
        }
        evaluationRepo.deleteById(id);
    }




  /* @Override
  public Evaluation addEvaluationToProjet(Long projetId, Evaluation evaluation) {
      // Récupérer le projet en fonction de l'ID
      Projet projet = projetRepo.findById(projetId)
              .orElseThrow(() -> new RuntimeException("Projet introuvable avec l'ID : " + projetId));

      // Vérifier que la date de l'évaluation est après la date de fin prévue du projet
      if (evaluation.getDateEvaluation().isBefore(projet.getDateFinPrevue())) {
          throw new RuntimeException("La date de l'évaluation doit être après la date de fin prévue du projet.");
      }

      // Associer l'évaluation au projet
      evaluation.setProjet(projet);

      // Sauvegarder l'évaluation et la retourner
      return evaluationRepo.save(evaluation);
  }*/

   /* @Override
    public Evaluation addEvaluationToSprint(Long sprintId, Evaluation evaluation) {
        // Récupérer le sprint par son ID
        Sprint sprint = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint introuvable avec l'ID : " + sprintId));

        // Vérifier que la date de l'évaluation est après la date de fin prévue du sprint
        if (evaluation.getDateEvaluation().isBefore(sprint.getDateFin())) {
            throw new RuntimeException("La date de l'évaluation doit être après la date de fin du sprint.");
        }

        // Associer l'évaluation au sprint
        evaluation.setSprint(sprint);

        // Sauvegarder et retourner l'évaluation
        return evaluationRepo.save(evaluation);
    }*/


    @Override
    public Evaluation updateEvaluation(Long id, Evaluation updatedEvaluation) {
        // Vérifier si l'évaluation existe
        Evaluation existingEvaluation = evaluationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Évaluation introuvable avec l'ID : " + id));

        // Vérifier si la nouvelle date d'évaluation est valide (doit être après la date de fin prévue du projet)
        if (updatedEvaluation.getDateEvaluation().isBefore(existingEvaluation.getProjet().getDateFinPrevue())) {
            throw new RuntimeException("La date d'évaluation doit être postérieure à la date de fin prévue du projet.");
        }

        // Mettre à jour les champs nécessaires
        existingEvaluation.setTitre(updatedEvaluation.getTitre());
        existingEvaluation.setDescription(updatedEvaluation.getDescription());
        existingEvaluation.setDateEvaluation(updatedEvaluation.getDateEvaluation());
        existingEvaluation.setCoef(updatedEvaluation.getCoef());

        // Sauvegarder et retourner l'évaluation mise à jour
        return evaluationRepo.save(existingEvaluation);
    }


@Override
    public Evaluation addEvaluationToProjet(Long projetId, String titre, String description,
                                            LocalDate dateEvaluation, double coef, Long sprintId) {
        Projet projet = projetRepo.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet not found"));

        Sprint sprint = null;
        if (sprintId != null) {
            sprint = sprintRepo.findById(sprintId)
                    .orElseThrow(() -> new RuntimeException("Sprint not found"));
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setTitre(titre);
        evaluation.setDescription(description);
        evaluation.setDateEvaluation(dateEvaluation);
        evaluation.setCoef(coef);
        evaluation.setProjet(projet);
        evaluation.setSprint(sprint);

        return evaluationRepo.save(evaluation);
    }


}
