package model;

import java.sql.Timestamp;

/**
 * Model representing an Undergraduate Student in FAMS.
 * Combines data from both 'users' (authentication, contact) and 'undergraduates' (academic details).
 * Extends User to support polymorphic authentication and dashboard metrics.
 */
public class Student extends User {

    // From 'undergraduates' table
    private String indexNo;        // e.g. "TG/2024/2001"
    private String batch;          // e.g. "2024/2025"
    private String academicYear;   // e.g. "Level II"
    private String department;     // e.g. "Information and Communication Technology"
    private String studentStatus;  // "REGULAR", "REPEAT", "BATCH_MISSED"

    // Computed / Dashboard metrics
    private int enrolledCourseCount = 0;
    private double attendancePercentage = 0.0;
    private int pendingAssignmentCount = 0;
    private double currentGpa = 0.0;
    private double currentCgpa = 0.0;

    public Student() {
        super();
        this.studentStatus = "REGULAR";
        setStatus("ACTIVE");
        setRole("UNDERGRADUATE");
    }

    /**
     * 14-parameter constructor (used by Student Dashboard and Authentication flows).
     */
    public Student(String userId, String username, String role, String firstName, String lastName,
                   String email, String phone, String address, String status,
                   String indexNo, String batch, String academicYear, String department, String studentStatus) {
        super(userId, username, role, firstName, lastName, email, phone, address, status);
        this.indexNo = indexNo;
        this.batch = batch;
        this.academicYear = academicYear;
        this.department = department;
        this.studentStatus = (studentStatus != null && !studentStatus.isBlank()) ? studentStatus : "REGULAR";
    }

    /**
     * 12-parameter constructor (used by Admin Student Management module).
     */
    public Student(String studentId, String indexNo, String username, String firstName,
                   String lastName, String email, String phone, String batch,
                   String academicYear, String department, String studentStatus, String accountStatus) {
        super(studentId, username, "UNDERGRADUATE", firstName, lastName, email, phone, "",
                (accountStatus != null && !accountStatus.isBlank()) ? accountStatus : "ACTIVE");
        this.indexNo = indexNo;
        this.batch = batch;
        this.academicYear = academicYear;
        this.department = department;
        this.studentStatus = (studentStatus != null && !studentStatus.isBlank()) ? studentStatus : "REGULAR";
    }

    // Student ID aliases mapping to user_id
    public String getStudentId() {
        return getUserId();
    }

    public void setStudentId(String studentId) {
        setUserId(studentId);
    }

    // Account status aliases mapping to status
    public String getAccountStatus() {
        return getStatus();
    }

    public void setAccountStatus(String accountStatus) {
        setStatus(accountStatus);
    }

    // Academic properties
    public String getIndexNo() {
        return indexNo;
    }

    public void setIndexNo(String indexNo) {
        this.indexNo = indexNo;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStudentStatus() {
        return studentStatus;
    }

    public void setStudentStatus(String studentStatus) {
        this.studentStatus = studentStatus;
    }

    // Dashboard metrics
    public int getEnrolledCourseCount() {
        return enrolledCourseCount;
    }

    public void setEnrolledCourseCount(int enrolledCourseCount) {
        this.enrolledCourseCount = enrolledCourseCount;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public int getPendingAssignmentCount() {
        return pendingAssignmentCount;
    }

    public void setPendingAssignmentCount(int pendingAssignmentCount) {
        this.pendingAssignmentCount = pendingAssignmentCount;
    }

    public double getCurrentGpa() {
        return currentGpa;
    }

    public void setCurrentGpa(double currentGpa) {
        this.currentGpa = currentGpa;
    }

    public double getCurrentCgpa() {
        return currentCgpa;
    }

    public void setCurrentCgpa(double currentCgpa) {
        this.currentCgpa = currentCgpa;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + getStudentId() + '\'' +
                ", indexNo='" + indexNo + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", batch='" + batch + '\'' +
                ", department='" + department + '\'' +
                ", studentStatus='" + studentStatus + '\'' +
                ", accountStatus='" + getAccountStatus() + '\'' +
                '}';
    }
}
