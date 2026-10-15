package ui.student;

import dao.StudentDAO;
import model.*;
import ui.login.LoginFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JFrame {

    // Dashboard colors (as specified by user)
    Color darkBlue = new Color(27, 67, 103);
    Color lightBackground = new Color(245, 247, 250);
    Color sidebarColor = new Color(27, 67, 103); // Blue navigation bar
    Color textColor = new Color(35, 45, 55);

    // Navigation bar colors
    Color navTextColor = new Color(220, 235, 248);
    Color navHoverColor = new Color(38, 85, 128);
    Color navActiveColor = new Color(18, 48, 76);
    Color navAccentColor = new Color(180, 240, 220);

    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JPanel dynamicViewsPanel;
    private JLabel welcomeLabel;
    private JLabel subtitleLabel;
    private JLabel headerAvatarLabel;

    private Student currentStudent;
    private final StudentDAO studentDAO = new StudentDAO();
    private final List<JButton> menuButtons = new ArrayList<>();

    // Default constructor for standalone launch
    public StudentDashboard() {
        this(null);
    }

    // Constructor with authenticated student
    public StudentDashboard(Student student) {
        if (student == null) {
            // Load STU001 by default from DB
            Student dbStudent = studentDAO.getStudentProfile("STU001");
            if (dbStudent != null) {
                this.currentStudent = dbStudent;
            } else {
                // Fallback mock student
                this.currentStudent = new Student(
                        "STU001", "stu001", "UNDERGRADUATE", "Amal", "Perera",
                        "amal.perera@student.uor.lk", "0711000001", "Galle", "ACTIVE",
                        "TG/2024/2001", "2024/2025", "Level II", "Information and Communication Technology", "REGULAR"
                );
                this.currentStudent.setEnrolledCourseCount(9);
                this.currentStudent.setAttendancePercentage(87.0);
                this.currentStudent.setPendingAssignmentCount(3);
                this.currentStudent.setCurrentGpa(2.42);
            }
        } else {
            this.currentStudent = student;
        }

        setTitle("Student Dashboard - Faculty Academic Management System");
        setSize(1050, 650);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(1050, 65));

        JLabel systemTitle = new JLabel("  FACULTY ACADEMIC MANAGEMENT SYSTEM");
        systemTitle.setForeground(Color.WHITE);
        systemTitle.setFont(new Font("Arial", Font.BOLD, 19));

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        headerRight.setOpaque(false);

        String initials = getStudentInitials();
        headerAvatarLabel = new JLabel(createAvatarIcon(currentStudent.getProfilePicture(), 36, initials));

        JLabel studentBadge = new JLabel(currentStudent.getFirstName() + " (" + currentStudent.getIndexNo() + ")  ");
        studentBadge.setForeground(Color.WHITE);
        studentBadge.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel userType = new JLabel("STUDENT  ");
        userType.setForeground(new Color(180, 240, 220));
        userType.setFont(new Font("Arial", Font.BOLD, 13));

        headerRight.add(headerAvatarLabel);
        headerRight.add(studentBadge);
        headerRight.add(userType);

        header.add(systemTitle, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Left sidebar (Blue navigation bar)
        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(195, 585));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(20, 50, 80)));

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(sidebarColor);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("  STUDENT MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 12));
        menuTitle.setForeground(new Color(170, 215, 245));
        menuTitle.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 0));

        menuPanel.add(menuTitle);

        // Menu buttons
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "My Courses");
        addMenuButton(menuPanel, "My Results");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Assignments");
        addMenuButton(menuPanel, "Course Materials");
        addMenuButton(menuPanel, "Medical Submissions");
        addMenuButton(menuPanel, "My Profile");

        sidebar.add(menuPanel, BorderLayout.NORTH);

        // Logout
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        logoutPanel.setBackground(sidebarColor);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 13));
        logoutButton.setForeground(new Color(255, 130, 130));
        logoutButton.setBackground(sidebarColor);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutButton.setForeground(new Color(255, 175, 175));
            }

            public void mouseExited(MouseEvent e) {
                logoutButton.setForeground(new Color(255, 130, 130));
            }
        });

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(
                        this,
                        "You have been logged out.",
                        "Logout",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        // Content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(lightBackground);

        // Welcome section
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(lightBackground);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(22, 25, 12, 25));

        welcomeLabel = new JLabel("Welcome, " + currentStudent.getFirstName() + " " + currentStudent.getLastName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 26));
        welcomeLabel.setForeground(textColor);

        String deptBatch = currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "Faculty of Technology";
        subtitleLabel = new JLabel(deptBatch + " • " + currentStudent.getIndexNo() + " • " + currentStudent.getBatch());
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 110, 120));

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(4));
        welcomePanel.add(subtitleLabel);

        contentPanel.add(welcomePanel, BorderLayout.NORTH);

        // Multi-view panel powered by CardLayout for seamless transitions
        contentCardLayout = new CardLayout();
        dynamicViewsPanel = new JPanel(contentCardLayout);
        dynamicViewsPanel.setBackground(lightBackground);

        // Add views
        dynamicViewsPanel.add(buildDashboardOverviewPanel(), "Dashboard");
        dynamicViewsPanel.add(buildCoursesPanel(), "My Courses");
        dynamicViewsPanel.add(buildResultsPanel(), "My Results");
        dynamicViewsPanel.add(buildAttendancePanel(), "Attendance");
        dynamicViewsPanel.add(buildAssignmentsPanel(), "Assignments");
        dynamicViewsPanel.add(buildCourseMaterialsPanel(), "Course Materials");
        dynamicViewsPanel.add(buildMedicalPanel(), "Medical Submissions");
        dynamicViewsPanel.add(buildProfilePanel(), "My Profile");

        contentPanel.add(dynamicViewsPanel, BorderLayout.CENTER);

        // Add everything to main panel
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Select Dashboard initially
        highlightMenuButton("Dashboard");

        setVisible(true);
    }

    // ==========================================
    // 1. DASHBOARD OVERVIEW VIEW
    // ==========================================
    private JPanel buildDashboardOverviewPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(lightBackground);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

        // 4 KPI Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsPanel.setBackground(lightBackground);

        String courseCountStr = String.valueOf(currentStudent.getEnrolledCourseCount() > 0 ? currentStudent.getEnrolledCourseCount() : 3);
        String attendanceStr = (currentStudent.getAttendancePercentage() > 0) ? (currentStudent.getAttendancePercentage() + "%") : "87%";
        String assignmentsStr = String.valueOf(currentStudent.getPendingAssignmentCount() > 0 ? currentStudent.getPendingAssignmentCount() : 3);
        String gpaStr = (currentStudent.getCurrentGpa() > 0) ? String.format("%.2f", currentStudent.getCurrentGpa()) : "3.50";

        cardsPanel.add(createCard("My Courses", courseCountStr, new Color(45, 100, 160)));
        cardsPanel.add(createCard("Attendance", attendanceStr, new Color(20, 165, 145)));
        cardsPanel.add(createCard("Assignments", assignmentsStr, new Color(45, 75, 100)));
        cardsPanel.add(createCard("Current GPA", gpaStr, new Color(115, 75, 180)));

        // Split panel for Student Activities and Faculty Notices
        JPanel bottomGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomGrid.setBackground(lightBackground);

        // Activities card
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel activityTitle = new JLabel("Recent Student Activities");
        activityTitle.setFont(new Font("Arial", Font.BOLD, 15));
        activityTitle.setForeground(textColor);

        JPanel activityList = new JPanel();
        activityList.setBackground(Color.WHITE);
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Load recent dynamic activities from assessments / materials
        List<Assessment> assessments = studentDAO.getAssessments(currentStudent.getUserId());
        List<CourseMaterial> materials = studentDAO.getCourseMaterials(currentStudent.getUserId());

        if (!assessments.isEmpty()) {
            Assessment first = assessments.get(0);
            addActivity(activityList, "New " + first.getAssessmentName() + " available for " + first.getCourseCode());
        } else {
            addActivity(activityList, "Available New Assignment");
        }

        if (!materials.isEmpty()) {
            CourseMaterial mat = materials.get(0);
            addActivity(activityList, "Lecture notes uploaded: " + mat.getTitle() + " (" + mat.getCourseCode() + ")");
        } else {
            addActivity(activityList, "Course material uploaded");
        }

        addActivity(activityList, "Attendance record verified: Level II semester 1");
        addActivity(activityList, "Results released for latest semester assessments");

        activityPanel.add(activityTitle, BorderLayout.NORTH);
        activityPanel.add(activityList, BorderLayout.CENTER);

        // Notices card
        JPanel noticePanel = new JPanel(new BorderLayout());
        noticePanel.setBackground(Color.WHITE);
        noticePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel noticeTitle = new JLabel("Faculty Announcements & Notices");
        noticeTitle.setFont(new Font("Arial", Font.BOLD, 15));
        noticeTitle.setForeground(darkBlue);

        JPanel noticeList = new JPanel();
        noticeList.setBackground(Color.WHITE);
        noticeList.setLayout(new BoxLayout(noticeList, BoxLayout.Y_AXIS));
        noticeList.setBorder(new EmptyBorder(10, 0, 0, 0));

        List<Notice> notices = studentDAO.getActiveNotices();
        if (notices.isEmpty()) {
            addNoticeItem(noticeList, "Semester Examination Notice", "Examinations will be held according to published timetables.");
            addNoticeItem(noticeList, "Assignment Submission", "Ensure all submissions are uploaded through the system before due dates.");
        } else {
            for (int i = 0; i < Math.min(notices.size(), 3); i++) {
                Notice n = notices.get(i);
                addNoticeItem(noticeList, n.getTitle(), n.getDescription());
            }
        }

        noticePanel.add(noticeTitle, BorderLayout.NORTH);
        noticePanel.add(noticeList, BorderLayout.CENTER);

        bottomGrid.add(activityPanel);
        bottomGrid.add(noticePanel);

        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(bottomGrid, BorderLayout.CENTER);

        return centerPanel;
    }

    // ==========================================
    // 2. MY COURSES VIEW
    // ==========================================
    private JPanel buildCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("My Enrolled Courses");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);

        JButton openWindowBtn = new JButton("Open in Window ↗");
        openWindowBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        openWindowBtn.setBackground(Color.WHITE);
        openWindowBtn.setForeground(darkBlue);
        openWindowBtn.setFocusPainted(false);
        openWindowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openWindowBtn.addActionListener(e -> new MyCoursesFrame(currentStudent).setVisible(true));

        topBar.add(sectionTitle, BorderLayout.WEST);
        topBar.add(openWindowBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        List<Course> courses = studentDAO.getEnrolledCourses(currentStudent.getUserId());
        String[] columns = {"Course Code", "Course Name", "Credits", "Semester", "Academic Year", "Coordinator"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Course c : courses) {
            model.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredits() + " Credits",
                    c.getSemester(),
                    c.getAcademicYear(),
                    c.getLecturerName()
            });
        }

        JTable table = createStandardTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 3. MY RESULTS VIEW
    // ==========================================
    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("Academic Results & Semester Performance");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);

        JLabel gpaBadge = new JLabel("Current SGPA / GPA: " + String.format("%.2f", currentStudent.getCurrentGpa()) + "  ");
        gpaBadge.setFont(new Font("Arial", Font.BOLD, 14));
        gpaBadge.setForeground(new Color(115, 75, 180));

        topBar.add(sectionTitle, BorderLayout.WEST);
        topBar.add(gpaBadge, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        List<Result> results = studentDAO.getStudentResults(currentStudent.getUserId());
        String[] columns = {"Course Code", "Course Name", "Credits", "CA Mark", "Final Mark", "Total", "Grade", "Grade Point", "SGPA", "CGPA", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Result r : results) {
            String ca = (r.getCaMark() != null) ? r.getCaMark().toString() : "N/A";
            String finalM = (r.getFinalExamMark() != null) ? r.getFinalExamMark().toString() : "N/A";
            String total = (r.getFinalMark() != null) ? r.getFinalMark().toString() : "N/A";
            String grade = (r.getGrade() != null) ? r.getGrade() : "-";
            String gp = (r.getGradePoint() != null) ? r.getGradePoint().toString() : "-";
            String sgpa = (r.getSgpa() != null) ? r.getSgpa().toString() : String.format("%.2f", currentStudent.getCurrentGpa());
            String cgpa = (r.getCgpa() != null) ? r.getCgpa().toString() : String.format("%.2f", currentStudent.getCurrentGpa());
            String status = (r.isFinalEligible() || (r.getGrade() != null && !r.getGrade().equals("E") && !r.getGrade().equals("F"))) ? "PASSED" : "PENDING";

            model.addRow(new Object[]{
                    r.getCourseCode(),
                    r.getCourseName(),
                    r.getCredits() + " Cr",
                    ca,
                    finalM,
                    total,
                    grade,
                    gp,
                    sgpa,
                    cgpa,
                    status
            });
        }

        JTable table = createStandardTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 4. ATTENDANCE VIEW
    // ==========================================
    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("Attendance Records");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);

        JLabel attBadge = new JLabel("Overall Attendance: " + currentStudent.getAttendancePercentage() + "%  ");
        attBadge.setFont(new Font("Arial", Font.BOLD, 14));
        attBadge.setForeground(new Color(20, 165, 145));

        topBar.add(sectionTitle, BorderLayout.WEST);
        topBar.add(attBadge, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        List<AttendanceRecord> records = studentDAO.getAttendanceRecords(currentStudent.getUserId());
        String[] columns = {"Date", "Course", "Session Type", "Session #", "Topic", "Attendance Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (AttendanceRecord a : records) {
            model.addRow(new Object[]{
                    a.getSessionDate() != null ? sdf.format(a.getSessionDate()) : "N/A",
                    a.getCourseCode() + " - " + a.getCourseName(),
                    a.getSessionType(),
                    "Session " + a.getSessionNumber(),
                    a.getTopic() != null ? a.getTopic() : "Lecture Session",
                    a.getStatus()
            });
        }

        JTable table = createStandardTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 5. ASSIGNMENTS VIEW
    // ==========================================
    private JPanel buildAssignmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("Assignments & Assessments");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);

        topBar.add(sectionTitle, BorderLayout.WEST);
        panel.add(topBar, BorderLayout.NORTH);

        List<Assessment> list = studentDAO.getAssessments(currentStudent.getUserId());
        String[] columns = {"Course Code", "Assessment Name", "Type", "Weight (%)", "Max Marks", "Due / Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Assessment a : list) {
            model.addRow(new Object[]{
                    a.getCourseCode(),
                    a.getAssessmentName(),
                    a.getAssessmentType(),
                    a.getWeightPercentage() + "%",
                    a.getMaxMarks(),
                    a.getAssessmentDate() != null ? a.getAssessmentDate().toString() : "TBD"
            });
        }

        JTable table = createStandardTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 6. COURSE MATERIALS VIEW
    // ==========================================
    private JPanel buildCourseMaterialsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("Course Learning Materials & Notes");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);

        topBar.add(sectionTitle, BorderLayout.WEST);
        panel.add(topBar, BorderLayout.NORTH);

        List<CourseMaterial> list = studentDAO.getCourseMaterials(currentStudent.getUserId());
        String[] columns = {"Course", "Title", "Description", "Document / File", "Lecturer", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (CourseMaterial m : list) {
            model.addRow(new Object[]{
                    m.getCourseCode(),
                    m.getTitle(),
                    m.getDescription() != null ? m.getDescription() : "-",
                    m.getFileName() != null ? m.getFileName() : "document.pdf",
                    m.getLecturerName(),
                    m.getUploadedAt() != null ? sdf.format(m.getUploadedAt()) : "Recent"
            });
        }

        JTable table = createStandardTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 7. MEDICAL SUBMISSIONS VIEW
    // ==========================================
    private JPanel buildMedicalPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBackground);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("Medical Certificate Submissions");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);

        JLabel sectionSub = new JLabel("Submit and track medical certificates for missed lectures, examinations, and laboratory sessions.");
        sectionSub.setFont(new Font("Arial", Font.PLAIN, 12));
        sectionSub.setForeground(new Color(110, 120, 130));

        titles.add(sectionTitle);
        titles.add(Box.createVerticalStrut(3));
        titles.add(sectionSub);

        JButton submitNewBtn = new JButton("+ Submit Medical Certificate");
        submitNewBtn.setFont(new Font("Arial", Font.BOLD, 12));
        submitNewBtn.setForeground(Color.WHITE);
        submitNewBtn.setBackground(darkBlue);
        submitNewBtn.setFocusPainted(false);
        submitNewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitNewBtn.setPreferredSize(new Dimension(235, 38));

        submitNewBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                submitNewBtn.setBackground(new Color(38, 85, 128));
            }
            public void mouseExited(MouseEvent e) {
                submitNewBtn.setBackground(darkBlue);
            }
        });

        topBar.add(titles, BorderLayout.WEST);
        topBar.add(submitNewBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // SECTION 1: LECTURES LACKING ATTENDANCE (ABSENCES)
        // -------------------------------------------------------------
        JPanel absentCard = new JPanel(new BorderLayout(0, 8));
        absentCard.setBackground(Color.WHITE);
        absentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel absentHeader = new JPanel(new BorderLayout());
        absentHeader.setBackground(Color.WHITE);

        JPanel absentTitlePanel = new JPanel();
        absentTitlePanel.setLayout(new BoxLayout(absentTitlePanel, BoxLayout.Y_AXIS));
        absentTitlePanel.setBackground(Color.WHITE);

        JLabel absentTitle = new JLabel("Lectures Lacking Attendance (Recorded Absences)");
        absentTitle.setFont(new Font("Arial", Font.BOLD, 14));
        absentTitle.setForeground(textColor);

        JLabel absentSubTitle = new JLabel("Lectures where you were marked ABSENT. Double-click any row or select a lecture to submit a medical certificate.");
        absentSubTitle.setFont(new Font("Arial", Font.PLAIN, 11));
        absentSubTitle.setForeground(new Color(110, 120, 130));

        absentTitlePanel.add(absentTitle);
        absentTitlePanel.add(Box.createVerticalStrut(2));
        absentTitlePanel.add(absentSubTitle);

        JPanel absentActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        absentActions.setBackground(Color.WHITE);

        JLabel absentCountBadge = new JLabel("0 Lectures Lacking Attendance");
        absentCountBadge.setFont(new Font("Arial", Font.BOLD, 12));
        absentCountBadge.setForeground(new Color(220, 38, 38));

        JButton submitSelectedBtn = new JButton("Submit Medical for Selected");
        submitSelectedBtn.setFont(new Font("Arial", Font.BOLD, 11));
        submitSelectedBtn.setBackground(new Color(240, 245, 250));
        submitSelectedBtn.setForeground(darkBlue);
        submitSelectedBtn.setFocusPainted(false);
        submitSelectedBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitSelectedBtn.setEnabled(false);

        absentActions.add(absentCountBadge);
        absentActions.add(submitSelectedBtn);

        absentHeader.add(absentTitlePanel, BorderLayout.WEST);
        absentHeader.add(absentActions, BorderLayout.EAST);
        absentCard.add(absentHeader, BorderLayout.NORTH);

        String[] absentColumns = {"Course Code", "Course Name", "Session Date", "Session Type", "Session #", "Lecture Topic", "Attendance", "Medical Status"};
        DefaultTableModel absentModel = new DefaultTableModel(absentColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable absentTable = createStandardTable(absentModel);
        absentCard.add(new JScrollPane(absentTable), BorderLayout.CENTER);

        // Renderer for Attendance Status (col 6)
        absentTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Arial", Font.BOLD, 11));
                lbl.setForeground(new Color(220, 38, 38)); // Red for ABSENT
                return lbl;
            }
        });

        // Renderer for Medical Status (col 7)
        absentTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Arial", Font.BOLD, 11));
                String st = (value != null) ? value.toString() : "";
                if (st.contains("Approved")) {
                    lbl.setForeground(new Color(20, 165, 145));
                } else if (st.contains("Pending")) {
                    lbl.setForeground(new Color(217, 119, 6));
                } else {
                    lbl.setForeground(new Color(185, 28, 28));
                }
                return lbl;
            }
        });

        // -------------------------------------------------------------
        // SECTION 2: MEDICAL SUBMISSIONS HISTORY
        // -------------------------------------------------------------
        JPanel historyCard = new JPanel(new BorderLayout(0, 8));
        historyCard.setBackground(Color.WHITE);
        historyCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel historyHeader = new JPanel(new BorderLayout());
        historyHeader.setBackground(Color.WHITE);

        JPanel historyTitlePanel = new JPanel();
        historyTitlePanel.setLayout(new BoxLayout(historyTitlePanel, BoxLayout.Y_AXIS));
        historyTitlePanel.setBackground(Color.WHITE);

        JLabel historyTitle = new JLabel("Medical Certificate Submissions History");
        historyTitle.setFont(new Font("Arial", Font.BOLD, 14));
        historyTitle.setForeground(textColor);

        JLabel historySubTitle = new JLabel("Review submitted medical documents, technical officer verification, and approval statuses.");
        historySubTitle.setFont(new Font("Arial", Font.PLAIN, 11));
        historySubTitle.setForeground(new Color(110, 120, 130));

        historyTitlePanel.add(historyTitle);
        historyTitlePanel.add(Box.createVerticalStrut(2));
        historyTitlePanel.add(historySubTitle);

        // KPI summary bar
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        summaryBar.setBackground(Color.WHITE);

        JLabel totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 11));
        totalLabel.setForeground(textColor);

        JLabel approvedLabel = new JLabel("Approved: 0");
        approvedLabel.setFont(new Font("Arial", Font.BOLD, 11));
        approvedLabel.setForeground(new Color(20, 165, 145));

        JLabel pendingLabel = new JLabel("Pending: 0");
        pendingLabel.setFont(new Font("Arial", Font.BOLD, 11));
        pendingLabel.setForeground(new Color(217, 119, 6));

        JLabel notApprovedLabel = new JLabel("Not Approved: 0");
        notApprovedLabel.setFont(new Font("Arial", Font.BOLD, 11));
        notApprovedLabel.setForeground(new Color(220, 38, 38));

        summaryBar.add(totalLabel);
        summaryBar.add(new JSeparator(SwingConstants.VERTICAL));
        summaryBar.add(approvedLabel);
        summaryBar.add(new JSeparator(SwingConstants.VERTICAL));
        summaryBar.add(pendingLabel);
        summaryBar.add(new JSeparator(SwingConstants.VERTICAL));
        summaryBar.add(notApprovedLabel);

        historyHeader.add(historyTitlePanel, BorderLayout.WEST);
        historyHeader.add(summaryBar, BorderLayout.EAST);
        historyCard.add(historyHeader, BorderLayout.NORTH);

        String[] historyColumns = {"Medical ID", "Course", "Date of Absence", "Reason / Medical Condition", "Supporting Document", "Status", "Review Remarks"};
        DefaultTableModel historyModel = new DefaultTableModel(historyColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable historyTable = createStandardTable(historyModel);
        historyCard.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        // Custom status renderer for history table
        historyTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Arial", Font.BOLD, 12));
                String st = (value != null) ? value.toString() : "";
                if ("APPROVED".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(20, 165, 145));
                } else if ("PENDING".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(217, 119, 6));
                } else {
                    lbl.setForeground(new Color(220, 38, 38));
                }
                return lbl;
            }
        });

        // -------------------------------------------------------------
        // SPLIT PANE: VERTICAL STACK OF BOTH TABLES
        // -------------------------------------------------------------
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, absentCard, historyCard);
        splitPane.setResizeWeight(0.48);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setBackground(lightBackground);
        panel.add(splitPane, BorderLayout.CENTER);

        // Store absent records in memory for table interaction
        List<AttendanceRecord> currentAbsentRecords = new ArrayList<>();

        // Unified reload helper
        Runnable reloadMedicals = () -> {
            // 1. Reload absent sessions (lectures lacking attendance)
            absentModel.setRowCount(0);
            currentAbsentRecords.clear();
            List<AttendanceRecord> absentList = studentDAO.getAbsentSessions(currentStudent.getUserId());
            currentAbsentRecords.addAll(absentList);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (AttendanceRecord a : absentList) {
                String medDisplay;
                if (a.getMedicalStatus() == null) {
                    medDisplay = "Action Required: Not Submitted";
                } else if ("PENDING".equalsIgnoreCase(a.getMedicalStatus())) {
                    medDisplay = "Medical Submitted (Pending)";
                } else if ("APPROVED".equalsIgnoreCase(a.getMedicalStatus())) {
                    medDisplay = "Medical Approved (" + (a.getMedicalId() != null ? a.getMedicalId() : "") + ")";
                } else {
                    medDisplay = "Medical: " + a.getMedicalStatus();
                }

                absentModel.addRow(new Object[]{
                        a.getCourseCode(),
                        a.getCourseName(),
                        a.getSessionDate() != null ? sdf.format(a.getSessionDate()) : "N/A",
                        a.getSessionType(),
                        "#" + a.getSessionNumber(),
                        a.getTopic() != null ? a.getTopic() : "Lecture Session",
                        a.getStatus(),
                        medDisplay
                });
            }

            if (absentList.isEmpty()) {
                absentCountBadge.setText("✓ Full Attendance (0 Absences)");
                absentCountBadge.setForeground(new Color(20, 165, 145));
                submitSelectedBtn.setEnabled(false);
            } else {
                absentCountBadge.setText(absentList.size() + " Lecture(s) Lacking Attendance");
                absentCountBadge.setForeground(new Color(220, 38, 38));
            }

            // 2. Reload medical submissions history
            historyModel.setRowCount(0);
            List<Medical> list = studentDAO.getStudentMedicals(currentStudent.getUserId());
            int total = list.size();
            int approved = 0;
            int pending = 0;
            int notApproved = 0;

            for (Medical m : list) {
                if ("APPROVED".equalsIgnoreCase(m.getApprovalStatus())) {
                    approved++;
                } else if ("PENDING".equalsIgnoreCase(m.getApprovalStatus())) {
                    pending++;
                } else {
                    notApproved++;
                }

                String docName = (m.getDocumentPath() != null && !m.getDocumentPath().isEmpty())
                        ? new File(m.getDocumentPath()).getName()
                        : "No attachment";

                historyModel.addRow(new Object[]{
                        m.getMedicalId(),
                        m.getCourseCode() + " - " + m.getCourseName(),
                        m.getMedicalDate() != null ? sdf.format(m.getMedicalDate()) : "N/A",
                        m.getReason(),
                        docName,
                        m.getApprovalStatus(),
                        m.getRemarks() != null ? m.getRemarks() : "Under evaluation"
                });
            }

            totalLabel.setText("Total: " + total);
            approvedLabel.setText("Approved: " + approved);
            pendingLabel.setText("Pending: " + pending);
            notApprovedLabel.setText("Not Approved: " + notApproved);
        };

        // Table selection handler
        absentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = absentTable.getSelectedRow();
                submitSelectedBtn.setEnabled(selectedRow >= 0 && selectedRow < currentAbsentRecords.size());
            }
        });

        // Double-click on absent lecture opens dialog with pre-filled record
        absentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = absentTable.getSelectedRow();
                    if (row >= 0 && row < currentAbsentRecords.size()) {
                        openMedicalSubmissionDialog(reloadMedicals, currentAbsentRecords.get(row));
                    }
                }
            }
        });

        // Submit for selected button
        submitSelectedBtn.addActionListener(e -> {
            int row = absentTable.getSelectedRow();
            if (row >= 0 && row < currentAbsentRecords.size()) {
                openMedicalSubmissionDialog(reloadMedicals, currentAbsentRecords.get(row));
            }
        });

        // New submission button (general)
        submitNewBtn.addActionListener(e -> openMedicalSubmissionDialog(reloadMedicals, null));

        reloadMedicals.run();
        return panel;
    }

    private void openMedicalSubmissionDialog(Runnable onSuccess) {
        openMedicalSubmissionDialog(onSuccess, null);
    }

    private void openMedicalSubmissionDialog(Runnable onSuccess, AttendanceRecord initialRecord) {
        JDialog dialog = new JDialog(this, "Submit Medical Certificate", true);
        dialog.setSize(620, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("New Medical Certificate Submission");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Excuse recorded absences by submitting certified medical documentation");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(new Color(210, 225, 240));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setBackground(darkBlue);
        headerText.add(title);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(subtitle);
        header.add(headerText, BorderLayout.WEST);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 25, 20, 25));

        // -------------------------------------------------------------
        // 1. SELECT LECTURE LACKING ATTENDANCE (PRIMARY WORKFLOW)
        // -------------------------------------------------------------
        JLabel absentLbl = new JLabel("Lectures Lacking Attendance (Recorded Absences) *");
        absentLbl.setFont(new Font("Arial", Font.BOLD, 12));
        absentLbl.setForeground(textColor);

        List<AttendanceRecord> absentSessions = studentDAO.getAbsentSessions(currentStudent.getUserId());
        JComboBox<AbsentSessionItem> absentCombo = new JComboBox<>();
        absentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        absentCombo.setPreferredSize(new Dimension(540, 36));

        if (absentSessions.isEmpty()) {
            absentCombo.addItem(new AbsentSessionItem(null, "-- No absent sessions recorded (Manual Entry) --"));
        } else {
            absentCombo.addItem(new AbsentSessionItem(null, "-- Select a lecture lacking attendance to autofill -- (" + absentSessions.size() + " recorded absences)"));
            for (AttendanceRecord a : absentSessions) {
                String label = a.getCourseCode() + " | " + a.getSessionDate() + " | " + a.getSessionType() + " #" + a.getSessionNumber() +
                        " - " + (a.getTopic() != null ? a.getTopic() : "Lecture") +
                        (a.getMedicalStatus() != null ? " [" + a.getMedicalStatus() + "]" : " [ABSENT]");
                absentCombo.addItem(new AbsentSessionItem(a, label));
            }
            absentCombo.addItem(new AbsentSessionItem(null, "-- Other / Manual Course & Date Entry --"));
        }

        // Lecture Info Preview Banner
        JPanel infoBanner = new JPanel(new BorderLayout(8, 4));
        infoBanner.setBackground(new Color(240, 246, 254));
        infoBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        infoBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        infoBanner.setPreferredSize(new Dimension(540, 60));

        JLabel infoBannerLabel = new JLabel("<html><b>💡 Quick Selection:</b> Choose an absent lecture above to automatically pre-fill the course offering and date of absence.</html>");
        infoBannerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoBannerLabel.setForeground(new Color(30, 58, 138));
        infoBanner.add(infoBannerLabel, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // 2. ENROLLED COURSE OFFERING
        // -------------------------------------------------------------
        JLabel courseLbl = new JLabel("Enrolled Course Offering *");
        courseLbl.setFont(new Font("Arial", Font.BOLD, 12));
        courseLbl.setForeground(textColor);

        List<Course> courses = studentDAO.getEnrolledCourses(currentStudent.getUserId());
        JComboBox<CourseItem> courseCombo = new JComboBox<>();
        courseCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        courseCombo.setPreferredSize(new Dimension(540, 36));
        for (Course c : courses) {
            courseCombo.addItem(new CourseItem(c.getOfferingId(), c.getCourseCode() + " - " + c.getCourseName()));
        }

        // -------------------------------------------------------------
        // 3. DATE OF ABSENCE
        // -------------------------------------------------------------
        JLabel dateLbl = new JLabel("Date of Absence / Medical Date (YYYY-MM-DD) *");
        dateLbl.setFont(new Font("Arial", Font.BOLD, 12));
        dateLbl.setForeground(textColor);

        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        dateField.setPreferredSize(new Dimension(540, 36));

        // -------------------------------------------------------------
        // 4. REASON / DIAGNOSIS
        // -------------------------------------------------------------
        JLabel reasonLbl = new JLabel("Reason / Medical Diagnosis *");
        reasonLbl.setFont(new Font("Arial", Font.BOLD, 12));
        reasonLbl.setForeground(textColor);

        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setFont(new Font("Arial", Font.PLAIN, 12));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        reasonScroll.setPreferredSize(new Dimension(540, 70));

        // -------------------------------------------------------------
        // 5. FILE ATTACHMENT
        // -------------------------------------------------------------
        JLabel attachLbl = new JLabel("Medical Certificate Document (PDF, JPG, PNG) *");
        attachLbl.setFont(new Font("Arial", Font.BOLD, 12));
        attachLbl.setForeground(textColor);

        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        fileRow.setBackground(Color.WHITE);
        fileRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JTextField fileField = new JTextField("No document chosen");
        fileField.setEditable(false);
        fileField.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton chooseBtn = new JButton("Browse Document...");
        chooseBtn.setFont(new Font("Arial", Font.BOLD, 12));
        chooseBtn.setFocusPainted(false);
        chooseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final File[] chosenFile = new File[1];
        chooseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Medical Certificate (PDF, JPG, PNG)");
            fc.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Documents & Images (*.pdf, *.jpg, *.jpeg, *.png)", "pdf", "jpg", "jpeg", "png");
            fc.addChoosableFileFilter(filter);

            int res = fc.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                chosenFile[0] = fc.getSelectedFile();
                fileField.setText(chosenFile[0].getName());
            }
        });

        fileRow.add(fileField, BorderLayout.CENTER);
        fileRow.add(chooseBtn, BorderLayout.EAST);

        // Autofill logic when absentCombo selection changes
        absentCombo.addActionListener(e -> {
            AbsentSessionItem selected = (AbsentSessionItem) absentCombo.getSelectedItem();
            if (selected != null && selected.record != null) {
                AttendanceRecord rec = selected.record;

                // Match Course Offering
                for (int i = 0; i < courseCombo.getItemCount(); i++) {
                    CourseItem ci = courseCombo.getItemAt(i);
                    if (ci.offeringId.equals(rec.getOfferingId())) {
                        courseCombo.setSelectedIndex(i);
                        break;
                    }
                }

                // Set date
                if (rec.getSessionDate() != null) {
                    dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(rec.getSessionDate()));
                }

                // Suggest reason if empty or generic
                String curReason = reasonArea.getText().trim();
                if (curReason.isEmpty() || curReason.startsWith("Medical absence for")) {
                    reasonArea.setText("Medical absence for " + rec.getCourseCode() + " " + rec.getSessionType() +
                            " Session #" + rec.getSessionNumber() + " (" + (rec.getTopic() != null ? rec.getTopic() : "Lecture") + ") due to illness.");
                }

                // Update Info Banner
                String medStatusText = rec.getMedicalStatus() != null
                        ? "<span style='color:#D97706;'>[Medical already " + rec.getMedicalStatus() + "]</span>"
                        : "<span style='color:#DC2626;'>[Status: ABSENT - Excusal Required]</span>";
                infoBannerLabel.setText("<html><b>📌 Linked Lecture:</b> " + rec.getCourseCode() + " (" + rec.getCourseName() + ")<br/>" +
                        "<b>Session:</b> " + rec.getSessionType() + " #" + rec.getSessionNumber() + " on <b>" + rec.getSessionDate() + "</b> &nbsp;|&nbsp; " +
                        medStatusText + "</html>");
            } else {
                infoBannerLabel.setText("<html><b>💡 Tip:</b> Select a missed lecture above to automatically link your medical certificate, or enter details below manually.</html>");
            }
        });

        // Assemble body
        body.add(absentLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(absentCombo);
        body.add(Box.createVerticalStrut(8));
        body.add(infoBanner);
        body.add(Box.createVerticalStrut(12));
        body.add(courseLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(courseCombo);
        body.add(Box.createVerticalStrut(12));
        body.add(dateLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(dateField);
        body.add(Box.createVerticalStrut(12));
        body.add(reasonLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(reasonScroll);
        body.add(Box.createVerticalStrut(12));
        body.add(attachLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(fileRow);

        // Pre-select initial record if provided
        if (initialRecord != null) {
            for (int i = 0; i < absentCombo.getItemCount(); i++) {
                AbsentSessionItem item = absentCombo.getItemAt(i);
                if (item.record != null && item.record.getSessionId().equals(initialRecord.getSessionId())) {
                    absentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Wrap body in scroll pane for responsive display
        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = new JButton("Submit Medical Certificate");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 12));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(darkBlue);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        submitBtn.addActionListener(e -> {
            CourseItem selectedCourse = (CourseItem) courseCombo.getSelectedItem();
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an enrolled course.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String dateStr = dateField.getText().trim();
            java.sql.Date sqlDate;
            try {
                java.util.Date parsed = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                sqlDate = new java.sql.Date(parsed.getTime());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format! Please enter YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please provide the reason / medical diagnosis.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (chosenFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please attach a medical certificate file (PDF, JPG, PNG).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String docPath = null;
            try {
                File dir = new File("uploads/medicals");
                if (!dir.exists()) dir.mkdirs();

                String origName = chosenFile[0].getName().toLowerCase();
                String ext = origName.contains(".") ? origName.substring(origName.lastIndexOf('.')) : ".pdf";
                String targetName = currentStudent.getUserId() + "_" + System.currentTimeMillis() + ext;
                File targetFile = new File(dir, targetName);
                Files.copy(chosenFile[0].toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                docPath = targetFile.getPath().replace("\\", "/");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to copy attached document: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Medical m = new Medical();
            m.setStudentId(currentStudent.getUserId());
            m.setOfferingId(selectedCourse.offeringId);
            m.setMedicalDate(sqlDate);
            m.setReason(reason);
            m.setDocumentPath(docPath);
            m.setRemarks("Submitted by student. Awaiting Technical Officer verification.");

            boolean ok = studentDAO.submitMedical(m);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Medical certificate submitted successfully!\nReference ID: " + m.getMedicalId(), "Submission Successful", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Database error: could not save medical certificate record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancelBtn);
        footer.add(submitBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static class AbsentSessionItem {
        final AttendanceRecord record;
        final String displayText;

        AbsentSessionItem(AttendanceRecord record, String displayText) {
            this.record = record;
            this.displayText = displayText;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

    private static class CourseItem {
        final String offeringId;
        final String displayName;

        CourseItem(String offeringId, String displayName) {
            this.offeringId = offeringId;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // ==========================================
    // 8. MY PROFILE VIEW (WITH PHOTO UPLOAD)
    // ==========================================
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JLabel sectionTitle = new JLabel("Undergraduate Student Profile");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);
        panel.add(sectionTitle, BorderLayout.NORTH);

        // Center split: Left Card (Photo & Identity) + Right Card (Detailed Info)
        JPanel centerGrid = new JPanel(new BorderLayout(20, 0));
        centerGrid.setBackground(lightBackground);

        // 1. LEFT CARD: Photo & Identity
        JPanel leftCard = new JPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setBackground(Color.WHITE);
        leftCard.setPreferredSize(new Dimension(280, 440));
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));

        String initials = getStudentInitials();
        JLabel profileAvatarLabel = new JLabel(createAvatarIcon(currentStudent.getProfilePicture(), 130, initials));
        profileAvatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton uploadBtn = new JButton("📷  Upload Photo");
        uploadBtn.setFont(new Font("Arial", Font.BOLD, 12));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setBackground(darkBlue);
        uploadBtn.setFocusPainted(false);
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadBtn.setMaximumSize(new Dimension(180, 36));
        uploadBtn.setPreferredSize(new Dimension(180, 36));

        uploadBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                uploadBtn.setBackground(new Color(38, 85, 128));
            }
            public void mouseExited(MouseEvent e) {
                uploadBtn.setBackground(darkBlue);
            }
        });

        uploadBtn.addActionListener(e -> handlePhotoUpload(profileAvatarLabel, headerAvatarLabel));

        JLabel formatHint = new JLabel("Formats: JPG, JPEG, PNG");
        formatHint.setFont(new Font("Arial", Font.PLAIN, 11));
        formatHint.setForeground(new Color(110, 120, 130));
        formatHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel maxHint = new JLabel("Standard photographs or PNG");
        maxHint.setFont(new Font("Arial", Font.PLAIN, 10));
        maxHint.setForeground(new Color(140, 150, 160));
        maxHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(230, 1));
        sep.setForeground(new Color(230, 235, 240));

        JLabel nameLabel = new JLabel(currentStudent.getFullName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(textColor);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel indexLabel = new JLabel(currentStudent.getIndexNo());
        indexLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        indexLabel.setForeground(new Color(100, 110, 120));
        indexLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusPill = new JLabel("  " + currentStudent.getStudentStatus() + " • ACTIVE  ");
        statusPill.setFont(new Font("Arial", Font.BOLD, 11));
        statusPill.setForeground(new Color(20, 165, 145));
        statusPill.setOpaque(true);
        statusPill.setBackground(new Color(230, 247, 244));
        statusPill.setBorder(BorderFactory.createLineBorder(new Color(180, 235, 225), 1, true));
        statusPill.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftCard.add(profileAvatarLabel);
        leftCard.add(Box.createVerticalStrut(14));
        leftCard.add(uploadBtn);
        leftCard.add(Box.createVerticalStrut(6));
        leftCard.add(formatHint);
        leftCard.add(Box.createVerticalStrut(2));
        leftCard.add(maxHint);
        leftCard.add(Box.createVerticalStrut(14));
        leftCard.add(sep);
        leftCard.add(Box.createVerticalStrut(14));
        leftCard.add(nameLabel);
        leftCard.add(Box.createVerticalStrut(4));
        leftCard.add(indexLabel);
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(statusPill);

        // 2. RIGHT CARD: Detailed Information
        JPanel rightCard = new JPanel(new BorderLayout());
        rightCard.setBackground(Color.WHITE);
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(22, 25, 22, 25)
        ));

        JLabel cardHeading = new JLabel("Academic & Personal Information");
        cardHeading.setFont(new Font("Arial", Font.BOLD, 15));
        cardHeading.setForeground(darkBlue);
        cardHeading.setBorder(new EmptyBorder(0, 0, 15, 0));
        rightCard.add(cardHeading, BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(5, 2, 20, 14));
        fieldsGrid.setBackground(Color.WHITE);

        addProfileField(fieldsGrid, "Full Name", currentStudent.getFullName());
        addProfileField(fieldsGrid, "Student User ID", currentStudent.getUserId());
        addProfileField(fieldsGrid, "Student Index No", currentStudent.getIndexNo());
        addProfileField(fieldsGrid, "Department", currentStudent.getDepartment());
        addProfileField(fieldsGrid, "Academic Batch", currentStudent.getBatch());
        addProfileField(fieldsGrid, "Current Academic Level", currentStudent.getAcademicYear());
        addProfileField(fieldsGrid, "Official University Email", currentStudent.getEmail());
        addProfileField(fieldsGrid, "Contact Phone Number", currentStudent.getPhone());
        addProfileField(fieldsGrid, "Permanent Residence", currentStudent.getAddress());
        addProfileField(fieldsGrid, "Student Status", currentStudent.getStudentStatus());

        rightCard.add(fieldsGrid, BorderLayout.CENTER);

        centerGrid.add(leftCard, BorderLayout.WEST);
        centerGrid.add(rightCard, BorderLayout.CENTER);

        panel.add(centerGrid, BorderLayout.CENTER);
        return panel;
    }

    private String getStudentInitials() {
        String f = (currentStudent != null) ? currentStudent.getFirstName() : "S";
        String l = (currentStudent != null) ? currentStudent.getLastName() : "";
        String first = (f != null && !f.isEmpty()) ? f.substring(0, 1).toUpperCase() : "S";
        String last = (l != null && !l.isEmpty()) ? l.substring(0, 1).toUpperCase() : "";
        return first + last;
    }

    private ImageIcon createAvatarIcon(String imagePath, int size, String initials) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        boolean loaded = false;
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File file = new File(imagePath);
            if (file.exists() && file.isFile()) {
                try {
                    BufferedImage raw = ImageIO.read(file);
                    if (raw != null) {
                        g2.setClip(new Ellipse2D.Double(0, 0, size, size));
                        g2.drawImage(raw, 0, 0, size, size, null);
                        g2.setClip(null);

                        g2.setColor(new Color(27, 67, 103));
                        g2.setStroke(new BasicStroke(2.5f));
                        g2.drawOval(1, 1, size - 3, size - 3);
                        loaded = true;
                    }
                } catch (Exception ex) {
                    System.err.println("Could not load avatar: " + ex.getMessage());
                }
            }
        }

        if (!loaded) {
            GradientPaint gp = new GradientPaint(0, 0, new Color(35, 85, 130), size, size, new Color(18, 48, 76));
            g2.setPaint(gp);
            g2.fillOval(0, 0, size, size);

            g2.setColor(new Color(180, 240, 220));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(1, 1, size - 3, size - 3);

            g2.setColor(Color.WHITE);
            int fontSize = Math.max(12, size / 3);
            g2.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(initials);
            int strH = fm.getAscent();
            g2.drawString(initials, (size - strW) / 2, (size + strH) / 2 - 3);
        }

        g2.dispose();
        return new ImageIcon(image);
    }

    private void handlePhotoUpload(JLabel avatarLabel, JLabel headerAvatar) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Photo (JPEG or PNG)");
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png");
        chooser.addChoosableFileFilter(filter);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String name = selectedFile.getName().toLowerCase();

            // Validate format: JPEG (.jpg, .jpeg) and PNG (.png)
            if (!name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".png")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Unsupported format! Please select a JPEG (.jpg, .jpeg) or PNG (.png) file.",
                        "Invalid Format",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Size limit: 5MB
            if (selectedFile.length() > 5 * 1024 * 1024) {
                JOptionPane.showMessageDialog(
                        this,
                        "File exceeds 5 MB limit. Please choose a smaller image.",
                        "File Too Large",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                File uploadDir = new File("uploads/profiles");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String ext = name.substring(name.lastIndexOf('.'));
                String targetFileName = currentStudent.getUserId() + "_" + System.currentTimeMillis() + ext;
                File targetFile = new File(uploadDir, targetFileName);

                Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String savedPath = targetFile.getPath().replace("\\", "/");
                boolean updated = studentDAO.updateProfilePicture(currentStudent.getUserId(), savedPath);

                if (updated) {
                    currentStudent.setProfilePicture(savedPath);

                    String initials = getStudentInitials();
                    avatarLabel.setIcon(createAvatarIcon(savedPath, 130, initials));
                    if (headerAvatar != null) {
                        headerAvatar.setIcon(createAvatarIcon(savedPath, 36, initials));
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            "Profile photo updated successfully!",
                            "Upload Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Database error: could not save photo path.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error saving image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        }
    }

    private void addProfileField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(new Color(110, 120, 130));

        JLabel val = new JLabel((value != null && !value.isEmpty()) ? value : "Not specified");
        val.setFont(new Font("Arial", Font.BOLD, 14));
        val.setForeground(textColor);

        fieldPanel.add(lbl);
        fieldPanel.add(Box.createVerticalStrut(3));
        fieldPanel.add(val);

        panel.add(fieldPanel);
    }

    // ==========================================
    // UI HELPERS & COMPONENT BUILDERS
    // ==========================================

    // Create menu button with user's styling & hover effects
    private void addMenuButton(JPanel panel, String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setForeground(navTextColor);
        button.setBackground(sidebarColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!button.getBackground().equals(navActiveColor)) {
                    button.setBackground(navHoverColor);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (!button.getBackground().equals(navActiveColor)) {
                    button.setBackground(sidebarColor);
                }
            }
        });

        // Button action - switch views seamlessly and support popup frame
        button.addActionListener(e -> {
            String selected = button.getText().trim();
            highlightMenuButton(selected);
            contentCardLayout.show(dynamicViewsPanel, selected);
        });

        menuButtons.add(button);
        panel.add(button);
    }

    private void highlightMenuButton(String selectedText) {
        for (JButton b : menuButtons) {
            if (b.getText().trim().equals(selectedText)) {
                b.setBackground(navActiveColor);
                b.setFont(new Font("Arial", Font.BOLD, 13));
                b.setForeground(Color.WHITE);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, navAccentColor),
                        BorderFactory.createEmptyBorder(12, 14, 12, 10)
                ));
            } else {
                b.setBackground(sidebarColor);
                b.setFont(new Font("Arial", Font.PLAIN, 13));
                b.setForeground(navTextColor);
                b.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 10));
            }
        }
    }

    // Create dashboard card (user's exact design)
    private JPanel createCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 110, 120));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 25));
        valueLabel.setForeground(valueColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    // Add activity bullet (user's design)
    private void addActivity(JPanel panel, String text) {
        JLabel activity = new JLabel("•  " + text);
        activity.setFont(new Font("Arial", Font.PLAIN, 13));
        activity.setForeground(new Color(70, 80, 90));
        activity.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        panel.add(activity);
    }

    // Add notice item
    private void addNoticeItem(JPanel panel, String title, String body) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));

        JLabel tLabel = new JLabel("📢  " + title);
        tLabel.setFont(new Font("Arial", Font.BOLD, 12));
        tLabel.setForeground(textColor);

        JLabel bLabel = new JLabel("<html><body style='width: 320px'>" + body + "</body></html>");
        bLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        bLabel.setForeground(new Color(110, 120, 130));

        item.add(tLabel);
        item.add(Box.createVerticalStrut(2));
        item.add(bLabel);
        panel.add(item);
    }

    // Helper for beautiful consistent tables
    private JTable createStandardTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setGridColor(new Color(235, 238, 242));
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setSelectionForeground(textColor);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 244, 248));
        tableHeader.setForeground(textColor);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 38));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i == 2 || i >= table.getColumnCount() - 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(center);
            }
        }
        return table;
    }

    // Main method to test and launch StudentDashboard directly
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new StudentDashboard();
        });
    }
}
