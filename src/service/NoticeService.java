package service;

import dao.NoticeDAO;
import model.Notice;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service coordinating Academic Notice business logic, validations,
 * and role-based access permissions.
 */
public class NoticeService {

    private static NoticeService instance;
    private final NoticeDAO noticeDAO;

    // Pattern for Notice ID: NOT followed by at least 1 digit (e.g. NOT001)
    private static final Pattern NOTICE_ID_PATTERN = Pattern.compile("^NOT[0-9]{1,7}$");

    public NoticeService() {
        this.noticeDAO = new NoticeDAO();
    }

    public NoticeService(NoticeDAO noticeDAO) {
        this.noticeDAO = noticeDAO;
    }

    public static synchronized NoticeService getInstance() {
        if (instance == null) {
            instance = new NoticeService();
        }
        return instance;
    }

    /**
     * Checks if a given role string is authorized for administrative notice operations.
     */
    public boolean isAdmin(String role) {
        return role != null && "ADMIN".equalsIgnoreCase(role.trim());
    }

    /**
     * Retrieves notices appropriate for the specified user role.
     * Administrators receive all notices (ACTIVE and INACTIVE).
     * Lecturers, Technical Officers, and Undergraduates receive only ACTIVE notices.
     */
    public List<Notice> getNoticesForRole(String role) {
        if (isAdmin(role)) {
            return noticeDAO.getAllNotices();
        } else {
            return noticeDAO.getActiveNotices();
        }
    }

    /**
     * Retrieves all notices (Admin mode).
     */
    public List<Notice> getAllNotices() {
        return noticeDAO.getAllNotices();
    }

    /**
     * Retrieves only active notices (Public/Student/Staff mode).
     */
    public List<Notice> getActiveNotices() {
        return noticeDAO.getActiveNotices();
    }

    /**
     * Retrieves a notice by ID.
     */
    public Notice getNoticeById(String noticeId) {
        if (noticeId == null || noticeId.isBlank()) return null;
        return noticeDAO.getNoticeById(noticeId.trim());
    }

    /**
     * Searches notices based on user role constraints.
     */
    public List<Notice> searchNotices(String keyword, String statusFilter, String role) {
        boolean activeOnly = !isAdmin(role);
        return noticeDAO.searchNotices(keyword, statusFilter, activeOnly);
    }

    /**
     * Suggests the next sequential Notice ID.
     */
    public String getNextSuggestedNoticeId() {
        return noticeDAO.getNextSuggestedNoticeId();
    }

    /**
     * Retrieves summary counts for dashboard metric cards.
     */
    public Map<String, Object> getNoticeSummaryCounts() {
        return noticeDAO.getNoticeSummaryCounts();
    }

    /**
     * Validates notice fields.
     */
    public OperationResult validateNotice(Notice notice, boolean isNew) {
        if (notice == null) {
            return OperationResult.error("Notice details cannot be empty.");
        }

        // 1. Notice ID
        String id = notice.getNoticeId();
        if (id == null || id.trim().isEmpty()) {
            return OperationResult.error("Notice ID is required (e.g. NOT001).");
        }
        id = id.trim().toUpperCase();
        if (!NOTICE_ID_PATTERN.matcher(id).matches()) {
            return OperationResult.error("Notice ID must follow the 'NOT' prefix format (e.g. NOT001).");
        }
        if (isNew && noticeDAO.existsNoticeId(id)) {
            return OperationResult.error("Notice ID '" + id + "' already exists in the system.");
        }

        // 2. Title
        String title = notice.getTitle();
        if (title == null || title.trim().isEmpty()) {
            return OperationResult.error("Notice title is required.");
        }
        title = title.trim();
        if (title.length() < 3 || title.length() > 200) {
            return OperationResult.error("Notice title must be between 3 and 200 characters.");
        }

        // 3. Description
        String desc = notice.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            return OperationResult.error("Notice description/content is required.");
        }
        if (desc.trim().length() < 5) {
            return OperationResult.error("Notice description must be at least 5 characters long.");
        }

        // 4. Notice Date
        if (notice.getNoticeDate() == null) {
            return OperationResult.error("Publish date is required.");
        }

