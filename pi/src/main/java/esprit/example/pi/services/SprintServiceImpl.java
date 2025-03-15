package esprit.example.pi.services;

import esprit.example.pi.entities.Sprint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SprintServiceImpl implements ISprintService{
    @Override
    public List<Sprint> retrieveAllSprints() {
        return List.of();
    }

    @Override
    public Sprint saveSprint(Sprint sprint) {
        return null;
    }

    @Override
    public Sprint getSprintById(Long id) {
        return null;
    }

    @Override
    public List<Sprint> getAllSprints() {
        return List.of();
    }
}
