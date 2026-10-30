package service;

import dao.TechnicalOfficerDAO;
import model.TechnicalOfficer;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service coordinating Technical Officer Management business logic, data validation,
 * foreign key integrity, and transactions.
 */
public class TechnicalOfficerService {

    private static TechnicalOfficerService instance;
    private final TechnicalOfficerDAO technicalOfficerDAO;

    // Email validation regex: standard RFC 5322 simplified
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // Phone validation regex: allows digits, optional leading +, spaces/hyphens, length 9-20
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9\\-\\s]{9,20}$");

    public TechnicalOfficerService() {
        this.technicalOfficerDAO = new TechnicalOfficerDAO();
    }

    public TechnicalOfficerService(TechnicalOfficerDAO dao) {
        this.technicalOfficerDAO = dao;
    }

    public static synchronized TechnicalOfficerService getInstance() {
        if (instance == null) {
            instance = new TechnicalOfficerService();
        }
        return instance;
    }

    /**
     * Retrieves all technical officers.
     */
    public List<TechnicalOfficer> getAllTechnicalOfficers() {
        return technicalOfficerDAO.getAllTechnicalOfficers();
    }

    /**
     * Retrieves a single technical officer by ID.
     */
    public TechnicalOfficer getTechnicalOfficerById(String toId) {
        if (toId == null || toId.isBlank()) return null;
        return technicalOfficerDAO.getTechnicalOfficerById(toId.trim());
    }

    /**
     * Searches technical officers by keyword and filters.
     */
    public List<TechnicalOfficer> searchTechnicalOfficers(String keyword, String departmentFilter, String accountStatus) {
        return technicalOfficerDAO.searchTechnicalOfficers(keyword, departmentFilter, accountStatus);
    }

    /**
     * Suggests the next sequential Technical Officer ID (e.g. TO005).
     */
    public String getNextSuggestedTechnicalOfficerId() {
        return technicalOfficerDAO.getNextSuggestedTechnicalOfficerId();
    }

    /**
     * Suggests the next staff number (e.g. TO-STF-005).
     */
    public String getNextSuggestedStaffNo() {
        return technicalOfficerDAO.getNextSuggestedStaffNo();
    }

    /**
     * Retrieves summary of foreign records associated with the technical officer.
     */
    public Map<String, Integer> getTechnicalOfficerRelatedRecords(String toId) {
        return technicalOfficerDAO.getRelatedRecordsSummary(toId);
    }

    /**
     * Validates technical officer data for addition or update.
     *
     * @param to    target technical officer
     * @param isNew true if creating a new technical officer, false if updating
     * @return OperationResult indicating success or validation error message
     */
    public OperationResult validateTechnicalOfficer(TechnicalOfficer to, boolean isNew) {
        if (to == null) {
            return OperationResult.error("Technical officer information cannot be empty.");
        }

        // 1. Technical Officer ID
        String id = to.getTechnicalOfficerId();
        if (id == null || id.trim().isEmpty()) {
            return OperationResult.error("Technical Officer ID is required (e.g. TO001).");
        }
        id = id.trim();
        if (id.length() > 10) {
            return OperationResult.error("Technical Officer ID cannot exceed 10 characters.");
        }
        if (!id.matches("^[A-Za-z0-9_\\-]+$")) {
            return OperationResult.error("Technical Officer ID may only contain letters, digits, hyphens, and underscores.");
        }
        if (isNew && technicalOfficerDAO.existsById(id)) {
            return OperationResult.error("Technical Officer ID '" + id + "' is already registered in the system.");
        }

        // 2. Staff Number
        String staffNo = to.getStaffNo();
        if (staffNo == null || staffNo.trim().isEmpty()) {
            return OperationResult.error("Staff number is required (e.g. TO-STF-001).");
        }
        staffNo = staffNo.trim();
        if (staffNo.length() > 30) {
            return OperationResult.error("Staff number cannot exceed 30 characters.");
        }
        if (technicalOfficerDAO.existsByStaffNo(staffNo, isNew ? null : id)) {
            return OperationResult.error("Staff number '" + staffNo + "' is already registered to another technical officer.");
        }

        // 3. Username
        String username = to.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return OperationResult.error("Username is required.");
        }
        username = username.trim();
        if (username.length() > 50) {
            return OperationResult.error("Username cannot exceed 50 characters.");
        }
        if (username.contains(" ")) {
            return OperationResult.error("Username cannot contain spaces.");
        }
        if (technicalOfficerDAO.existsByUsername(username, isNew ? null : id)) {
            return OperationResult.error("Username '" + username + "' is already taken. Please choose another username.");
        }

        // 4. Password
        String pwd = to.getPassword();
        if (isNew) {
            if (pwd == null || pwd.isBlank()) {
                return OperationResult.error("Password is required for a new technical officer account.");
            }
            if (pwd.length() < 4) {
                return OperationResult.error("Password must be at least 4 characters long.");
            }
        } else {
            if (pwd != null && !pwd.isBlank() && pwd.length() < 4) {
                return OperationResult.error("Password must be at least 4 characters long if changing.");
            }
        }

        // 5. First Name & Last Name
        String fn = to.getFirstName();
        if (fn == null || fn.trim().isEmpty()) {
            return OperationResult.error("First name is required.");
        }
        if (fn.trim().length() > 50) {
            return OperationResult.error("First name cannot exceed 50 characters.");
        }

        String ln = to.getLastName();
        if (ln == null || ln.trim().isEmpty()) {
            return OperationResult.error("Last name is required.");
        }
        if (ln.trim().length() > 50) {
            return OperationResult.error("Last name cannot exceed 50 characters.");
        }

        // 6. Department
        if (to.getDepartment() == null || to.getDepartment().trim().isEmpty()) {
            return OperationResult.error("Department is required.");
        }

        // 7. Email (Optional in DB, but if provided must be valid)
        String email = to.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();
            if (email.length() > 100) {
                return OperationResult.error("Email address cannot exceed 100 characters.");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return OperationResult.error("Please enter a valid email address (e.g. officer@uor.lk).");
            }
            if (technicalOfficerDAO.existsByEmail(email, isNew ? null : id)) {
                return OperationResult.error("Email address '" + email + "' is already in use by another account.");
            }
        }

        // 8. Phone (Optional)
        String phone = to.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.trim();
            if (phone.length() > 20) {
                return OperationResult.error("Phone number cannot exceed 20 characters.");
            }
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return OperationResult.error("Please enter a valid phone number (digits only, e.g. 0710000007).");
            }
        }

        // 9. Account Status
        String as = to.getAccountStatus();
        if (as == null || as.isBlank()) {
            to.setAccountStatus("ACTIVE");
        } else if (!as.equalsIgnoreCase("ACTIVE") && !as.equalsIgnoreCase("INACTIVE")) {
            return OperationResult.error("Invalid account status. Allowed values: ACTIVE, INACTIVE.");
        }

        return OperationResult.success("Validation passed.");
    }

    /**
     * Adds a new technical officer record to the system with transaction management and validation.
     *
     * @param to technical officer details
     * @return OperationResult
     */
    public OperationResult addTechnicalOfficer(TechnicalOfficer to) {
        OperationResult valResult = validateTechnicalOfficer(to, true);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        to.setTechnicalOfficerId(to.getTechnicalOfficerId().trim().toUpperCase());
        to.setStaffNo(to.getStaffNo().trim());
        to.setUsername(to.getUsername().trim());
        to.setFirstName(to.getFirstName().trim());
        to.setLastName(to.getLastName().trim());
        to.setDepartment(to.getDepartment().trim());
        if (to.getEmail() != null) to.setEmail(to.getEmail().trim().toLowerCase());
        if (to.getPhone() != null) to.setPhone(to.getPhone().trim());
        if (to.getAddress() != null) to.setAddress(to.getAddress().trim());

        try {
            boolean added = technicalOfficerDAO.addTechnicalOfficer(to);
            if (added) {
                return OperationResult.success("Technical Officer " + to.getTechnicalOfficerId() + " (" + to.getFullName() + ") added successfully.", to);
            } else {
                return OperationResult.error("Failed to add technical officer. Please verify database availability.");
            }
        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerService ERROR] addTechnicalOfficer failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate key error: Technical Officer ID, Staff No, Username, or Email already exists in the database.");
            }
            return OperationResult.error("Database error while adding technical officer: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Updates an existing technical officer record with validation and error diagnostics.
     *
     * @param to updated technical officer details
     * @return OperationResult
     */
    public OperationResult updateTechnicalOfficer(TechnicalOfficer to) {
        OperationResult valResult = validateTechnicalOfficer(to, false);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        to.setStaffNo(to.getStaffNo().trim());
        to.setUsername(to.getUsername().trim());
        to.setFirstName(to.getFirstName().trim());
        to.setLastName(to.getLastName().trim());
        to.setDepartment(to.getDepartment().trim());
        if (to.getEmail() != null) to.setEmail(to.getEmail().trim().toLowerCase());
        if (to.getPhone() != null) to.setPhone(to.getPhone().trim());
        if (to.getAddress() != null) to.setAddress(to.getAddress().trim());

        try {
            boolean updated = technicalOfficerDAO.updateTechnicalOfficer(to);
            if (updated) {
                return OperationResult.success("Technical Officer " + to.getTechnicalOfficerId() + " (" + to.getFullName() + ") updated successfully.", to);
            } else {
                return OperationResult.error("Technical Officer " + to.getTechnicalOfficerId() + " was not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerService ERROR] updateTechnicalOfficer failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate value error: Staff Number, Username, or Email already exists on another account.");
            }
            return OperationResult.error("Database error while updating technical officer: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Deletes a technical officer if no foreign key constraints or dependent records prevent it.
     * Respects database constraints without bypassing them.
     *
     * @param toId ID of technical officer to delete
     * @return OperationResult
     */
    public OperationResult deleteTechnicalOfficer(String toId) {
        if (toId == null || toId.isBlank()) {
            return OperationResult.error("Invalid technical officer ID.");
        }

        toId = toId.trim();

        // 1. Check if technical officer exists
        TechnicalOfficer existing = technicalOfficerDAO.getTechnicalOfficerById(toId);
        if (existing == null) {
            return OperationResult.error("Technical Officer " + toId + " does not exist or was already removed.");
        }

        // 2. Attempt deletion respecting foreign keys
        try {
            boolean deleted = technicalOfficerDAO.deleteTechnicalOfficer(toId);
            if (deleted) {
                return OperationResult.success("Technical Officer " + toId + " (" + existing.getFullName() + ") was deleted successfully.");
            } else {
                return OperationResult.error("Failed to delete technical officer " + toId + ".");
            }
        } catch (IllegalStateException e) {
            // Foreign key dependent records exist
            return OperationResult.error(e.getMessage());
        } catch (SQLException e) {
            System.err.println("[TechnicalOfficerService ERROR] deleteTechnicalOfficer failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1451) {
                return OperationResult.error("Cannot delete technical officer " + toId + " due to active foreign key relationships in other tables. " +
                        "Historical records must be preserved. You may mark the account as INACTIVE instead.");
            }
            return OperationResult.error("Database error while deleting technical officer: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Deactivates a technical officer's user account (sets status = 'INACTIVE').
     */
    public OperationResult deactivateTechnicalOfficer(String toId) {
        return changeTechnicalOfficerStatus(toId, "INACTIVE", "deactivated");
    }

    /**
     * Activates a technical officer's user account (sets status = 'ACTIVE').
     */
    public OperationResult activateTechnicalOfficer(String toId) {
        return changeTechnicalOfficerStatus(toId, "ACTIVE", "activated");
    }

    private OperationResult changeTechnicalOfficerStatus(String toId, String status, String verb) {
        if (toId == null || toId.isBlank()) {
            return OperationResult.error("Invalid technical officer ID.");
        }
        toId = toId.trim();
        TechnicalOfficer existing = technicalOfficerDAO.getTechnicalOfficerById(toId);
        if (existing == null) {
            return OperationResult.error("Technical Officer " + toId + " does not exist.");
        }

        try {
            boolean updated = technicalOfficerDAO.setTechnicalOfficerStatus(toId, status);
            if (updated) {
                return OperationResult.success("Technical Officer " + toId + " (" + existing.getFullName() + ") account was " + verb + " successfully.");
            } else {
                return OperationResult.error("Failed to update status for technical officer " + toId + ".");
            }
        } catch (SQLException e) {
            return OperationResult.error("Database error while updating status: " + e.getMessage());
        }
    }

    /**
     * Result value holder for service operations.
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final TechnicalOfficer technicalOfficer;

        public OperationResult(boolean success, String message, TechnicalOfficer technicalOfficer) {
            this.success = success;
            this.message = message;
            this.technicalOfficer = technicalOfficer;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult success(String message, TechnicalOfficer technicalOfficer) {
            return new OperationResult(true, message, technicalOfficer);
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

        public TechnicalOfficer getTechnicalOfficer() {
            return technicalOfficer;
        }
    }
}
