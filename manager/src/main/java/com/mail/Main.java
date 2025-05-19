package com.mail;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Applique le Look & Feel système
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Démarre la UI sur l'Event-Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Ouvre l'écran de login, qui lancera ensuite MainFrame
            new LoginUI().setVisible(true);
        });
    }
}
