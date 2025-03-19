package esprit.example.pi.services;

import esprit.example.pi.entities.Evaluation;
import esprit.example.pi.entities.Projet;
import esprit.example.pi.repositories.EvaluationRepo;
import esprit.example.pi.repositories.ProjetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationServiceImpl implements IEvaluationService {

    @Autowired
    private EvaluationRepo evaluationRepo;
    @Autowired
    private ProjetRepo projetRepo; // ✅ Ajout du repo Projet

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
    public void deleteEvaluation(Long id) {
        if (!evaluationRepo.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Évaluation introuvable avec l'ID : " + id);
        }
        evaluationRepo.deleteById(id);
    }

  @Override
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
  }


}
