package dao;

import model.Notice;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for Academic Notices in FAMS.
 * Interfaces with the 'notices' table and joins with 'users'.
 * Provides high-fidelity in-memory fallback when MySQL is offline.
 * Encapsulates department notices, urgent priorities, expiry tracking,
 * and attachment metadata.
 */
public class NoticeDAO {

    private final List<Notice> mockNotices = new ArrayList<>();
    private final Set<String> readNoticeIds = new HashSet<>();

    public NoticeDAO() {
        initMockData();
    }

    private void initMockData() {
        // Notice 1: Urgent Exam notice for ICT department with timetable attachment
        Notice n1 = new Notice("NOT001", "Mid Semester Examination Notice",
                "Mid semester examinations for all Level II ICT degree programs will be conducted according to the published timetable. " +
                "All eligible students and invigilating technical officers must ensure laboratory venues and workstations are prepared.",
                Date.valueOf("2026-10-15"), Date.valueOf("2026-12-31"), "ADM001", "ACTIVE",
                "Information and Communication Technology", "Mid_Semester_Exam_Timetable_2026.pdf",
                true, "System Administrator");
        n1.setCreatorUsername("admin001");
        mockNotices.add(n1);

        // Notice 2: ICT Lab Maintenance and Workstation Safety
        Notice n2 = new Notice("NOT002", "ICT Lab Safety Protocols & Hardware Maintenance Protocol",
                "Technical Officers are kindly instructed to conduct bi-weekly audit runs across all assigned computer fleets. " +
                "Report any hardware anomalies, power supply interruptions, or network cable issues promptly using the Lab Maintenance module.",
                Date.valueOf("2026-10-20"), Date.valueOf("2026-12-15"), "ADM001", "ACTIVE",
                "Information and Communication Technology", "Lab_Hardware_Maintenance_Guidelines_2026.pdf",
                false, "System Administrator");
        n2.setCreatorUsername("admin001");
        mockNotices.add(n2);

        // Notice 3: Academic Registration
        Notice n3 = new Notice("NOT003", "Academic Registration & Course Enrollment Verification",
                "Please check your registered courses and attendance rosters. Technical Officers should verify student lab registration lists " +
                "before continuous assessment practical evaluations commence.",
                Date.valueOf("2026-10-01"), Date.valueOf("2026-11-30"), "ADM001", "ACTIVE",
                "Faculty-wide", "Course_Registration_Manual.pdf",
                true, "System Administrator");
        n3.setCreatorUsername("admin001");
        mockNotices.add(n3);

        // Notice 4: Continuous Assessment Assignment Submission
        Notice n4 = new Notice("NOT004", "Continuous Assessment Assignment 01 Submission Schedule",
                "Students enrolled in ICT2132 and ICT2123 must submit practical project source code and laboratory documentation " +
                "before the specified cutoff date through the faculty portal.",
                Date.valueOf("2026-10-25"), Date.valueOf("2026-12-20"), "ADM001", "ACTIVE",
                "Information and Communication Technology", "Assignment_Submission_Rubrics.pdf",
                false, "System Administrator");
        n4.setCreatorUsername("admin001");
        mockNotices.add(n4);

        // Notice 5: Archived / Expired notice
        Notice n5 = new Notice("NOT005", "Archived: Annual Faculty Tech Symposium 2025",
                "The annual technology showcase and research symposium was successfully concluded on November 2025. " +
                "All laboratories have returned to standard academic schedule operations.",
                Date.valueOf("2025-10-15"), Date.valueOf("2025-11-20"), "ADM001", "INACTIVE",
                "Faculty-wide", null,
                false, "System Administrator");
        n5.setCreatorUsername("admin001");
        mockNotices.add(n5);
    }

