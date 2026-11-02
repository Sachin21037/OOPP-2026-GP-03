package dao;

import database.DBConnection;
import model.TechnicalOfficer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Technical Officer Management CRUD operations in FAMS.
 * Interfaces atomically with 'technical_officers' and 'users' tables using PreparedStatement and transactions.
 */
public class TechnicalOfficerDAO {

    private static TechnicalOfficer mockOfficer;
    private static String currentStoredPassword = "TO@123";
    private static final List<String[]> auditLogs = new ArrayList<>();
    private static final SimpleDateFormat LOG_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    static {
        initMockData();
    }

    private static void initMockData() {
        mockOfficer = new TechnicalOfficer(
                "TO001",
                "TO-STF-001",
                "to001",
                "Dinesh",
                "Kumara",
                "dinesh.to@uor.lk",
                "0710000007",
                "Information and Communication Technology",
                "Faculty of Technology, University of Ruhuna, Karagoda Uyangoda, Kamburupitiya",
                "Room A-204, IT Technical Office, Block A",
                "Technical Officer (Grade II / Lab Supervisor)",
                "Faculty of Technology",
                "ACTIVE",
                null,
                "2026-10-07 14:51:10"
        );

        auditLogs.add(new String[]{"2026-03-27 10:15", "Routine Maintenance", "Completed weekly preventative check in Computer Lab 01", "Success"});
        auditLogs.add(new String[]{"2026-03-26 14:30", "Medical Verification", "Verified and approved medical certificate MED001 for STU005", "Approved"});
        auditLogs.add(new String[]{"2026-03-25 11:00", "Fault Ticket", "Assigned Router hardware maintenance ticket MNT001 for Lab 03", "In Progress"});
        auditLogs.add(new String[]{"2026-03-24 09:00", "Attendance Session", "Conducted practical session 01 for ICT2132 in Software Lab 02", "Logged"});
        auditLogs.add(new String[]{"2026-03-22 16:20", "Workstation Audit", "Updated workstation operational inventory for Embedded Lab 04", "Completed"});
        auditLogs.add(new String[]{"2026-03-20 08:45", "System Login", "Authenticated successfully into FAMS Technical Officer Terminal", "Success"});
    }

    private static final String BASE_SELECT =
            "SELECT t.technical_officer_id, t.staff_no, t.department, " +
            "       usr.user_id, usr.username, usr.password, usr.first_name, usr.last_name, " +
            "       usr.email, usr.phone, usr.address, usr.profile_picture, usr.status, usr.created_at " +
            "FROM technical_officers t " +
            "JOIN users usr ON t.technical_officer_id = usr.user_id COLLATE utf8mb4_unicode_ci ";

