package dao;

import database.DBConnection;
import model.Notice;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Access Object for Academic Notices in FAMS.
 * Interfaces with the 'notices' table, supporting CRUD operations,
 * search filtering, auto-ID generation, and creator joins.
 */
public class NoticeDAO {

    private final Set<String> readNoticeIds = new HashSet<>();

    private static final String BASE_SELECT =
            "SELECT n.notice_id, n.title, n.description, n.notice_date, n.created_by, n.status, " +
            "       TRIM(CONCAT(IFNULL(u.first_name, ''), ' ', IFNULL(u.last_name, ''))) AS creator_name, " +
            "       u.username AS creator_username " +
            "FROM notices n " +
            "LEFT JOIN users u ON n.created_by = u.user_id COLLATE utf8mb4_unicode_ci ";

    /**
     * Retrieves all notices in reverse chronological order.
     */
    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY n.notice_date DESC, n.notice_id DESC";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToNotice(rs));
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] getAllNotices failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves only ACTIVE notices (for students, lecturers, technical officers).
     */
    public List<Notice> getActiveNotices() {
        List<Notice> list = new ArrayList<>();
        String sql = BASE_SELECT +
                "WHERE n.status = 'ACTIVE' " +
                "ORDER BY n.notice_date DESC, n.notice_id DESC";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToNotice(rs));
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] getActiveNotices failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves a notice by its primary key ID (e.g. NOT001).
     */
    public Notice getNoticeById(String noticeId) {
        if (noticeId == null || noticeId.isBlank()) return null;
        String sql = BASE_SELECT + "WHERE n.notice_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, noticeId.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNotice(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] getNoticeById failed for " + noticeId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Searches notices by keyword (matching title, description, or notice_id), status filter,
     * and optional activeOnly restriction for non-admin viewers.
     */
    public List<Notice> searchNotices(String keyword, String statusFilter, boolean activeOnly) {
        List<Notice> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (activeOnly) {
            sql.append("AND n.status = 'ACTIVE' ");
        } else if (statusFilter != null && !statusFilter.trim().isEmpty()
                && !statusFilter.equalsIgnoreCase("ALL")
                && !statusFilter.equalsIgnoreCase("All Status")) {
            sql.append("AND n.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append("AND (n.title LIKE ? OR n.description LIKE ? OR n.notice_id LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append("ORDER BY n.notice_date DESC, n.notice_id DESC");

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotice(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] searchNotices failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Suggests the next sequential Notice ID (e.g. NOT004).
     */
    public String getNextSuggestedNoticeId() {
        String sql = "SELECT notice_id FROM notices WHERE notice_id LIKE 'NOT%' ORDER BY LENGTH(notice_id) DESC, notice_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String lastId = rs.getString("notice_id");
                if (lastId != null && lastId.startsWith("NOT")) {
                    String numPart = lastId.substring(3).trim();
                    try {
                        int nextNum = Integer.parseInt(numPart) + 1;
                        return String.format("NOT%03d", nextNum);
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] getNextSuggestedNoticeId failed: " + e.getMessage());
        }

        return "NOT001";
    }

    /**
     * Checks if a notice with the given ID already exists.
     */
    public boolean existsNoticeId(String noticeId) {
        if (noticeId == null || noticeId.isBlank()) return false;
        String sql = "SELECT 1 FROM notices WHERE notice_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, noticeId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] existsNoticeId failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the default admin ID (e.g. ADM001) for the created_by foreign key.
     */
    public String getDefaultAdminId() {
        String sql = "SELECT admin_id FROM admins ORDER BY admin_id ASC LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString("admin_id");
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] getDefaultAdminId failed: " + e.getMessage());
        }

        return "ADM001";
    }

    /**
     * Inserts a new notice into the database.
     */
    public boolean addNotice(Notice notice) throws SQLException {
        if (notice == null) return false;

        String sql = "INSERT INTO notices (notice_id, title, description, notice_date, created_by, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notice.getNoticeId());
            pstmt.setString(2, notice.getTitle());
            pstmt.setString(3, notice.getDescription());
            pstmt.setDate(4, notice.getNoticeDate() != null ? notice.getNoticeDate() : Date.valueOf(java.time.LocalDate.now()));
            pstmt.setString(5, (notice.getCreatedBy() != null && !notice.getCreatedBy().isBlank()) ? notice.getCreatedBy() : getDefaultAdminId());
            pstmt.setString(6, notice.getStatus());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing notice's title, description, date, and status.
     */
    public boolean updateNotice(Notice notice) throws SQLException {
        if (notice == null || notice.getNoticeId() == null) return false;

        String sql = "UPDATE notices SET title = ?, description = ?, notice_date = ?, status = ? " +
                     "WHERE notice_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notice.getTitle());
            pstmt.setString(2, notice.getDescription());
            pstmt.setDate(3, notice.getNoticeDate() != null ? notice.getNoticeDate() : Date.valueOf(java.time.LocalDate.now()));
            pstmt.setString(4, notice.getStatus());
            pstmt.setString(5, notice.getNoticeId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a notice by its notice ID.
     */
    public boolean deleteNotice(String noticeId) throws SQLException {
        if (noticeId == null || noticeId.isBlank()) return false;

        String sql = "DELETE FROM notices WHERE notice_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, noticeId.trim());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Toggles notice status between ACTIVE and INACTIVE.
     */
    public boolean toggleStatus(String noticeId, String newStatus) throws SQLException {
        if (noticeId == null || noticeId.isBlank()) return false;

        String sql = "UPDATE notices SET status = ? WHERE notice_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.toUpperCase());
            pstmt.setString(2, noticeId.trim());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves summary counts: total, active, inactive, and latest published date.
     */
    public Map<String, Object> getNoticeSummaryCounts() {
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("total", 0);
        counts.put("active", 0);
        counts.put("inactive", 0);
        counts.put("latest_date", "None");

        String sql = "SELECT " +
                     "    COUNT(*) AS total_notices, " +
                     "    SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_notices, " +
                     "    SUM(CASE WHEN status = 'INACTIVE' THEN 1 ELSE 0 END) AS inactive_notices, " +
                     "    MAX(notice_date) AS latest_date " +
                     "FROM notices";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                counts.put("total", rs.getInt("total_notices"));
                counts.put("active", rs.getInt("active_notices"));
                counts.put("inactive", rs.getInt("inactive_notices"));
                Date maxDate = rs.getDate("latest_date");
                counts.put("latest_date", (maxDate != null) ? maxDate.toString() : "None");
            }

        } catch (SQLException e) {
            System.err.println("[NoticeDAO ERROR] getNoticeSummaryCounts failed: " + e.getMessage());
        }

        return counts;
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

    public List<Notice> getDepartmentNotices(String department, String statusMode) {
        return getNoticesByStatus(statusMode);
    }

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
}
