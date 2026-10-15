package model;

public class Student extends User {
    private String indexNo;
    private String batch;
    private String academicYear;
    private String department;
    private String studentStatus;

    // Computed / Dashboard metrics
    private int enrolledCourseCount = 0;
    private double attendancePercentage = 0.0;
    private int pendingAssignmentCount = 0;
    private double currentGpa = 0.0;
    private double currentCgpa = 0.0;

    public Student() {
        super();
    }

    public Student(String userId, String username, String role, String firstName, String lastName,
                   String email, String phone, String address, String status,
                   String indexNo, String batch, String academicYear, String department, String studentStatus) {
        super(userId, username, role, firstName, lastName, email, phone, address, status);
        this.indexNo = indexNo;
        this.batch = batch;
        this.academicYear = academicYear;
        this.department = department;
        this.studentStatus = studentStatus;
    }

    public String getIndexNo() { return indexNo; }
    public void setIndexNo(String indexNo) { this.indexNo = indexNo; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStudentStatus() { return studentStatus; }
    public void setStudentStatus(String studentStatus) { this.studentStatus = studentStatus; }

    public int getEnrolledCourseCount() { return enrolledCourseCount; }
    public void setEnrolledCourseCount(int enrolledCourseCount) { this.enrolledCourseCount = enrolledCourseCount; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public int getPendingAssignmentCount() { return pendingAssignmentCount; }
    public void setPendingAssignmentCount(int pendingAssignmentCount) { this.pendingAssignmentCount = pendingAssignmentCount; }

    public double getCurrentGpa() { return currentGpa; }
    public void setCurrentGpa(double currentGpa) { this.currentGpa = currentGpa; }

    public double getCurrentCgpa() { return currentCgpa; }
    public void setCurrentCgpa(double currentCgpa) { this.currentCgpa = currentCgpa; }
}
