package dao;

import model.TimetableEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Academic Timetables in FAMS.
 * Interfaces with the 'timetables' table and joins with 'course_offerings',
 * 'courses', 'course_lecturers', and 'users'.
 * Provides high-fidelity in-memory fallback when MySQL is offline.
 * Supports multi-dimensional filtering by department, day of week, session type,
 * academic year, semester, and text keyword search.
 */
public class TimetableDAO {

    private final List<TimetableEntry> mockEntries = new ArrayList<>();

    public TimetableDAO() {
        initMockData();
    }

    private void initMockData() {
        // Monday - Lab Session
        TimetableEntry e1 = new TimetableEntry("TBL001", "OFF001", "MONDAY",
                Time.valueOf("08:30:00"), Time.valueOf("10:30:00"), "ICT Lab 01", "2024/2025",
                "Information and Communication Technology");
        e1.setCourseCode("ICT2132");
        e1.setCourseName("Object Oriented Programming");
        e1.setCredits(3);
        e1.setAcademicYear("2026/2027");
        e1.setSemester("SEMESTER 1");
        e1.setLecturers("Kasun Perera");
        mockEntries.add(e1);

        // Wednesday - Lecture Session
        TimetableEntry e2 = new TimetableEntry("TBL002", "OFF001", "WEDNESDAY",
                Time.valueOf("13:00:00"), Time.valueOf("15:00:00"), "Lecture Hall 02", "2024/2025",
                "Information and Communication Technology");
        e2.setCourseCode("ICT2132");
        e2.setCourseName("Object Oriented Programming");
        e2.setCredits(3);
        e2.setAcademicYear("2026/2027");
        e2.setSemester("SEMESTER 1");
        e2.setLecturers("Kasun Perera");
        mockEntries.add(e2);

        // Tuesday - Lecture Session
        TimetableEntry e3 = new TimetableEntry("TBL003", "OFF002", "TUESDAY",
                Time.valueOf("08:30:00"), Time.valueOf("10:30:00"), "Lecture Hall 03", "2024/2025",
                "Information and Communication Technology");
        e3.setCourseCode("ICT2112");
        e3.setCourseName("Data Structures and Algorithms");
        e3.setCredits(2);
        e3.setAcademicYear("2026/2027");
        e3.setSemester("SEMESTER 1");
        e3.setLecturers("Nadeesha Fernando");
        mockEntries.add(e3);

        // Thursday - Database Management Lecture
        TimetableEntry e4 = new TimetableEntry("TBL004", "OFF003", "THURSDAY",
                Time.valueOf("10:30:00"), Time.valueOf("12:30:00"), "Lecture Hall 01", "2024/2025",
                "Information and Communication Technology");
        e4.setCourseCode("ICT2122");
        e4.setCourseName("Database Systems");
        e4.setCredits(2);
        e4.setAcademicYear("2026/2027");
        e4.setSemester("SEMESTER 1");
        e4.setLecturers("Tharindu Silva");
        mockEntries.add(e4);

        // Friday - Web Development Lab Practical
        TimetableEntry e5 = new TimetableEntry("TBL005", "OFF004", "FRIDAY",
                Time.valueOf("08:30:00"), Time.valueOf("10:30:00"), "ICT Lab 02", "2024/2025",
                "Information and Communication Technology");
        e5.setCourseCode("ICT2152");
        e5.setCourseName("Web Development");
        e5.setCredits(3);
        e5.setAcademicYear("2026/2027");
        e5.setSemester("SEMESTER 2");
        e5.setLecturers("Ayesha Bandara");
        mockEntries.add(e5);

        // Saturday - Software Engineering Practical
        TimetableEntry e6 = new TimetableEntry("TBL006", "OFF005", "SATURDAY",
                Time.valueOf("09:00:00"), Time.valueOf("11:00:00"), "ICT Lab 03", "2024/2025",
                "Information and Communication Technology");
        e6.setCourseCode("ICT2142");
        e6.setCourseName("Software Engineering");
        e6.setCredits(2);
        e6.setAcademicYear("2026/2027");
        e6.setSemester("SEMESTER 2");
        e6.setLecturers("Ruwan Jayasinghe");
        mockEntries.add(e6);

        // Biosystems Technology Timetable Entries
        TimetableEntry e7 = new TimetableEntry("TBL007", "OFF006", "MONDAY",
                Time.valueOf("10:30:00"), Time.valueOf("12:30:00"), "Bio Tech Lab 01", "2024/2025",
                "Biosystems Technology");
        e7.setCourseCode("BST2113");
        e7.setCourseName("Food Processing Technology");
        e7.setCredits(3);
        e7.setAcademicYear("2026/2027");
        e7.setSemester("SEMESTER 1");
        e7.setLecturers("Dr. H. M. Senarathne");
        mockEntries.add(e7);

        TimetableEntry e8 = new TimetableEntry("TBL008", "OFF007", "WEDNESDAY",
                Time.valueOf("08:30:00"), Time.valueOf("10:30:00"), "Agriculture Lab 02", "2024/2025",
                "Biosystems Technology");
        e8.setCourseCode("BST2122");
        e8.setCourseName("Agricultural Biotechnology");
        e8.setCredits(2);
        e8.setAcademicYear("2026/2027");
        e8.setSemester("SEMESTER 1");
        e8.setLecturers("Prof. K. Jayawardena");
        mockEntries.add(e8);

        // Engineering Technology Timetable Entries
        TimetableEntry e9 = new TimetableEntry("TBL009", "OFF008", "TUESDAY",
                Time.valueOf("13:00:00"), Time.valueOf("15:00:00"), "Mechanical Workshop", "2024/2025",
                "Engineering Technology");
        e9.setCourseCode("ENT2113");
        e9.setCourseName("Applied Thermodynamics");
        e9.setCredits(3);
        e9.setAcademicYear("2026/2027");
        e9.setSemester("SEMESTER 1");
        e9.setLecturers("Dr. S. Wickramasinghe");
        mockEntries.add(e9);

        TimetableEntry e10 = new TimetableEntry("TBL010", "OFF009", "THURSDAY",
                Time.valueOf("08:30:00"), Time.valueOf("10:30:00"), "Electronics Lab 01", "2024/2025",
                "Engineering Technology");
        e10.setCourseCode("ENT2123");
        e10.setCourseName("Circuits and Systems");
        e10.setCredits(3);
        e10.setAcademicYear("2026/2027");
        e10.setSemester("SEMESTER 1");
        e10.setLecturers("Eng. N. Gamage");
        mockEntries.add(e10);

        // Multidisciplinary Studies Timetable Entry
        TimetableEntry e11 = new TimetableEntry("TBL011", "OFF010", "FRIDAY",
                Time.valueOf("10:30:00"), Time.valueOf("12:30:00"), "Main Auditorium", "2024/2025",
                "Multidisciplinary Studies");
        e11.setCourseCode("MDS2112");
        e11.setCourseName("Professional Ethics and Law");
        e11.setCredits(2);
        e11.setAcademicYear("2026/2027");
        e11.setSemester("SEMESTER 1");
        e11.setLecturers("Attorney D. De Silva");
        mockEntries.add(e11);
    }

