 package dao;

import database.DBConnection;

import java.sql.*;
import java.util.*;

/**
 * MarksGradesDAO - Data Access Object for Marks and Grades Management.
 * Handles database operations for marks, assessments, CA/Final eligibility, GPA recalculations, and metrics.
 */
public class MarksGradesDAO {

    public static class CourseOfferingItem {
        public final String offeringId;
        public final String courseCode;
        public final String courseName;
        public final String academicYear;
        public final String semester;

        public CourseOfferingItem(String offeringId, String courseCode, String courseName, String academicYear, String semester) {
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.semester = semester != null ? semester : "";
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    public static class AssessmentItem {
        public final String id;
        public final String name;
        public final String type;
        public final double weight;
        public final double maxMarks;

        public AssessmentItem(String id, String name, String type, double weight, double maxMarks) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.weight = weight;
            this.maxMarks = maxMarks;
        }
    }

    public static class StudentMarksRecord {
        public final String studentId;
        public final String studentName;
        public final String offeringId;
        public final String courseCode;
        public final String semester;
        public final Double assignmentMark;
        public final Double quizMark;
        public final Double midExamMark;
        public final Double finalExamMark;
        public final Double caMark;
        public final Double totalMark;
        public final String grade;
        public final Double gradePoint;
        public final Double attendancePercentage;
        public final Integer caEligible;
        public final Integer finalEligible;
        public final Double sgpa;
        public final Double cgpa;

        public StudentMarksRecord(
                String studentId, String studentName, String offeringId, String courseCode, String semester,
                Double assignmentMark, Double quizMark, Double midExamMark,
                Double finalExamMark, Double caMark, Double totalMark,
                String grade, Double gradePoint,
                Double attendancePercentage, Integer caEligible, Integer finalEligible,
                Double sgpa, Double cgpa
        ) {
            this.studentId = studentId != null ? studentId : "";
            this.studentName = studentName != null ? studentName : "";
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.semester = semester != null ? semester : "";
            this.assignmentMark = assignmentMark;
            this.quizMark = quizMark;
            this.midExamMark = midExamMark;
            this.finalExamMark = finalExamMark;
            this.caMark = caMark;
            this.totalMark = totalMark;
            this.grade = grade != null ? grade : "-";
            this.gradePoint = gradePoint;
            this.attendancePercentage = attendancePercentage;
            this.caEligible = caEligible;
            this.finalEligible = finalEligible;
            this.sgpa = sgpa;
            this.cgpa = cgpa;
        }

        public StudentMarksRecord(
                String studentId, String studentName, String offeringId, String courseCode,
                Double assignmentMark, Double quizMark, Double midExamMark,
                Double finalExamMark, Double caMark, Double totalMark,
                String grade, Double gradePoint,
                Double attendancePercentage, Integer caEligible, Integer finalEligible,
                Double sgpa, Double cgpa
        ) {
            this(studentId, studentName, offeringId, courseCode, "",
                 assignmentMark, quizMark, midExamMark, finalExamMark, caMark, totalMark,
                 grade, gradePoint, attendancePercentage, caEligible, finalEligible, sgpa, cgpa);
        }

        public Object[] toTableRow() {
            return new Object[]{
                    studentId,
                    studentName,
                    assignmentMark != null ? String.format(Locale.US, "%.1f", assignmentMark) : "-",
                    quizMark != null ? String.format(Locale.US, "%.1f", quizMark) : "-",
                    midExamMark != null ? String.format(Locale.US, "%.1f", midExamMark) : "-",
                    finalExamMark != null ? String.format(Locale.US, "%.1f", finalExamMark) : "-",
                    totalMark != null ? String.format(Locale.US, "%.1f", totalMark) : "-",
                    grade != null && !grade.isEmpty() ? grade : "-"
            };
        }
    }

    public static class MarksSummary {
        public final int totalStudents;
        public final String passRateFormatted;
        public final String classAverageFormatted;
        public final String pendingMarksFormatted;
        public final String highestMarksFormatted;
        public final String lowestMarksFormatted;

