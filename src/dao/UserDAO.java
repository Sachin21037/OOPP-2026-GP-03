package dao;

import database.DBConnection;
import model.User;
import model.UserAccount;

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
 * Data Access Object for User Management in FAMS.
 * Manages system login accounts ('users' table) and joins linked profile tables:
 * - 'admins'
 * - 'lecturers'
 * - 'technical_officers'
 * - 'undergraduates'
 */
public class UserDAO {

    private static final String BASE_QUERY =
            "SELECT " +
            "    u.user_id, u.username, u.role, u.first_name, u.last_name, " +
            "    u.email, u.phone, u.address, u.profile_picture, u.status, u.created_at, " +
            "    CASE " +
            "        WHEN u.role = 'UNDERGRADUATE' THEN CONCAT('Index: ', IFNULL(s.index_no, 'None')) " +
            "        WHEN u.role = 'LECTURER' THEN CONCAT('Staff ID: ', IFNULL(l.staff_no, 'None')) " +
            "        WHEN u.role = 'TECHNICAL_OFFICER' THEN CONCAT('Staff ID: ', IFNULL(t.staff_no, 'None')) " +
            "        WHEN u.role = 'ADMIN' THEN CONCAT('Admin ID: ', IFNULL(a.admin_id, 'None')) " +
            "        ELSE 'None' " +
            "    END AS linked_profile_identifier, " +
            "    CASE " +
            "        WHEN u.role = 'UNDERGRADUATE' THEN s.department " +
            "        WHEN u.role = 'LECTURER' THEN l.department " +
            "        WHEN u.role = 'TECHNICAL_OFFICER' THEN t.department " +
            "        WHEN u.role = 'ADMIN' THEN a.department " +
            "        ELSE 'Faculty of Technology' " +
            "    END AS department " +
            "FROM users u " +
            "LEFT JOIN undergraduates s ON u.user_id = s.student_id COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN lecturers l ON u.user_id = l.lecturer_id COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN technical_officers t ON u.user_id = t.technical_officer_id COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN admins a ON u.user_id = a.admin_id COLLATE utf8mb4_unicode_ci ";

