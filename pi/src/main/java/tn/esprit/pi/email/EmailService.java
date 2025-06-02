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
import tn.esprit.pi.entities.enumerations.ProjectType;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

import tn.esprit.pi.entities.enumerations.ProjectType;


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


    // MÉTHODE POUR L'AFFECTATION DE PROJET AUX ÉTUDIANTS (sendProjetAssignmentEmail) - MAINTENANT CORRIGÉE
    public void sendProjetAssignmentEmail(
            String to,
            String studentName,
            String projectName,
            String projectDescription,
            String projectDateDebut, // La date formatée
            String projectDateFinPrevue, // NOUVEAU PARAMÈTRE CRUCIAL AJOUTÉ ICI !
            String teacherEmail,
            ProjectType projectType
    ) throws MessagingException {
        String templateName = "projet-assignment";

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put("username", studentName);
        properties.put("projetName", projectName);
        properties.put("projetDescription", projectDescription);
        properties.put("projetDateDebut", projectDateDebut);
        properties.put("projetDateFinPrevue", projectDateFinPrevue); // AJOUT TRÈS IMPORTANT : Passer la date de fin au template
        properties.put("teacherEmail", teacherEmail);
        properties.put("projectType", projectType != null ? projectType.name() : "Non spécifié");

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom("ziedtuihri@gmail.com");
        helper.setTo(to);
        helper.setSubject("Affectation à un nouveau projet : " + projectName);

        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);

        mailSender.send(mimeMessage);
        log.info("Projet assignment email sent successfully to: {}", to);
    }

    // NOUVELLE MÉTHODE GÉNÉRIQUE POUR L'ENVOI D'EMAIL (UTILISÉE POUR L'ENCADRANT)
    /**
     * Envoie un e-mail en texte brut avec un sujet et un corps spécifiés.
     * Cette méthode est ajoutée pour permettre l'envoi d'e-mails simples sans template Thymeleaf.
     *
     * @param to Le destinataire de l'e-mail.
     * @param subject Le sujet de l'e-mail.
     * @param body Le corps de l'e-mail (texte brut ou HTML si 'true' est passé pour isHtml).
     * @throws MessagingException Si une erreur survient lors de l'envoi du message.
     */
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );

        helper.setFrom("ziedtuihri@gmail.com"); // Assurez-vous que c'est votre adresse d'envoi
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, false); // 'false' car le corps est construit en texte brut dans ProjetServiceImpl

        mailSender.send(mimeMessage);
        log.info("Email générique envoyé avec succès à: {} avec le sujet: {}", to, subject);
    }



}