    /**
     * Retrieves all technical officer records ordered by ID ascending.
     *
     * @return List of TechnicalOfficer objects
     */
    public List<TechnicalOfficer> getAllTechnicalOfficers() {
        List<TechnicalOfficer> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY t.technical_officer_id ASC";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTechnicalOfficer(rs));
            }

        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] getAllTechnicalOfficers failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves a single technical officer record by their ID (e.g. TO001).
     *
     * @param toId unique technical officer ID
     * @return TechnicalOfficer object or null if not found
     */
    public TechnicalOfficer getTechnicalOfficerById(String toId) {
        String sql = BASE_SELECT + "WHERE t.technical_officer_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, toId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTechnicalOfficer(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] getTechnicalOfficerById failed for ID " + toId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Searches and filters technical officers based on query keywords and filter parameters.
     *
     * @param keyword          search keyword matching ID, staff no, name, email, or department
     * @param departmentFilter department filter (or null/"ALL")
     * @param accountStatus    account status filter (or null/"ALL")
     * @return List of matching TechnicalOfficer objects
     */
    public List<TechnicalOfficer> searchTechnicalOfficers(String keyword, String departmentFilter, String accountStatus) {
        List<TechnicalOfficer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append("AND (t.technical_officer_id LIKE ? OR t.staff_no LIKE ? OR usr.username LIKE ? " +
                       "     OR usr.first_name LIKE ? OR usr.last_name LIKE ? OR CONCAT(usr.first_name, ' ', usr.last_name) LIKE ? " +
                       "     OR usr.email LIKE ? OR t.department LIKE ?) ");
            for (int i = 0; i < 8; i++) {
                params.add(pattern);
            }
        }

        if (departmentFilter != null && !departmentFilter.trim().isEmpty() && !departmentFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND t.department = ? ");
            params.add(departmentFilter.trim());
        }

        if (accountStatus != null && !accountStatus.trim().isEmpty() && !accountStatus.equalsIgnoreCase("ALL")) {
            sql.append("AND usr.status = ? ");
            params.add(accountStatus.trim());
        }

        sql.append("ORDER BY t.technical_officer_id ASC");

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTechnicalOfficer(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] searchTechnicalOfficers failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Checks if a technical officer / user ID already exists in the system.
     */
    public boolean existsById(String toId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, toId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] existsById check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a staff number already exists, optionally excluding a specific ID (for updates).
     */
    public boolean existsByStaffNo(String staffNo, String excludeId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM technical_officers WHERE staff_no = ?");
        if (excludeId != null && !excludeId.isBlank()) {
            sql.append(" AND technical_officer_id <> ?");
        }
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, staffNo);
            if (excludeId != null && !excludeId.isBlank()) {
                pstmt.setString(2, excludeId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] existsByStaffNo check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a username already exists, optionally excluding a specific ID (for updates).
     */
    public boolean existsByUsername(String username, String excludeId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE username = ?");
        if (excludeId != null && !excludeId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, username);
            if (excludeId != null && !excludeId.isBlank()) {
                pstmt.setString(2, excludeId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] existsByUsername check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an email already exists, optionally excluding a specific ID (for updates).
     */
    public boolean existsByEmail(String email, String excludeId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE email = ?");
        if (excludeId != null && !excludeId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, email);
            if (excludeId != null && !excludeId.isBlank()) {
                pstmt.setString(2, excludeId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] existsByEmail check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates the next sequential Technical Officer ID (e.g. TO005) based on current max ID.
     */
    public String getNextSuggestedTechnicalOfficerId() {
        String sql = "SELECT technical_officer_id FROM technical_officers WHERE technical_officer_id LIKE 'TO%' ORDER BY technical_officer_id DESC";
        int maxNum = 0;

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && id.startsWith("TO")) {
                    try {
                        int num = Integer.parseInt(id.substring(2).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] Failed to determine next technical officer ID: " + e.getMessage());
        }

        return String.format("TO%03d", maxNum + 1);
    }

    /**
     * Generates the next suggested staff number (e.g. TO-STF-005) based on current max.
     */
    public String getNextSuggestedStaffNo() {
        String sql = "SELECT staff_no FROM technical_officers WHERE staff_no LIKE 'TO-STF-%' ORDER BY staff_no DESC";
        int maxNum = 0;

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String stf = rs.getString(1);
                if (stf != null && stf.startsWith("TO-STF-")) {
                    try {
                        int num = Integer.parseInt(stf.substring(7).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] Failed to determine next staff number: " + e.getMessage());
        }

        return String.format("TO-STF-%03d", maxNum + 1);
    }

    /**
     * Adds a new technical officer record across 'users' and 'technical_officers' tables atomically inside a transaction.
     *
     * @param to populated TechnicalOfficer entity
     * @return true if both inserts succeed
     * @throws SQLException on database error
     */
    public boolean addTechnicalOfficer(TechnicalOfficer to) throws SQLException {
        String insertUserSql = "INSERT INTO users " +
                "(user_id, username, password, role, first_name, last_name, email, phone, address, profile_picture, status) " +
                "VALUES (?, ?, ?, 'TECHNICAL_OFFICER', ?, ?, ?, ?, ?, ?, ?)";

        String insertToSql = "INSERT INTO technical_officers " +
                "(technical_officer_id, staff_no, department) " +
                "VALUES (?, ?, ?)";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getNewConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Insert into users
            try (PreparedStatement pstmtUser = conn.prepareStatement(insertUserSql)) {
                pstmtUser.setString(1, to.getTechnicalOfficerId());
                pstmtUser.setString(2, to.getUsername());
                pstmtUser.setString(3, to.getPassword());
                pstmtUser.setString(4, to.getFirstName());
                pstmtUser.setString(5, to.getLastName());
                pstmtUser.setString(6, to.getEmail());
                pstmtUser.setString(7, to.getPhone());
                pstmtUser.setString(8, to.getAddress());
                pstmtUser.setString(9, to.getProfilePicture());
                pstmtUser.setString(10, to.getAccountStatus() != null ? to.getAccountStatus() : "ACTIVE");

                int affected = pstmtUser.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("Failed to create user account record for technical officer.");
                }
            }

            // 2. Insert into technical_officers
            try (PreparedStatement pstmtTo = conn.prepareStatement(insertToSql)) {
                pstmtTo.setString(1, to.getTechnicalOfficerId());
                pstmtTo.setString(2, to.getStaffNo());
                pstmtTo.setString(3, to.getDepartment());

                int affected = pstmtTo.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("Failed to create technical officer details record.");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[TechnicalOfficerDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
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
     * Updates an existing technical officer record across 'users' and 'technical_officers' tables within a transaction.
     *
     * @param to TechnicalOfficer entity with updated values
     * @return true if update succeeds
     * @throws SQLException on database error
     */
    public boolean updateTechnicalOfficer(TechnicalOfficer to) throws SQLException {
        boolean updatePassword = to.getPassword() != null && !to.getPassword().isBlank();

        String updateUserSql = updatePassword
                ? "UPDATE users SET username=?, password=?, first_name=?, last_name=?, email=?, phone=?, address=?, status=? WHERE user_id=?"
                : "UPDATE users SET username=?, first_name=?, last_name=?, email=?, phone=?, address=?, status=? WHERE user_id=?";

        String updateToSql = "UPDATE technical_officers " +
                "SET staff_no=?, department=? " +
                "WHERE technical_officer_id=?";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getNewConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Update users
            try (PreparedStatement pstmtUser = conn.prepareStatement(updateUserSql)) {
                int paramIdx = 1;
                pstmtUser.setString(paramIdx++, to.getUsername());
                if (updatePassword) {
                    pstmtUser.setString(paramIdx++, to.getPassword());
                }
                pstmtUser.setString(paramIdx++, to.getFirstName());
                pstmtUser.setString(paramIdx++, to.getLastName());
                pstmtUser.setString(paramIdx++, to.getEmail());
                pstmtUser.setString(paramIdx++, to.getPhone());
                pstmtUser.setString(paramIdx++, to.getAddress());
                pstmtUser.setString(paramIdx++, to.getAccountStatus() != null ? to.getAccountStatus() : "ACTIVE");
                pstmtUser.setString(paramIdx, to.getTechnicalOfficerId());

                pstmtUser.executeUpdate();
            }

            // 2. Update technical_officers
            try (PreparedStatement pstmtTo = conn.prepareStatement(updateToSql)) {
                pstmtTo.setString(1, to.getStaffNo());
                pstmtTo.setString(2, to.getDepartment());
                pstmtTo.setString(3, to.getTechnicalOfficerId());

                int affected = pstmtTo.executeUpdate();
                if (affected <= 0) {
                    throw new SQLException("No technical officer record found with ID " + to.getTechnicalOfficerId());
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[TechnicalOfficerDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
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
     * Checks related foreign records across attendance_sessions (conducted_by) and medicals (approved_by).
     * Respects database relational integrity without modifying or deleting related records automatically.
     *
     * @param toId target technical officer ID
     * @return Map of related table names and record counts
     */
    public Map<String, Integer> getRelatedRecordsSummary(String toId) {
        Map<String, Integer> summary = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getNewConnection()) {
            // 1. attendance_sessions
            checkCount(conn, "SELECT COUNT(*) FROM attendance_sessions WHERE conducted_by = ?", toId, "attendance_sessions", summary);

            // 2. medicals
            checkCount(conn, "SELECT COUNT(*) FROM medicals WHERE approved_by = ?", toId, "medicals", summary);

        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerDAO ERROR] getRelatedRecordsSummary failed: " + e.getMessage());
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
            System.err.println("[TechnicalOfficerDAO Notice] Could not check table " + tableName + ": " + e.getMessage());
        }
    }

    /**
     * Deletes a technical officer record if and only if no foreign key relations exist.
     * Respects existing foreign-key constraints and does not bypass constraints.
     *
     * @param toId ID of technical officer to delete
     * @return true if deleted cleanly
     * @throws SQLException on database error
     * @throws IllegalStateException if related foreign records exist
     */
    public boolean deleteTechnicalOfficer(String toId) throws SQLException, IllegalStateException {
        // Enforce foreign-key relational safety
        Map<String, Integer> related = getRelatedRecordsSummary(toId);
        if (!related.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Technical Officer ").append(toId).append(" cannot be deleted because dependent academic records exist: ");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : related.entrySet()) {
                if (!first) msg.append(", ");
                msg.append(entry.getValue()).append(" in ").append(entry.getKey());
                first = false;
            }
            msg.append(". To maintain audit history and database constraints, related records cannot be deleted automatically. " +
                       "Please deactivate the technical officer account instead.");
            throw new IllegalStateException(msg.toString());
        }

        String deleteToSql = "DELETE FROM technical_officers WHERE technical_officer_id = ?";
        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getNewConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtTo = conn.prepareStatement(deleteToSql)) {
                pstmtTo.setString(1, toId);
                pstmtTo.executeUpdate();
            }

            try (PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSql)) {
                pstmtUser.setString(1, toId);
                pstmtUser.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[TechnicalOfficerDAO ERROR] Rollback failed: " + rollbackEx.getMessage());
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
     * Sets the account status of a technical officer (ACTIVE or INACTIVE).
     *
     * @param toId   target technical officer ID
     * @param status "ACTIVE" or "INACTIVE"
     * @return true if status was updated
     * @throws SQLException on database error
     */
    public boolean setTechnicalOfficerStatus(String toId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status != null ? status.toUpperCase() : "INACTIVE");
            pstmt.setString(2, toId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Maps a SQL ResultSet row to a TechnicalOfficer model object.
     */
    private TechnicalOfficer mapResultSetToTechnicalOfficer(ResultSet rs) throws SQLException {
        TechnicalOfficer to = new TechnicalOfficer();
        to.setTechnicalOfficerId(rs.getString("technical_officer_id"));
        to.setStaffNo(rs.getString("staff_no"));
        to.setDepartment(rs.getString("department"));

        to.setUsername(rs.getString("username"));
        to.setPassword(rs.getString("password"));
        to.setFirstName(rs.getString("first_name"));
        to.setLastName(rs.getString("last_name"));
        to.setEmail(rs.getString("email"));
        to.setPhone(rs.getString("phone"));
        to.setAddress(rs.getString("address"));
        to.setProfilePicture(rs.getString("profile_picture"));
        to.setAccountStatus(rs.getString("status"));
        to.setCreatedAt(rs.getTimestamp("created_at"));
        return to;
    }

    public TechnicalOfficer getOfficerById(String officerId) {
        if (officerId == null || officerId.trim().isEmpty()) {
            officerId = "TO001";
        }
        TechnicalOfficer officer = getTechnicalOfficerById(officerId);
        if (officer != null) {
            if (officer.getOfficeRoom() == null || officer.getOfficeRoom().isEmpty()) {
                officer.setOfficeRoom(mockOfficer.getOfficeRoom());
            }
            if (officer.getDesignation() == null || officer.getDesignation().isEmpty()) {
                officer.setDesignation(mockOfficer.getDesignation());
            }
            if (officer.getFaculty() == null || officer.getFaculty().isEmpty()) {
                officer.setFaculty(mockOfficer.getFaculty());
            }
            if (officer.getCreatedDate() == null || officer.getCreatedDate().isEmpty()) {
                officer.setCreatedDate(mockOfficer.getCreatedDate());
            }
            return officer;
        }
        return cloneOfficer(mockOfficer);
    }

    public boolean updateOfficer(TechnicalOfficer officer) {
        if (officer == null) return false;
        try {
            boolean ok = updateTechnicalOfficer(officer);
            if (ok) {
                mockOfficer.setFirstName(officer.getFirstName());
                mockOfficer.setLastName(officer.getLastName());
                mockOfficer.setEmail(officer.getEmail());
                mockOfficer.setPhone(officer.getPhone());
                mockOfficer.setDepartment(officer.getDepartment());
                if (officer.getAddress() != null) mockOfficer.setAddress(officer.getAddress());
                if (officer.getOfficeRoom() != null) mockOfficer.setOfficeRoom(officer.getOfficeRoom());
                addAuditLog("Profile Update", "Updated personal details and contact preferences", "Success");
                return true;
            }
        } catch (Exception e) {
            System.err.println("[TechnicalOfficerDAO] Error in updateOfficer: " + e.getMessage());
        }
        mockOfficer.setFirstName(officer.getFirstName());
        mockOfficer.setLastName(officer.getLastName());
        mockOfficer.setEmail(officer.getEmail());
        mockOfficer.setPhone(officer.getPhone());
        mockOfficer.setDepartment(officer.getDepartment());
        if (officer.getAddress() != null) mockOfficer.setAddress(officer.getAddress());
        if (officer.getOfficeRoom() != null) mockOfficer.setOfficeRoom(officer.getOfficeRoom());
        addAuditLog("Profile Update", "Updated personal details and contact preferences", "Success");
        return true;
    }

    public boolean changePassword(String officerId, String currentPassword, String newPassword) {
        if (officerId == null || currentPassword == null || newPassword == null) return false;

        boolean verified = false;
        String checkSql = "SELECT password FROM users WHERE user_id=?";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setString(1, officerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbPass = rs.getString("password");
                    if (currentPassword.equals(dbPass)) {
                        verified = true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error verifying current password: " + e.getMessage());
        }

        if (verified) {
            String updateSql = "UPDATE users SET password=? WHERE user_id=?";
            try (Connection conn = DBConnection.getNewConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, officerId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    currentStoredPassword = newPassword;
                    addAuditLog("Security", "User account password updated successfully", "Success");
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("Error changing password in DB: " + e.getMessage());
            }
        }

        if (currentPassword.equals(currentStoredPassword)) {
            currentStoredPassword = newPassword;
            addAuditLog("Security", "User account password updated successfully", "Success");
            return true;
        }

        return false;
    }

    public List<String[]> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    public void addAuditLog(String category, String description, String status) {
        String time = LOG_DATE_FMT.format(new Date());
        auditLogs.add(0, new String[]{time, category, description, status});
        if (auditLogs.size() > 50) {
            auditLogs.remove(auditLogs.size() - 1);
        }
    }

    private TechnicalOfficer cloneOfficer(TechnicalOfficer src) {
        return new TechnicalOfficer(
                src.getTechnicalOfficerId(),
                src.getStaffNo(),
                src.getUsername(),
                src.getFirstName(),
                src.getLastName(),
                src.getEmail(),
                src.getPhone(),
                src.getDepartment(),
                src.getAddress(),
                src.getOfficeRoom(),
                src.getDesignation(),
                src.getFaculty(),
                src.getStatus(),
                src.getProfilePicture(),
                src.getCreatedDate()
        );
    }
}
