package model;

import java.sql.Time;

/**
 * Model class representing a Timetable Slot in FAMS.
 * Maps directly to the 'timetables' table and captures course offering,
 * schedule timing, venue, batch, and assigned academic staff.
 */
public class TimetableEntry {

    private String timetableId;   // 'timetable_id' varchar(10) PK, e.g. "TBL001"
    private String offeringId;    // 'offering_id' varchar(10) NOT NULL FK -> course_offerings(offering_id)
    private String dayOfWeek;     // 'day_of_week' enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY')
    private Time startTime;       // 'start_time' time NOT NULL
    private Time endTime;         // 'end_time' time NOT NULL
    private String venue;         // 'venue' varchar(100) NOT NULL
    private String batch;         // 'batch' varchar(20) NOT NULL, e.g. "2024/2025"
    private String department;    // 'department' varchar(100) NOT NULL

    // Joined metadata from course_offerings, courses, and lecturers
    private String courseCode;    // e.g. "ICT2132"
    private String courseName;    // e.g. "Object Oriented Programming"
    private int credits;          // e.g. 3
    private String academicYear;  // e.g. "2026/2027"
    private String semester;      // e.g. "SEMESTER 1"
    private String lecturers;     // e.g. "Kasun Perera"

    public TimetableEntry() {}

    public TimetableEntry(String timetableId, String offeringId, String dayOfWeek,
                          Time startTime, Time endTime, String venue,
                          String batch, String department) {
        this.timetableId = timetableId;
        this.offeringId = offeringId;
        this.dayOfWeek = (dayOfWeek != null) ? dayOfWeek.toUpperCase() : "MONDAY";
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.batch = batch;
        this.department = department;
    }

    // Getters and Setters
    public String getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(String timetableId) {
        this.timetableId = timetableId;
    }

    public String getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
    }

    public String getDayOfWeek() {
        return (dayOfWeek != null) ? dayOfWeek.toUpperCase() : "MONDAY";
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = (dayOfWeek != null) ? dayOfWeek.toUpperCase() : "MONDAY";
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getLecturers() {
        return (lecturers != null && !lecturers.isBlank()) ? lecturers : "Unassigned";
    }

    public void setLecturers(String lecturers) {
        this.lecturers = lecturers;
    }

    public String getFormattedTimeRange() {
        String s = (startTime != null) ? formatTime(startTime) : "--:--";
        String e = (endTime != null) ? formatTime(endTime) : "--:--";
        return s + " - " + e;
    }

    private String formatTime(Time t) {
        String str = t.toString();
        if (str.length() >= 5) {
            return str.substring(0, 5);
        }
        return str;
    }

    public boolean isLabSession() {
        return venue != null && venue.toLowerCase().contains("lab");
    }

    public String getSessionType() {
        return isLabSession() ? "LAB PRACTICAL" : "LECTURE";
    }

    public String getCourseDisplay() {
        if (courseCode != null && !courseCode.isBlank()) {
            return courseCode + " - " + (courseName != null ? courseName : "");
        }
        return offeringId != null ? offeringId : "N/A";
    }

    @Override
    public String toString() {
        return "TimetableEntry{" +
                "timetableId='" + timetableId + '\'' +
                ", offeringId='" + offeringId + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", time=" + getFormattedTimeRange() +
                ", venue='" + venue + '\'' +
                ", batch='" + batch + '\'' +
                ", course='" + getCourseDisplay() + '\'' +
                '}';
    }
}
