package model;

import java.util.Date;

/**
 * Medical Model Class
 * Follows Class Diagram Group 03
 */
public class Medical {
    private int medicalId;
    private String medicalCode; // e.g. "MED001" (matching fams.sql)
    private int studentId;
    private String studentCode; // e.g. "STU005" (matching fams.sql)
    private String indexNo;     // e.g. "TG/2024/2005"
    private String studentName; // e.g. "Sanduni Fernando"
    private int offeringId;
    private String offeringCode; // e.g. "OFF001"
    private String courseOfferingName; // e.g. "ICT2132 - Object Oriented Programming"
    private Date medicalDate;
    private String reason;
    private String documentPath;
    private String approvalStatus;  // "Approved", "Not Approved" / "Rejected", "Pending"
    private String approvedBy;
    private Date approvedDate;
    private String remarks;

    public Medical() {
    }

    public Medical(int medicalId, int studentId, Date medicalDate, String reason, String documentPath, String approvalStatus) {
        this.medicalId = medicalId;
        this.medicalCode = String.format("MED%03d", medicalId);
        this.studentId = studentId;
        this.studentCode = String.format("STU%03d", studentId);
        this.medicalDate = medicalDate;
        this.reason = reason;
        this.documentPath = documentPath;
        this.approvalStatus = approvalStatus;
    }

    public Medical(String medicalCode, String studentCode, String indexNo, String studentName,
                   String offeringCode, String courseOfferingName, Date medicalDate,
                   String reason, String documentPath, String approvalStatus, String approvedBy, String remarks) {
        this.medicalCode = medicalCode;
        try {
            this.medicalId = Integer.parseInt(medicalCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.medicalId = 1;
        }
        this.studentCode = studentCode;
        try {
            this.studentId = Integer.parseInt(studentCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.studentId = 1;
        }
        this.indexNo = indexNo;
        this.studentName = studentName;
        this.offeringCode = offeringCode;
        try {
            this.offeringId = Integer.parseInt(offeringCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.offeringId = 1;
        }
        this.courseOfferingName = courseOfferingName;
        this.medicalDate = medicalDate;
        this.reason = reason;
        this.documentPath = documentPath;
        this.approvalStatus = approvalStatus;
        this.approvedBy = approvedBy;
        this.remarks = remarks;
    }

    public int getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(int medicalId) {
        this.medicalId = medicalId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Date getMedicalDate() {
        return medicalDate;
    }

    public void setMedicalDate(Date medicalDate) {
        this.medicalDate = medicalDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getMedicalCode() {
        if (medicalCode == null || medicalCode.isEmpty()) {
            return String.format("MED%03d", medicalId);
        }
        return medicalCode;
    }

    public void setMedicalCode(String medicalCode) {
        this.medicalCode = medicalCode;
        try {
            this.medicalId = Integer.parseInt(medicalCode.replaceAll("\\D+", ""));
        } catch (Exception ignored) {
        }
    }

    public String getStudentCode() {
        if (studentCode == null || studentCode.isEmpty()) {
            return String.format("STU%03d", studentId);
        }
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
        try {
            this.studentId = Integer.parseInt(studentCode.replaceAll("\\D+", ""));
        } catch (Exception ignored) {
        }
    }

    public String getIndexNo() {
        return indexNo;
    }

    public void setIndexNo(String indexNo) {
        this.indexNo = indexNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(int offeringId) {
        this.offeringId = offeringId;
        if (this.offeringCode == null || this.offeringCode.isEmpty()) {
            this.offeringCode = String.format("OFF%03d", offeringId);
        }
    }

    public String getOfferingCode() {
        if (offeringCode == null || offeringCode.isEmpty()) {
            return String.format("OFF%03d", offeringId);
        }
        return offeringCode;
    }

    public void setOfferingCode(String offeringCode) {
        this.offeringCode = offeringCode;
        try {
            this.offeringId = Integer.parseInt(offeringCode.replaceAll("\\D+", ""));
        } catch (Exception ignored) {
        }
    }

    public String getCourseOfferingName() {
        return courseOfferingName;
    }

    public void setCourseOfferingName(String courseOfferingName) {
        this.courseOfferingName = courseOfferingName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void approveMedical() {
        // Business logic method as declared in Class Diagram Group 03
        this.approvalStatus = "Approved";
        this.approvedDate = new Date();
    }
}
