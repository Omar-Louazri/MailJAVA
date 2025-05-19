package com.mail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class App {

     private static final DateTimeFormatter INPUT_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

     public static void main(String[] args) throws InterruptedException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        MailSender sender = new MailSender("localhost", "25", "taha@india.com", "1234");
        Scheduler scheduler = new Scheduler(sender);

        // Démarrage
        scheduler.initScheduler();

        // Planification
        LocalDateTime now = LocalDateTime.now();
        scheduler.addScheduledMail("taha@india.com", "anas@india.com", "Immediate Test", "Immediate body", now);
        scheduler.addScheduledMail("taha@india.com", "anas@india.com", "Delayed Test", "Delay 1min", now.plusMinutes(1));
        scheduler.addScheduledMail(
            "taha@india.com",
            "anas@india.com",
            "Custom Test",
            "Custom at 2025-05-18 23:49:00",
            LocalDateTime.parse("2025-05-18 22:49:00", fmt)
        );

        // Laisser tourner
        Thread.sleep(180_000);
        scheduler.stop();

        // Affichage des envoyés
        System.out.println("Sent emails via EmailList:");
        List<Email> sent = scheduler.getSentEmails();
        for (Email e : sent) {
            System.out.println("- " + e.getSubject() + " | to=" + e.getRecipients() + " | at=" + e.getTime());
        }
    }
}  
