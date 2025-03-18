package esprit.example.pi.services;

import esprit.example.pi.entities.Projet;

import java.util.List;

public interface IProjetService {
    Projet saveProjet(Projet projet);
    Projet getProjetById(Long id);
    List<Projet> getAllProjets();

}
