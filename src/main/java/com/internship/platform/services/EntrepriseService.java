package com.internship.platform.services;

import com.internship.platform.entities.Entreprise;
import com.internship.platform.entities.StatutEntreprise;
import com.internship.platform.repositories.EntrepriseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;

    public EntrepriseService(EntrepriseRepository entrepriseRepository) {
        this.entrepriseRepository = entrepriseRepository;
    }

    public Entreprise createEntreprise(Entreprise entreprise) {
        entreprise.setStatut(StatutEntreprise.EN_ATTENTE);
        return entrepriseRepository.save(entreprise);
    }

    public List<Entreprise> getAllEntreprises() {
        return entrepriseRepository.findAll();
    }

    public Entreprise getEntrepriseById(Long id) {
        return entrepriseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + id));
    }

    public Entreprise updateEntreprise(Long id, Entreprise entrepriseDetails) {
        Entreprise entreprise = getEntrepriseById(id);

        entreprise.setNom(entrepriseDetails.getNom());
        entreprise.setSecteurActivite(entrepriseDetails.getSecteurActivite());
        entreprise.setTaille(entrepriseDetails.getTaille());
        entreprise.setSiteWeb(entrepriseDetails.getSiteWeb());
        entreprise.setAdresse(entrepriseDetails.getAdresse());
        entreprise.setEmail(entrepriseDetails.getEmail());
        entreprise.setTelephone(entrepriseDetails.getTelephone());
        entreprise.setContactRH(entrepriseDetails.getContactRH());

        return entrepriseRepository.save(entreprise);
    }

    public void deleteEntreprise(Long id) {
        entrepriseRepository.deleteById(id);
    }

    public Entreprise validateEntreprise(Long id) {
        Entreprise entreprise = getEntrepriseById(id);
        entreprise.setStatut(StatutEntreprise.VALIDE);
        return entrepriseRepository.save(entreprise);
    }

    public Entreprise refuseEntreprise(Long id) {
        Entreprise entreprise = getEntrepriseById(id);
        entreprise.setStatut(StatutEntreprise.REFUSE);
        return entrepriseRepository.save(entreprise);
    }
}
