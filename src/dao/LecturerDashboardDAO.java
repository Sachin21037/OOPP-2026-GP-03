package dao;

import database.DBConnection;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * LecturerDashboardDAO - Data Access Object for Lecturer Dashboard.
 * Handles database metrics retrieval, counts, and recent activity feeds.
 */
public class LecturerDashboardDAO {

    public static class ActivityItem {
        public final String text;
        public final String category;
        public final Color accent;

        public ActivityItem(String text, String category, Color accent) {
            this.text = text;
            this.category = category;
            this.accent = accent;
        }
    }

    public static class DashboardData {
        public String lecturerDisplayName = "Lecturer";
        public String myCoursesValue = "-";
        public String studentsValue = "-";
        public String assignmentsValue = "-";
        public String pendingMarksValue = "-";
        public String coursesCaption = "Assigned courses";
        public String studentsCaption = "Across your courses";
        public String assignmentsCaption = "Created this semester";
        public String pendingMarksCaption = "Need your attention";
        public String welcomeSubtitle = "Manage your courses, students, attendance and academic activities.";
        public final List<ActivityItem> activityItems = new ArrayList<>();
    }

    private static final Color NAVY = new Color(16, 40, 74);
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color RED = new Color(220, 38, 38);
    private static final Color MUTED = new Color(113, 128, 150);

    /**
     * Resolves lecturer full name from database.
     */
    public static String getLecturerFullName(String lecturerId) {
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return "Lecturer";
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.first_name, u.last_name FROM users u WHERE u.user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lecturerId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String first = rs.getString("first_name");
                        String last = rs.getString("last_name");
                        String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                        if (!fullName.isEmpty()) {
                            return fullName;
                        }
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return "Lecturer";
    }

    /**
     * Loads live dashboard statistics and recent activity list from database.
     */
    public static DashboardData loadDashboardData(String lecturerId) {
        DashboardData data = new DashboardData();

        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            data.coursesCaption = "Assigned courses (No lecturer ID set)";
            data.studentsCaption = "Across courses (No lecturer ID set)";
            data.assignmentsCaption = "Assignments (No lecturer ID set)";
            data.pendingMarksCaption = "Pending marks (No lecturer ID set)";
            data.welcomeSubtitle = "Manage your courses, students, attendance and academic activities. (Pass a lecturer ID to view personal data)";
            loadPublicNotices(data);
            return data;
        }

