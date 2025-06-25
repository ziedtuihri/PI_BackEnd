package tn.esprit.pi.services;

import tn.esprit.pi.entities.Commentaire;
import tn.esprit.pi.repositories.CommentaireRepo; // Utilisation de CommentaireRepo
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
