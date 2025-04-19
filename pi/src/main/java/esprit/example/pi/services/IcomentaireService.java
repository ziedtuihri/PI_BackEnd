package esprit.example.pi.services;

import esprit.example.pi.entities.Commentaire;


import java.util.List;

public interface IcomentaireService {
    Commentaire saveCommentaire(Commentaire commentaire);

    List<Commentaire> getAllCommentaires();
    void deleteCommentaire(Long id);
    Commentaire updateCommentaire(Long id, Commentaire commentaire);
}
