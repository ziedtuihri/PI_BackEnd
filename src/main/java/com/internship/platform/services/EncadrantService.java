package com.internship.platform.services;

import com.internship.platform.entities.Encadrant;
import com.internship.platform.repositories.EncadrantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EncadrantService {

    @Autowired
    private EncadrantRepository encadrantRepository;

    public Encadrant createEncadrant(Encadrant encadrant) {
        return encadrantRepository.save(encadrant);
    }

    public Encadrant updateEncadrant(Long id, Encadrant updatedEncadrant) {
        Encadrant encadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant not found"));
        encadrant.setNom(updatedEncadrant.getNom());
        encadrant.setPrenom(updatedEncadrant.getPrenom());
        encadrant.setEmail(updatedEncadrant.getEmail());
        encadrant.setTelephone(updatedEncadrant.getTelephone());
        encadrant.setSpecialite(updatedEncadrant.getSpecialite());
        return encadrantRepository.save(encadrant);
    }

    public void deleteEncadrant(Long id) {
        encadrantRepository.deleteById(id);
    }

    public Encadrant getEncadrant(Long id) {
        return encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant not found"));
    }

    public List<Encadrant> getAllEncadrants() {
        return encadrantRepository.findAll();
    }
}
