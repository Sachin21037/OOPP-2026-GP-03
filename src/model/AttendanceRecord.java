package model;

import java.util.Date;

/**
 * AttendanceRecord DTO Class
 * Bridges undergraduate and attendance details for UI tables, reporting, and history
 */
public class AttendanceRecord {
    private int attendanceId;
    private int sessionId;
    private int studentId;
    private String indexNo;
    private String studentName;
    private String status;      // "Present", "Absent", "Medical"
    private String remarks;
    
    // Additional session metadata for history views
    private int sessionNumber;
    private String sessionType;
    private Date sessionDate;
    private boolean medicalApproved;

    public AttendanceRecord() {
    }

    public AttendanceRecord(int studentId, String indexNo, String studentName, String status, String remarks) {
        this.studentId = studentId;
        this.indexNo = indexNo;
        this.studentName = studentName;
        this.status = status;
        this.remarks = remarks;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public boolean isMedicalApproved() {
        return medicalApproved;
    }

    public void setMedicalApproved(boolean medicalApproved) {
        this.medicalApproved = medicalApproved;
    }
}
