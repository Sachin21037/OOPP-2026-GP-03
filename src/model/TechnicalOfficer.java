package model;

import java.sql.Timestamp;

/**
 * Model representing a Technical Officer in FAMS.
 * Combines data from both 'users' (authentication, contact, account status)
 * and 'technical_officers' (staff number, department, office room, designation).
 */
public class TechnicalOfficer {

    // From 'technical_officers' table
    private String technicalOfficerId; // e.g. "TO001", matches users.user_id
    private String staffNo;            // e.g. "TO-STF-001"
    private String department;         // e.g. "Information and Communication Technology"
    private String officeRoom;         // e.g. "Room A-204, IT Technical Office"
    private String designation;        // e.g. "Technical Officer (Grade II)"
    private String faculty;            // e.g. "Faculty of Technology"

    // From 'users' table
    private String username;           // e.g. "to001"
    private String password;           // e.g. "To@123"
    private String firstName;          // e.g. "Dinesh"
    private String lastName;           // e.g. "Kumara"
    private String email;              // e.g. "dinesh.to@uor.lk"
    private String phone;              // e.g. "0710000007"
    private String address;            // optional
    private String profilePicture;     // optional
    private String accountStatus;      // "ACTIVE", "INACTIVE"
    private String status;             // alias for accountStatus
    private Timestamp createdAt;
    private String createdDate;

    public TechnicalOfficer() {
        this.status = "ACTIVE";
        this.accountStatus = "ACTIVE";
        this.faculty = "Faculty of Technology";
        this.designation = "Technical Officer (Grade II)";
        this.department = "Information and Communication Technology";
    }

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String firstName, String lastName, String email, String phone, String department) {
        this();
        this.technicalOfficerId = technicalOfficerId;
        this.staffNo = staffNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.department = department;
    }

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String department,
                            String username, String firstName, String lastName,
                            String email, String phone, String accountStatus) {
        this();
        this.technicalOfficerId = technicalOfficerId;
        this.staffNo = staffNo;
        this.department = department;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.accountStatus = (accountStatus != null && !accountStatus.isBlank()) ? accountStatus : "ACTIVE";
        this.status = this.accountStatus;
    }

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String department,
                            String username, String password, String firstName, String lastName,
                            String email, String phone, String address, String accountStatus) {
        this(technicalOfficerId, staffNo, department, username, firstName, lastName, email, phone, accountStatus);
        this.password = password;
        this.address = address;
    }

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String username, String firstName, String lastName,
                            String email, String phone, String department, String address, String officeRoom,
                            String designation, String faculty, String status, String profilePicture, String createdDate) {
        this();
        this.technicalOfficerId = technicalOfficerId;
        this.staffNo = staffNo;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.address = address;
        this.officeRoom = officeRoom;
        this.designation = designation != null ? designation : "Technical Officer (Grade II)";
        this.faculty = faculty != null ? faculty : "Faculty of Technology";
        this.status = status != null ? status : "ACTIVE";
        this.accountStatus = this.status;
        this.profilePicture = profilePicture;
        this.createdDate = createdDate;
    }

    // Getters and Setters

    public String getTechnicalOfficerId() {
        return technicalOfficerId;
    }

    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;
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

    public String getOfficeRoom() {
        return officeRoom != null ? officeRoom : "Room A-204, IT Technical Office";
    }

    public void setOfficeRoom(String officeRoom) {
        this.officeRoom = officeRoom;
    }

    public String getDesignation() {
        return designation != null ? designation : "Technical Officer (Grade II)";
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getFaculty() {
        return faculty != null ? faculty : "Faculty of Technology";
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getStatus() {
        if (status != null && !status.isBlank()) return status;
        if (accountStatus != null && !accountStatus.isBlank()) return accountStatus;
        return "ACTIVE";
    }

    public void setStatus(String status) {
        this.status = status;
        this.accountStatus = status;
    }

    public String getAccountStatus() {
        return getStatus();
    }

    public void setAccountStatus(String accountStatus) {
        setStatus(accountStatus);
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
        if (createdAt != null) {
            this.createdDate = createdAt.toString();
        }
    }

    public String getCreatedDate() {
        if (createdDate != null) return createdDate;
        if (createdAt != null) return createdAt.toString();
        return "2026-10-07 14:51:10";
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "TechnicalOfficer{" +
                "id='" + technicalOfficerId + '\'' +
                ", staffNo='" + staffNo + '\'' +
                ", name='" + getFullName() + '\'' +
                ", department='" + department + '\'' +
                ", email='" + email + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
