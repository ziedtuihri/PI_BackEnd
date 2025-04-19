package esprit.example.pi.services;

import esprit.example.pi.dto.CalendarEventDto;
import esprit.example.pi.entities.Sprint;
import esprit.example.pi.entities.Projet;
import esprit.example.pi.repositories.ProjetRepo;
import esprit.example.pi.repositories.SprintRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SprintServiceImpl implements ISprintService {

    private final SprintRepo sprintRepository;  // Utilisation de SprintRepository
    private final ProjetRepo projetRepository;  // Utilisation de ProjetRepository

    // Injection des repositories via le constructeur
    @Autowired
    public SprintServiceImpl(SprintRepo sprintRepository, ProjetRepo projetRepository) {
        this.sprintRepository = sprintRepository;
        this.projetRepository = projetRepository;
    }

    @Override
    public Sprint saveSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    @Override
    public Sprint getSprintById(Long id) {
        return sprintRepository.findById(id).orElse(null);
    }

    @Override
    public List<Sprint> getAllSprints() {
        return sprintRepository.findAll();
    }

    @Override
    public void deleteSprint(Long id) {
        sprintRepository.deleteById(id);
    }

    @Override
    public Sprint updateSprint(Long id, Sprint sprint) {
        if (sprintRepository.existsById(id)) {
            sprint.setIdSprint(id);
            return sprintRepository.save(sprint);
        } else {
            return null;
        }
    }

    @Override
    public List<CalendarEventDto> getAllCalendarEvents() {
        List<CalendarEventDto> events = new ArrayList<>();

        List<Sprint> sprints = sprintRepository.findAll();
        for (Sprint sprint : sprints) {
            CalendarEventDto sprintEvent = new CalendarEventDto();
            sprintEvent.setTitle("Sprint: " + sprint.getNom());

            if (sprint.getProjet() != null) {
                sprintEvent.setTitle("Sprint: " + sprint.getNom() + " (Projet: " + sprint.getProjet().getNom() + ")");
            } else {
                sprintEvent.setTitle("Sprint: " + sprint.getNom() + " (Projet inconnu)");
            }

            sprintEvent.setStart(sprint.getDateDebut());
            sprintEvent.setEnd(sprint.getDateFin());
            events.add(sprintEvent);
        }

        List<Projet> projets = projetRepository.findAll();
        for (Projet projet : projets) {
            CalendarEventDto projetEvent = new CalendarEventDto();
            projetEvent.setTitle("Projet: " + projet.getNom());
            projetEvent.setStart(projet.getDateDebut());
            projetEvent.setEnd(projet.getDateFinPrevue());
            events.add(projetEvent);
        }

        return events;
    }


}
