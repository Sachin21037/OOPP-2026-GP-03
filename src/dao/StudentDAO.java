package dao;

import database.DBConnection;
import model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Data Access Object for Student CRUD operations in FAMS.
 * Interfaces with 'undergraduates' and 'users' tables using PreparedStatement and transactions.
 */
public class StudentDAO {

    private static final String BASE_SELECT =
            "SELECT u.student_id, u.index_no, u.batch, u.academic_year, u.department, u.student_status, " +
            "       usr.user_id, usr.username, usr.password, usr.first_name, usr.last_name, " +
            "       usr.email, usr.phone, usr.address, usr.profile_picture, usr.status, usr.created_at " +
            "FROM undergraduates u " +
            "JOIN users usr ON u.student_id = usr.user_id COLLATE utf8mb4_unicode_ci ";

    /**
     * Retrieves all student records ordered by student ID ascending.
     *
     * @return List of Student objects
     */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY u.student_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }

        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] getAllStudents failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves a single student record by their primary student ID (e.g. STU001).
     *
     * @param studentId unique student ID
     * @return Student object or null if not found
     */
    public Student getStudentById(String studentId) {
        String sql = BASE_SELECT + "WHERE u.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] getStudentById failed for ID " + studentId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Searches and filters students based on query keywords and filter parameters.
     *
     * @param keyword      search keyword matching ID, index, name, email, department, or batch
     * @param studentStatus academic status filter (or null/"ALL")
     * @param accountStatus account status filter (or null/"ALL")
     * @param batchFilter   batch filter (or null/"ALL")
     * @return List of matching Student objects
     */
    public List<Student> searchStudents(String keyword, String studentStatus, String accountStatus, String batchFilter) {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append("AND (u.student_id LIKE ? OR u.index_no LIKE ? OR usr.first_name LIKE ? " +
                       "     OR usr.last_name LIKE ? OR CONCAT(usr.first_name, ' ', usr.last_name) LIKE ? " +
                       "     OR usr.email LIKE ? OR u.department LIKE ? OR u.batch LIKE ?) ");
            for (int i = 0; i < 8; i++) {
                params.add(pattern);
            }
        }

        if (studentStatus != null && !studentStatus.trim().isEmpty() && !studentStatus.equalsIgnoreCase("ALL")) {
            sql.append("AND u.student_status = ? ");
            params.add(studentStatus.trim());
        }

        if (accountStatus != null && !accountStatus.trim().isEmpty() && !accountStatus.equalsIgnoreCase("ALL")) {
            sql.append("AND usr.status = ? ");
            params.add(accountStatus.trim());
        }

        if (batchFilter != null && !batchFilter.trim().isEmpty() && !batchFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND u.batch = ? ");
            params.add(batchFilter.trim());
        }

        sql.append("ORDER BY u.student_id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudent(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] searchStudents failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Checks if a student ID already exists in the system.
     */
    public boolean existsById(String studentId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] existsById check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an index number already exists, optionally excluding a specific student ID (for updates).
     */
    public boolean existsByIndexNo(String indexNo, String excludeStudentId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM undergraduates WHERE index_no = ?");
        if (excludeStudentId != null && !excludeStudentId.isBlank()) {
            sql.append(" AND student_id <> ?");
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, indexNo);
            if (excludeStudentId != null && !excludeStudentId.isBlank()) {
                pstmt.setString(2, excludeStudentId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] existsByIndexNo check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a username already exists, optionally excluding a specific student ID (for updates).
     */
    public boolean existsByUsername(String username, String excludeStudentId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE username = ?");
        if (excludeStudentId != null && !excludeStudentId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, username);
            if (excludeStudentId != null && !excludeStudentId.isBlank()) {
                pstmt.setString(2, excludeStudentId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] existsByUsername check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an email already exists, optionally excluding a specific student ID (for updates).
     */
    public boolean existsByEmail(String email, String excludeStudentId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE email = ?");
        if (excludeStudentId != null && !excludeStudentId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, email);
            if (excludeStudentId != null && !excludeStudentId.isBlank()) {
                pstmt.setString(2, excludeStudentId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] existsByEmail check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates the next sequential Student ID (e.g. STU021) based on current max ID in undergraduates.
     */
    public String getNextSuggestedStudentId() {
        String sql = "SELECT student_id FROM undergraduates WHERE student_id LIKE 'STU%' ORDER BY student_id DESC";
        int maxNum = 0;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && id.startsWith("STU")) {
                    try {
                        int num = Integer.parseInt(id.substring(3).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] Failed to determine next student ID: " + e.getMessage());
        }

        return String.format("STU%03d", maxNum + 1);
    }

    /**
     * Adds a new student record to both 'users' and 'undergraduates' tables atomically inside a transaction.
     *
     * @param student populated Student entity
     * @return true if both inserts succeed
     * @throws SQLException on database error
     */
    public boolean addStudent(Student student) throws SQLException {
        String insertUserSql = "INSERT INTO users " +
                "(user_id, username, password, role, first_name, last_name, email, phone, address, profile_picture, status) " +
                "VALUES (?, ?, ?, 'UNDERGRADUATE', ?, ?, ?, ?, ?, ?, ?)";

        String insertUndergraduateSql = "INSERT INTO undergraduates " +
                "(student_id, index_no, batch, academic_year, department, student_status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Insert into users
            try (PreparedStatement pstmtUser = conn.prepareStatement(insertUserSql)) {
                pstmtUser.setString(1, student.getStudentId());
                pstmtUser.setString(2, student.getUsername());
                pstmtUser.setString(3, student.getPassword());
                pstmtUser.setString(4, student.getFirstName());
                pstmtUser.setString(5, student.getLastName());
                pstmtUser.setString(6, student.getEmail());
                pstmtUser.setString(7, student.getPhone());
                pstmtUser.setString(8, student.getAddress());
                pstmtUser.setString(9, student.getProfilePicture());
                pstmtUser.setString(10, student.getAccountStatus() != null ? student.getAccountStatus() : "ACTIVE");

                int affected = pstmtUser.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("Failed to create user account record for student.");
                }
            }

            // 2. Insert into undergraduates
            try (PreparedStatement pstmtUndergrad = conn.prepareStatement(insertUndergraduateSql)) {
                pstmtUndergrad.setString(1, student.getStudentId());
                pstmtUndergrad.setString(2, student.getIndexNo());
                pstmtUndergrad.setString(3, student.getBatch());
                pstmtUndergrad.setString(4, student.getAcademicYear());
                pstmtUndergrad.setString(5, student.getDepartment());
                pstmtUndergrad.setString(6, student.getStudentStatus() != null ? student.getStudentStatus() : "REGULAR");

                int affected = pstmtUndergrad.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("Failed to create undergraduate record for student.");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[StudentDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Updates an existing student record across 'users' and 'undergraduates' tables within a transaction.
     *
     * @param student Student entity with updated values
     * @return true if update succeeds
     * @throws SQLException on database error
     */
    public boolean updateStudent(Student student) throws SQLException {
        boolean updatePassword = student.getPassword() != null && !student.getPassword().isBlank();

        String updateUserSql = updatePassword
                ? "UPDATE users SET username=?, password=?, first_name=?, last_name=?, email=?, phone=?, address=?, status=? WHERE user_id=?"
                : "UPDATE users SET username=?, first_name=?, last_name=?, email=?, phone=?, address=?, status=? WHERE user_id=?";

        String updateUndergradSql = "UPDATE undergraduates " +
                "SET index_no=?, batch=?, academic_year=?, department=?, student_status=? " +
                "WHERE student_id=?";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Update users
            try (PreparedStatement pstmtUser = conn.prepareStatement(updateUserSql)) {
                int paramIdx = 1;
                pstmtUser.setString(paramIdx++, student.getUsername());
                if (updatePassword) {
                    pstmtUser.setString(paramIdx++, student.getPassword());
                }
                pstmtUser.setString(paramIdx++, student.getFirstName());
                pstmtUser.setString(paramIdx++, student.getLastName());
                pstmtUser.setString(paramIdx++, student.getEmail());
                pstmtUser.setString(paramIdx++, student.getPhone());
                pstmtUser.setString(paramIdx++, student.getAddress());
                pstmtUser.setString(paramIdx++, student.getAccountStatus() != null ? student.getAccountStatus() : "ACTIVE");
                pstmtUser.setString(paramIdx, student.getStudentId());

                pstmtUser.executeUpdate();
            }

            // 2. Update undergraduates
            try (PreparedStatement pstmtUndergrad = conn.prepareStatement(updateUndergradSql)) {
                pstmtUndergrad.setString(1, student.getIndexNo());
                pstmtUndergrad.setString(2, student.getBatch());
                pstmtUndergrad.setString(3, student.getAcademicYear());
                pstmtUndergrad.setString(4, student.getDepartment());
                pstmtUndergrad.setString(5, student.getStudentStatus() != null ? student.getStudentStatus() : "REGULAR");
                pstmtUndergrad.setString(6, student.getStudentId());

                int affected = pstmtUndergrad.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("No undergraduate record found with ID " + student.getStudentId());
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[StudentDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Checks related foreign records across attendance, marks, enrollments, results, medicals.
     * Respects database relational integrity without modifying or deleting related records automatically.
     *
     * @param studentId target student ID
     * @return Map of related table names and record counts
     */
    public Map<String, Integer> getRelatedRecordsSummary(String studentId) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        String[] tables = {"attendance", "marks", "enrollments", "results", "medicals"};

        try (Connection conn = DBConnection.getConnection()) {
            for (String table : tables) {
                String sql = "SELECT COUNT(*) FROM " + table + " WHERE student_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, studentId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            if (count > 0) {
                                summary.put(table, count);
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("[StudentDAO Notice] Could not check table " + table + ": " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO ERROR] getRelatedRecordsSummary failed: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Deletes a student record if and only if no foreign key relations exist.
     * Respects existing foreign-key constraints and does not bypass constraints.
     *
     * @param studentId ID of student to delete
     * @return true if deleted cleanly
     * @throws SQLException on database error
     * @throws IllegalStateException if the student has related foreign records
     */
    public boolean deleteStudent(String studentId) throws SQLException, IllegalStateException {
        // Enforce foreign-key relational safety
        Map<String, Integer> related = getRelatedRecordsSummary(studentId);
        if (!related.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Student ").append(studentId).append(" cannot be deleted because dependent academic records exist: ");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : related.entrySet()) {
                if (!first) msg.append(", ");
                msg.append(entry.getValue()).append(" in ").append(entry.getKey());
                first = false;
            }
            msg.append(". To maintain academic audit history and database constraints, related records cannot be deleted automatically. " +
                       "Please deactivate the student account instead.");
            throw new IllegalStateException(msg.toString());
        }

        String deleteUndergradSql = "DELETE FROM undergraduates WHERE student_id = ?";
        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtUndergrad = conn.prepareStatement(deleteUndergradSql)) {
                pstmtUndergrad.setString(1, studentId);
                pstmtUndergrad.executeUpdate();
            }

            try (PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSql)) {
                pstmtUser.setString(1, studentId);
                pstmtUser.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[StudentDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Maps a SQL ResultSet row to a Student model object.
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getString("student_id"));
        s.setIndexNo(rs.getString("index_no"));
        s.setBatch(rs.getString("batch"));
        s.setAcademicYear(rs.getString("academic_year"));
        s.setDepartment(rs.getString("department"));
        s.setStudentStatus(rs.getString("student_status"));

        s.setUsername(rs.getString("username"));
        s.setPassword(rs.getString("password"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setProfilePicture(rs.getString("profile_picture"));
        s.setAccountStatus(rs.getString("status"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }

    // =========================================================================
    // --- STUDENT PORTAL & DASHBOARD DATA ACCESS METHODS (Preserved from TG2132) ---
    // =========================================================================

    /**
     * Retrieves student profile including undergraduate academic metadata and calculated dashboard metrics.
     */
    public Student getStudentProfile(String studentId) {
        String sql = "SELECT u.*, un.index_no, un.batch, un.academic_year, un.department, un.student_status " +
                     "FROM users u " +
                     "LEFT JOIN undergraduates un ON u.user_id = un.student_id COLLATE utf8mb4_unicode_ci " +
                     "WHERE u.user_id = ?";

        Student student = null;
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        student = new Student();
                        student.setUserId(rs.getString("user_id"));
                        student.setUsername(rs.getString("username"));
                        student.setRole(rs.getString("role"));
                        student.setFirstName(rs.getString("first_name"));
                        student.setLastName(rs.getString("last_name"));
                        student.setEmail(rs.getString("email"));
                        student.setPhone(rs.getString("phone"));
                        student.setAddress(rs.getString("address"));
                        student.setProfilePicture(rs.getString("profile_picture"));
                        student.setStatus(rs.getString("status"));
                        student.setCreatedAt(rs.getTimestamp("created_at"));

                        student.setIndexNo(rs.getString("index_no") != null ? rs.getString("index_no") : "N/A");
                        student.setBatch(rs.getString("batch") != null ? rs.getString("batch") : "N/A");
                        student.setAcademicYear(rs.getString("academic_year") != null ? rs.getString("academic_year") : "N/A");
                        student.setDepartment(rs.getString("department") != null ? rs.getString("department") : "Faculty of Technology");
                        student.setStudentStatus(rs.getString("student_status") != null ? rs.getString("student_status") : "REGULAR");
                    }
                }
            }

            if (student != null) {
                // Populate dashboard metrics
                student.setEnrolledCourseCount(countEnrolledCourses(conn, studentId));
                student.setAttendancePercentage(calculateAttendanceRate(conn, studentId));
                student.setPendingAssignmentCount(countAssignments(conn, studentId));
                double gpa = calculateGPA(conn, studentId);
                student.setCurrentGpa(gpa);
                student.setCurrentCgpa(gpa);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student profile: " + e.getMessage());
        }

        return student;
    }

    private int countEnrolledCourses(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private double calculateAttendanceRate(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) as total, " +
                     "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as presents " +
                     "FROM attendance WHERE student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int presents = rs.getInt("presents");
                    if (total > 0) {
                        return Math.round(((double) presents / total * 100.0) * 10.0) / 10.0;
                    }
                }
            }
        }
        return 0.0;
    }

    private int countAssignments(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM assessments a " +
                     "JOIN course_offerings co ON a.offering_id = co.offering_id COLLATE utf8mb4_unicode_ci " +
                     "JOIN enrollments e ON co.offering_id = e.offering_id " +
                     "WHERE e.student_id = ? AND a.assessment_type IN ('ASSIGNMENT', 'QUIZ')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private double calculateGPA(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT r.grade_point, c.credits FROM results r " +
                     "JOIN course_offerings co ON r.offering_id = co.offering_id COLLATE utf8mb4_unicode_ci " +
                     "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "WHERE r.student_id = ? AND r.grade_point IS NOT NULL";

        double totalPoints = 0.0;
        int totalCredits = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal gp = rs.getBigDecimal("grade_point");
                    int credits = rs.getInt("credits");
                    if (gp != null && credits > 0) {
                        totalPoints += gp.doubleValue() * credits;
                        totalCredits += credits;
                    }
                }
            }
        }

        if (totalCredits > 0) {
            BigDecimal gpa = BigDecimal.valueOf(totalPoints / totalCredits).setScale(2, RoundingMode.HALF_UP);
            return gpa.doubleValue();
        }
        return 0.00;
    }

    /**
     * Retrieves all courses enrolled by the student.
     */
    public List<Course> getEnrolledCourses(String studentId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_code, c.course_name, c.credits, c.department, c.description, " +
                     "co.offering_id, co.academic_year, co.semester, co.batch, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                     "FROM enrollments e " +
                     "JOIN course_offerings co ON e.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "LEFT JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                     "LEFT JOIN users u ON cl.lecturer_id = u.user_id COLLATE utf8mb4_unicode_ci " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY c.course_code";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Course course = new Course();
                        course.setCourseId(rs.getString("course_id"));
                        course.setOfferingId(rs.getString("offering_id"));
                        course.setCourseCode(rs.getString("course_code"));
                        course.setCourseName(rs.getString("course_name"));
                        course.setCredits(rs.getInt("credits"));
                        course.setDepartment(rs.getString("department"));
                        course.setDescription(rs.getString("description"));
                        course.setAcademicYear(rs.getString("academic_year"));
                        course.setSemester(rs.getString("semester"));
                        course.setBatch(rs.getString("batch"));
                        course.setLecturerName(rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Assigned Lecturer");
                        list.add(course);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrolled courses: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves results for the student.
     */
    public List<Result> getStudentResults(String studentId) {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT r.*, c.course_code, c.course_name, c.credits " +
                     "FROM results r " +
                     "JOIN course_offerings co ON r.offering_id = co.offering_id COLLATE utf8mb4_unicode_ci " +
                     "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "WHERE r.student_id = ? " +
                     "ORDER BY c.course_code";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Result result = new Result();
                        result.setResultId(rs.getString("result_id"));
                        result.setStudentId(rs.getString("student_id"));
                        result.setOfferingId(rs.getString("offering_id"));
                        result.setCourseCode(rs.getString("course_code"));
                        result.setCourseName(rs.getString("course_name"));
                        result.setCredits(rs.getInt("credits"));
                        result.setCaMark(rs.getBigDecimal("ca_mark"));
                        result.setFinalExamMark(rs.getBigDecimal("final_exam_mark"));
                        result.setFinalMark(rs.getBigDecimal("final_mark"));
                        result.setGrade(rs.getString("grade"));
                        result.setGradePoint(rs.getBigDecimal("grade_point"));
                        result.setAttendancePercentage(rs.getBigDecimal("attendance_percentage"));
                        result.setCaEligible(rs.getInt("ca_eligible") == 1);
                        result.setFinalEligible(rs.getInt("final_eligible") == 1);
                        result.setSgpa(rs.getBigDecimal("sgpa"));
                        result.setCgpa(rs.getBigDecimal("cgpa"));
                        list.add(result);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student results: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves detailed session attendance records for the student.
     */
    public List<AttendanceRecord> getAttendanceRecords(String studentId) {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT att.attendance_id, att.session_id, att.student_id, att.status, att.remarks, " +
                     "s.offering_id, s.session_date, s.session_type, s.session_number, s.topic, " +
                     "c.course_code, c.course_name " +
                     "FROM attendance att " +
                     "JOIN attendance_sessions s ON att.session_id = s.session_id " +
                     "JOIN course_offerings co ON s.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "WHERE att.student_id = ? " +
                     "ORDER BY s.session_date DESC, s.session_number DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        AttendanceRecord rec = new AttendanceRecord();
                        rec.setAttendanceId(rs.getString("attendance_id"));
                        rec.setSessionId(rs.getString("session_id"));
                        rec.setStudentId(rs.getString("student_id"));
                        rec.setOfferingId(rs.getString("offering_id"));
                        rec.setStatus(rs.getString("status"));
                        rec.setRemarks(rs.getString("remarks"));
                        rec.setSessionDate(rs.getDate("session_date"));
                        rec.setSessionType(rs.getString("session_type"));
                        rec.setSessionNumber(rs.getInt("session_number"));
                        rec.setTopic(rs.getString("topic"));
                        rec.setCourseCode(rs.getString("course_code"));
                        rec.setCourseName(rs.getString("course_name"));
                        list.add(rec);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves lectures/attendance sessions where the student is recorded as ABSENT (lacking attendance).
     */
    public List<AttendanceRecord> getAbsentSessions(String studentId) {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT att.attendance_id, att.session_id, att.student_id, att.status, att.remarks, " +
                     "s.offering_id, s.session_date, s.session_type, s.session_number, s.topic, " +
                     "c.course_code, c.course_name, " +
                     "(SELECT m.approval_status FROM medicals m WHERE m.student_id = att.student_id AND m.offering_id = s.offering_id AND (m.medical_date = s.session_date OR (m.start_date <= s.session_date AND m.end_date >= s.session_date)) LIMIT 1) as medical_status, " +
                     "(SELECT m.medical_id FROM medicals m WHERE m.student_id = att.student_id AND m.offering_id = s.offering_id AND (m.medical_date = s.session_date OR (m.start_date <= s.session_date AND m.end_date >= s.session_date)) LIMIT 1) as linked_medical_id " +
                     "FROM attendance att " +
                     "JOIN attendance_sessions s ON att.session_id = s.session_id " +
                     "JOIN course_offerings co ON s.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE att.student_id = ? AND att.status = 'ABSENT' " +
                     "ORDER BY s.session_date DESC, s.session_number DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        AttendanceRecord rec = new AttendanceRecord();
                        rec.setAttendanceId(rs.getString("attendance_id"));
                        rec.setSessionId(rs.getString("session_id"));
                        rec.setStudentId(rs.getString("student_id"));
                        rec.setOfferingId(rs.getString("offering_id"));
                        rec.setStatus(rs.getString("status"));
                        rec.setRemarks(rs.getString("remarks"));
                        rec.setSessionDate(rs.getDate("session_date"));
                        rec.setSessionType(rs.getString("session_type"));
                        rec.setSessionNumber(rs.getInt("session_number"));
                        rec.setTopic(rs.getString("topic"));
                        rec.setCourseCode(rs.getString("course_code"));
                        rec.setCourseName(rs.getString("course_name"));
                        rec.setMedicalStatus(rs.getString("medical_status"));
                        rec.setMedicalId(rs.getString("linked_medical_id"));
                        list.add(rec);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching absent sessions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves assessments and assignments for student's enrolled courses.
     */
    public List<Assessment> getAssessments(String studentId) {
        List<Assessment> list = new ArrayList<>();
        String sql = "SELECT a.*, c.course_code, c.course_name " +
                     "FROM assessments a " +
                     "JOIN course_offerings co ON a.offering_id = co.offering_id COLLATE utf8mb4_unicode_ci " +
                     "JOIN enrollments e ON co.offering_id = e.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY a.assessment_date ASC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Assessment asm = new Assessment();
                        asm.setAssessmentId(rs.getString("assessment_id"));
                        asm.setOfferingId(rs.getString("offering_id"));
                        asm.setCourseCode(rs.getString("course_code"));
                        asm.setCourseName(rs.getString("course_name"));
                        asm.setAssessmentName(rs.getString("assessment_name"));
                        asm.setAssessmentType(rs.getString("assessment_type"));
                        asm.setWeightPercentage(rs.getBigDecimal("weight_percentage"));
                        asm.setMaxMarks(rs.getBigDecimal("max_marks"));
                        asm.setAssessmentDate(rs.getDate("assessment_date"));
                        asm.setCreatedBy(rs.getString("created_by"));
                        list.add(asm);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assessments: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves materials uploaded for student's enrolled courses.
     */
    public List<CourseMaterial> getCourseMaterials(String studentId) {
        List<CourseMaterial> list = new ArrayList<>();
        String sql = "SELECT cm.*, c.course_code, c.course_name, CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                     "FROM course_materials cm " +
                     "JOIN course_offerings co ON cm.offering_id = co.offering_id " +
                     "JOIN enrollments e ON co.offering_id = e.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "LEFT JOIN users u ON cm.lecturer_id = u.user_id COLLATE utf8mb4_unicode_ci " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY cm.uploaded_at DESC";

        try {
            Connection conn = DBConnection.getConnection();
            if (studentId != null && !studentId.trim().isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, studentId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            CourseMaterial mat = new CourseMaterial();
                            mat.setMaterialId(rs.getString("material_id"));
                            mat.setOfferingId(rs.getString("offering_id"));
                            mat.setLecturerId(rs.getString("lecturer_id"));
                            mat.setLecturerName(rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Lecturer");
                            mat.setCourseCode(rs.getString("course_code"));
                            mat.setCourseName(rs.getString("course_name"));
                            mat.setTitle(rs.getString("title"));
                            mat.setDescription(rs.getString("description"));
                            mat.setFileName(rs.getString("file_name"));
                            mat.setFilePath(rs.getString("file_path"));
                            mat.setUploadedAt(rs.getTimestamp("uploaded_at"));
                            list.add(mat);
                        }
                    }
                }
            }

            // If no enrolled materials found or student is not yet enrolled in specific offerings,
            // query all active course materials in the database so students can access all faculty materials
            if (list.isEmpty()) {
                String fallbackSql = "SELECT cm.*, c.course_code, c.course_name, CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                                     "FROM course_materials cm " +
                                     "LEFT JOIN course_offerings co ON cm.offering_id = co.offering_id " +
                                     "LEFT JOIN courses c ON co.course_id = c.course_id " +
                                     "LEFT JOIN users u ON cm.lecturer_id = u.user_id " +
                                     "ORDER BY cm.uploaded_at DESC";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(fallbackSql)) {
                    while (rs.next()) {
                        CourseMaterial mat = new CourseMaterial();
                        mat.setMaterialId(rs.getString("material_id"));
                        mat.setOfferingId(rs.getString("offering_id"));
                        mat.setLecturerId(rs.getString("lecturer_id"));
                        mat.setLecturerName(rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Faculty Lecturer");
                        mat.setCourseCode(rs.getString("course_code") != null ? rs.getString("course_code") : "ICT2112");
                        mat.setCourseName(rs.getString("course_name") != null ? rs.getString("course_name") : "Course Material");
                        mat.setTitle(rs.getString("title"));
                        mat.setDescription(rs.getString("description"));
                        mat.setFileName(rs.getString("file_name"));
                        mat.setFilePath(rs.getString("file_path"));
                        mat.setUploadedAt(rs.getTimestamp("uploaded_at"));
                        list.add(mat);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course materials: " + e.getMessage());
        }

        // Offline / seed fallback if database is empty or connection is unavailable
        if (list.isEmpty()) {
            list.add(createSampleMaterial("MAT001", "ICT2112", "Object Oriented Programming", "OOP Introduction",
                    "Introduction to object oriented programming concepts: classes, objects, abstraction, encapsulation.",
                    "oop_introduction.pdf", "materials/oop_introduction.pdf", "Dr. Samantha Perera"));
            list.add(createSampleMaterial("MAT002", "ICT2112", "Object Oriented Programming", "Inheritance Notes",
                    "Java inheritance and superclass/subclass concepts, method overriding, and dynamic method dispatch.",
                    "inheritance.pdf", "materials/inheritance.pdf", "Dr. Samantha Perera"));
            list.add(createSampleMaterial("MAT003", "ICT2122", "E-Business Technologies", "E-Business Basics",
                    "Introduction to e-business models, electronic commerce infrastructure, and digital transaction architectures.",
                    "ebusiness_basics.pdf", "materials/ebusiness_basics.pdf", "Prof. K. Wickramasinghe"));
            list.add(createSampleMaterial("MAT004", "ICT2132", "Database Management Systems", "SQL Fundamentals",
                    "SQL commands, DDL/DML, relational algebra, joins, and relational database management concepts.",
                    "sql_fundamentals.pdf", "materials/sql_fundamentals.pdf", "Dr. Nirmal Silva"));
            list.add(createSampleMaterial("MAT005", "ICT2142", "Web Development", "HTML and CSS",
                    "Core web development concepts, semantic HTML5 tags, CSS3 styling, responsive grid, and layout design.",
                    "html_css.pdf", "materials/html_css.pdf", "Dr. Samantha Perera"));
        }
        return list;
    }

    private CourseMaterial createSampleMaterial(String id, String code, String courseName, String title, String desc, String fileName, String filePath, String lecturer) {
        CourseMaterial m = new CourseMaterial();
        m.setMaterialId(id);
        m.setCourseCode(code);
        m.setCourseName(courseName);
        m.setTitle(title);
        m.setDescription(desc);
        m.setFileName(fileName);
        m.setFilePath(filePath);
        m.setLecturerName(lecturer);
        m.setUploadedAt(new java.sql.Timestamp(System.currentTimeMillis() - 86400000L * 2));
        return m;
    }

    /**
     * Retrieves active system notices / faculty announcements.
     */
    public List<Notice> getActiveNotices() {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.*, CONCAT(u.first_name, ' ', u.last_name) as author_name " +
                     "FROM notices n " +
                     "LEFT JOIN users u ON n.created_by = u.user_id " +
                     "WHERE n.status = 'ACTIVE' " +
                     "ORDER BY n.notice_date DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Notice notice = new Notice();
                    notice.setNoticeId(rs.getString("notice_id"));
                    notice.setTitle(rs.getString("title"));
                    notice.setDescription(rs.getString("description"));
                    notice.setNoticeDate(rs.getDate("notice_date"));
                    notice.setCreatedBy(rs.getString("created_by"));
                    notice.setAuthorName(rs.getString("author_name") != null ? rs.getString("author_name") : "Administration");
                    notice.setStatus(rs.getString("status"));
                    list.add(notice);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notices: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates student profile picture path in users table.
     */
    public boolean updateProfilePicture(String userId, String picturePath) {
        String sql = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, picturePath);
                stmt.setString(2, userId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating profile picture: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all medical certificate submissions for a student.
     */
    public List<Medical> getStudentMedicals(String studentId) {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT m.*, c.course_code, c.course_name " +
                     "FROM medicals m " +
                     "JOIN course_offerings co ON m.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "WHERE m.student_id = ? " +
                     "ORDER BY m.medical_date DESC, m.medical_id DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Medical med = new Medical();
                        med.setMedicalId(rs.getString("medical_id"));
                        med.setStudentId(rs.getString("student_id"));
                        med.setOfferingId(rs.getString("offering_id"));
                        med.setCourseCode(rs.getString("course_code"));
                        med.setCourseName(rs.getString("course_name"));
                        med.setMedicalDate(rs.getDate("medical_date"));
                        try {
                            med.setStartDate(rs.getDate("start_date"));
                            med.setEndDate(rs.getDate("end_date"));
                        } catch (Exception ignored) {}
                        med.setReason(rs.getString("reason"));
                        med.setDocumentPath(rs.getString("document_path"));
                        med.setApprovalStatus(rs.getString("approval_status"));
                        med.setApprovedBy(rs.getString("approved_by"));
                        med.setRemarks(rs.getString("remarks"));
                        list.add(med);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medical submissions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Submits a new medical certificate record with support for specific days taken off.
     */
    public boolean submitMedical(Medical medical) {
        String nextIdQuery = "SELECT MAX(CAST(SUBSTRING(medical_id, 4) AS UNSIGNED)) FROM medicals";
        String insertSql = "INSERT INTO medicals (medical_id, student_id, offering_id, medical_date, reason, document_path, approval_status, remarks) " +
                           "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?)";

        try {
            Connection conn = DBConnection.getConnection();

            int nextNum = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(nextIdQuery)) {
                if (rs.next()) {
                    nextNum = rs.getInt(1) + 1;
                }
            }
            String newMedicalId = String.format("MED%03d", nextNum);
            medical.setMedicalId(newMedicalId);

            java.sql.Date mDate = medical.getMedicalDate() != null ? medical.getMedicalDate() :
                    (medical.getStartDate() != null ? medical.getStartDate() : new java.sql.Date(System.currentTimeMillis()));

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, newMedicalId);
                stmt.setString(2, medical.getStudentId());
                stmt.setString(3, medical.getOfferingId());
                stmt.setDate(4, mDate);
                stmt.setString(5, medical.getReason());
                stmt.setString(6, medical.getDocumentPath());
                stmt.setString(7, medical.getRemarks() != null ? medical.getRemarks() : "Submitted for academic verification");
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error submitting medical certificate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing medical leave entry (selected days taken off, offering, reason, document).
     */
    public boolean updateMedical(Medical medical) {
        String updateSql = "UPDATE medicals SET offering_id = ?, medical_date = ?, reason = ?, " +
                           "document_path = COALESCE(?, document_path), remarks = ? " +
                           "WHERE medical_id = ? AND student_id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            java.sql.Date mDate = medical.getMedicalDate() != null ? medical.getMedicalDate() :
                    (medical.getStartDate() != null ? medical.getStartDate() : new java.sql.Date(System.currentTimeMillis()));

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, medical.getOfferingId());
                stmt.setDate(2, mDate);
                stmt.setString(3, medical.getReason());
                stmt.setString(4, medical.getDocumentPath());
                stmt.setString(5, medical.getRemarks() != null ? medical.getRemarks() : "Updated by student");
                stmt.setString(6, medical.getMedicalId());
                stmt.setString(7, medical.getStudentId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating medical certificate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all announcements, including faculty notices and advance assessment alerts (quizzes & assignments).
     */
    public List<Notice> getAllAnnouncements(String studentId) {
        List<Notice> list = new ArrayList<>();

        // 1. Fetch active faculty notices
        String noticeSql = "SELECT n.*, CONCAT(u.first_name, ' ', u.last_name) as author_name, u.role as author_role " +
                           "FROM notices n " +
                           "LEFT JOIN users u ON n.created_by = u.user_id " +
                           "WHERE n.status = 'ACTIVE' " +
                           "ORDER BY n.notice_date DESC";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(noticeSql)) {
                while (rs.next()) {
                    Notice notice = new Notice();
                    notice.setNoticeId(rs.getString("notice_id"));
                    notice.setTitle(rs.getString("title"));
                    notice.setDescription(rs.getString("description"));
                    notice.setNoticeDate(rs.getDate("notice_date"));
                    notice.setCreatedBy(rs.getString("created_by"));
                    String author = rs.getString("author_name");
                    String role = rs.getString("author_role");
                    if (author != null) {
                        notice.setAuthorName(author + (role != null && role.equalsIgnoreCase("LECTURER") ? " (Lecturer)" : " (Faculty Admin)"));
                    } else {
                        notice.setAuthorName("Academic Department");
                    }
                    notice.setStatus(rs.getString("status"));
                    list.add(notice);
                }
            }

            // 2. Fetch advance assessment announcements (quizzes & assignments) for student's enrolled courses
            String asmSql = "SELECT a.*, c.course_code, c.course_name, CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                            "FROM assessments a " +
                            "JOIN course_offerings co ON a.offering_id = co.offering_id COLLATE utf8mb4_unicode_ci " +
                            (studentId != null && !studentId.isEmpty() ? "JOIN enrollments e ON co.offering_id = e.offering_id AND e.student_id = ? " : "") +
                            "JOIN courses c ON co.course_id = c.course_id " +
                            "LEFT JOIN users u ON a.created_by = u.user_id " +
                            "ORDER BY a.assessment_date ASC";
            try (PreparedStatement stmt = conn.prepareStatement(asmSql)) {
                if (studentId != null && !studentId.isEmpty()) {
                    stmt.setString(1, studentId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String asmName = rs.getString("assessment_name");
                        String courseCode = rs.getString("course_code");
                        String type = rs.getString("assessment_type");
                        java.sql.Date date = rs.getDate("assessment_date");
                        BigDecimal weight = rs.getBigDecimal("weight_percentage");
                        BigDecimal maxMarks = rs.getBigDecimal("max_marks");
                        String lecturer = rs.getString("lecturer_name");

                        Notice asmNotice = new Notice();
                        asmNotice.setNoticeId(rs.getString("assessment_id"));
                        asmNotice.setCourseCode(courseCode);
                        asmNotice.setCourseName(rs.getString("course_name"));
                        asmNotice.setOfferingId(rs.getString("offering_id"));
                        asmNotice.setEventDate(date);
                        asmNotice.setNoticeDate(date);
                        asmNotice.setNoticeType(type);
                        asmNotice.setAuthorName(lecturer != null ? lecturer + " (Course Lecturer)" : "Course Lecturer");
                        asmNotice.setCreatedBy(rs.getString("created_by"));

                        String typeDisplay = "QUIZ".equalsIgnoreCase(type) ? "Quiz Announcement" : 
                                             "ASSIGNMENT".equalsIgnoreCase(type) ? "Assignment Notice" : "Assessment Notice";
                        asmNotice.setTitle("[" + courseCode + "] " + typeDisplay + ": " + asmName);

                        String desc = "Advance Announcement for " + courseCode + " - " + rs.getString("course_name") + ".\n" +
                                      "Scheduled Date: " + (date != null ? date.toString() : "TBA") + " | " +
                                      "Weight: " + (weight != null ? weight + "%" : "N/A") + " | " +
                                      "Max Marks: " + (maxMarks != null ? maxMarks : "100") + ".\n" +
                                      "Please ensure thorough preparation beforehand. Contact course lecturer for any clarification.";
                        asmNotice.setDescription(desc);
                        asmNotice.setStatus("ACTIVE");

                        list.add(asmNotice);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching announcements: " + e.getMessage());
        }

        // Sort: items with closest future dates first, then by date descending
        list.sort((n1, n2) -> {
            java.sql.Date d1 = n1.getEventDate() != null ? n1.getEventDate() : n1.getNoticeDate();
            java.sql.Date d2 = n2.getEventDate() != null ? n2.getEventDate() : n2.getNoticeDate();
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d2.compareTo(d1);
        });

        return list;
    }

    /**
     * Allows a lecturer to post an advance announcement.
     * Optionally registers the quiz/assignment in the assessments table so students can track it.
     */
    public boolean postAnnouncement(Notice notice, String offeringId, String assessmentType, BigDecimal weightPercentage, BigDecimal maxMarks, boolean syncWithAssessments) {
        String nextNoticeSql = "SELECT MAX(CAST(SUBSTRING(notice_id, 4) AS UNSIGNED)) FROM notices";
        String insertNoticeSql = "INSERT INTO notices (notice_id, title, description, notice_date, created_by, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try {
            Connection conn = DBConnection.getConnection();

            // Strict Role-Based Access Control: Only users with LECTURER or ADMIN role can post announcements
            String creatorId = notice.getCreatedBy();
            if (creatorId == null || creatorId.trim().isEmpty()) {
                System.err.println("Access Denied: Creator ID is required to post announcements.");
                return false;
            }

            String roleQuery = "SELECT role FROM users WHERE user_id = ?";
            try (PreparedStatement roleStmt = conn.prepareStatement(roleQuery)) {
                roleStmt.setString(1, creatorId);
                try (ResultSet roleRs = roleStmt.executeQuery()) {
                    if (roleRs.next()) {
                        String userRole = roleRs.getString("role");
                        if (!"LECTURER".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
                            System.err.println("Access Denied: User " + creatorId + " has role '" + userRole + "'. Only lecturers can post announcements.");
                            return false;
                        }
                    } else {
                        System.err.println("Access Denied: Creator user ID '" + creatorId + "' not found in database.");
                        return false;
                    }
                }
            }

            // 1. Determine next notice_id
            int nextNum = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(nextNoticeSql)) {
                if (rs.next()) {
                    nextNum = rs.getInt(1) + 1;
                }
            }
            String newNoticeId = String.format("NOT%03d", nextNum);
            notice.setNoticeId(newNoticeId);

            java.sql.Date pDate = notice.getNoticeDate() != null ? notice.getNoticeDate() : new java.sql.Date(System.currentTimeMillis());

            // Insert notice record
            try (PreparedStatement stmt = conn.prepareStatement(insertNoticeSql)) {
                stmt.setString(1, newNoticeId);
                stmt.setString(2, notice.getTitle());
                stmt.setString(3, notice.getDescription());
                stmt.setDate(4, pDate);
                stmt.setString(5, notice.getCreatedBy() != null ? notice.getCreatedBy() : "LEC001");
                stmt.executeUpdate();
            } catch (SQLException ex) {
                // If foreign key constraint triggers due to legacy admin FK, fallback to ADM001 safely
                if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("CONSTRAINT")) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertNoticeSql)) {
                        stmt.setString(1, newNoticeId);
                        stmt.setString(2, notice.getTitle());
                        stmt.setString(3, notice.getDescription());
                        stmt.setDate(4, pDate);
                        stmt.setString(5, "ADM001");
                        stmt.executeUpdate();
                    }
                } else {
                    throw ex;
                }
            }

            // 2. If syncWithAssessments requested and offeringId given, insert into assessments table
            if (syncWithAssessments && offeringId != null && !offeringId.isEmpty()) {
                String nextAsmSql = "SELECT MAX(CAST(SUBSTRING(assessment_id, 4) AS UNSIGNED)) FROM assessments";
                int nextAsmNum = 1;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(nextAsmSql)) {
                    if (rs.next()) {
                        nextAsmNum = rs.getInt(1) + 1;
                    }
                }
                String newAsmId = String.format("ASM%03d", nextAsmNum);

                String insertAsmSql = "INSERT INTO assessments (assessment_id, offering_id, assessment_name, assessment_type, weight_percentage, max_marks, assessment_date, created_by) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertAsmSql)) {
                    stmt.setString(1, newAsmId);
                    stmt.setString(2, offeringId);
                    stmt.setString(3, notice.getTitle());
                    stmt.setString(4, assessmentType != null ? assessmentType : "QUIZ");
                    stmt.setBigDecimal(5, weightPercentage != null ? weightPercentage : new BigDecimal("10.00"));
                    stmt.setBigDecimal(6, maxMarks != null ? maxMarks : new BigDecimal("100.00"));
                    stmt.setDate(7, notice.getEventDate() != null ? notice.getEventDate() : pDate);
                    stmt.setString(8, notice.getCreatedBy() != null ? notice.getCreatedBy() : "LEC001");
                    stmt.executeUpdate();
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error posting announcement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all lecturers for announcement posting and contact information.
     */
    public List<User> getLecturers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, l.department, l.designation " +
                     "FROM users u " +
                     "JOIN lecturers l ON u.user_id = l.lecturer_id " +
                     "WHERE u.status = 'ACTIVE' " +
                     "ORDER BY u.first_name";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getString("user_id"));
                    u.setFirstName(rs.getString("first_name"));
                    u.setLastName(rs.getString("last_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setRole("LECTURER");
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching lecturers: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all course offerings with associated course code, name, and assigned lecturer.
     */
    public List<Course> getAllCourseOfferings() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT co.offering_id, c.course_id, c.course_code, c.course_name, c.credits, " +
                     "co.academic_year, co.semester, co.batch, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                     "FROM course_offerings co " +
                     "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "LEFT JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                     "LEFT JOIN users u ON cl.lecturer_id = u.user_id COLLATE utf8mb4_unicode_ci " +
                     "ORDER BY c.course_code";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Course c = new Course();
                    c.setCourseId(rs.getString("course_id"));
                    c.setCourseCode(rs.getString("course_code"));
                    c.setCourseName(rs.getString("course_name"));
                    c.setCredits(rs.getInt("credits"));
                    c.setOfferingId(rs.getString("offering_id"));
                    c.setLecturerName(rs.getString("lecturer_name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course offerings: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Lecturer Portal Student Management DTOs & Operations
    // -------------------------------------------------------------------------

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
                "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                "JOIN enrollments e ON co.offering_id = e.offering_id " +
                "JOIN undergraduates ug ON e.student_id = ug.student_id COLLATE utf8mb4_unicode_ci " +
                "JOIN users u ON ug.student_id = u.user_id COLLATE utf8mb4_unicode_ci " +
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
