package tn.esprit.pi.controller;

import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.services.IProjetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final IProjetService projetService;

    @Autowired
    public ProjetController(IProjetService projetService) {
        this.projetService = projetService;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping
    public ResponseEntity<Projet> createProjet(@RequestBody Projet projet) {
        Projet savedProjet = projetService.saveProjet(projet);
        return new ResponseEntity<>(savedProjet, HttpStatus.CREATED);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getProjetById(@PathVariable Long id) {
        Projet projet = projetService.getProjetById(id);
        return projet != null ?
                new ResponseEntity<>(projet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping
    public ResponseEntity<List<Projet>> getAllProjets() {
        List<Projet> projets = projetService.getAllProjets();
        return new ResponseEntity<>(projets, HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/{id}")
    public ResponseEntity<Projet> updateProjet(@PathVariable Long id, @RequestBody Projet projet) {
        Projet updatedProjet = projetService.updateProjet(id, projet);
        return updatedProjet != null ?
                new ResponseEntity<>(updatedProjet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjet(@PathVariable Long id) {
        projetService.deleteProjet(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @Operation(summary = "Upload un fichier pour un projet",
            description = "Téléverse un fichier et l'associe à un projet existant",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier uploadé avec succès",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(type = "object", example = "{\"message\": \"Fichier téléchargé avec succès !\"}"))),
                    @ApiResponse(responseCode = "404", description = "Projet non trouvé"),
                    @ApiResponse(responseCode = "500", description = "Erreur lors de l'upload")
            })
    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @Parameter(description = "ID du projet", required = true)
            @PathVariable Long id,

            @Parameter(description = "Fichier à uploader",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        try {
            projetService.uploadFile(id, file);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Fichier téléchargé avec succès !");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de l'upload du fichier");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @Operation(summary = "Télécharger un fichier",
            description = "Récupère un fichier associé à un projet")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            Projet projet = projetService.getProjetById(id);
            if (projet != null && projet.getFilePath() != null) {
                Path path = Paths.get(projet.getFilePath());
                byte[] fileData = Files.readAllBytes(path);
                String fileName = path.getFileName().toString();
                String mimeType = Files.probeContentType(path);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream"));
                headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

                return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/{id}/etudiants")
    public ResponseEntity<Projet> ajouterEtudiantAuProjet(@PathVariable Long id, @RequestBody String nomEtudiant) {
        Projet updatedProjet = projetService.ajouterEtudiantAuProjet(id, nomEtudiant);
        return updatedProjet != null ?
                new ResponseEntity<>(updatedProjet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/{id}/etudiants/{nomEtudiant}")
    public ResponseEntity<Projet> supprimerEtudiantDuProjet(@PathVariable Long id, @PathVariable String nomEtudiant) {
        Projet updatedProjet = projetService.supprimerEtudiantDuProjet(id, nomEtudiant);
        return updatedProjet != null ?
                new ResponseEntity<>(updatedProjet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<String>> getEtudiantsDuProjet(@PathVariable Long id) {
        List<String> etudiants = projetService.getEtudiantsDuProjet(id);
        return etudiants != null ?
                new ResponseEntity<>(etudiants, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}