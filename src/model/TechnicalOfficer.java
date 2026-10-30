package model;

import java.sql.Timestamp;

/**
 * Model representing a Technical Officer in FAMS.
 * Combines data from both 'users' (authentication, contact, account status)
 * and 'technical_officers' (staff number, department).
 */
public class TechnicalOfficer {

    // From 'technical_officers' table
    private String technicalOfficerId; // e.g. "TO001", matches users.user_id
    private String staffNo;            // e.g. "TO-STF-001"
    private String department;         // e.g. "Information and Communication Technology"

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
    private Timestamp createdAt;

    public TechnicalOfficer() {
        this.department = "Information and Communication Technology";
        this.accountStatus = "ACTIVE";
    }

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String department,
                            String username, String firstName, String lastName,
                            String email, String phone, String accountStatus) {
        this.technicalOfficerId = technicalOfficerId;
        this.staffNo = staffNo;
        this.department = department;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.accountStatus = (accountStatus != null && !accountStatus.isBlank()) ? accountStatus : "ACTIVE";
    }

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String department,
                            String username, String password, String firstName, String lastName,
                            String email, String phone, String address, String accountStatus) {
        this(technicalOfficerId, staffNo, department, username, firstName, lastName, email, phone, accountStatus);
        this.password = password;
        this.address = address;
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
        return "TechnicalOfficer{" +
                "id='" + technicalOfficerId + '\'' +
                ", staffNo='" + staffNo + '\'' +
                ", name='" + getFullName() + '\'' +
                ", department='" + department + '\'' +
                ", email='" + email + '\'' +
                ", status='" + accountStatus + '\'' +
                '}';
    }
}
