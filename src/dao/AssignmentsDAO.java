package dao;

import database.DBConnection;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * AssignmentsDAO - Data Access Object for Course Assignments.
 * Handles database CRUD operations, foreign-key checks, queries, and ID generation.
 */
public class AssignmentsDAO {

    public static class CourseOfferingItem {
        public final String offeringId;
        public final String courseCode;
        public final String courseName;
        public final String academicYear;
        public final String semester;

        public CourseOfferingItem(String offeringId, String courseCode, String courseName, String academicYear, String semester) {
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.semester = semester != null ? semester : "";
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    public static class AssignmentRecord {
        public final String assignmentId;
        public final String title;
        public final String offeringId;
        public final String courseCode;
        public final String courseName;
        public final String academicYear;
        public final String semester;
        public final String description;
        public final String assignedDate;
        public final String dueDate;
        public final double maxMarks;
        public final double weight;
        public final String status;
        public final String createdBy;

        public AssignmentRecord(
                String assignmentId, String title, String offeringId,
                String courseCode, String courseName, String academicYear,
                String semester, String dueDate, double maxMarks,
                double weight, String createdBy
        ) {
            this.assignmentId = assignmentId != null ? assignmentId : "";
            this.title = title != null ? title : "";
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.semester = semester != null ? semester : "";
            this.description = "Course Continuous Assessment (" + String.format(Locale.US, "%.0f%% weight", weight) + ")";
            this.assignedDate = "-";
            this.dueDate = dueDate != null ? dueDate : "-";
            this.maxMarks = maxMarks;
            this.weight = weight;
            this.status = computeStatus(dueDate);
            this.createdBy = createdBy != null ? createdBy : "";
        }

        public static String computeStatus(String dueDateStr) {
            if (dueDateStr == null || dueDateStr.trim().isEmpty() || "-".equals(dueDateStr.trim())) {
                return "Active";
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                Date due = sdf.parse(dueDateStr.trim());
                Date today = sdf.parse(sdf.format(new Date()));

                long diff = due.getTime() - today.getTime();
                long days = diff / (24 * 60 * 60 * 1000);

                if (days < 0) {
                    return "Closed";
                } else if (days <= 3) {
                    return "Due Soon";
                } else {
                    return "Active";
                }
            } catch (Exception ignored) {
                return "Active";
            }
        }

        public Object[] toTableRow() {
            return new Object[]{
                    assignmentId,
                    title,
                    courseCode + " - " + courseName,
                    description,
                    assignedDate,
                    dueDate,
                    String.format(Locale.US, "%.0f", maxMarks),
                    status
            };
        }
    }

    public static class AssignmentSummary {
        public final int totalAssignments;
        public final int activeCount;
        public final int dueCount;
        public final int closedCount;

        public AssignmentSummary(int totalAssignments, int activeCount, int dueCount, int closedCount) {
            this.totalAssignments = totalAssignments;
            this.activeCount = activeCount;
            this.dueCount = dueCount;
            this.closedCount = closedCount;
        }
    }

    /**
     * Loads assigned course offerings for the lecturer.
     */
    public static List<CourseOfferingItem> loadCourseOfferings(String lecturerId) throws SQLException {
        List<CourseOfferingItem> offerings = new ArrayList<>();
        String safeId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";

        String sql =
                "SELECT DISTINCT co.offering_id, c.course_code, c.course_name, co.academic_year, co.semester " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offerings.add(new CourseOfferingItem(
                            rs.getString("offering_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("academic_year"),
                            rs.getString("semester")
                    ));
                }
            }
        }
        return offerings;
    }

