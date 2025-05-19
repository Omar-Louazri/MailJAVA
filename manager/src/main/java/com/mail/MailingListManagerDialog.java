package com.mail;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialogue de gestion complète des listes de diffusion :
 * création/suppression de groupes, ajout/retrait de membres et envoi de message.
 */
public class MailingListManagerDialog extends JDialog {

    /* ──────────────────────────── Services ──────────────────────────── */
    private final MailingListManager manager;
    private final MailingListSender  listSender;
    private final MailSender         mailSender;

    /* ──────────────────────────── Widgets ───────────────────────────── */
    private JComboBox<String>   groupCombo;
    private DefaultListModel<String> memberModel;
    private JList<String>       memberList;

    private JTextField newGroupField;
    private JButton    createGroupBtn;
    private JButton    deleteGroupBtn;

    private JTextField newEmailField;
    private JButton    addEmailBtn;
    private JButton    removeEmailBtn;

    private JTextField subjectField;
    private JTextArea  bodyArea;

    /* ──────────────────────────── Ctor ──────────────────────────────── */
    public MailingListManagerDialog(Frame parent, MailSender mailSender) {
        super(parent, "Gestionnaire de Liste Diffusion", true);

        this.manager    = new MailingListManager();
        this.mailSender = mailSender;
        this.listSender = new MailingListSender(mailSender);

        initUI();
        loadGroups();                  // Asynchrone → remplit la combo
        pack();
        setSize(750, 560);
        setLocationRelativeTo(parent);
    }

    /* ─────────────────────────── Interface ──────────────────────────── */
    private void initUI() {
        setLayout(new BorderLayout(10,10));

        /* -------- Top : groupes -------- */
        JPanel groupPanel = new JPanel(new GridLayout(2,1,5,5));
        groupPanel.setBorder(BorderFactory.createTitledBorder("Groupes"));

        /*  - création */
        JPanel createRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        newGroupField  = new JTextField(15);
        createGroupBtn = new JButton("Créer Groupe");
        createGroupBtn.addActionListener(e -> createGroup());
        createRow.add(new JLabel("Nouveau :"));
        createRow.add(newGroupField);
        createRow.add(createGroupBtn);
        groupPanel.add(createRow);

        /*  - sélection / suppression */
        JPanel deleteRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        groupCombo     = new JComboBox<>();
        groupCombo.setPreferredSize(new Dimension(200,25));
        groupCombo.addActionListener(e -> loadMembers());   // ← important
        deleteGroupBtn = new JButton("Supprimer Groupe");
        deleteGroupBtn.addActionListener(e -> deleteGroup());
        deleteRow.add(new JLabel("Sélection :"));
        deleteRow.add(groupCombo);
        deleteRow.add(deleteGroupBtn);
        groupPanel.add(deleteRow);

        add(groupPanel, BorderLayout.NORTH);

        /* -------- Centre : membres -------- */
        JPanel memberPanel = new JPanel(new BorderLayout(5,5));
        memberPanel.setBorder(BorderFactory.createTitledBorder("Membres du groupe"));

        memberModel = new DefaultListModel<>();
        memberList  = new JList<>(memberModel);
        memberList.setVisibleRowCount(10);
        memberPanel.add(new JScrollPane(memberList), BorderLayout.CENTER);

        JPanel memBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        newEmailField  = new JTextField(22);
        addEmailBtn    = new JButton("Ajouter E‑mail");
        removeEmailBtn = new JButton("Retirer E‑mail");
        addEmailBtn.addActionListener(e -> addEmail());
        removeEmailBtn.addActionListener(e -> removeEmail());

        memBtnPanel.add(new JLabel("Adresse :"));
        memBtnPanel.add(newEmailField);
        memBtnPanel.add(addEmailBtn);
        memBtnPanel.add(removeEmailBtn);

        memberPanel.add(memBtnPanel, BorderLayout.SOUTH);
        add(memberPanel, BorderLayout.WEST);

        /* -------- Bas : envoi -------- */
        JPanel sendPanel = new JPanel(new BorderLayout(5,5));
        sendPanel.setBorder(BorderFactory.createTitledBorder("Envoi au groupe"));

        JPanel subjP = new JPanel(new BorderLayout(5,5));
        subjP.add(new JLabel("Sujet :"), BorderLayout.WEST);
        subjectField = new JTextField();
        subjP.add(subjectField, BorderLayout.CENTER);
        sendPanel.add(subjP, BorderLayout.NORTH);

        bodyArea = new JTextArea(8,40);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        sendPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        JButton sendBtn = new JButton("Envoyer");
        sendBtn.addActionListener(e -> sendToGroup());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(sendBtn);
        sendPanel.add(btnP, BorderLayout.SOUTH);

        add(sendPanel, BorderLayout.CENTER);
    }

