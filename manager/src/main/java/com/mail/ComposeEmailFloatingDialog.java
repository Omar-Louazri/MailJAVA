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
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

public class ComposeEmailFloatingDialog extends JDialog {
    private static final int MINIMIZED_HEIGHT = 40;
    private static final int EXPANDED_HEIGHT  = 500;
    private static final int WIDTH            = 400;

    private final MailSender mailSender;
    private JTextField toField;
    private JTextField subjectField;
    private JTextArea  bodyArea;
    private JButton    sendButton;
    private JButton    minimizeButton;
    private JButton    closeButton;
    private JPanel     headerPanel;
    private JPanel     contentPanel;
    private boolean    isMinimized = false;
    private boolean    sent        = false;
    private Point      dragPoint;

    public ComposeEmailFloatingDialog(Frame parent, MailSender mailSender) {
        super(parent, false); // non-modal
        this.mailSender = mailSender;

        setUndecorated(true);
        initializeComponents();
        setupDragAndDrop();

        setSize(WIDTH, EXPANDED_HEIGHT);
        setLocationRelativeTo(parent);
        setLocation(getX() + 100, getY() - 100);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(220, 220, 220));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel titleLabel = new JLabel("New Message");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        minimizeButton = new JButton("−");
        minimizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        minimizeButton.setPreferredSize(new Dimension(25, 25));
        minimizeButton.setFocusPainted(false);
        minimizeButton.setBorderPainted(false);
        minimizeButton.setContentAreaFilled(false);

        closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);

        ctrl.add(minimizeButton);
        ctrl.add(closeButton);
        headerPanel.add(ctrl, BorderLayout.EAST);

        // Content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,5,5,5);

        // To
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("To:"), gbc);
        gbc.gridx   = 1; gbc.weightx = 1.0;
        toField     = new JTextField(20);
        form.add(toField, gbc);

        // Subject
        gbc.gridx   = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Subject:"), gbc);
        gbc.gridx   = 1; gbc.weightx = 1.0;
        subjectField = new JTextField(20);
        form.add(subjectField, gbc);

        // Body
        gbc.gridx   = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty   = 1.0;
        gbc.fill      = GridBagConstraints.BOTH;
        bodyArea      = new JTextArea(10, 20);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        form.add(new JScrollPane(bodyArea), gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 120, 212));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        btnPanel.add(sendButton);

        contentPanel.add(form, BorderLayout.CENTER);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        // Add to dialog
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Listeners
        minimizeButton.addActionListener(e -> toggleMinimize());
        closeButton.addActionListener(e -> dispose());
        sendButton.addActionListener(e -> performSend());

        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                headerPanel.setBackground(new Color(200,200,200));
            }
            public void windowDeactivated(WindowEvent e) {
                headerPanel.setBackground(new Color(220,220,220));
            }
        });
    }

    private void performSend() {
        String to      = toField.getText().trim();
        String subject = subjectField.getText();
        String body    = bodyArea.getText();

        if (to.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a recipient address.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // From address is your authenticated username:
            mailSender.sendEmail(
                mailSender.getUsername(), // your Gmail address
                to,
                subject,
                body
            );
            sent = true;
            dispose();
        } catch (MessagingException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to send email:\n" + ex.getMessage(),
                "Send Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleMinimize() {
        isMinimized = !isMinimized;
        if (isMinimized) {
            setSize(WIDTH, MINIMIZED_HEIGHT);
            contentPanel.setVisible(false);
            minimizeButton.setText("□");
        } else {
            setSize(WIDTH, EXPANDED_HEIGHT);
            contentPanel.setVisible(true);
            minimizeButton.setText("−");
        }
    }

    private void setupDragAndDrop() {
        headerPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragPoint = e.getPoint();
            }
        });
        headerPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - dragPoint.x,
                            loc.y + e.getY() - dragPoint.y);
            }
        });
    }

    public boolean isSent() {
        return sent;
    }
}
