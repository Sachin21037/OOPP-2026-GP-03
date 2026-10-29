package dao;

import model.Notice;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Academic Notices in FAMS.
 * Interfaces with the 'notices' table and joins with 'users'.
 * Provides high-fidelity in-memory fallback when MySQL is offline.
 */
public class NoticeDAO {

    private final List<Notice> mockNotices = new ArrayList<>();

    public NoticeDAO() {
        initMockData();
    }

    private void initMockData() {
        mockNotices.add(new Notice("NOT001", "Mid Semester Examination Notice",
                "Mid semester examinations will be conducted according to the published timetable. All eligible students must bring their university ID.",
                Date.valueOf("2026-03-01"), "ADM001", "ACTIVE", "System Administrator", "admin001"));
        mockNotices.add(new Notice("NOT002", "Assignment Submission",
                "Students are requested to submit pending continuous assessment assignments before the deadline through the department portal.",
                Date.valueOf("2026-03-05"), "ADM001", "ACTIVE", "System Administrator", "admin001"));
        mockNotices.add(new Notice("NOT003", "Academic Registration Notice",
                "Please check your registered courses and report any discrepancies to the department office immediately.",
                Date.valueOf("2026-01-12"), "ADM001", "ACTIVE", "System Administrator", "admin001"));
    }

    /**
     * Retrieves all published notices.
     */
    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT n.notice_id, n.title, n.description, n.notice_date, n.created_by, n.status, " +
                         "       TRIM(CONCAT(IFNULL(u.first_name, ''), ' ', IFNULL(u.last_name, ''))) AS creator_name, " +
                         "       u.username AS creator_username " +
                         "FROM notices n " +
                         "LEFT JOIN users u ON n.created_by = u.user_id " +
                         "ORDER BY n.notice_date DESC, n.notice_id DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToNotice(rs));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[NoticeDAO ERROR] getAllNotices failed: " + e.getMessage());
            }
        }
        return new ArrayList<>(mockNotices);
    }

    /**
     * Retrieves only ACTIVE notices.
     */
    public List<Notice> getActiveNotices() {
        List<Notice> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT n.notice_id, n.title, n.description, n.notice_date, n.created_by, n.status, " +
                         "       TRIM(CONCAT(IFNULL(u.first_name, ''), ' ', IFNULL(u.last_name, ''))) AS creator_name, " +
                         "       u.username AS creator_username " +
                         "FROM notices n " +
                         "LEFT JOIN users u ON n.created_by = u.user_id " +
                         "WHERE n.status = 'ACTIVE' " +
                         "ORDER BY n.notice_date DESC, n.notice_id DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToNotice(rs));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[NoticeDAO ERROR] getActiveNotices failed: " + e.getMessage());
            }
        }

        for (Notice n : mockNotices) {
            if ("ACTIVE".equalsIgnoreCase(n.getStatus())) {
                list.add(n);
            }
        }
        return list;
    }

    /**
     * Searches notices by keyword in title or description.
     */
    public List<Notice> searchNotices(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveNotices();
        }
        keyword = keyword.trim().toLowerCase();
        List<Notice> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT n.notice_id, n.title, n.description, n.notice_date, n.created_by, n.status, " +
                         "       TRIM(CONCAT(IFNULL(u.first_name, ''), ' ', IFNULL(u.last_name, ''))) AS creator_name, " +
                         "       u.username AS creator_username " +
                         "FROM notices n " +
                         "LEFT JOIN users u ON n.created_by = u.user_id " +
                         "WHERE (LOWER(n.title) LIKE ? OR LOWER(n.description) LIKE ?) " +
                         "ORDER BY n.notice_date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String pattern = "%" + keyword + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToNotice(rs));
                    }
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[NoticeDAO ERROR] searchNotices failed: " + e.getMessage());
            }
        }

        for (Notice n : mockNotices) {
            if (n.getTitle().toLowerCase().contains(keyword) || n.getDescription().toLowerCase().contains(keyword)) {
                list.add(n);
            }
        }
        return list;
    }

    public Notice getNoticeById(String noticeId) {
        if (noticeId == null) return null;
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT n.notice_id, n.title, n.description, n.notice_date, n.created_by, n.status, " +
                         "       TRIM(CONCAT(IFNULL(u.first_name, ''), ' ', IFNULL(u.last_name, ''))) AS creator_name, " +
                         "       u.username AS creator_username " +
                         "FROM notices n " +
                         "LEFT JOIN users u ON n.created_by = u.user_id " +
                         "WHERE n.notice_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, noticeId.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToNotice(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[NoticeDAO ERROR] getNoticeById failed: " + e.getMessage());
            }
        }

        for (Notice n : mockNotices) {
            if (n.getNoticeId().equalsIgnoreCase(noticeId.trim())) {
                return n;
            }
        }
        return null;
    }

    private Notice mapResultSetToNotice(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setNoticeId(rs.getString("notice_id"));
        n.setTitle(rs.getString("title"));
        n.setDescription(rs.getString("description"));
        n.setNoticeDate(rs.getDate("notice_date"));
        n.setCreatedBy(rs.getString("created_by"));
        n.setStatus(rs.getString("status"));

        String creatorName = rs.getString("creator_name");
        String creatorUsername = rs.getString("creator_username");
        if (creatorName != null && !creatorName.isBlank()) {
            n.setCreatorName(creatorName);
        } else {
            n.setCreatorName(n.getCreatedBy());
        }
        n.setCreatorUsername(creatorUsername != null ? creatorUsername : "");

        return n;
    }
}