    /**
     * Authenticates a user by username and password.
     * Returns the User object if valid, or null if invalid.
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE'";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setUserId(rs.getString("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setRole(rs.getString("role"));
                        user.setFirstName(rs.getString("first_name"));
                        user.setLastName(rs.getString("last_name"));
                        user.setEmail(rs.getString("email"));
                        user.setPhone(rs.getString("phone"));
                        user.setAddress(rs.getString("address"));
                        user.setProfilePicture(rs.getString("profile_picture"));
                        user.setStatus(rs.getString("status"));
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all user accounts ordered by role and ID.
     */
    public List<UserAccount> getAllUsers() {
        List<UserAccount> list = new ArrayList<>();
        String sql = BASE_QUERY +
                "ORDER BY " +
                "    CASE u.role " +
                "        WHEN 'ADMIN' THEN 1 " +
                "        WHEN 'LECTURER' THEN 2 " +
                "        WHEN 'TECHNICAL_OFFICER' THEN 3 " +
                "        WHEN 'UNDERGRADUATE' THEN 4 " +
                "    END, " +
                "    u.user_id ASC";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUserAccount(rs));
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] getAllUsers failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves a single user account by User ID.
     */
    public UserAccount getUserById(String userId) {
        if (userId == null || userId.isBlank()) return null;
        String sql = BASE_QUERY + "WHERE u.user_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserAccount(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] getUserById failed for " + userId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Retrieves a single user account by username.
     */
    public UserAccount getUserByUsername(String username) {
        if (username == null || username.isBlank()) return null;
        String sql = BASE_QUERY + "WHERE u.username = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserAccount(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] getUserByUsername failed for " + username + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Searches and filters user accounts by keyword, role, and status.
     *
     * @param keyword      matches username, name, email, user_id, or linked identifier
     * @param roleFilter   role enum filter (or null/"ALL")
     * @param statusFilter status filter (or null/"ALL")
     * @return List of matching UserAccount objects
     */
    public List<UserAccount> searchUsers(String keyword, String roleFilter, String statusFilter) {
        List<UserAccount> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_QUERY).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append("AND (u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? " +
                       "     OR CONCAT(u.first_name, ' ', u.last_name) LIKE ? " +
                       "     OR u.email LIKE ? OR u.user_id LIKE ? " +
                       "     OR s.index_no LIKE ? OR l.staff_no LIKE ? OR t.staff_no LIKE ?) ");
            for (int i = 0; i < 9; i++) {
                params.add(pattern);
            }
        }

        if (roleFilter != null && !roleFilter.trim().isEmpty() && !roleFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND u.role = ? ");
            params.add(roleFilter.trim().toUpperCase());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND u.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }

        sql.append("ORDER BY " +
                   "    CASE u.role " +
                   "        WHEN 'ADMIN' THEN 1 " +
                   "        WHEN 'LECTURER' THEN 2 " +
                   "        WHEN 'TECHNICAL_OFFICER' THEN 3 " +
                   "        WHEN 'UNDERGRADUATE' THEN 4 " +
                   "    END, " +
                   "    u.user_id ASC");

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUserAccount(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] searchUsers failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Calculates real-time user summary counts from the database:
     * - total, active, inactive, admin, lecturer, technical_officer, undergraduate
     */
    public Map<String, Integer> getUserSummaryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("total", 0);
        counts.put("active", 0);
        counts.put("inactive", 0);
        counts.put("admin", 0);
        counts.put("lecturer", 0);
        counts.put("technical_officer", 0);
        counts.put("undergraduate", 0);

        String sql = "SELECT " +
                     "    COUNT(*) AS total, " +
                     "    SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_count, " +
                     "    SUM(CASE WHEN status = 'INACTIVE' THEN 1 ELSE 0 END) AS inactive_count, " +
                     "    SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) AS admin_count, " +
                     "    SUM(CASE WHEN role = 'LECTURER' THEN 1 ELSE 0 END) AS lecturer_count, " +
                     "    SUM(CASE WHEN role = 'TECHNICAL_OFFICER' THEN 1 ELSE 0 END) AS to_count, " +
                     "    SUM(CASE WHEN role = 'UNDERGRADUATE' THEN 1 ELSE 0 END) AS stu_count " +
                     "FROM users";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                counts.put("total", rs.getInt("total"));
                counts.put("active", rs.getInt("active_count"));
                counts.put("inactive", rs.getInt("inactive_count"));
                counts.put("admin", rs.getInt("admin_count"));
                counts.put("lecturer", rs.getInt("lecturer_count"));
                counts.put("technical_officer", rs.getInt("to_count"));
                counts.put("undergraduate", rs.getInt("stu_count"));
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] getUserSummaryCounts failed: " + e.getMessage());
        }

        return counts;
    }

    /**
     * Checks if a username is already taken by another account.
     */
    public boolean existsByUsername(String username, String excludeUserId) {
        if (username == null || username.isBlank()) return false;
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE username = ?");
        if (excludeUserId != null && !excludeUserId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, username.trim());
            if (excludeUserId != null && !excludeUserId.isBlank()) {
                pstmt.setString(2, excludeUserId.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] existsByUsername failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an email is already taken by another account.
     */
    public boolean existsByEmail(String email, String excludeUserId) {
        if (email == null || email.isBlank()) return false;
        StringBuilder sql = new StringBuilder("SELECT 1 FROM users WHERE email = ?");
        if (excludeUserId != null && !excludeUserId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, email.trim().toLowerCase());
            if (excludeUserId != null && !excludeUserId.isBlank()) {
                pstmt.setString(2, excludeUserId.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] existsByEmail failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates permitted user account details (username, name, email, phone, status).
     * Never alters password directly to prevent plaintext overwrite and preserve security.
     *
     * @param user updated user details
     * @return true if updated successfully
     */
    public boolean updateUserAccount(UserAccount user) throws SQLException {
        if (user == null || user.getUserId() == null) return false;

        String sql = "UPDATE users SET " +
                     "    username = ?, " +
                     "    first_name = ?, " +
                     "    last_name = ?, " +
                     "    email = ?, " +
                     "    phone = ?, " +
                     "    status = ? " +
                     "WHERE user_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername().trim());
            pstmt.setString(2, user.getFirstName().trim());
            pstmt.setString(3, user.getLastName().trim());
            pstmt.setString(4, user.getEmail() != null ? user.getEmail().trim().toLowerCase() : null);
            pstmt.setString(5, user.getPhone() != null ? user.getPhone().trim() : null);
            pstmt.setString(6, user.getStatus() != null ? user.getStatus().trim().toUpperCase() : "ACTIVE");
            pstmt.setString(7, user.getUserId().trim());

            int affected = pstmt.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Activates or deactivates a user account by updating 'status' to 'ACTIVE' or 'INACTIVE'.
     *
     * @param userId target user ID
     * @param status 'ACTIVE' or 'INACTIVE'
     * @return true if status was updated
     */
    public boolean setUserStatus(String userId, String status) throws SQLException {
        if (userId == null || status == null) return false;

        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.trim().toUpperCase());
            pstmt.setString(2, userId.trim());

            int affected = pstmt.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Atomically creates a new Administrator login account in both 'users' and 'admins' tables.
     * Sets standard secure initial password ("Admin@123").
     *
     * @param user administrator account details
     * @return true if both records were created
     */
    public boolean createAdminAccount(UserAccount user) throws SQLException {
        if (user == null) return false;

        String insertUserSql =
                "INSERT INTO users " +
                "(user_id, username, password, role, first_name, last_name, email, phone, address, status) " +
                "VALUES (?, ?, ?, 'ADMIN', ?, ?, ?, ?, ?, ?)";

        String insertAdminSql =
                "INSERT INTO admins (admin_id, department) VALUES (?, ?)";

        String defaultInitialPassword = (user.getPassword() != null && !user.getPassword().isBlank())
                ? user.getPassword()
                : "Admin@123";

        String dept = (user.getDepartment() != null && !user.getDepartment().isBlank())
                ? user.getDepartment()
                : "Faculty of Technology";

        try (Connection conn = DBConnection.getNewConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtUser = conn.prepareStatement(insertUserSql);
                 PreparedStatement pstmtAdmin = conn.prepareStatement(insertAdminSql)) {

                // 1. Insert into users
                pstmtUser.setString(1, user.getUserId().trim());
                pstmtUser.setString(2, user.getUsername().trim());
                pstmtUser.setString(3, defaultInitialPassword);
                pstmtUser.setString(4, user.getFirstName().trim());
                pstmtUser.setString(5, user.getLastName().trim());
                pstmtUser.setString(6, user.getEmail() != null ? user.getEmail().trim().toLowerCase() : null);
                pstmtUser.setString(7, user.getPhone() != null ? user.getPhone().trim() : null);
                pstmtUser.setString(8, user.getAddress() != null ? user.getAddress().trim() : "Faculty of Technology, University of Ruhuna");
                pstmtUser.setString(9, user.getStatus() != null ? user.getStatus().trim().toUpperCase() : "ACTIVE");

                int userRows = pstmtUser.executeUpdate();
                if (userRows == 0) {
                    conn.rollback();
                    return false;
                }

                // 2. Insert into admins
                pstmtAdmin.setString(1, user.getUserId().trim());
                pstmtAdmin.setString(2, dept);

                int adminRows = pstmtAdmin.executeUpdate();
                if (adminRows == 0) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Determines the next sequential Admin ID (e.g. ADM002).
     */
    public String getNextSuggestedAdminId() {
        String sql = "SELECT user_id FROM users WHERE user_id LIKE 'ADM%' ORDER BY LENGTH(user_id) DESC, user_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String lastId = rs.getString("user_id");
                if (lastId != null && lastId.startsWith("ADM")) {
                    try {
                        int num = Integer.parseInt(lastId.substring(3).trim());
                        return String.format("ADM%03d", num + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] getNextSuggestedAdminId failed: " + e.getMessage());
        }

        return "ADM002";
    }

    private UserAccount mapResultSetToUserAccount(ResultSet rs) throws SQLException {
        UserAccount u = new UserAccount();
        u.setUserId(rs.getString("user_id"));
        u.setUsername(rs.getString("username"));
        u.setRole(rs.getString("role"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setProfilePicture(rs.getString("profile_picture"));
        u.setStatus(rs.getString("status"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setLinkedProfileIdentifier(rs.getString("linked_profile_identifier"));
        u.setDepartment(rs.getString("department"));
        return u;
    }
}
