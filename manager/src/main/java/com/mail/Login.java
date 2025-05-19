package com.mail;

/**
 * Modèle pour les informations de connexion.
 */
public class Login {
    private final String email;
    private final String password;

    public Login(String email, String password) {
        this.email    = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
