package com.mail;

import javax.swing.JOptionPane;

public class Deconexion {

    private final String email;
    private final String password;

    public Deconexion(String email, String password) {
        this.email    = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void deconnexion() {
        // Logique de déconnexion
       int res = JOptionPane.showConfirmDialog(null, "Voulez-vous vraiment vous déconnecter?", "Déconnexion", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                dispose();
                new LoginUI().setVisible(true);
            }




    }

    public void dispose() {
        doDispose();
    }
    private void doDispose() {
        // Logique de nettoyage ou de fermeture de la fenêtre
        System.out.println("Fermeture de la fenêtre.");
        // Par exemple, si vous avez une référence à la fenêtre principale :
        //mainFrame.dispose();
        // Note: Deconexion n'est pas un JFrame, donc ne peut pas appeler dispose() sur lui-même
    }

    
}
