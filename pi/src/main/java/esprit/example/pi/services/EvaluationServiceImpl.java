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
        Projet projet = projetRepo.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable avec l'ID : " + projetId));

        evaluation.setProjet(projet); // Associer l'évaluation au projet
        return evaluationRepo.save(evaluation);
    }

}
