package com.mail;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for delivering queued emails periodically and saving sent emails.
 * Scheduled emails are also persisted in a database table `scheduled_emails`.
 * Inherits from EmailList to record sent emails.
 */
public class Scheduler extends EmailList {
    private static final int DEFAULT_CYCLE_SECONDS = 60;

    private final BlockingQueue<Email> queue;
    private final ScheduledExecutorService executor;
    private volatile boolean running;
    private final MailSender mailSender;

    // Database config
    private static final String DB_URL  = "jdbc:postgresql://localhost:5433/jamesdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "1234";

    public Scheduler(MailSender mailSender) {
        super(); // EmailList constructor
        this.queue = new LinkedBlockingQueue<>();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.running = false;
        this.mailSender = mailSender;
    }

    public void initScheduler() {
        if (running) return;
        running = true;
        loadPendingFromDB();
        executor.scheduleAtFixedRate(this::processQueue, 0, DEFAULT_CYCLE_SECONDS, TimeUnit.SECONDS);
        System.out.println("Scheduler started, cycle = " + DEFAULT_CYCLE_SECONDS + "s");
    }

    public void stop() {
        if (!running) return;
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Scheduler stopped.");
    }

    public void addScheduledMail(
            String from,
            String recipient,
            String subject,
            String body,
            LocalDateTime sendTime
    ) {
        Email email = new Email();
        email.setFrom(from);
        email.addRecipient(recipient);
        email.setSubject(subject);
        email.setBody(body);
        email.setTime(sendTime);
        email.setRead(false);
        email.setArchived(false);

        String insert = "INSERT INTO scheduled_emails " +
            "(message_id, sender, recipient, subject, body, send_time) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, email.getId());
            ps.setString(2, email.getFrom());
            ps.setString(3, recipient);
            ps.setString(4, email.getSubject());
            ps.setString(5, email.getBody());
            ps.setTimestamp(6, Timestamp.valueOf(sendTime));
            ps.executeUpdate();
            System.out.println("Scheduled and persisted email '" + subject + "' for " + sendTime);
        } catch (SQLException e) {
            System.err.println("Erreur persistance scheduled email: " + e.getMessage());
        }
        loadPendingFromDB();
    }

    private void loadPendingFromDB() {
        queue.clear();
        String query = "SELECT message_id, sender, recipient, subject, body, send_time " +
                       "FROM scheduled_emails " +
                       "WHERE status='PENDING' " +
                       "ORDER BY send_time ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Email email = new Email();
                email.setId(rs.getString("message_id"));
                email.setFrom(rs.getString("sender"));
                email.setRecipients(List.of(rs.getString("recipient")));
                email.setSubject(rs.getString("subject"));
                email.setBody(rs.getString("body"));
                email.setTime(rs.getTimestamp("send_time").toLocalDateTime());
                queue.offer(email);
            }
            System.out.println("Loaded " + queue.size() + " pending emails from DB into queue.");
        } catch (SQLException e) {
            System.err.println("Erreur chargement scheduled emails: " + e.getMessage());
        }
    }

    private void processQueue() {
        System.out.println("Processing queue at " + LocalDateTime.now());
        Email email;
        while ((email = queue.poll()) != null) {
            try {
                if (email.getTime().isAfter(LocalDateTime.now())) {
                    queue.offer(email);
                } else {
                    mailSender.sendAndSave(email,"SENT");
                    markAsSent(email.getId());
                    addEmail(email); // record sent email via EmailList inheritance
                    System.out.println("Sent and saved: " + email.getSubject());
                }
            } catch (Exception ex) {
                System.err.println("Failed to send " + email.getSubject() + ": " + ex.getMessage());
                queue.offer(email);
            }
        }
    }

    private void markAsSent(String messageId) {
        String update = "UPDATE scheduled_emails SET status='SENT' WHERE message_id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statut scheduled email: " + e.getMessage());
        }
    }

    public List<Email> getQueueSnapshot() {
        return List.copyOf(queue);
    }

    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Returns a list of emails that have been sent in this session.
     * Implemented via inherited EmailList.getEmails().
     */
    public List<Email> getSentEmails() {
        return getEmails();
    }
}
