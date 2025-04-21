package com.internship.platform.services;

import com.internship.platform.entities.Entreprise;
import com.internship.platform.entities.Offre;
import com.internship.platform.repositories.EntrepriseRepository;
import com.internship.platform.repositories.OffreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OffreService {

    private final OffreRepository offreRepository;
    private final EntrepriseRepository entrepriseRepository;

    public OffreService(OffreRepository offreRepository, EntrepriseRepository entrepriseRepository) {
        this.offreRepository = offreRepository;
        this.entrepriseRepository = entrepriseRepository;
    }

    public Offre createOffre(Long entrepriseId, Offre offre) {
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + entrepriseId));
        offre.setEntreprise(entreprise);
        return offreRepository.save(offre);
    }

    public List<Offre> getAllOffres() {
        return offreRepository.findAll();
    }

    public List<Offre> getAllAvailableOffres() {
        return offreRepository.findByDisponibleTrue();
    }

    public Offre getOffreById(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre not found with id: " + id));
    }

    public Offre updateOffre(Long id, Offre updatedOffre) {
        Offre offre = getOffreById(id);

        offre.setTitre(updatedOffre.getTitre());
        offre.setDescription(updatedOffre.getDescription());
        offre.setCompetences(updatedOffre.getCompetences());
        offre.setLocalisation(updatedOffre.getLocalisation());
        offre.setDateDebut(updatedOffre.getDateDebut());
        offre.setDateFin(updatedOffre.getDateFin());
        offre.setDisponible(updatedOffre.isDisponible());

        return offreRepository.save(offre);
    }

    public void deleteOffre(Long id) {
        offreRepository.deleteById(id);
    }

    public Offre disableOffre(Long id) {
        Offre offre = getOffreById(id);
        offre.setDisponible(false);
        return offreRepository.save(offre);
    }
}
