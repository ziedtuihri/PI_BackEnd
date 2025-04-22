
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
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

            // Vérifier la disponibilité de la salle avant de la marquer comme non disponible
            boolean salleDisponible = verifierDisponibiliteSalle(reunion.getSalle(), reunion.getDate(), reunion.getHeure(), reunion.getDuree(), null);
            if (!salleDisponible) {
                throw new IllegalArgumentException("La salle n'est pas disponible pour la date et l'heure spécifiées");
            }

            // Marquer la salle comme non disponible
            reunion.getSalle().setDisponible(false);

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

            // Vérifier la disponibilité de la salle avant de l'affecter
            boolean salleDisponible = verifierDisponibiliteSalle(updatedReunion.getSalle(), updatedReunion.getDate(), updatedReunion.getHeure(), updatedReunion.getDuree(), reunionId);
            if (!salleDisponible) {
                throw new IllegalArgumentException("La salle n'est pas disponible pour la date et l'heure spécifiées");
            }

            // Marquer la salle comme non disponible pour la réunion présentielle
            updatedReunion.getSalle().setDisponible(false);

            // Mise à jour de la salle
            existingReunion.setSalle(updatedReunion.getSalle());

            // Si la réunion est modifiée en présentiel, supprimer les participants
            existingReunion.setParticipants(null);

            // Mise à jour de la réservation de la salle
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

    public List<Reunion> getAllReunions() {
        return reunionRepository.findAll(); // Vérifiez ici que le repository fonctionne correctement
    }


    public List<Salle> getSallesDisponiblesPourReunion(String date, String heure, String duree) {
        try {
            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + heure);
            long dureeMinutes = Long.parseLong(duree.replaceAll("[^0-9]", ""));
            LocalDateTime endDateTime = startDateTime.plusMinutes(dureeMinutes);

            // Récupérer toutes les salles
            List<Salle> allSalles = salleRepository.findAll();

            // Filtrer les salles disponibles pendant la plage horaire
            List<Salle> sallesDisponibles = allSalles.stream()
                    .filter(salle -> isSalleDisponible(salle, startDateTime, endDateTime))
                    .collect(Collectors.toList());

            return sallesDisponibles;

        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur lors de la récupération des salles disponibles", e);
        }
    }
    public boolean isSalleDisponible(Salle salle, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Récupérer toutes les réservations de la salle
        List<ReservationSalle> reservations = reservationSalleRepository.findBySalle(salle);

        // Vérifier les chevauchements de dates
        for (ReservationSalle reservation : reservations) {
            // Vérifier si la date est non null avant de la parser
            String dateStr = reservation.getDate();
            if (dateStr == null || dateStr.isEmpty()) {
                System.out.println("Date de réservation invalide pour la salle " + salle.getNom());
                continue; // Ignorer cette réservation
            }

            // Convertir la date et l'heure (String) en LocalDate et LocalTime
            LocalDate reservationDate = LocalDate.parse(dateStr);  // La date est une chaîne au format "YYYY-MM-DD"
            LocalTime reservationTime = LocalTime.parse(reservation.getHeure());  // L'heure est une chaîne au format "HH:MM"

            // Créer un LocalDateTime en combinant la date et l'heure
            LocalDateTime reservationStart = LocalDateTime.of(reservationDate, reservationTime);

            // Assurez-vous que la durée est bien un nombre entier
            long dureeMinutes;
            try {
                dureeMinutes = Long.parseLong(reservation.getDuree().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                // Si la durée n'est pas un nombre valide, on lève une exception ou on gère autrement
                throw new IllegalArgumentException("La durée de réservation est invalide", e);
            }

            // Calculer l'heure de fin de la réservation
            LocalDateTime reservationEnd = reservationStart.plusMinutes(dureeMinutes);

            // Vérifier si la réservation est dans le passé
            if (reservationEnd.isBefore(LocalDateTime.now())) {
                // Ignorer les réservations passées (elles ne devraient pas bloquer la disponibilité)
                System.out.println("Réservation déjà passée pour la salle " + salle.getNom());
                continue;
            }

            // Si l'une des périodes se chevauche, la salle n'est pas disponible
            if (!(endDateTime.isBefore(reservationStart) || startDateTime.isAfter(reservationEnd))) {
                return false; // La salle n'est pas disponible
            }
        }

        // Si aucune réservation ne se chevauche, la salle est disponible
        return true;
    }



    public Salle getSalleById(Long id) {
        return salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'ID : " + id));
    }
    public ReservationSalle saveReservation(ReservationSalle reservation) {
        return reservationSalleRepository.save(reservation);
    }


    public Reunion getReunionById(Long reunionId) {
        return reunionRepository.findById(reunionId).orElse(null);  // Retourne null si la réunion n'est pas trouvée
    }

    public List<Salle> getAllSalleDisponible() {
        // Récupérer toutes les salles
        List<Salle> allSalles = salleRepository.findAll();

        // Filtrer uniquement les salles disponibles
        List<Salle> availableSalles = allSalles.stream()
                .filter(Salle::isDisponible)  // Filtrer selon la disponibilité
                .collect(Collectors.toList());

        return availableSalles;
    }

    @Override
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

            // Mettre à jour les informations du participant
            if (updatedParticipant.getNom() != null) {
                existingParticipant.setNom(updatedParticipant.getNom());
            }
            if (updatedParticipant.getEmail() != null) {
                existingParticipant.setEmail(updatedParticipant.getEmail());
            }

            // Mettre à jour la relation User (si elle existe dans la requête)
            if (updatedParticipant.getUser() != null) {
                existingParticipant.setUser(updatedParticipant.getUser());
            }

            // Sauvegarder le participant mis à jour
            return participantRepository.save(existingParticipant);
        } else {
            throw new NoSuchElementException("Participant non trouvé avec ID : " + id);
        }
    }



    public ReservationSalle getReservationById(Long id) {
        Optional<ReservationSalle> reservation = reservationSalleRepository.findById(id);
        return reservation.orElse(null);
    }


}


