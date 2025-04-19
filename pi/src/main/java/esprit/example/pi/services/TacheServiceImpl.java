package esprit.example.pi.services;

import esprit.example.pi.entities.Tache;
import esprit.example.pi.repositories.TacheRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TacheServiceImpl implements ITacheService {

    private final TacheRepo tacheRepository;

    @Autowired
    public TacheServiceImpl(TacheRepo tacheRepository) {
        this.tacheRepository = tacheRepository;
    }

    @Override
    public Tache saveTache(Tache tache) {
        return tacheRepository.save(tache);
    }

    @Override
    public Tache getTacheById(Long id) {
        Optional<Tache> tache = tacheRepository.findById(id);
        return tache.orElse(null);
    }

    @Override
    public List<Tache> getAllTaches() {
        return tacheRepository.findAll();
    }

    @Override
    public void deleteTache(Long id) {
        tacheRepository.deleteById(id);
    }

    @Override
    public Tache updateTache(Long id, Tache tache) {
        if (tacheRepository.existsById(id)) {
            tache.setIdTache(id);
            return tacheRepository.save(tache);
        } else {
            return null;
        }
    }
}
