package com.mail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents an email message with metadata for UI and persistence.
 */
public class Email {
    private String id;
    private String subject;
    private String body;
    private String from;
    private List<String> recipients;
    private LocalDateTime time;
    private List<String> attachments;
    private List<String> smsNumbers;
    private List<String> mailingLists;
    private boolean isRead;
    private boolean isArchived;

    public Email() {
        this.id = UUID.randomUUID().toString();
        this.recipients    = new ArrayList<>();
        this.attachments   = new ArrayList<>();
        this.smsNumbers    = new ArrayList<>();
        this.mailingLists  = new ArrayList<>();
        this.time          = LocalDateTime.now();
        this.isRead        = false;
        this.isArchived    = false;
    }

    // Unique identifier
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    /**
     * Returns an unmodifiable view of recipients.
     */
    public List<String> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    /**
     * Replace entire recipient list.
     */
    public void setRecipients(List<String> recipients) {
        this.recipients = new ArrayList<>(recipients);
    }

    /**
     * Adds a single recipient to the email.
     */
    public void addRecipient(String recipient) {
        if (recipient != null && !recipient.isBlank()) {
            recipients.add(recipient.trim());
        }
    }

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    /**
     * Attachments list (file paths or URLs).
     */
    public List<String> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }
    public void setAttachments(List<String> attachments) {
        this.attachments = new ArrayList<>(attachments);
    }
    public void addAttachment(String path) {
        if (path != null && !path.isBlank()) attachments.add(path);
    }

    /**
     * SMS numbers included in this email flow.
     */
    public List<String> getSmsNumbers() {
        return Collections.unmodifiableList(smsNumbers);
    }
    public void addSmsNumber(String number, String provider) {
        if (number != null && !number.isBlank()) {
            smsNumbers.add(number.trim() + (provider != null ? " (" + provider + ")" : ""));
        }
    }

    /**
     * Mailing lists addressed by this email.
     */
    public List<String> getMailingLists() {
        return Collections.unmodifiableList(mailingLists);
    }
    public void addMailingList(String alias) {
        if (alias != null && !alias.isBlank()) mailingLists.add(alias.trim());
    }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
}
