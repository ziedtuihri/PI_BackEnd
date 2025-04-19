package esprit.example.pi.services;

import esprit.example.pi.dto.CalendarEventDto;
import esprit.example.pi.entities.Sprint;

import java.util.List;

public interface ISprintService {
    Sprint saveSprint(Sprint sprint);
    Sprint getSprintById(Long id);
    List<Sprint> getAllSprints();
    void deleteSprint(Long id);
    Sprint updateSprint(Long id, Sprint sprint);
    List<CalendarEventDto> getAllCalendarEvents();
}
