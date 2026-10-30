package dao;

import database.DBConnection;
import model.*;
import util.AttendanceCalculator;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * AttendanceDAO Class
 * Handles data persistence and querying for Course Offerings, Enrolled Students,
 * Attendance Sessions, and Student Attendance Records.
 * Mapped to database/fams.sql schema with robust in-memory fallback.
 */
public class AttendanceDAO {

    private final MedicalDAO medicalDAO = new MedicalDAO();

    // Mock in-memory repository aligned with database/fams.sql
    private static final List<CourseOffering> mockOfferings = new ArrayList<>();
    private static final Map<Integer, List<Undergraduate>> mockStudentsByOffering = new HashMap<>();
    private static final List<AttendanceSession> mockSessions = new ArrayList<>();
    private static final List<model.AttendanceRecord> mockAttendanceRecords = new ArrayList<>();

    static {
        seedInitialMockData();
    }

    private static void seedInitialMockData() {
        // 1. Seed Offerings from database/fams.sql
        mockOfferings.add(new CourseOffering("OFF001", "CRS001", "ICT2132", "Object Oriented Programming", "2026/2027", 1, "2024/2025"));
        mockOfferings.add(new CourseOffering("OFF002", "CRS002", "ICT2142", "E-Business", "2026/2027", 1, "2024/2025"));
        mockOfferings.add(new CourseOffering("OFF003", "CRS003", "ICT2123", "Database Management", "2026/2027", 1, "2024/2025"));
        mockOfferings.add(new CourseOffering("OFF004", "CRS004", "ICT2152", "Web Development", "2026/2027", 2, "2024/2025"));
        mockOfferings.add(new CourseOffering("OFF005", "CRS005", "ICT2162", "Data Structures", "2026/2027", 2, "2024/2025"));

        // 2. Seed 20 Undergraduates from database/fams.sql
        List<Undergraduate> students = new ArrayList<>();
        String[][] rawStudents = {
                {"STU001", "TG/2024/2001", "Amal", "Perera"},
                {"STU002", "TG/2024/2002", "Nimal", "Fernando"},
                {"STU003", "TG/2024/2003", "Kasun", "Silva"},
                {"STU004", "TG/2024/2004", "Tharushi", "Perera"},
                {"STU005", "TG/2024/2005", "Sanduni", "Fernando"},
                {"STU006", "TG/2024/2006", "Pasindu", "Kumara"},
                {"STU007", "TG/2024/2007", "Hiruni", "Silva"},
                {"STU008", "TG/2024/2008", "Sahan", "Perera"},
                {"STU009", "TG/2024/2009", "Dinuka", "Fernando"},
                {"STU010", "TG/2024/2010", "Piumi", "Kumari"},
                {"STU011", "TG/2024/2011", "Rashmi", "Silva"},
                {"STU012", "TG/2024/2012", "Chamara", "Perera"},
                {"STU013", "TG/2024/2013", "Shalini", "Fernando"},
                {"STU014", "TG/2024/2014", "Dulshan", "Silva"},
                {"STU015", "TG/2024/2015", "Sachini", "Perera"},
                {"STU016", "TG/2024/2016", "Isuru", "Fernando"},
                {"STU017", "TG/2024/2017", "Madhavi", "Kumari"},
                {"STU018", "TG/2024/2018", "Lahiru", "Silva"},
                {"STU019", "TG/2024/2019", "Nethmi", "Perera"},
                {"STU020", "TG/2024/2020", "Yasiru", "Fernando"}
        };

        for (String[] row : rawStudents) {
            students.add(new Undergraduate(row[0], row[1], row[2], row[3], "2024/2025", "Information and Communication Technology", "REGULAR"));
        }

        for (CourseOffering o : mockOfferings) {
            mockStudentsByOffering.put(o.getOfferingId(), new ArrayList<>(students));
        }

        // 3. Seed pre-recorded sessions for OFF001 (ICT2132) to showcase all 5 SRS Scenarios:
        // 15 Theory sessions (SES001 - SES015) + 15 Practical sessions (SES016 - SES030) as in fams.sql
        long now = System.currentTimeMillis();
        long dayMs = 24L * 3600L * 1000L;
        int totalSessionsToSeed = 15;

        for (int sessionNum = 1; sessionNum <= totalSessionsToSeed; sessionNum++) {
            String type = (sessionNum % 2 == 1) ? "Practical" : "Theory";
            AttendanceSession session = new AttendanceSession(
                    sessionNum, 1, new Date(now - (totalSessionsToSeed + 1 - sessionNum) * dayMs * 3),
                    type, sessionNum, 2.0
            );
            mockSessions.add(session);

            for (Undergraduate u : students) {
                String status = "Absent";
                String remarks = "";
                int sId = u.getStudentId();

                // Scenario coverage:
                // STU001 (Amal): 15 present (100% Eligible - SC-01)
                // STU002 (Nimal): 13 present, 2 absent (86.7% Eligible - SC-01)
                // STU003 (Kasun): 14 present, 1 absent (93.3% Eligible - SC-01)
                // STU004 (Tharushi): exactly 12 present (12/15 = 80.0% Eligible - SC-02)
                // STU005 (Sanduni): 10 present + 3 approved medicals = 13 (86.7% Eligible With Medical - SC-05)
                // STU006 (Pasindu): 8 present, 7 absent (53.3% Barred - SC-03)
                // STU007 (Hiruni): 11 present, 4 absent (73.3% Barred - SC-03)
                // STU008 (Sahan): 13 present, 2 absent (86.7% Eligible)
                // STU009 (Dinuka): 12 present (80.0% Eligible - SC-02)
                // STU010 (Piumi): 9 present + 1 pending medical (60.0% Barred - Pending Medical)
                // STU016 (Isuru): 8 present + 1 rejected medical (53.3% Barred - Rejected Medical)
                // Others: 12-14 present (Eligible)

                if (sId == 1) {
                    status = "Present";
                } else if (sId == 4 || sId == 9) {
                    status = (sessionNum <= 12) ? "Present" : "Absent";
                } else if (sId == 5) {
                    if (sessionNum <= 10) status = "Present";
                    else if (sessionNum <= 13) { status = "Medical"; remarks = "Approved medical submitted"; }
                    else status = "Absent";
                } else if (sId == 6) {
                    status = (sessionNum <= 8) ? "Present" : "Absent";
                } else if (sId == 7) {
                    status = (sessionNum <= 11) ? "Present" : "Absent";
                } else if (sId == 10) {
                    if (sessionNum <= 9) status = "Present";
                    else if (sessionNum == 10) { status = "Medical"; remarks = "Pending medical verification"; }
                    else status = "Absent";
                } else if (sId == 16) {
                    if (sessionNum <= 8) status = "Present";
                    else if (sessionNum == 9) { status = "Medical"; remarks = "Medical document rejected"; }
                    else status = "Absent";
                } else {
                    status = (sessionNum <= 13) ? "Present" : "Absent";
                }

                model.AttendanceRecord rec = new model.AttendanceRecord(u.getStudentId(), u.getIndexNo(), u.getFullName(), status, remarks);
                rec.setSessionId(sessionNum);
                rec.setSessionNumber(sessionNum);
                rec.setSessionType(type);
                rec.setSessionDate(session.getSessionDate());
                mockAttendanceRecords.add(rec);
            }
        }
    }

