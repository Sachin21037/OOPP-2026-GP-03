package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * NoticesDAO - Data Access Object for Faculty Notices.
 * Handles database retrieval, CRUD operations, filtering, and summary metrics.
 */
public class NoticesDAO {

    public static class NoticeItem {
        public final String noticeId;
        public final String title;
        public final String description;
        public final String noticeDate;
        public final String createdBy;
        public final String creatorName;
        public final String status;

        public NoticeItem(
                String noticeId, String title, String description,
                String noticeDate, String createdBy, String creatorName, String status
        ) {
            this.noticeId = noticeId != null ? noticeId : "";
            this.title = title != null ? title : "";
            this.description = description != null ? description : "";
            this.noticeDate = noticeDate != null ? noticeDate : "";
            this.createdBy = createdBy != null ? createdBy : "";
            this.creatorName = creatorName != null ? creatorName : "";
            this.status = status != null ? status.trim().toUpperCase(Locale.ROOT) : "ACTIVE";
        }

        public Object[] toTableRow() {
            String poster = !creatorName.isEmpty() ? creatorName + " (" + createdBy + ")" : createdBy;
            return new Object[]{noticeId, title, noticeDate, poster, status};
        }
    }

    public static class NoticesSummary {
        public final int activeCount;
        public final int recentCount;
        public final int totalCount;

        public NoticesSummary(int activeCount, int recentCount, int totalCount) {
            this.activeCount = activeCount;
            this.recentCount = recentCount;
            this.totalCount = totalCount;
        }
    }

    /**
     * Loads faculty notices from the MySQL database.
     */
    public static List<NoticeItem> loadNotices() throws SQLException {
        List<NoticeItem> list = new ArrayList<>();
        String sql =
                "SELECT n.notice_id, n.title, n.description, n.notice_date, n.created_by, n.status, " +
                "       u.first_name, u.last_name " +
                "FROM notices n " +
                "LEFT JOIN users u ON n.created_by = u.user_id " +
                "ORDER BY n.notice_date DESC, n.notice_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String noticeId = rs.getString("notice_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                java.sql.Date d = rs.getDate("notice_date");
                String noticeDate = d != null ? d.toString() : "";
                String createdBy = rs.getString("created_by");
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String creatorName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                String status = rs.getString("status");

                list.add(new NoticeItem(
                        noticeId, title, description, noticeDate, createdBy, creatorName, status
                ));
            }
        }
        return list;
    }

    /**
     * Inserts a new notice into MySQL database.
     */
    public static String insertNotice(String title, String description, java.sql.Date date, String createdBy, String status) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String nextId = getNextNoticeId(conn);
            String sql = "INSERT INTO notices (notice_id, title, description, notice_date, created_by, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nextId);
                ps.setString(2, title != null ? title.trim() : "");
                ps.setString(3, description != null ? description.trim() : "");
                ps.setDate(4, date != null ? date : new java.sql.Date(System.currentTimeMillis()));
                ps.setString(5, createdBy != null ? createdBy.trim() : "ADM001");
                ps.setString(6, status != null ? status.trim().toUpperCase(Locale.ROOT) : "ACTIVE");
                ps.executeUpdate();
            }
            return nextId;
        }
    }

    /**
     * Updates an existing notice in MySQL database.
     */
    public static boolean updateNotice(String noticeId, String title, String description, java.sql.Date date, String status) throws SQLException {
        String sql = "UPDATE notices SET title = ?, description = ?, notice_date = ?, status = ? WHERE notice_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title != null ? title.trim() : "");
            ps.setString(2, description != null ? description.trim() : "");
            ps.setDate(3, date != null ? date : new java.sql.Date(System.currentTimeMillis()));
            ps.setString(4, status != null ? status.trim().toUpperCase(Locale.ROOT) : "ACTIVE");
            ps.setString(5, noticeId != null ? noticeId.trim() : "");
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a notice from MySQL database.
     */
    public static boolean deleteNotice(String noticeId) throws SQLException {
        String sql = "DELETE FROM notices WHERE notice_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, noticeId != null ? noticeId.trim() : "");
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Filters notices based on status and search query.
     */
    public static List<NoticeItem> filterNotices(
            List<NoticeItem> loadedNotices,
            String selectedStatusFilter,
            String searchQuery
    ) {
        List<NoticeItem> result = new ArrayList<>();
        if (loadedNotices == null) return result;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        for (NoticeItem item : loadedNotices) {
            boolean matchesSearch = query.isEmpty()
                    || item.title.toLowerCase(Locale.ROOT).contains(query)
                    || item.description.toLowerCase(Locale.ROOT).contains(query)
                    || item.noticeId.toLowerCase(Locale.ROOT).contains(query)
                    || item.createdBy.toLowerCase(Locale.ROOT).contains(query)
                    || item.creatorName.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesStatus = true;
            if ("Active Notices".equalsIgnoreCase(selectedStatusFilter)) {
                matchesStatus = "ACTIVE".equalsIgnoreCase(item.status);
            } else if ("Recent (30 Days)".equalsIgnoreCase(selectedStatusFilter)) {
                if (!item.noticeDate.isEmpty()) {
                    try {
                        LocalDate d = LocalDate.parse(item.noticeDate);
                        matchesStatus = !d.isBefore(thirtyDaysAgo);
                    } catch (Exception ignored) {
                        matchesStatus = false;
                    }
                } else {
                    matchesStatus = false;
                }
            } else if ("Inactive Notices".equalsIgnoreCase(selectedStatusFilter)) {
                matchesStatus = "INACTIVE".equalsIgnoreCase(item.status);
            }

            if (matchesSearch && matchesStatus) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Calculates summary metrics for notices.
     */
    public static NoticesSummary calculateSummary(List<NoticeItem> loadedNotices) {
        if (loadedNotices == null || loadedNotices.isEmpty()) {
            return new NoticesSummary(0, 0, 0);
        }

        int activeCount = 0;
        int recentCount = 0;
        int totalCount = loadedNotices.size();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        for (NoticeItem item : loadedNotices) {
            if ("ACTIVE".equalsIgnoreCase(item.status)) {
                activeCount++;
            }
            if (!item.noticeDate.isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(item.noticeDate);
                    if (!date.isBefore(thirtyDaysAgo)) {
                        recentCount++;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return new NoticesSummary(activeCount, recentCount, totalCount);
    }

    private static String getNextNoticeId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(notice_id, 4) AS UNSIGNED)), 0) + 1 FROM notices WHERE notice_id LIKE 'NOT%'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                long nextNum = rs.getLong(1);
                return String.format(Locale.US, "NOT%03d", nextNum);
            }
        }
        return "NOT001";
    }
}
