package dao;

import database.DBConnection;
import model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Academic Courses in FAMS.
 * Interfaces with the 'courses' table and checks dependent 'course_offerings' relationships.
 */
public class CourseDAO {

    private static final String BASE_SELECT =
            "SELECT c.course_id, c.course_code, c.course_name, c.credits, c.description, c.department, " +
            "       COUNT(co.offering_id) AS offering_count, " +
            "       GROUP_CONCAT(CONCAT(co.academic_year, ' (', co.semester, ')') ORDER BY co.academic_year DESC, co.semester ASC SEPARATOR ', ') AS offering_details " +
            "FROM courses c " +
            "LEFT JOIN course_offerings co ON c.course_id COLLATE utf8mb4_unicode_ci = co.course_id ";

    /**
     * Retrieves all courses ordered by course code.
     */
    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = BASE_SELECT +
                "GROUP BY c.course_id, c.course_code, c.course_name, c.credits, c.description, c.department " +
                "ORDER BY c.course_code ASC";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] getAllCourses failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves a single course by its Course ID (e.g. CRS001).
     */
    public Course getCourseById(String courseId) {
        if (courseId == null || courseId.isBlank()) return null;
        String sql = BASE_SELECT +
                "WHERE c.course_id = ? " +
                "GROUP BY c.course_id, c.course_code, c.course_name, c.credits, c.description, c.department";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseId.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] getCourseById failed for " + courseId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Retrieves a single course by its unique Course Code (e.g. ICT2132).
     */
    public Course getCourseByCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) return null;
        String sql = BASE_SELECT +
                "WHERE c.course_code = ? " +
                "GROUP BY c.course_id, c.course_code, c.course_name, c.credits, c.description, c.department";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseCode.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] getCourseByCode failed for " + courseCode + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Searches courses by code, name, description, and department.
     */
    public List<Course> searchCourses(String keyword, String departmentFilter) {
        List<Course> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append("AND (c.course_code LIKE ? OR c.course_name LIKE ? OR c.description LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (departmentFilter != null && !departmentFilter.trim().isEmpty()
                && !departmentFilter.equalsIgnoreCase("ALL")
                && !departmentFilter.equalsIgnoreCase("All Departments")) {
            sql.append("AND c.department = ? ");
            params.add(departmentFilter.trim());
        }

        sql.append("GROUP BY c.course_id, c.course_code, c.course_name, c.credits, c.description, c.department ");
        sql.append("ORDER BY c.course_code ASC");

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] searchCourses failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Checks if a course code already exists, optionally excluding a specific course ID.
     */
    public boolean existsByCourseCode(String courseCode, String excludeCourseId) {
        if (courseCode == null || courseCode.isBlank()) return false;
        StringBuilder sql = new StringBuilder("SELECT 1 FROM courses WHERE course_code = ?");
        if (excludeCourseId != null && !excludeCourseId.isBlank()) {
            sql.append(" AND course_id <> ?");
        }

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, courseCode.trim());
            if (excludeCourseId != null && !excludeCourseId.isBlank()) {
                pstmt.setString(2, excludeCourseId.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] existsByCourseCode failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Suggests the next sequential Course ID (e.g. CRS006).
     */
    public String getNextSuggestedCourseId() {
        String sql = "SELECT course_id FROM courses WHERE course_id LIKE 'CRS%' ORDER BY LENGTH(course_id) DESC, course_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String lastId = rs.getString("course_id");
                if (lastId != null && lastId.startsWith("CRS")) {
                    try {
                        int num = Integer.parseInt(lastId.substring(3).trim());
                        return String.format("CRS%03d", num + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] getNextSuggestedCourseId failed: " + e.getMessage());
        }

        return "CRS006";
    }

    /**
     * Inserts a new course into the 'courses' table.
     */
    public boolean addCourse(Course course) throws SQLException {
        if (course == null) return false;

        String sql = "INSERT INTO courses (course_id, course_code, course_name, credits, description, department) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseId().trim());
            pstmt.setString(2, course.getCourseCode().trim());
            pstmt.setString(3, course.getCourseName().trim());
            pstmt.setInt(4, course.getCredits());
            pstmt.setString(5, course.getDescription() != null ? course.getDescription().trim() : null);
            pstmt.setString(6, course.getDepartment() != null ? course.getDepartment().trim() : "Information and Communication Technology");

            int affected = pstmt.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Updates an existing course's details.
     */
    public boolean updateCourse(Course course) throws SQLException {
        if (course == null || course.getCourseId() == null) return false;

        String sql = "UPDATE courses SET " +
                     "    course_code = ?, " +
                     "    course_name = ?, " +
                     "    credits = ?, " +
                     "    description = ?, " +
                     "    department = ? " +
                     "WHERE course_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseCode().trim());
            pstmt.setString(2, course.getCourseName().trim());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDescription() != null ? course.getDescription().trim() : null);
            pstmt.setString(5, course.getDepartment() != null ? course.getDepartment().trim() : "Information and Communication Technology");
            pstmt.setString(6, course.getCourseId().trim());

            int affected = pstmt.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Inspects foreign references to verify if a course can be safely deleted.
     */
    public Map<String, Integer> getDependentRecordsSummary(String courseId) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        if (courseId == null || courseId.isBlank()) return summary;

        String sqlOfferings = "SELECT COUNT(*) FROM course_offerings WHERE course_id = ?";
        String sqlEnrollments = "SELECT COUNT(*) FROM enrollments e JOIN course_offerings co ON e.offering_id = co.offering_id WHERE co.course_id = ?";
        String sqlMaterials = "SELECT COUNT(*) FROM course_materials cm JOIN course_offerings co ON cm.offering_id = co.offering_id WHERE co.course_id = ?";
        String sqlAssessments = "SELECT COUNT(*) FROM assessments a JOIN course_offerings co ON a.offering_id = co.offering_id WHERE co.course_id = ?";
        String sqlAttendance = "SELECT COUNT(*) FROM attendance_sessions ses JOIN course_offerings co ON ses.offering_id = co.offering_id WHERE co.course_id = ?";

        try (Connection conn = DBConnection.getNewConnection()) {
            checkDependency(conn, sqlOfferings, courseId, "course_offerings", summary);
            if (summary.containsKey("course_offerings")) {
                summary.put("offerings", summary.get("course_offerings"));
            }
            checkDependency(conn, sqlEnrollments, courseId, "enrollments", summary);
            checkDependency(conn, sqlMaterials, courseId, "course_materials", summary);
            checkDependency(conn, sqlAssessments, courseId, "assessments", summary);
            checkDependency(conn, sqlAttendance, courseId, "attendance_sessions", summary);
        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] getDependentRecordsSummary failed: " + e.getMessage());
        }

        return summary;
    }

    private void checkDependency(Connection conn, String sql, String courseId, String label, Map<String, Integer> map) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        map.put(label, count);
                    }
                }
            }
        } catch (SQLException ignored) {}
    }

    /**
     * Safely deletes a course if and only if no semester offerings exist.
     */
    public boolean deleteCourse(String courseId) throws SQLException {
        if (courseId == null || courseId.isBlank()) return false;
        courseId = courseId.trim();

        Map<String, Integer> related = getDependentRecordsSummary(courseId);
        if (!related.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Course ").append(courseId).append(" cannot be deleted because dependent academic records exist: ");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : related.entrySet()) {
                if (!first) msg.append(", ");
                msg.append(entry.getValue()).append(" in ").append(entry.getKey());
                first = false;
            }
            msg.append(". To maintain curriculum and academic integrity, remove semester offerings first.");
            throw new IllegalStateException(msg.toString());
        }

        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Calculates real-time course summary metrics:
     * - total, offered, unscheduled, total_credits
     */
    public Map<String, Integer> getCourseSummaryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("total", 0);
        counts.put("offered", 0);
        counts.put("unscheduled", 0);
        counts.put("total_credits", 0);

        String sql = "SELECT " +
                     "    COUNT(c.course_id) AS total_courses, " +
                     "    SUM(CASE WHEN co_cnt.cnt > 0 THEN 1 ELSE 0 END) AS offered_courses, " +
                     "    SUM(CASE WHEN co_cnt.cnt IS NULL OR co_cnt.cnt = 0 THEN 1 ELSE 0 END) AS unscheduled_courses, " +
                     "    COALESCE(SUM(c.credits), 0) AS total_credits " +
                     "FROM courses c " +
                     "LEFT JOIN (SELECT course_id, COUNT(*) AS cnt FROM course_offerings GROUP BY course_id) co_cnt ON c.course_id COLLATE utf8mb4_unicode_ci = co_cnt.course_id";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                counts.put("total", rs.getInt("total_courses"));
                int offered = rs.getInt("offered_courses");
                counts.put("offered", offered);
                counts.put("active", offered);
                counts.put("unscheduled", rs.getInt("unscheduled_courses"));
                int credits = rs.getInt("total_credits");
                counts.put("total_credits", credits);
                counts.put("credits", credits);
            }

        } catch (SQLException e) {
            System.err.println("[CourseDAO ERROR] getCourseSummaryCounts failed: " + e.getMessage());
        }

        return counts;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getString("course_id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setCourseName(rs.getString("course_name"));
        c.setCredits(rs.getInt("credits"));
        c.setDescription(rs.getString("description"));
        c.setDepartment(rs.getString("department"));
        c.setOfferingCount(rs.getInt("offering_count"));
        c.setOfferingDetails(rs.getString("offering_details"));
        return c;
    }
}
