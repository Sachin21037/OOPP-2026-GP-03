package dao;

import database.DBConnection;
import model.AdminDashboardStats;
import model.RecentActivityItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for retrieving administrative dashboard metrics and recent records from MySQL.
 */
public class AdminDashboardDAO {

    /**
     * Retrieves aggregated statistics from the database tables.
     * Queries undergraduates, lecturers, technical_officers, courses, and notices.
     *
     * @return AdminDashboardStats populated with real database counts
     */
    public AdminDashboardStats getDashboardStatistics() {
        AdminDashboardStats stats = new AdminDashboardStats();

        try (Connection conn = DBConnection.getConnection()) {
            stats.setDbConnected(true);

            // 1. Total Students (from undergraduates table, fallback to users table)
            stats.setTotalStudents(executeCountQuery(conn,
                    "SELECT COUNT(*) FROM undergraduates",
                    "SELECT COUNT(*) FROM users WHERE role = 'UNDERGRADUATE' AND status = 'ACTIVE'"));

            // 2. Total Lecturers (from lecturers table, fallback to users table)
            stats.setTotalLecturers(executeCountQuery(conn,
                    "SELECT COUNT(*) FROM lecturers",
                    "SELECT COUNT(*) FROM users WHERE role = 'LECTURER' AND status = 'ACTIVE'"));

            // 3. Total Technical Officers (from technical_officers table, fallback to users table)
            stats.setTotalTechnicalOfficers(executeCountQuery(conn,
                    "SELECT COUNT(*) FROM technical_officers",
                    "SELECT COUNT(*) FROM users WHERE role = 'TECHNICAL_OFFICER' AND status = 'ACTIVE'"));

            // 4. Total Courses (from courses table)
            stats.setTotalCourses(executeCountQuery(conn,
                    "SELECT COUNT(*) FROM courses",
                    null));

            // 5. Total Notices (from notices table)
            stats.setTotalNotices(executeCountQuery(conn,
                    "SELECT COUNT(*) FROM notices WHERE status = 'ACTIVE'",
                    "SELECT COUNT(*) FROM notices"));

            // Database version metadata
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
                if (rs.next()) {
                    stats.setDatabaseVersion(rs.getString(1));
                }
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.err.println("[AdminDashboardDAO ERROR] Failed to fetch dashboard statistics: " + e.getMessage());
            stats.setDbConnected(false);
        }

        return stats;
    }

    /**
     * Helper to safely execute a count query with an optional fallback query.
     */
    private int executeCountQuery(Connection conn, String primarySql, String fallbackSql) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(primarySql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException primaryEx) {
            if (fallbackSql != null) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(fallbackSql)) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                } catch (SQLException ignored) {}
            }
            System.err.println("[AdminDashboardDAO Query Warning] Could not execute count query: " + primaryEx.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves recent notices published in the system.
     *
     * @param limit maximum records to retrieve
     * @return List of RecentActivityItem
     */
    public List<RecentActivityItem> getRecentNotices(int limit) {
        List<RecentActivityItem> list = new ArrayList<>();
        String sql = "SELECT notice_id, title, description, notice_date, status " +
                     "FROM notices ORDER BY notice_date DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Math.max(1, limit));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("notice_id");
                    String title = rs.getString("title");
                    String desc = rs.getString("description");
                    String date = rs.getString("notice_date");
                    String status = rs.getString("status");

                    list.add(new RecentActivityItem(id, title, desc, date, "NOTICE", status));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminDashboardDAO ERROR] Failed to fetch recent notices: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves recent user registrations from the users table.
     *
     * @param limit maximum records to retrieve
     * @return List of RecentActivityItem
     */
    public List<RecentActivityItem> getRecentUserRegistrations(int limit) {
        List<RecentActivityItem> list = new ArrayList<>();
        String sql = "SELECT user_id, first_name, last_name, role, created_at, status " +
                     "FROM users ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Math.max(1, limit));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("user_id");
                    String name = rs.getString("first_name") + " " + rs.getString("last_name");
                    String role = rs.getString("role");
                    String createdAt = rs.getString("created_at");
                    String status = rs.getString("status");

                    String desc = "Role: " + role + " (" + id + ")";
                    list.add(new RecentActivityItem(id, name, desc, createdAt, "USER", status));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminDashboardDAO ERROR] Failed to fetch recent user registrations: " + e.getMessage());
        }

        return list;
    }
}