        // 5. Status
        String status = notice.getStatus();
        if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
            return OperationResult.error("Notice status must be either ACTIVE or INACTIVE.");
        }

        return OperationResult.success("Validation passed.");
    }

    /**
     * Publishes a new notice (Restricted to Administrators).
     */
    public OperationResult addNotice(Notice notice, String userRole) {
        if (!isAdmin(userRole)) {
            return OperationResult.error("Permission Denied: Only administrators are authorized to publish faculty notices.");
        }

        OperationResult valResult = validateNotice(notice, true);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize
        notice.setNoticeId(notice.getNoticeId().trim().toUpperCase());
        notice.setTitle(notice.getTitle().trim());
        notice.setDescription(notice.getDescription().trim());
        if (notice.getCreatedBy() == null || notice.getCreatedBy().isBlank()) {
            notice.setCreatedBy(noticeDAO.getDefaultAdminId());
        }

        try {
            boolean added = noticeDAO.addNotice(notice);
            if (added) {
                return OperationResult.success("Notice '" + notice.getTitle() + "' published successfully.", notice);
            } else {
                return OperationResult.error("Failed to publish notice. Please verify database connection.");
            }
        } catch (SQLException e) {
            System.err.println("[NoticeService ERROR] addNotice failed: " + e.getMessage());
            if (e.getErrorCode() == 1062) {
                return OperationResult.error("Duplicate notice ID '" + notice.getNoticeId() + "' already exists.");
            }
            return OperationResult.error("Database error while publishing notice: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Updates an existing notice (Restricted to Administrators).
     */
    public OperationResult updateNotice(Notice notice, String userRole) {
        if (!isAdmin(userRole)) {
            return OperationResult.error("Permission Denied: Only administrators are authorized to modify faculty notices.");
        }

        OperationResult valResult = validateNotice(notice, false);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize
        notice.setTitle(notice.getTitle().trim());
        notice.setDescription(notice.getDescription().trim());

        try {
            boolean updated = noticeDAO.updateNotice(notice);
            if (updated) {
                return OperationResult.success("Notice '" + notice.getTitle() + "' updated successfully.", notice);
            } else {
                return OperationResult.error("Notice " + notice.getNoticeId() + " was not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("[NoticeService ERROR] updateNotice failed: " + e.getMessage());
            return OperationResult.error("Database error while updating notice: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Deletes a notice (Restricted to Administrators).
     */
    public OperationResult deleteNotice(String noticeId, String userRole) {
        if (!isAdmin(userRole)) {
            return OperationResult.error("Permission Denied: Only administrators are authorized to delete faculty notices.");
        }

        if (noticeId == null || noticeId.isBlank()) {
            return OperationResult.error("Invalid Notice ID.");
        }
        noticeId = noticeId.trim();

        Notice existing = noticeDAO.getNoticeById(noticeId);
        if (existing == null) {
            return OperationResult.error("Notice " + noticeId + " does not exist or was already deleted.");
        }

        try {
            boolean deleted = noticeDAO.deleteNotice(noticeId);
            if (deleted) {
                return OperationResult.success("Notice '" + existing.getTitle() + "' was deleted successfully.");
            } else {
                return OperationResult.error("Failed to delete notice " + noticeId + ".");
            }
        } catch (SQLException e) {
            System.err.println("[NoticeService ERROR] deleteNotice failed: " + e.getMessage());
            return OperationResult.error("Database error while deleting notice: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Toggles notice status between ACTIVE and INACTIVE (Restricted to Administrators).
     */
    public OperationResult toggleStatus(String noticeId, String newStatus, String userRole) {
        if (!isAdmin(userRole)) {
            return OperationResult.error("Permission Denied: Only administrators are authorized to change notice status.");
        }

        if (noticeId == null || noticeId.isBlank()) {
            return OperationResult.error("Invalid Notice ID.");
        }

        try {
            boolean toggled = noticeDAO.toggleStatus(noticeId.trim(), newStatus);
            if (toggled) {
                return OperationResult.success("Notice status set to " + newStatus + ".");
            } else {
                return OperationResult.error("Notice " + noticeId + " not found.");
            }
        } catch (SQLException e) {
            return OperationResult.error("Database error while updating notice status: " + e.getMessage());
        }
    }

    // =========================================================================
    // OPERATION RESULT VALUE OBJECT
    // =========================================================================
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final Object data;

        private OperationResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult success(String message, Object data) {
            return new OperationResult(true, message, data);
        }

        public static OperationResult error(String message) {
            return new OperationResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
