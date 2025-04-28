package com.internship.platform.Controllers;

import com.internship.platform.entities.Offre;
import com.internship.platform.services.OffreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@CrossOrigin(origins = "http://localhost:4200")
public class OffreController {

    private final OffreService offreService;

    public OffreController(OffreService offreService) {
        this.offreService = offreService;
    }

    @PostMapping("/entreprise/{entrepriseId}")
    public Offre createOffre(@PathVariable Long entrepriseId, @RequestBody Offre offre) {
        return offreService.createOffre(entrepriseId, offre);
    }

    @GetMapping
    public List<Offre> getAllOffres() {
        return offreService.getAllOffres();
    }

    @GetMapping("/disponibles")
    public List<Offre> getAllAvailableOffres() {
        return offreService.getAllAvailableOffres();
    }

    @GetMapping("/{id}")
    public Offre getOffreById(@PathVariable Long id) {
        return offreService.getOffreById(id);
    }

    @PutMapping("/{id}")
    public Offre updateOffre(@PathVariable Long id, @RequestBody Offre offre) {
        return offreService.updateOffre(id, offre);
    }

    @DeleteMapping("/{id}")
    public void deleteOffre(@PathVariable Long id) {
        offreService.deleteOffre(id);
    }

    @PostMapping("/{id}/disable")
    public Offre disableOffre(@PathVariable Long id) {
        return offreService.disableOffre(id);
    }
}
