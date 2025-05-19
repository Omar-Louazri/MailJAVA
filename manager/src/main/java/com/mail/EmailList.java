package com.mail;

import java.util.ArrayList;


public class EmailList {
	protected ArrayList<Email> emails;

	public EmailList() {
		emails = new ArrayList<Email>();
	}

	public ArrayList<Email> getEmails() {
		return emails;
	}

	public void setEmails(ArrayList<Email> emails) {
		this.emails = emails;
	}

	
	public void addEmail(Email e) {
		emails.add(e);
	}

	public ArrayList<Email> filterAndSort() {
		
		return emails;
	}
}
