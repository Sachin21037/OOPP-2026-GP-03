package model;

import java.sql.Timestamp;

/**
 * Model representing an Academic Staff Lecturer in FAMS.
 * Combines data from both 'users' (authentication, contact, account status)
 * and 'lecturers' (staff number, department, academic designation).
 */
public class Lecturer {

    // From 'lecturers' table
    private String lecturerId;    // e.g. "LEC001", matches users.user_id
    private String staffNo;       // e.g. "LEC-STF-001"
    private String department;    // e.g. "Information and Communication Technology"
    private String designation;   // e.g. "Senior Lecturer", "Lecturer", "Professor"

    // From 'users' table
    private String username;       // e.g. "lec001"
    private String password;       // e.g. "Lec@123"
    private String firstName;      // e.g. "Kasun"
    private String lastName;       // e.g. "Perera"
    private String email;          // e.g. "kasun.perera@uor.lk"
    private String phone;          // e.g. "0710000002"
    private String address;        // optional
    private String profilePicture; // optional
    private String accountStatus;  // "ACTIVE", "INACTIVE"
    private Timestamp createdAt;

    public Lecturer() {
        this.department = "Information and Communication Technology";
        this.designation = "Lecturer";
        this.accountStatus = "ACTIVE";
    }

    public Lecturer(String lecturerId, String staffNo, String department, String designation,
                    String username, String firstName, String lastName, String email,
                    String phone, String accountStatus) {
        this.lecturerId = lecturerId;
        this.staffNo = staffNo;
        this.department = department;
        this.designation = designation;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.accountStatus = (accountStatus != null && !accountStatus.isBlank()) ? accountStatus : "ACTIVE";
    }

    // Getters and Setters

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName != null ? lastName.trim() : "";
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
        return "Lecturer{" +
                "lecturerId='" + lecturerId + '\'' +
                ", staffNo='" + staffNo + '\'' +
                ", name='" + getFullName() + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", email='" + email + '\'' +
                ", status='" + accountStatus + '\'' +
                '}';
    }
}
