package com.mail;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class EmailTableModel extends AbstractTableModel {
    private List<Email> emails;
    private final String[] columnNames = {"De", "Sujet", "Date"};

    public EmailTableModel() {
        this.emails = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return emails.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Email email = emails.get(rowIndex);
        switch (columnIndex) {
            case 0: return email.getFrom();
            case 1: return email.getSubject();
            case 2: return email.getTime();
            default: return null;
        }
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
        fireTableDataChanged();
    }

    public Email getEmailAt(int row) {
        return emails.get(row);
    }
} 