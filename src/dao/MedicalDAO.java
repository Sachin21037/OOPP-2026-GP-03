package dao;

import model.Medical;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * MedicalDAO Class
 * Accesses medical certificate records from MySQL (fams.sql schema)
 * or aligned local in-memory storage for resilient operation.
 */
public class MedicalDAO {

    // Seed mock storage precisely aligned with database/fams.sql
    private static final List<Medical> mockMedicals = new ArrayList<>();

    static {
        seedMockMedicals();
    }

    private static void seedMockMedicals() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // Row 1: MED001 from fams.sql
            Medical m1 = new Medical(
                    "MED001", "STU005", "TG/2024/2005", "Sanduni Fernando",
                    "OFF001", "ICT2132 - Object Oriented Programming (2024/2025)",
                    sdf.parse("2026-02-10"), "Fever and medical treatment",
                    "medical/STU005_MED001.pdf", "Approved", "TO001",
                    "Approved after document verification."
            );
            m1.setApprovedDate(sdf.parse("2026-02-10"));
            mockMedicals.add(m1);

            // Row 2: MED002 from fams.sql
            Medical m2 = new Medical(
                    "MED002", "STU010", "TG/2024/2010", "Piumi Kumari",
                    "OFF001", "ICT2132 - Object Oriented Programming (2024/2025)",
                    sdf.parse("2026-03-02"), "Viral infection",
                    "medical/STU010_MED002.pdf", "Pending", "-",
                    "Awaiting verification."
            );
            mockMedicals.add(m2);

