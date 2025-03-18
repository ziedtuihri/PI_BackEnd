package esprit.example.pi.services;

import esprit.example.pi.entities.Projet;
import esprit.example.pi.repositories.ProjetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProjetServiceImpl implements IProjetService{

    @Autowired
    private ProjetRepo projetRepo; // Injection du repository

    @Override
    public Projet saveProjet(Projet projet) {
        return projetRepo.save(projet);
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
