package com.mail;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Envoie des emails via SMTP et peut sauvegarder le mail envoyé en base.
 */
public class MailSender {
    private final String host;
    private final String port;
    private final String username;
    private final String password;

    public MailSender(String host, String port, String username, String password) {
        this.host     = host;
        this.port     = port;
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getHost()     { return host; }
    public String getPort()     { return port; }

    /**
     * Envoie un email en utilisant les champs de l'objet Email.
     */
    public void sendEmail(Email email) throws MessagingException {
        sendEmail(
            email.getFrom(),
            String.join(",", email.getRecipients()),
            email.getSubject(),
            email.getBody()
        );
    }

    /**
     * Envoie l'email puis le sauvegarde dans la table messages.
     *
     * @param userId Identifiant de l'utilisateur courant
     * @param email  Objet Email à envoyer et sauvegarder
     * @param folder Dossier à indiquer dans la base ("SENT", "INBOX", etc.)
     */
    public void sendAndSave(Email email, String folder) throws MessagingException {
        // 1. Envoi SMTP
        sendEmail(email);

        // 2. Mettre à jour l'état de l'email
        email.setRead(true);
        email.setArchived(false);

        // 3. Sauvegarde en base
        EmailSaver.saveEmailSent(email, folder);
    }

    /**
     * Envoi basique par SMTP.
     */
    public void sendEmail(String from, String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
