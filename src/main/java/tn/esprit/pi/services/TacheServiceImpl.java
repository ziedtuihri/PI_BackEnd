package tn.esprit.pi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.dto.TacheCreationDTO;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.entities.enumerations.TaskStatus;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.repositories.TacheRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TacheServiceImpl implements ITacheService {

    private final TacheRepo tacheRepository;
    private final SprintRepo sprintRepository;
    private final ISprintService sprintService;
    private final IProjetService projetService;

    @Autowired
    public TacheServiceImpl(TacheRepo tacheRepository, SprintRepo sprintRepository,
                            ISprintService sprintService, IProjetService projetService) {
        this.tacheRepository = tacheRepository;
        this.sprintRepository = sprintRepository;
        this.sprintService = sprintService;
        this.projetService = projetService;
    }

    @Override
    @Transactional
    public Tache createTacheFromDTO(TacheCreationDTO tacheCreationDTO) {
        Tache tache = new Tache();

        tache.setNom(tacheCreationDTO.getNom());
        tache.setDescription(tacheCreationDTO.getDescription());
        tache.setDateDebut(tacheCreationDTO.getDateDebut());
        tache.setDateFin(tacheCreationDTO.getDateFin());
        tache.setStatut(tacheCreationDTO.getStatut() != null ? tacheCreationDTO.getStatut() : TaskStatus.TODO);
        tache.setStoryPoints(tacheCreationDTO.getStoryPoints());
        tache.setEstimatedHours(tacheCreationDTO.getEstimatedHours());
        tache.setLoggedHours(0.0);
        tache.setEtudiantsAffectes(new ArrayList<>());

        validateTaskData(tache);

        if (tacheCreationDTO.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(tacheCreationDTO.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + tacheCreationDTO.getSprintId()));
            tache.setSprint(sprint);
            tache.setProjet(sprint.getProjet());

            if (sprint.getEtudiantsAffectes() != null) {
                tache.setEtudiantsAffectes(sprint.getEtudiantsAffectes().stream()
                        .map(projetService::cleanEmail)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }

            validateTaskDatesWithSprint(tache);
        } else {
            tache.setSprint(null);
            tache.setProjet(null);
            if (tacheCreationDTO.getEtudiantsAffectes() != null && !tacheCreationDTO.getEtudiantsAffectes().isEmpty()) {
                tache.setEtudiantsAffectes(tacheCreationDTO.getEtudiantsAffectes().stream()
                        .map(projetService::cleanEmail)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
        }

        return tacheRepository.save(tache);
    }

    @Override
    @Transactional
    public Tache updateTacheFromDTO(Long id, TacheCreationDTO tacheCreationDTO) {
        Tache existingTache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id)); // Using RuntimeException

        Optional.ofNullable(tacheCreationDTO.getNom()).ifPresent(existingTache::setNom);
        Optional.ofNullable(tacheCreationDTO.getDescription()).ifPresent(existingTache::setDescription);
        Optional.ofNullable(tacheCreationDTO.getDateDebut()).ifPresent(existingTache::setDateDebut);
        Optional.ofNullable(tacheCreationDTO.getDateFin()).ifPresent(existingTache::setDateFin);
        Optional.ofNullable(tacheCreationDTO.getStatut()).ifPresent(existingTache::setStatut);
        Optional.ofNullable(tacheCreationDTO.getStoryPoints()).ifPresent(existingTache::setStoryPoints);
        Optional.ofNullable(tacheCreationDTO.getEstimatedHours()).ifPresent(existingTache::setEstimatedHours);

        if (tacheCreationDTO.getSprintId() != null) {
            if (existingTache.getSprint() == null || !existingTache.getSprint().getIdSprint().equals(tacheCreationDTO.getSprintId())) {
                Sprint newSprint = sprintRepository.findById(tacheCreationDTO.getSprintId())
                        .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + tacheCreationDTO.getSprintId()));
                existingTache.setSprint(newSprint);
                existingTache.setProjet(newSprint.getProjet());
                existingTache.setEtudiantsAffectes(newSprint.getEtudiantsAffectes() != null ?
                        newSprint.getEtudiantsAffectes().stream()
                                .map(projetService::cleanEmail)
                                .collect(Collectors.toCollection(ArrayList::new)) : new ArrayList<>());
            }
        } else if (existingTache.getSprint() != null) {
            existingTache.setSprint(null);
            existingTache.setProjet(null);
            existingTache.setEtudiantsAffectes(new ArrayList<>());
        }

        validateTaskData(existingTache);
        validateTaskDatesWithSprint(existingTache);

        return tacheRepository.save(existingTache);
    }

    @Override
    @Transactional
    public Tache updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Tache tache = tacheRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + taskId)); // Using RuntimeException

        if (newStatus == null) {
            throw new IllegalArgumentException("Le statut de la tâche ne peut pas être nul."); // Using IllegalArgumentException
        }

        tache.setStatut(newStatus);
        Tache updated = tacheRepository.save(tache);

        if (newStatus == TaskStatus.DONE && updated.getSprint() != null) {
            sprintService.checkAndCompleteSprintIfAllTasksDone(updated.getSprint().getIdSprint());
        }
        return updated;
    }

    @Override
    @Transactional
    public Tache saveTache(Tache tache) {
        if (tache.getLoggedHours() == null) tache.setLoggedHours(0.0);
        if (tache.getEtudiantsAffectes() == null) {
            tache.setEtudiantsAffectes(new ArrayList<>());
        } else {
            tache.setEtudiantsAffectes(tache.getEtudiantsAffectes().stream()
                    .map(projetService::cleanEmail)
                    .collect(Collectors.toCollection(ArrayList::new)));
        }

        if (tache.getSprint() != null && tache.getSprint().getIdSprint() != null) {
            Sprint sprint = sprintRepository.findById(tache.getSprint().getIdSprint())
                    .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + tache.getSprint().getIdSprint()));
            tache.setSprint(sprint);
            tache.setProjet(sprint.getProjet());
            if (sprint.getEtudiantsAffectes() != null) {
                tache.setEtudiantsAffectes(sprint.getEtudiantsAffectes().stream()
                        .map(projetService::cleanEmail)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
        } else {
            tache.setSprint(null);
            tache.setProjet(null);
        }

        validateTaskData(tache);
        validateTaskDatesWithSprint(tache);

        return tacheRepository.save(tache);
    }

    @Override
    public Tache getTacheById(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id)); // Using RuntimeException
    }

    @Override
    public List<Tache> getAllTaches() {
        return tacheRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteTache(Long id) {
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id)); // Using RuntimeException

        if (tache.getSprint() != null) {
            tache.getSprint().getTaches().remove(tache);
            sprintRepository.save(tache.getSprint());
        }
        tacheRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Tache updateTache(Long id, Tache updatedTache) {
        Tache existingTache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id)); // Using RuntimeException

        existingTache.setNom(updatedTache.getNom());
        existingTache.setDescription(updatedTache.getDescription());
        existingTache.setDateDebut(updatedTache.getDateDebut());
        existingTache.setDateFin(updatedTache.getDateFin());
        existingTache.setStatut(updatedTache.getStatut());
        existingTache.setStoryPoints(updatedTache.getStoryPoints());
        existingTache.setEstimatedHours(updatedTache.getEstimatedHours());
        existingTache.setLoggedHours(updatedTache.getLoggedHours());

        existingTache.setEtudiantsAffectes(updatedTache.getEtudiantsAffectes() != null ?
                updatedTache.getEtudiantsAffectes().stream()
                        .map(projetService::cleanEmail)
                        .collect(Collectors.toCollection(ArrayList::new)) : new ArrayList<>());

        if (updatedTache.getSprint() != null && updatedTache.getSprint().getIdSprint() != null) {
            if (existingTache.getSprint() == null || !existingTache.getSprint().getIdSprint().equals(updatedTache.getSprint().getIdSprint())) {
                Sprint newSprint = sprintRepository.findById(updatedTache.getSprint().getIdSprint())
                        .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + updatedTache.getSprint().getIdSprint()));
                existingTache.setSprint(newSprint);
                existingTache.setProjet(newSprint.getProjet());
                existingTache.setEtudiantsAffectes(newSprint.getEtudiantsAffectes() != null ?
                        newSprint.getEtudiantsAffectes().stream()
                                .map(projetService::cleanEmail)
                                .collect(Collectors.toCollection(ArrayList::new)) : new ArrayList<>());
            }
        } else {
            existingTache.setSprint(null);
            existingTache.setProjet(null);
            existingTache.setEtudiantsAffectes(new ArrayList<>());
        }

        validateTaskData(existingTache);
        validateTaskDatesWithSprint(existingTache);

        return tacheRepository.save(existingTache);
    }

    @Override
    @Transactional
    public Tache logTimeOnTask(Long taskId, Double hoursToLog) {
        if (hoursToLog == null || hoursToLog <= 0) {
            throw new IllegalArgumentException("Les heures à loguer doivent être positives."); // Using IllegalArgumentException
        }
        Tache tache = tacheRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + taskId)); // Using RuntimeException

        Double currentLoggedHours = tache.getLoggedHours() != null ? tache.getLoggedHours() : 0.0;
        tache.setLoggedHours(currentLoggedHours + hoursToLog);
        return tacheRepository.save(tache);
    }

    @Override
    public List<Tache> getTasksBySprintId(Long sprintId) {
        return tacheRepository.findBySprint_IdSprint(sprintId);
    }

    @Override
    public List<Tache> getTasksByAssignedToUserId(Long userId) {
        return new ArrayList<>();
    }

    @Override
    public List<Tache> getTasksByStatus(TaskStatus status) {
        return tacheRepository.findByStatut(status);
    }

    @Override
    public List<Tache> getTachesByStudentEmail(String studentEmail) {
        String cleanedEmail = projetService.cleanEmail(studentEmail);
        return tacheRepository.findByEtudiantsAffectesContainingIgnoreCase(cleanedEmail);
    }

    @Override
    @Transactional
    public Tache affecterEtudiantToTache(Long tacheId, String etudiantEmail) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + tacheId)); // Using RuntimeException

        String cleanedEmail = projetService.cleanEmail(etudiantEmail);

        if (tache.getEtudiantsAffectes() == null) {
            tache.setEtudiantsAffectes(new ArrayList<>());
        }

        if (!tache.getEtudiantsAffectes().contains(cleanedEmail)) {
            tache.getEtudiantsAffectes().add(cleanedEmail);
            return tacheRepository.save(tache);
        } else {
            return tache;
        }
    }

    @Override
    @Transactional
    public Tache supprimerEtudiantFromTache(Long tacheId, String etudiantEmail) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + tacheId)); // Using RuntimeException

        String cleanedEmail = projetService.cleanEmail(etudiantEmail);

        if (tache.getEtudiantsAffectes() != null && tache.getEtudiantsAffectes().remove(cleanedEmail)) {
            return tacheRepository.save(tache);
        } else {
            throw new IllegalArgumentException("L'étudiant " + cleanedEmail + " n'est pas affecté à la tâche " + tacheId + " ou liste vide."); // Using IllegalArgumentException
        }
    }

    private void validateTaskData(Tache tache) {
        if (tache.getNom() == null || tache.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide."); // Using IllegalArgumentException
        }
        if (tache.getDateDebut() == null || tache.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et fin ne peuvent pas être nulles."); // Using IllegalArgumentException
        }
        if (tache.getDateDebut().isAfter(tache.getDateFin())) {
            throw new IllegalArgumentException("La date de début ne peut pas être après la date de fin."); // Using IllegalArgumentException
        }
    }

    private void validateTaskDatesWithSprint(Tache tache) {
        if (tache.getSprint() != null) {
            Sprint sprint = tache.getSprint();
            if (sprint.getDateDebut() == null || sprint.getDateFin() == null) {
                return;
            }
            if (tache.getDateDebut().isBefore(sprint.getDateDebut()) || tache.getDateFin().isAfter(sprint.getDateFin())) {
                throw new IllegalArgumentException("Les dates de la tâche (" + tache.getDateDebut() + " - " + tache.getDateFin()
                        + ") doivent être comprises dans celles du sprint (" + sprint.getDateDebut() + " - " + sprint.getDateFin() + ")."); // Using IllegalArgumentException
            }
        }
    }
}