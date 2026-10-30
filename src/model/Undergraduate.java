package model;

/**
 * Undergraduate Model Class
 * Represents undergraduate student entity mapped to `undergraduates` and `users` in database/fams.sql.
 */
public class Undergraduate {
    private int studentId;
    private String studentCode; // e.g. "STU001" (matching fams.sql)
    private String indexNo;     // e.g. "TG/2024/2001"
    private String firstName;
    private String lastName;
    private String batch;       // e.g. "2024/2025"
    private String studentStatus; // "REGULAR", "REPEAT", "BATCH_MISSED"
    private String phone;
    private String department;  // "Information and Communication Technology"
    private String profilePicture;

    public Undergraduate() {
    }

    public Undergraduate(int studentId, String indexNo, String firstName, String lastName, String batch, String department) {
        this.studentId = studentId;
        this.studentCode = String.format("STU%03d", studentId);
        this.indexNo = indexNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.batch = batch;
        this.department = department;
        this.studentStatus = "REGULAR";
    }

    public Undergraduate(String studentCode, String indexNo, String firstName, String lastName, String batch, String department, String studentStatus) {
        this.studentCode = studentCode;
        try {
            this.studentId = Integer.parseInt(studentCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.studentId = 1;
        }
        this.indexNo = indexNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.batch = batch;
        this.department = department;
        this.studentStatus = studentStatus != null ? studentStatus : "REGULAR";
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
        if (this.studentCode == null || this.studentCode.isEmpty()) {
            this.studentCode = String.format("STU%03d", studentId);
        }
    }

    public String getStudentCode() {
        if (studentCode == null || studentCode.isEmpty()) {
            return String.format("STU%03d", studentId);
        }
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
        try {
            this.studentId = Integer.parseInt(studentCode.replaceAll("\\D+", ""));
        } catch (Exception ignored) {
        }
    }

    public String getIndexNo() {
        return indexNo;
    }

    public void setIndexNo(String indexNo) {
        this.indexNo = indexNo;
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

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getStudentStatus() {
        return studentStatus;
    }

    public void setStudentStatus(String studentStatus) {
        this.studentStatus = studentStatus;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return indexNo + " - " + getFullName();
    }
}
