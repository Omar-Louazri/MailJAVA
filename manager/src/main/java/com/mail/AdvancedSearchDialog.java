package com.mail;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.YearMonth;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog allowing advanced search by text, date, and case sensitivity.
 */
public class AdvancedSearchDialog extends JDialog {
    private JTextField searchField;
    private JCheckBox matchCaseCheckBox;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private boolean searchPerformed = false;
    private String searchText;
    private YearMonth selectedDate;
    private boolean matchCase;

    public AdvancedSearchDialog(Frame parent) {
        super(parent, "Recherche avancée", true);
        initializeComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Rechercher dans les messages :"), gbc);

        gbc.gridy = 1; gbc.gridwidth = 2;
        searchField = new JTextField(20);
        mainPanel.add(searchField, gbc);

        gbc.gridy = 2; gbc.gridwidth = 2;
        matchCaseCheckBox = new JCheckBox("Respecter la casse");
        mainPanel.add(matchCaseCheckBox, gbc);

        gbc.gridy = 3; gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Filtrer par date :"), gbc);

        gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yearComboBox = new JComboBox<>(generateYears());
        monthComboBox = new JComboBox<>(new String[]{
            "Tous les mois", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        });
        datePanel.add(new JLabel("Année :")); datePanel.add(yearComboBox);
        datePanel.add(new JLabel("Mois :")); datePanel.add(monthComboBox);
        mainPanel.add(datePanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton searchButton = new JButton("Rechercher");
        JButton cancelButton = new JButton("Annuler");
        searchButton.addActionListener(e -> performSearch());
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(searchButton); buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private Integer[] generateYears() {
        int currentYear = java.time.Year.now().getValue();
        Integer[] years = new Integer[5];
        for (int i = 0; i < 5; i++) years[i] = currentYear - i;
        return years;
    }

    private void performSearch() {
        searchText = searchField.getText().trim();
        matchCase = matchCaseCheckBox.isSelected();
        int year = (Integer) yearComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex();
        selectedDate = month > 0 ? YearMonth.of(year, month) : null;
        searchPerformed = true;
        dispose();
    }

    public boolean isSearchPerformed() { return searchPerformed; }
    public String getSearchText() { return searchText; }
    public YearMonth getSelectedDate() { return selectedDate; }
    public boolean isMatchCase() { return matchCase; }
}