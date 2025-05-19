package com.mail;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Dialogue de gestion complète des listes de diffusion :
 * création/suppression de groupes, ajout/retrait de membres et envoi de message.
 */
public class MailingListManagerDialog extends JDialog {
    private final MailingListManager manager;
    private final MailingListSender   listSender;
    private final MailSender          mailSender;

    private JComboBox<String> groupCombo;
    private DefaultListModel<String> memberModel;
    private JList<String>            memberList;

    private JTextField newGroupField;
    private JButton    createGroupBtn;
    private JButton    deleteGroupBtn;

    private JTextField newEmailField;
    private JButton    addEmailBtn;
    private JButton    removeEmailBtn;

    private JTextField subjectField;
    private JTextArea  bodyArea;

    public MailingListManagerDialog(Frame parent, MailSender mailSender) {
        super(parent, "Gestionnaire de Liste Diffusion", true);
        this.manager    = new MailingListManager();
        this.mailSender = mailSender;
        this.listSender = new MailingListSender(mailSender);

        initUI();
        loadGroups();
        pack();
        setSize(700, 550);
        setLocationRelativeTo(parent);
    }

        private void initUI() {
        setLayout(new BorderLayout(10,10));

        // --- Top: création et suppression de groupe (deux lignes pour visibilité) ---
        JPanel groupPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        groupPanel.setBorder(BorderFactory.createTitledBorder("Groupes"));

        // Ligne 1: création
        JPanel createRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        newGroupField   = new JTextField(15);
        createGroupBtn  = new JButton("Créer Groupe");
        createGroupBtn.addActionListener(e -> createGroup());
        createRow.add(new JLabel("Nouveau :"));
        createRow.add(newGroupField);
        createRow.add(createGroupBtn);
        groupPanel.add(createRow);

        // Ligne 2: sélection + suppression
        JPanel deleteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        groupCombo     = new JComboBox<>();
        groupCombo.setPreferredSize(new Dimension(180,25));
        deleteGroupBtn = new JButton("Supprimer Groupe");
        deleteGroupBtn.addActionListener(e -> deleteGroup());
        deleteRow.add(new JLabel("Sélection :"));
        deleteRow.add(groupCombo);
        deleteRow.add(deleteGroupBtn);
        groupPanel.add(deleteRow);

        add(groupPanel, BorderLayout.NORTH);


        // --- Center: liste des membres et boutons ajout/retrait ---
        JPanel memberPanel = new JPanel(new BorderLayout(5,5));
        memberPanel.setBorder(BorderFactory.createTitledBorder("Membres du groupe"));

        memberModel = new DefaultListModel<>();
        memberList  = new JList<>(memberModel);
        memberList.setVisibleRowCount(8);
        memberPanel.add(new JScrollPane(memberList), BorderLayout.CENTER);

        JPanel memBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8));
        newEmailField   = new JTextField(20);
        addEmailBtn     = new JButton("Ajouter E-mail");
        removeEmailBtn  = new JButton("Retirer E-mail");

        addEmailBtn.addActionListener(e -> addEmail());
        removeEmailBtn.addActionListener(e -> removeEmail());

        memBtnPanel.add(new JLabel("Adresse :"));
        memBtnPanel.add(newEmailField);
        memBtnPanel.add(addEmailBtn);
        memBtnPanel.add(removeEmailBtn);

        memberPanel.add(memBtnPanel, BorderLayout.SOUTH);
        add(memberPanel, BorderLayout.WEST);

        // --- Bottom: sujet, corps et envoi ---
        JPanel sendPanel = new JPanel(new BorderLayout(5,5));
        sendPanel.setBorder(BorderFactory.createTitledBorder("Envoi au groupe"));

        JPanel subjP = new JPanel(new BorderLayout(5,5));
        subjP.add(new JLabel("Sujet:"), BorderLayout.WEST);
        subjectField = new JTextField();
        subjP.add(subjectField, BorderLayout.CENTER);
        sendPanel.add(subjP, BorderLayout.NORTH);

        bodyArea = new JTextArea(8,40);
        bodyArea.setLineWrap(true);
        sendPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        JButton sendBtn = new JButton("Envoyer");
        sendBtn.addActionListener(e -> sendToGroup());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(sendBtn);
        sendPanel.add(btnP, BorderLayout.SOUTH);

        add(sendPanel, BorderLayout.CENTER);
    }

    private void loadGroups() {
        groupCombo.removeAllItems();
        try {
            List<String> groups = manager.getAllGroups();
            for (String g : groups) groupCombo.addItem(g);
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void createGroup() {
        String name = newGroupField.getText().trim();
        if (name.isEmpty()) return;
        try {
            manager.createGroup(name);
            newGroupField.setText("");
            loadGroups();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void deleteGroup() {
        String grp = (String) groupCombo.getSelectedItem();
        if (grp == null) return;
        try {
            manager.deleteGroup(grp);
            memberModel.clear();
            loadGroups();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadMembers() {
        String grp = (String) groupCombo.getSelectedItem();
        memberModel.clear();
        if (grp == null) return;
        try {
            List<String> emails = manager.getMembers(grp);
            for (String e : emails) memberModel.addElement(e);
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void addEmail() {
        String grp   = (String) groupCombo.getSelectedItem();
        String email = newEmailField.getText().trim();
        if (grp == null || email.isEmpty()) return;
        try {
            manager.addMember(grp, email);
            newEmailField.setText("");
            loadMembers();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void removeEmail() {
        String grp   = (String) groupCombo.getSelectedItem();
        String email = memberList.getSelectedValue();
        if (grp == null || email == null) return;
        try {
            manager.removeMember(grp, email);
            loadMembers();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void sendToGroup() {
        String grp = (String) groupCombo.getSelectedItem();
        if (grp == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un groupe", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String subj = subjectField.getText().trim();
        if (subj.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le sujet ne peut pas être vide", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String body = bodyArea.getText();
        listSender.sendToGroup(mailSender.getUsername(), grp, subj, body);
        JOptionPane.showMessageDialog(this, "Messages envoyés au groupe ‘" + grp + "’", "Succès", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
