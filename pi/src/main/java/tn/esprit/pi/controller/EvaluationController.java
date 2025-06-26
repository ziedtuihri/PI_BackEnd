package tn.esprit.pi.controller;


import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.entities.Evaluation;
import tn.esprit.pi.services.IEvaluationService;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")


public class   EvaluationController {
    @Autowired
    private IEvaluationService evaluationService;


    //ajouter evaluation
  /* @PostMapping("/add_evaluation/{projetId}")
   public ResponseEntity<Object> addEvaluationToProjet(@PathVariable Long projetId, @RequestBody Evaluation evaluation) {
       try {
           // Ajouter l'évaluation au projet
           Evaluation savedEvaluation = evaluationService.addEvaluationToProjet(projetId, evaluation);
           return new ResponseEntity<>(savedEvaluation, HttpStatus.CREATED); // Retourner l'évaluation créée
       } catch (RuntimeException e) {
           // Retourner un message d'erreur si la validation échoue
           return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
       }
   }*/


// ajouter une evaluation
    @PostMapping("/add_evaluation/{projetId}/{sprintId}")
    public ResponseEntity<Evaluation> addEvaluationToProjet(
            @PathVariable Long projetId,
            @PathVariable Long sprintId,
            @RequestBody Evaluation evaluation) {
        try {
            Evaluation savedEvaluation = evaluationService.addEvaluationToProjet(
                    projetId,
                    evaluation.getTitre(),
                    evaluation.getDescription(),
                    evaluation.getDateEvaluation(),
                    evaluation.getCoef(),
                    sprintId
            );
            return new ResponseEntity<>(savedEvaluation, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new RuntimeException("Problem !!");
        }
    }









    //supprimer une evaluation
   @DeleteMapping("/delete_evaluation/{evaluationId}")
   public ResponseEntity<Object> deleteEvaluation(@PathVariable Long evaluationId) {
       try {
           evaluationService.deleteEvaluation(evaluationId);
           return new ResponseEntity<>("Évaluation supprimée avec succès.", HttpStatus.OK);
       } catch (RuntimeException e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
       }
   }

   //afficher liste des evaluation par projet

    @GetMapping("/evaluation_by_projet/{projetId}")
    public ResponseEntity<List<Evaluation>> getEvaluationsByProjet(@PathVariable Long projetId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByProjet(projetId);
        if (evaluations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(evaluations, HttpStatus.OK);
    }

    //ajout evaluation dun projet et sprint




   /* @GetMapping("/evaluation_by_sprint/{sprintId}")
    public ResponseEntity<List<Evaluation>> getEvaluationsBySprint(@PathVariable Long sprintId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsBySprint(sprintId);
        if (evaluations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(evaluations, HttpStatus.OK);
    }*/



    //modifier evaluation

    @PutMapping("/update_evaluation/{id}")
    public ResponseEntity<Object> updateEvaluation(@PathVariable("id") Long id, @RequestBody Evaluation evaluation) {
        try {
            Evaluation updatedEvaluation = evaluationService.updateEvaluation(id, evaluation);
            return new ResponseEntity<>(updatedEvaluation, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get_all_evaluations")
    public ResponseEntity<List<Evaluation>> getAllEvaluations() {
        List<Evaluation> evaluations = evaluationService.getAllEvaluations();
        if (evaluations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(evaluations, HttpStatus.OK);
    }




}