        public MarksSummary(int totalStudents, String passRateFormatted, String classAverageFormatted, String pendingMarksFormatted) {
            this(totalStudents, passRateFormatted, classAverageFormatted, pendingMarksFormatted, "-", "-");
        }

        public MarksSummary(int totalStudents, String passRateFormatted, String classAverageFormatted, String pendingMarksFormatted, String highestMarksFormatted, String lowestMarksFormatted) {
            this.totalStudents = totalStudents;
            this.passRateFormatted = passRateFormatted;
            this.classAverageFormatted = classAverageFormatted;
            this.pendingMarksFormatted = pendingMarksFormatted;
            this.highestMarksFormatted = highestMarksFormatted;
            this.lowestMarksFormatted = lowestMarksFormatted;
        }
    }

    /**
     * Loads assigned course offerings for lecturer.
     */
    public static List<CourseOfferingItem> loadCourseOfferings(String lecturerId) throws SQLException {
        List<CourseOfferingItem> offerings = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return offerings;
        }

        String sql =
                "SELECT DISTINCT co.offering_id, c.course_code, c.course_name, co.academic_year, co.semester " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offerings.add(new CourseOfferingItem(
                            rs.getString("offering_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("academic_year"),
                            rs.getString("semester")
                    ));
                }
            }
        }
        return offerings;
    }

    /**
     * Loads marks and calculated results from database.
     */
    public static List<StudentMarksRecord> loadMarks(String lecturerId, String selectedOfferingId) throws SQLException {
        List<StudentMarksRecord> records = new ArrayList<>();
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            return records;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.student_id, u.first_name, u.last_name, co.offering_id, c.course_code, co.semester, ")
           .append("       r.ca_mark, r.final_exam_mark, r.final_mark, r.grade, r.grade_point, ")
           .append("       r.attendance_percentage, r.ca_eligible, r.final_eligible, r.sgpa, r.cgpa, ")
           .append("       MAX(CASE WHEN UPPER(a.assessment_type) = 'ASSIGNMENT' THEN m.marks END) AS assignment_mark, ")
           .append("       MAX(CASE WHEN UPPER(a.assessment_type) = 'QUIZ' THEN m.marks END) AS quiz_mark, ")
           .append("       MAX(CASE WHEN UPPER(a.assessment_type) = 'MID_EXAM' THEN m.marks END) AS mid_exam_mark ")
           .append("FROM enrollments e ")
           .append("JOIN course_offerings co ON e.offering_id = co.offering_id ")
           .append("JOIN courses c ON co.course_id = c.course_id ")
           .append("JOIN course_lecturers cl ON co.offering_id = cl.offering_id ")
           .append("JOIN users u ON e.student_id = u.user_id ")
           .append("LEFT JOIN assessments a ON co.offering_id = a.offering_id ")
           .append("LEFT JOIN marks m ON a.assessment_id = m.assessment_id AND e.student_id = m.student_id ")
           .append("LEFT JOIN results r ON e.offering_id = r.offering_id AND e.student_id = r.student_id ")
           .append("WHERE cl.lecturer_id = ? ");

        if (selectedOfferingId != null && !selectedOfferingId.trim().isEmpty() && !"All Courses".equalsIgnoreCase(selectedOfferingId)) {
            sql.append("AND co.offering_id = ? ");
        }

        sql.append("GROUP BY e.student_id, u.first_name, u.last_name, co.offering_id, c.course_code, co.semester, ")
           .append("         r.ca_mark, r.final_exam_mark, r.final_mark, r.grade, r.grade_point, ")
           .append("         r.attendance_percentage, r.ca_eligible, r.final_eligible, r.sgpa, r.cgpa ")
           .append("ORDER BY c.course_code ASC, e.student_id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, lecturerId.trim());
            if (selectedOfferingId != null && !selectedOfferingId.trim().isEmpty() && !"All Courses".equalsIgnoreCase(selectedOfferingId)) {
                ps.setString(2, selectedOfferingId.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("student_id");
                    String fn = rs.getString("first_name");
                    String ln = rs.getString("last_name");
                    String sname = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
                    if (sname.isEmpty()) sname = sid;

                    String offId = rs.getString("offering_id");
                    String ccode = rs.getString("course_code");
                    String sem = rs.getString("semester");

                    Double assMark = getDoubleOrNull(rs, "assignment_mark");
                    Double qzMark = getDoubleOrNull(rs, "quiz_mark");
                    Double midMark = getDoubleOrNull(rs, "mid_exam_mark");
                    Double feMark = getDoubleOrNull(rs, "final_exam_mark");
                    Double caMark = getDoubleOrNull(rs, "ca_mark");
                    Double totMark = getDoubleOrNull(rs, "final_mark");
                    String grade = rs.getString("grade");
                    Double gp = getDoubleOrNull(rs, "grade_point");
                    Double attPct = getDoubleOrNull(rs, "attendance_percentage");
                    int caEl = rs.getInt("ca_eligible");
                    Integer caEligible = rs.wasNull() ? null : caEl;
                    int feEl = rs.getInt("final_eligible");
                    Integer finalEligible = rs.wasNull() ? null : feEl;
                    Double sgpa = getDoubleOrNull(rs, "sgpa");
                    Double cgpa = getDoubleOrNull(rs, "cgpa");

                    records.add(new StudentMarksRecord(
                            sid, sname, offId, ccode, sem,
                            assMark, qzMark, midMark, feMark,
                            caMark, totMark, grade, gp,
                            attPct, caEligible, finalEligible,
                            sgpa, cgpa
                    ));
                }
            }
        }
        return records;
    }

    /**
     * Loads assessments for a given course offering.
     */
    public static List<AssessmentItem> loadAssessmentsForOffering(String offeringId) throws SQLException {
        List<AssessmentItem> list = new ArrayList<>();
        String sql = "SELECT assessment_id, assessment_name, assessment_type, weight_percentage, max_marks " +
                     "FROM assessments WHERE offering_id = ? ORDER BY assessment_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AssessmentItem(
                            rs.getString("assessment_id"),
                            rs.getString("assessment_name"),
                            rs.getString("assessment_type"),
                            rs.getDouble("weight_percentage"),
                            rs.getDouble("max_marks")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Loads enrolled students for an offering.
     */
    public static Map<String, String> loadEnrolledStudents(String offeringId) throws SQLException {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT e.student_id, u.first_name, u.last_name FROM enrollments e " +
                     "JOIN users u ON e.student_id = u.user_id WHERE e.offering_id = ? ORDER BY e.student_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("student_id");
                    String fn = rs.getString("first_name");
                    String ln = rs.getString("last_name");
                    String name = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
                    map.put(sid, name.isEmpty() ? sid : name);
                }
            }
        }
        return map;
    }

    /**
     * Loads existing assessment marks for a specific student in an offering.
     */
    public static Map<String, Double> loadStudentAssessmentMarks(String offeringId, String studentId) throws SQLException {
        Map<String, Double> map = new LinkedHashMap<>();
        if (offeringId == null || studentId == null) return map;

        String sql = "SELECT a.assessment_id, m.marks FROM assessments a " +
                     "LEFT JOIN marks m ON a.assessment_id = m.assessment_id AND m.student_id = ? " +
                     "WHERE a.offering_id = ? " +
                     "ORDER BY a.assessment_type, a.assessment_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            ps.setString(2, offeringId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double val = rs.getDouble("marks");
                    if (!rs.wasNull()) {
                        map.put(rs.getString("assessment_id"), val);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Saves marks and synchronizes student results in the database.
     */
    public static void saveMarksAndResults(
            String offeringId, String studentId,
            Map<AssessmentItem, Double> marksMap,
            List<AssessmentItem> allAssessments,
            String lecturerId
    ) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Persist each assessment mark entered (Update existing or insert new)
                for (Map.Entry<AssessmentItem, Double> entry : marksMap.entrySet()) {
                    AssessmentItem a = entry.getKey();
                    double markVal = entry.getValue();

                    String checkSql = "SELECT mark_id FROM marks WHERE assessment_id = ? AND student_id = ?";
                    String existingMarkId = null;
                    try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                        psCheck.setString(1, a.id);
                        psCheck.setString(2, studentId);
                        try (ResultSet rsCheck = psCheck.executeQuery()) {
                            if (rsCheck.next()) {
                                existingMarkId = rsCheck.getString(1);
                            }
                        }
                    }

                    if (existingMarkId != null) {
                        String updateSql = "UPDATE marks SET marks = ?, entered_by = ?, entered_at = NOW() WHERE mark_id = ?";
                        try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                            psUp.setDouble(1, markVal);
                            psUp.setString(2, lecturerId);
                            psUp.setString(3, existingMarkId);
                            psUp.executeUpdate();
                        }
                    } else {
                        String nextMarkId = getNextId(conn, "marks", "mark_id", "MRK", 4);
                        String insertSql = "INSERT INTO marks (mark_id, assessment_id, student_id, marks, entered_by, entered_at) VALUES (?, ?, ?, ?, ?, NOW())";
                        try (PreparedStatement psIn = conn.prepareStatement(insertSql)) {
                            psIn.setString(1, nextMarkId);
                            psIn.setString(2, a.id);
                            psIn.setString(3, studentId);
                            psIn.setDouble(4, markVal);
                            psIn.setString(5, lecturerId);
                            psIn.executeUpdate();
                        }
                    }
                }

                // 2. Query attendance data
                int totalSessions = 0;
                String sqlTotalSessions = "SELECT COUNT(*) FROM attendance_sessions WHERE offering_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlTotalSessions)) {
                    ps.setString(1, offeringId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) totalSessions = rs.getInt(1);
                    }
                }

                int attendedSessions = 0;
                String sqlAttended = "SELECT COUNT(*) FROM attendance a " +
                        "JOIN attendance_sessions s ON a.session_id = s.session_id " +
                        "WHERE s.offering_id = ? AND a.student_id = ? " +
                        "  AND (UPPER(a.status) = 'PRESENT' OR UPPER(a.status) = 'MEDICAL')";
                try (PreparedStatement ps = conn.prepareStatement(sqlAttended)) {
                    ps.setString(1, offeringId);
                    ps.setString(2, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) attendedSessions = rs.getInt(1);
                    }
                }

                Double attendancePercentage = null;
                boolean attendanceEligible = false;
                if (totalSessions > 0) {
                    attendancePercentage = Math.round((((double) attendedSessions / totalSessions) * 100.0) * 100.0) / 100.0;
                    attendanceEligible = (attendancePercentage >= 80.0);
                } else {
                    // Default to eligible if sessions are not tracked
                    attendanceEligible = true;
                }

                // 3. Query all current marks
                Map<String, Double> currentStudentMarks = new LinkedHashMap<>();
                String queryCurrent = "SELECT m.assessment_id, m.marks FROM marks m " +
                        "JOIN assessments a ON m.assessment_id = a.assessment_id " +
                        "WHERE a.offering_id = ? AND m.student_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(queryCurrent)) {
                    ps.setString(1, offeringId);
                    ps.setString(2, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            currentStudentMarks.put(rs.getString("assessment_id"), rs.getDouble("marks"));
                        }
                    }
                }

                // Ensure newly entered marks take precedence
                for (Map.Entry<AssessmentItem, Double> entry : marksMap.entrySet()) {
                    currentStudentMarks.put(entry.getKey().id, entry.getValue());
                }

                // Compute CA mark and final mark
                double caMark = 0.0;
                double totalCaWeight = 0.0;
                boolean hasAnyCaMark = false;
                Double finalExamMark = null;
                double finalExamWeight = 0.0;
                double finalExamMax = 100.0;

                for (AssessmentItem a : allAssessments) {
                    Double score = currentStudentMarks.get(a.id);
                    String aType = a.type != null ? a.type.toUpperCase(Locale.ROOT) : "";
                    String aName = a.name != null ? a.name.toUpperCase(Locale.ROOT) : "";

                    if (aType.contains("FINAL") || aName.contains("FINAL")) {
                        finalExamWeight = a.weight > 0 ? a.weight : 45.0;
                        finalExamMax = a.maxMarks > 0 ? a.maxMarks : 100.0;
                        if (score != null) finalExamMark = score;
                    } else {
                        totalCaWeight += a.weight;
                        if (score != null) {
                            hasAnyCaMark = true;
                            double maxM = a.maxMarks > 0 ? a.maxMarks : 100.0;
                            caMark += (score / maxM) * a.weight;
                        }
                    }
                }

                // Fallback check for existing final_exam_mark if not in assessments
                if (finalExamMark == null) {
                    String sqlFe = "SELECT final_exam_mark FROM results WHERE student_id = ? AND offering_id = ?";
                    try (PreparedStatement psFe = conn.prepareStatement(sqlFe)) {
                        psFe.setString(1, studentId);
                        psFe.setString(2, offeringId);
                        try (ResultSet rsFe = psFe.executeQuery()) {
                            if (rsFe.next()) {
                                double d = rsFe.getDouble(1);
                                if (!rsFe.wasNull()) finalExamMark = d;
                            }
                        }
                    }
                }

                boolean caEligible = false;
                if (totalCaWeight > 0 && hasAnyCaMark) {
                    double caPercentage = Math.round(((caMark / totalCaWeight) * 100.0) * 100.0) / 100.0;
                    caEligible = (caPercentage >= 40.0);
                } else if (!hasAnyCaMark) {
                    caEligible = true;
                }

                boolean finalEligible = attendanceEligible && caEligible;

                Double totalFinalMark = null;
                double maxCaAllowed = 100.0 - (finalExamWeight > 0 ? finalExamWeight : 55.0);
                double effectiveCaMark = caMark;
                if (totalCaWeight > maxCaAllowed && totalCaWeight > 0) {
                    effectiveCaMark = (caMark / totalCaWeight) * maxCaAllowed;
                }

                if (finalExamMark != null) {
                    double feWeight = (finalExamWeight > 0) ? finalExamWeight : 55.0;
                    double feContrib = (finalExamMark / finalExamMax) * feWeight;
                    totalFinalMark = Math.round((effectiveCaMark + feContrib) * 100.0) / 100.0;
                } else if (hasAnyCaMark) {
                    totalFinalMark = Math.round(effectiveCaMark * 100.0) / 100.0;
                }

                if (totalFinalMark != null) {
                    totalFinalMark = Math.min(100.0, Math.max(0.0, totalFinalMark));
                }
                double finalSavedCaMark = Math.min(100.0, Math.max(0.0, Math.round(effectiveCaMark * 100.0) / 100.0));
                Double finalSavedFeMark = finalExamMark != null ? Math.min(100.0, Math.max(0.0, finalExamMark)) : null;

                String grade;
                double gradePoint;
                if (!finalEligible && hasAnyCaMark) {
                    grade = "F";
                    gradePoint = 0.0;
                } else {
                    if (totalFinalMark != null) {
                        grade = computeGrade(totalFinalMark);
                        gradePoint = computeGradePoint(grade);
                    } else {
                        grade = "-";
                        gradePoint = 0.0;
                    }
                }

                // 4. Upsert results table (Update existing or insert new)
                String checkResSql = "SELECT result_id FROM results WHERE student_id = ? AND offering_id = ?";
                String existingResultId = null;
                try (PreparedStatement psCheck = conn.prepareStatement(checkResSql)) {
                    psCheck.setString(1, studentId);
                    psCheck.setString(2, offeringId);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next()) {
                            existingResultId = rsCheck.getString(1);
                        }
                    }
                }

                if (existingResultId != null) {
                    String updateResultSql =
                            "UPDATE results SET ca_mark = ?, final_exam_mark = ?, final_mark = ?, " +
                            "grade = ?, grade_point = ?, attendance_percentage = ?, ca_eligible = ?, final_eligible = ? " +
                            "WHERE result_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateResultSql)) {
                        ps.setDouble(1, finalSavedCaMark);
                        if (finalSavedFeMark != null) ps.setDouble(2, finalSavedFeMark);
                        else ps.setNull(2, Types.DECIMAL);

                        if (totalFinalMark != null) ps.setDouble(3, totalFinalMark);
                        else ps.setNull(3, Types.DECIMAL);

                        if (grade != null && !"-".equals(grade)) ps.setString(4, grade);
                        else ps.setNull(4, Types.VARCHAR);

                        ps.setDouble(5, gradePoint);
                        if (attendancePercentage != null) ps.setDouble(6, attendancePercentage);
                        else ps.setNull(6, Types.DECIMAL);

                        ps.setInt(7, caEligible ? 1 : 0);
                        ps.setInt(8, finalEligible ? 1 : 0);
                        ps.setString(9, existingResultId);
                        ps.executeUpdate();
                    }
                } else {
                    String nextResultId = getNextId(conn, "results", "result_id", "RES", 4);
                    String insertResultSql =
                            "INSERT INTO results (result_id, student_id, offering_id, ca_mark, final_exam_mark, final_mark, grade, grade_point, attendance_percentage, ca_eligible, final_eligible) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertResultSql)) {
                        ps.setString(1, nextResultId);
                        ps.setString(2, studentId);
                        ps.setString(3, offeringId);
                        ps.setDouble(4, finalSavedCaMark);
                        if (finalSavedFeMark != null) ps.setDouble(5, finalSavedFeMark);
                        else ps.setNull(5, Types.DECIMAL);

                        if (totalFinalMark != null) ps.setDouble(6, totalFinalMark);
                        else ps.setNull(6, Types.DECIMAL);

                        if (grade != null && !"-".equals(grade)) ps.setString(7, grade);
                        else ps.setNull(7, Types.VARCHAR);

                        ps.setDouble(8, gradePoint);
                        if (attendancePercentage != null) ps.setDouble(9, attendancePercentage);
                        else ps.setNull(9, Types.DECIMAL);

                        ps.setInt(10, caEligible ? 1 : 0);
                        ps.setInt(11, finalEligible ? 1 : 0);
                        ps.executeUpdate();
                    }
                }

                // 5. Update GPA for student
                updateGpaForStudent(conn, studentId);

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static String computeGrade(double mark) {
        if (mark >= 85.0) return "A+";
        if (mark >= 75.0) return "A";
        if (mark >= 70.0) return "A-";
        if (mark >= 65.0) return "B+";
        if (mark >= 60.0) return "B";
        if (mark >= 55.0) return "B-";
        if (mark >= 50.0) return "C+";
        if (mark >= 45.0) return "C";
        if (mark >= 40.0) return "C-";
        if (mark >= 30.0) return "D";
        return "F";
    }

    public static double computeGradePoint(String grade) {
        if (grade == null) return 0.0;
        switch (grade) {
            case "A+":
            case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.5;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D": return 1.0;
            default: return 0.0;
        }
    }

    public static void updateGpaForStudent(Connection conn, String studentId) throws SQLException {
        if (studentId == null || studentId.trim().isEmpty()) return;

        // Recalculate SGPA
        String sqlSgpa =
                "SELECT co.academic_year, co.semester, " +
                "       SUM(c.credits * r.grade_point) AS total_qp, " +
                "       SUM(c.credits) AS total_credits " +
                "FROM results r " +
                "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE r.student_id = ? AND r.grade_point IS NOT NULL " +
                "GROUP BY co.academic_year, co.semester";

        Map<String, Double> semGpas = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sqlSgpa)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ay = rs.getString("academic_year");
                    String sem = rs.getString("semester");
                    double qp = rs.getDouble("total_qp");
                    double cr = rs.getDouble("total_credits");
                    double sgpa = (cr > 0) ? Math.round((qp / cr) * 100.0) / 100.0 : 0.0;
                    semGpas.put(ay + "##" + sem, sgpa);
                }
            }
        }

        String sqlUpdateSgpa =
                "UPDATE results r " +
                "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                "SET r.sgpa = ? " +
                "WHERE r.student_id = ? AND co.academic_year = ? AND co.semester = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSgpa)) {
            for (Map.Entry<String, Double> entry : semGpas.entrySet()) {
                String[] parts = entry.getKey().split("##", 2);
                ps.setDouble(1, entry.getValue());
                ps.setString(2, studentId);
                ps.setString(3, parts[0]);
                ps.setString(4, parts[1]);
                ps.executeUpdate();
            }
        }

        // Recalculate CGPA
        String sqlCgpa =
                "SELECT SUM(c.credits * r.grade_point) AS total_qp, " +
                "       SUM(c.credits) AS total_credits " +
                "FROM results r " +
                "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE r.student_id = ? AND r.grade_point IS NOT NULL";

        Double cgpa = null;
        try (PreparedStatement ps = conn.prepareStatement(sqlCgpa)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double cr = rs.getDouble("total_credits");
                    if (!rs.wasNull() && cr > 0) {
                        double qp = rs.getDouble("total_qp");
                        cgpa = Math.round((qp / cr) * 100.0) / 100.0;
                    }
                }
            }
        }

        if (cgpa != null) {
            String sqlUpdateCgpa = "UPDATE results SET cgpa = ? WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateCgpa)) {
                ps.setDouble(1, cgpa);
                ps.setString(2, studentId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Filters student marks records by course, semester, status, and search query.
     */
    public static List<StudentMarksRecord> filterMarks(
            List<StudentMarksRecord> loadedRecords,
            String filterCourseCode,
            String filterSemester,
            String filterStatus,
            String searchQuery
    ) {
        List<StudentMarksRecord> list = new ArrayList<>();
        if (loadedRecords == null) return list;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";

        for (StudentMarksRecord r : loadedRecords) {
            boolean matchesCourse = (filterCourseCode == null || filterCourseCode.equals("All Courses") || filterCourseCode.isEmpty())
                    || r.courseCode.equalsIgnoreCase(filterCourseCode);

            boolean matchesSemester = true;
            if (filterSemester != null && !filterSemester.equals("All Semesters") && !filterSemester.isEmpty()) {
                if (filterSemester.contains("1")) {
                    matchesSemester = r.semester != null && (r.semester.equalsIgnoreCase("Semester 1") || r.semester.equalsIgnoreCase("SEM 1") || r.semester.contains("1"));
                } else if (filterSemester.contains("2")) {
                    matchesSemester = r.semester != null && (r.semester.equalsIgnoreCase("Semester 2") || r.semester.equalsIgnoreCase("SEM 2") || r.semester.contains("2"));
                } else {
                    matchesSemester = r.semester != null && r.semester.equalsIgnoreCase(filterSemester);
                }
            }

            boolean matchesStatus = true;
            if (filterStatus != null && !filterStatus.equals("All Statuses") && !filterStatus.isEmpty()) {
                if ("Submitted".equalsIgnoreCase(filterStatus)) {
                    matchesStatus = r.totalMark != null;
                } else if ("Pending".equalsIgnoreCase(filterStatus)) {
                    matchesStatus = r.totalMark == null;
                } else if ("Pass".equalsIgnoreCase(filterStatus)) {
                    matchesStatus = r.grade != null && !"F".equalsIgnoreCase(r.grade) && !"-".equals(r.grade);
                } else if ("Fail".equalsIgnoreCase(filterStatus)) {
                    matchesStatus = "F".equalsIgnoreCase(r.grade);
                }
            }

            boolean matchesSearch = query.isEmpty()
                    || r.studentId.toLowerCase(Locale.ROOT).contains(query)
                    || r.studentName.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesSemester && matchesStatus && matchesSearch) {
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Backward-compatible filter method.
     */
    public static List<StudentMarksRecord> filterMarks(
            List<StudentMarksRecord> loadedRecords,
            String filterCourseCode,
            String filterStatus,
            String searchQuery
    ) {
        return filterMarks(loadedRecords, filterCourseCode, null, filterStatus, searchQuery);
    }

    /**
     * Calculates summary metrics for Marks & Grades (Total Students, Average Marks, Highest Marks, Lowest Marks).
     */
    public static MarksSummary calculateSummary(List<StudentMarksRecord> list) {
        if (list == null || list.isEmpty()) {
            return new MarksSummary(0, "-", "-", "0", "-", "-");
        }

        int total = list.size();
        int passCount = 0;
        int withFinalMarks = 0;
        double sumTotalMarks = 0.0;
        int pending = 0;
        double maxMark = Double.NEGATIVE_INFINITY;
        double minMark = Double.POSITIVE_INFINITY;
        boolean hasScores = false;

        for (StudentMarksRecord r : list) {
            if (r.totalMark != null) {
                sumTotalMarks += r.totalMark;
                withFinalMarks++;
                if (r.totalMark > maxMark) maxMark = r.totalMark;
                if (r.totalMark < minMark) minMark = r.totalMark;
                hasScores = true;
                if (r.grade != null && !"F".equalsIgnoreCase(r.grade) && !"-".equals(r.grade)) {
                    passCount++;
                }
            } else {
                pending++;
            }
        }

        String passRateStr = withFinalMarks > 0
                ? String.format(Locale.US, "%.1f%%", ((double) passCount / withFinalMarks) * 100.0)
                : "-";

        String avgStr = withFinalMarks > 0
                ? String.format(Locale.US, "%.1f", sumTotalMarks / withFinalMarks)
                : "-";

        String highestStr = hasScores
                ? String.format(Locale.US, "%.1f", maxMark)
                : "-";

        String lowestStr = hasScores
                ? String.format(Locale.US, "%.1f", minMark)
                : "-";

        return new MarksSummary(total, passRateStr, avgStr, String.valueOf(pending), highestStr, lowestStr);
    }

    /**
     * Updates student marks directly from table inline row editing.
     */
    public static void updateStudentRowMarks(
            String offeringId, String studentId,
            Double assignmentMark, Double quizMark, Double midExamMark, Double finalExamMark,
            String lecturerId
    ) throws SQLException {
        List<AssessmentItem> assessments = loadAssessmentsForOffering(offeringId);
        Map<AssessmentItem, Double> marksMap = new LinkedHashMap<>();

        for (AssessmentItem a : assessments) {
            String type = a.type != null ? a.type.toUpperCase(Locale.ROOT) : "";
            String name = a.name != null ? a.name.toUpperCase(Locale.ROOT) : "";

            if ((type.contains("ASSIGNMENT") || name.contains("ASSIGNMENT")) && assignmentMark != null) {
                marksMap.put(a, assignmentMark);
            } else if ((type.contains("QUIZ") || name.contains("QUIZ")) && quizMark != null) {
                marksMap.put(a, quizMark);
            } else if ((type.contains("MID") || name.contains("MID")) && midExamMark != null) {
                marksMap.put(a, midExamMark);
            } else if ((type.contains("FINAL") || type.contains("EXAM") || name.contains("FINAL") || name.contains("EXAM")) && finalExamMark != null) {
                marksMap.put(a, finalExamMark);
            }
        }

        saveMarksAndResults(offeringId, studentId, marksMap, assessments, lecturerId);
    }

    private static Double getDoubleOrNull(ResultSet rs, String col) throws SQLException {
        double d = rs.getDouble(col);
        return rs.wasNull() ? null : d;
    }

    private static String getNextId(Connection conn, String table, String col, String prefix, int digits) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(" + col + ", " + (prefix.length() + 1) + ") AS UNSIGNED)), 0) + 1 FROM " + table;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                long nextNum = rs.getLong(1);
                return String.format(Locale.US, prefix + "%0" + digits + "d", nextNum);
            }
        }
        return prefix + "0001";
    }
}

