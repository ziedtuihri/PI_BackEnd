package com.esprit.tn.pi.interfaces;

import com.esprit.tn.pi.entities.*;

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