package com.esprit.tn.pi.controllers;

import com.esprit.tn.pi.entities.Participant;
import com.esprit.tn.pi.services.ParticipantService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@CrossOrigin(origins = "*")
@RequestMapping("/pi/participants")
public class ParticipantController {
    private final ParticipantService participantService;

    @PostMapping
    public ResponseEntity<Participant> ajouterParticipant(@RequestBody Participant participant) {
        Participant createdParticipant = participantService.ajouterParticipant(participant);
        return new ResponseEntity<>(createdParticipant, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Participant>> getAllParticipants() {
        return new ResponseEntity<>(participantService.getAllParticipants(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteParticipant(@PathVariable Long id) {
        Participant participant = participantService.getParticipantById(id);
        if (participant == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Participant non trouvé");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Vérifier si le participant est réservé à une réunion
        if (participant.getReunions() != null && !participant.getReunions().isEmpty()) {
            Map<String, String> reservedResponse = new HashMap<>();
            reservedResponse.put("message", "Ce participant est réservé à une réunion");
            return new ResponseEntity<>(reservedResponse, HttpStatus.FORBIDDEN);
        }

        participantService.deleteParticipant(id);
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Participant supprimé avec succès");
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Participant> updateParticipant(@PathVariable Long id, @RequestBody Participant updatedParticipant) {
        Participant participant = participantService.updateParticipant(id, updatedParticipant);
        return new ResponseEntity<>(participant, HttpStatus.OK);
    }
}
