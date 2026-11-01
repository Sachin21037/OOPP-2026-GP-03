package service;

import dao.UserDAO;
import model.UserAccount;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service coordinating User Management business logic, input validations,
 * account status changes, and Admin account creation.
 */
public class UserService {

    private static UserService instance;
    private final UserDAO userDAO;

    // Email validation regex: standard RFC 5322 simplified
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // Phone validation regex: allows digits, optional leading +, spaces/hyphens, length 9-20
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9\\-\\s]{9,20}$");

    // Username regex: alphanumeric, underscores, hyphens, dots, 3-50 chars
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Retrieves all user accounts.
     */
    public List<UserAccount> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Retrieves a single user account by User ID.
     */
    public UserAccount getUserById(String userId) {
        if (userId == null || userId.isBlank()) return null;
        return userDAO.getUserById(userId.trim());
    }

    /**
     * Retrieves a single user account by username.
     */
    public UserAccount getUserByUsername(String username) {
        if (username == null || username.isBlank()) return null;
        return userDAO.getUserByUsername(username.trim());
    }

    /**
     * Searches and filters user accounts by keyword, role, and status.
     */
    public List<UserAccount> searchUsers(String keyword, String roleFilter, String statusFilter) {
        return userDAO.searchUsers(keyword, roleFilter, statusFilter);
    }

    /**
     * Retrieves real-time user summary counts (total, active, inactive, roles).
     */
    public Map<String, Integer> getUserSummaryCounts() {
        return userDAO.getUserSummaryCounts();
    }

    /**
     * Suggests the next sequential Administrator ID (e.g. ADM002).
     */
    public String getNextSuggestedAdminId() {
        return userDAO.getNextSuggestedAdminId();
    }

    /**
     * Validates user account fields for update or creation.
     *
     * @param user  target account
     * @param isNew true if creating a new account, false if updating
     * @return OperationResult
     */
    public OperationResult validateUserAccount(UserAccount user, boolean isNew) {
        if (user == null) {
            return OperationResult.error("User account information cannot be empty.");
        }

        // 1. User ID (required for new Admin, or for existing record)
        String userId = user.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            return OperationResult.error("User ID is required.");
        }

        // 2. Username
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return OperationResult.error("Username is required.");
        }
        username = username.trim();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return OperationResult.error("Username must be 3-50 characters long and contain only letters, numbers, dots, hyphens, or underscores.");
        }
        if (userDAO.existsByUsername(username, isNew ? null : userId)) {
            return OperationResult.error("Username '" + username + "' is already taken. Please choose another username.");
        }

        // 3. Names
        String firstName = user.getFirstName();
        if (firstName == null || firstName.trim().isEmpty()) {
            return OperationResult.error("First name is required.");
        }
        String lastName = user.getLastName();
        if (lastName == null || lastName.trim().isEmpty()) {
            return OperationResult.error("Last name is required.");
        }

        // 4. Email
        String email = user.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return OperationResult.error("Please enter a valid email address (e.g. user@fams.uor.lk).");
            }
            if (userDAO.existsByEmail(email, isNew ? null : userId)) {
                return OperationResult.error("Email address '" + email + "' is already registered to another user account.");
            }
        }

        // 5. Phone
        String phone = user.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.trim();
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return OperationResult.error("Please enter a valid phone number (digits only, e.g. 0710000001).");
            }
        }

        // 6. Role
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            return OperationResult.error("User role is required.");
        }
        role = role.trim().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("LECTURER") &&
            !role.equals("TECHNICAL_OFFICER") && !role.equals("UNDERGRADUATE")) {
            return OperationResult.error("Invalid role. Permitted roles: ADMIN, LECTURER, TECHNICAL_OFFICER, UNDERGRADUATE.");
        }

        // 7. Status
        String status = user.getStatus();
        if (status == null || status.trim().isEmpty()) {
            user.setStatus("ACTIVE");
        } else {
            status = status.trim().toUpperCase();
            if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
                return OperationResult.error("Invalid account status. Allowed values: ACTIVE, INACTIVE.");
            }
        }

        return OperationResult.success("Validation passed.");
    }

    /**
     * Updates permitted user account details (username, name, email, phone, status).
     * Preserves existing password and linked profile relations without modification.
     */
    public OperationResult updateUserAccount(UserAccount user) {
        OperationResult valResult = validateUserAccount(user, false);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        user.setUsername(user.getUsername().trim());
        user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());
        if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
        if (user.getPhone() != null) user.setPhone(user.getPhone().trim());
        if (user.getStatus() != null) user.setStatus(user.getStatus().trim().toUpperCase());

        try {
            boolean updated = userDAO.updateUserAccount(user);
            if (updated) {
                return OperationResult.success("User account '" + user.getUsername() + "' (" + user.getUserId() + ") updated successfully.", user);
            } else {
                return OperationResult.error("User account " + user.getUserId() + " was not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("[UserService ERROR] updateUserAccount failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate value error: Username or Email is already taken by another account.");
            }
            return OperationResult.error("Database error while updating user account: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Activates a user account (status = 'ACTIVE').
     */
    public OperationResult activateUser(String userId) {
        return setUserStatus(userId, "ACTIVE", "activated");
    }

    /**
     * Deactivates a user account (status = 'INACTIVE').
     */
    public OperationResult deactivateUser(String userId) {
        return setUserStatus(userId, "INACTIVE", "deactivated");
    }

    private OperationResult setUserStatus(String userId, String status, String verb) {
        if (userId == null || userId.isBlank()) {
            return OperationResult.error("Invalid user ID.");
        }
        userId = userId.trim();

        UserAccount existing = userDAO.getUserById(userId);
        if (existing == null) {
            return OperationResult.error("User account " + userId + " does not exist.");
        }

        try {
            boolean updated = userDAO.setUserStatus(userId, status);
            if (updated) {
                return OperationResult.success("User account '" + existing.getUsername() + "' (" + userId + ") was " + verb + " successfully.");
            } else {
                return OperationResult.error("Failed to update status for user account " + userId + ".");
            }
        } catch (SQLException e) {
            return OperationResult.error("Database error while updating account status: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Creates a new Administrator login account in both 'users' and 'admins' tables.
     */
    public OperationResult createAdminAccount(UserAccount user) {
        if (user == null) {
            return OperationResult.error("Account details cannot be empty.");
        }
        user.setRole("ADMIN");

        OperationResult valResult = validateUserAccount(user, true);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        user.setUserId(user.getUserId().trim().toUpperCase());
        user.setUsername(user.getUsername().trim());
        user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());
        if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
        if (user.getPhone() != null) user.setPhone(user.getPhone().trim());
        user.setStatus("ACTIVE");

        try {
            boolean created = userDAO.createAdminAccount(user);
            if (created) {
                return OperationResult.success("Administrator login account '" + user.getUsername() + "' (" + user.getUserId() + ") created successfully.", user);
            } else {
                return OperationResult.error("Failed to create administrator account.");
            }
        } catch (SQLException e) {
            System.err.println("[UserService ERROR] createAdminAccount failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate key error: Admin ID, Username, or Email already exists in the system.");
            }
            return OperationResult.error("Database error while creating administrator account: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Service Operation Result container.
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final UserAccount userAccount;

        public OperationResult(boolean success, String message, UserAccount userAccount) {
            this.success = success;
            this.message = message;
            this.userAccount = userAccount;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult success(String message, UserAccount userAccount) {
            return new OperationResult(true, message, userAccount);
        }

        public static OperationResult error(String message) {
            return new OperationResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public UserAccount getUserAccount() {
            return userAccount;
        }
    }
}
