package service;

import dao.StudentDAO;
import dao.UserDAO;
import model.Student;
import model.User;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public User login(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    public Student getStudentForUser(User user) {
        if (user == null || !"UNDERGRADUATE".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return studentDAO.getStudentProfile(user.getUserId());
    }
}
