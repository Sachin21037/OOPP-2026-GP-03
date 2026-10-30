package model;

import java.math.BigDecimal;

public class Result {
    private String resultId;
    private String studentId;
    private String offeringId;
    private String courseCode;
    private String courseName;
    private int credits;
    private BigDecimal caMark;
    private BigDecimal finalExamMark;
    private BigDecimal finalMark;
    private String grade;
    private BigDecimal gradePoint;
    private BigDecimal attendancePercentage;
    private boolean caEligible;
    private boolean finalEligible;
    private BigDecimal sgpa;
    private BigDecimal cgpa;

    public Result() {}

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getOfferingId() { return offeringId; }
    public void setOfferingId(String offeringId) { this.offeringId = offeringId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public BigDecimal getCaMark() { return caMark; }
    public void setCaMark(BigDecimal caMark) { this.caMark = caMark; }

    public BigDecimal getFinalExamMark() { return finalExamMark; }
    public void setFinalExamMark(BigDecimal finalExamMark) { this.finalExamMark = finalExamMark; }

    public BigDecimal getFinalMark() { return finalMark; }
    public void setFinalMark(BigDecimal finalMark) { this.finalMark = finalMark; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public BigDecimal getGradePoint() { return gradePoint; }
    public void setGradePoint(BigDecimal gradePoint) { this.gradePoint = gradePoint; }

    public BigDecimal getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public boolean isCaEligible() { return caEligible; }
    public void setCaEligible(boolean caEligible) { this.caEligible = caEligible; }

    public boolean isFinalEligible() { return finalEligible; }
    public void setFinalEligible(boolean finalEligible) { this.finalEligible = finalEligible; }

    public BigDecimal getSgpa() { return sgpa; }
    public void setSgpa(BigDecimal sgpa) { this.sgpa = sgpa; }

    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
}
