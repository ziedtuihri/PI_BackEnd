package esprit.example.pi.services;

import esprit.example.pi.dto.CalendarEventDto;
import esprit.example.pi.dto.CreateSprintDto;
import esprit.example.pi.entities.Sprint;

import java.util.List;

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

}
