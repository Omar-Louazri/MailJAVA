package com.mail;

import java.util.Properties;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;

public class MailReceiver {
	private final String host;
	private final String port;
	private final String username;
	private final String password;

	public MailReceiver(String host, String port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public void fetchUnreadEmails() throws MessagingException {
		Properties props = new Properties();
		props.put("mail.store.protocol", "imaps"); // Gmail needs SSL
		props.put("mail.imap.ssl.enable", "true");
		props.put("mail.imap.host", host);
		props.put("mail.imap.port", port);

		Session session = Session.getInstance(props);
		Store store = session.getStore("imaps");
		store.connect(host, username, password);

		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);

		Message[] messages = inbox.getMessages();
		System.out.println("Emails non lus (ou tous) : " + messages.length);
		for (Message message : messages) {
			if (!message.isSet(Flags.Flag.SEEN)) {
				System.out.println("-----");
				System.out.println("De : " + message.getFrom()[0]);
				System.out.println("Sujet : " + message.getSubject());
			}
		}

		inbox.close(false);
		store.close();
	}
}