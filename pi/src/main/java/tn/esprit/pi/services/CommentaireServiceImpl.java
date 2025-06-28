<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/services/CommentaireServiceImpl.java
package tn.esprit.pi.services;

import tn.esprit.pi.entities.Commentaire;
import tn.esprit.pi.repositories.CommentaireRepo; // Utilisation de CommentaireRepo
=======
package esprit.example.pi.services;

import esprit.example.pi.entities.Commentaire;
import esprit.example.pi.repositories.CommentaireRepo; // Utilisation de CommentaireRepo
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/services/CommentaireServiceImpl.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentaireServiceImpl implements IcomentaireService {

    private final CommentaireRepo commentaireRepo;

    @Autowired
    public CommentaireServiceImpl(CommentaireRepo commentaireRepo) {
        this.commentaireRepo = commentaireRepo;
    }

    @Override
    public Commentaire saveCommentaire(Commentaire commentaire) {
        return commentaireRepo.save(commentaire);
    }



    @Override
    public List<Commentaire> getAllCommentaires() {
        return commentaireRepo.findAll();
    }

    @Override
    public void deleteCommentaire(Long id) {
        commentaireRepo.deleteById(id);
    }

    @Override
    public Commentaire updateCommentaire(Long id, Commentaire commentaire) {
        if (commentaireRepo.existsById(id)) {
            commentaire.setIdCommentaire(id);
            return commentaireRepo.save(commentaire);
        } else {
            return null;
        }
    }
}
