package model;

import java.util.Date;

/**
 * Medical Model Class
 * Represents student medical certificate submissions.
 * Unifies fields and operations needed by Admin, Student, and Technical Officer portals.
 */
public class Medical {
    private String medicalId;
    private String medicalCode;
    private String studentId;
    private String studentCode;
    private String indexNo;
    private String studentName;
    private String offeringId;
    private String offeringCode;
    private String courseCode;
    private String courseName;
    private String courseOfferingName;
    private Date medicalDate;
    private Date startDate;
    private Date endDate;
    private String reason;
    private String documentPath;
    private String approvalStatus; // "PENDING", "APPROVED", "NOT_APPROVED", "Approved", etc.
    private String approvedBy;
    private Date approvedDate;
    private String remarks;

    public Medical() {}

    public Medical(int medicalId, int studentId, Date medicalDate, String reason, String documentPath, String approvalStatus) {
        this.medicalId = String.format("MED%03d", medicalId);
        this.medicalCode = this.medicalId;
        this.studentId = String.format("STU%03d", studentId);
        this.studentCode = this.studentId;
        this.medicalDate = medicalDate;
        this.startDate = medicalDate;
        this.endDate = medicalDate;
        this.reason = reason;
        this.documentPath = documentPath;
        this.approvalStatus = approvalStatus;
    }

    public Medical(String medicalId, String studentId, String offeringId, Date medicalDate,
                   String reason, String documentPath, String approvalStatus, String approvedBy, String remarks) {
        this.medicalId = medicalId;
        this.medicalCode = medicalId;
        this.studentId = studentId;
        this.studentCode = studentId;
        this.offeringId = offeringId;
        this.offeringCode = offeringId;
        this.medicalDate = medicalDate;
        this.startDate = medicalDate;
        this.endDate = medicalDate;
        this.reason = reason;
        this.documentPath = documentPath;
        this.approvalStatus = approvalStatus;
        this.approvedBy = approvedBy;
        this.remarks = remarks;
    }

    public Medical(String medicalCode, String studentCode, String indexNo, String studentName,
                   String offeringCode, String courseOfferingName, Date medicalDate,
                   String reason, String documentPath, String approvalStatus, String approvedBy, String remarks) {
        this.medicalCode = medicalCode;
        this.medicalId = medicalCode;
        this.studentCode = studentCode;
        this.studentId = studentCode;
        this.indexNo = indexNo;
        this.studentName = studentName;
        this.offeringCode = offeringCode;
        this.offeringId = offeringCode;
        this.courseOfferingName = courseOfferingName;
        this.medicalDate = medicalDate;
        this.startDate = medicalDate;
        this.endDate = medicalDate;
        this.reason = reason;
        this.documentPath = documentPath;
        this.approvalStatus = approvalStatus;
        this.approvedBy = approvedBy;
        this.remarks = remarks;
    }

    public String getMedicalId() {
        if (medicalId != null && !medicalId.isEmpty()) return medicalId;
        return medicalCode != null ? medicalCode : "";
    }

    public void setMedicalId(String medicalId) {
        this.medicalId = medicalId;
        if (this.medicalCode == null || this.medicalCode.isEmpty()) {
            this.medicalCode = medicalId;
        }
    }

    public void setMedicalId(int medicalId) {
        this.medicalId = String.format("MED%03d", medicalId);
        this.medicalCode = this.medicalId;
    }

    public String getMedicalCode() {
        if (medicalCode != null && !medicalCode.isEmpty()) return medicalCode;
        return medicalId != null ? medicalId : "";
    }

    public void setMedicalCode(String medicalCode) {
        this.medicalCode = medicalCode;
        this.medicalId = medicalCode;
    }

    public String getStudentId() {
        if (studentId != null && !studentId.isEmpty()) return studentId;
        return studentCode != null ? studentCode : "";
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        if (this.studentCode == null || this.studentCode.isEmpty()) {
            this.studentCode = studentId;
        }
    }

    public void setStudentId(int studentId) {
        this.studentId = String.format("STU%03d", studentId);
        this.studentCode = this.studentId;
    }

    public String getStudentCode() {
        if (studentCode != null && !studentCode.isEmpty()) return studentCode;
        return studentId != null ? studentId : "";
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
        this.studentId = studentCode;
    }

    public String getIndexNo() { return indexNo; }
    public void setIndexNo(String indexNo) { this.indexNo = indexNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getOfferingId() {
        if (offeringId != null && !offeringId.isEmpty()) return offeringId;
        return offeringCode != null ? offeringCode : "";
    }

    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
        if (this.offeringCode == null || this.offeringCode.isEmpty()) {
            this.offeringCode = offeringId;
        }
    }

    public void setOfferingId(int offeringId) {
        this.offeringId = String.format("OFF%03d", offeringId);
        this.offeringCode = this.offeringId;
    }

    public String getOfferingCode() {
        if (offeringCode != null && !offeringCode.isEmpty()) return offeringCode;
        return offeringId != null ? offeringId : "";
    }

    public void setOfferingCode(String offeringCode) {
        this.offeringCode = offeringCode;
        this.offeringId = offeringCode;
    }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseOfferingName() { return courseOfferingName; }
    public void setCourseOfferingName(String courseOfferingName) { this.courseOfferingName = courseOfferingName; }

    public java.sql.Date getMedicalDate() {
        if (medicalDate == null) return null;
        if (medicalDate instanceof java.sql.Date) return (java.sql.Date) medicalDate;
        return new java.sql.Date(medicalDate.getTime());
    }

    public void setMedicalDate(Date medicalDate) {
        this.medicalDate = medicalDate;
        if (this.startDate == null) this.startDate = medicalDate;
        if (this.endDate == null) this.endDate = medicalDate;
    }

    public void setMedicalDate(java.sql.Date medicalDate) {
        this.medicalDate = medicalDate;
        if (this.startDate == null) this.startDate = medicalDate;
        if (this.endDate == null) this.endDate = medicalDate;
    }

    public java.sql.Date getStartDate() {
        Date d = (startDate != null) ? startDate : medicalDate;
        if (d == null) return null;
        if (d instanceof java.sql.Date) return (java.sql.Date) d;
        return new java.sql.Date(d.getTime());
    }

    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setStartDate(java.sql.Date startDate) { this.startDate = startDate; }

    public java.sql.Date getEndDate() {
        Date d = (endDate != null) ? endDate : (startDate != null ? startDate : medicalDate);
        if (d == null) return null;
        if (d instanceof java.sql.Date) return (java.sql.Date) d;
        return new java.sql.Date(d.getTime());
    }

    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setEndDate(java.sql.Date endDate) { this.endDate = endDate; }

    public String getDaysTakenOffDisplay() {
        Date start = getStartDate();
        Date end = getEndDate();
        if (start == null && end == null) return "N/A";
        if (start != null && (end == null || start.equals(end))) {
            return start.toString() + " (1 day)";
        }
        long diff = (end.getTime() - start.getTime()) / (1000L * 60 * 60 * 24) + 1;
        return start + " to " + end + " (" + diff + " days)";
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Date getApprovedDate() { return approvedDate; }
    public void setApprovedDate(Date approvedDate) { this.approvedDate = approvedDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public void approveMedical() {
        this.approvalStatus = "Approved";
        this.approvedDate = new Date();
    }
}
