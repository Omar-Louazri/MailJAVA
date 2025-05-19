package com.mail;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;

/**
 * Panel pour planifier un envoi d'email.
 * Ne contient plus de bouton « Schedule E-mail » superflu ; 
 * il propose uniquement le formulaire et le bouton de submission.
 */
public class SchedulerPanel extends JPanel {
    private final Scheduler scheduler;
    private final MailSender mailSender;

    private JTextField toField, dateField, timeField, subjectField;
    private JTextArea  bodyArea;
    private JLabel     dateStatus, timeStatus;

    public SchedulerPanel(MailSender mailSender) {
        this.mailSender = mailSender;
        // Créer et démarrer le scheduler (cycle 60s, dossier "SENT")
        this.scheduler = new Scheduler(mailSender);
        this.scheduler.initScheduler();

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        // Titre
        JLabel title = new JLabel("Schedule an E-mail", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // Formulaire
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5,5,5,5);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        toField     = new JTextField(30);
        dateField   = new JTextField(10);
        timeField   = new JTextField(8);
        subjectField= new JTextField(30);
        bodyArea    = new JTextArea(8,30);
        dateStatus  = createStatusLabel();
        timeStatus  = createStatusLabel();


        // …

        DateTimeFormatter dateFmt  = DateTimeFormatter.ISO_LOCAL_DATE;          // 2025‑05‑19
        DateTimeFormatter timeFmt  = DateTimeFormatter.ofPattern("HH:mm:ss");   // 22:00:00


        dateField.setText(LocalDate.now().format(dateFmt));
        timeField.setText(LocalTime.now()
                                .withNano(0)           // keep seconds, drop nanos
                                .format(timeFmt));

        addFormRow(form, gbc, row++, "To (comma-sep):",        toField,      null);
        addFormRow(form, gbc, row++, "Date (YYYY-MM-DD):",     dateField,    dateStatus);
        addFormRow(form, gbc, row++, "Time (HH:MM):",          timeField,    timeStatus);
        addFormRow(form, gbc, row++, "Subject:",               subjectField, null);

        // Body (pleine largeur)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Body:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        form.add(new JScrollPane(bodyArea), gbc);

        add(form, BorderLayout.CENTER);

        // Bouton « Schedule »
        JButton scheduleBtn = new JButton("Schedule");
        scheduleBtn.addActionListener(e -> scheduleEmails());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(scheduleBtn);
        add(btnP, BorderLayout.SOUTH);
    }

    private void scheduleEmails() {
        // Validation date
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
            dateStatus.setText("OK"); dateStatus.setForeground(new Color(0,128,0));
        } catch (DateTimeParseException ex) {
            dateStatus.setText("Bad date"); dateStatus.setForeground(Color.RED);
            return;
        }
        // Validation time
        LocalTime time;
        try {
            time = LocalTime.parse(timeField.getText().trim());
            timeStatus.setText("OK"); timeStatus.setForeground(new Color(0,128,0));
        } catch (DateTimeParseException ex) {
            timeStatus.setText("Bad time"); timeStatus.setForeground(Color.RED);
            return;
        }

        String[] recipients = toField.getText().trim().split("\\s*,\\s*");
        String subject = subjectField.getText().trim();
        String body    = bodyArea.getText();
        LocalDateTime sendAt = LocalDateTime.of(date, time);

        for (String r : recipients) {
            scheduler.addScheduledMail(
                mailSender.getUsername(),
                r,
                subject,
                body,
                sendAt
            );
        }

        JOptionPane.showMessageDialog(
            this,
            "E-mails planifiés à " + sendAt,
            "Succès",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static JLabel createStatusLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC, 11f));
        return lbl;
    }

    private static void addFormRow(JPanel form, GridBagConstraints gbc,
                                   int row, String label,
                                   JComponent field, JLabel status) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
        if (status != null) {
            gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            form.add(status, gbc);
        }
    }
}