        String safeId = lecturerId.trim();

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Fetch Lecturer Name
            String nameQuery = "SELECT u.first_name, u.last_name, l.designation " +
                    "FROM lecturers l " +
                    "JOIN users u ON l.lecturer_id = u.user_id " +
                    "WHERE l.lecturer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(nameQuery)) {
                ps.setString(1, safeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String first = rs.getString("first_name");
                        String last = rs.getString("last_name");
                        String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                        if (!fullName.isEmpty()) {
                            data.lecturerDisplayName = fullName;
                        }
                    } else {
                        data.welcomeSubtitle = "Lecturer ID '" + safeId + "' not found in database.";
                    }
                }
            }

            // 2. Count Assigned Courses
            String coursesQuery = "SELECT COUNT(DISTINCT cl.offering_id) " +
                    "FROM course_lecturers cl " +
                    "WHERE cl.lecturer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(coursesQuery)) {
                ps.setString(1, safeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.myCoursesValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 3. Count Enrolled Students
            String studentsQuery = "SELECT COUNT(DISTINCT e.student_id) " +
                    "FROM course_lecturers cl " +
                    "JOIN enrollments e ON cl.offering_id = e.offering_id " +
                    "WHERE cl.lecturer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(studentsQuery)) {
                ps.setString(1, safeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.studentsValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 4. Count Assignments
            String assignmentsQuery = "SELECT COUNT(*) " +
                    "FROM assessments a " +
                    "JOIN course_lecturers cl ON a.offering_id = cl.offering_id " +
                    "WHERE cl.lecturer_id = ? AND a.assessment_type = 'ASSIGNMENT'";
            try (PreparedStatement ps = conn.prepareStatement(assignmentsQuery)) {
                ps.setString(1, safeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.assignmentsValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 5. Count Pending Marks
            String pendingMarksQuery = "SELECT COUNT(*) " +
                    "FROM assessments a " +
                    "JOIN course_lecturers cl ON a.offering_id = cl.offering_id " +
                    "JOIN enrollments e ON a.offering_id = e.offering_id " +
                    "LEFT JOIN marks m ON a.assessment_id = m.assessment_id AND e.student_id = m.student_id " +
                    "WHERE cl.lecturer_id = ? AND m.mark_id IS NULL";
            try (PreparedStatement ps = conn.prepareStatement(pendingMarksQuery)) {
                ps.setString(1, safeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.pendingMarksValue = String.valueOf(rs.getInt(1));
                    }
                }
            }

            // 6. Load Recent Activities
            loadActivities(conn, safeId, data);

        } catch (SQLException e) {
            System.err.println("[LecturerDashboardDAO] Database error loading stats: " + e.getMessage());
            data.coursesCaption = "Assigned courses (DB Error)";
            data.studentsCaption = "Across courses (DB Error)";
            data.assignmentsCaption = "Assignments (DB Error)";
            data.pendingMarksCaption = "Pending marks (DB Error)";
            data.welcomeSubtitle = "Database connection error: " + e.getMessage();
            data.activityItems.add(new ActivityItem("Database connection unavailable", "DATABASE", RED));
        }

        return data;
    }

    private static void loadActivities(Connection conn, String lecturerId, DashboardData data) {
        // Attendance
        String attSql = "SELECT s.session_number, c.course_code " +
                "FROM attendance_sessions s " +
                "JOIN course_lecturers cl ON s.offering_id = cl.offering_id " +
                "JOIN course_offerings co ON s.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY s.session_date DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(attSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.activityItems.add(new ActivityItem(
                            "Attendance session " + rs.getInt("session_number") + " updated for " + rs.getString("course_code"),
                            "ATTENDANCE",
                            TEAL
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        // Assignment
        String asmSql = "SELECT a.assessment_name, c.course_code " +
                "FROM assessments a " +
                "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE a.created_by = ? " +
                "ORDER BY a.assessment_id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(asmSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.activityItems.add(new ActivityItem(
                            "Created " + rs.getString("assessment_name") + " (" + rs.getString("course_code") + ")",
                            "ASSIGNMENT",
                            NAVY
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        // Marks
        String marksSql = "SELECT a.assessment_name, c.course_code " +
                "FROM marks m " +
                "JOIN assessments a ON m.assessment_id = a.assessment_id " +
                "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE m.entered_by = ? " +
                "GROUP BY a.assessment_name, c.course_code " +
                "ORDER BY MAX(m.entered_at) DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(marksSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.activityItems.add(new ActivityItem(
                            "Marks submitted for " + rs.getString("assessment_name") + " (" + rs.getString("course_code") + ")",
                            "MARKS",
                            PURPLE
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        // Materials
        String matSql = "SELECT cm.title, c.course_code " +
                "FROM course_materials cm " +
                "JOIN course_offerings co ON cm.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cm.lecturer_id = ? " +
                "ORDER BY cm.uploaded_at DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(matSql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.activityItems.add(new ActivityItem(
                            "Course material: " + rs.getString("title") + " (" + rs.getString("course_code") + ")",
                            "MATERIALS",
                            new Color(190, 125, 45)
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        if (data.activityItems.isEmpty()) {
            data.activityItems.add(new ActivityItem(
                    "No recent activities recorded for lecturer " + lecturerId,
                    "INFO",
                    MUTED
            ));
        }
    }

    private static void loadPublicNotices(DashboardData data) {
        try (Connection conn = DBConnection.getConnection()) {
            String noticeSql = "SELECT title FROM notices WHERE status = 'ACTIVE' ORDER BY notice_date DESC LIMIT 3";
            try (PreparedStatement ps = conn.prepareStatement(noticeSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.activityItems.add(new ActivityItem(
                            rs.getString("title"),
                            "NOTICE",
                            NAVY
                    ));
                }
            }
        } catch (SQLException ignored) {
        }

        if (data.activityItems.isEmpty()) {
            data.activityItems.add(new ActivityItem(
                    "No active lecturer session. Provide a lecturer ID (e.g. LEC001) to view activities.",
                    "INFO",
                    MUTED
            ));
        }
    }
}

