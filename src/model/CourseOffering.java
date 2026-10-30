package model;

/**
 * CourseOffering Model Class
 * Represents course offering entity mapped to `course_offerings` and `courses` in database/fams.sql.
 */
public class CourseOffering {
    private int offeringId;
    private String offeringCode; // e.g. "OFF001" (matching fams.sql)
    private int courseId;
    private String courseCodeId; // e.g. "CRS001" (matching fams.sql)
    private String courseCode;   // e.g. "ICT2132"
    private String courseName;   // e.g. "Object Oriented Programming"
    private String academicYear; // e.g. "2026/2027"
    private int semester;        // 1 or 2
    private String batch;        // e.g. "2024/2025"

    public CourseOffering() {
    }

    public CourseOffering(int offeringId, int courseId, String courseCode, String courseName, String academicYear, int semester, String batch) {
        this.offeringId = offeringId;
        this.offeringCode = String.format("OFF%03d", offeringId);
        this.courseId = courseId;
        this.courseCodeId = String.format("CRS%03d", courseId);
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.academicYear = academicYear;
        this.semester = semester;
        this.batch = batch;
    }

    public CourseOffering(String offeringCode, String courseCodeId, String courseCode, String courseName, String academicYear, int semester, String batch) {
        this.offeringCode = offeringCode;
        try {
            this.offeringId = Integer.parseInt(offeringCode.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.offeringId = 1;
        }
        this.courseCodeId = courseCodeId;
        try {
            this.courseId = Integer.parseInt(courseCodeId.replaceAll("\\D+", ""));
        } catch (Exception e) {
            this.courseId = 1;
        }
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.academicYear = academicYear;
        this.semester = semester;
        this.batch = batch;
    }

    public int getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(int offeringId) {
        this.offeringId = offeringId;
        if (this.offeringCode == null || this.offeringCode.isEmpty()) {
            this.offeringCode = String.format("OFF%03d", offeringId);
        }
    }

    public String getOfferingCode() {
        if (offeringCode == null || offeringCode.isEmpty()) {
            return String.format("OFF%03d", offeringId);
        }
        return offeringCode;
    }

    public void setOfferingCode(String offeringCode) {
        this.offeringCode = offeringCode;
        try {
            this.offeringId = Integer.parseInt(offeringCode.replaceAll("\\D+", ""));
        } catch (Exception ignored) {
        }
    }

    public String getCourseCodeId() {
        return courseCodeId;
    }

    public void setCourseCodeId(String courseCodeId) {
        this.courseCodeId = courseCodeId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getOfferingInfo() {
        return courseCode + " - " + courseName + " (" + batch + ")";
    }

    @Override
    public String toString() {
        return getOfferingInfo();
    }
}
