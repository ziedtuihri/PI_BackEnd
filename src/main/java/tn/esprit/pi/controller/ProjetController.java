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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException; // Pour des exceptions plus idiomatiques

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private static final Logger log = LoggerFactory.getLogger(ProjetController.class);

    private final IProjetService projetService;

    // Injection de dépendance via constructeur
    public ProjetController(IProjetService projetService) {
        this.projetService = projetService;
    }

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
    }
}