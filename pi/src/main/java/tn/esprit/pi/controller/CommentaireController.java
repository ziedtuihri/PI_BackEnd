<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/CommentaireController.java
package tn.esprit.pi.controller;

import tn.esprit.pi.entities.Commentaire;
import tn.esprit.pi.services.IcomentaireService;
=======
package esprit.example.pi.controller;

import esprit.example.pi.entities.Commentaire;
import esprit.example.pi.services.IcomentaireService;
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/CommentaireController.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequestMapping("/api/commentaires")
public class CommentaireController {

    private final IcomentaireService commentaireService;

    @Autowired
    public CommentaireController(IcomentaireService commentaireService) {
        this.commentaireService = commentaireService;
    }

<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/CommentaireController.java
=======
    @CrossOrigin(origins = "http://localhost:4200")
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/CommentaireController.java
    @PostMapping
    public ResponseEntity<Commentaire> createCommentaire(@RequestBody Commentaire commentaire) {
        Commentaire savedCommentaire = commentaireService.saveCommentaire(commentaire);
        return new ResponseEntity<>(savedCommentaire, HttpStatus.CREATED);
    }

<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/CommentaireController.java

=======
    @CrossOrigin(origins = "http://localhost:4200")
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/CommentaireController.java
    @GetMapping
    public ResponseEntity<List<Commentaire>> getAllCommentaires() {
        List<Commentaire> commentaires = commentaireService.getAllCommentaires();
        return new ResponseEntity<>(commentaires, HttpStatus.OK);
    }


<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/CommentaireController.java
=======
    @CrossOrigin(origins = "http://localhost:4200")
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/CommentaireController.java
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommentaire(@PathVariable Long id) {
        commentaireService.deleteCommentaire(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Retourne un code 204 (no content) si supprimé avec succès
    }

<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/CommentaireController.java
=======
    @CrossOrigin(origins = "http://localhost:4200")
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/CommentaireController.java
    @PutMapping("/{id}")
    public ResponseEntity<Commentaire> updateCommentaire(@PathVariable Long id, @RequestBody Commentaire commentaire) {
        Commentaire updatedCommentaire = commentaireService.updateCommentaire(id, commentaire);
        if (updatedCommentaire != null) {
            return new ResponseEntity<>(updatedCommentaire, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Retourne 404 si le commentaire n'existe pas
        }
    }
}
