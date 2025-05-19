package com.mail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.stream.Collectors;

/**
 * Gestion de la persistance des emails dans PostgreSQL.
 */
public class EmailSaver {

    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/jamesdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "1234";

    /**
     * Sauvegarde un Email en base pour un utilisateur donné.
     *
     * @param userId  Identifiant de l'utilisateur
     * @param email   Objet Email contenant toutes les infos
     * @param folder  Dossier (e.g. "SENT", "INBOX")
     */
    public static void saveEmailSent(Email email, String folder) {
        String sql = "INSERT INTO messages (" +
                     "message_id, sender, recipients, subject, body, received_at, read, folder" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.getId());
            stmt.setString(2, email.getFrom());
            // Concatène les destinataires par virgule
            String recips = email.getRecipients().stream().collect(Collectors.joining(","));
            stmt.setString(3, recips);
            stmt.setString(4, email.getSubject());
            stmt.setString(5, email.getBody());
            stmt.setTimestamp(6, Timestamp.valueOf(email.getTime()));
            stmt.setBoolean(7, email.isRead());
            stmt.setString(8, folder);

            stmt.executeUpdate();
            System.out.println("Email sauvegardé dans la table `messages`.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde de l'email : " + e.getMessage());
        }
    }
}
