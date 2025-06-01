package tn.esprit.pi.services;

import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.CreateSprintDto;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache;

import java.util.List;
import java.util.Optional;

public interface ISprintService {
    Sprint createSprint(CreateSprintDto createSprintDto);
    Sprint saveSprint(Sprint sprint);
    Sprint getSprintById(Long id);
    List<Sprint> getAllSprints();
    void deleteSprint(Long id);
    Sprint updateSprint(Long id, Sprint sprint);
    List<CalendarEventDto> getAllCalendarEvents();
    Sprint affecterEtudiantAuSprint(Long sprintId, String nomEtudiant);
    Sprint supprimerEtudiantDuSprint(Long sprintId, String nomEtudiant);
    List<String> getEtudiantsAffectesAuSprint(Long sprintId);
     List<Sprint> searchSprintsByNom(String nom) ;
   /* Optional<SprintWithTasksDTO> getSprintWithTasks(Long sprintId);*/
    Tache createTaskForSprint(Long sprintId, Tache tache);


    public List<Sprint> getSprintsByProjetId(Long projetId);



    }
