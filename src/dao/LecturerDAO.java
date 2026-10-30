package dao;

import database.DBConnection;
import model.Lecturer;

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
 * Data Access Object for Lecturer Management CRUD operations in FAMS.
 * Interfaces atomically with 'lecturers' and 'users' tables using PreparedStatement and transactions.
 */
public class LecturerDAO {

    private static final String BASE_SELECT =
            "SELECT l.lecturer_id, l.staff_no, l.department, l.designation, " +
            "       usr.user_id, usr.username, usr.password, usr.first_name, usr.last_name, " +
            "       usr.email, usr.phone, usr.address, usr.profile_picture, usr.status, usr.created_at " +
            "FROM lecturers l " +
            "JOIN users usr ON l.lecturer_id = usr.user_id COLLATE utf8mb4_unicode_ci ";

    /**
     * Retrieves all lecturer records ordered by lecturer ID ascending.
     *
     * @return List of Lecturer objects
     */
    public List<Lecturer> getAllLecturers() {
        List<Lecturer> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY l.lecturer_id ASC";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToLecturer(rs));
            }

        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] getAllLecturers failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves a single lecturer record by their ID (e.g. LEC001).
     *
     * @param lecturerId unique lecturer ID
     * @return Lecturer object or null if not found
     */
    public Lecturer getLecturerById(String lecturerId) {
        String sql = BASE_SELECT + "WHERE l.lecturer_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lecturerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLecturer(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] getLecturerById failed for ID " + lecturerId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Searches and filters lecturers based on query keywords and filter parameters.
     *
     * @param keyword          search keyword matching ID, staff no, name, email, department, or designation
     * @param departmentFilter department filter (or null/"ALL")
     * @param designationFilter designation filter (or null/"ALL")
     * @param accountStatus    account status filter (or null/"ALL")
     * @return List of matching Lecturer objects
     */
    public List<Lecturer> searchLecturers(String keyword, String departmentFilter, String designationFilter, String accountStatus) {
        List<Lecturer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append("AND (l.lecturer_id LIKE ? OR l.staff_no LIKE ? OR usr.first_name LIKE ? " +
                       "     OR usr.last_name LIKE ? OR CONCAT(usr.first_name, ' ', usr.last_name) LIKE ? " +
                       "     OR usr.email LIKE ? OR l.department LIKE ? OR l.designation LIKE ?) ");
            for (int i = 0; i < 8; i++) {
                params.add(pattern);
            }
        }

        if (departmentFilter != null && !departmentFilter.trim().isEmpty() && !departmentFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND l.department = ? ");
            params.add(departmentFilter.trim());
        }

        if (designationFilter != null && !designationFilter.trim().isEmpty() && !designationFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND l.designation = ? ");
            params.add(designationFilter.trim());
        }

        if (accountStatus != null && !accountStatus.trim().isEmpty() && !accountStatus.equalsIgnoreCase("ALL")) {
            sql.append("AND usr.status = ? ");
            params.add(accountStatus.trim());
        }

        sql.append("ORDER BY l.lecturer_id ASC");

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLecturer(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] searchLecturers failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Checks if a lecturer/user ID already exists in the system.
     */
    public boolean existsById(String lecturerId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lecturerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] existsById check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a staff number already exists, optionally excluding a specific lecturer ID (for updates).
     */
    public boolean existsByStaffNo(String staffNo, String excludeLecturerId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM lecturers WHERE staff_no = ?");
        if (excludeLecturerId != null && !excludeLecturerId.isBlank()) {
            sql.append(" AND lecturer_id <> ?");
        }
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, staffNo);
            if (excludeLecturerId != null && !excludeLecturerId.isBlank()) {
                pstmt.setString(2, excludeLecturerId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] existsByStaffNo check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a username already exists, optionally excluding a specific lecturer ID (for updates).
     */
    public boolean existsByUsername(String username, String excludeLecturerId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE username = ?");
        if (excludeLecturerId != null && !excludeLecturerId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, username);
            if (excludeLecturerId != null && !excludeLecturerId.isBlank()) {
                pstmt.setString(2, excludeLecturerId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] existsByUsername check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an email already exists, optionally excluding a specific lecturer ID (for updates).
     */
    public boolean existsByEmail(String email, String excludeLecturerId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE email = ?");
        if (excludeLecturerId != null && !excludeLecturerId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, email);
            if (excludeLecturerId != null && !excludeLecturerId.isBlank()) {
                pstmt.setString(2, excludeLecturerId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] existsByEmail check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates the next sequential Lecturer ID (e.g. LEC006) based on current max ID in lecturers.
     */
    public String getNextSuggestedLecturerId() {
        String sql = "SELECT lecturer_id FROM lecturers WHERE lecturer_id LIKE 'LEC%' ORDER BY lecturer_id DESC";
        int maxNum = 0;

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && id.startsWith("LEC")) {
                    try {
                        int num = Integer.parseInt(id.substring(3).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] Failed to determine next lecturer ID: " + e.getMessage());
        }

        return String.format("LEC%03d", maxNum + 1);
    }

    /**
     * Generates the next suggested staff number (e.g. LEC-STF-006) based on current max in lecturers.
     */
    public String getNextSuggestedStaffNo() {
        String sql = "SELECT staff_no FROM lecturers WHERE staff_no LIKE 'LEC-STF-%' ORDER BY staff_no DESC";
        int maxNum = 0;

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String stf = rs.getString(1);
                if (stf != null && stf.startsWith("LEC-STF-")) {
                    try {
                        int num = Integer.parseInt(stf.substring(8).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] Failed to determine next staff number: " + e.getMessage());
        }

        return String.format("LEC-STF-%03d", maxNum + 1);
    }

    /**
     * Adds a new lecturer record across 'users' and 'lecturers' tables atomically inside a transaction.
     *
     * @param lecturer populated Lecturer entity
     * @return true if both inserts succeed
     * @throws SQLException on database error
     */
    public boolean addLecturer(Lecturer lecturer) throws SQLException {
        String insertUserSql = "INSERT INTO users " +
                "(user_id, username, password, role, first_name, last_name, email, phone, address, profile_picture, status) " +
                "VALUES (?, ?, ?, 'LECTURER', ?, ?, ?, ?, ?, ?, ?)";

        String insertLecturerSql = "INSERT INTO lecturers " +
                "(lecturer_id, staff_no, department, designation) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getNewConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Insert into users
            try (PreparedStatement pstmtUser = conn.prepareStatement(insertUserSql)) {
                pstmtUser.setString(1, lecturer.getLecturerId());
                pstmtUser.setString(2, lecturer.getUsername());
                pstmtUser.setString(3, lecturer.getPassword());
                pstmtUser.setString(4, lecturer.getFirstName());
                pstmtUser.setString(5, lecturer.getLastName());
                pstmtUser.setString(6, lecturer.getEmail());
                pstmtUser.setString(7, lecturer.getPhone());
                pstmtUser.setString(8, lecturer.getAddress());
                pstmtUser.setString(9, lecturer.getProfilePicture());
                pstmtUser.setString(10, lecturer.getAccountStatus() != null ? lecturer.getAccountStatus() : "ACTIVE");

                int affected = pstmtUser.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("Failed to create user account record for lecturer.");
                }
            }

            // 2. Insert into lecturers
            try (PreparedStatement pstmtLecturer = conn.prepareStatement(insertLecturerSql)) {
                pstmtLecturer.setString(1, lecturer.getLecturerId());
                pstmtLecturer.setString(2, lecturer.getStaffNo());
                pstmtLecturer.setString(3, lecturer.getDepartment());
                pstmtLecturer.setString(4, lecturer.getDesignation());

                int affected = pstmtLecturer.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("Failed to create lecturer details record.");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[LecturerDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Updates an existing lecturer record across 'users' and 'lecturers' tables within a transaction.
     *
     * @param lecturer Lecturer entity with updated values
     * @return true if update succeeds
     * @throws SQLException on database error
     */
    public boolean updateLecturer(Lecturer lecturer) throws SQLException {
        boolean updatePassword = lecturer.getPassword() != null && !lecturer.getPassword().isBlank();

        String updateUserSql = updatePassword
                ? "UPDATE users SET username=?, password=?, first_name=?, last_name=?, email=?, phone=?, address=?, status=? WHERE user_id=?"
                : "UPDATE users SET username=?, first_name=?, last_name=?, email=?, phone=?, address=?, status=? WHERE user_id=?";

        String updateLecturerSql = "UPDATE lecturers " +
                "SET staff_no=?, department=?, designation=? " +
                "WHERE lecturer_id=?";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getNewConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Update users
            try (PreparedStatement pstmtUser = conn.prepareStatement(updateUserSql)) {
                int paramIdx = 1;
                pstmtUser.setString(paramIdx++, lecturer.getUsername());
                if (updatePassword) {
                    pstmtUser.setString(paramIdx++, lecturer.getPassword());
                }
                pstmtUser.setString(paramIdx++, lecturer.getFirstName());
                pstmtUser.setString(paramIdx++, lecturer.getLastName());
                pstmtUser.setString(paramIdx++, lecturer.getEmail());
                pstmtUser.setString(paramIdx++, lecturer.getPhone());
                pstmtUser.setString(paramIdx++, lecturer.getAddress());
                pstmtUser.setString(paramIdx++, lecturer.getAccountStatus() != null ? lecturer.getAccountStatus() : "ACTIVE");
                pstmtUser.setString(paramIdx, lecturer.getLecturerId());

                pstmtUser.executeUpdate();
            }

            // 2. Update lecturers
            try (PreparedStatement pstmtLecturer = conn.prepareStatement(updateLecturerSql)) {
                pstmtLecturer.setString(1, lecturer.getStaffNo());
                pstmtLecturer.setString(2, lecturer.getDepartment());
                pstmtLecturer.setString(3, lecturer.getDesignation());
                pstmtLecturer.setString(4, lecturer.getLecturerId());

                int affected = pstmtLecturer.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("No lecturer record found with ID " + lecturer.getLecturerId());
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[LecturerDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Checks related foreign records across course_lecturers, course_materials, assessments, marks.
     * Respects database relational integrity without modifying or deleting related records automatically.
     *
     * @param lecturerId target lecturer ID
     * @return Map of related table names and record counts
     */
    public Map<String, Integer> getRelatedRecordsSummary(String lecturerId) {
        Map<String, Integer> summary = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getNewConnection()) {
            // 1. course_lecturers
            checkCount(conn, "SELECT COUNT(*) FROM course_lecturers WHERE lecturer_id = ?", lecturerId, "course_lecturers", summary);

            // 2. course_materials
            checkCount(conn, "SELECT COUNT(*) FROM course_materials WHERE lecturer_id = ?", lecturerId, "course_materials", summary);

            // 3. assessments
            checkCount(conn, "SELECT COUNT(*) FROM assessments WHERE created_by = ?", lecturerId, "assessments", summary);

            // 4. marks
            checkCount(conn, "SELECT COUNT(*) FROM marks WHERE entered_by = ?", lecturerId, "marks", summary);

        } catch (SQLException e) {
            System.err.println("[LecturerDAO ERROR] getRelatedRecordsSummary failed: " + e.getMessage());
        }

        return summary;
    }

    private void checkCount(Connection conn, String sql, String param, String tableName, Map<String, Integer> summary) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        summary.put(tableName, count);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[LecturerDAO Notice] Could not check table " + tableName + ": " + e.getMessage());
        }
    }

    /**
     * Deletes a lecturer record if and only if no foreign key relations exist.
     * Respects existing foreign-key constraints and does not bypass constraints.
     *
     * @param lecturerId ID of lecturer to delete
     * @return true if deleted cleanly
     * @throws SQLException on database error
     * @throws IllegalStateException if the lecturer has related foreign records
     */
    public boolean deleteLecturer(String lecturerId) throws SQLException, IllegalStateException {
        // Enforce foreign-key relational safety
        Map<String, Integer> related = getRelatedRecordsSummary(lecturerId);
        if (!related.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Lecturer ").append(lecturerId).append(" cannot be deleted because dependent academic records exist: ");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : related.entrySet()) {
                if (!first) msg.append(", ");
                msg.append(entry.getValue()).append(" in ").append(entry.getKey());
                first = false;
            }
            msg.append(". To maintain academic audit history and database constraints, related records cannot be deleted automatically. " +
                       "Please deactivate the lecturer account instead.");
            throw new IllegalStateException(msg.toString());
        }

        String deleteLecturerSql = "DELETE FROM lecturers WHERE lecturer_id = ?";
        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getNewConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtLecturer = conn.prepareStatement(deleteLecturerSql)) {
                pstmtLecturer.setString(1, lecturerId);
                pstmtLecturer.executeUpdate();
            }

            try (PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSql)) {
                pstmtUser.setString(1, lecturerId);
                pstmtUser.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[LecturerDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Sets the account status of a lecturer (ACTIVE or INACTIVE).
     *
     * @param lecturerId target lecturer ID
     * @param status     "ACTIVE" or "INACTIVE"
     * @return true if status was updated
     * @throws SQLException on database error
     */
    public boolean setLecturerStatus(String lecturerId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status != null ? status.toUpperCase() : "INACTIVE");
            pstmt.setString(2, lecturerId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Maps a SQL ResultSet row to a Lecturer model object.
     */
    private Lecturer mapResultSetToLecturer(ResultSet rs) throws SQLException {
        Lecturer l = new Lecturer();
        l.setLecturerId(rs.getString("lecturer_id"));
        l.setStaffNo(rs.getString("staff_no"));
        l.setDepartment(rs.getString("department"));
        l.setDesignation(rs.getString("designation"));

        l.setUsername(rs.getString("username"));
        l.setPassword(rs.getString("password"));
        l.setFirstName(rs.getString("first_name"));
        l.setLastName(rs.getString("last_name"));
        l.setEmail(rs.getString("email"));
        l.setPhone(rs.getString("phone"));
        l.setAddress(rs.getString("address"));
        l.setProfilePicture(rs.getString("profile_picture"));
        l.setAccountStatus(rs.getString("status"));
        l.setCreatedAt(rs.getTimestamp("created_at"));
        return l;
    }
}
