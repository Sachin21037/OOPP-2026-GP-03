package model;

import java.sql.Timestamp;

/**
 * Domain entity model representing a system login account and its linked profile.
 * Unifies records from the 'users' table and child profile tables:
 * - 'admins' (admin_id)
 * - 'lecturers' (lecturer_id, staff_no)
 * - 'technical_officers' (technical_officer_id, staff_no)
 * - 'undergraduates' (student_id, index_no)
 */
public class UserAccount {

    private String userId;                  // e.g. "ADM001", "STU001", "LEC001", "TO001"
    private String username;                // e.g. "admin001", "stu001"
    private String password;                // Internal credential (never exposed or displayed)
    private String role;                    // "ADMIN", "LECTURER", "TECHNICAL_OFFICER", "UNDERGRADUATE"
    private String firstName;               // e.g. "Kasun"
    private String lastName;                // e.g. "Perera"
    private String email;                   // e.g. "kasun.perera@uor.lk"
    private String phone;                   // e.g. "0710000002"
    private String address;                 // e.g. "Galle"
    private String profilePicture;          // optional avatar path
    private String status;                  // "ACTIVE", "INACTIVE"
    private Timestamp createdAt;

    // Linked profile details from child tables
    private String linkedProfileIdentifier; // e.g. "Index: TG/2024/2001", "Staff ID: LEC-STF-001", "Admin ID: ADM001"
    private String department;              // e.g. "Information and Communication Technology"
    private String linkedDetail;            // e.g. "Batch: 2024/2025", "Senior Lecturer", "Laboratory Staff"

    public UserAccount() {
        this.status = "ACTIVE";
    }

    public UserAccount(String userId, String username, String role,
                       String firstName, String lastName, String email,
                       String phone, String status) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.status = (status != null && !status.isBlank()) ? status : "ACTIVE";
    }

    public UserAccount(String userId, String username, String role,
                       String firstName, String lastName, String email,
                       String phone, String address, String status,
                       String linkedProfileIdentifier, String department) {
        this(userId, username, role, firstName, lastName, email, phone, status);
        this.address = address;
        this.linkedProfileIdentifier = linkedProfileIdentifier;
        this.department = department;
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
        String f = (firstName != null) ? firstName.trim() : "";
        String l = (lastName != null) ? lastName.trim() : "";
        String full = (f + " " + l).trim();
        return full.isEmpty() ? ((username != null) ? username : "-") : full;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getLinkedProfileIdentifier() {
        return linkedProfileIdentifier;
    }

    public void setLinkedProfileIdentifier(String linkedProfileIdentifier) {
        this.linkedProfileIdentifier = linkedProfileIdentifier;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLinkedDetail() {
        return linkedDetail;
    }

    public void setLinkedDetail(String linkedDetail) {
        this.linkedDetail = linkedDetail;
    }

    /**
     * Formats role enum to user-friendly display text.
     */
    public String getFormattedRole() {
        if (role == null) return "Unknown";
        switch (role.toUpperCase()) {
            case "ADMIN":
                return "Administrator";
            case "LECTURER":
                return "Lecturer";
            case "TECHNICAL_OFFICER":
                return "Technical Officer";
            case "UNDERGRADUATE":
                return "Undergraduate";
            default:
                return role;
        }
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", linkedProfile='" + linkedProfileIdentifier + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
