package model;

/**
 * TechnicalOfficer Model Class
 * Represents technical officer entity mapped to `technical_officers` and `users` in database/fams.sql.
 */
public class TechnicalOfficer {
    private String technicalOfficerId; // e.g. "TO001"
    private String staffNo;            // e.g. "TO-STF-001"
    private String username;           // e.g. "to001"
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String address;
    private String officeRoom;
    private String designation;
    private String faculty;
    private String status;
    private String profilePicture;
    private String createdDate;

    public TechnicalOfficer() {
        this.status = "ACTIVE";
        this.faculty = "Faculty of Technology";
        this.designation = "Technical Officer (Grade II)";
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

    public TechnicalOfficer(String technicalOfficerId, String staffNo, String username, String firstName, String lastName,
                            String email, String phone, String department, String address, String officeRoom,
                            String designation, String faculty, String status, String profilePicture, String createdDate) {
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
        this.designation = designation;
        this.faculty = faculty;
        this.status = status;
        this.profilePicture = profilePicture;
        this.createdDate = createdDate;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOfficeRoom() {
        return officeRoom;
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
        return status != null ? status : "ACTIVE";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