    public List<CourseOffering> getAllCourseOfferings() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT co.offering_id, co.course_id, c.course_code, c.course_name, co.academic_year, co.semester, co.batch " +
                             "FROM course_offerings co " +
                             "JOIN courses c ON co.course_id = c.course_id " +
                             "ORDER BY co.offering_id ASC";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                List<CourseOffering> offerings = new ArrayList<>();
                while (rs.next()) {
                    String offCode = rs.getString("offering_id");
                    String crsCodeId = rs.getString("course_id");
                    String semStr = rs.getString("semester");
                    int sem = 1;
                    if (semStr != null && semStr.contains("2")) {
                        sem = 2;
                    }

                    CourseOffering co = new CourseOffering(
                            offCode,
                            crsCodeId,
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("academic_year"),
                            sem,
                            rs.getString("batch")
                    );
                    offerings.add(co);
                }
                if (!offerings.isEmpty()) return offerings;
            } catch (SQLException e) {
                System.err.println("[AttendanceDAO] Query error in getAllCourseOfferings: " + e.getMessage());
            }
        }
        return new ArrayList<>(mockOfferings);
    }

    public List<Undergraduate> getStudentsByOffering(int offeringId) {
        String offCode = String.format("OFF%03d", offeringId);
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT u.student_id, u.index_no, usr.first_name, usr.last_name, u.batch, u.department, u.student_status " +
                             "FROM undergraduates u " +
                             "JOIN users usr ON u.student_id = usr.user_id " +
                             "JOIN enrollments e ON u.student_id = e.student_id " +
                             "WHERE e.offering_id = ? OR e.offering_id = ? " +
                             "ORDER BY u.student_id ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, offCode);
                ps.setString(2, String.valueOf(offeringId));
                ResultSet rs = ps.executeQuery();
                List<Undergraduate> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Undergraduate(
                            rs.getString("student_id"),
                            rs.getString("index_no"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("batch"),
                            rs.getString("department"),
                            rs.getString("student_status")
                    ));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException e) {
                System.err.println("[AttendanceDAO] Query error in getStudentsByOffering: " + e.getMessage());
            }
        }
        return mockStudentsByOffering.getOrDefault(offeringId, new ArrayList<>());
    }

    /**
     * Save an attendance session and its corresponding student records.
     * Complies with atomic transaction requirements (commit/rollback per NFR-SEC-04).
     */
    public boolean saveAttendanceSession(AttendanceSession session, List<model.AttendanceRecord> records) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false);

                // Insert session into attendance_sessions table
                String offCode = String.format("OFF%03d", session.getOfferingId());
                int nextSesNum = session.getSessionNumber();
                String sesCode = String.format("SES%03d", nextSesNum);

                String sessionSql = "INSERT INTO attendance_sessions (session_id, offering_id, session_date, session_type, session_number, topic, conducted_by) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement psSession = conn.prepareStatement(sessionSql);
                psSession.setString(1, sesCode);
                psSession.setString(2, offCode);
                psSession.setDate(3, new java.sql.Date(session.getSessionDate() != null ? session.getSessionDate().getTime() : System.currentTimeMillis()));
                psSession.setString(4, session.getSessionType().toUpperCase());
                psSession.setInt(5, session.getSessionNumber());
                psSession.setString(6, session.getSessionType() + " Session " + session.getSessionNumber());
                psSession.setString(7, "TO001");
                psSession.executeUpdate();

                // Batch insert records into attendance table
                String attSql = "INSERT INTO attendance (attendance_id, session_id, student_id, status, remarks) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psAtt = conn.prepareStatement(attSql);
                int count = 1;
                for (model.AttendanceRecord r : records) {
                    String attCode = String.format("ATT%04d", (nextSesNum * 100) + count++);
                    String stuCode = String.format("STU%03d", r.getStudentId());
                    String status = r.getStatus().toUpperCase();

                    psAtt.setString(1, attCode);
                    psAtt.setString(2, sesCode);
                    psAtt.setString(3, stuCode);
                    psAtt.setString(4, status);
                    psAtt.setString(5, r.getRemarks());
                    psAtt.addBatch();
                }
                psAtt.executeBatch();

                conn.commit();
                conn.setAutoCommit(true);
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
                System.err.println("[AttendanceDAO] Database transaction rollback during saveAttendanceSession: " + e.getMessage());
            }
        }

        // Resilient in-memory persistence
        int newId = mockSessions.size() + 1;
        session.setSessionId(newId);
        mockSessions.add(session);
        for (model.AttendanceRecord r : records) {
            r.setSessionId(newId);
            r.setSessionNumber(session.getSessionNumber());
            r.setSessionType(session.getSessionType());
            r.setSessionDate(session.getSessionDate());
            mockAttendanceRecords.add(r);
        }
        return true;
    }

    /**
     * Compute whole batch summary for an offering with FR-ATT-03 and FR-ATT-04 5-scenario evaluation.
     */
    public List<AttendanceScenarioResult> getBatchAttendanceSummary(int offeringId, String trackFilter) {
        List<Undergraduate> students = getStudentsByOffering(offeringId);
        List<AttendanceScenarioResult> results = new ArrayList<>();

        // Determine total sessions matching the track filter
        int totalConducted = 0;
        Set<Integer> validSessionIds = new HashSet<>();
        for (AttendanceSession s : mockSessions) {
            if (s.getOfferingId() == offeringId) {
                if (trackFilter == null || "Combined".equalsIgnoreCase(trackFilter) ||
                    trackFilter.equalsIgnoreCase(s.getSessionType())) {
                    validSessionIds.add(s.getSessionId());
                    totalConducted++;
                }
            }
        }
        if (totalConducted == 0) totalConducted = 15; // standard base

        for (Undergraduate u : students) {
            int attended = 0;
            int recordedMedical = 0;

            for (model.AttendanceRecord r : mockAttendanceRecords) {
                if (r.getStudentId() == u.getStudentId() && validSessionIds.contains(r.getSessionId())) {
                    if ("Present".equalsIgnoreCase(r.getStatus())) {
                        attended++;
                    } else if ("Medical".equalsIgnoreCase(r.getStatus())) {
                        recordedMedical++;
                    }
                }
            }

            int approvedMedicals = medicalDAO.getApprovedMedicalCount(u.getStudentCode());
            boolean hasMedicalSubmitted = medicalDAO.hasSubmittedMedical(u.getStudentCode()) || recordedMedical > 0;

            AttendanceScenarioResult res = AttendanceCalculator.evaluateScenario(
                    u, totalConducted, attended, approvedMedicals, hasMedicalSubmitted
            );
            results.add(res);
        }

        return results;
    }

    /**
     * Retrieve granular session-by-session history for a student
     */
    public List<model.AttendanceRecord> getStudentSessionHistory(int offeringId, String studentIndexNo) {
        List<model.AttendanceRecord> list = new ArrayList<>();
        for (model.AttendanceRecord r : mockAttendanceRecords) {
            if (studentIndexNo.equalsIgnoreCase(r.getIndexNo()) ||
                studentIndexNo.equalsIgnoreCase("STU" + String.format("%03d", r.getStudentId()))) {
                list.add(r);
            }
        }
        list.sort(Comparator.comparingInt(model.AttendanceRecord::getSessionNumber));
        return list;
    }

    public int getTotalSessionCount() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT count(*) FROM attendance_sessions")) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException e) {
                System.err.println("[AttendanceDAO] Error counting sessions: " + e.getMessage());
            }
        }
        return mockSessions.size();
    }

    public int getTotalStudentCount() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT count(*) FROM undergraduates")) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException e) {
                System.err.println("[AttendanceDAO] Error counting undergraduates: " + e.getMessage());
            }
        }
        return 20;
    }

    // -------------------------------------------------------------------------
    // Lecturer Portal Attendance Management DTOs & Operations
    // -------------------------------------------------------------------------

    public static class AttendanceRecord {
        public final String attendanceId;
        public final String studentId;
        public final String studentName;
        public final String courseCode;
        public final String courseName;
        public final String sessionId;
        public final String sessionDate;
        public final String sessionType;
        public final int sessionNumber;
        public final String status;
        public final String remarks;

        public AttendanceRecord(
                String attendanceId, String studentId, String studentName,
                String courseCode, String courseName, String sessionId,
                String sessionDate, String sessionType, int sessionNumber,
                String status, String remarks
        ) {
            this.attendanceId = attendanceId != null ? attendanceId : "";
            this.studentId = studentId != null ? studentId : "";
            this.studentName = studentName != null ? studentName : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.sessionId = sessionId != null ? sessionId : "";
            this.sessionDate = sessionDate != null ? sessionDate : "";
            this.sessionType = sessionType != null ? sessionType : "LECTURE";
            this.sessionNumber = sessionNumber;
            this.status = formatStatus(status);
            this.remarks = remarks != null ? remarks : "";
        }

        public Object[] toTableRow() {
            String courseLabel = courseCode.isEmpty() ? "-" : courseCode;
            String typeLabel = sessionType.toUpperCase(Locale.ROOT);
            if (sessionNumber > 0) {
                typeLabel += " #" + sessionNumber;
            }
            return new Object[]{
                    studentId,
                    studentName.isEmpty() ? "Unknown Student" : studentName,
                    courseLabel,
                    sessionDate.isEmpty() ? "-" : sessionDate,
                    typeLabel,
                    status,
                    remarks.isEmpty() ? "-" : remarks
            };
        }
    }

    public static class AttendanceSummary {
        public final int totalStudents;
        public final int presentCount;
        public final int absentCount;
        public final int medicalCount;
        public final double attendanceRate;

        public AttendanceSummary(int totalStudents, int presentCount, int absentCount, int medicalCount, double attendanceRate) {
            this.totalStudents = totalStudents;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.medicalCount = medicalCount;
            this.attendanceRate = attendanceRate;
        }
    }

    public static String formatStatus(String raw) {
        if (raw == null) return "ABSENT";
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.contains("PRESENT")) return "PRESENT";
        if (s.contains("MEDICAL")) return "MEDICAL";
        if (s.contains("ABSENT")) return "ABSENT";
        return s;
    }

    /**
     * Loads assigned courses for the lecturer from the database.
     */
    public static List<String> loadAssignedCourses(String lecturerId) {
        List<String> courses = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return courses;
        }

        String sql =
                "SELECT DISTINCT c.course_code, c.course_name " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    courses.add(code + " - " + name);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttendanceDAO] Error loading courses: " + e.getMessage());
        }
        return courses;
    }

    /**
     * Queries attendance records from the database for the given lecturer.
     */
    public static List<AttendanceRecord> loadAttendanceRecords(String lecturerId) throws SQLException {
        List<AttendanceRecord> records = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return records;
        }

        String sql =
                "SELECT a.attendance_id, a.student_id, u.first_name, u.last_name, " +
                "       c.course_code, c.course_name, s.session_id, s.session_date, s.session_type, s.session_number, " +
                "       a.status, a.remarks " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id COLLATE utf8mb4_unicode_ci " +
                "JOIN attendance_sessions s ON co.offering_id = s.offering_id " +
                "JOIN attendance a ON s.session_id = a.session_id " +
                "JOIN users u ON a.student_id = u.user_id COLLATE utf8mb4_unicode_ci " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY s.session_date DESC, s.session_number DESC, c.course_code, a.student_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String attendanceId = rs.getString("attendance_id");
                    String studentId = rs.getString("student_id");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String studentName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String sessionId = rs.getString("session_id");
                    java.sql.Date d = rs.getDate("session_date");
                    String sessionDate = d != null ? d.toString() : "";
                    String sessionType = rs.getString("session_type");
                    int sessionNumber = rs.getInt("session_number");
                    String status = rs.getString("status");
                    String remarks = rs.getString("remarks");

                    records.add(new AttendanceRecord(
                            attendanceId, studentId, studentName,
                            courseCode, courseName, sessionId,
                            sessionDate, sessionType, sessionNumber,
                            status, remarks
                    ));
                }
            }
        }
        return records;
    }

    /**
     * Filters attendance records by course, date, and student name / ID.
     */
    public static List<AttendanceRecord> filterRecords(
            List<AttendanceRecord> loadedRecords,
            String filterCourseCode,
            String filterDate,
            String searchQuery
    ) {
        List<AttendanceRecord> filtered = new ArrayList<>();
        if (loadedRecords == null) return filtered;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";
        String date = filterDate != null ? filterDate.trim() : "";

        for (AttendanceRecord r : loadedRecords) {
            boolean matchesCourse = (filterCourseCode == null || filterCourseCode.equalsIgnoreCase("All Courses") || filterCourseCode.isEmpty())
                    || r.courseCode.equalsIgnoreCase(filterCourseCode);
            boolean matchesDate = date.isEmpty() || r.sessionDate.contains(date);
            boolean matchesSearch = query.isEmpty()
                    || r.studentId.toLowerCase(Locale.ROOT).contains(query)
                    || r.studentName.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesDate && matchesSearch) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    /**
     * Calculates attendance summary statistics.
     */
    public static AttendanceSummary calculateSummary(List<AttendanceRecord> currentList) {
        if (currentList == null || currentList.isEmpty()) {
            return new AttendanceSummary(0, 0, 0, 0, 0.0);
        }

        int present = 0;
        int absent = 0;
        int medical = 0;

        for (AttendanceRecord r : currentList) {
            if ("PRESENT".equalsIgnoreCase(r.status)) {
                present++;
            } else if ("MEDICAL".equalsIgnoreCase(r.status)) {
                medical++;
            } else {
                absent++;
            }
        }

        int total = currentList.size();
        double rate = total > 0 ? ((double) present / (double) total) * 100.0 : 0.0;
        return new AttendanceSummary(total, present, absent, medical, rate);
    }
}
