package esprit.example.pi.services;

import esprit.example.pi.entities.Projet;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProjetServiceImpl implements IProjetService{
    @Override
    public Projet saveProjet(Projet projet) {
        return null;
    }

    @Override
    public Projet getProjetById(Long id) {
        return null;
    }

    @Override
    public List<Projet> getAllProjets() {
        return List.of();
    }
}
