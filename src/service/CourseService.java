package service;

import dao.CourseDAO;
import model.Course;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service coordinating Course Management business logic, validation rules,
 * unique constraints, and foreign-key safeguards.
 */
public class CourseService {

    private static CourseService instance;
    private final CourseDAO courseDAO;

    // Course code format regex: letters and numbers, optional hyphen/underscore, 3-20 chars (e.g. "ICT2132", "ENG-101")
    private static final Pattern COURSE_CODE_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{3,20}$");

    public CourseService() {
        this.courseDAO = new CourseDAO();
    }

    public CourseService(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
    }

    public static synchronized CourseService getInstance() {
        if (instance == null) {
            instance = new CourseService();
        }
        return instance;
    }

    /**
     * Retrieves all courses.
     */
    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
    }

    /**
     * Retrieves a course by its Course ID.
     */
    public Course getCourseById(String courseId) {
        if (courseId == null || courseId.isBlank()) return null;
        return courseDAO.getCourseById(courseId.trim());
    }

    /**
     * Retrieves a course by its unique Course Code.
     */
    public Course getCourseByCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) return null;
        return courseDAO.getCourseByCode(courseCode.trim());
    }

    /**
     * Searches courses by keyword and department.
     */
    public List<Course> searchCourses(String keyword, String department) {
        return courseDAO.searchCourses(keyword, department);
    }

    /**
     * Suggests the next sequential Course ID (e.g. CRS006).
     */
    public String getNextSuggestedCourseId() {
        return courseDAO.getNextSuggestedCourseId();
    }

    /**
     * Retrieves summary of dependent semester offerings and records for a course.
     */
    public Map<String, Integer> getDependentRecordsSummary(String courseId) {
        return courseDAO.getDependentRecordsSummary(courseId);
    }

    /**
     * Retrieves real-time course summary counts.
     */
    public Map<String, Integer> getCourseSummaryCounts() {
        return courseDAO.getCourseSummaryCounts();
    }

    /**
     * Validates course fields before addition or update.
     *
     * @param course target course
     * @param isNew  true if inserting a new course, false if updating
     * @return OperationResult
     */
    public OperationResult validateCourse(Course course, boolean isNew) {
        if (course == null) {
            return OperationResult.error("Course details cannot be empty.");
        }

        // 1. Course ID
        String id = course.getCourseId();
        if (id == null || id.trim().isEmpty()) {
            return OperationResult.error("Course ID is required (e.g. CRS001).");
        }

        // 2. Course Code
        String code = course.getCourseCode();
        if (code == null || code.trim().isEmpty()) {
            return OperationResult.error("Course Code is required (e.g. ICT2132).");
        }
        code = code.trim().toUpperCase();
        if (!COURSE_CODE_PATTERN.matcher(code).matches()) {
            return OperationResult.error("Course Code must be 3-20 characters long (letters, numbers, hyphens only).");
        }
        if (courseDAO.existsByCourseCode(code, isNew ? null : id.trim())) {
            return OperationResult.error("Course Code '" + code + "' is already registered to another course.");
        }

        // 3. Course Name
        String name = course.getCourseName();
        if (name == null || name.trim().isEmpty()) {
            return OperationResult.error("Course Name is required.");
        }
        name = name.trim();
        if (name.length() < 3 || name.length() > 150) {
            return OperationResult.error("Course Name must be between 3 and 150 characters.");
        }

        // 4. Credits
        int credits = course.getCredits();
        if (credits < 1 || credits > 10) {
            return OperationResult.error("Credits must be a valid number between 1 and 10.");
        }

        // 5. Department
        String dept = course.getDepartment();
        if (dept == null || dept.trim().isEmpty()) {
            return OperationResult.error("Department is required.");
        }

        // 6. Description (optional, max 500 chars)
        String desc = course.getDescription();
        if (desc != null && desc.length() > 500) {
            return OperationResult.error("Course description cannot exceed 500 characters.");
        }

        return OperationResult.success("Validation passed.");
    }

    /**
     * Adds a new course to the curriculum.
     */
    public OperationResult addCourse(Course course) {
        OperationResult valResult = validateCourse(course, true);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        course.setCourseId(course.getCourseId().trim().toUpperCase());
        course.setCourseCode(course.getCourseCode().trim().toUpperCase());
        course.setCourseName(course.getCourseName().trim());
        course.setDepartment(course.getDepartment().trim());
        if (course.getDescription() != null) {
            course.setDescription(course.getDescription().trim());
        }

        try {
            boolean added = courseDAO.addCourse(course);
            if (added) {
                return OperationResult.success("Course '" + course.getCourseCode() + "' (" + course.getCourseName() + ") was added successfully.", course);
            } else {
                return OperationResult.error("Failed to add course. Please verify database connection.");
            }
        } catch (SQLException e) {
            System.err.println("[CourseService ERROR] addCourse failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate key error: Course ID or Course Code already exists in the database.");
            }
            return OperationResult.error("Database error while adding course: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Updates an existing course's details.
     */
    public OperationResult updateCourse(Course course) {
        OperationResult valResult = validateCourse(course, false);
        if (!valResult.isSuccess()) {
            return valResult;
        }

        // Normalize text
        course.setCourseCode(course.getCourseCode().trim().toUpperCase());
        course.setCourseName(course.getCourseName().trim());
        course.setDepartment(course.getDepartment().trim());
        if (course.getDescription() != null) {
            course.setDescription(course.getDescription().trim());
        }

        try {
            boolean updated = courseDAO.updateCourse(course);
            if (updated) {
                return OperationResult.success("Course '" + course.getCourseCode() + "' (" + course.getCourseName() + ") was updated successfully.", course);
            } else {
                return OperationResult.error("Course " + course.getCourseId() + " was not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("[CourseService ERROR] updateCourse failed: " + e.getMessage());
            int code = e.getErrorCode();
            if (code == 1062) {
                return OperationResult.error("Duplicate value error: Course Code '" + course.getCourseCode() + "' is already registered.");
            }
            return OperationResult.error("Database error while updating course: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Safely deletes a course if it is not referenced in semester offerings.
     */
    public OperationResult deleteCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return OperationResult.error("Invalid Course ID.");
        }
        courseId = courseId.trim();

        Course existing = courseDAO.getCourseById(courseId);
        if (existing == null) {
            return OperationResult.error("Course " + courseId + " does not exist or was already deleted.");
        }

        try {
            boolean deleted = courseDAO.deleteCourse(courseId);
            if (deleted) {
                return OperationResult.success("Course '" + existing.getCourseCode() + "' (" + existing.getCourseName() + ") was deleted successfully.");
            } else {
                return OperationResult.error("Failed to delete course " + courseId + ".");
            }
        } catch (IllegalStateException e) {
            // Dependent semester offerings exist
            return OperationResult.error(e.getMessage());
        } catch (SQLException e) {
            int code = e.getErrorCode();
            if (code == 1451) {
                return OperationResult.error("Cannot delete Course " + courseId + " due to active foreign key relationships in semester offerings or student enrollments.");
            }
            return OperationResult.error("Database error while deleting course: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Operation Result container.
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final Course course;

        public OperationResult(boolean success, String message, Course course) {
            this.success = success;
            this.message = message;
            this.course = course;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult success(String message, Course course) {
            return new OperationResult(true, message, course);
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

        public Course getCourse() {
            return course;
        }
    }
}
