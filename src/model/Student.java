package model;

import java.sql.Timestamp;

/**
 * Model representing an Undergraduate Student in FAMS.
 * Combines data from both 'users' (authentication, contact) and 'undergraduates' (academic details).
 */
public class Student {

    // From 'undergraduates' table
    private String studentId;      // e.g. "STU001", matches users.user_id
    private String indexNo;        // e.g. "TG/2024/2001"
    private String batch;          // e.g. "2024/2025"
    private String academicYear;   // e.g. "Level II"
    private String department;     // e.g. "Information and Communication Technology"
    private String studentStatus;  // "REGULAR", "REPEAT", "BATCH_MISSED"

    // From 'users' table
    private String username;       // e.g. "stu001"
    private String password;       // e.g. "Stu@123"
    private String firstName;      // e.g. "Amal"
    private String lastName;       // e.g. "Perera"
    private String email;          // e.g. "amal.perera@student.uor.lk"
    private String phone;          // e.g. "0711000001"
    private String address;        // e.g. "Galle"
    private String profilePicture; // optional path/URL
    private String accountStatus;  // "ACTIVE", "INACTIVE"
    private Timestamp createdAt;

    public Student() {
        this.studentStatus = "REGULAR";
        this.accountStatus = "ACTIVE";
    }

    public Student(String studentId, String indexNo, String username, String firstName,
                   String lastName, String email, String phone, String batch,
                   String academicYear, String department, String studentStatus, String accountStatus) {
        this.studentId = studentId;
        this.indexNo = indexNo;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.batch = batch;
        this.academicYear = academicYear;
        this.department = department;
        this.studentStatus = (studentStatus != null && !studentStatus.isBlank()) ? studentStatus : "REGULAR";
        this.accountStatus = (accountStatus != null && !accountStatus.isBlank()) ? accountStatus : "ACTIVE";
    }

    // Getters and Setters

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        String fn = (firstName != null) ? firstName : "";
        String ln = (lastName != null) ? lastName : "";
        return (fn + " " + ln).trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", indexNo='" + indexNo + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", batch='" + batch + '\'' +
                ", department='" + department + '\'' +
                ", studentStatus='" + studentStatus + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}
