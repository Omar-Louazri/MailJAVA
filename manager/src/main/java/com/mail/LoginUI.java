package com.mail;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginUI extends JFrame {
    private final JTextField emailField;
    private final JPasswordField passField;

    public LoginUI() {
        super("Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 180);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(2,2,5,5));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        form.add(new JLabel("Adresse mail :"));
        emailField = new JTextField();
        form.add(emailField);
        form.add(new JLabel("Mot de passe :"));
        passField  = new JPasswordField();
        form.add(passField);
        add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Se connecter");
        loginBtn.addActionListener(e -> doLogin());
        JPanel btnP = new JPanel();
        btnP.add(loginBtn);
        add(btnP, BorderLayout.SOUTH);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passField.getPassword()).trim();
        // we ignore password validity for now
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez saisir votre mail.",
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Launch the main app with this email
        SwingUtilities.invokeLater(() -> {
            MainFrame main = new MainFrame(email, password);
            main.setVisible(true);
        });
        dispose();
    }

    public static void main(String[] args) {
        // just to test LoginUI in isolation
        SwingUtilities.invokeLater(() -> {
            new LoginUI().setVisible(true);
        });
    }
}
