package model;

import java.util.Date;

/**
 * AttendanceRecord DTO Class
 * Bridges undergraduate and attendance details for UI tables, reporting, and history.
 */
public class AttendanceRecord {
    private int attendanceId;
    private String attendanceCode; // "ATT0001"
    private int sessionId;
    private String sessionCode;    // "SES001"
    private int studentId;
    private String studentCode;    // "STU001"
    private String indexNo;        // "TG/2024/2001"
    private String studentName;
    private String status;         // "Present", "Absent", "Medical"
    private String remarks;

    // Additional session metadata for history views
    private int sessionNumber;
    private String sessionType;    // "Theory", "Practical"
    private Date sessionDate;
    private boolean medicalApproved;

    public AttendanceRecord() {
    }

    public AttendanceRecord(int studentId, String indexNo, String studentName, String status, String remarks) {
        this.studentId = studentId;
        this.studentCode = String.format("STU%03d", studentId);
        this.indexNo = indexNo;
        this.studentName = studentName;
        this.status = status;
        this.remarks = remarks;
    }

    public AttendanceRecord(String studentCode, String indexNo, String studentName, String status, String remarks) {
        this.studentCode = studentCode;
        try {
            this.studentId = Integer.parseInt(studentCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.studentId = 1;
        }
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
        this.attendanceCode = String.format("ATT%04d", attendanceId);
    }

    public String getAttendanceCode() {
        if (attendanceCode == null || attendanceCode.isEmpty()) {
            return String.format("ATT%04d", attendanceId);
        }
        return attendanceCode;
    }

    public void setAttendanceCode(String attendanceCode) {
        this.attendanceCode = attendanceCode;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
        this.sessionCode = String.format("SES%03d", sessionId);
    }

    public String getSessionCode() {
        if (sessionCode == null || sessionCode.isEmpty()) {
            return String.format("SES%03d", sessionId);
        }
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
        if (this.studentCode == null || this.studentCode.isEmpty()) {
            this.studentCode = String.format("STU%03d", studentId);
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