    /**
     * Loads assignments created for lecturer's assigned course offerings.
     */
    public static List<AssignmentRecord> loadAssignments(String lecturerId) throws SQLException {
        List<AssignmentRecord> list = new ArrayList<>();
        String safeId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";

        String sql =
                "SELECT DISTINCT a.assessment_id, a.assessment_name, a.offering_id, " +
                "       c.course_code, c.course_name, co.academic_year, co.semester, " +
                "       a.assessment_date, a.max_marks, a.weight_percentage, a.created_by " +
                "FROM assessments a " +
                "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                "WHERE cl.lecturer_id = ? AND a.assessment_type = 'ASSIGNMENT' " +
                "ORDER BY a.assessment_date DESC, a.assessment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("assessment_date");
                    String dateStr = d != null ? d.toString() : "-";

                    list.add(new AssignmentRecord(
                            rs.getString("assessment_id"),
                            rs.getString("assessment_name"),
                            rs.getString("offering_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("academic_year"),
                            rs.getString("semester"),
                            dateStr,
                            rs.getDouble("max_marks"),
                            rs.getDouble("weight_percentage"),
                            rs.getString("created_by")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Inserts a new assignment into MySQL database.
     */
    public static String insertAssignment(
            String offeringId, String title, double maxMarks,
            double weight, java.sql.Date dueDate, String lecturerId
    ) throws SQLException {
        String insertSql =
                "INSERT INTO assessments (assessment_id, offering_id, assessment_name, assessment_type, weight_percentage, max_marks, assessment_date, created_by) " +
                "VALUES (?, ?, ?, 'ASSIGNMENT', ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            String nextId = getNextAssessmentId(conn);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, nextId);
                ps.setString(2, offeringId != null ? offeringId.trim() : "");
                ps.setString(3, title != null ? title.trim() : "");
                ps.setDouble(4, weight);
                ps.setDouble(5, maxMarks);
                if (dueDate != null) {
                    ps.setDate(6, dueDate);
                } else {
                    ps.setNull(6, Types.DATE);
                }
                ps.setString(7, lecturerId != null ? lecturerId.trim() : "LEC001");

                ps.executeUpdate();
            }
            return nextId;
        }
    }

    /**
     * Updates an existing assignment in MySQL database.
     */
    public static boolean updateAssignment(
            String assignmentId, String offeringId, String title, double maxMarks,
            double weight, java.sql.Date dueDate, String lecturerId
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE assessments SET assessment_name = ?, weight_percentage = ?, max_marks = ?, assessment_date = ?");
        boolean hasOffering = offeringId != null && !offeringId.trim().isEmpty();
        if (hasOffering) {
            sql.append(", offering_id = ?");
        }
        sql.append(" WHERE assessment_id = ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, title != null ? title.trim() : "");
            ps.setDouble(2, weight);
            ps.setDouble(3, maxMarks);
            if (dueDate != null) {
                ps.setDate(4, dueDate);
            } else {
                ps.setNull(4, Types.DATE);
            }

            int pIdx = 5;
            if (hasOffering) {
                ps.setString(pIdx++, offeringId.trim());
            }
            ps.setString(pIdx, assignmentId != null ? assignmentId.trim() : "");

            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    /**
     * Overloaded method for backward compatibility.
     */
    public static boolean updateAssignment(
            String assignmentId, String title, double maxMarks,
            double weight, java.sql.Date dueDate, String lecturerId
    ) throws SQLException {
        return updateAssignment(assignmentId, null, title, maxMarks, weight, dueDate, lecturerId);
    }

    /**
     * Checks if marks exist for this assignment (foreign key check).
     */
    public static int countSubmittedMarks(String assignmentId) throws SQLException {
        if (assignmentId == null || assignmentId.trim().isEmpty()) return 0;
        String sql = "SELECT COUNT(*) FROM marks WHERE assessment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assignmentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Deletes an assignment from MySQL database in a transaction,
     * safely cleaning up any child marks first to maintain integrity.
     */
    public static boolean deleteAssignment(String assignmentId) throws SQLException {
        if (assignmentId == null || assignmentId.trim().isEmpty()) return false;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete child marks first
                try (PreparedStatement psMarks = conn.prepareStatement("DELETE FROM marks WHERE assessment_id = ?")) {
                    psMarks.setString(1, assignmentId.trim());
                    psMarks.executeUpdate();
                }

                // Delete assessment
                int affected;
                try (PreparedStatement psAsm = conn.prepareStatement("DELETE FROM assessments WHERE assessment_id = ?")) {
                    psAsm.setString(1, assignmentId.trim());
                    affected = psAsm.executeUpdate();
                }

                conn.commit();
                return affected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Overloaded delete method with lecturerId.
     */
    public static boolean deleteAssignment(String assignmentId, String lecturerId) throws SQLException {
        return deleteAssignment(assignmentId);
    }

    /**
     * Filters assignments by course, semester, status, and search query.
     */
    public static List<AssignmentRecord> filterAssignments(
            List<AssignmentRecord> loadedAssignments,
            String filterCourseCode,
            String filterSemester,
            String filterStatus,
            String searchQuery
    ) {
        List<AssignmentRecord> filtered = new ArrayList<>();
        if (loadedAssignments == null) return filtered;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";

        for (AssignmentRecord a : loadedAssignments) {
            boolean matchesCourse = (filterCourseCode == null || filterCourseCode.equals("All Courses") || filterCourseCode.isEmpty())
                    || a.courseCode.equalsIgnoreCase(filterCourseCode);

            boolean matchesSem = (filterSemester == null || filterSemester.equals("All Semesters") || filterSemester.isEmpty())
                    || a.semester.equalsIgnoreCase(filterSemester);

            boolean matchesStatus = (filterStatus == null || filterStatus.equals("All Statuses") || filterStatus.isEmpty())
                    || a.status.equalsIgnoreCase(filterStatus)
                    || ("Due Soon".equalsIgnoreCase(filterStatus) && ("Due".equalsIgnoreCase(a.status) || "Due Soon".equalsIgnoreCase(a.status)));

            boolean matchesSearch = query.isEmpty()
                    || a.title.toLowerCase(Locale.ROOT).contains(query)
                    || a.assignmentId.toLowerCase(Locale.ROOT).contains(query)
                    || a.courseCode.toLowerCase(Locale.ROOT).contains(query)
                    || a.courseName.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesSem && matchesStatus && matchesSearch) {
                filtered.add(a);
            }
        }
        return filtered;
    }

    /**
     * Calculates summary metrics for assignment list.
     */
    public static AssignmentSummary calculateSummary(List<AssignmentRecord> list) {
        if (list == null || list.isEmpty()) {
            return new AssignmentSummary(0, 0, 0, 0);
        }
        int active = 0, due = 0, closed = 0;
        for (AssignmentRecord a : list) {
            if ("Active".equalsIgnoreCase(a.status) || "Upcoming".equalsIgnoreCase(a.status)) {
                active++;
            } else if ("Due Soon".equalsIgnoreCase(a.status) || "Due".equalsIgnoreCase(a.status)) {
                due++;
            } else if ("Closed".equalsIgnoreCase(a.status)) {
                closed++;
            }
        }
        return new AssignmentSummary(list.size(), active, due, closed);
    }

    private static String getNextAssessmentId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(assessment_id, 4) AS UNSIGNED)), 0) + 1 FROM assessments WHERE assessment_id LIKE 'ASM%'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                long nextNum = rs.getLong(1);
                return String.format(Locale.US, "ASM%03d", nextNum);
            }
        }
        return "ASM001";
    }
}