            // Row 3: MED003 from fams.sql
            Medical m3 = new Medical(
                    "MED003", "STU016", "TG/2024/2016", "Isuru Fernando",
                    "OFF002", "ICT2142 - E-Business (2024/2025)",
                    sdf.parse("2026-03-18"), "Medical appointment",
                    "medical/STU016_MED003.pdf", "Rejected", "TO002",
                    "Supporting document was incomplete."
            );
            m3.setApprovedDate(sdf.parse("2026-03-18"));
            mockMedicals.add(m3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve all medical submissions (for MedicalManagementPage)
     */
    public List<Medical> getAllMedicals() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT m.medical_id, m.student_id, u.index_no, usr.first_name, usr.last_name, " +
                             "       m.offering_id, c.course_code, c.course_name, co.batch, " +
                             "       m.medical_date, m.reason, m.document_path, m.approval_status, m.approved_by, m.remarks " +
                             "FROM medicals m " +
                             "LEFT JOIN undergraduates u ON m.student_id = u.student_id " +
                             "LEFT JOIN users usr ON m.student_id = usr.user_id " +
                             "LEFT JOIN course_offerings co ON m.offering_id = co.offering_id " +
                             "LEFT JOIN courses c ON co.course_id = c.course_id " +
                             "ORDER BY m.medical_id ASC";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                List<Medical> list = new ArrayList<>();
                while (rs.next()) {
                    String status = rs.getString("approval_status");
                    if ("APPROVED".equalsIgnoreCase(status)) status = "Approved";
                    else if ("NOT_APPROVED".equalsIgnoreCase(status)) status = "Rejected";
                    else if ("PENDING".equalsIgnoreCase(status)) status = "Pending";

                    String studentName = (rs.getString("first_name") != null ? rs.getString("first_name") : "") + " " +
                                         (rs.getString("last_name") != null ? rs.getString("last_name") : "");
                    String courseInfo = (rs.getString("course_code") != null ? rs.getString("course_code") : "") + " - " +
                                        (rs.getString("course_name") != null ? rs.getString("course_name") : "");
                    if (rs.getString("batch") != null) {
                        courseInfo += " (" + rs.getString("batch") + ")";
                    }

                    Medical m = new Medical(
                            rs.getString("medical_id"),
                            rs.getString("student_id"),
                            rs.getString("index_no") != null ? rs.getString("index_no") : rs.getString("student_id"),
                            studentName.trim(),
                            rs.getString("offering_id"),
                            courseInfo.trim(),
                            rs.getDate("medical_date"),
                            rs.getString("reason"),
                            rs.getString("document_path"),
                            status,
                            rs.getString("approved_by") != null ? rs.getString("approved_by") : "-",
                            rs.getString("remarks") != null ? rs.getString("remarks") : ""
                    );
                    list.add(m);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[MedicalDAO] Query error in getAllMedicals: " + e.getMessage());
            }
        }
        return new ArrayList<>(mockMedicals);
    }

    public List<Medical> getMedicalsByStudent(int studentId) {
        String code = String.format("STU%03d", studentId);
        return getMedicalsByStudentCode(code);
    }

    public List<Medical> getMedicalsByStudentCode(String studentCode) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT m.medical_id, m.student_id, u.index_no, usr.first_name, usr.last_name, " +
                             "       m.offering_id, c.course_code, c.course_name, co.batch, " +
                             "       m.medical_date, m.reason, m.document_path, m.approval_status, m.approved_by, m.remarks " +
                             "FROM medicals m " +
                             "LEFT JOIN undergraduates u ON m.student_id = u.student_id " +
                             "LEFT JOIN users usr ON m.student_id = usr.user_id " +
                             "LEFT JOIN course_offerings co ON m.offering_id = co.offering_id " +
                             "LEFT JOIN courses c ON co.course_id = c.course_id " +
                             "WHERE m.student_id = ? OR u.index_no = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, studentCode);
                ps.setString(2, studentCode);
                ResultSet rs = ps.executeQuery();
                List<Medical> results = new ArrayList<>();
                while (rs.next()) {
                    String status = rs.getString("approval_status");
                    if ("APPROVED".equalsIgnoreCase(status)) status = "Approved";
                    else if ("NOT_APPROVED".equalsIgnoreCase(status)) status = "Rejected";
                    else if ("PENDING".equalsIgnoreCase(status)) status = "Pending";

                    String studentName = (rs.getString("first_name") != null ? rs.getString("first_name") : "") + " " +
                                         (rs.getString("last_name") != null ? rs.getString("last_name") : "");
                    String courseInfo = (rs.getString("course_code") != null ? rs.getString("course_code") : "") + " - " +
                                        (rs.getString("course_name") != null ? rs.getString("course_name") : "");

                    Medical m = new Medical(
                            rs.getString("medical_id"),
                            rs.getString("student_id"),
                            rs.getString("index_no"),
                            studentName.trim(),
                            rs.getString("offering_id"),
                            courseInfo.trim(),
                            rs.getDate("medical_date"),
                            rs.getString("reason"),
                            rs.getString("document_path"),
                            status,
                            rs.getString("approved_by") != null ? rs.getString("approved_by") : "-",
                            rs.getString("remarks") != null ? rs.getString("remarks") : ""
                    );
                    results.add(m);
                }
                return results;
            } catch (SQLException e) {
                System.err.println("[MedicalDAO] Query error in getMedicalsByStudentCode: " + e.getMessage());
            }
        }

        List<Medical> filtered = new ArrayList<>();
        for (Medical m : mockMedicals) {
            if (studentCode.equalsIgnoreCase(m.getStudentCode()) ||
                studentCode.equalsIgnoreCase(m.getIndexNo()) ||
                (studentCode.matches("\\d+") && m.getStudentId() == Integer.parseInt(studentCode))) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    public int getApprovedMedicalCount(int studentId) {
        return getApprovedMedicalCount(String.format("STU%03d", studentId));
    }

    public int getApprovedMedicalCount(String studentCode) {
        List<Medical> medicals = getMedicalsByStudentCode(studentCode);
        int count = 0;
        for (Medical m : medicals) {
            if ("Approved".equalsIgnoreCase(m.getApprovalStatus())) {
                count++;
            }
        }
        return count;
    }

    public boolean hasSubmittedMedical(int studentId) {
        return hasSubmittedMedical(String.format("STU%03d", studentId));
    }

    public boolean hasSubmittedMedical(String studentCode) {
        List<Medical> medicals = getMedicalsByStudentCode(studentCode);
        return !medicals.isEmpty();
    }

    public int getPendingMedicalCount() {
        int count = 0;
        for (Medical m : getAllMedicals()) {
            if ("Pending".equalsIgnoreCase(m.getApprovalStatus())) {
                count++;
            }
        }
        return count;
    }

    public boolean approveMedical(String medicalCode, String approvedBy) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "UPDATE medicals SET approval_status = 'APPROVED', approved_by = ?, remarks = 'Approved after document verification.' WHERE medical_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, approvedBy != null ? approvedBy : "TO001");
                ps.setString(2, medicalCode);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[MedicalDAO] Error approving medical: " + e.getMessage());
            }
        }

        for (Medical m : mockMedicals) {
            if (medicalCode.equalsIgnoreCase(m.getMedicalCode())) {
                m.setApprovalStatus("Approved");
                m.setApprovedBy(approvedBy != null ? approvedBy : "TO001");
                m.setApprovedDate(new Date());
                m.setRemarks("Approved after document verification.");
                return true;
            }
        }
        return true;
    }

    public boolean rejectMedical(String medicalCode, String approvedBy) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "UPDATE medicals SET approval_status = 'NOT_APPROVED', approved_by = ?, remarks = 'Supporting document rejected after review.' WHERE medical_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, approvedBy != null ? approvedBy : "TO001");
                ps.setString(2, medicalCode);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[MedicalDAO] Error rejecting medical: " + e.getMessage());
            }
        }

        for (Medical m : mockMedicals) {
            if (medicalCode.equalsIgnoreCase(m.getMedicalCode())) {
                m.setApprovalStatus("Rejected");
                m.setApprovedBy(approvedBy != null ? approvedBy : "TO001");
                m.setApprovedDate(new Date());
                m.setRemarks("Supporting document rejected after review.");
                return true;
            }
        }
        return true;
    }

    public boolean addMedical(Medical medical) {
        if (medical == null) return false;

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "INSERT INTO medicals (medical_id, student_id, offering_id, medical_date, reason, document_path, approval_status, approved_by, remarks) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, medical.getMedicalCode());
                ps.setString(2, medical.getStudentCode());
                ps.setString(3, medical.getOfferingCode() != null ? medical.getOfferingCode() : "OFF001");
                ps.setDate(4, new java.sql.Date(medical.getMedicalDate() != null ? medical.getMedicalDate().getTime() : System.currentTimeMillis()));
                ps.setString(5, medical.getReason() != null ? medical.getReason() : "Medical certificate");
                ps.setString(6, medical.getDocumentPath() != null ? medical.getDocumentPath() : "medical/slip.pdf");
                ps.setString(7, "PENDING");
                ps.setString(8, null);
                ps.setString(9, medical.getRemarks() != null ? medical.getRemarks() : "Submitted for approval");
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[MedicalDAO] Error adding medical: " + e.getMessage());
            }
        }

        mockMedicals.add(medical);
        return true;
    }

    public boolean deleteMedical(String medicalCode) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                String sql = "DELETE FROM medicals WHERE medical_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, medicalCode);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[MedicalDAO] Error deleting medical: " + e.getMessage());
            }
        }

        mockMedicals.removeIf(m -> medicalCode.equalsIgnoreCase(m.getMedicalCode()));
        return true;
    }
}
