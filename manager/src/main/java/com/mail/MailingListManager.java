package com.mail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MailingListManager {
    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/jamesdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "1234";

    public static void createGroup(String groupEmail) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO mailinglist (groupmail, emails) VALUES (?, ?)");
            stmt.setString(1, groupEmail);
            stmt.setString(2, ""); // initially empty list
            stmt.executeUpdate();
            System.out.println("Group created: " + groupEmail);
        }
    }

    public static void addMember(String groupEmail, String newEmail) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Fetch current list
            PreparedStatement select = conn.prepareStatement(
                "SELECT emails FROM mailinglist WHERE groupmail = ?");
            select.setString(1, groupEmail);
            ResultSet rs = select.executeQuery();
            if (!rs.next()) throw new SQLException("Group not found: " + groupEmail);

            String currentEmails = rs.getString("emails");
            Set<String> emailSet = new HashSet<>(Arrays.asList(currentEmails.split(",")));
            emailSet.add(newEmail.trim());

            // Clean up empty entries
            emailSet.removeIf(e -> e.trim().isEmpty());

            String updatedEmails = String.join(",", emailSet);

            // Update the list
            PreparedStatement update = conn.prepareStatement(
                "UPDATE mailinglist SET emails = ? WHERE groupmail = ?");
            update.setString(1, updatedEmails);
            update.setString(2, groupEmail);
            update.executeUpdate();
            System.out.println("Added " + newEmail + " to group " + groupEmail);
        }
    }

    public static List<String> getMembers(String groupEmail) throws SQLException {
        List<String> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT emails FROM mailinglist WHERE groupmail = ?");
            stmt.setString(1, groupEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String emailStr = rs.getString("emails");
                if (emailStr != null && !emailStr.isBlank()) {
                    result = Arrays.asList(emailStr.split(","));
                }
            }
        }
        return result;
    }

    public static List<String> getAllGroups() throws SQLException {
        List<String> groups = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT groupmail FROM mailinglist");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                groups.add(rs.getString("groupmail"));
            }
        }
        return groups;
    }

    /**
     * Deletes an entire mailing list group.
     */
    public static void deleteGroup(String groupEmail) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM mailinglist WHERE groupmail = ?");
            stmt.setString(1, groupEmail);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Group deleted: " + groupEmail);
            } else {
                System.out.println("No group found with email: " + groupEmail);
            }
        }
    }

    /**
     * Removes a single email address from an existing group.
     */
    public static void removeMember(String groupEmail, String emailToRemove) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Fetch current list
            PreparedStatement select = conn.prepareStatement(
                "SELECT emails FROM mailinglist WHERE groupmail = ?");
            select.setString(1, groupEmail);
            ResultSet rs = select.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Group not found: " + groupEmail);
            }

            String currentEmails = rs.getString("emails");
            Set<String> emailSet = new HashSet<>(Arrays.asList(currentEmails.split(",")));
            boolean removed = emailSet.remove(emailToRemove.trim());

            if (!removed) {
                System.out.println(emailToRemove + " not found in group " + groupEmail);
                return;
            }

            // Clean up empty entries
            emailSet.removeIf(e -> e.trim().isEmpty());

            String updatedEmails = String.join(",", emailSet);

            // Update database
            PreparedStatement update = conn.prepareStatement(
                "UPDATE mailinglist SET emails = ? WHERE groupmail = ?");
            update.setString(1, updatedEmails);
            update.setString(2, groupEmail);
            update.executeUpdate();
            System.out.println("Removed " + emailToRemove + " from group " + groupEmail);
        }
    }
}
