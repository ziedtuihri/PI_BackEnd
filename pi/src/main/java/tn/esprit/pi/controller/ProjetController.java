<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/ProjetController.java
package tn.esprit.pi.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.dto.EmailDTO;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.services.IProjetService;
import tn.esprit.pi.services.ProjetServiceImpl; // Import pour les exceptions personnalisées
=======
package esprit.example.pi.controller;

import esprit.example.pi.entities.Projet;
import esprit.example.pi.services.IProjetService;
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
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/ProjetController.java

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/ProjetController.java
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException; // Pour des exceptions plus idiomatiques
=======
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/ProjetController.java

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/ProjetController.java
    private static final Logger log = LoggerFactory.getLogger(ProjetController.class);

    private final IProjetService projetService;

    // Injection de dépendance via constructeur
=======
    private final IProjetService projetService;

    @Autowired
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/ProjetController.java
    public ProjetController(IProjetService projetService) {
        this.projetService = projetService;
    }

<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/controller/ProjetController.java
    /**
     * Crée un nouveau projet.
     * Gère les validations automatiques grâce à @Valid.
     */
    @PostMapping
    public ResponseEntity<Projet> createProjet(@Valid @RequestBody Projet projet) {
        try {
            Projet created = projetService.saveProjet(projet);
            log.info("Projet créé avec succès, ID : {}", created.getIdProjet());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation lors de la création du projet : {}", e.getMessage());
            // Retourne un 400 Bad Request pour les problèmes de validation (ex: dates incohérentes)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création du projet : {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création du projet.");
        }
    }

    /**
     * Récupère un projet par son ID.
     * Utilise les exceptions personnalisées du service.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getProjetById(@PathVariable Long id) {
        try {
            Projet projet = projetService.getProjetById(id);
            log.debug("Projet récupéré avec succès, ID : {}", id);
            // ResponseEntity.ok() est préférable à ResponseEntity.of(Optional.ofNullable()) si le service lance une exception
            return ResponseEntity.ok(projet);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé avec l'ID : {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération du projet ID {} : {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération du projet.");
        }
    }

    /**
     * Récupère tous les projets.
     */
    @GetMapping
    public ResponseEntity<List<Projet>> getAllProjets() {
        log.debug("Récupération de tous les projets.");
        List<Projet> projets = projetService.getAllProjets();
        return ResponseEntity.ok(projets);
    }

    /**
     * Met à jour un projet existant.
     * Gère les validations et les exceptions du service.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Projet> updateProjet(@PathVariable Long id, @Valid @RequestBody Projet projet) {
        try {
            Projet updated = projetService.updateProjet(id, projet);
            log.info("Projet ID {} mis à jour avec succès.", id);
            return ResponseEntity.ok(updated); // 200 OK
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Tentative de mise à jour d'un projet non trouvé, ID : {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation lors de la mise à jour du projet ID {} : {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour du projet ID {} : {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la mise à jour du projet.");
        }
    }

    /**
     * Supprime un projet par son ID.
     * Utilise les exceptions personnalisées du service.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjet(@PathVariable Long id) {
        try {
            projetService.deleteProjet(id);
            log.info("Projet ID {} supprimé avec succès.", id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Tentative de suppression d'un projet non trouvé, ID : {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression du projet ID {} : {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la suppression du projet.");
        }
    }

    /**
     * Gère l'upload d'un fichier pour un projet.
     */
    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Utiliser MediaType constant
    public ResponseEntity<Map<String, String>> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            projetService.uploadFile(id, file);
            log.info("Fichier téléchargé avec succès pour le projet ID : {}", id);
            return ResponseEntity.ok(Map.of("message", "Fichier téléchargé avec succès !"));
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé pour le téléchargement de fichier, ID : {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation du fichier pour le projet ID {} : {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            log.error("Erreur d'E/S lors du téléchargement du fichier pour le projet ID {} : {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors du téléchargement du fichier : " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du téléchargement du fichier pour le projet ID {} : {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur inattendue lors du téléchargement du fichier.");
        }
    }

    /**
     * Permet le téléchargement d'un fichier associé à un projet.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            byte[] data = projetService.downloadFile(id); // Le service gère déjà l'existence et la lisibilité du fichier
            Projet projet = projetService.getProjetById(id); // Récupérer le projet pour le chemin du fichier

            // Construire le chemin à partir de la propriété filePath du projet
            Path path = Path.of(projet.getFilePath());
            String fileName = path.getFileName().toString();
            String mimeType = Files.probeContentType(path); // Tente de déterminer le type MIME

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(data);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé pour le téléchargement de fichier, ID : {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ProjetServiceImpl.FileNotFoundException e) {
            log.warn("Fichier non trouvé pour le projet ID {} : {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            log.error("Erreur d'E/S lors du téléchargement du fichier pour le projet ID {} : {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors du téléchargement du fichier : " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du téléchargement du fichier pour le projet ID {} : {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur inattendue lors du téléchargement du fichier.");
        }
    }

    // --- Gestion des emails via EmailDTO ---

    /**
     * Ajoute un email d'étudiant à un projet.
     */
    @PostMapping("/{projetId}/add-student-email")
    public ResponseEntity<Projet> addStudentEmailToProjet(
            @PathVariable Long projetId,
            @Valid @RequestBody EmailDTO emailDTO) {
        try {
            Projet projet = projetService.addStudentEmailToProjet(projetId, emailDTO.getEmail());
            log.info("Email étudiant {} ajouté au projet ID {}.", emailDTO.getEmail(), projetId);
            return ResponseEntity.ok(projet);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé pour l'ajout d'email étudiant, ID : {}", projetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'ajout de l'email {} au projet ID {} : {}", emailDTO.getEmail(), projetId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'ajout de l'email étudiant.");
        }
    }

    /**
     * Retire un email d'étudiant d'un projet.
     */
    @DeleteMapping("/{projetId}/remove-student-email")
    public ResponseEntity<Projet> removeStudentEmailFromProjet(
            @PathVariable Long projetId,
            @Valid @RequestBody EmailDTO emailDTO) {
        try {
            Projet projet = projetService.removeStudentEmailFromProjet(projetId, emailDTO.getEmail());
            log.info("Email étudiant {} retiré du projet ID {}.", emailDTO.getEmail(), projetId);
            return ResponseEntity.ok(projet);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé pour le retrait d'email étudiant, ID : {}", projetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du retrait de l'email {} du projet ID {} : {}", emailDTO.getEmail(), projetId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors du retrait de l'email étudiant.");
        }
    }

    /**
     * Assigne un email d'enseignant à un projet.
     */
    @PutMapping("/{projetId}/assign-teacher-email")
    public ResponseEntity<Projet> assignTeacherEmailToProjet(
            @PathVariable Long projetId,
            @Valid @RequestBody EmailDTO emailDTO) {
        try {
            Projet projet = projetService.assignTeacherEmailToProjet(projetId, emailDTO.getEmail());
            log.info("Email enseignant {} assigné au projet ID {}.", emailDTO.getEmail(), projetId);
            return ResponseEntity.ok(projet);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé pour l'assignation d'encadrant, ID : {}", projetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'assignation de l'email enseignant {} au projet ID {} : {}", emailDTO.getEmail(), projetId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'assignation de l'email enseignant.");
        }
    }

    /**
     * Récupère la liste des emails étudiants d'un projet.
     */
    @GetMapping("/{projetId}/student-emails")
    public ResponseEntity<List<String>> getStudentEmailsForProjet(@PathVariable Long projetId) {
        try {
            List<String> emails = projetService.getStudentEmailsForProjet(projetId);
            log.debug("Récupération des emails étudiants pour le projet ID {}.", projetId);
            return ResponseEntity.ok(emails);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé pour la récupération des emails étudiants, ID : {}", projetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des emails étudiants pour le projet ID {} : {}", projetId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération des emails étudiants.");
        }
    }

    /**
     * Remplace la liste complète des emails étudiants d'un projet.
     */
    @PostMapping("/{projetId}/set-student-emails")
    public ResponseEntity<Projet> setStudentEmailsToProjet(
            @PathVariable Long projetId,
            @RequestBody List<String> studentEmails) { // Pas besoin de @Valid ici pour une simple List<String>
        try {
            Projet projet = projetService.setStudentEmailsToProjet(projetId, studentEmails);
            log.info("Liste d'emails étudiants mise à jour pour le projet ID {}.", projetId);
            return ResponseEntity.ok(projet);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Projet non trouvé pour la mise à jour de la liste d'emails étudiants, ID : {}", projetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour de la liste d'emails étudiants pour le projet ID {} : {}", projetId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la mise à jour de la liste d'emails étudiants.");
        }
    }

    /**
     * Récupère les projets affectés à un étudiant (basé sur son email).
     * NOTE : `Principal principal` est lié à Spring Security.
     * Sans Spring Security, cette méthode ne fonctionnera pas comme prévu.
     * Si tu veux l'utiliser sans Spring Security, l'email de l'étudiant
     * devrait être passé comme @PathVariable ou @RequestParam.
     */
    @GetMapping("/mes-projets/{studentEmail}") // Modifier le chemin pour inclure l'email de l'étudiant
    public ResponseEntity<List<Projet>> getMesProjets(@PathVariable String studentEmail) {
        // En l'absence de Spring Security et Principal, on passe l'email directement
        log.debug("Récupération des projets affectés à l'étudiant : {}", studentEmail);
        List<Projet> projets = projetService.getProjetsAffectesParEtudiant(studentEmail);
        return ResponseEntity.ok(projets);
=======
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
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/controller/ProjetController.java
    }
}