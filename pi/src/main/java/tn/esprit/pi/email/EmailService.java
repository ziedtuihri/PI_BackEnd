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

import java.nio.charset.StandardCharsets;
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


    public void sendProjectAverageEmail(String to, String username, String projectName, double average) {
        try {
            // Créer le message MIME
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            // Préparer les variables pour le template Thymeleaf
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("projectName", projectName);
            context.setVariable("average", String.format("%.2f", average));

            // Générer le contenu HTML via Thymeleaf
            String htmlContent = templateEngine.process("project-average", context);

            // Configurer l'e-mail
            helper.setFrom("ziedtuihri@gmail.com");
            helper.setTo(to);
            helper.setSubject("Votre moyenne du projet " + projectName);
            helper.setText(htmlContent, true); // true => HTML

            // Envoyer l'e-mail
            mailSender.send(mimeMessage);
            log.info("Email envoyé à {} avec la moyenne du projet '{}'", to, projectName);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'e-mail à {} : {}", to, e.getMessage(), e);
        }
    }




}
