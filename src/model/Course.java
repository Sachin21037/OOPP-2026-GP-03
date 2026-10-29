package model;

/**
 * Domain entity model representing an academic course in FAMS.
 * Maps directly to the 'courses' database table and holds derived semester offering metadata.
 */
public class Course {

    private String courseId;         // e.g. "CRS001" (Primary Key)
    private String courseCode;       // e.g. "ICT2132" (Unique Key)
    private String courseName;       // e.g. "Object Oriented Programming"
    private int credits;             // e.g. 3
    private String description;      // Course syllabus summary
    private String department;       // e.g. "Information and Communication Technology"

    // Associated metadata from 'course_offerings'
    private int offeringCount;       // Number of active/historical semester offerings
    private String offeringDetails;  // e.g. "2026/2027 SEMESTER 1"

    public Course() {
        this.credits = 3;
        this.department = "Information and Communication Technology";
    }

    public Course(String courseId, String courseCode, String courseName, int credits, String description, String department) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.description = description;
        this.department = (department != null && !department.isBlank())
                ? department
                : "Information and Communication Technology";
    }

    public Course(String courseId, String courseCode, String courseName, int credits,
                  String description, String department, int offeringCount, String offeringDetails) {
        this(courseId, courseCode, courseName, credits, description, department);
        this.offeringCount = offeringCount;
        this.offeringDetails = offeringDetails;
    }

    // Getters and Setters

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getOfferingCount() {
        return offeringCount;
    }

    public void setOfferingCount(int offeringCount) {
        this.offeringCount = offeringCount;
    }

    public String getOfferingDetails() {
        return offeringDetails;
    }

    public void setOfferingDetails(String offeringDetails) {
        this.offeringDetails = offeringDetails;
    }

    /**
     * Derived offering status based on semester offerings.
     */
    public String getStatus() {
        return offeringCount > 0 ? "ACTIVE" : "UNOFFERED";
    }

    public boolean isOffered() {
        return offeringCount > 0;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", credits=" + credits +
                ", department='" + department + '\'' +
                ", offeringCount=" + offeringCount +
                '}';
    }
}