    /**
     * Retrieves all notices from database or mock store.
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
        applyReadState(mockNotices);
        return new ArrayList<>(mockNotices);
    }

    /**
     * Retrieves notices filtered by department and status mode.
     * @param department Target department (e.g., "Information and Communication Technology" or null for all)
     * @param statusMode "ACTIVE" (hides expired), "ARCHIVED" (only expired or inactive), or "ALL"
     */
    public List<Notice> getDepartmentNotices(String department, String statusMode) {
        List<Notice> all = getAllNotices();
        List<Notice> filtered = new ArrayList<>();

        String targetDept = (department != null) ? department.trim().toLowerCase() : "";
        String mode = (statusMode != null) ? statusMode.trim().toUpperCase() : "ACTIVE";

        for (Notice n : all) {
            // Department filter: notice matches if Faculty-wide or matches target department
            boolean deptMatches = targetDept.isEmpty()
                    || "all departments".equalsIgnoreCase(targetDept)
                    || n.getTargetDepartment().equalsIgnoreCase("Faculty-wide")
                    || n.getTargetDepartment().toLowerCase().contains(targetDept)
                    || targetDept.contains(n.getTargetDepartment().toLowerCase());

            // Status filter
            boolean statusMatches;
            if ("ACTIVE".equalsIgnoreCase(mode)) {
                statusMatches = n.isActive();
            } else if ("ARCHIVED".equalsIgnoreCase(mode) || "EXPIRED".equalsIgnoreCase(mode)) {
                statusMatches = !n.isActive() || n.isExpired();
            } else {
                statusMatches = true; // ALL
            }

            if (deptMatches && statusMatches) {
                filtered.add(n);
            }
        }

        // Sort newest first
        filtered.sort((a, b) -> {
            if (a.getNoticeDate() == null && b.getNoticeDate() == null) return 0;
            if (a.getNoticeDate() == null) return 1;
            if (b.getNoticeDate() == null) return -1;
            return b.getNoticeDate().compareTo(a.getNoticeDate());
        });

        return filtered;
    }

    /**
     * Retrieves active notices (hiding expired ones).
     */
    public List<Notice> getActiveNotices() {
        return getDepartmentNotices(null, "ACTIVE");
    }

    /**
     * Searches notices by keyword in title, description, or publisher.
     */
    public List<Notice> searchNotices(String keyword, String department, String statusMode) {
        List<Notice> base = getDepartmentNotices(department, statusMode);
        if (keyword == null || keyword.trim().isEmpty()) {
            return base;
        }

        String kw = keyword.trim().toLowerCase();
        List<Notice> result = new ArrayList<>();
        for (Notice n : base) {
            if (n.getTitle().toLowerCase().contains(kw)
                    || n.getDescription().toLowerCase().contains(kw)
                    || n.getCreatorName().toLowerCase().contains(kw)
                    || n.getTargetDepartment().toLowerCase().contains(kw)
                    || (n.getAttachmentName() != null && n.getAttachmentName().toLowerCase().contains(kw))) {
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
            for (Notice n : mockNotices) {
                if (n.getNoticeId().equalsIgnoreCase(noticeId.trim())) {
                    n.setRead(true);
                }
            }
        }
    }

    public int getUnreadCount(String department) {
        List<Notice> notices = getDepartmentNotices(department, "ACTIVE");
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

        // Expiry date (derive from notice date if column not in schema)
        if ("ACTIVE".equalsIgnoreCase(n.getStatus())) {
            n.setExpiryDate(Date.valueOf(LocalDate.now().plusMonths(2)));
        } else if (n.getNoticeDate() != null) {
            n.setExpiryDate(Date.valueOf(n.getNoticeDate().toLocalDate().plusDays(30)));
        }

        // Attachments & department derived for existing sample notices
        if ("NOT001".equalsIgnoreCase(n.getNoticeId())) {
            n.setAttachmentName("Mid_Semester_Exam_Timetable_2026.pdf");
            n.setTargetDepartment("Information and Communication Technology");
            n.setUrgent(true);
        } else if ("NOT002".equalsIgnoreCase(n.getNoticeId())) {
            n.setAttachmentName("Assignment_Submission_Rubrics.pdf");
            n.setTargetDepartment("Information and Communication Technology");
            n.setUrgent(false);
        } else if ("NOT003".equalsIgnoreCase(n.getNoticeId())) {
            n.setAttachmentName("Course_Registration_Manual.pdf");
            n.setTargetDepartment("Faculty-wide");
            n.setUrgent(true);
        } else {
            n.setTargetDepartment("Information and Communication Technology");
        }

        if (readNoticeIds.contains(n.getNoticeId())) {
            n.setRead(true);
        }

        return n;
    }
}
