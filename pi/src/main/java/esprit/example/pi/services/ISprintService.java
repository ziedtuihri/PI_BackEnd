package esprit.example.pi.services;

import esprit.example.pi.entities.Sprint;

import java.util.List;

public interface ISprintService {
    List<Sprint> retrieveAllSprints();
    Sprint saveSprint(Sprint sprint);
    Sprint getSprintById(Long id);
    List<Sprint> getAllSprints();
}





