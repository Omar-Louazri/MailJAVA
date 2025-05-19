// ComposeEmailDialog.java
package com.mail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jakarta.mail.MessagingException;

public class ComposeEmailDialog extends JDialog {
    private static final int MIN_HEIGHT = 40;
    private static final int EXP_HEIGHT = 600;
    private static final int WIDTH      = 500;

    private final MailSender mailSender;
    private JTextField toField;
    private JTextField subjectField;
    private JTextArea  bodyArea;
    private JButton    sendButton;
    private JButton    minimizeButton;
    private JButton    closeButton;
    private JPanel     headerPanel;
    private JPanel     toolbarPanel;
    private JPanel     contentPanel;
    private boolean    isMinimized = false;
    private boolean    sent        = false;
    private Point      dragPoint;

    public ComposeEmailDialog(Frame parent, MailSender mailSender) {
        super(parent, false);
        this.mailSender = mailSender;
        setUndecorated(true);

        initComponents();
        setupDrag();

        setSize(WIDTH, EXP_HEIGHT);
        setLocationRelativeTo(parent);
        setBackground(new Color(240,240,240));
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // HEADER
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240,240,240));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(200,200,200)));
        JLabel title = new JLabel("Nouveau message");
        title.setFont(title.getFont().deriveFont(Font.PLAIN,14f));
        headerPanel.add(title, BorderLayout.WEST);

        JPanel ctr = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        minimizeButton = createCtrlButton("−");
        closeButton    = createCtrlButton("×");
        ctr.add(minimizeButton);
        ctr.add(closeButton);
        headerPanel.add(ctr, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // TOOLBAR (facultatif)
        toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,2,2));
        toolbarPanel.setBackground(new Color(245,245,245));
        toolbarPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(200,200,200)));
        toolbarPanel.add(createToolbarBtn("G","Gras"));
        toolbarPanel.add(createToolbarBtn("I","Italique"));
        toolbarPanel.add(createToolbarBtn("S","Souligné"));
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(createToolbarBtn("📎","Joindre"));
        toolbarPanel.add(createToolbarBtn("📷","Insérer image"));
        add(toolbarPanel, BorderLayout.CENTER);

        // CONTENT
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx= 1;

        // To
        gbc.gridx=0; gbc.gridy=0; gbc.weightx=0;
        contentPanel.add(new JLabel("À :"), gbc);
        gbc.gridx=1; gbc.weightx=1;
        toField = new JTextField(20);
        contentPanel.add(toField, gbc);

        // Sujet
        gbc.gridx=0; gbc.gridy=1; gbc.weightx=0;
        contentPanel.add(new JLabel("Sujet :"), gbc);
        gbc.gridx=1; gbc.weightx=1;
        subjectField = new JTextField(20);
        contentPanel.add(subjectField, gbc);

        // Corps
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; gbc.weighty=1; gbc.fill=GridBagConstraints.BOTH;
        bodyArea = new JTextArea(15,20);
        bodyArea.setLineWrap(true); bodyArea.setWrapStyleWord(true);
        contentPanel.add(new JScrollPane(bodyArea), gbc);

        // Bouton Envoyer
        gbc.gridy=3; gbc.weighty=0; gbc.fill=GridBagConstraints.NONE;
        sendButton = new JButton("Envoyer");
        sendButton.setBackground(new Color(0,120,212));
        sendButton.setForeground(Color.WHITE);
        contentPanel.add(sendButton, gbc);

        add(contentPanel, BorderLayout.SOUTH);

        // LISTENERS
        minimizeButton.addActionListener(e -> toggleMinimize());
        closeButton.addActionListener(e -> dispose());
        sendButton.addActionListener(e -> handleSend());

        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                headerPanel.setBackground(new Color(230,230,230));
            }
            public void windowDeactivated(WindowEvent e) {
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
        headerPanel.setVisible(!isMinimized);
        toolbarPanel.setVisible(!isMinimized);
        contentPanel.setVisible(!isMinimized);
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
                setLocation(loc.x + e.getX() - dragPoint.x,
                            loc.y + e.getY() - dragPoint.y);
            }
        });
    }

    private JButton createCtrlButton(String txt) {
        JButton b = new JButton(txt);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setPreferredSize(new Dimension(30,30));
        b.setFocusPainted(false);
        return b;
    }

    private JButton createToolbarBtn(String txt, String tip) {
        JButton b = new JButton(txt);
        b.setToolTipText(tip);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 12f));
        b.setPreferredSize(new Dimension(30,30));
        b.setFocusPainted(false);
        return b;
    }

    public boolean isSent() { return sent; }
}
