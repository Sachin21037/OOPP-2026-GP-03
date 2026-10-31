package dao;

import database.DBConnection;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * MedicalRecordsDAO - Data Access Object for Student Medical Records.
 * Handles database retrieval, filtering, updates, and summary metrics.
 */
public class MedicalRecordsDAO {

    public static class MedicalItem {
        public final String medicalId;
        public final String studentId;
        public final String indexNo;
        public final String studentName;
        public final String courseCode;
        public final String courseName;
        public final String medicalDate;
        public final String reason;
        public final String documentPath;
        public final String approvalStatus;
        public final String approvedBy;
        public final String remarks;

        public MedicalItem(
                String medicalId, String studentId, String indexNo,
                String studentName, String courseCode, String courseName,
                String medicalDate, String reason, String documentPath,
                String approvalStatus, String approvedBy, String remarks
        ) {
            this.medicalId = medicalId != null ? medicalId : "";
            this.studentId = studentId != null ? studentId : "";
            this.indexNo = indexNo != null ? indexNo : "";
            this.studentName = studentName != null ? studentName : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.medicalDate = medicalDate != null ? medicalDate : "";
            this.reason = reason != null ? reason : "";
            this.documentPath = documentPath != null ? documentPath : "";
            this.approvalStatus = approvalStatus != null ? approvalStatus.trim().toUpperCase(Locale.ROOT) : "PENDING";
            this.approvedBy = approvedBy != null ? approvedBy : "";
            this.remarks = remarks != null ? remarks : "";
        }

        public Object[] toTableRow() {
            String docStatus = (documentPath != null && !documentPath.trim().isEmpty()) ? "Available" : "-";
            return new Object[]{
                    medicalId,
                    studentId,
                    studentName,
                    courseCode,
                    medicalDate,
                    approvalStatus,
                    docStatus
            };
        }
    }

    public static class MedicalSummary {
        public final int total;
        public final int approved;
        public final int pending;
        public final int courseCount;

        public MedicalSummary(int total, int approved, int pending, int courseCount) {
            this.total = total;
            this.approved = approved;
            this.pending = pending;
            this.courseCount = courseCount;
        }
    }

    /**
     * Queries distinct medical records submitted for the lecturer's course offerings.
     */
    public static List<MedicalItem> loadMedicalRecords(String lecturerId) throws SQLException {
        List<MedicalItem> list = new ArrayList<>();
        String safeId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";

        String sql =
                "SELECT DISTINCT m.medical_id, m.student_id, ug.index_no, u.first_name, u.last_name, " +
                "       c.course_code, c.course_name, m.medical_date, m.reason, m.document_path, " +
                "       m.approval_status, m.approved_by, m.remarks " +
                "FROM medicals m " +
                "JOIN course_offerings co ON m.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                "JOIN undergraduates ug ON m.student_id = ug.student_id " +
                "JOIN users u ON ug.student_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY m.medical_date DESC, m.medical_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String medicalId = rs.getString("medical_id");
                    String studentId = rs.getString("student_id");
                    String indexNo = rs.getString("index_no");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    java.sql.Date d = rs.getDate("medical_date");
                    String medicalDate = d != null ? d.toString() : "";
                    String reason = rs.getString("reason");
                    String docPath = rs.getString("document_path");
                    String approvalStatus = rs.getString("approval_status");
                    String approvedBy = rs.getString("approved_by");
                    String remarks = rs.getString("remarks");

                    list.add(new MedicalItem(
                            medicalId, studentId, indexNo, fullName,
                            courseCode, courseName, medicalDate, reason,
                            docPath, approvalStatus, approvedBy, remarks
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Updates remarks for a medical record in MySQL database.
     */
    public static boolean updateMedicalRemarks(String medicalId, String remarks) throws SQLException {
        if (medicalId == null || medicalId.trim().isEmpty()) return false;
        String sql = "UPDATE medicals SET remarks = ? WHERE medical_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, remarks);
            ps.setString(2, medicalId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates approval status and remarks for a medical record.
     */
    public static boolean updateMedicalStatus(String medicalId, String status, String remarks, String lecturerId) throws SQLException {
        if (medicalId == null || medicalId.trim().isEmpty()) return false;
        String sql = "UPDATE medicals SET approval_status = ?, remarks = ?, approved_by = ? WHERE medical_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setString(3, lecturerId);
            ps.setString(4, medicalId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Filters medical records by course code, status, and search query.
     */
    public static List<MedicalItem> filterRecords(
            List<MedicalItem> loadedRecords,
            String selectedCourse,
            String selectedStatus,
            String searchQuery
    ) {
        List<MedicalItem> result = new ArrayList<>();
        if (loadedRecords == null) return result;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";

        for (MedicalItem item : loadedRecords) {
            boolean matchesSearch = query.isEmpty()
                    || item.studentId.toLowerCase(Locale.ROOT).contains(query)
                    || item.indexNo.toLowerCase(Locale.ROOT).contains(query)
                    || item.studentName.toLowerCase(Locale.ROOT).contains(query)
                    || item.medicalId.toLowerCase(Locale.ROOT).contains(query)
                    || item.reason.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesCourse = selectedCourse == null
                    || "All Courses".equalsIgnoreCase(selectedCourse)
                    || item.courseCode.equalsIgnoreCase(selectedCourse);

            boolean matchesStatus = selectedStatus == null
                    || "All Statuses".equalsIgnoreCase(selectedStatus)
                    || item.approvalStatus.equalsIgnoreCase(selectedStatus);

            if (matchesSearch && matchesCourse && matchesStatus) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Calculates summary metrics for medical records.
     */
    public static MedicalSummary calculateSummary(List<MedicalItem> list) {
        if (list == null || list.isEmpty()) {
            return new MedicalSummary(0, 0, 0, 0);
        }

        int approved = 0;
        int pending = 0;
        Set<String> courses = new HashSet<>();

        for (MedicalItem item : list) {
            String st = item.approvalStatus.toUpperCase(Locale.ROOT);
            if ("APPROVED".equals(st)) {
                approved++;
            } else if ("PENDING".equals(st)) {
                pending++;
            }
            if (!item.courseCode.isEmpty()) {
                courses.add(item.courseCode);
            }
        }

        return new MedicalSummary(list.size(), approved, pending, courses.size());
    }

    public static File resolveDocumentFile(String documentPath) {
        if (documentPath == null || documentPath.trim().isEmpty()) return null;
        File f = new File(documentPath);
        if (f.exists()) return f;
        f = new File("medical", f.getName());
        if (f.exists()) return f;
        f = new File("uploads/medicals", f.getName());
        if (f.exists()) return f;
        return null;
    }
}
