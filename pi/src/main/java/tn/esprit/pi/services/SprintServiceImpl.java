package tn.esprit.pi.services;

import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.CreateSprintDto; // Assurez-vous d'avoir ce DTO
import tn.esprit.pi.dto.SprintWithTasksDTO;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.repositories.ProjetRepo;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.repositories.TacheRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SprintServiceImpl implements ISprintService {

    private final SprintRepo sprintRepository;
    private final ProjetRepo projetRepository;
    private final TacheRepo tacheRepository;

    @Autowired
    public SprintServiceImpl(SprintRepo sprintRepository, ProjetRepo projetRepository, TacheRepo tacheRepository) {
        this.sprintRepository = sprintRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
    }

    @Override
    public Sprint saveSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    // Nouvelle méthode pour créer un sprint en l'associant à un projet
    public Sprint createSprint(CreateSprintDto createSprintDto) {
        Projet projet = projetRepository.findById(createSprintDto.getProjetId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID : " + createSprintDto.getProjetId()));

        Sprint sprint = new Sprint();
        sprint.setNom(createSprintDto.getNom());
        sprint.setDateDebut(createSprintDto.getDateDebut());
        sprint.setDateFin(createSprintDto.getDateFin());
        sprint.setStatut(createSprintDto.getStatut());
        sprint.setProjet(projet); // Associer le projet au sprint

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

    @Override
    public Sprint affecterEtudiantAuSprint(Long sprintId, String nomEtudiant) {
        Optional<Sprint> sprintOptional = sprintRepository.findById(sprintId);
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            List<String> etudiantsAffectes = sprint.getEtudiantsAffectes();
            if (etudiantsAffectes == null) {
                etudiantsAffectes = new ArrayList<>();
            }
            etudiantsAffectes.add(nomEtudiant);
            sprint.setEtudiantsAffectes(etudiantsAffectes);
            return sprintRepository.save(sprint);
        }
        return null;
    }

    @Override
    public Sprint supprimerEtudiantDuSprint(Long sprintId, String nomEtudiant) {
        Optional<Sprint> sprintOptional = sprintRepository.findById(sprintId);
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            List<String> etudiantsAffectes = sprint.getEtudiantsAffectes();
            if (etudiantsAffectes != null) {
                etudiantsAffectes.remove(nomEtudiant);
                sprint.setEtudiantsAffectes(etudiantsAffectes);
                return sprintRepository.save(sprint);
            }
        }
        return null;
    }

    @Override
    public List<String> getEtudiantsAffectesAuSprint(Long sprintId) {
        Optional<Sprint> sprintOptional = sprintRepository.findById(sprintId);
        return sprintOptional.map(Sprint::getEtudiantsAffectes).orElse(null);
    }

    @Override
    public List<Sprint> searchSprintsByNom(String nom) {
        return sprintRepository.findByNomContainingIgnoreCase(nom);
        //  return sprintRepository.searchByNom(nom.toLowerCase());
    }

    @Override
    public Optional<SprintWithTasksDTO> getSprintWithTasks(Long sprintId) {
        Optional<Sprint> sprintOptional = sprintRepository.findById(sprintId);
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            List<Tache> taches = tacheRepository.findBySprint_IdSprint(sprintId);
            SprintWithTasksDTO dto = new SprintWithTasksDTO(
                    sprint.getIdSprint(),
                    sprint.getNom(),
                    sprint.getDateDebut(),
                    sprint.getDateFin(),
                    sprint.getStatut().toString(),
                    // sprint.getDescription()
                    taches
            );
            return Optional.of(dto);
        }
        return Optional.empty();
    }


        @Override
        public Tache createTaskForSprint(Long sprintId, Tache tache) {
            Optional<Sprint> sprintOptional = sprintRepository.findById(sprintId);
            if (sprintOptional.isPresent()) {
                Sprint sprint = sprintOptional.get();
                tache.setSprint(sprint);
                return tacheRepository.save(tache);
            }
            return null;
        }
    }

