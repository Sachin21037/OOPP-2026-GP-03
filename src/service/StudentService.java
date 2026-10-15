package service;

import dao.StudentDAO;
import model.*;

import java.util.List;

public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();

    public Student getStudentProfile(String studentId) {
        return studentDAO.getStudentProfile(studentId);
    }

    public List<Course> getEnrolledCourses(String studentId) {
        return studentDAO.getEnrolledCourses(studentId);
    }

    public List<Result> getStudentResults(String studentId) {
        return studentDAO.getStudentResults(studentId);
    }

    public List<AttendanceRecord> getAttendanceRecords(String studentId) {
        return studentDAO.getAttendanceRecords(studentId);
    }

    public List<Assessment> getAssessments(String studentId) {
        return studentDAO.getAssessments(studentId);
    }

    public List<CourseMaterial> getCourseMaterials(String studentId) {
        return studentDAO.getCourseMaterials(studentId);
    }

    public List<Notice> getActiveNotices() {
        return studentDAO.getActiveNotices();
    }

    public boolean updateProfilePicture(String userId, String picturePath) {
        return studentDAO.updateProfilePicture(userId, picturePath);
    }

    public List<Medical> getStudentMedicals(String studentId) {
        return studentDAO.getStudentMedicals(studentId);
    }

    public List<AttendanceRecord> getAbsentSessions(String studentId) {
        return studentDAO.getAbsentSessions(studentId);
    }

    public boolean submitMedical(Medical medical) {
        return studentDAO.submitMedical(medical);
    }
}
