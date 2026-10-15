package ui.login;

import dao.StudentDAO;
import dao.UserDAO;
import model.Student;
import model.User;
import ui.student.StudentDashboard;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private final Color darkBlue = new Color(27, 67, 103);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color textColor = new Color(35, 45, 55);
    private final Color accentColor = new Color(20, 165, 145);
    private final Color borderColor = new Color(225, 228, 232);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public LoginFrame() {
        setTitle("Student Login - Faculty Academic Management System");
        setSize(480, 580);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(darkBlue);
        headerPanel.setPreferredSize(new Dimension(480, 110));
        headerPanel.setBorder(new EmptyBorder(25, 20, 20, 20));

        JLabel titleLabel = new JLabel("FACULTY ACADEMIC MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel portalLabel = new JLabel("STUDENT PORTAL LOGIN");
        portalLabel.setFont(new Font("Arial", Font.BOLD, 13));
        portalLabel.setForeground(new Color(180, 240, 220));
        portalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(portalLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(lightBackground);
        centerWrapper.setBorder(new EmptyBorder(20, 25, 25, 25));

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Color.WHITE);
        formCard.setPreferredSize(new Dimension(410, 380));
        formCard.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel cardHeading = new JLabel("Sign In to Your Account");
        cardHeading.setFont(new Font("Arial", Font.BOLD, 18));
        cardHeading.setForeground(textColor);
        cardHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel cardSub = new JLabel("Enter your student credentials to access your portal");
        cardSub.setFont(new Font("Arial", Font.PLAIN, 12));
        cardSub.setForeground(new Color(110, 120, 130));
        cardSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username
        JLabel userLabel = new JLabel("Username / Student ID");
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        userLabel.setForeground(textColor);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 13));
        usernameField.setPreferredSize(new Dimension(350, 38));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passLabel.setForeground(textColor);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setPreferredSize(new Dimension(350, 38));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Show password checkbox
        JCheckBox showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setFont(new Font("Arial", Font.PLAIN, 11));
        showPassCheck.setForeground(new Color(100, 110, 120));
        showPassCheck.setBackground(Color.WHITE);
        showPassCheck.setFocusPainted(false);
        showPassCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPassCheck.addActionListener(e -> {
            if (showPassCheck.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        // Status / Error message
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(210, 60, 60));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login Button
        loginButton = new JButton("LOGIN TO DASHBOARD");
        loginButton.setFont(new Font("Arial", Font.BOLD, 13));
        loginButton.setBackground(darkBlue);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(350, 42));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(35, 85, 130));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(darkBlue);
            }
        });

        loginButton.addActionListener(e -> performLogin());

        // Enter key to submit
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);

        // Demo credentials quick-fill button
        JButton demoBtn = new JButton("Use Sample Student Credentials (stu001 / Stu@123)");
        demoBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        demoBtn.setForeground(new Color(27, 67, 103));
        demoBtn.setBackground(new Color(240, 245, 250));
        demoBtn.setBorder(new LineBorder(new Color(200, 220, 240), 1));
        demoBtn.setFocusPainted(false);
        demoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        demoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        demoBtn.addActionListener(e -> {
            usernameField.setText("stu001");
            passwordField.setText("Stu@123");
            statusLabel.setText(" ");
        });

        // Assemble card
        formCard.add(cardHeading);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(cardSub);
        formCard.add(Box.createVerticalStrut(18));
        formCard.add(userLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(showPassCheck);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(statusLabel);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(loginButton);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(demoBtn);

        centerWrapper.add(formCard);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        statusLabel.setForeground(darkBlue);
        statusLabel.setText("Authenticating credentials...");

        // Authenticate via database
        User user = userDAO.authenticate(username, password);

        if (user != null) {
            if ("UNDERGRADUATE".equalsIgnoreCase(user.getRole())) {
                statusLabel.setForeground(accentColor);
                statusLabel.setText("Login successful! Opening dashboard...");

                // Retrieve detailed student profile
                Student student = studentDAO.getStudentProfile(user.getUserId());
                if (student == null) {
                    student = new Student(
                            user.getUserId(), user.getUsername(), user.getRole(),
                            user.getFirstName(), user.getLastName(), user.getEmail(),
                            user.getPhone(), user.getAddress(), user.getStatus(),
                            "TG/2024/2001", "2024/2025", "Level II",
                            "Information and Communication Technology", "REGULAR"
                    );
                }

                final Student finalStudent = student;
                Timer timer = new Timer(350, evt -> {
                    dispose();
                    new StudentDashboard(finalStudent).setVisible(true);
                });
                timer.setRepeats(false);
                timer.start();

            } else {
                statusLabel.setForeground(new Color(210, 60, 60));
                statusLabel.setText("Access denied: User is not an undergraduate student.");
            }
        } else {
            statusLabel.setForeground(new Color(210, 60, 60));
            statusLabel.setText("Invalid username or password. Please try again.");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
