
package com.esprit.tn.pi.services;
import com.esprit.tn.pi.entities.*;
import com.esprit.tn.pi.interfaces.allInterfaces;
import com.esprit.tn.pi.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AllServices implements allInterfaces {

    ReunionRepository reunionRepository;
    UserRepository userRepository;
    SalleRepository salleRepository;
    ParticipantRepository participantRepository;
    ReservationSalleRepository reservationSalleRepository;


    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public Participant ajouterParticipant(Participant p) {
        return participantRepository.save(p);
    }

    @Override
    public User ajouterUser(User u) {
        return userRepository.save(u);
    }

    @Override
    public Salle ajouterSalle(Salle s) {
        return salleRepository.save(s);
    }

    @Override
    @Transactional
    public void deleteReunion(Long id) {
        if (reunionRepository.existsById(id)) {
            // Récupérer la réunion à supprimer
            Reunion reunion = reunionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Réunion non trouvée"));

            // Supprimer les réservations de salle associées à la réunion
            List<ReservationSalle> reservations = reservationSalleRepository.findByReunion(reunion);
            for (ReservationSalle reservation : reservations) {
                reservationSalleRepository.delete(reservation);
            }

            // Supprimer la réunion elle-même
            reunionRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("La réunion avec cet ID n'existe pas.");
        }
    }

    @Transactional
    public Reunion createReunion(Reunion reunion) {
        if (reunion.getCreateur() == null) {
            throw new IllegalArgumentException("Le créateur de la réunion ne peut pas être nul");
        }

        // Vérification si la réunion est de type "PRESENTIEL"
        if ("PRESENTIEL".equals(reunion.getType().name())) {
            if (reunion.getSalle() == null) {
                throw new IllegalArgumentException("La salle ne peut pas être nulle pour une réunion en présentiel");
            }

            boolean salleDisponible = verifierDisponibiliteSalle(reunion.getSalle(), reunion.getDate(), reunion.getHeure(), reunion.getDuree());
            if (!salleDisponible) {
                throw new IllegalArgumentException("La salle n'est pas disponible pour la date et l'heure spécifiées");
            }

            // Sauvegarde de la réunion avant la réservation
            Reunion savedReunion = reunionRepository.save(reunion);

            // Réserver la salle
            ReservationSalle reservation = new ReservationSalle();
            reservation.setReunion(savedReunion);
            reservation.setSalle(reunion.getSalle());
            reservationSalleRepository.save(reservation);

            // Ajout des participants si nécessaire
            if (reunion.getParticipants() != null && !reunion.getParticipants().isEmpty()) {
                for (Participant participant : reunion.getParticipants()) {
                    // Vérifier si le participant existe déjà, sinon l'ajouter à la réunion
                    if (!savedReunion.getParticipants().contains(participant)) {
                        savedReunion.getParticipants().add(participant);
                    }
                }
            }

            return savedReunion;
        }

        // Vérification si la réunion est de type "EN LIGNE"
        if ("EN_LIGNE".equals(reunion.getType().name())) {
            if (reunion.getLienZoom() == null || reunion.getLienZoom().isEmpty()) {
                throw new IllegalArgumentException("Le lien Zoom ne peut pas être nul pour une réunion en ligne");
            }

            // S'il y a des participants, envoyer un email
            if (reunion.getParticipants() != null && !reunion.getParticipants().isEmpty()) {
                envoyerEmailAuxParticipants(reunion);
            }
        }

        // Sauvegarde de la réunion après toute la logique de réservation
        return reunionRepository.save(reunion);
    }


    @Transactional
    public Reunion updateReunion(Long reunionId, Reunion updatedReunion) {
        // Récupérer la réunion existante
        Reunion existingReunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Réunion non trouvée avec l'ID : " + reunionId));

        // Mettre à jour les propriétés de la réunion existante avec les nouvelles valeurs
        existingReunion.setTitre(updatedReunion.getTitre());
        existingReunion.setDate(updatedReunion.getDate());
        existingReunion.setHeure(updatedReunion.getHeure());
        existingReunion.setDuree(updatedReunion.getDuree());
        existingReunion.setType(updatedReunion.getType());
        existingReunion.setDescription(updatedReunion.getDescription());
        existingReunion.setLienZoom(updatedReunion.getLienZoom());

        // Mise à jour du créateur (on suppose que le créateur ne change pas, mais si c'est le cas, à vous d'ajouter une logique)
        if (updatedReunion.getCreateur() != null) {
            existingReunion.setCreateur(updatedReunion.getCreateur());
        }

        // Traitement pour le type "PRESENTIEL"
        if ("PRESENTIEL".equals(existingReunion.getType().name())) {
            // Vérification de la salle pour une réunion en présentiel
            if (updatedReunion.getSalle() == null) {
                throw new IllegalArgumentException("La salle ne peut pas être nulle pour une réunion en présentiel");
            }

            // Vérification de la disponibilité de la salle pour la réunion modifiée
            boolean salleDisponible = verifierDisponibiliteSalle(updatedReunion.getSalle(), updatedReunion.getDate(), updatedReunion.getHeure(), updatedReunion.getDuree());
            if (!salleDisponible) {
                throw new IllegalArgumentException("La salle n'est pas disponible pour la date et l'heure spécifiées");
            }

            // Mise à jour de la salle
            existingReunion.setSalle(updatedReunion.getSalle());

            // Si la réunion est modifiée en présentiel, supprimer les participants (car ils ne sont pas nécessaires pour ce type)
            existingReunion.setParticipants(null);

            // Mettre à jour la réservation de la salle
            Optional<ReservationSalle> existingReservation = reservationSalleRepository.findByReunionId(reunionId);
            if (existingReservation.isPresent()) {
                ReservationSalle reservation = existingReservation.get();
                reservation.setSalle(updatedReunion.getSalle());
                reservationSalleRepository.save(reservation);
            } else {
                // Si la réservation n'existe pas, créer une nouvelle réservation
                ReservationSalle newReservation = new ReservationSalle();
                newReservation.setReunion(existingReunion);
                newReservation.setSalle(updatedReunion.getSalle());
                reservationSalleRepository.save(newReservation);
            }

        }

        // Traitement pour le type "EN LIGNE"
        if ("EN_LIGNE".equals(existingReunion.getType().name())) {
            // Vérification du lien Zoom pour une réunion en ligne
            if (updatedReunion.getLienZoom() == null || updatedReunion.getLienZoom().isEmpty()) {
                throw new IllegalArgumentException("Le lien Zoom ne peut pas être nul pour une réunion en ligne");
            }

            // Mise à jour du lien Zoom
            existingReunion.setLienZoom(updatedReunion.getLienZoom());

            // Si des participants sont fournis, mettre à jour et envoyer des invitations
            if (updatedReunion.getParticipants() != null && !updatedReunion.getParticipants().isEmpty()) {
                // Mettre à jour les participants
                existingReunion.setParticipants(updatedReunion.getParticipants());

                // Envoi des emails de mise à jour aux participants
                envoyerEmailAuxParticipants(existingReunion);
            }
        }

        // Sauvegarde de la réunion après modification
        return reunionRepository.save(existingReunion);
    }


    private boolean verifierDisponibiliteSalle(Salle salle, String date, String heure, String duree) {
        try {
            // Convertir l'heure de début et la durée en objets LocalDateTime
            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + heure);

            // Extraire la durée numérique de la chaîne et la convertir en minutes
            String dureeNum = duree.replaceAll("[^0-9]", ""); // Retirer tout ce qui n'est pas un chiffre
            Long dureeMinutes = Long.parseLong(dureeNum); // Convertir la durée en minutes

            // Calculer l'heure de fin
            LocalDateTime endDateTime = startDateTime.plusMinutes(dureeMinutes);

            // Rechercher les réunions existantes qui utilisent la même salle et qui se chevauchent
            List<Reunion> reunionsExistantes = reunionRepository.findBySalleIdAndDate(salle.getId(), date);

            for (Reunion reunionExistante : reunionsExistantes) {
                LocalDateTime existantStartDateTime = LocalDateTime.parse(reunionExistante.getDate() + "T" + reunionExistante.getHeure());
                // Extraire et convertir la durée de la réunion existante
                String dureeExistante = reunionExistante.getDuree();
                String dureeExistanteNum = dureeExistante.replaceAll("[^0-9]", ""); // Retirer tout ce qui n'est pas un chiffre
                Long dureeExistanteMinutes = Long.parseLong(dureeExistanteNum); // Convertir la durée en minutes

                LocalDateTime existantEndDateTime = existantStartDateTime.plusMinutes(dureeExistanteMinutes);

                // Vérifier si les plages horaires se chevauchent
                if ((startDateTime.isBefore(existantEndDateTime) && endDateTime.isAfter(existantStartDateTime))) {
                    return false; // Si il y a chevauchement, la salle n'est pas disponible
                }
            }

            return true; // La salle est disponible
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur lors de la vérification de la disponibilité de la salle", e);
        }
    }

    private void envoyerEmailAuxParticipants(Reunion reunion) {
        for (Participant participant : reunion.getParticipants()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(participant.getEmail());
            message.setSubject("Invitation à la réunion : " + reunion.getTitre());
            message.setText(
                    "Bonjour " + participant.getNom() + ",\n\n" +
                            "Vous êtes invité à une réunion.\n\n" +
                            "📅 Date : " + reunion.getDate() + "\n" +
                            "⏰ Heure : " + reunion.getHeure() + "\n" +
                            "⏳ Durée : " + reunion.getDuree() + "\n" +
                            "📌 Lieu : " + (reunion.getSalle() != null ? reunion.getSalle().getNom() : "En ligne") + "\n" +
                            (reunion.getLienZoom() != null ? "🔗 Lien Zoom : " + reunion.getLienZoom() + "\n" : "") +
                            "\nDescription : " + reunion.getDescription() + "\n\n" +
                            "Merci de votre présence !"
            );
            javaMailSender.send(message);
        }
    }


    public List<EvenementDTO> getEvenements() {
        List<Reunion> reunions = reunionRepository.findAll();

        return reunions.stream().map(reunion -> {
            String start = reunion.getDate() + "T" + reunion.getHeure();
            LocalDateTime startDateTime = LocalDateTime.parse(start);

            // Extraction sécurisée de la durée
            long dureeMinutes = 0;
            try {
                String dureeStr = reunion.getDuree().replaceAll("[^\\d]", ""); // Supprime tout sauf les chiffres
                dureeMinutes = Long.parseLong(dureeStr);
            } catch (NumberFormatException e) {
                // Gérer l'erreur ici (log, valeur par défaut, etc.)
                dureeMinutes = 0;
            }

            LocalDateTime endDateTime = startDateTime.plusMinutes(dureeMinutes);

            return new EvenementDTO(
                    reunion.getId(),
                    reunion.getTitre(),
                    start,
                    endDateTime.toString(),
                    reunion.getType().name()
            );
        }).collect(Collectors.toList());
    }


    @Override
    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<Salle> getAllSalle() {
        return salleRepository.findAll();
    }

    @Override
    public List<Reunion> getAllReunions() {
        return reunionRepository.findAll();
    }

}


