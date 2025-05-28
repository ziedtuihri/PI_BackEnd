package tn.esprit.pi.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject

    ) throws MessagingException {
        String templateName = (emailTemplate == null) ? "confirm-email" : emailTemplate.getName();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom("ziedtuihri@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);

        mailSender.send(mimeMessage);
        log.info("Email sent successfully to: {}", to);
    }
    // --- Nouvelle méthode pour l'affectation de projet (sendProjetAssignmentEmail) ---
    public void sendProjetAssignmentEmail(
            String to,
            String studentName,
            String projectName,
            String projectDescription,
            String projectDateDebut, // La date formatée
            String teacherEmail
    ) throws MessagingException {
        // Le nom du template Thymeleaf pour l'affectation de projet
        String templateName = "projet-assignment"; // Doit correspondre à src/main/resources/templates/projet-assignment.html

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put("username", studentName); // Le template peut utiliser "username" pour saluer
        properties.put("projetName", projectName);
        properties.put("projetDescription", projectDescription);
        properties.put("projetDateDebut", projectDateDebut); // C'est déjà une String formatée
        properties.put("teacherEmail", teacherEmail);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom("ziedtuihri@gmail.com"); // Assurez-vous que c'est votre adresse d'envoi
        helper.setTo(to);
        helper.setSubject("Affectation à un nouveau projet : " + projectName);

        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);

        mailSender.send(mimeMessage);
        log.info("Projet assignment email sent successfully to: {}", to);
    }
}
