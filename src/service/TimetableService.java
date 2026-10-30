package service;

import dao.TimetableDAO;
import model.TimetableEntry;

import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service coordinating Timetable Management business logic, validations,
 * conflict checking, and role-based permissions.
 */
public class TimetableService {

    private static TimetableService instance;
    private final TimetableDAO timetableDAO;

    private static final Pattern TIMETABLE_ID_PATTERN = Pattern.compile("^TBL[0-9]{1,7}$");
    private static final Set<String> VALID_DAYS = Set.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
    );

    public TimetableService() {
        this.timetableDAO = new TimetableDAO();
    }

    public TimetableService(TimetableDAO timetableDAO) {
        this.timetableDAO = timetableDAO;
    }

    public static synchronized TimetableService getInstance() {
        if (instance == null) {
            instance = new TimetableService();
        }
        return instance;
    }

    /**
     * Checks if a role is authorized for administrative timetable modifications.
     */
    public boolean isAdmin(String role) {
        return role != null && "ADMIN".equalsIgnoreCase(role.trim());
    }

    /**
     * Retrieves all timetables (Admin full access).
     */
    public List<TimetableEntry> getAllTimetables() {
        return timetableDAO.getAllTimetables();
    }

    /**
     * Retrieves timetable entries tailored to the user's role.
     */
    public List<TimetableEntry> getTimetablesForRole(String role, String userId, String batch, String department) {
        if (isAdmin(role)) {
            return timetableDAO.getAllTimetables();
        }

        if ("LECTURER".equalsIgnoreCase(role)) {
            if (userId != null && !userId.isBlank()) {
                List<TimetableEntry> lecturerSchedule = timetableDAO.getTimetablesForLecturer(userId);
                if (!lecturerSchedule.isEmpty()) {
                    return lecturerSchedule;
                }
            }
            if (department != null && !department.isBlank()) {
                return timetableDAO.getTimetablesForDepartment(department);
            }
        } else if ("TECHNICAL_OFFICER".equalsIgnoreCase(role)) {
            if (department != null && !department.isBlank()) {
                return timetableDAO.getTimetablesForDepartment(department);
            }
        } else if ("UNDERGRADUATE".equalsIgnoreCase(role)) {
            return timetableDAO.getTimetablesForStudent(batch, department);
        }

        return timetableDAO.getAllTimetables();
    }

    /**
     * Retrieves a timetable by ID.
     */
    public TimetableEntry getTimetableById(String timetableId) {
        if (timetableId == null || timetableId.isBlank()) return null;
        return timetableDAO.getTimetableById(timetableId.trim());
    }

    /**
     * Searches timetables.
     */
    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String batchFilter, String departmentFilter) {
        return timetableDAO.searchTimetables(keyword, dayFilter, batchFilter, departmentFilter);
    }

    /**
     * Suggests next sequential ID.
     */
    public String getNextSuggestedTimetableId() {
        return timetableDAO.getNextSuggestedTimetableId();
    }

    /**
     * Retrieves available offerings for form dropdown.
     */
    public Map<String, String> getAvailableOfferings() {
        return timetableDAO.getAvailableOfferings();
    }

    /**
     * Retrieves distinct batches.
     */
    public List<String> getDistinctBatches() {
        return timetableDAO.getDistinctBatches();
    }

    /**
     * Retrieves summary counts for metric cards.
     */
    public Map<String, Integer> getSummaryMetrics() {
        return timetableDAO.getSummaryMetrics();
    }

    /**
     * Validates timetable fields.
     */
    public OperationResult validateTimetable(TimetableEntry entry, boolean isNew) {
        if (entry == null) {
            return OperationResult.error("Timetable details cannot be empty.");
        }

        // 1. Timetable ID
        String id = entry.getTimetableId();
        if (id == null || id.trim().isEmpty()) {
            return OperationResult.error("Timetable ID is required (e.g. TBL001).");
        }
        id = id.trim().toUpperCase();
        if (!TIMETABLE_ID_PATTERN.matcher(id).matches()) {
            return OperationResult.error("Timetable ID must follow the 'TBL' format (e.g. TBL001).");
        }
        if (isNew && timetableDAO.existsTimetableId(id)) {
            return OperationResult.error("Timetable ID '" + id + "' already exists in the system.");
        }

        // 2. Offering ID
        String offId = entry.getOfferingId();
        if (offId == null || offId.trim().isEmpty()) {
            return OperationResult.error("Course Offering selection is required.");
        }

        // 3. Day of Week
        String day = entry.getDayOfWeek();
        if (day == null || !VALID_DAYS.contains(day.trim().toUpperCase())) {
            return OperationResult.error("Valid Day of Week is required (MONDAY through SATURDAY).");
        }

        // 4. Start & End Time
        Time start = entry.getStartTime();
        Time end = entry.getEndTime();
        if (start == null || end == null) {
            return OperationResult.error("Start time and End time are both required.");
        }
        if (!end.after(start)) {
            return OperationResult.error("End time must be strictly after Start time.");
        }

        // 5. Venue
        String venue = entry.getVenue();
        if (venue == null || venue.trim().isEmpty()) {
            return OperationResult.error("Venue is required (e.g. ICT Lab 01, Lecture Hall 02).");
        }
        if (venue.trim().length() < 2 || venue.trim().length() > 100) {
            return OperationResult.error("Venue must be between 2 and 100 characters.");
        }

        // 6. Batch
        String batch = entry.getBatch();
        if (batch == null || batch.trim().isEmpty()) {
            return OperationResult.error("Academic batch is required (e.g. 2024/2025).");
        }

        // 7. Department
        String dept = entry.getDepartment();
        if (dept == null || dept.trim().isEmpty()) {
            return OperationResult.error("Department is required.");
        }

        return OperationResult.success("Validation passed.");
    }

    /**
     * Creates a new timetable slot (Restricted to Administrators).
     */
    public OperationResult addTimetable(TimetableEntry entry, String userRole) {
        if (!isAdmin(userRole)) {
            return OperationResult.error("Permission Denied: Only administrators are authorized to schedule timetable sessions.");
        }

        OperationResult valRes = validateTimetable(entry, true);
        if (!valRes.isSuccess()) {
            return valRes;
        }

        // Normalize text
        entry.setTimetableId(entry.getTimetableId().trim().toUpperCase());
        entry.setOfferingId(entry.getOfferingId().trim().toUpperCase());
        entry.setDayOfWeek(entry.getDayOfWeek().trim().toUpperCase());
        entry.setVenue(entry.getVenue().trim());
        entry.setBatch(entry.getBatch().trim());
        entry.setDepartment(entry.getDepartment().trim());

        // Check for venue collision
        if (timetableDAO.hasVenueCollision(null, entry.getVenue(), entry.getDayOfWeek(), entry.getStartTime(), entry.getEndTime())) {
            return OperationResult.error("Schedule Conflict: Venue '" + entry.getVenue() + "' is already occupied on " +
                    entry.getDayOfWeek() + " between " + entry.getFormattedTimeRange() + ".");
        }

        try {
            boolean added = timetableDAO.addTimetable(entry);
            if (added) {
                return OperationResult.success("Timetable slot '" + entry.getTimetableId() + "' (" + entry.getVenue() + ") added successfully.", entry);
            } else {
                return OperationResult.error("Failed to add timetable slot. Please verify database connection.");
            }
        } catch (SQLException e) {
            System.err.println("[TimetableService ERROR] addTimetable failed: " + e.getMessage());
            if (e.getErrorCode() == 1062) {
                return OperationResult.error("Duplicate key error: Timetable ID '" + entry.getTimetableId() + "' already exists.");
            }
            return OperationResult.error("Database error while saving timetable slot: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Updates an existing timetable slot (Restricted to Administrators).
     */
    public OperationResult updateTimetable(TimetableEntry entry, String userRole) {
        if (!isAdmin(userRole)) {
            return OperationResult.error("Permission Denied: Only administrators are authorized to modify timetable schedules.");
        }

        OperationResult valRes = validateTimetable(entry, false);
        if (!valRes.isSuccess()) {
            return valRes;
        }

        // Normalize text
        entry.setOfferingId(entry.getOfferingId().trim().toUpperCase());
        entry.setDayOfWeek(entry.getDayOfWeek().trim().toUpperCase());
        entry.setVenue(entry.getVenue().trim());
        entry.setBatch(entry.getBatch().trim());
        entry.setDepartment(entry.getDepartment().trim());

        // Check for venue collision with other slots
        if (timetableDAO.hasVenueCollision(entry.getTimetableId(), entry.getVenue(), entry.getDayOfWeek(), entry.getStartTime(), entry.getEndTime())) {
            return OperationResult.error("Schedule Conflict: Venue '" + entry.getVenue() + "' is already occupied on " +
                    entry.getDayOfWeek() + " between " + entry.getFormattedTimeRange() + ".");
        }

        try {
            boolean updated = timetableDAO.updateTimetable(entry);
            if (updated) {
                return OperationResult.success("Timetable slot '" + entry.getTimetableId() + "' updated successfully.", entry);
            } else {
                return OperationResult.error("Timetable slot " + entry.getTimetableId() + " was not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("[TimetableService ERROR] updateTimetable failed: " + e.getMessage());
            return OperationResult.error("Database error while updating timetable slot: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Deletes a timetable slot (Restricted to Administrators).
     */
    public OperationResult deleteTimetable(String timetableId, String userRole) {
        if (!isAdmin(userRole)) {
            return OperationResult.error("Permission Denied: Only administrators are authorized to delete timetable schedules.");
        }

        if (timetableId == null || timetableId.isBlank()) {
            return OperationResult.error("Invalid Timetable ID.");
        }
        timetableId = timetableId.trim();

        TimetableEntry existing = timetableDAO.getTimetableById(timetableId);
        if (existing == null) {
            return OperationResult.error("Timetable slot " + timetableId + " does not exist or was already deleted.");
        }

        try {
            boolean deleted = timetableDAO.deleteTimetable(timetableId);
            if (deleted) {
                return OperationResult.success("Timetable slot '" + timetableId + "' (" + existing.getVenue() + " - " + existing.getDayOfWeek() + ") deleted successfully.");
            } else {
                return OperationResult.error("Failed to delete timetable slot " + timetableId + ".");
            }
        } catch (SQLException e) {
            System.err.println("[TimetableService ERROR] deleteTimetable failed: " + e.getMessage());
            return OperationResult.error("Database error while deleting timetable slot: " + e.getMessage());
        } catch (Exception ex) {
            return OperationResult.error("Unexpected error: " + ex.getMessage());
        }
    }

    // OPERATION RESULT VALUE OBJECT
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final Object data;

        private OperationResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult success(String message, Object data) {
            return new OperationResult(true, message, data);
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

        public Object getData() {
            return data;
        }
    }
}
