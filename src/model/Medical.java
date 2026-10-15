package model;

import java.sql.Date;

public class Medical {
    private String medicalId;
    private String studentId;
    private String offeringId;
    private String courseCode;
    private String courseName;
    private Date medicalDate;
    private String reason;
    private String documentPath;
    private String approvalStatus; // 'PENDING', 'APPROVED', 'NOT_APPROVED'
    private String approvedBy;
    private String remarks;

    public Medical() {}

    public Medical(String medicalId, String studentId, String offeringId, Date medicalDate,
                   String reason, String documentPath, String approvalStatus, String approvedBy, String remarks) {
        this.medicalId = medicalId;
        this.studentId = studentId;
        this.offeringId = offeringId;
        this.medicalDate = medicalDate;
        this.reason = reason;
        this.documentPath = documentPath;
        this.approvalStatus = approvalStatus;
        this.approvedBy = approvedBy;
        this.remarks = remarks;
    }

    public String getMedicalId() { return medicalId; }
    public void setMedicalId(String medicalId) { this.medicalId = medicalId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getOfferingId() { return offeringId; }
    public void setOfferingId(String offeringId) { this.offeringId = offeringId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Date getMedicalDate() { return medicalDate; }
    public void setMedicalDate(Date medicalDate) { this.medicalDate = medicalDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
