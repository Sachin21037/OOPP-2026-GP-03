package service;

import dao.LecturerDAO;
import model.Lecturer;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service coordinating Lecturer Management business logic, data validation,
 * foreign key integrity, and transactions.
 */
public class LecturerService {

    private static LecturerService instance;
    private final LecturerDAO lecturerDAO;

    // Email validation regex: standard RFC 5322 simplified
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // Phone validation regex: allows digits, optional leading +, spaces/hyphens, length 9-20
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9\\-\\s]{9,20}$");

    public LecturerService() {
        this.lecturerDAO = new LecturerDAO();
    }

    public LecturerService(LecturerDAO dao) {
        this.lecturerDAO = dao;
    }

    public static synchronized LecturerService getInstance() {
        if (instance == null) {
            instance = new LecturerService();
        }
        return instance;
    }

    /**
     * Retrieves all lecturers.
     */
    public List<Lecturer> getAllLecturers() {
        return lecturerDAO.getAllLecturers();
    }

    /**
     * Retrieves a single lecturer by ID.
     */
    public Lecturer getLecturerById(String lecturerId) {
        if (lecturerId == null || lecturerId.isBlank()) return null;
        return lecturerDAO.getLecturerById(lecturerId.trim());
    }

    /**
     * Searches lecturers by keyword and filters.
     */
    public List<Lecturer> searchLecturers(String keyword, String departmentFilter, String designationFilter, String accountStatus) {
        return lecturerDAO.searchLecturers(keyword, departmentFilter, designationFilter, accountStatus);
    }

    /**
     * Suggests the next sequential lecturer ID (e.g. LEC006).
     */
    public String getNextSuggestedLecturerId() {
        return lecturerDAO.getNextSuggestedLecturerId();
    }

    /**
     * Suggests the next staff number (e.g. LEC-STF-006).
     */
    public String getNextSuggestedStaffNo() {
        return lecturerDAO.getNextSuggestedStaffNo();
    }

    /**
     * Retrieves summary of foreign records associated with the lecturer.
     */
    public Map<String, Integer> getLecturerRelatedRecords(String lecturerId) {
        return lecturerDAO.getRelatedRecordsSummary(lecturerId);
    }

    /**
     * Validates lecturer data for addition or update.
     *
     * @param lecturer target lecturer
     * @param isNew    true if creating a new lecturer, false if updating
     * @return OperationResult indicating success or validation error message
     */
    public OperationResult validateLecturer(Lecturer lecturer, boolean isNew) {
        if (lecturer == null) {
            return OperationResult.error("Lecturer information cannot be empty.");
        }

        // 1. Lecturer ID
        String id = lecturer.getLecturerId();
        if (id == null || id.trim().isEmpty()) {
            return OperationResult.error("Lecturer ID is required (e.g. LEC001).");
        }
        id = id.trim();
        if (id.length() > 10) {
            return OperationResult.error("Lecturer ID cannot exceed 10 characters.");
        }
        if (!id.matches("^[A-Za-z0-9_\\-]+$")) {
            return OperationResult.error("Lecturer ID may only contain letters, digits, hyphens, and underscores.");
        }
        if (isNew && lecturerDAO.existsById(id)) {
            return OperationResult.error("Lecturer ID '" + id + "' is already registered in the system.");
        }

        // 2. Staff Number
        String staffNo = lecturer.getStaffNo();
        if (staffNo == null || staffNo.trim().isEmpty()) {
            return OperationResult.error("Staff number is required (e.g. LEC-STF-001).");
        }
        staffNo = staffNo.trim();
        if (staffNo.length() > 30) {
            return OperationResult.error("Staff number cannot exceed 30 characters.");
        }
        if (lecturerDAO.existsByStaffNo(staffNo, isNew ? null : id)) {
            return OperationResult.error("Staff number '" + staffNo + "' is already registered to another lecturer.");
        }

        // 3. Username
        String username = lecturer.getUsername();
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
        if (lecturerDAO.existsByUsername(username, isNew ? null : id)) {
            return OperationResult.error("Username '" + username + "' is already taken. Please choose another username.");
        }

        // 4. Password
        String pwd = lecturer.getPassword();
        if (isNew) {
            if (pwd == null || pwd.isBlank()) {
                return OperationResult.error("Password is required for a new lecturer account.");
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
        String fn = lecturer.getFirstName();
        if (fn == null || fn.trim().isEmpty()) {
            return OperationResult.error("First name is required.");
        }
        if (fn.trim().length() > 50) {
            return OperationResult.error("First name cannot exceed 50 characters.");
        }

        String ln = lecturer.getLastName();
        if (ln == null || ln.trim().isEmpty()) {
            return OperationResult.error("Last name is required.");
        }
        if (ln.trim().length() > 50) {
            return OperationResult.error("Last name cannot exceed 50 characters.");
        }

        // 6. Department & Designation
        if (lecturer.getDepartment() == null || lecturer.getDepartment().trim().isEmpty()) {
            return OperationResult.error("Department is required.");
        }
        if (lecturer.getDesignation() == null || lecturer.getDesignation().trim().isEmpty()) {
            return OperationResult.error("Designation is required.");
        }

        // 7. Email (Optional in DB, but if provided must be valid)
        String email = lecturer.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();
            if (email.length() > 100) {
                return OperationResult.error("Email address cannot exceed 100 characters.");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return OperationResult.error("Please enter a valid email address (e.g. lecturer@uor.lk).");
            }
            if (lecturerDAO.existsByEmail(email, isNew ? null : id)) {
                return OperationResult.error("Email address '" + email + "' is already in use by another account.");
            }
        }

        // 8. Phone (Optional)
        String phone = lecturer.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.trim();
            if (phone.length() > 20) {
                return OperationResult.error("Phone number cannot exceed 20 characters.");
            }
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return OperationResult.error("Please enter a valid phone number (digits only, e.g. 0710000002).");
            }
        }

        // 9. Account Status
        String as = lecturer.getAccountStatus();
        if (as == null || as.isBlank()) {
            lecturer.setAccountStatus("ACTIVE");
        } else if (!as.equalsIgnoreCase("ACTIVE") && !as.equalsIgnoreCase("INACTIVE")) {
            return OperationResult.error("Invalid account status. Allowed values: ACTIVE, INACTIVE.");
        }

        return OperationResult.success("Validation passed.");
    }

    /**
     * Adds a new lecturer record to the system with transaction management and validation.
     *
     * @param lecturer lecturer details
     * @return OperationResult
     */
    public OperationResult addLecturer(Lecturer lecturer) {
        OperationResult valResult = validateLecturer(lecturer, true);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        lecturer.setLecturerId(lecturer.getLecturerId().trim().toUpperCase());
        lecturer.setStaffNo(lecturer.getStaffNo().trim());
        lecturer.setUsername(lecturer.getUsername().trim());
        lecturer.setFirstName(lecturer.getFirstName().trim());
        lecturer.setLastName(lecturer.getLastName().trim());
        lecturer.setDepartment(lecturer.getDepartment().trim());
        lecturer.setDesignation(lecturer.getDesignation().trim());
        if (lecturer.getEmail() != null) lecturer.setEmail(lecturer.getEmail().trim().toLowerCase());
        if (lecturer.getPhone() != null) lecturer.setPhone(lecturer.getPhone().trim());
        if (lecturer.getAddress() != null) lecturer.setAddress(lecturer.getAddress().trim());

        try {
            boolean added = lecturerDAO.addLecturer(lecturer);
            if (added) {
                return OperationResult.success("Lecturer " + lecturer.getLecturerId() + " (" + lecturer.getFullName() + ") added successfully.", lecturer);
            } else {
                return OperationResult.error("Failed to add lecturer. Please verify database availability.");
            }
        } catch (SQLException e) {
            System.err.println("[LecturerService ERROR] addLecturer failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate key error: Lecturer ID, Staff No, Username, or Email already exists in the database.");
            }
            return OperationResult.error("Database error while adding lecturer: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Updates an existing lecturer record with validation and error diagnostics.
     *
     * @param lecturer updated lecturer details
     * @return OperationResult
     */
    public OperationResult updateLecturer(Lecturer lecturer) {
        OperationResult valResult = validateLecturer(lecturer, false);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        lecturer.setStaffNo(lecturer.getStaffNo().trim());
        lecturer.setUsername(lecturer.getUsername().trim());
        lecturer.setFirstName(lecturer.getFirstName().trim());
        lecturer.setLastName(lecturer.getLastName().trim());
        lecturer.setDepartment(lecturer.getDepartment().trim());
        lecturer.setDesignation(lecturer.getDesignation().trim());
        if (lecturer.getEmail() != null) lecturer.setEmail(lecturer.getEmail().trim().toLowerCase());
        if (lecturer.getPhone() != null) lecturer.setPhone(lecturer.getPhone().trim());
        if (lecturer.getAddress() != null) lecturer.setAddress(lecturer.getAddress().trim());

        try {
            boolean updated = lecturerDAO.updateLecturer(lecturer);
            if (updated) {
                return OperationResult.success("Lecturer " + lecturer.getLecturerId() + " (" + lecturer.getFullName() + ") updated successfully.", lecturer);
            } else {
                return OperationResult.error("Lecturer " + lecturer.getLecturerId() + " was not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("[LecturerService ERROR] updateLecturer failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate value error: Staff Number, Username, or Email already exists on another account.");
            }
            return OperationResult.error("Database error while updating lecturer: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Deletes a lecturer if no foreign key constraints or dependent records prevent it.
     * Respects database constraints without bypassing them.
     *
     * @param lecturerId ID of lecturer to delete
     * @return OperationResult
     */
    public OperationResult deleteLecturer(String lecturerId) {
        if (lecturerId == null || lecturerId.isBlank()) {
            return OperationResult.error("Invalid lecturer ID.");
        }

        lecturerId = lecturerId.trim();

        // 1. Check if lecturer exists
        Lecturer existing = lecturerDAO.getLecturerById(lecturerId);
        if (existing == null) {
            return OperationResult.error("Lecturer " + lecturerId + " does not exist or was already removed.");
        }

        // 2. Attempt deletion respecting foreign keys
        try {
            boolean deleted = lecturerDAO.deleteLecturer(lecturerId);
            if (deleted) {
                return OperationResult.success("Lecturer " + lecturerId + " (" + existing.getFullName() + ") was deleted successfully.");
            } else {
                return OperationResult.error("Failed to delete lecturer " + lecturerId + ".");
            }
        } catch (IllegalStateException e) {
            // Foreign key dependent records exist
            return OperationResult.error(e.getMessage());
        } catch (SQLException e) {
            System.err.println("[LecturerService ERROR] deleteLecturer failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1451) {
                return OperationResult.error("Cannot delete lecturer " + lecturerId + " due to active foreign key relationships in other tables. " +
                        "Academic integrity requires records to be preserved. You may mark the lecturer as INACTIVE instead.");
            }
            return OperationResult.error("Database error while deleting lecturer: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Deactivates a lecturer's user account (sets status = 'INACTIVE').
     */
    public OperationResult deactivateLecturer(String lecturerId) {
        return changeLecturerStatus(lecturerId, "INACTIVE", "deactivated");
    }

    /**
     * Activates a lecturer's user account (sets status = 'ACTIVE').
     */
    public OperationResult activateLecturer(String lecturerId) {
        return changeLecturerStatus(lecturerId, "ACTIVE", "activated");
    }

    private OperationResult changeLecturerStatus(String lecturerId, String status, String verb) {
        if (lecturerId == null || lecturerId.isBlank()) {
            return OperationResult.error("Invalid lecturer ID.");
        }
        lecturerId = lecturerId.trim();
        Lecturer existing = lecturerDAO.getLecturerById(lecturerId);
        if (existing == null) {
            return OperationResult.error("Lecturer " + lecturerId + " does not exist.");
        }

        try {
            boolean updated = lecturerDAO.setLecturerStatus(lecturerId, status);
            if (updated) {
                return OperationResult.success("Lecturer " + lecturerId + " (" + existing.getFullName() + ") account was " + verb + " successfully.");
            } else {
                return OperationResult.error("Failed to update status for lecturer " + lecturerId + ".");
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
        private final Lecturer lecturer;

        public OperationResult(boolean success, String message, Lecturer lecturer) {
            this.success = success;
            this.message = message;
            this.lecturer = lecturer;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult success(String message, Lecturer lecturer) {
            return new OperationResult(true, message, lecturer);
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

        public Lecturer getLecturer() {
            return lecturer;
        }
    }
}
