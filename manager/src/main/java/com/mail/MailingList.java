package com.mail;

import java.util.ArrayList;
import java.util.List;

public class MailingList {
    private String name;
    private List<String> emailAddresses;

    public MailingList(String name) {
        this.name = name;
        this.emailAddresses = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEmailAddresses() {
        return new ArrayList<>(emailAddresses);
    }

    public void addEmailAddress(String email) {
        if (!emailAddresses.contains(email)) {
            emailAddresses.add(email);
        }
    }

    public void removeEmailAddress(String email) {
        emailAddresses.remove(email);
    }
} 