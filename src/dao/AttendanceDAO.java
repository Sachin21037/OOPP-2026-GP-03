package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AttendanceDAO - Data Access Object for Student Attendance Management.
 * Handles database operations, attendance queries, course retrieval, and record filtering.
 */
public class AttendanceDAO {

    public static class AttendanceRecord {
        public final String attendanceId;
        public final String studentId;
        public final String studentName;
        public final String courseCode;
        public final String courseName;
        public final String sessionId;
        public final String sessionDate;
        public final String sessionType;
        public final int sessionNumber;
        public final String status;
        public final String remarks;

        public AttendanceRecord(
                String attendanceId, String studentId, String studentName,
                String courseCode, String courseName, String sessionId,
                String sessionDate, String sessionType, int sessionNumber,
                String status, String remarks
        ) {
            this.attendanceId = attendanceId != null ? attendanceId : "";
            this.studentId = studentId != null ? studentId : "";
            this.studentName = studentName != null ? studentName : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.sessionId = sessionId != null ? sessionId : "";
            this.sessionDate = sessionDate != null ? sessionDate : "";
            this.sessionType = sessionType != null ? sessionType : "LECTURE";
            this.sessionNumber = sessionNumber;
            this.status = formatStatus(status);
            this.remarks = remarks != null ? remarks : "";
        }

        public Object[] toTableRow() {
            String courseLabel = courseCode.isEmpty() ? "-" : courseCode;
            String typeLabel = sessionType.toUpperCase(Locale.ROOT);
            if (sessionNumber > 0) {
                typeLabel += " #" + sessionNumber;
            }
            return new Object[]{
                    studentId,
                    studentName.isEmpty() ? "Unknown Student" : studentName,
                    courseLabel,
                    sessionDate.isEmpty() ? "-" : sessionDate,
                    typeLabel,
                    status,
                    remarks.isEmpty() ? "-" : remarks
            };
        }
    }

    public static class AttendanceSummary {
        public final int totalStudents;
        public final int presentCount;
        public final int absentCount;
        public final int medicalCount;
        public final double attendanceRate;

        public AttendanceSummary(int totalStudents, int presentCount, int absentCount, int medicalCount, double attendanceRate) {
            this.totalStudents = totalStudents;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.medicalCount = medicalCount;
            this.attendanceRate = attendanceRate;
        }
    }

    public static String formatStatus(String raw) {
        if (raw == null) return "ABSENT";
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.contains("PRESENT")) return "PRESENT";
        if (s.contains("MEDICAL")) return "MEDICAL";
        if (s.contains("ABSENT")) return "ABSENT";
        return s;
    }

    /**
     * Loads assigned courses for the lecturer from the database.
     */
    public static List<String> loadAssignedCourses(String lecturerId) {
        List<String> courses = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return courses;
        }

        String sql =
                "SELECT DISTINCT c.course_code, c.course_name " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    courses.add(code + " - " + name);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttendanceDAO] Error loading courses: " + e.getMessage());
        }
        return courses;
    }

    /**
     * Queries attendance records from the database for the given lecturer.
     */
    public static List<AttendanceRecord> loadAttendanceRecords(String lecturerId) throws SQLException {
        List<AttendanceRecord> records = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return records;
        }

        String sql =
                "SELECT a.attendance_id, a.student_id, u.first_name, u.last_name, " +
                "       c.course_code, c.course_name, s.session_id, s.session_date, s.session_type, s.session_number, " +
                "       a.status, a.remarks " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN attendance_sessions s ON co.offering_id = s.offering_id " +
                "JOIN attendance a ON s.session_id = a.session_id " +
                "JOIN users u ON a.student_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY s.session_date DESC, s.session_number DESC, c.course_code, a.student_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String attendanceId = rs.getString("attendance_id");
                    String studentId = rs.getString("student_id");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String studentName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String sessionId = rs.getString("session_id");
                    java.sql.Date d = rs.getDate("session_date");
                    String sessionDate = d != null ? d.toString() : "";
                    String sessionType = rs.getString("session_type");
                    int sessionNumber = rs.getInt("session_number");
                    String status = rs.getString("status");
                    String remarks = rs.getString("remarks");

                    records.add(new AttendanceRecord(
                            attendanceId, studentId, studentName,
                            courseCode, courseName, sessionId,
                            sessionDate, sessionType, sessionNumber,
                            status, remarks
                    ));
                }
            }
        }
        return records;
    }

    /**
     * Filters attendance records by course, date, and student name / ID.
     */
    public static List<AttendanceRecord> filterRecords(
            List<AttendanceRecord> loadedRecords,
            String filterCourseCode,
            String filterDate,
            String searchQuery
    ) {
        List<AttendanceRecord> filtered = new ArrayList<>();
        if (loadedRecords == null) return filtered;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";
        String date = filterDate != null ? filterDate.trim() : "";

        for (AttendanceRecord r : loadedRecords) {
            boolean matchesCourse = (filterCourseCode == null || filterCourseCode.equalsIgnoreCase("All Courses") || filterCourseCode.isEmpty())
                    || r.courseCode.equalsIgnoreCase(filterCourseCode);
            boolean matchesDate = date.isEmpty() || r.sessionDate.contains(date);
            boolean matchesSearch = query.isEmpty()
                    || r.studentId.toLowerCase(Locale.ROOT).contains(query)
                    || r.studentName.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesDate && matchesSearch) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    /**
     * Calculates attendance summary statistics.
     */
    public static AttendanceSummary calculateSummary(List<AttendanceRecord> currentList) {
        if (currentList == null || currentList.isEmpty()) {
            return new AttendanceSummary(0, 0, 0, 0, 0.0);
        }

        int present = 0;
        int absent = 0;
        int medical = 0;

        for (AttendanceRecord r : currentList) {
            if ("PRESENT".equalsIgnoreCase(r.status)) {
                present++;
            } else if ("MEDICAL".equalsIgnoreCase(r.status)) {
                medical++;
            } else {
                absent++;
            }
        }

        int total = currentList.size();
        double rate = total > 0 ? ((double) present / (double) total) * 100.0 : 0.0;
        return new AttendanceSummary(total, present, absent, medical, rate);
    }
}

