package esprit.example.pi.controller;

import esprit.example.pi.entities.Evaluation;
import esprit.example.pi.services.IEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    @Autowired
    private IEvaluationService evaluationService;

   @PostMapping("/add_evaluation/{projetId}")
   public ResponseEntity<Object> addEvaluationToProjet(@PathVariable Long projetId, @RequestBody Evaluation evaluation) {
       try {
           // Ajouter l'évaluation au projet
           Evaluation savedEvaluation = evaluationService.addEvaluationToProjet(projetId, evaluation);
           return new ResponseEntity<>(savedEvaluation, HttpStatus.CREATED); // Retourner l'évaluation créée
       } catch (RuntimeException e) {
           // Retourner un message d'erreur si la validation échoue
           return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
       }
   }





}