    private static final String BASE_SELECT =
            "SELECT t.timetable_id, t.offering_id, t.day_of_week, t.start_time, t.end_time, " +
            "       t.venue, t.batch, t.department, " +
            "       co.academic_year, co.semester, " +
            "       c.course_code, c.course_name, c.credits, " +
            "       GROUP_CONCAT(DISTINCT TRIM(CONCAT(IFNULL(u.first_name, ''), ' ', IFNULL(u.last_name, ''))) SEPARATOR ', ') AS lecturers " +
            "FROM timetables t " +
            "LEFT JOIN course_offerings co ON t.offering_id = co.offering_id " +
            "LEFT JOIN courses c ON co.course_id = c.course_id " +
            "LEFT JOIN course_lecturers cl ON t.offering_id = cl.offering_id " +
            "LEFT JOIN lecturers l ON cl.lecturer_id = l.lecturer_id " +
            "LEFT JOIN users u ON l.lecturer_id = u.user_id ";

    private static final String GROUP_BY_CLAUSE =
            "GROUP BY t.timetable_id, t.offering_id, t.day_of_week, t.start_time, t.end_time, " +
            "         t.venue, t.batch, t.department, co.academic_year, co.semester, c.course_code, c.course_name, c.credits ";

    private static final String ORDER_BY_CLAUSE =
            "ORDER BY FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'), t.start_time ASC, t.timetable_id ASC";

