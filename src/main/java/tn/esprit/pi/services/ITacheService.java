package tn.esprit.pi.services;

import tn.esprit.pi.entities.Tache;

import java.util.List;

public interface ITacheService {
    Tache saveTache(Tache tache);
    Tache getTacheById(Long id);
    List<Tache> getAllTaches();
    void deleteTache(Long id);
    Tache updateTache(Long id, Tache tache);
}
