package dao;

import database.DBConnection;
import model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    /**
     * Retrieves student profile including undergraduate academic metadata and calculated dashboard metrics.
     */
    public Student getStudentProfile(String studentId) {
        String sql = "SELECT u.*, un.index_no, un.batch, un.academic_year, un.department, un.student_status " +
                     "FROM users u " +
                     "LEFT JOIN undergraduates un ON u.user_id = un.student_id " +
                     "WHERE u.user_id = ?";

        Student student = null;
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        student = new Student();
                        student.setUserId(rs.getString("user_id"));
                        student.setUsername(rs.getString("username"));
                        student.setRole(rs.getString("role"));
                        student.setFirstName(rs.getString("first_name"));
                        student.setLastName(rs.getString("last_name"));
                        student.setEmail(rs.getString("email"));
                        student.setPhone(rs.getString("phone"));
                        student.setAddress(rs.getString("address"));
                        student.setProfilePicture(rs.getString("profile_picture"));
                        student.setStatus(rs.getString("status"));
                        student.setCreatedAt(rs.getTimestamp("created_at"));

                        student.setIndexNo(rs.getString("index_no") != null ? rs.getString("index_no") : "N/A");
                        student.setBatch(rs.getString("batch") != null ? rs.getString("batch") : "N/A");
                        student.setAcademicYear(rs.getString("academic_year") != null ? rs.getString("academic_year") : "N/A");
                        student.setDepartment(rs.getString("department") != null ? rs.getString("department") : "Faculty of Technology");
                        student.setStudentStatus(rs.getString("student_status") != null ? rs.getString("student_status") : "REGULAR");
                    }
                }
            }

            if (student != null) {
                // Populate dashboard metrics
                student.setEnrolledCourseCount(countEnrolledCourses(conn, studentId));
                student.setAttendancePercentage(calculateAttendanceRate(conn, studentId));
                student.setPendingAssignmentCount(countAssignments(conn, studentId));
                double gpa = calculateGPA(conn, studentId);
                student.setCurrentGpa(gpa);
                student.setCurrentCgpa(gpa);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student profile: " + e.getMessage());
        }

        return student;
    }

    private int countEnrolledCourses(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private double calculateAttendanceRate(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) as total, " +
                     "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as presents " +
                     "FROM attendance WHERE student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int presents = rs.getInt("presents");
                    if (total > 0) {
                        return Math.round(((double) presents / total * 100.0) * 10.0) / 10.0;
                    }
                }
            }
        }
        return 0.0;
    }

    private int countAssignments(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM assessments a " +
                     "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                     "JOIN enrollments e ON co.offering_id = e.offering_id " +
                     "WHERE e.student_id = ? AND a.assessment_type IN ('ASSIGNMENT', 'QUIZ')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private double calculateGPA(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT r.grade_point, c.credits FROM results r " +
                     "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE r.student_id = ? AND r.grade_point IS NOT NULL";

        double totalPoints = 0.0;
        int totalCredits = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal gp = rs.getBigDecimal("grade_point");
                    int credits = rs.getInt("credits");
                    if (gp != null && credits > 0) {
                        totalPoints += gp.doubleValue() * credits;
                        totalCredits += credits;
                    }
                }
            }
        }

        if (totalCredits > 0) {
            BigDecimal gpa = BigDecimal.valueOf(totalPoints / totalCredits).setScale(2, RoundingMode.HALF_UP);
            return gpa.doubleValue();
        }
        return 0.00;
    }

    /**
     * Retrieves all courses enrolled by the student.
     */
    public List<Course> getEnrolledCourses(String studentId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_code, c.course_name, c.credits, c.department, c.description, " +
                     "co.offering_id, co.academic_year, co.semester, co.batch, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                     "FROM enrollments e " +
                     "JOIN course_offerings co ON e.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "LEFT JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                     "LEFT JOIN users u ON cl.lecturer_id = u.user_id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY c.course_code";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Course course = new Course();
                        course.setCourseId(rs.getString("course_id"));
                        course.setOfferingId(rs.getString("offering_id"));
                        course.setCourseCode(rs.getString("course_code"));
                        course.setCourseName(rs.getString("course_name"));
                        course.setCredits(rs.getInt("credits"));
                        course.setDepartment(rs.getString("department"));
                        course.setDescription(rs.getString("description"));
                        course.setAcademicYear(rs.getString("academic_year"));
                        course.setSemester(rs.getString("semester"));
                        course.setBatch(rs.getString("batch"));
                        course.setLecturerName(rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Assigned Lecturer");
                        list.add(course);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrolled courses: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves results for the student.
     */
    public List<Result> getStudentResults(String studentId) {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT r.*, c.course_code, c.course_name, c.credits " +
                     "FROM results r " +
                     "JOIN course_offerings co ON r.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE r.student_id = ? " +
                     "ORDER BY c.course_code";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Result result = new Result();
                        result.setResultId(rs.getString("result_id"));
                        result.setStudentId(rs.getString("student_id"));
                        result.setOfferingId(rs.getString("offering_id"));
                        result.setCourseCode(rs.getString("course_code"));
                        result.setCourseName(rs.getString("course_name"));
                        result.setCredits(rs.getInt("credits"));
                        result.setCaMark(rs.getBigDecimal("ca_mark"));
                        result.setFinalExamMark(rs.getBigDecimal("final_exam_mark"));
                        result.setFinalMark(rs.getBigDecimal("final_mark"));
                        result.setGrade(rs.getString("grade"));
                        result.setGradePoint(rs.getBigDecimal("grade_point"));
                        result.setAttendancePercentage(rs.getBigDecimal("attendance_percentage"));
                        result.setCaEligible(rs.getInt("ca_eligible") == 1);
                        result.setFinalEligible(rs.getInt("final_eligible") == 1);
                        result.setSgpa(rs.getBigDecimal("sgpa"));
                        result.setCgpa(rs.getBigDecimal("cgpa"));
                        list.add(result);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student results: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves detailed session attendance records for the student.
     */
    public List<AttendanceRecord> getAttendanceRecords(String studentId) {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT att.attendance_id, att.session_id, att.student_id, att.status, att.remarks, " +
                     "s.offering_id, s.session_date, s.session_type, s.session_number, s.topic, " +
                     "c.course_code, c.course_name " +
                     "FROM attendance att " +
                     "JOIN attendance_sessions s ON att.session_id = s.session_id " +
                     "JOIN course_offerings co ON s.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE att.student_id = ? " +
                     "ORDER BY s.session_date DESC, s.session_number DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        AttendanceRecord rec = new AttendanceRecord();
                        rec.setAttendanceId(rs.getString("attendance_id"));
                        rec.setSessionId(rs.getString("session_id"));
                        rec.setStudentId(rs.getString("student_id"));
                        rec.setOfferingId(rs.getString("offering_id"));
                        rec.setStatus(rs.getString("status"));
                        rec.setRemarks(rs.getString("remarks"));
                        rec.setSessionDate(rs.getDate("session_date"));
                        rec.setSessionType(rs.getString("session_type"));
                        rec.setSessionNumber(rs.getInt("session_number"));
                        rec.setTopic(rs.getString("topic"));
                        rec.setCourseCode(rs.getString("course_code"));
                        rec.setCourseName(rs.getString("course_name"));
                        list.add(rec);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves lectures/attendance sessions where the student is recorded as ABSENT (lacking attendance).
     */
    public List<AttendanceRecord> getAbsentSessions(String studentId) {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT att.attendance_id, att.session_id, att.student_id, att.status, att.remarks, " +
                     "s.offering_id, s.session_date, s.session_type, s.session_number, s.topic, " +
                     "c.course_code, c.course_name, " +
                     "(SELECT m.approval_status FROM medicals m WHERE m.student_id = att.student_id AND m.offering_id = s.offering_id AND (m.medical_date = s.session_date OR (m.start_date <= s.session_date AND m.end_date >= s.session_date)) LIMIT 1) as medical_status, " +
                     "(SELECT m.medical_id FROM medicals m WHERE m.student_id = att.student_id AND m.offering_id = s.offering_id AND (m.medical_date = s.session_date OR (m.start_date <= s.session_date AND m.end_date >= s.session_date)) LIMIT 1) as linked_medical_id " +
                     "FROM attendance att " +
                     "JOIN attendance_sessions s ON att.session_id = s.session_id " +
                     "JOIN course_offerings co ON s.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE att.student_id = ? AND att.status = 'ABSENT' " +
                     "ORDER BY s.session_date DESC, s.session_number DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        AttendanceRecord rec = new AttendanceRecord();
                        rec.setAttendanceId(rs.getString("attendance_id"));
                        rec.setSessionId(rs.getString("session_id"));
                        rec.setStudentId(rs.getString("student_id"));
                        rec.setOfferingId(rs.getString("offering_id"));
                        rec.setStatus(rs.getString("status"));
                        rec.setRemarks(rs.getString("remarks"));
                        rec.setSessionDate(rs.getDate("session_date"));
                        rec.setSessionType(rs.getString("session_type"));
                        rec.setSessionNumber(rs.getInt("session_number"));
                        rec.setTopic(rs.getString("topic"));
                        rec.setCourseCode(rs.getString("course_code"));
                        rec.setCourseName(rs.getString("course_name"));
                        rec.setMedicalStatus(rs.getString("medical_status"));
                        rec.setMedicalId(rs.getString("linked_medical_id"));
                        list.add(rec);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching absent sessions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves assessments and assignments for student's enrolled courses.
     */
    public List<Assessment> getAssessments(String studentId) {
        List<Assessment> list = new ArrayList<>();
        String sql = "SELECT a.*, c.course_code, c.course_name " +
                     "FROM assessments a " +
                     "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                     "JOIN enrollments e ON co.offering_id = e.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY a.assessment_date ASC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Assessment asm = new Assessment();
                        asm.setAssessmentId(rs.getString("assessment_id"));
                        asm.setOfferingId(rs.getString("offering_id"));
                        asm.setCourseCode(rs.getString("course_code"));
                        asm.setCourseName(rs.getString("course_name"));
                        asm.setAssessmentName(rs.getString("assessment_name"));
                        asm.setAssessmentType(rs.getString("assessment_type"));
                        asm.setWeightPercentage(rs.getBigDecimal("weight_percentage"));
                        asm.setMaxMarks(rs.getBigDecimal("max_marks"));
                        asm.setAssessmentDate(rs.getDate("assessment_date"));
                        asm.setCreatedBy(rs.getString("created_by"));
                        list.add(asm);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assessments: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves materials uploaded for student's enrolled courses.
     */
    public List<CourseMaterial> getCourseMaterials(String studentId) {
        List<CourseMaterial> list = new ArrayList<>();
        String sql = "SELECT cm.*, c.course_code, c.course_name, CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                     "FROM course_materials cm " +
                     "JOIN course_offerings co ON cm.offering_id = co.offering_id " +
                     "JOIN enrollments e ON co.offering_id = e.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "LEFT JOIN users u ON cm.lecturer_id = u.user_id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY cm.uploaded_at DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        CourseMaterial mat = new CourseMaterial();
                        mat.setMaterialId(rs.getString("material_id"));
                        mat.setOfferingId(rs.getString("offering_id"));
                        mat.setLecturerId(rs.getString("lecturer_id"));
                        mat.setLecturerName(rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Lecturer");
                        mat.setCourseCode(rs.getString("course_code"));
                        mat.setCourseName(rs.getString("course_name"));
                        mat.setTitle(rs.getString("title"));
                        mat.setDescription(rs.getString("description"));
                        mat.setFileName(rs.getString("file_name"));
                        mat.setFilePath(rs.getString("file_path"));
                        mat.setUploadedAt(rs.getTimestamp("uploaded_at"));
                        list.add(mat);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course materials: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves active system notices / faculty announcements.
     */
    public List<Notice> getActiveNotices() {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.*, CONCAT(u.first_name, ' ', u.last_name) as author_name " +
                     "FROM notices n " +
                     "LEFT JOIN users u ON n.created_by = u.user_id " +
                     "WHERE n.status = 'ACTIVE' " +
                     "ORDER BY n.notice_date DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Notice notice = new Notice();
                    notice.setNoticeId(rs.getString("notice_id"));
                    notice.setTitle(rs.getString("title"));
                    notice.setDescription(rs.getString("description"));
                    notice.setNoticeDate(rs.getDate("notice_date"));
                    notice.setCreatedBy(rs.getString("created_by"));
                    notice.setAuthorName(rs.getString("author_name") != null ? rs.getString("author_name") : "Administration");
                    notice.setStatus(rs.getString("status"));
                    list.add(notice);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notices: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates student profile picture path in users table.
     */
    public boolean updateProfilePicture(String userId, String picturePath) {
        String sql = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, picturePath);
                stmt.setString(2, userId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating profile picture: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all medical certificate submissions for a student.
     */
    public List<Medical> getStudentMedicals(String studentId) {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT m.*, c.course_code, c.course_name " +
                     "FROM medicals m " +
                     "JOIN course_offerings co ON m.offering_id = co.offering_id " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "WHERE m.student_id = ? " +
                     "ORDER BY m.medical_date DESC, m.medical_id DESC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Medical med = new Medical();
                        med.setMedicalId(rs.getString("medical_id"));
                        med.setStudentId(rs.getString("student_id"));
                        med.setOfferingId(rs.getString("offering_id"));
                        med.setCourseCode(rs.getString("course_code"));
                        med.setCourseName(rs.getString("course_name"));
                        med.setMedicalDate(rs.getDate("medical_date"));
                        try {
                            med.setStartDate(rs.getDate("start_date"));
                            med.setEndDate(rs.getDate("end_date"));
                        } catch (Exception ignored) {}
                        med.setReason(rs.getString("reason"));
                        med.setDocumentPath(rs.getString("document_path"));
                        med.setApprovalStatus(rs.getString("approval_status"));
                        med.setApprovedBy(rs.getString("approved_by"));
                        med.setRemarks(rs.getString("remarks"));
                        list.add(med);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medical submissions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Submits a new medical certificate record with support for specific days taken off.
     */
    public boolean submitMedical(Medical medical) {
        String nextIdQuery = "SELECT MAX(CAST(SUBSTRING(medical_id, 4) AS UNSIGNED)) FROM medicals";
        String insertSql = "INSERT INTO medicals (medical_id, student_id, offering_id, medical_date, start_date, end_date, reason, document_path, approval_status, remarks) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?)";

        try {
            Connection conn = DBConnection.getConnection();

            int nextNum = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(nextIdQuery)) {
                if (rs.next()) {
                    nextNum = rs.getInt(1) + 1;
                }
            }
            String newMedicalId = String.format("MED%03d", nextNum);
            medical.setMedicalId(newMedicalId);

            java.sql.Date sDate = medical.getStartDate() != null ? medical.getStartDate() : medical.getMedicalDate();
            java.sql.Date eDate = medical.getEndDate() != null ? medical.getEndDate() : sDate;

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, newMedicalId);
                stmt.setString(2, medical.getStudentId());
                stmt.setString(3, medical.getOfferingId());
                stmt.setDate(4, sDate);
                stmt.setDate(5, sDate);
                stmt.setDate(6, eDate);
                stmt.setString(7, medical.getReason());
                stmt.setString(8, medical.getDocumentPath());
                stmt.setString(9, medical.getRemarks() != null ? medical.getRemarks() : "Submitted for academic verification");
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error submitting medical certificate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing medical leave entry (selected days taken off, offering, reason, document).
     */
    public boolean updateMedical(Medical medical) {
        String updateSql = "UPDATE medicals SET offering_id = ?, medical_date = ?, start_date = ?, end_date = ?, reason = ?, " +
                           "document_path = COALESCE(?, document_path), remarks = ? " +
                           "WHERE medical_id = ? AND student_id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            java.sql.Date sDate = medical.getStartDate() != null ? medical.getStartDate() : medical.getMedicalDate();
            java.sql.Date eDate = medical.getEndDate() != null ? medical.getEndDate() : sDate;

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, medical.getOfferingId());
                stmt.setDate(2, sDate);
                stmt.setDate(3, sDate);
                stmt.setDate(4, eDate);
                stmt.setString(5, medical.getReason());
                stmt.setString(6, medical.getDocumentPath());
                stmt.setString(7, medical.getRemarks() != null ? medical.getRemarks() : "Updated by student");
                stmt.setString(8, medical.getMedicalId());
                stmt.setString(9, medical.getStudentId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating medical certificate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all announcements, including faculty notices and advance assessment alerts (quizzes & assignments).
     */
    public List<Notice> getAllAnnouncements(String studentId) {
        List<Notice> list = new ArrayList<>();

        // 1. Fetch active faculty notices
        String noticeSql = "SELECT n.*, CONCAT(u.first_name, ' ', u.last_name) as author_name, u.role as author_role " +
                           "FROM notices n " +
                           "LEFT JOIN users u ON n.created_by = u.user_id " +
                           "WHERE n.status = 'ACTIVE' " +
                           "ORDER BY n.notice_date DESC";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(noticeSql)) {
                while (rs.next()) {
                    Notice notice = new Notice();
                    notice.setNoticeId(rs.getString("notice_id"));
                    notice.setTitle(rs.getString("title"));
                    notice.setDescription(rs.getString("description"));
                    notice.setNoticeDate(rs.getDate("notice_date"));
                    notice.setCreatedBy(rs.getString("created_by"));
                    String author = rs.getString("author_name");
                    String role = rs.getString("author_role");
                    if (author != null) {
                        notice.setAuthorName(author + (role != null && role.equalsIgnoreCase("LECTURER") ? " (Lecturer)" : " (Faculty Admin)"));
                    } else {
                        notice.setAuthorName("Academic Department");
                    }
                    notice.setStatus(rs.getString("status"));
                    list.add(notice);
                }
            }

            // 2. Fetch advance assessment announcements (quizzes & assignments) for student's enrolled courses
            String asmSql = "SELECT a.*, c.course_code, c.course_name, CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                            "FROM assessments a " +
                            "JOIN course_offerings co ON a.offering_id = co.offering_id " +
                            (studentId != null && !studentId.isEmpty() ? "JOIN enrollments e ON co.offering_id = e.offering_id AND e.student_id = ? " : "") +
                            "JOIN courses c ON co.course_id = c.course_id " +
                            "LEFT JOIN users u ON a.created_by = u.user_id " +
                            "ORDER BY a.assessment_date ASC";
            try (PreparedStatement stmt = conn.prepareStatement(asmSql)) {
                if (studentId != null && !studentId.isEmpty()) {
                    stmt.setString(1, studentId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String asmName = rs.getString("assessment_name");
                        String courseCode = rs.getString("course_code");
                        String type = rs.getString("assessment_type");
                        java.sql.Date date = rs.getDate("assessment_date");
                        BigDecimal weight = rs.getBigDecimal("weight_percentage");
                        BigDecimal maxMarks = rs.getBigDecimal("max_marks");
                        String lecturer = rs.getString("lecturer_name");

                        Notice asmNotice = new Notice();
                        asmNotice.setNoticeId(rs.getString("assessment_id"));
                        asmNotice.setCourseCode(courseCode);
                        asmNotice.setCourseName(rs.getString("course_name"));
                        asmNotice.setOfferingId(rs.getString("offering_id"));
                        asmNotice.setEventDate(date);
                        asmNotice.setNoticeDate(date);
                        asmNotice.setNoticeType(type);
                        asmNotice.setAuthorName(lecturer != null ? lecturer + " (Course Lecturer)" : "Course Lecturer");
                        asmNotice.setCreatedBy(rs.getString("created_by"));

                        String typeDisplay = "QUIZ".equalsIgnoreCase(type) ? "Quiz Announcement" : 
                                             "ASSIGNMENT".equalsIgnoreCase(type) ? "Assignment Notice" : "Assessment Notice";
                        asmNotice.setTitle("[" + courseCode + "] " + typeDisplay + ": " + asmName);

                        String desc = "Advance Announcement for " + courseCode + " - " + rs.getString("course_name") + ".\n" +
                                      "Scheduled Date: " + (date != null ? date.toString() : "TBA") + " | " +
                                      "Weight: " + (weight != null ? weight + "%" : "N/A") + " | " +
                                      "Max Marks: " + (maxMarks != null ? maxMarks : "100") + ".\n" +
                                      "Please ensure thorough preparation beforehand. Contact course lecturer for any clarification.";
                        asmNotice.setDescription(desc);
                        asmNotice.setStatus("ACTIVE");

                        list.add(asmNotice);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching announcements: " + e.getMessage());
        }

        // Sort: items with closest future dates first, then by date descending
        list.sort((n1, n2) -> {
            java.sql.Date d1 = n1.getEventDate() != null ? n1.getEventDate() : n1.getNoticeDate();
            java.sql.Date d2 = n2.getEventDate() != null ? n2.getEventDate() : n2.getNoticeDate();
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d2.compareTo(d1);
        });

        return list;
    }

    /**
     * Allows a lecturer to post an advance announcement.
     * Optionally registers the quiz/assignment in the assessments table so students can track it.
     */
    public boolean postAnnouncement(Notice notice, String offeringId, String assessmentType, BigDecimal weightPercentage, BigDecimal maxMarks, boolean syncWithAssessments) {
        String nextNoticeSql = "SELECT MAX(CAST(SUBSTRING(notice_id, 4) AS UNSIGNED)) FROM notices";
        String insertNoticeSql = "INSERT INTO notices (notice_id, title, description, notice_date, created_by, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try {
            Connection conn = DBConnection.getConnection();

            // 1. Determine next notice_id
            int nextNum = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(nextNoticeSql)) {
                if (rs.next()) {
                    nextNum = rs.getInt(1) + 1;
                }
            }
            String newNoticeId = String.format("NOT%03d", nextNum);
            notice.setNoticeId(newNoticeId);

            java.sql.Date pDate = notice.getNoticeDate() != null ? notice.getNoticeDate() : new java.sql.Date(System.currentTimeMillis());

            // Insert notice record
            try (PreparedStatement stmt = conn.prepareStatement(insertNoticeSql)) {
                stmt.setString(1, newNoticeId);
                stmt.setString(2, notice.getTitle());
                stmt.setString(3, notice.getDescription());
                stmt.setDate(4, pDate);
                stmt.setString(5, notice.getCreatedBy() != null ? notice.getCreatedBy() : "LEC001");
                stmt.executeUpdate();
            } catch (SQLException ex) {
                // If foreign key constraint triggers due to legacy admin FK, fallback to ADM001 safely
                if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("CONSTRAINT")) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertNoticeSql)) {
                        stmt.setString(1, newNoticeId);
                        stmt.setString(2, notice.getTitle());
                        stmt.setString(3, notice.getDescription());
                        stmt.setDate(4, pDate);
                        stmt.setString(5, "ADM001");
                        stmt.executeUpdate();
                    }
                } else {
                    throw ex;
                }
            }

            // 2. If syncWithAssessments requested and offeringId given, insert into assessments table
            if (syncWithAssessments && offeringId != null && !offeringId.isEmpty()) {
                String nextAsmSql = "SELECT MAX(CAST(SUBSTRING(assessment_id, 4) AS UNSIGNED)) FROM assessments";
                int nextAsmNum = 1;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(nextAsmSql)) {
                    if (rs.next()) {
                        nextAsmNum = rs.getInt(1) + 1;
                    }
                }
                String newAsmId = String.format("ASM%03d", nextAsmNum);

                String insertAsmSql = "INSERT INTO assessments (assessment_id, offering_id, assessment_name, assessment_type, weight_percentage, max_marks, assessment_date, created_by) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertAsmSql)) {
                    stmt.setString(1, newAsmId);
                    stmt.setString(2, offeringId);
                    stmt.setString(3, notice.getTitle());
                    stmt.setString(4, assessmentType != null ? assessmentType : "QUIZ");
                    stmt.setBigDecimal(5, weightPercentage != null ? weightPercentage : new BigDecimal("10.00"));
                    stmt.setBigDecimal(6, maxMarks != null ? maxMarks : new BigDecimal("100.00"));
                    stmt.setDate(7, notice.getEventDate() != null ? notice.getEventDate() : pDate);
                    stmt.setString(8, notice.getCreatedBy() != null ? notice.getCreatedBy() : "LEC001");
                    stmt.executeUpdate();
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error posting announcement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all lecturers for announcement posting and contact information.
     */
    public List<User> getLecturers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, l.department, l.designation " +
                     "FROM users u " +
                     "JOIN lecturers l ON u.user_id = l.lecturer_id " +
                     "WHERE u.status = 'ACTIVE' " +
                     "ORDER BY u.first_name";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getString("user_id"));
                    u.setFirstName(rs.getString("first_name"));
                    u.setLastName(rs.getString("last_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setRole("LECTURER");
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching lecturers: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all course offerings with associated course code, name, and assigned lecturer.
     */
    public List<Course> getAllCourseOfferings() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT co.offering_id, c.course_id, c.course_code, c.course_name, c.credits, " +
                     "co.academic_year, co.semester, co.batch, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as lecturer_name " +
                     "FROM course_offerings co " +
                     "JOIN courses c ON co.course_id = c.course_id " +
                     "LEFT JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                     "LEFT JOIN users u ON cl.lecturer_id = u.user_id " +
                     "ORDER BY c.course_code";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Course c = new Course();
                    c.setCourseId(rs.getString("course_id"));
                    c.setCourseCode(rs.getString("course_code"));
                    c.setCourseName(rs.getString("course_name"));
                    c.setCredits(rs.getInt("credits"));
                    c.setOfferingId(rs.getString("offering_id"));
                    c.setLecturerName(rs.getString("lecturer_name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course offerings: " + e.getMessage());
        }
        return list;
    }
}

