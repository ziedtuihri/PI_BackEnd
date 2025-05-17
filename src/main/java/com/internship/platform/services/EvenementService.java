package com.internship.platform.services;

import com.internship.platform.entities.Evenement;
import com.internship.platform.repositories.EvenementRepository;
import com.internship.platform.repositories.EntrepriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    public Evenement createEvenement(Evenement evenement) {
        return evenementRepository.save(evenement);
    }

    @Autowired
    private EntrepriseRepository entrepriseRepository; // <-- you need this!

    public Evenement updateEvenement(Long id, Evenement updatedEvenement) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evenement not found"));

        evenement.setTitre(updatedEvenement.getTitre());
        evenement.setDescription(updatedEvenement.getDescription());
        evenement.setDate(updatedEvenement.getDate());
        evenement.setLieu(updatedEvenement.getLieu());

        // FIX: Update entreprise
        if (updatedEvenement.getEntreprise() != null && updatedEvenement.getEntreprise().getId() != null) {
            evenement.setEntreprise(
                    entrepriseRepository.findById(updatedEvenement.getEntreprise().getId())
                            .orElseThrow(() -> new RuntimeException("Entreprise not found"))
            );
        }

        return evenementRepository.save(evenement);
    }


    public void deleteEvenement(Long id) {
        evenementRepository.deleteById(id);
    }

    public Evenement getEvenement(Long id) {
        return evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evenement not found"));
    }

    public List<Evenement> getAllEvenements() {
        return evenementRepository.findAll();
    }
}
