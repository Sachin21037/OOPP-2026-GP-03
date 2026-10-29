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
 */
public class TimetableDAO {

    private final List<TimetableEntry> mockEntries = new ArrayList<>();

    public TimetableDAO() {
        initMockData();
    }

    private void initMockData() {
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

        TimetableEntry e5 = new TimetableEntry("TBL005", "OFF004", "FRIDAY",
                Time.valueOf("08:30:00"), Time.valueOf("10:30:00"), "ICT Lab 02", "2024/2025",
                "Information and Communication Technology");
        e5.setCourseCode("ICT2142");
        e5.setCourseName("Computer Networks");
        e5.setCredits(2);
        e5.setAcademicYear("2026/2027");
        e5.setSemester("SEMESTER 1");
        e5.setLecturers("Ayesha Bandara");
        mockEntries.add(e5);

        TimetableEntry e6 = new TimetableEntry("TBL006", "OFF005", "SATURDAY",
                Time.valueOf("09:00:00"), Time.valueOf("11:00:00"), "ICT Lab 03", "2024/2025",
                "Information and Communication Technology");
        e6.setCourseCode("ICT2152");
        e6.setCourseName("Software Engineering");
        e6.setCredits(2);
        e6.setAcademicYear("2026/2027");
        e6.setSemester("SEMESTER 1");
        e6.setLecturers("Ruwan Jayasinghe");
        mockEntries.add(e6);
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
        if (department == null || department.trim().isEmpty()) {
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

    public List<TimetableEntry> searchTimetables(String keyword, String dayFilter, String department) {
        List<TimetableEntry> source = (department != null && !department.isBlank())
                ? getTimetablesForDepartment(department)
                : getAllTimetables();

        String kw = (keyword != null) ? keyword.trim().toLowerCase() : "";
        String day = (dayFilter != null && !dayFilter.equalsIgnoreCase("All Days")) ? dayFilter.trim().toUpperCase() : "";

        List<TimetableEntry> filtered = new ArrayList<>();
        for (TimetableEntry e : source) {
            boolean matchesDay = day.isEmpty() || (e.getDayOfWeek() != null && e.getDayOfWeek().equalsIgnoreCase(day));
            boolean matchesKw = kw.isEmpty()
                    || (e.getCourseCode() != null && e.getCourseCode().toLowerCase().contains(kw))
                    || (e.getCourseName() != null && e.getCourseName().toLowerCase().contains(kw))
                    || (e.getVenue() != null && e.getVenue().toLowerCase().contains(kw))
                    || (e.getLecturers() != null && e.getLecturers().toLowerCase().contains(kw));

            if (matchesDay && matchesKw) {
                filtered.add(e);
            }
        }
        return filtered;
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
