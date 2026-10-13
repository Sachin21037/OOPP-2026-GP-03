package model;

/**
 * Attendance Model Class
 * Strictly follows Class Diagram Group 03
 */
public class Attendance {
    private int attendanceId;
    private int sessionId;
    private int studentId;
    private String status;      // "Present", "Absent", "Medical"
    private String remarks;

    public Attendance() {
    }

    public Attendance(int attendanceId, int sessionId, int studentId, String status, String remarks) {
        this.attendanceId = attendanceId;
        this.sessionId = sessionId;
        this.studentId = studentId;
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
        // Business logic method as declared in Class Diagram Group 03
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "Present";
        }
    }
}