    public List<TimetableEntry> getAllTimetables() {
        List<TimetableEntry> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = BASE_SELECT + GROUP_BY_CLAUSE + ORDER_BY_CLAUSE;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToEntry(rs));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[TimetableDAO ERROR] getAllTimetables failed: " + e.getMessage());
            }
        }
        return new ArrayList<>(mockEntries);
    }

    public List<TimetableEntry> getTimetablesForDepartment(String department) {
        if (department == null || department.trim().isEmpty() || "All Departments".equalsIgnoreCase(department)) {
            return getAllTimetables();
        }
        department = department.trim();
        List<TimetableEntry> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = BASE_SELECT + "WHERE LOWER(t.department) = LOWER(?) " + GROUP_BY_CLAUSE + ORDER_BY_CLAUSE;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, department);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToEntry(rs));
                    }
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[TimetableDAO ERROR] getTimetablesForDepartment failed: " + e.getMessage());
            }
        }

        for (TimetableEntry e : mockEntries) {
            if (e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(department)) {
                list.add(e);
            }
        }
        return list;
    }

    public List<String> getAvailableDepartments() {
        List<String> depts = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT DISTINCT department FROM timetables WHERE department IS NOT NULL AND department != '' ORDER BY department ASC";
            try (Statement stmt = conn.createStatement();
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
        }
        for (TimetableEntry e : mockEntries) {
            if (e.getDepartment() != null && !e.getDepartment().isBlank() && !depts.contains(e.getDepartment())) {
                depts.add(e.getDepartment());
            }
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

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department, String sessionType) {
        return searchTimetables(keyword, dayFilter, department, sessionType, "All Years", "All Semesters");
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
            // 1. Day of week
            boolean matchesDay = day.isEmpty() || (e.getDayOfWeek() != null && e.getDayOfWeek().equalsIgnoreCase(day));

            // 2. Keyword Search
            boolean matchesKw = kw.isEmpty()
                    || (e.getCourseCode() != null && e.getCourseCode().toLowerCase().contains(kw))
                    || (e.getCourseName() != null && e.getCourseName().toLowerCase().contains(kw))
                    || (e.getVenue() != null && e.getVenue().toLowerCase().contains(kw))
                    || (e.getLecturers() != null && e.getLecturers().toLowerCase().contains(kw))
                    || (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(kw));

            // 3. Session Type
            boolean matchesType = true;
            if (type.contains("lab") || type.contains("practical")) {
                matchesType = e.isLabSession();
            } else if (type.contains("lecture")) {
                matchesType = !e.isLabSession();
            }

            // 4. Academic Year
            boolean matchesYear = targetYear.isEmpty()
                    || (e.getAcademicYear() != null && e.getAcademicYear().equalsIgnoreCase(targetYear));

            // 5. Semester
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

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO timetables (timetable_id, offering_id, day_of_week, start_time, end_time, venue, batch, department) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entry.getTimetableId());
                ps.setString(2, entry.getOfferingId() != null ? entry.getOfferingId() : "OFF001");
                ps.setString(3, entry.getDayOfWeek() != null ? entry.getDayOfWeek().toUpperCase() : "MONDAY");
                ps.setTime(4, entry.getStartTime() != null ? entry.getStartTime() : Time.valueOf("08:30:00"));
                ps.setTime(5, entry.getEndTime() != null ? entry.getEndTime() : Time.valueOf("10:30:00"));
                ps.setString(6, entry.getVenue() != null ? entry.getVenue() : "ICT Lab 01");
                ps.setString(7, entry.getBatch() != null ? entry.getBatch() : "2024/2025");
                ps.setString(8, entry.getDepartment() != null ? entry.getDepartment() : "Information and Communication Technology");

                ps.executeUpdate();
            } catch (SQLException ex) {
                System.err.println("[TimetableDAO ERROR] addTimetableEntry failed: " + ex.getMessage());
            }
        }

        // Always update mockEntries so in-memory store and offline mode reflect the new entry
        mockEntries.removeIf(e -> e.getTimetableId().equalsIgnoreCase(entry.getTimetableId()));
        mockEntries.add(entry);

        return true;
    }

    public boolean deleteTimetableEntry(String timetableId) {
        if (timetableId == null) return false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "DELETE FROM timetables WHERE timetable_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, timetableId);
                ps.executeUpdate();
            } catch (SQLException ex) {
                System.err.println("[TimetableDAO ERROR] deleteTimetableEntry failed: " + ex.getMessage());
            }
        }
        mockEntries.removeIf(e -> e.getTimetableId().equalsIgnoreCase(timetableId));
        return true;
    }

    private TimetableEntry mapResultSetToEntry(ResultSet rs) throws SQLException {
        TimetableEntry e = new TimetableEntry(
                rs.getString("timetable_id"),
                rs.getString("offering_id"),
                rs.getString("day_of_week"),
                rs.getTime("start_time"),
                rs.getTime("end_time"),
                rs.getString("venue"),
                rs.getString("batch"),
                rs.getString("department")
        );
        e.setAcademicYear(rs.getString("academic_year"));
        e.setSemester(rs.getString("semester"));
        e.setCourseCode(rs.getString("course_code"));
        e.setCourseName(rs.getString("course_name"));
        e.setCredits(rs.getInt("credits"));
        e.setLecturers(rs.getString("lecturers"));
        return e;
    }
}
