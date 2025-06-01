package com.internship.platform.services;

import com.internship.platform.entities.Encadrant;
import com.internship.platform.repositories.EncadrantRepository;
import com.internship.platform.repositories.EntrepriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EncadrantService {

    @Autowired
    private EncadrantRepository encadrantRepository;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    public Encadrant createEncadrant(Encadrant encadrant) {
        if (encadrant.getEntreprise() != null && encadrant.getEntreprise().getId() != null) {
            Long entrepriseId = encadrant.getEntreprise().getId();
            entrepriseRepository.findById(entrepriseId).ifPresent(encadrant::setEntreprise);
        } else {
            encadrant.setEntreprise(null); // Or throw error if entreprise is required
        }

        return encadrantRepository.save(encadrant);
    }

    public Encadrant createEncadrantForEntreprise(Long entrepriseId, Encadrant encadrant) {
        return entrepriseRepository.findById(entrepriseId).map(entreprise -> {
            encadrant.setEntreprise(entreprise);
            return encadrantRepository.save(encadrant);
        }).orElseThrow(() -> new RuntimeException("Entreprise not found with ID: " + entrepriseId));
    }



    public Encadrant updateEncadrant(Long id, Encadrant updatedEncadrant) {
        Encadrant encadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant not found"));

        encadrant.setNom(updatedEncadrant.getNom());
        encadrant.setPrenom(updatedEncadrant.getPrenom());
        encadrant.setEmail(updatedEncadrant.getEmail());
        encadrant.setTelephone(updatedEncadrant.getTelephone());
        encadrant.setSpecialite(updatedEncadrant.getSpecialite());

        if (updatedEncadrant.getEntreprise() != null && updatedEncadrant.getEntreprise().getId() != null) {
            Long entrepriseId = updatedEncadrant.getEntreprise().getId();
            entrepriseRepository.findById(entrepriseId).ifPresent(encadrant::setEntreprise);
        }

        return encadrantRepository.save(encadrant);
    }


    public void deleteEncadrant(Long id) {
        encadrantRepository.deleteById(id);
    }

    public Encadrant getEncadrant(Long id) {
        return encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant not found"));
    }

    public List<Encadrant> getEncadrantsByEntrepriseId(Long entrepriseId) {
        return encadrantRepository.findByEntrepriseId(entrepriseId);
    }

    public List<Encadrant> getAllEncadrants() {
        return encadrantRepository.findAll();
    }
}

