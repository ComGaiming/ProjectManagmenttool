package de.comgaming.projectmanagmenttool.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailHandler {

    private final EmailConfig config;

    public EmailHandler() {
        this.config = new EmailConfig();
    }

    public void sendEmail(
            String recipient,
            String subject,
            String content
    ) throws MessagingException {

        if (recipient == null ||
                recipient.isBlank()) {

            throw new IllegalArgumentException(
                    "Empfänger darf nicht leer sein."
            );
        }

        if (subject == null) {
            throw new IllegalArgumentException(
                    "Betreff darf nicht null sein."
            );
        }

        if (content == null) {
            throw new IllegalArgumentException(
                    "E-Mail-Inhalt darf nicht null sein."
            );
        }

        Properties properties = new Properties();

        properties.put(
                "mail.smtp.host",
                config.getSmtpHost()
        );

        properties.put(
                "mail.smtp.port",
                String.valueOf(config.getSmtpPort())
        );

        properties.put(
                "mail.smtp.auth",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.required",
                "true"
        );

        Session session = Session.getInstance(
                properties,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                config.getUsername(),
                                config.getPassword()
                        );
                    }
                }
        );

        Message message =
                new MimeMessage(session);

        message.setFrom(
                new InternetAddress(
                        config.getSender()
                )
        );

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(
                        recipient
                )
        );

        message.setSubject(
                subject
        );

        message.setText(
                content
        );

        Transport.send(message);
    }
}
