package model;

/**
 * TechnicalOfficer Model Class
 * Represents technical officer entity mapped to `technical_officers` and `users` in database/fams.sql.
 */
public class TechnicalOfficer {
    private String technicalOfficerId; // e.g. "TO001"
    private String staffNo;            // e.g. "TO-STF-001"
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;

    public TechnicalOfficer() {
    }

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String firstName, String lastName, String email, String phone, String department) {
        this.technicalOfficerId = technicalOfficerId;
        this.staffNo = staffNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.department = department;
    }

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
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
