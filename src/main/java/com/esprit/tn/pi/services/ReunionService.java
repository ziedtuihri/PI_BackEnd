package com.esprit.tn.pi.services;

import com.esprit.tn.pi.entities.*;
import com.esprit.tn.pi.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReunionService {
    @Autowired
    private SalleRepository salleRepository;
    @Autowired
    ReservationSalleRepository reservationSalleRepository;
    @Autowired
    private final ReunionRepository reunionRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    public List<Reunion> getAllReunions() {
        return reunionRepository.findAll();
    }

    public Reunion getReunionById(Long id) {
        return reunionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Réunion non trouvée avec l'ID : " + id));
    }


    @Transactional
    public void deleteReunion(Long id) {
        if (reunionRepository.existsById(id)) {
            // Récupérer la réunion à supprimer
            Reunion reunion = reunionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Réunion non trouvée"));

            // Supprimer les réservations de salle associées à la réunion
            List<ReservationSalle> reservations = reservationSalleRepository.findByReunion(reunion);
            reservationSalleRepository.deleteAll(reservations);

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

        if ("PRESENTIEL".equals(reunion.getType().name())) {
            if (reunion.getSalle() == null) {
                throw new IllegalArgumentException("La salle ne peut pas être nulle pour une réunion en présentiel");
            }

            boolean salleDisponible = verifierDisponibiliteSalle(reunion.getSalle(), reunion.getDate(), reunion.getHeure(), reunion.getDuree(), null);
            if (!salleDisponible) {
                throw new IllegalArgumentException("La salle n'est pas disponible pour la date et l'heure spécifiées");
            }

            // Marquer la salle comme non disponible uniquement si elle est déjà persistée
            if (reunion.getSalle().getId() != null) {
                Salle salleExistante = salleRepository.findById(reunion.getSalle().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));
                salleExistante.setDisponible(false);
                salleRepository.save(salleExistante);
                reunion.setSalle(salleExistante); // on rattache la salle mise à jour à la réunion
            } else {
                reunion.getSalle().setDisponible(false); // cas rare : nouvelle salle
            }

            Reunion savedReunion = reunionRepository.save(reunion);

            ReservationSalle reservation = new ReservationSalle();
            reservation.setReunion(savedReunion);
            reservation.setSalle(reunion.getSalle());
            reservationSalleRepository.save(reservation);

            if (reunion.getParticipants() != null && !reunion.getParticipants().isEmpty()) {
                for (Participant participant : reunion.getParticipants()) {
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

        return reunionRepository.save(reunion);
    }


    @Transactional
    public Reunion updateReunion(Long reunionId, Reunion updatedReunion) {
        // Récupérer la réunion existante
        Reunion existingReunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Réunion non trouvée avec l'ID : " + reunionId));

        // Mettre à jour les propriétés de la réunion existante
        existingReunion.setTitre(updatedReunion.getTitre());
        existingReunion.setDate(updatedReunion.getDate());
        existingReunion.setHeure(updatedReunion.getHeure());
        existingReunion.setDuree(updatedReunion.getDuree());
        existingReunion.setType(updatedReunion.getType());
        existingReunion.setDescription(updatedReunion.getDescription());
        existingReunion.setLienZoom(updatedReunion.getLienZoom());

        // Mise à jour du créateur (on suppose que le créateur ne change pas)
        if (updatedReunion.getCreateur() != null) {
            existingReunion.setCreateur(updatedReunion.getCreateur());
        }

        // Traitement pour le type "PRESENTIEL"
        if ("PRESENTIEL".equals(existingReunion.getType().name())) {
            if (updatedReunion.getSalle() == null) {
                throw new IllegalArgumentException("La salle ne peut pas être nulle pour une réunion en présentiel");
            }

            boolean salleDisponible = verifierDisponibiliteSalle(
                    updatedReunion.getSalle(), updatedReunion.getDate(),
                    updatedReunion.getHeure(), updatedReunion.getDuree(), reunionId
            );

            if (!salleDisponible) {
                throw new IllegalArgumentException("La salle n'est pas disponible pour la date et l'heure spécifiées");
            }

            // Marquer la salle comme non disponible si elle est déjà persistée
            if (updatedReunion.getSalle().getId() != null) {
                Salle salleExistante = salleRepository.findById(updatedReunion.getSalle().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));
                salleExistante.setDisponible(false);
                salleRepository.save(salleExistante);
                existingReunion.setSalle(salleExistante);
            } else {
                updatedReunion.getSalle().setDisponible(false);
                existingReunion.setSalle(updatedReunion.getSalle());
            }

            // Si la réunion est modifiée en présentiel, supprimer les participants existants
            existingReunion.setParticipants(null);

            // Mise à jour ou création de la réservation de salle
            Optional<ReservationSalle> existingReservation = reservationSalleRepository.findByReunionId(reunionId);
            if (existingReservation.isPresent()) {
                ReservationSalle reservation = existingReservation.get();
                reservation.setSalle(existingReunion.getSalle());
                reservationSalleRepository.save(reservation);
            } else {
                ReservationSalle newReservation = new ReservationSalle();
                newReservation.setReunion(existingReunion);
                newReservation.setSalle(existingReunion.getSalle());
                reservationSalleRepository.save(newReservation);
            }
        }

        // Traitement pour le type "EN LIGNE"
        if ("EN_LIGNE".equals(existingReunion.getType().name())) {
            if (updatedReunion.getLienZoom() == null || updatedReunion.getLienZoom().isEmpty()) {
                throw new IllegalArgumentException("Le lien Zoom ne peut pas être nul pour une réunion en ligne");
            }

            existingReunion.setLienZoom(updatedReunion.getLienZoom());

            // Si des participants sont fournis, mettre à jour et envoyer des invitations
            if (updatedReunion.getParticipants() != null && !updatedReunion.getParticipants().isEmpty()) {
                existingReunion.setParticipants(updatedReunion.getParticipants());
                envoyerEmailAuxParticipants(existingReunion);
            }
        }

        // Sauvegarder les modifications dans la base de données
        return reunionRepository.save(existingReunion);
    }







    public boolean verifierDisponibiliteSalle(Salle salle, String date, String heure, String duree, Long reunionId) {
        try {
            // Convertir la date, l'heure et la durée en objets LocalDateTime
            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + heure);
            long dureeMinutes = Long.parseLong(duree.replaceAll("[^0-9]", ""));
            LocalDateTime endDateTime = startDateTime.plusMinutes(dureeMinutes);

            // Récupérer toutes les réunions dans cette salle
            List<Reunion> reunionsExistantes = reunionRepository.findBySalleId(salle.getId());

            for (Reunion reunionExistante : reunionsExistantes) {
                // Exclure la réunion en cours (si elle existe)
                if (reunionExistante.getId().equals(reunionId)) {
                    continue; // Si c'est la même réunion, on passe à l'itération suivante
                }

                LocalDateTime existantStartDateTime = LocalDateTime.parse(reunionExistante.getDate() + "T" + reunionExistante.getHeure());
                long existantDureeMinutes = Long.parseLong(reunionExistante.getDuree().replaceAll("[^0-9]", ""));
                LocalDateTime existantEndDateTime = existantStartDateTime.plusMinutes(existantDureeMinutes);

                // Vérifier le chevauchement des horaires
                if ((startDateTime.isBefore(existantEndDateTime) && endDateTime.isAfter(existantStartDateTime))) {
                    return false; // La salle est déjà réservée pendant cette période
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
            LocalDateTime endDateTime = getLocalDateTime(reunion, start);

            return new EvenementDTO(
                    reunion.getId(),
                    reunion.getTitre(),
                    start,
                    endDateTime.toString(),
                    reunion.getType().name()
            );
        }).collect(Collectors.toList());
    }


    private static LocalDateTime getLocalDateTime(Reunion reunion, String start) {
        LocalDateTime startDateTime = LocalDateTime.parse(start);

        // Extraction sécurisée de la durée
        long dureeMinutes = 0;
        try {
            String dureeStr = reunion.getDuree().replaceAll("[^\\d]", ""); // Supprime tout sauf les chiffres
            dureeMinutes = Long.parseLong(dureeStr);
        } catch (NumberFormatException e) {
            dureeMinutes = 0;
        }

        LocalDateTime endDateTime = startDateTime.plusMinutes(dureeMinutes);
        return endDateTime;
    }


}
