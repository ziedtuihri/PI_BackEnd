package com.internship.platform.Controllers;

import com.internship.platform.entities.Encadrant;
import com.internship.platform.services.EncadrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encadrants")
@CrossOrigin(origins = "http://localhost:4200")
public class EncadrantController {

    @Autowired
    private EncadrantService encadrantService;

    @PostMapping
    public Encadrant createEncadrant(@RequestBody Encadrant encadrant) {
        return encadrantService.createEncadrant(encadrant);
    }

    @PutMapping("/{id}")
    public Encadrant updateEncadrant(@PathVariable Long id, @RequestBody Encadrant encadrant) {
        return encadrantService.updateEncadrant(id, encadrant);
    }

    @DeleteMapping("/{id}")
    public void deleteEncadrant(@PathVariable Long id) {
        encadrantService.deleteEncadrant(id);
    }

    @GetMapping("/{id}")
    public Encadrant getEncadrant(@PathVariable Long id) {
        return encadrantService.getEncadrant(id);
    }

    @GetMapping
    public List<Encadrant> getAllEncadrants() {
        return encadrantService.getAllEncadrants();
    }
}
