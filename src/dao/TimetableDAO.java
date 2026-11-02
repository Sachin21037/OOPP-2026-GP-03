package dao;

import database.DBConnection;
import model.TimetableEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Academic Timetables in FAMS.
 * Interfaces with the 'timetables' table and joins with 'course_offerings',
 * 'courses', 'course_lecturers', and 'users'.
 */
public class TimetableDAO {

    private static final String BASE_SELECT =
            "SELECT t.timetable_id, t.offering_id, t.day_of_week, t.start_time, t.end_time, " +
            "       t.venue, t.batch, t.department, " +
            "       co.academic_year, co.semester, " +
            "       c.course_code, c.course_name, c.credits, " +
            "       GROUP_CONCAT(DISTINCT TRIM(CONCAT(IFNULL(u.first_name, ''), ' ', IFNULL(u.last_name, ''))) SEPARATOR ', ') AS lecturers " +
            "FROM timetables t " +
            "LEFT JOIN course_offerings co ON t.offering_id = co.offering_id " +
            "LEFT JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN course_lecturers cl ON t.offering_id = cl.offering_id " +
            "LEFT JOIN lecturers l ON cl.lecturer_id = l.lecturer_id " +
            "LEFT JOIN users u ON l.lecturer_id = u.user_id ";

    private static final String GROUP_BY_CLAUSE =
            "GROUP BY t.timetable_id, t.offering_id, t.day_of_week, t.start_time, t.end_time, " +
            "         t.venue, t.batch, t.department, co.academic_year, co.semester, c.course_code, c.course_name, c.credits ";

    private static final String ORDER_BY_CLAUSE =
            "ORDER BY FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'), t.start_time ASC, t.timetable_id ASC";

