package com.internship.platform.Controllers;

import com.internship.platform.entities.Entreprise;
import com.internship.platform.services.EntrepriseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/entreprises")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    @PostMapping
    public Entreprise createEntreprise(@RequestBody Entreprise entreprise) {
        return entrepriseService.createEntreprise(entreprise);
    }

    @GetMapping
    public List<Entreprise> getAllEntreprises() {
        return entrepriseService.getAllEntreprises();
    }

    @GetMapping("/{id}")
    public Entreprise getEntrepriseById(@PathVariable Long id) {
        return entrepriseService.getEntrepriseById(id);
    }

    @PutMapping("/{id}")
    public Entreprise updateEntreprise(@PathVariable Long id, @RequestBody Entreprise entreprise) {
        return entrepriseService.updateEntreprise(id, entreprise);
    }

    @DeleteMapping("/{id}")
    public void deleteEntreprise(@PathVariable Long id) {
        entrepriseService.deleteEntreprise(id);
    }

    @PutMapping("/{id}/validate")
    //@PostMapping("/{id}/validate")
    //@CrossOrigin(origins = "http://localhost:4200", methods = RequestMethod.PUT)
    public Entreprise validateEntreprise(@PathVariable Long id) {
        return entrepriseService.validateEntreprise(id);
    }

    //@PostMapping("/{id}/refuse")
    @PutMapping("/{id}/refuse")
    public Entreprise refuseEntreprise(@PathVariable Long id) {
        return entrepriseService.refuseEntreprise(id);
    }
}
