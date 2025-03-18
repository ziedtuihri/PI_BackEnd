package esprit.example.pi.controller;

import esprit.example.pi.entities.Evaluation;
import esprit.example.pi.services.IEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    @Autowired
    private IEvaluationService evaluationService;
    @PostMapping("/add_evaluation/{projetId}")
    public Evaluation addEvaluationToProjet(@PathVariable Long projetId, @RequestBody Evaluation evaluation) {
        return evaluationService.addEvaluationToProjet(projetId, evaluation);
    }





}