    /**
     * Retrieves all timetable entries ordered by day and start time.
     */
    public List<TimetableEntry> getAllTimetables() {
        List<TimetableEntry> list = new ArrayList<>();
        String sql = BASE_SELECT + GROUP_BY_CLAUSE + ORDER_BY_CLAUSE;

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntry(rs));
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getAllTimetables failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves a single timetable entry by primary key.
     */
    public TimetableEntry getTimetableById(String timetableId) {
        if (timetableId == null || timetableId.isBlank()) return null;
        String sql = BASE_SELECT + "WHERE t.timetable_id = ? " + GROUP_BY_CLAUSE;

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, timetableId.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntry(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getTimetableById failed for " + timetableId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Retrieves timetables relevant to an undergraduate by batch and department.
     */
    public List<TimetableEntry> getTimetablesForStudent(String batch, String department) {
        List<TimetableEntry> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (batch != null && !batch.isBlank()) {
            sql.append("AND t.batch = ? ");
            params.add(batch.trim());
        }
        if (department != null && !department.isBlank()) {
            sql.append("AND t.department = ? ");
            params.add(department.trim());
        }

        sql.append(GROUP_BY_CLAUSE).append(ORDER_BY_CLAUSE);

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntry(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getTimetablesForStudent failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves timetables where the given lecturer is assigned.
     */
    public List<TimetableEntry> getTimetablesForLecturer(String lecturerId) {
        List<TimetableEntry> list = new ArrayList<>();
        String sql = BASE_SELECT +
                "WHERE t.offering_id IN (SELECT offering_id FROM course_lecturers WHERE lecturer_id = ?) " +
                GROUP_BY_CLAUSE + ORDER_BY_CLAUSE;

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lecturerId.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntry(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getTimetablesForLecturer failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves timetables for technical officers by department.
     */
    public List<TimetableEntry> getTimetablesForDepartment(String department) {
        List<TimetableEntry> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.department = ? " + GROUP_BY_CLAUSE + ORDER_BY_CLAUSE;

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntry(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getTimetablesForDepartment failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Dynamic search and filter query.
     */
    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String batchFilter, String departmentFilter) {
        List<TimetableEntry> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (dayFilter != null && !dayFilter.isBlank() && !dayFilter.equalsIgnoreCase("ALL") && !dayFilter.equalsIgnoreCase("All Days")) {
            sql.append("AND t.day_of_week = ? ");
            params.add(dayFilter.trim().toUpperCase());
        }

        if (batchFilter != null && !batchFilter.isBlank() && !batchFilter.equalsIgnoreCase("ALL") && !batchFilter.equalsIgnoreCase("All Batches")) {
            sql.append("AND t.batch = ? ");
            params.add(batchFilter.trim());
        }

        if (departmentFilter != null && !departmentFilter.isBlank() && !departmentFilter.equalsIgnoreCase("ALL") && !departmentFilter.equalsIgnoreCase("All Departments")) {
            sql.append("AND t.department = ? ");
            params.add(departmentFilter.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append("AND (t.timetable_id LIKE ? OR t.venue LIKE ? OR c.course_code LIKE ? OR c.course_name LIKE ? OR t.batch LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(GROUP_BY_CLAUSE).append(ORDER_BY_CLAUSE);

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntry(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] searchTimetables failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Suggests the next sequential Timetable ID (e.g. TBL007).
     */
    public String getNextSuggestedTimetableId() {
        String sql = "SELECT timetable_id FROM timetables WHERE timetable_id LIKE 'TBL%' ORDER BY LENGTH(timetable_id) DESC, timetable_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String lastId = rs.getString("timetable_id");
                if (lastId != null && lastId.startsWith("TBL")) {
                    String numPart = lastId.substring(3).trim();
                    try {
                        int nextNum = Integer.parseInt(numPart) + 1;
                        return String.format("TBL%03d", nextNum);
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getNextSuggestedTimetableId failed: " + e.getMessage());
        }

        return "TBL001";
    }

    /**
     * Checks if a timetable ID exists.
     */
    public boolean existsTimetableId(String timetableId) {
        if (timetableId == null || timetableId.isBlank()) return false;
        String sql = "SELECT 1 FROM timetables WHERE timetable_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, timetableId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] existsTimetableId failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserts a new timetable slot.
     */
    public boolean addTimetable(TimetableEntry entry) throws SQLException {
        if (entry == null) return false;

        String sql = "INSERT INTO timetables (timetable_id, offering_id, day_of_week, start_time, end_time, venue, batch, department) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getTimetableId());
            pstmt.setString(2, entry.getOfferingId());
            pstmt.setString(3, entry.getDayOfWeek());
            pstmt.setTime(4, entry.getStartTime());
            pstmt.setTime(5, entry.getEndTime());
            pstmt.setString(6, entry.getVenue());
            pstmt.setString(7, entry.getBatch());
            pstmt.setString(8, entry.getDepartment());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing timetable slot.
     */
    public boolean updateTimetable(TimetableEntry entry) throws SQLException {
        if (entry == null || entry.getTimetableId() == null) return false;

        String sql = "UPDATE timetables SET offering_id = ?, day_of_week = ?, start_time = ?, end_time = ?, venue = ?, batch = ?, department = ? " +
                     "WHERE timetable_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getOfferingId());
            pstmt.setString(2, entry.getDayOfWeek());
            pstmt.setTime(3, entry.getStartTime());
            pstmt.setTime(4, entry.getEndTime());
            pstmt.setString(5, entry.getVenue());
            pstmt.setString(6, entry.getBatch());
            pstmt.setString(7, entry.getDepartment());
            pstmt.setString(8, entry.getTimetableId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a timetable slot by ID.
     */
    public boolean deleteTimetable(String timetableId) throws SQLException {
        if (timetableId == null || timetableId.isBlank()) return false;

        String sql = "DELETE FROM timetables WHERE timetable_id = ?";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, timetableId.trim());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves available course offerings for populating offering combo box.
     */
    public Map<String, String> getAvailableOfferings() {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT co.offering_id, co.academic_year, co.semester, c.course_code, c.course_name " +
                     "FROM course_offerings co " +
                     "LEFT JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                     "ORDER BY co.offering_id ASC";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("offering_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                String year = rs.getString("academic_year");
                String sem = rs.getString("semester");

                String label = id + " - " + code + " (" + name + ") [" + year + " " + sem + "]";
                map.put(id, label);
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getAvailableOfferings failed: " + e.getMessage());
        }

        return map;
    }

    /**
     * Checks if a venue has a scheduling collision.
     */
    public boolean hasVenueCollision(String excludeTimetableId, String venue, String dayOfWeek, Time start, Time end) {
        String sql = "SELECT timetable_id FROM timetables " +
                     "WHERE venue = ? AND day_of_week = ? " +
                     "  AND start_time < ? AND end_time > ? " +
                     (excludeTimetableId != null ? "  AND timetable_id <> ? " : "") +
                     "LIMIT 1";

        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, venue.trim());
            pstmt.setString(2, dayOfWeek.trim().toUpperCase());
            pstmt.setTime(3, end);
            pstmt.setTime(4, start);
            if (excludeTimetableId != null) {
                pstmt.setString(5, excludeTimetableId.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] hasVenueCollision check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves distinct batches.
     */
    public List<String> getDistinctBatches() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT batch FROM timetables UNION SELECT DISTINCT batch FROM undergraduates ORDER BY 1 ASC";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String b = rs.getString(1);
                if (b != null && !b.isBlank()) {
                    list.add(b.trim());
                }
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getDistinctBatches failed: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list.add("2024/2025");
        }
        return list;
    }

    /**
     * Retrieves summary counts for metric cards.
     */
    public Map<String, Integer> getSummaryMetrics() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("sessions", 0);
        map.put("courses", 0);
        map.put("venues", 0);
        map.put("batches", 0);

        String sql = "SELECT " +
                     "    COUNT(t.timetable_id) AS total_sessions, " +
                     "    COUNT(DISTINCT t.offering_id) AS total_courses, " +
                     "    COUNT(DISTINCT t.venue) AS total_venues, " +
                     "    COUNT(DISTINCT t.batch) AS total_batches " +
                     "FROM timetables t";

        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                map.put("sessions", rs.getInt("total_sessions"));
                map.put("courses", rs.getInt("total_courses"));
                map.put("venues", rs.getInt("total_venues"));
                map.put("batches", rs.getInt("total_batches"));
            }

        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getSummaryMetrics failed: " + e.getMessage());
        }

        return map;
    }

    private TimetableEntry mapResultSetToEntry(ResultSet rs) throws SQLException {
        TimetableEntry e = new TimetableEntry();
        e.setTimetableId(rs.getString("timetable_id"));
        e.setOfferingId(rs.getString("offering_id"));
        e.setDayOfWeek(rs.getString("day_of_week"));
        e.setStartTime(rs.getTime("start_time"));
        e.setEndTime(rs.getTime("end_time"));
        e.setVenue(rs.getString("venue"));
        e.setBatch(rs.getString("batch"));
        e.setDepartment(rs.getString("department"));

        e.setAcademicYear(rs.getString("academic_year"));
        e.setSemester(rs.getString("semester"));
        e.setCourseCode(rs.getString("course_code"));
        e.setCourseName(rs.getString("course_name"));
        e.setCredits(rs.getInt("credits"));

        String staff = rs.getString("lecturers");
        e.setLecturers(staff != null && !staff.isBlank() ? staff : "Unassigned");

        return e;
    }

    public List<String> getAvailableDepartments() {
        List<String> depts = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM timetables WHERE department IS NOT NULL AND department != '' ORDER BY department ASC";
        try (Connection conn = DBConnection.getNewConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String d = rs.getString("department");
                if (d != null && !d.isBlank() && !depts.contains(d)) {
                    depts.add(d);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TimetableDAO ERROR] getAvailableDepartments failed: " + e.getMessage());
        }
        if (!depts.contains("Information and Communication Technology")) {
            depts.add(0, "Information and Communication Technology");
        }
        return depts;
    }

    public List<String> getAvailableAcademicYears() {
        List<String> years = new ArrayList<>();
        years.add("All Years");
        years.add("2026/2027");
        years.add("2025/2026");
        years.add("2024/2025");
        return years;
    }

    public List<String> getAvailableSemesters() {
        List<String> semesters = new ArrayList<>();
        semesters.add("All Semesters");
        semesters.add("SEMESTER 1");
        semesters.add("SEMESTER 2");
        return semesters;
    }

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department) {
        return searchTimetables(keyword, dayFilter, department, "All Sessions", "All Years", "All Semesters");
    }

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department,
                                                 String sessionType, String academicYear, String semester) {
        List<TimetableEntry> source;
        if (department == null || department.isBlank() || department.equalsIgnoreCase("All Departments")) {
            source = getAllTimetables();
        } else {
            source = getTimetablesForDepartment(department);
        }

        String kw = (keyword != null) ? keyword.trim().toLowerCase() : "";
        String day = (dayFilter != null && !dayFilter.equalsIgnoreCase("All Days")) ? dayFilter.trim().toUpperCase() : "";
        String type = (sessionType != null) ? sessionType.trim().toLowerCase() : "all sessions";
        String targetYear = (academicYear != null && !academicYear.equalsIgnoreCase("All Years")) ? academicYear.trim() : "";
        String targetSem = (semester != null && !semester.equalsIgnoreCase("All Semesters")) ? semester.trim().toUpperCase() : "";

        List<TimetableEntry> filtered = new ArrayList<>();
        for (TimetableEntry e : source) {
            boolean matchesDay = day.isEmpty() || (e.getDayOfWeek() != null && e.getDayOfWeek().equalsIgnoreCase(day));

            boolean matchesKw = kw.isEmpty()
                    || (e.getCourseCode() != null && e.getCourseCode().toLowerCase().contains(kw))
                    || (e.getCourseName() != null && e.getCourseName().toLowerCase().contains(kw))
                    || (e.getVenue() != null && e.getVenue().toLowerCase().contains(kw))
                    || (e.getLecturers() != null && e.getLecturers().toLowerCase().contains(kw))
                    || (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(kw));

            boolean matchesType = true;
            if (type.contains("lab") || type.contains("practical")) {
                matchesType = e.isLabSession();
            } else if (type.contains("lecture")) {
                matchesType = !e.isLabSession();
            }

            boolean matchesYear = targetYear.isEmpty()
                    || (e.getAcademicYear() != null && e.getAcademicYear().equalsIgnoreCase(targetYear));

            boolean matchesSem = targetSem.isEmpty()
                    || (e.getSemester() != null && e.getSemester().equalsIgnoreCase(targetSem));

            if (matchesDay && matchesKw && matchesType && matchesYear && matchesSem) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public String getNextTimetableId() {
        int maxId = 0;
        List<TimetableEntry> all = getAllTimetables();
        for (TimetableEntry e : all) {
            String tid = e.getTimetableId();
            if (tid != null && tid.toUpperCase().startsWith("TBL")) {
                try {
                    int num = Integer.parseInt(tid.substring(3).trim());
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("TBL%03d", maxId + 1);
    }

    public boolean addTimetableEntry(TimetableEntry entry) {
        if (entry == null) return false;
        if (entry.getTimetableId() == null || entry.getTimetableId().isBlank()) {
            entry.setTimetableId(getNextTimetableId());
        }

        String sql = "INSERT INTO timetables (timetable_id, offering_id, day_of_week, start_time, end_time, venue, batch, department) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getTimetableId());
            ps.setString(2, entry.getOfferingId() != null ? entry.getOfferingId() : "OFF001");
            ps.setString(3, entry.getDayOfWeek() != null ? entry.getDayOfWeek().toUpperCase() : "MONDAY");
            ps.setTime(4, entry.getStartTime() != null ? entry.getStartTime() : Time.valueOf("08:30:00"));
            ps.setTime(5, entry.getEndTime() != null ? entry.getEndTime() : Time.valueOf("10:30:00"));
            ps.setString(6, entry.getVenue() != null ? entry.getVenue() : "ICT Lab 01");
            ps.setString(7, entry.getBatch() != null ? entry.getBatch() : "2024/2025");
            ps.setString(8, entry.getDepartment() != null ? entry.getDepartment() : "Information and Communication Technology");

            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("[TimetableDAO ERROR] addTimetableEntry failed: " + ex.getMessage());
        }
        return false;
    }

    public boolean deleteTimetableEntry(String timetableId) {
        if (timetableId == null) return false;
        String sql = "DELETE FROM timetables WHERE timetable_id = ?";
        try (Connection conn = DBConnection.getNewConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, timetableId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("[TimetableDAO ERROR] deleteTimetableEntry failed: " + ex.getMessage());
        }
        return false;
    }
}
