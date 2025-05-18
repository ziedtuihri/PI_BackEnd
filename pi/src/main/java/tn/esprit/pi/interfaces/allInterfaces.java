package tn.esprit.pi.interfaces;

import tn.esprit.pi.entities.*;
import tn.esprit.pi.user.User;

import java.util.List;

public interface allInterfaces {

    void deleteReunion(Long id);
    User ajouterUser(User u);
    Participant ajouterParticipant (Participant p);
    Salle ajouterSalle(Salle s);
    Reunion createReunion(Reunion reunion);
    Reunion updateReunion(Long reunionId, Reunion updatedReunion);
    List<EvenementDTO> getEvenements();
    List<Participant> getAllParticipants();
    List<User> getAllUsers();
    List<Salle> getAllSalle();

    List<Reunion> getAllReunions();
    void deleteParticipant(Long id);  // Ajouter cette ligne pour supprimer un participant

}