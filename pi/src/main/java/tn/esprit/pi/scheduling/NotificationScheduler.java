package tn.esprit.pi.scheduling;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.email.EmailService; // Import your EmailService
import tn.esprit.pi.services.ISprintService; // Import your ISprintService

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final ISprintService sprintService;
    private final EmailService emailService;

    // This method will run every day at a specific time (e.g., 9:00 AM)
    // Or every 6 hours if you prefer (e.g., cron = "0 0 */6 * * *")
    // Or every 5 minutes for testing (e.g., fixedRate = 300000)
    @Scheduled(cron = "0 0 9 * * *") // Runs every day at 9:00 AM
    // @Scheduled(fixedRate = 300000) // Runs every 5 minutes (for testing)
    public void sendUrgentSprintNotifications() {
        log.info("Running scheduled task: Checking for urgent sprint deadlines...");

        List<Sprint> urgentSprints = sprintService.getSprintsWithUpcomingDeadlines();

        if (urgentSprints.isEmpty()) {
            log.info("No urgent sprints with upcoming deadlines found.");
            return;
        }

        for (Sprint sprint : urgentSprints) {
            // Determine who to send the notification to.
            // Options:
            // 1. All assigned students (`sprint.getEtudiantsAffectes()`)
            // 2. The project's teacher/professor (`sprint.getProjet().getTeacherEmail()`)
            // 3. Both

            // Let's send to all assigned students for the sprint
            List<String> recipients = sprint.getEtudiantsAffectes();
            if (sprint.getProjet() != null && sprint.getProjet().getTeacherEmail() != null) {
                // Also send to the project teacher if you want
                // recipients.add(sprint.getProjet().getTeacherEmail());
            }

            if (recipients.isEmpty()) {
                log.warn("Sprint ID {} ({}) is urgent but has no assigned students/recipients to notify.", sprint.getIdSprint(), sprint.getNom());
                continue;
            }

            String projectName = (sprint.getProjet() != null) ? sprint.getProjet().getNom() : "N/A";
            String subject = "[URGENT] Rappel de Délai - Sprint : " + sprint.getNom() + " (Projet: " + projectName + ")";
            String body = "Bonjour,\n\n"
                    + "Ceci est un rappel urgent pour le sprint suivant :\n\n"
                    + "Nom du Sprint : " + sprint.getNom() + "\n"
                    + "Projet Associé : " + projectName + "\n"
                    + "Date de Début : " + (sprint.getDateDebut() != null ? sprint.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A") + "\n"
                    + "Date de Fin : " + (sprint.getDateFin() != null ? sprint.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A") + "\n"
                    + "Statut : " + (sprint.getStatut() != null ? sprint.getStatut().name() : "N/A") + "\n\n"
                    + "Le délai de notification pour ce sprint a été atteint. Veuillez vous assurer que toutes les tâches sont en cours et que le sprint progresse comme prévu.\n\n"
                    + "Cordialement,\nVotre équipe de gestion de projets";

            for (String recipientEmail : recipients) {
                try {
                    emailService.sendEmail(recipientEmail, subject, body);
                    log.info("Urgent sprint notification sent to {} for sprint: {}", recipientEmail, sprint.getNom());
                } catch (MessagingException e) {
                    log.error("Failed to send urgent sprint notification email to {} for sprint {}: {}", recipientEmail, sprint.getNom(), e.getMessage());
                }
            }
        }
        log.info("Urgent sprint deadline check completed.");
    }
}