package model;

/**
 * Attendance Model Class
 * Represents an individual student attendance entry for a specific session.
 * Mapped to table `attendance` in database/fams.sql.
 */
public class Attendance {
    private String attendanceId; // e.g. "ATT0001"
    private String sessionId;    // e.g. "SES001"
    private String studentId;    // e.g. "STU001"
    private String status;       // "PRESENT", "ABSENT", "MEDICAL"
    private String remarks;

    public Attendance() {
    }

    public Attendance(String attendanceId, String sessionId, String studentId, String status, String remarks) {
        this.attendanceId = attendanceId;
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.status = status;
        this.remarks = remarks;
    }

    public Attendance(int attendanceId, int sessionId, int studentId, String status, String remarks) {
        this.attendanceId = String.format("ATT%04d", attendanceId);
        this.sessionId = String.format("SES%03d", sessionId);
        this.studentId = String.format("STU%03d", studentId);
        this.status = status;
        this.remarks = remarks;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public void markAttendance() {
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "PRESENT";
        }
    }
}
