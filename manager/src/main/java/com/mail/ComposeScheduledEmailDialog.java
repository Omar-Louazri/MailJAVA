package com.mail;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javax.swing.*;


import jakarta.mail.MessagingException;

/**
 * A unified dialog that looks and behaves like your existing {@link ComposeEmailDialog}
 * but embeds the scheduling form and validation logic taken from {@link SchedulerPanel}.
 * <p>
 * Fields
 * <ul>
 *   <li>Destinataires (comma‑separated)</li>
 *   <li>Date d'envoi (AAAA‑MM‑JJ)</li>
 *   <li>Heure d'envoi (HH:MM:SS)</li>
 *   <li>Sujet</li>
 *   <li>Corps</li>
 * </ul>
 * A « Valider » button shows live ISO‑formatted feedback for the date & time; an « Programmer » button
 * calls {@code mailSender.schedule(email, sendDateTime)} (you need to implement this persistence
 * method in your MailSender / service layer). If the user only presses « Envoyer » the message is
 * sent immediately, just like before.
 */
public class ComposeScheduledEmailDialog extends JDialog {
    private static final int WIDTH = 650;
    private static final int HEIGHT = 600;

    private final MailSender mailSender;
    private final int        userId;

    private JTextField recipientsField;
    private JTextField dateField;
    private JLabel     dateStatus;
    private JTextField timeField;
    private JLabel     timeStatus;
    private JTextField subjectField;
    private JTextArea  bodyArea;

    private JButton    validateBtn;
    private JButton    sendBtn;
    private JButton    scheduleBtn;

    public ComposeScheduledEmailDialog(Frame owner, MailSender mailSender, int userId) {
        super(owner, "Programmer un e‑mail", true);
        this.mailSender = mailSender;
        this.userId     = userId;
        buildUI();
        pack();
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(owner);
    }

    // --------------------- UI ---------------------
    private void buildUI() {
        setLayout(new BorderLayout(10,10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel title = new JLabel("Programmer un e‑mail", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row=0;
        recipientsField = new JTextField(30);
        addRow(form, gbc, row++, "Destinataires (séparés par virgule):", recipientsField);

        dateField = new JTextField(12);
        dateStatus = statusLabel();
        addRowWithStatus(form, gbc, row++, "Date d'envoi (AAAA‑MM‑JJ):", dateField, dateStatus);

        timeField = new JTextField(10);
        timeStatus = statusLabel();
        addRowWithStatus(form, gbc, row++, "Heure d'envoi (HH:MM:SS):", timeField, timeStatus);

        subjectField = new JTextField(30);
        addRow(form, gbc, row++, "Sujet:", subjectField);

        bodyArea = new JTextArea(8, 30);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridx=0; gbc.gridy=row; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        form.add(new JLabel("Corps:"), gbc);
        gbc.gridx=1; gbc.gridwidth=2; gbc.weightx=1; gbc.weighty=1; gbc.fill=GridBagConstraints.BOTH;
        form.add(bodyScroll, gbc);

        add(form, BorderLayout.CENTER);

        // ---------- buttons ----------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        validateBtn = new JButton("Valider");
        sendBtn     = new JButton("Envoyer maintenant");
        scheduleBtn = new JButton("Programmer");

        validateBtn.addActionListener(e->validateDateTime());
        sendBtn.addActionListener(e->sendNow());
        scheduleBtn.addActionListener(e->scheduleSend());

        btnPanel.add(validateBtn);
        btnPanel.add(sendBtn);
        btnPanel.add(scheduleBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ------------------- actions -------------------
    private void validateDateTime() {
        // date
        if (isValidDate(dateField.getText())) {
            dateStatus.setForeground(new Color(0,128,0));
            dateStatus.setText(LocalDate.parse(dateField.getText()).toString());
        } else { bad(dateStatus, "Format invalide"); }
        // time
        if (isValidTime(timeField.getText())) {
            timeStatus.setForeground(new Color(0,128,0));
            timeStatus.setText(LocalTime.parse(timeField.getText()).toString());
        } else { bad(timeStatus, "Format invalide"); }
    }

    private void sendNow() {
        if (!buildAndSendEmail(null)) return;
        dispose();
    }

    private void scheduleSend() {
        if (!isValidDate(dateField.getText()) || !isValidTime(timeField.getText())) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une date et heure valides.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate d = LocalDate.parse(dateField.getText());
        LocalTime t = LocalTime.parse(timeField.getText());
        if (!buildAndSendEmail(d.atTime(t))) return;
        dispose();
    }

    // returns true if send/schedule succeeded
    private boolean buildAndSendEmail(java.time.LocalDateTime when) {
        String to = recipientsField.getText().trim();
        if (to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir au moins un destinataire.", "Validation", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Email email = new Email();
        email.setFrom(mailSender.getUsername());
        for (String r : to.split(",")) email.addRecipient(r.trim());
        email.setSubject(subjectField.getText());
        email.setBody(bodyArea.getText());

        try {
            if (when == null) {
                mailSender.sendAndSave(email, "SENT");
            }
            return true;
        } catch (MessagingException ex) {
            JOptionPane.showMessageDialog(this, "Échec de l'envoi : "+ex.getMessage(), "Erreur SMTP", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // --------------------- helpers ---------------------
    private static void addRow(JPanel p, GridBagConstraints gbc, int row, String lbl, JComponent field) {
        gbc.gridx=0; gbc.gridy=row; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        p.add(new JLabel(lbl), gbc);
        gbc.gridx=1; gbc.weightx=1; gbc.gridwidth=2; gbc.fill=GridBagConstraints.HORIZONTAL;
        p.add(field, gbc);
        gbc.gridwidth=1;
    }
    private static void addRowWithStatus(JPanel p, GridBagConstraints gbc,int row,String lbl,JComponent field,JLabel status){
        gbc.gridx=0; gbc.gridy=row; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        p.add(new JLabel(lbl), gbc);
        gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL;
        p.add(field, gbc);
        gbc.gridx=2; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        p.add(status, gbc);
    }
    private static JLabel statusLabel(){
        JLabel l=new JLabel(" ");
        l.setFont(l.getFont().deriveFont(Font.ITALIC,11f));
        return l;
    }
    private static void bad(JLabel lbl,String msg){ lbl.setForeground(Color.RED); lbl.setText(msg);}    
    private static boolean isValidDate(String txt){try{LocalDate.parse(txt);return true;}catch(DateTimeParseException e){return false;}}
    private static boolean isValidTime(String txt){try{LocalTime.parse(txt);return true;}catch(DateTimeParseException e){return false;}}

            public static void startUI(Frame owner,
                                    MailSender mailSender,
                                    int        userId) {

                ComposeScheduledEmailDialog dlg =
                    new ComposeScheduledEmailDialog(owner, mailSender, userId);

                dlg.setVisible(true);          // modal → blocks until closed

                // Example post‑processing if the caller cares
               
            }
}
