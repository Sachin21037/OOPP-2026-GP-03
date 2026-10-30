package dao;

import model.Notice;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for Academic Notices in FAMS.
 * Interfaces strictly with the 'notices' table joined with 'users'.
 * All notice data originates directly from the MySQL database schema.
 */
public class NoticeDAO {

    private final Set<String> readNoticeIds = new HashSet<>();

    public NoticeDAO() {
    }

    /**
     * Retrieves all notices directly from the database.
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
            } catch (SQLException e) {
                System.err.println("[NoticeDAO ERROR] getAllNotices failed: " + e.getMessage());
            }
        }
        applyReadState(list);
        return list;
    }

    /**
     * Retrieves notices filtered by status mode: "ACTIVE", "INACTIVE", or "ALL".
     */
    public List<Notice> getNoticesByStatus(String statusMode) {
        List<Notice> all = getAllNotices();
        if (statusMode == null || "ALL".equalsIgnoreCase(statusMode.trim())) {
            return all;
        }

        String mode = statusMode.trim().toUpperCase();
        List<Notice> filtered = new ArrayList<>();
        for (Notice n : all) {
            if (mode.equalsIgnoreCase(n.getStatus())) {
                filtered.add(n);
            }
        }
        return filtered;
    }

    /**
     * Department-aware service call mapping to status filter.
     */
    public List<Notice> getDepartmentNotices(String department, String statusMode) {
        return getNoticesByStatus(statusMode);
    }

    /**
     * Retrieves active notices.
     */
    public List<Notice> getActiveNotices() {
        return getNoticesByStatus("ACTIVE");
    }

    /**
     * Searches notices by keyword in title, description, or creator name.
     */
    public List<Notice> searchNotices(String keyword, String department, String statusMode) {
        List<Notice> base = getNoticesByStatus(statusMode);
        if (keyword == null || keyword.trim().isEmpty()) {
            return base;
        }

        String kw = keyword.trim().toLowerCase();
        List<Notice> result = new ArrayList<>();
        for (Notice n : base) {
            if ((n.getTitle() != null && n.getTitle().toLowerCase().contains(kw))
                    || (n.getDescription() != null && n.getDescription().toLowerCase().contains(kw))
                    || (n.getCreatorName() != null && n.getCreatorName().toLowerCase().contains(kw))
                    || (n.getNoticeId() != null && n.getNoticeId().toLowerCase().contains(kw))) {
                result.add(n);
            }
        }
        return result;
    }

    public Notice getNoticeById(String noticeId) {
        if (noticeId == null) return null;
        for (Notice n : getAllNotices()) {
            if (n.getNoticeId().equalsIgnoreCase(noticeId.trim())) {
                return n;
            }
        }
        return null;
    }

    public void markNoticeAsRead(String noticeId) {
        if (noticeId != null) {
            readNoticeIds.add(noticeId.trim());
        }
    }

    public int getUnreadCount(String department) {
        List<Notice> notices = getActiveNotices();
        int count = 0;
        for (Notice n : notices) {
            if (!readNoticeIds.contains(n.getNoticeId())) {
                count++;
            }
        }
        return count;
    }

    private void applyReadState(List<Notice> list) {
        for (Notice n : list) {
            if (readNoticeIds.contains(n.getNoticeId())) {
                n.setRead(true);
            }
        }
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

        if (readNoticeIds.contains(n.getNoticeId())) {
            n.setRead(true);
        }

        return n;
    }
}
