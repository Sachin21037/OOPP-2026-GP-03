package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * StudentDAO - Data Access Object for Student Information Management.
 * Handles database retrieval of enrolled undergraduates, filtering, and summary statistics.
 */
public class StudentDAO {

    public static class StudentItem {
        public final String studentId;
        public final String indexNumber;
        public final String studentName;
        public final String courseCode;
        public final String courseName;
        public final String batch;
        public final String academicYear;
        public final String department;
        public final String status;
        public final String email;
        public final String studentStatus;

        public StudentItem(
                String studentId, String indexNumber, String studentName,
                String courseCode, String courseName, String batch,
                String academicYear, String department,
                String status, String email, String studentStatus
        ) {
            this.studentId = studentId != null ? studentId : "";
            this.indexNumber = indexNumber != null ? indexNumber : "";
            this.studentName = studentName != null ? studentName : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.batch = batch != null ? batch : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.department = department != null ? department : "";
            this.status = status != null ? status : "Active";
            this.email = email != null ? email : "";
            this.studentStatus = studentStatus != null ? studentStatus : "";
        }

        public Object[] toTableRow() {
            return new Object[]{studentId, indexNumber, studentName, courseCode, batch, status};
        }
    }

    public static class StudentsSummary {
        public final int totalStudents;
        public final int activeStudents;
        public final int courseCount;

        public StudentsSummary(int totalStudents, int activeStudents, int courseCount) {
            this.totalStudents = totalStudents;
            this.activeStudents = activeStudents;
            this.courseCount = courseCount;
        }
    }

    /**
     * Loads enrolled undergraduate students for lecturer's course offerings from MySQL.
     */
    public static List<StudentItem> loadStudents(String lecturerId) throws SQLException {
        List<StudentItem> list = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return list;
        }

        String sql =
                "SELECT ug.student_id, ug.index_no, u.first_name, u.last_name, u.email, " +
                "       c.course_code, c.course_name, ug.batch, ug.academic_year, ug.department, " +
                "       ug.student_status, u.status AS user_status, e.status AS enrollment_status " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN enrollments e ON co.offering_id = e.offering_id " +
                "JOIN undergraduates ug ON e.student_id = ug.student_id " +
                "JOIN users u ON ug.student_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code, ug.student_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String indexNo = rs.getString("index_no");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String email = rs.getString("email");
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String batch = rs.getString("batch");
                    String academicYear = rs.getString("academic_year");
                    String department = rs.getString("department");
                    String studentStatus = rs.getString("student_status");
                    String userStatus = rs.getString("user_status");

                    String displayStatus = "Active";
                    if (userStatus != null && !userStatus.equalsIgnoreCase("ACTIVE")) {
                        displayStatus = "Inactive";
                    }

                    list.add(new StudentItem(
                            studentId, indexNo, fullName, courseCode, courseName,
                            batch, academicYear, department, displayStatus, email, studentStatus
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Filters students based on course, batch, and search query.
     */
    public static List<StudentItem> filterStudents(
            List<StudentItem> loadedStudents,
            String selectedCourse,
            String selectedBatch,
            String searchQuery
    ) {
        List<StudentItem> result = new ArrayList<>();
        if (loadedStudents == null) return result;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";

        for (StudentItem s : loadedStudents) {
            boolean matchesSearch = query.isEmpty()
                    || s.studentId.toLowerCase(Locale.ROOT).contains(query)
                    || s.indexNumber.toLowerCase(Locale.ROOT).contains(query)
                    || s.studentName.toLowerCase(Locale.ROOT).contains(query)
                    || s.email.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesCourse = selectedCourse == null
                    || "All Courses".equalsIgnoreCase(selectedCourse)
                    || s.courseCode.equalsIgnoreCase(selectedCourse);

            boolean matchesBatch = selectedBatch == null
                    || "All Batches".equalsIgnoreCase(selectedBatch)
                    || s.batch.equalsIgnoreCase(selectedBatch);

            if (matchesSearch && matchesCourse && matchesBatch) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Calculates summary metrics for students.
     */
    public static StudentsSummary calculateSummary(List<StudentItem> list) {
        if (list == null || list.isEmpty()) {
            return new StudentsSummary(0, 0, 0);
        }

        Set<String> uniqueStudents = new HashSet<>();
        int activeCount = 0;
        Set<String> courses = new HashSet<>();

        for (StudentItem s : list) {
            uniqueStudents.add(s.studentId);
            if ("Active".equalsIgnoreCase(s.status)) {
                activeCount++;
            }
            if (!s.courseCode.isEmpty()) {
                courses.add(s.courseCode);
            }
        }

        return new StudentsSummary(uniqueStudents.size(), activeCount, courses.size());
    }
}