    /* ────────────────────── Chargement asynchrone ───────────────────── */
    /** Charge les groupes en arrière‑plan. */
    private void loadGroups() {
        new SwingWorker<List<String>,Void>() {
            @Override protected List<String> doInBackground() throws Exception {
                return MailingListManager.getAllGroups();
            }
            @Override protected void done() {
                try {
                    List<String> groups = get();
                    groupCombo.removeAllItems();
                    for (String g : groups) groupCombo.addItem(g);
                    if (groupCombo.getItemCount() > 0) {
                        groupCombo.setSelectedIndex(0);      // déclenche loadMembers()
                    } else {
                        memberModel.clear();
                    }
                } catch (Exception ex) {
                    showError(ex);
                }
            }
        }.execute();
    }

    /** Charge les membres du groupe courant en arrière‑plan. */
    private void loadMembers() {
        String grp = (String) groupCombo.getSelectedItem();
        memberModel.clear();
        if (grp == null) return;

        new SwingWorker<List<String>,Void>() {
            @Override protected List<String> doInBackground() throws Exception {
                return MailingListManager.getMembers(grp);
            }
            @Override protected void done() {
                try {
                    for (String m : get()) memberModel.addElement(m);
                } catch (Exception ex) {
                    showError(ex);
                }
            }
        }.execute();
    }

    /* ─────────────────────────── Actions CRUD ───────────────────────── */
    private void createGroup() {
        String name = newGroupField.getText().trim();
        if (name.isEmpty()) return;
        newGroupField.setText("");

        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                MailingListManager.createGroup(name);
                return null;
            }
            @Override protected void done() { loadGroups(); }
        }.execute();
    }

    private void deleteGroup() {
        String grp = (String) groupCombo.getSelectedItem();
        if (grp == null) return;

        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                MailingListManager.deleteGroup(grp);
                return null;
            }
            @Override protected void done() { loadGroups(); }
        }.execute();
    }

    private void addEmail() {
        String grp   = (String) groupCombo.getSelectedItem();
        String email = newEmailField.getText().trim();
        if (grp == null || email.isEmpty()) return;
        newEmailField.setText("");

        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                MailingListManager.addMember(grp, email);
                return null;
            }
            @Override protected void done() { loadMembers(); }
        }.execute();
    }

    private void removeEmail() {
        String grp   = (String) groupCombo.getSelectedItem();
        String email = memberList.getSelectedValue();
        if (grp == null || email == null) return;

        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                MailingListManager.removeMember(grp, email);
                return null;
            }
            @Override protected void done() { loadMembers(); }
        }.execute();
    }

    /* ─────────────────────────── Envoi mail ─────────────────────────── */
    private void sendToGroup() {
        String grp = (String) groupCombo.getSelectedItem();
        if (grp == null) {
            JOptionPane.showMessageDialog(this,"Sélectionnez un groupe","Info",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String subj = subjectField.getText().trim();
        if (subj.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Le sujet ne peut pas être vide","Attention",JOptionPane.WARNING_MESSAGE);
            return;
        }
        String body = bodyArea.getText();

        // Envoi en tâche de fond pour garder l’IU réactive
        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() {
                listSender.sendToGroup(mailSender.getUsername(), grp, subj, body);
                return null;
            }
            @Override protected void done() {
                JOptionPane.showMessageDialog(MailingListManagerDialog.this,
                        "Messages envoyés au groupe « "+grp+" ».",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        }.execute();
    }

    /* ───────────────────────── Outils IHM ───────────────────────────── */
    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
