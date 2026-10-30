package service;

import dao.StudentDAO;
import model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service coordinating Student Management business logic, data validation,
 * foreign key integrity, and transactions.
 */
public class StudentService {

    private static StudentService instance;
    private final StudentDAO studentDAO;

    // Email validation regex: standard RFC 5322 simplified
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // Phone validation regex: allows digits, optional leading +, spaces/hyphens, length 9-15
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9\\-\\s]{9,15}$");

    public StudentService() {
        this.studentDAO = new StudentDAO();
    }

    public StudentService(StudentDAO dao) {
        this.studentDAO = dao;
    }

    public static synchronized StudentService getInstance() {
        if (instance == null) {
            instance = new StudentService();
        }
        return instance;
    }

    /**
     * Retrieves all students.
     */
    public List<Student> getAllStudents() {
        return studentDAO.getAllStudents();
    }

    /**
     * Retrieves a single student by ID.
     */
    public Student getStudentById(String studentId) {
        if (studentId == null || studentId.isBlank()) return null;
        return studentDAO.getStudentById(studentId.trim());
    }

    /**
     * Searches students by keyword and filters.
     */
    public List<Student> searchStudents(String keyword, String studentStatus, String accountStatus, String batchFilter) {
        return studentDAO.searchStudents(keyword, studentStatus, accountStatus, batchFilter);
    }

    /**
     * Suggests the next sequential student ID.
     */
    public String getNextSuggestedStudentId() {
        return studentDAO.getNextSuggestedStudentId();
    }

    /**
     * Retrieves summary of foreign records associated with the student.
     */
    public Map<String, Integer> getStudentRelatedRecords(String studentId) {
        return studentDAO.getRelatedRecordsSummary(studentId);
    }

    /**
     * Validates student data for addition or update.
     *
     * @param student target student
     * @param isNew   true if creating a new student, false if updating
     * @return OperationResult indicating success or validation error message
     */
    public OperationResult validateStudent(Student student, boolean isNew) {
        if (student == null) {
            return OperationResult.error("Student information cannot be empty.");
        }

        // 1. Student ID
        String id = student.getStudentId();
        if (id == null || id.trim().isEmpty()) {
            return OperationResult.error("Student ID is required (e.g. STU001).");
        }
        id = id.trim();
        if (id.length() > 10) {
            return OperationResult.error("Student ID cannot exceed 10 characters.");
        }
        if (!id.matches("^[A-Za-z0-9_\\-]+$")) {
            return OperationResult.error("Student ID may only contain letters, digits, hyphens, and underscores.");
        }
        if (isNew && studentDAO.existsById(id)) {
            return OperationResult.error("Student ID '" + id + "' is already registered in the system.");
        }

        // 2. Index Number
        String index = student.getIndexNo();
        if (index == null || index.trim().isEmpty()) {
            return OperationResult.error("Index number is required (e.g. TG/2024/2001).");
        }
        index = index.trim();
        if (index.length() > 30) {
            return OperationResult.error("Index number cannot exceed 30 characters.");
        }
        if (studentDAO.existsByIndexNo(index, isNew ? null : id)) {
            return OperationResult.error("Index number '" + index + "' is already registered to another student.");
        }

        // 3. Username
        String username = student.getUsername();
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
        if (studentDAO.existsByUsername(username, isNew ? null : id)) {
            return OperationResult.error("Username '" + username + "' is already taken. Please choose another username.");
        }

        // 4. Password
        String pwd = student.getPassword();
        if (isNew) {
            if (pwd == null || pwd.isBlank()) {
                return OperationResult.error("Password is required for a new student account.");
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
        String fn = student.getFirstName();
        if (fn == null || fn.trim().isEmpty()) {
            return OperationResult.error("First name is required.");
        }
        if (fn.trim().length() > 50) {
            return OperationResult.error("First name cannot exceed 50 characters.");
        }

        String ln = student.getLastName();
        if (ln == null || ln.trim().isEmpty()) {
            return OperationResult.error("Last name is required.");
        }
        if (ln.trim().length() > 50) {
            return OperationResult.error("Last name cannot exceed 50 characters.");
        }

        // 6. Email (Optional in DB, but if provided must be valid)
        String email = student.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();
            if (email.length() > 100) {
                return OperationResult.error("Email address cannot exceed 100 characters.");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return OperationResult.error("Please enter a valid email address (e.g. student@fams.uor.lk).");
            }
            if (studentDAO.existsByEmail(email, isNew ? null : id)) {
                return OperationResult.error("Email address '" + email + "' is already in use by another account.");
            }
        }

        // 7. Phone (Optional)
        String phone = student.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.trim();
            if (phone.length() > 20) {
                return OperationResult.error("Phone number cannot exceed 20 characters.");
            }
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return OperationResult.error("Please enter a valid phone number (digits only, e.g. 0711000001).");
            }
        }

        // 8. Batch, Academic Year, Department
        if (student.getBatch() == null || student.getBatch().trim().isEmpty()) {
            return OperationResult.error("Batch is required (e.g. 2024/2025).");
        }
        if (student.getAcademicYear() == null || student.getAcademicYear().trim().isEmpty()) {
            return OperationResult.error("Academic year is required (e.g. Level II).");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            return OperationResult.error("Department is required.");
        }

        // 9. Student Status
        String ss = student.getStudentStatus();
        if (ss == null || ss.isBlank()) {
            student.setStudentStatus("REGULAR");
        } else if (!ss.equals("REGULAR") && !ss.equals("REPEAT") && !ss.equals("BATCH_MISSED")) {
            return OperationResult.error("Invalid academic status. Allowed values: REGULAR, REPEAT, BATCH_MISSED.");
        }

        // 10. Account Status
        String as = student.getAccountStatus();
        if (as == null || as.isBlank()) {
            student.setAccountStatus("ACTIVE");
        } else if (!as.equals("ACTIVE") && !as.equals("INACTIVE")) {
            return OperationResult.error("Invalid account status. Allowed values: ACTIVE, INACTIVE.");
        }

        return OperationResult.success("Validation passed.");
    }

    /**
     * Adds a new student record to the system with transaction management and validation.
     *
     * @param student student details
     * @return OperationResult
     */
    public OperationResult addStudent(Student student) {
        OperationResult valResult = validateStudent(student, true);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        student.setStudentId(student.getStudentId().trim().toUpperCase());
        student.setIndexNo(student.getIndexNo().trim());
        student.setUsername(student.getUsername().trim());
        student.setFirstName(student.getFirstName().trim());
        student.setLastName(student.getLastName().trim());
        if (student.getEmail() != null) student.setEmail(student.getEmail().trim().toLowerCase());
        if (student.getPhone() != null) student.setPhone(student.getPhone().trim());
        if (student.getAddress() != null) student.setAddress(student.getAddress().trim());
        student.setBatch(student.getBatch().trim());
        student.setAcademicYear(student.getAcademicYear().trim());
        student.setDepartment(student.getDepartment().trim());

        try {
            boolean added = studentDAO.addStudent(student);
            if (added) {
                return OperationResult.success("Student " + student.getStudentId() + " (" + student.getFullName() + ") added successfully.", student);
            } else {
                return OperationResult.error("Failed to add student. Please verify database availability.");
            }
        } catch (SQLException e) {
            System.err.println("[StudentService ERROR] addStudent failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate key error: Student ID, Index Number, Username, or Email already exists in the database.");
            }
            return OperationResult.error("Database error while adding student: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Updates an existing student record with validation and error diagnostics.
     *
     * @param student updated student details
     * @return OperationResult
     */
    public OperationResult updateStudent(Student student) {
        OperationResult valResult = validateStudent(student, false);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        student.setIndexNo(student.getIndexNo().trim());
        student.setUsername(student.getUsername().trim());
        student.setFirstName(student.getFirstName().trim());
        student.setLastName(student.getLastName().trim());
        if (student.getEmail() != null) student.setEmail(student.getEmail().trim().toLowerCase());
        if (student.getPhone() != null) student.setPhone(student.getPhone().trim());
        if (student.getAddress() != null) student.setAddress(student.getAddress().trim());
        student.setBatch(student.getBatch().trim());
        student.setAcademicYear(student.getAcademicYear().trim());
        student.setDepartment(student.getDepartment().trim());

        try {
            boolean updated = studentDAO.updateStudent(student);
            if (updated) {
                return OperationResult.success("Student " + student.getStudentId() + " (" + student.getFullName() + ") updated successfully.", student);
            } else {
                return OperationResult.error("Student " + student.getStudentId() + " was not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("[StudentService ERROR] updateStudent failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate value error: Index Number, Username, or Email already exists on another account.");
            }
            return OperationResult.error("Database error while updating student: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Deletes a student if no foreign key constraints or dependent records prevent it.
     * Respects database constraints without bypassing them.
     *
     * @param studentId ID of student to delete
     * @return OperationResult
     */
    public OperationResult deleteStudent(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return OperationResult.error("Invalid student ID.");
        }

        studentId = studentId.trim();

        // 1. Check if student exists
        Student existing = studentDAO.getStudentById(studentId);
        if (existing == null) {
            return OperationResult.error("Student " + studentId + " does not exist or was already removed.");
        }

        // 2. Attempt deletion respecting foreign keys
        try {
            boolean deleted = studentDAO.deleteStudent(studentId);
            if (deleted) {
                return OperationResult.success("Student " + studentId + " (" + existing.getFullName() + ") was deleted successfully.");
            } else {
                return OperationResult.error("Failed to delete student " + studentId + ".");
            }
        } catch (IllegalStateException e) {
            // Foreign key dependent records exist
            return OperationResult.error(e.getMessage());
        } catch (SQLException e) {
            System.err.println("[StudentService ERROR] deleteStudent failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1451) {
                return OperationResult.error("Cannot delete student " + studentId + " due to active foreign key relationships in other tables. " +
                        "Academic integrity requires records to be preserved. You may mark the student as INACTIVE instead.");
            }
            return OperationResult.error("Database error while deleting student: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Result value holder for service operations.
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final Student student;

        public OperationResult(boolean success, String message, Student student) {
            this.success = success;
            this.message = message;
            this.student = student;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult success(String message, Student student) {
            return new OperationResult(true, message, student);
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

        public Student getStudent() {
            return student;
        }
    }
}
