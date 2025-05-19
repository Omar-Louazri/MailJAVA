package com.mail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jakarta.mail.MessagingException;

public class ComposeEmailDialog extends JDialog {
    private static final int MIN_HEIGHT = 40;
    private static final int EXP_HEIGHT = 600;
    private static final int WIDTH      = 500;

    private final MailSender mailSender;
    private JTextField       toField;
    private JTextField       subjectField;
    private JTextArea        bodyArea;
    private JButton          sendButton;
    private JButton          minimizeButton;
    private JButton          closeButton;
    private JPanel           headerPanel;
    private JPanel           contentPanel;
    private boolean          isMinimized = false;
    private boolean          sent        = false;
    private Point            dragPoint;

    public ComposeEmailDialog(Frame parent, MailSender mailSender) {
        super(parent, false);
        this.mailSender = mailSender;
        setUndecorated(true);

        initComponents();
        setupDrag();

        setSize(WIDTH, EXP_HEIGHT);
        setLocationRelativeTo(parent);
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- HEADER ---
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240,240,240));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.GRAY));
        JLabel title = new JLabel("Nouveau message");
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 14f));
        headerPanel.add(title, BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        minimizeButton = createCtrl("−");
        closeButton    = createCtrl("×");
        ctrl.add(minimizeButton);
        ctrl.add(closeButton);
        headerPanel.add(ctrl, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- CONTENT FORM ---
        contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // À :
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        contentPanel.add(new JLabel("À :"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        toField = new JTextField();
        contentPanel.add(toField, gbc);

        // Sujet :
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        contentPanel.add(new JLabel("Sujet :"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        subjectField = new JTextField();
        contentPanel.add(subjectField, gbc);

        // Corps (remplit l'espace restant)
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.fill    = GridBagConstraints.BOTH;
        bodyArea = new JTextArea();
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        contentPanel.add(new JScrollPane(bodyArea), gbc);

        // Bouton Envoyer
        gbc.gridy    = 3;
        gbc.weighty  = 0;
        gbc.fill     = GridBagConstraints.NONE;
        sendButton  = new JButton("Envoyer");
        sendButton.setBackground(new Color(0, 120, 212));
        sendButton.setForeground(Color.BLACK);
        contentPanel.add(sendButton, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // ─── LISTENERS ───
        minimizeButton.addActionListener(e -> toggleMinimize());
        closeButton.   addActionListener(e -> dispose());
        sendButton.    addActionListener(e -> handleSend());
        headerPanel.   addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                headerPanel.setBackground(new Color(230,230,230));
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                headerPanel.setBackground(new Color(240,240,240));
            }
        });
    }

    private void handleSend() {
        String to = toField.getText().trim();
        if (to.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez saisir l'adresse du destinataire.",
                "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Email email = new Email();
        email.setFrom(mailSender.getUsername());
        email.addRecipient(to);
        email.setSubject(subjectField.getText());
        email.setBody(bodyArea.getText());

        try {
            mailSender.sendAndSave(email, "SENT");
            sent = true;
            dispose();
        } catch (MessagingException ex) {
            JOptionPane.showMessageDialog(this,
                "Échec de l'envoi : " + ex.getMessage(),
                "Erreur SMTP", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleMinimize() {
        isMinimized = !isMinimized;
        headerPanel  .setVisible(!isMinimized);
        contentPanel .setVisible(!isMinimized);
        minimizeButton.setText(isMinimized ? "□" : "−");
        setSize(WIDTH, isMinimized ? MIN_HEIGHT : EXP_HEIGHT);
    }

    private void setupDrag() {
        headerPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint = e.getPoint(); }
        });
        headerPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(
                  loc.x + e.getX() - dragPoint.x,
                  loc.y + e.getY() - dragPoint.y
                );
            }
        });
    }

    private JButton createCtrl(String txt) {
        JButton b = new JButton(txt);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setPreferredSize(new Dimension(30,30));
        b.setFocusPainted(false);
        return b;
    }

    public boolean isSent() {
        return sent;
    }
}