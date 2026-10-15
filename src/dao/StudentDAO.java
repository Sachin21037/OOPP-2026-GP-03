package dao;

import database.DBConnection;
import model.Student;

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
 * Data Access Object for Student CRUD operations in FAMS.
 * Interfaces with 'undergraduates' and 'users' tables using PreparedStatement and transactions.
 */
public class StudentDAO {

    private static final String BASE_SELECT =
            "SELECT u.student_id, u.index_no, u.batch, u.academic_year, u.department, u.student_status, " +
            "       usr.user_id, usr.username, usr.password, usr.first_name, usr.last_name, " +
            "       usr.email, usr.phone, usr.address, usr.profile_picture, usr.status, usr.created_at " +
            "FROM undergraduates u " +
            "JOIN users usr ON u.student_id = usr.user_id ";

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
}
