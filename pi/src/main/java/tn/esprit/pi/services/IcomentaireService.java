<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/services/IcomentaireService.java
package tn.esprit.pi.services;

import tn.esprit.pi.entities.Commentaire;
=======
package esprit.example.pi.services;

import esprit.example.pi.entities.Commentaire;
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/services/IcomentaireService.java


import java.util.List;

public interface IcomentaireService {
    Commentaire saveCommentaire(Commentaire commentaire);

    List<Commentaire> getAllCommentaires();
    void deleteCommentaire(Long id);
    Commentaire updateCommentaire(Long id, Commentaire commentaire);
}
