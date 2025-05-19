package com.mail;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class MainFrame extends JFrame {
    // Adresse mail de l'utilisateur connecté
    private final String userEmail;

    // UI components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel detailsPanel;
    private JTable emailTable;
    private JTextArea emailBody;

    // Recherche inline
    private JTextField searchField;
    private JComboBox<String> cbYear;
    private JComboBox<String> cbMonth;
    private JCheckBox chkMatchCase;
    private JButton filterButton;
    private JButton refreshButton;

    // Modèle et données
    private EmailTableModel emailTableModel;
    private List<Email> allEmails;
    private boolean isViewingSentEmails = false;

    // Mail sender
    private final MailSender mailSender;

    // DB config
    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/jamesdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "1234";

    // Dialogues
    private ComposeEmailDialog composeDialog;

    public MainFrame(String userEmail) {
        this.userEmail = userEmail;
        this.mailSender = new MailSender(
            "smtp.gmail.com", "587",
            userEmail, "zqjd meaz wuzj yflc"
        );

        this.emailTableModel = new EmailTableModel();
        this.allEmails       = new ArrayList<>();

        setTitle("Gestionnaire de Courriel – " + userEmail);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initializeSidebar();
        initializeContent();
        initializeDetails();

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.EAST);

        loadInboxMessages();
    }

    private void initializeSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebarPanel.setPreferredSize(new Dimension(160, 0));

        JButton composeBtn      = createSidebarButton("Nouveau message");
        JButton inboxBtn        = createSidebarButton("Boîte de réception");
        JButton sentBtn         = createSidebarButton("Messages envoyés");
        JButton mailingListsBtn = createSidebarButton("Listes de diffusion");
        JButton schedulerBtn    = createSidebarButton("Scheduler");
        JButton settingsBtn     = createSidebarButton("Paramètres");

        composeBtn.addActionListener(e -> showComposeDialog());
        inboxBtn.addActionListener(e -> { isViewingSentEmails = false; loadInboxMessages(); });
        sentBtn.addActionListener(e -> { isViewingSentEmails = true;  loadSentMessages(); });
        mailingListsBtn.addActionListener(e -> new MailingListManagerDialog(this, mailSender).setVisible(true));
        schedulerBtn.addActionListener(e -> {
            SchedulerPanel panel = new SchedulerPanel(mailSender);
            JDialog dlg = new JDialog(this, "Email Scheduler", true);
            dlg.getContentPane().add(panel);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });
        settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fonctionnalité à venir!"));

        sidebarPanel.add(composeBtn);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(inboxBtn);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(sentBtn);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(mailingListsBtn);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(schedulerBtn);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(settingsBtn);
        sidebarPanel.add(Box.createVerticalGlue());
    }

    private JButton createSidebarButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(140, 32));
        b.setPreferredSize(new Dimension(140, 32));
        return b;
    }

    private void initializeContent() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: inline search/filter
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchField = new JTextField(15);
        cbYear      = new JComboBox<>();
        cbMonth     = new JComboBox<>();
        chkMatchCase= new JCheckBox("Case-sensitive");
        filterButton= new JButton("Filter");
        refreshButton = new JButton("Rafraîchir");

        top.add(new JLabel("Search:")); top.add(searchField);
        top.add(new JLabel("Year:"));   top.add(cbYear);
        top.add(new JLabel("Month:"));  top.add(cbMonth);
        top.add(chkMatchCase);
        top.add(filterButton);
        top.add(refreshButton);

        // Populate year/month
        cbYear.addItem("All");
        for (int y=2025; y>=2000; y--) cbYear.addItem(String.valueOf(y));
        cbMonth.addItem("All");
        for (int m=1; m<=12; m++) cbMonth.addItem(YearMonth.of(2000,m).getMonth().name());

        filterButton.addActionListener(e -> applyFilter());
        refreshButton.addActionListener(e -> {
            if (isViewingSentEmails) loadSentMessages();
            else loadInboxMessages();
        });

        contentPanel.add(top, BorderLayout.NORTH);

        // Table
        emailTable = new JTable(emailTableModel);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = emailTable.getSelectedRow();
                if (r>=0) displayEmailDetails(emailTableModel.getEmailAt(r));
            }
        });
        contentPanel.add(new JScrollPane(emailTable), BorderLayout.CENTER);
    }

    private void initializeDetails() {
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        detailsPanel.setPreferredSize(new Dimension(300,0));

        JPanel hdr = new JPanel(new GridLayout(3,1));
        hdr.add(new JLabel("Sujet: "));
        hdr.add(new JLabel("De:    "));
        hdr.add(new JLabel("Date:  "));
        detailsPanel.add(hdr, BorderLayout.NORTH);

        emailBody = new JTextArea();
        emailBody.setEditable(false); emailBody.setLineWrap(true); emailBody.setWrapStyleWord(true);
        detailsPanel.add(new JScrollPane(emailBody), BorderLayout.CENTER);
    }

    private void displayEmailDetails(Email e) {
        JPanel hdr = (JPanel) detailsPanel.getComponent(0);
        JLabel subj = (JLabel) hdr.getComponent(0);
        JLabel frm  = (JLabel) hdr.getComponent(1);
        JLabel date = (JLabel) hdr.getComponent(2);

        subj.setText("Sujet: " + e.getSubject());
        if (isViewingSentEmails) frm.setText("À:     " + String.join(",", e.getRecipients()));
        else                    frm.setText("De:    " + e.getFrom());
        date.setText("Date:  " + e.getTime());

        emailBody.setText(e.getBody());
    }

    private void loadInboxMessages() {
        allEmails.clear();
        String sql = "SELECT sender, recipients, subject, body, received_at " +
                     "FROM messages WHERE recipients LIKE ? ORDER BY sender DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%"+userEmail+"%");
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()) {
                    Email e = new Email();
                    e.setFrom(rs.getString("sender"));
                    for (String r: rs.getString("recipients").split(",")) if (!r.isBlank()) e.addRecipient(r.trim());
                    e.setSubject(rs.getString("subject"));
                    e.setBody(rs.getString("body"));
                    e.setTime(rs.getTimestamp("received_at").toLocalDateTime());
                    allEmails.add(e);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur DB: "+ex.getMessage(),"Erreur",JOptionPane.ERROR_MESSAGE);
        }
        emailTableModel.setEmails(allEmails);
    }

    private void loadSentMessages() {
        allEmails.clear();
        String sql = "SELECT sender, recipients, subject, body, received_at " +
                     "FROM messages WHERE sender = ? ORDER BY recipients DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()) {
                    Email e = new Email();
                    e.setFrom(rs.getString("sender"));
                    for (String r: rs.getString("recipients").split(",")) if (!r.isBlank()) e.addRecipient(r.trim());
                    e.setSubject(rs.getString("subject"));
                    e.setBody(rs.getString("body"));
                    e.setTime(rs.getTimestamp("received_at").toLocalDateTime());
                    allEmails.add(e);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur DB: "+ex.getMessage(),"Erreur",JOptionPane.ERROR_MESSAGE);
        }
        emailTableModel.setEmails(allEmails);
    }

    private void applyFilter() {
        String txt   = searchField.getText().trim().toLowerCase();
        Integer yr   = cbYear.getSelectedIndex()==0?null:Integer.valueOf((String)cbYear.getSelectedItem());
        Integer mo   = cbMonth.getSelectedIndex()==0?null:cbMonth.getSelectedIndex();
        boolean cs   = chkMatchCase.isSelected();

        List<Email> filtered = new ArrayList<>();
        for (Email e: allEmails) {
            String hay = (e.getSubject()+" "+e.getBody()).toLowerCase();
            if ((!cs && !hay.contains(txt)) || (cs && !(e.getSubject()+" "+e.getBody()).contains(searchField.getText()))) {
                continue;
            }
            if (yr!=null && e.getTime().getYear()!=yr) continue;
            if (mo!=null && e.getTime().getMonthValue()!=mo) continue;
            filtered.add(e);
        }
        emailTableModel.setEmails(filtered);
    }

  // Dans MainFrame.java
    private void showComposeDialog() {
        // on passe uniquement parent + MailSender
        composeDialog = new ComposeEmailDialog(this, mailSender);
        composeDialog.setVisible(true);
    }

}
