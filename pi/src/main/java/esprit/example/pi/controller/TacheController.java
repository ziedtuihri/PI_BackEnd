package esprit.example.pi.controller;

import esprit.example.pi.entities.Tache;
import esprit.example.pi.services.ITacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/taches")
public class TacheController {

    private final ITacheService tacheService;

    @Autowired
    public TacheController(ITacheService tacheService) {
        this.tacheService = tacheService;
    }


    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping
    public ResponseEntity<Tache> createTache(@RequestBody Tache tache) {
        Tache savedTache = tacheService.saveTache(tache);
        return new ResponseEntity<>(savedTache, HttpStatus.CREATED); // 201 Created
    }


    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}")
    public ResponseEntity<Tache> getTacheById(@PathVariable Long id) {
        Tache tache = tacheService.getTacheById(id);
        if (tache != null) {
            return new ResponseEntity<>(tache, HttpStatus.OK); // 200 OK
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }


    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping
    public ResponseEntity<List<Tache>> getAllTaches() {
        List<Tache> taches = tacheService.getAllTaches();
        return new ResponseEntity<>(taches, HttpStatus.OK); // 200 OK
    }


    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/{id}")
    public ResponseEntity<Tache> updateTache(@PathVariable Long id, @RequestBody Tache tache) {
        Tache updatedTache = tacheService.updateTache(id, tache);
        if (updatedTache != null) {
            return new ResponseEntity<>(updatedTache, HttpStatus.OK); // 200 OK
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }


    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        tacheService.deleteTache(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }
}
