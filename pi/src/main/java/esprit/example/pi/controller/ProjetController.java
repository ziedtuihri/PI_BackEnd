package esprit.example.pi.controller;

import esprit.example.pi.entities.Projet;
import esprit.example.pi.services.ProjetServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {
    @Autowired
    private ProjetServiceImpl projetService;

    //  Ajouter un projet avec une réponse HTTP appropriée
    @PostMapping("/add_projet")
    public Projet createProjet(@RequestBody Projet projet) {
        return projetService.saveProjet(projet);
    }
    @GetMapping("/{id}")
    public Projet getProjetById(@PathVariable Long id) {
        return projetService.getProjetById(id);
    }
}