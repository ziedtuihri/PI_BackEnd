package com.internship.platform.controllers;

import com.internship.platform.entities.Evenement;
import com.internship.platform.services.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;

    @PostMapping
    public Evenement createEvenement(@RequestBody Evenement evenement) {
        return evenementService.createEvenement(evenement);
    }

    @PutMapping("/{id}")
    public Evenement updateEvenement(@PathVariable Long id, @RequestBody Evenement evenement) {
        return evenementService.updateEvenement(id, evenement);
    }

    @DeleteMapping("/{id}")
    public void deleteEvenement(@PathVariable Long id) {
        evenementService.deleteEvenement(id);
    }

    @GetMapping("/{id}")
    public Evenement getEvenement(@PathVariable Long id) {
        return evenementService.getEvenement(id);
    }

    @GetMapping
    public List<Evenement> getAllEvenements() {
        return evenementService.getAllEvenements();
    }
}
