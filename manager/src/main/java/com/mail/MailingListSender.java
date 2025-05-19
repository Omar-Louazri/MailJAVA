package com.mail;

import java.util.List;

public class MailingListSender {
	private final MailSender mailSender;

	public MailingListSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

    public void sendToGroup(String from, String groupEmail, String subject, String messageBody) {
		List<String> recipients;
		try {
			recipients = MailingListManager.getMembers(groupEmail);
		} catch (java.sql.SQLException e) {
			System.err.println("Erreur lors de la récupération des membres du groupe : " + e.getMessage());
			return;
		}

		for (String to : recipients) {
			Email mail = new Email();
			mail.setFrom(from);
			mail.addRecipient(to);
			mail.setSubject(subject);
			mail.setBody(messageBody);

			try {
				// send *and* save into `messages`
				mailSender.sendAndSave(mail, "SENT");
				System.out.println("Envoyé et sauvegardé à : " + to);
			} catch (Exception e) {
				System.err.println("Erreur pour " + to + " : " + e.getMessage());
			}
		}
	}

}