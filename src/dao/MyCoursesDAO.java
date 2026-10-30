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
 * MyCoursesDAO - Data Access Object for Lecturer's Assigned Courses.
 * Handles database retrieval, formatting, filtering, and summary metrics.
 */
public class MyCoursesDAO {

    public static class CourseItem {
        public final String courseCode;
        public final String courseName;
        public final int studentCount;
        public final String semester;
        public final String academicYear;
        public final int credits;
        public final String batch;

        public CourseItem(
                String courseCode, String courseName, int studentCount,
                String semester, String academicYear, int credits, String batch
        ) {
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.studentCount = studentCount;
            this.semester = semester != null ? semester : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.credits = credits;
            this.batch = batch != null ? batch : "";
        }

        public Object[] toTableRow() {
            return new Object[]{courseCode, courseName, studentCount, semester};
        }
    }

    public static class CoursesSummary {
        public final int totalCourses;
        public final int totalStudents;

        public CoursesSummary(int totalCourses, int totalStudents) {
            this.totalCourses = totalCourses;
            this.totalStudents = totalStudents;
        }
    }

    public static String formatSemester(String raw) {
        if (raw == null) return "Semester 1";
        String trimmed = raw.trim();
        if (trimmed.equalsIgnoreCase("SEMESTER 1") || trimmed.equalsIgnoreCase("SEM 1")) {
            return "Semester 1";
        } else if (trimmed.equalsIgnoreCase("SEMESTER 2") || trimmed.equalsIgnoreCase("SEM 2")) {
            return "Semester 2";
        }
        return trimmed;
    }

    /**
     * Loads assigned courses for the lecturer from MySQL database.
     */
    public static List<CourseItem> loadCourses(String lecturerId) throws SQLException {
        List<CourseItem> list = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return list;
        }

        String sql =
                "SELECT c.course_code, c.course_name, co.semester, co.academic_year, co.batch, c.credits, " +
                "       COUNT(DISTINCT e.student_id) AS student_count " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "LEFT JOIN enrollments e ON co.offering_id = e.offering_id " +
                "WHERE cl.lecturer_id = ? " +
                "GROUP BY c.course_code, c.course_name, co.semester, co.academic_year, co.batch, c.credits, co.offering_id " +
                "ORDER BY co.semester, c.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    String rawSemester = rs.getString("semester");
                    String formattedSemester = formatSemester(rawSemester);
                    String academicYear = rs.getString("academic_year");
                    String batch = rs.getString("batch");
                    int credits = rs.getInt("credits");
                    int students = rs.getInt("student_count");

                    list.add(new CourseItem(
                            code, name, students, formattedSemester, academicYear, credits, batch
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Filters courses by semester and search keyword.
     */
    public static List<CourseItem> filterCourses(
            List<CourseItem> loadedCourses,
            String semesterFilter,
            String searchQuery
    ) {
        List<CourseItem> result = new ArrayList<>();
        if (loadedCourses == null) return result;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";

        for (CourseItem course : loadedCourses) {
            boolean matchesText = query.isEmpty()
                    || course.courseCode.toLowerCase(Locale.ROOT).contains(query)
                    || course.courseName.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesSemester = semesterFilter == null
                    || "All Semesters".equalsIgnoreCase(semesterFilter)
                    || semesterFilter.equalsIgnoreCase(course.semester);

            if (matchesText && matchesSemester) {
                result.add(course);
            }
        }
        return result;
    }

    /**
     * Calculates summary metrics for courses list.
     */
    public static CoursesSummary calculateSummary(List<CourseItem> list) {
        if (list == null || list.isEmpty()) {
            return new CoursesSummary(0, 0);
        }
        int totalStudents = 0;
        for (CourseItem c : list) {
            totalStudents += c.studentCount;
        }
        return new CoursesSummary(list.size(), totalStudents);
    }
}

