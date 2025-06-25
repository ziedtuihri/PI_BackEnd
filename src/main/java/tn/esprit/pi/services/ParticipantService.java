package tn.esprit.pi.services;

import tn.esprit.pi.entities.Participant;
import tn.esprit.pi.repositories.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public Participant ajouterParticipant(Participant p) {
        return participantRepository.save(p);
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    public void deleteParticipant(Long id) {
        Optional<Participant> participantOpt = participantRepository.findById(id);
        if (participantOpt.isPresent()) {
            participantRepository.deleteById(id);
        } else {
            throw new RuntimeException("Participant non trouvé avec l'ID: " + id);
        }
    }

    public Participant updateParticipant(Long id, Participant updatedParticipant) {
        Optional<Participant> participantOptional = participantRepository.findById(id);
        if (participantOptional.isPresent()) {
            Participant existingParticipant = participantOptional.get();

            if (updatedParticipant.getNom() != null) {
                existingParticipant.setNom(updatedParticipant.getNom());
            }
            if (updatedParticipant.getEmail() != null) {
                existingParticipant.setEmail(updatedParticipant.getEmail());
            }
            if (updatedParticipant.getUser() != null) {
                existingParticipant.setUser(updatedParticipant.getUser());
            }

            return participantRepository.save(existingParticipant);
        } else {
            throw new NoSuchElementException("Participant non trouvé avec ID : " + id);
        }
    }

    public Participant getParticipantById(Long id) {
        return participantRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Participant non trouvé avec ID : " + id));
    }
}
