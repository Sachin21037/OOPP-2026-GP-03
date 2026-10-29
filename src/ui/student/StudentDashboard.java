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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
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

    // Announcements & Notifications state
    private NotificationBellButton notificationBtn;
    private JPanel announcementsListContainer;
    private List<Notice> allAnnouncementsList = new ArrayList<>();
    private String currentAnnouncementFilter = "ALL";
    private String currentAnnouncementSearch = "";
    private JLabel announcementsCountLabel;
    private JLabel quizzesCountLabel;
    private JLabel assignmentsCountLabel;
    private final List<JButton> filterButtons = new ArrayList<>();

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

        boolean isLec = currentStudent != null && "LECTURER".equalsIgnoreCase(currentStudent.getRole());

        String badgeId = (isLec ? currentStudent.getUserId() : currentStudent.getIndexNo());
        JLabel studentBadge = new JLabel(currentStudent.getFirstName() + " (" + (badgeId != null ? badgeId : currentStudent.getUserId()) + ")  ");
        studentBadge.setForeground(Color.WHITE);
        studentBadge.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel userType = new JLabel(isLec ? "LECTURER  " : "STUDENT  ");
        userType.setForeground(new Color(180, 240, 220));
        userType.setFont(new Font("Arial", Font.BOLD, 13));

        notificationBtn = new NotificationBellButton();
        notificationBtn.setToolTipText("View Notifications & Notices");
        notificationBtn.addActionListener(e -> showNotificationsPopup(notificationBtn));

        headerRight.add(notificationBtn);
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

        JLabel menuTitle = new JLabel(isLecturer() ? "  LECTURER MENU" : "  STUDENT MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 12));
        menuTitle.setForeground(new Color(170, 215, 245));
        menuTitle.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 0));

        menuPanel.add(menuTitle);

        // Menu buttons
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "Announcements");
        addMenuButton(menuPanel, "My Courses");
        addMenuButton(menuPanel, "My Results");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Assignments");
        addMenuButton(menuPanel, "Course Materials");
        addMenuButton(menuPanel, "Medical Submissions");
        addMenuButton(menuPanel, "My Profile");

        sidebar.add(menuPanel, BorderLayout.NORTH);

        // Logout
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        logoutPanel.setBackground(sidebarColor);

        JButton logoutButton = new JButton("🚪  Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 13));
        logoutButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        logoutButton.setOpaque(true);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(220, 38, 38)); // Vibrant danger red
        logoutButton.setBorder(new CompoundBorder(
                new LineBorder(new Color(185, 28, 28), 1, true),
                new EmptyBorder(8, 24, 8, 24)
        ));
        logoutButton.setPreferredSize(new Dimension(165, 38));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(new Color(239, 68, 68)); // Lighter red hover
            }

            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(new Color(220, 38, 38));
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
        dynamicViewsPanel.add(buildAnnouncementsPanel(), "Announcements");
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
        openWindowBtn.setFont(new Font("Arial", Font.BOLD, 12));
        openWindowBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        openWindowBtn.setOpaque(true);
        openWindowBtn.setBackground(new Color(29, 78, 216));
        openWindowBtn.setForeground(Color.WHITE);
        openWindowBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(7, 16, 7, 16)
        ));
        openWindowBtn.setFocusPainted(false);
        openWindowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openWindowBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                openWindowBtn.setBackground(new Color(37, 99, 235));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                openWindowBtn.setBackground(new Color(29, 78, 216));
            }
        });
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
        String[] columns = {"Date", "Course", "Session Type", "Session", "Topic", "Attendance Status"};
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
        submitNewBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        submitNewBtn.setOpaque(true);
        submitNewBtn.setForeground(Color.WHITE);
        submitNewBtn.setBackground(new Color(29, 78, 216));
        submitNewBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        submitNewBtn.setFocusPainted(false);
        submitNewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitNewBtn.setPreferredSize(new Dimension(235, 38));

        submitNewBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                submitNewBtn.setBackground(new Color(37, 99, 235));
            }
            public void mouseExited(MouseEvent e) {
                submitNewBtn.setBackground(new Color(29, 78, 216));
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
        submitSelectedBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        submitSelectedBtn.setOpaque(true);
        submitSelectedBtn.setBackground(new Color(29, 78, 216));
        submitSelectedBtn.setForeground(Color.WHITE);
        submitSelectedBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        submitSelectedBtn.setFocusPainted(false);
        submitSelectedBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitSelectedBtn.setEnabled(false);
        submitSelectedBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (submitSelectedBtn.isEnabled()) {
                    submitSelectedBtn.setBackground(new Color(37, 99, 235));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (submitSelectedBtn.isEnabled()) {
                    submitSelectedBtn.setBackground(new Color(29, 78, 216));
                }
            }
        });

        absentActions.add(absentCountBadge);
        absentActions.add(submitSelectedBtn);

        absentHeader.add(absentTitlePanel, BorderLayout.WEST);
        absentHeader.add(absentActions, BorderLayout.EAST);
        absentCard.add(absentHeader, BorderLayout.NORTH);

        String[] absentColumns = {"Course Code", "Course Name", "Session Date", "Session Type", "Session", "Lecture Topic", "Attendance", "Medical Status"};
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

        JLabel historySubTitle = new JLabel("Review submissions, edit specific days taken off for pending entries, or track technical officer reviews.");
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

        // Edit Entry button
        JButton editLeaveBtn = new JButton("✏️ Edit Selected Entry");
        editLeaveBtn.setFont(new Font("Arial", Font.BOLD, 11));
        editLeaveBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        editLeaveBtn.setOpaque(true);
        editLeaveBtn.setBackground(new Color(29, 78, 216));
        editLeaveBtn.setForeground(Color.WHITE);
        editLeaveBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        editLeaveBtn.setFocusPainted(false);
        editLeaveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editLeaveBtn.setEnabled(false);
        editLeaveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (editLeaveBtn.isEnabled()) {
                    editLeaveBtn.setBackground(new Color(37, 99, 235));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (editLeaveBtn.isEnabled()) {
                    editLeaveBtn.setBackground(new Color(29, 78, 216));
                }
            }
        });

        JPanel historyRightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        historyRightBar.setBackground(Color.WHITE);
        historyRightBar.add(summaryBar);
        historyRightBar.add(new JSeparator(SwingConstants.VERTICAL));
        historyRightBar.add(editLeaveBtn);

        historyHeader.add(historyTitlePanel, BorderLayout.WEST);
        historyHeader.add(historyRightBar, BorderLayout.EAST);
        historyCard.add(historyHeader, BorderLayout.NORTH);

        String[] historyColumns = {"Medical ID", "Course", "Days Taken Off", "Reason / Medical Condition", "Supporting Document", "Status", "Review Remarks"};
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

        // Store records in memory for table interaction
        List<AttendanceRecord> currentAbsentRecords = new ArrayList<>();
        List<Medical> currentMedicalList = new ArrayList<>();

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
            currentMedicalList.clear();
            List<Medical> list = studentDAO.getStudentMedicals(currentStudent.getUserId());
            currentMedicalList.addAll(list);
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
                        m.getDaysTakenOffDisplay(),
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
            editLeaveBtn.setEnabled(false);
        };

        // Table selection handler for absent table
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
                        openMedicalSubmissionDialog(reloadMedicals, currentAbsentRecords.get(row), null);
                    }
                }
            }
        });

        // Submit for selected button
        submitSelectedBtn.addActionListener(e -> {
            int row = absentTable.getSelectedRow();
            if (row >= 0 && row < currentAbsentRecords.size()) {
                openMedicalSubmissionDialog(reloadMedicals, currentAbsentRecords.get(row), null);
            }
        });

        // History table selection handler
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = historyTable.getSelectedRow();
                editLeaveBtn.setEnabled(row >= 0 && row < currentMedicalList.size());
            }
        });

        // Action for editing selected history entry
        editLeaveBtn.addActionListener(e -> {
            int row = historyTable.getSelectedRow();
            if (row >= 0 && row < currentMedicalList.size()) {
                Medical m = currentMedicalList.get(row);
                if ("APPROVED".equalsIgnoreCase(m.getApprovalStatus())) {
                    JOptionPane.showMessageDialog(this,
                            "This medical certificate has already been APPROVED and cannot be modified.\nPlease contact administration if you need changes.",
                            "Entry Already Approved", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                openMedicalSubmissionDialog(reloadMedicals, null, m);
            }
        });

        // Double-click on history entry opens it for editing
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = historyTable.getSelectedRow();
                    if (row >= 0 && row < currentMedicalList.size()) {
                        Medical m = currentMedicalList.get(row);
                        if ("APPROVED".equalsIgnoreCase(m.getApprovalStatus())) {
                            JOptionPane.showMessageDialog(StudentDashboard.this,
                                    "This medical certificate has already been APPROVED and cannot be modified.\nPlease contact administration if you need changes.",
                                    "Entry Already Approved", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        openMedicalSubmissionDialog(reloadMedicals, null, m);
                    }
                }
            }
        });

        // New submission button (general)
        submitNewBtn.addActionListener(e -> openMedicalSubmissionDialog(reloadMedicals, null, null));

        reloadMedicals.run();
        return panel;
    }

    private void openMedicalSubmissionDialog(Runnable onSuccess) {
        openMedicalSubmissionDialog(onSuccess, null, null);
    }

    private void openMedicalSubmissionDialog(Runnable onSuccess, AttendanceRecord initialRecord) {
        openMedicalSubmissionDialog(onSuccess, initialRecord, null);
    }

    private void openMedicalSubmissionDialog(Runnable onSuccess, AttendanceRecord initialRecord, Medical existingMedicalToEdit) {
        boolean isEditMode = (existingMedicalToEdit != null);
        String dialogTitle = isEditMode ? "Edit Medical Leave Entry - " + existingMedicalToEdit.getMedicalId() : "Submit Medical Leave Certificate";
        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setSize(640, 720);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel(isEditMode ? "Edit Medical Leave Entry (" + existingMedicalToEdit.getMedicalId() + ")" : "New Medical Leave Certificate Submission");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(isEditMode ? "Modify the specific days taken off, reason, or supporting certificate document" : "Select and edit the specific days taken off to excuse recorded absences");
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
        // 1. SELECT FROM RECORDED ABSENT LECTURES (OPTIONAL AUTOFILL)
        // -------------------------------------------------------------
        JLabel absentLbl = new JLabel("Quick Fill from Recorded Absent Lectures:");
        absentLbl.setFont(new Font("Arial", Font.BOLD, 12));
        absentLbl.setForeground(textColor);

        List<AttendanceRecord> absentSessions = studentDAO.getAbsentSessions(currentStudent.getUserId());
        JComboBox<AbsentSessionItem> absentCombo = new JComboBox<>();
        absentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        absentCombo.setPreferredSize(new Dimension(560, 36));

        if (absentSessions.isEmpty()) {
            absentCombo.addItem(new AbsentSessionItem(null, "-- No absent sessions recorded (Manual Entry) --"));
        } else {
            absentCombo.addItem(new AbsentSessionItem(null, "-- Select an absent lecture to autofill days taken off -- (" + absentSessions.size() + " recorded absences)"));
            for (AttendanceRecord a : absentSessions) {
                String label = a.getCourseCode() + " | " + a.getSessionDate() + " | " + a.getSessionType() + " #" + a.getSessionNumber() +
                        " - " + (a.getTopic() != null ? a.getTopic() : "Lecture") +
                        (a.getMedicalStatus() != null ? " [" + a.getMedicalStatus() + "]" : " [ABSENT]");
                absentCombo.addItem(new AbsentSessionItem(a, label));
            }
            absentCombo.addItem(new AbsentSessionItem(null, "-- Manual / Custom Leave Entry --"));
        }

        // -------------------------------------------------------------
        // 2. ENROLLED COURSE OFFERING
        // -------------------------------------------------------------
        JLabel courseLbl = new JLabel("Enrolled Course Offering *");
        courseLbl.setFont(new Font("Arial", Font.BOLD, 12));
        courseLbl.setForeground(textColor);

        List<Course> courses = studentDAO.getEnrolledCourses(currentStudent.getUserId());
        JComboBox<CourseItem> courseCombo = new JComboBox<>();
        courseCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        courseCombo.setPreferredSize(new Dimension(560, 36));
        for (Course c : courses) {
            courseCombo.addItem(new CourseItem(c.getOfferingId(), c.getCourseCode() + " - " + c.getCourseName()));
        }

        // -------------------------------------------------------------
        // 3. SPECIFIC DAYS TAKEN OFF (EDITABLE DATE RANGE / PRESETS)
        // -------------------------------------------------------------
        JPanel daysPanel = new JPanel();
        daysPanel.setLayout(new BoxLayout(daysPanel, BoxLayout.Y_AXIS));
        daysPanel.setBackground(new Color(248, 250, 252));
        daysPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238), 1),
                new EmptyBorder(12, 14, 12, 14)
        ));
        daysPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel daysHeader = new JLabel("Specific Days Taken Off / Medical Leave Period *");
        daysHeader.setFont(new Font("Arial", Font.BOLD, 12));
        daysHeader.setForeground(textColor);

        // Leave Mode Radios
        JRadioButton singleDayRadio = new JRadioButton("Single Day Taken Off", true);
        singleDayRadio.setFont(new Font("Arial", Font.PLAIN, 12));
        singleDayRadio.setBackground(new Color(248, 250, 252));

        JRadioButton multiDayRadio = new JRadioButton("Multiple Days Taken Off (Date Range)", false);
        multiDayRadio.setFont(new Font("Arial", Font.PLAIN, 12));
        multiDayRadio.setBackground(new Color(248, 250, 252));

        ButtonGroup dayGroup = new ButtonGroup();
        dayGroup.add(singleDayRadio);
        dayGroup.add(multiDayRadio);

        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        radioRow.setBackground(new Color(248, 250, 252));
        radioRow.add(singleDayRadio);
        radioRow.add(multiDayRadio);

        // Date input fields (Side-by-Side)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String defaultDate = sdf.format(new java.util.Date());
        if (isEditMode && existingMedicalToEdit.getStartDate() != null) {
            defaultDate = sdf.format(existingMedicalToEdit.getStartDate());
        }

        JTextField startDateField = new JTextField(defaultDate);
        startDateField.setPreferredSize(new Dimension(240, 34));

        String defaultEndDate = defaultDate;
        if (isEditMode && existingMedicalToEdit.getEndDate() != null) {
            defaultEndDate = sdf.format(existingMedicalToEdit.getEndDate());
        }
        JTextField endDateField = new JTextField(defaultEndDate);
        endDateField.setPreferredSize(new Dimension(240, 34));
        endDateField.setEnabled(false);

        JPanel dateFieldsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        dateFieldsRow.setBackground(new Color(248, 250, 252));

        JPanel startBox = new JPanel(new BorderLayout(0, 4));
        startBox.setBackground(new Color(248, 250, 252));
        JLabel startLbl = new JLabel("From Date / Start Date (YYYY-MM-DD) *");
        startLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        startLbl.setForeground(new Color(75, 85, 99));
        startBox.add(startLbl, BorderLayout.NORTH);
        startBox.add(startDateField, BorderLayout.CENTER);

        JPanel endBox = new JPanel(new BorderLayout(0, 4));
        endBox.setBackground(new Color(248, 250, 252));
        JLabel endLbl = new JLabel("To Date / End Date (YYYY-MM-DD) *");
        endLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        endLbl.setForeground(new Color(75, 85, 99));
        endBox.add(endLbl, BorderLayout.NORTH);
        endBox.add(endDateField, BorderLayout.CENTER);

        dateFieldsRow.add(startBox);
        dateFieldsRow.add(endBox);

        // Quick Day Presets Row
        JPanel presetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        presetRow.setBackground(new Color(248, 250, 252));
        JLabel presetLbl = new JLabel("Quick Presets:");
        presetLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        presetLbl.setForeground(new Color(100, 110, 120));
        presetRow.add(presetLbl);

        int[] presetDays = {1, 2, 3, 5, 7};
        String[] presetNames = {"1 Day", "2 Days", "3 Days", "5 Days", "1 Week"};
        for (int i = 0; i < presetDays.length; i++) {
            final int daysCount = presetDays[i];
            JButton pBtn = new JButton(presetNames[i]);
            pBtn.setFont(new Font("Arial", Font.BOLD, 11));
            pBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            pBtn.setOpaque(true);
            pBtn.setBackground(new Color(29, 78, 216));
            pBtn.setForeground(Color.WHITE);
            pBtn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(30, 64, 175), 1, true),
                    new EmptyBorder(4, 10, 4, 10)
            ));
            pBtn.setFocusPainted(false);
            pBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    pBtn.setBackground(new Color(37, 99, 235));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    pBtn.setBackground(new Color(29, 78, 216));
                }
            });
            pBtn.addActionListener(e -> {
                try {
                    java.util.Date s = sdf.parse(startDateField.getText().trim());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(s);
                    cal.add(Calendar.DAY_OF_MONTH, daysCount - 1);
                    endDateField.setText(sdf.format(cal.getTime()));
                    if (daysCount == 1) {
                        singleDayRadio.setSelected(true);
                        endDateField.setEnabled(false);
                    } else {
                        multiDayRadio.setSelected(true);
                        endDateField.setEnabled(true);
                    }
                } catch (Exception ex) {
                    // Ignore date parse issues during quick click
                }
            });
            presetRow.add(pBtn);
        }

        // Live calculation summary label
        JLabel daysSummaryLabel = new JLabel("📅 Days Taken Off: 1 day (" + defaultDate + ")");
        daysSummaryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        daysSummaryLabel.setForeground(new Color(30, 64, 175));

        JPanel summaryCard = new JPanel(new BorderLayout());
        summaryCard.setBackground(new Color(239, 246, 255));
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        summaryCard.add(daysSummaryLabel, BorderLayout.CENTER);

        // Helper to update days summary live
        Runnable updateDaysSummary = () -> {
            try {
                String sStr = startDateField.getText().trim();
                String eStr = singleDayRadio.isSelected() ? sStr : endDateField.getText().trim();
                java.util.Date sDate = sdf.parse(sStr);
                java.util.Date eDate = sdf.parse(eStr);
                long diff = (eDate.getTime() - sDate.getTime()) / (1000L * 60 * 60 * 24) + 1;
                if (diff < 1) {
                    daysSummaryLabel.setText("⚠️ Invalid date range: End date must be on or after Start date!");
                    daysSummaryLabel.setForeground(new Color(220, 38, 38));
                    return;
                }

                // Check covered absences
                int coveredCount = 0;
                for (AttendanceRecord a : absentSessions) {
                    if (a.getSessionDate() != null && !a.getSessionDate().before(new java.sql.Date(sDate.getTime())) && !a.getSessionDate().after(new java.sql.Date(eDate.getTime()))) {
                        coveredCount++;
                    }
                }

                String durationText = (diff == 1)
                        ? "1 day (" + sStr + ")"
                        : diff + " calendar days (" + sStr + " to " + eStr + ")";

                String coveredText = (coveredCount > 0)
                        ? " &nbsp;|&nbsp; <span style='color:#15803D;'><b>✓ Covers " + coveredCount + " recorded absent lecture(s)</b></span>"
                        : "";

                daysSummaryLabel.setText("<html>📅 <b>Days Taken Off:</b> " + durationText + coveredText + "</html>");
                daysSummaryLabel.setForeground(new Color(30, 64, 175));
            } catch (Exception ex) {
                daysSummaryLabel.setText("📅 Please enter valid dates in YYYY-MM-DD format");
                daysSummaryLabel.setForeground(new Color(156, 163, 175));
            }
        };

        // Radio listeners
        singleDayRadio.addActionListener(e -> {
            endDateField.setText(startDateField.getText().trim());
            endDateField.setEnabled(false);
            updateDaysSummary.run();
        });

        multiDayRadio.addActionListener(e -> {
            endDateField.setEnabled(true);
            updateDaysSummary.run();
        });

        // Document listeners for live updates
        DocumentListener dateDocListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if (singleDayRadio.isSelected()) {
                    SwingUtilities.invokeLater(() -> endDateField.setText(startDateField.getText().trim()));
                }
                updateDaysSummary.run();
            }
            public void removeUpdate(DocumentEvent e) {
                if (singleDayRadio.isSelected()) {
                    SwingUtilities.invokeLater(() -> endDateField.setText(startDateField.getText().trim()));
                }
                updateDaysSummary.run();
            }
            public void changedUpdate(DocumentEvent e) {
                updateDaysSummary.run();
            }
        };

        startDateField.getDocument().addDocumentListener(dateDocListener);
        endDateField.getDocument().addDocumentListener(dateDocListener);

        daysPanel.add(daysHeader);
        daysPanel.add(Box.createVerticalStrut(6));
        daysPanel.add(radioRow);
        daysPanel.add(Box.createVerticalStrut(8));
        daysPanel.add(dateFieldsRow);
        daysPanel.add(Box.createVerticalStrut(8));
        daysPanel.add(presetRow);
        daysPanel.add(Box.createVerticalStrut(8));
        daysPanel.add(summaryCard);

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
        if (isEditMode && existingMedicalToEdit.getReason() != null) {
            reasonArea.setText(existingMedicalToEdit.getReason());
        }
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        reasonScroll.setPreferredSize(new Dimension(560, 70));

        // -------------------------------------------------------------
        // 5. FILE ATTACHMENT
        // -------------------------------------------------------------
        JLabel attachLbl = new JLabel(isEditMode ? "Supporting Medical Certificate (Upload new to replace or keep current) *" : "Medical Certificate Document (PDF, JPG, PNG) *");
        attachLbl.setFont(new Font("Arial", Font.BOLD, 12));
        attachLbl.setForeground(textColor);

        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        fileRow.setBackground(Color.WHITE);
        fileRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        String initialFileName = isEditMode && existingMedicalToEdit.getDocumentPath() != null
                ? new File(existingMedicalToEdit.getDocumentPath()).getName() + " (Current document attached)"
                : "No document chosen";
        JTextField fileField = new JTextField(initialFileName);
        fileField.setEditable(false);
        fileField.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton chooseBtn = new JButton("Browse Document...");
        chooseBtn.setFont(new Font("Arial", Font.BOLD, 12));
        chooseBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        chooseBtn.setOpaque(true);
        chooseBtn.setBackground(new Color(29, 78, 216));
        chooseBtn.setForeground(Color.WHITE);
        chooseBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        chooseBtn.setFocusPainted(false);
        chooseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chooseBtn.setBackground(new Color(37, 99, 235));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                chooseBtn.setBackground(new Color(29, 78, 216));
            }
        });

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

        // Quick fill action when absentCombo selection changes
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

                // Set date taken off
                if (rec.getSessionDate() != null) {
                    String dStr = sdf.format(rec.getSessionDate());
                    startDateField.setText(dStr);
                    endDateField.setText(dStr);
                }

                // Suggest reason if empty or placeholder
                String curReason = reasonArea.getText().trim();
                if (curReason.isEmpty() || curReason.startsWith("Medical absence for")) {
                    reasonArea.setText("Medical absence for " + rec.getCourseCode() + " " + rec.getSessionType() +
                            " Session " + rec.getSessionNumber() + " (" + (rec.getTopic() != null ? rec.getTopic() : "Lecture") + ") due to illness.");
                }

                updateDaysSummary.run();
            }
        });

        // Assemble body
        body.add(absentLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(absentCombo);
        body.add(Box.createVerticalStrut(12));
        body.add(courseLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(courseCombo);
        body.add(Box.createVerticalStrut(12));
        body.add(daysPanel);
        body.add(Box.createVerticalStrut(12));
        body.add(reasonLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(reasonScroll);
        body.add(Box.createVerticalStrut(12));
        body.add(attachLbl);
        body.add(Box.createVerticalStrut(5));
        body.add(fileRow);

        // Initialize state for Edit Mode or Initial Absent Record
        if (isEditMode) {
            // Select course
            for (int i = 0; i < courseCombo.getItemCount(); i++) {
                CourseItem ci = courseCombo.getItemAt(i);
                if (ci.offeringId.equals(existingMedicalToEdit.getOfferingId())) {
                    courseCombo.setSelectedIndex(i);
                    break;
                }
            }
            if (existingMedicalToEdit.getStartDate() != null && existingMedicalToEdit.getEndDate() != null && !existingMedicalToEdit.getStartDate().equals(existingMedicalToEdit.getEndDate())) {
                multiDayRadio.setSelected(true);
                endDateField.setEnabled(true);
            } else {
                singleDayRadio.setSelected(true);
                endDateField.setEnabled(false);
            }
        } else if (initialRecord != null) {
            for (int i = 0; i < absentCombo.getItemCount(); i++) {
                AbsentSessionItem item = absentCombo.getItemAt(i);
                if (item.record != null && item.record.getSessionId().equals(initialRecord.getSessionId())) {
                    absentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        updateDaysSummary.run();

        // Wrap body in scroll pane
        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        cancelBtn.setOpaque(true);
        cancelBtn.setBackground(new Color(219, 234, 254));
        cancelBtn.setForeground(new Color(29, 78, 216));
        cancelBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(147, 197, 253), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelBtn.setBackground(new Color(191, 219, 254));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cancelBtn.setBackground(new Color(219, 234, 254));
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        String submitBtnTitle = isEditMode ? "💾 Save Changes & Update Entry" : "Submit Medical Certificate";
        JButton submitBtn = new JButton(submitBtnTitle);
        submitBtn.setFont(new Font("Arial", Font.BOLD, 12));
        submitBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        submitBtn.setOpaque(true);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(new Color(29, 78, 216));
        submitBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(8, 18, 8, 18)
        ));
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                submitBtn.setBackground(new Color(37, 99, 235));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                submitBtn.setBackground(new Color(29, 78, 216));
            }
        });

        submitBtn.addActionListener(e -> {
            CourseItem selectedCourse = (CourseItem) courseCombo.getSelectedItem();
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an enrolled course.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String startStr = startDateField.getText().trim();
            String endStr = singleDayRadio.isSelected() ? startStr : endDateField.getText().trim();
            java.sql.Date sqlStartDate;
            java.sql.Date sqlEndDate;
            try {
                java.util.Date pStart = sdf.parse(startStr);
                java.util.Date pEnd = sdf.parse(endStr);
                if (pEnd.before(pStart)) {
                    JOptionPane.showMessageDialog(dialog, "End date cannot be earlier than Start date!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sqlStartDate = new java.sql.Date(pStart.getTime());
                sqlEndDate = new java.sql.Date(pEnd.getTime());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format! Please enter dates in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please provide the reason / medical diagnosis.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!isEditMode && chosenFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please attach a medical certificate file (PDF, JPG, PNG).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String docPath = null;
            if (chosenFile[0] != null) {
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
            } else if (isEditMode) {
                docPath = existingMedicalToEdit.getDocumentPath();
            }

            if (isEditMode) {
                existingMedicalToEdit.setOfferingId(selectedCourse.offeringId);
                existingMedicalToEdit.setStartDate(sqlStartDate);
                existingMedicalToEdit.setEndDate(sqlEndDate);
                existingMedicalToEdit.setMedicalDate(sqlStartDate);
                existingMedicalToEdit.setReason(reason);
                if (docPath != null) {
                    existingMedicalToEdit.setDocumentPath(docPath);
                }

                boolean ok = studentDAO.updateMedical(existingMedicalToEdit);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                            "Medical leave entry " + existingMedicalToEdit.getMedicalId() + " updated successfully!\n" +
                            "Specific days taken off: " + existingMedicalToEdit.getDaysTakenOffDisplay(),
                            "Entry Updated", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Database error: could not update medical leave record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                Medical m = new Medical();
                m.setStudentId(currentStudent.getUserId());
                m.setOfferingId(selectedCourse.offeringId);
                m.setStartDate(sqlStartDate);
                m.setEndDate(sqlEndDate);
                m.setMedicalDate(sqlStartDate);
                m.setReason(reason);
                m.setDocumentPath(docPath);
                m.setRemarks("Submitted by student. Awaiting Technical Officer verification.");

                boolean ok = studentDAO.submitMedical(m);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                            "Medical leave certificate submitted successfully!\n" +
                            "Reference ID: " + m.getMedicalId() + "\n" +
                            "Specific days taken off: " + m.getDaysTakenOffDisplay(),
                            "Submission Successful", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Database error: could not save medical certificate record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
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
    // 8. MY PROFILE VIEW (WITH PHOTO UPLOAD)
    // ==========================================
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(lightBackground);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("My Profile & Academic Credentials");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 19));
        sectionTitle.setForeground(darkBlue);

        JLabel sectionSub = new JLabel("Official registration details, academic standing, and identity management");
        sectionSub.setFont(new Font("Arial", Font.PLAIN, 12));
        sectionSub.setForeground(new Color(100, 116, 139));

        titleBlock.add(sectionTitle);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(sectionSub);

        JLabel topBadge = new JLabel("  ● ACTIVE ENROLLMENT  ");
        topBadge.setFont(new Font("Arial", Font.BOLD, 11));
        topBadge.setForeground(new Color(16, 185, 129));
        topBadge.setBackground(new Color(236, 253, 245));
        topBadge.setOpaque(true);
        topBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(167, 243, 208), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(topBadge, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center split: Left Card (Photo & Identity) + Right Card (Detailed Info)
        JPanel centerGrid = new JPanel(new BorderLayout(20, 0));
        centerGrid.setBackground(lightBackground);

        // 1. LEFT CARD: Photo & Identity & Quick Stats
        JPanel leftCard = new JPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setBackground(Color.WHITE);
        leftCard.setPreferredSize(new Dimension(275, 480));
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 16, 18, 16)
        ));

        String initials = getStudentInitials();
        JLabel profileAvatarLabel = new JLabel(createAvatarIcon(currentStudent.getProfilePicture(), 125, initials));
        profileAvatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(currentStudent.getFullName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(15, 23, 42));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String idText = (currentStudent.getIndexNo() != null && !currentStudent.getIndexNo().isEmpty())
                ? currentStudent.getIndexNo() : currentStudent.getUserId();
        JLabel indexLabel = new JLabel("  " + idText + "  ");
        indexLabel.setFont(new Font("Arial", Font.BOLD, 12));
        indexLabel.setForeground(new Color(30, 64, 175));
        indexLabel.setBackground(new Color(239, 246, 255));
        indexLabel.setOpaque(true);
        indexLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        indexLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String roleStatus = (isLecturer() ? "Faculty Member • Active" : (currentStudent.getStudentStatus() != null ? currentStudent.getStudentStatus() + " Undergraduate" : "Undergraduate"));
        JLabel roleLabel = new JLabel(roleStatus);
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(100, 116, 139));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton uploadBtn = new JButton("📷  Update Photo");
        uploadBtn.setFont(new Font("Arial", Font.BOLD, 12));
        uploadBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        uploadBtn.setOpaque(true);
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setBackground(new Color(29, 78, 216)); // Royal Blue
        uploadBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(7, 16, 7, 16)
        ));
        uploadBtn.setFocusPainted(false);
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadBtn.setMaximumSize(new Dimension(185, 36));
        uploadBtn.setPreferredSize(new Dimension(185, 36));

        uploadBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                uploadBtn.setBackground(new Color(37, 99, 235));
            }
            public void mouseExited(MouseEvent e) {
                uploadBtn.setBackground(new Color(29, 78, 216));
            }
        });
        uploadBtn.addActionListener(e -> handlePhotoUpload(profileAvatarLabel, headerAvatarLabel));

        JLabel formatHint = new JLabel("JPG, JPEG or PNG • Max 5MB");
        formatHint.setFont(new Font("Arial", Font.PLAIN, 10));
        formatHint.setForeground(new Color(148, 163, 184));
        formatHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(235, 1));
        sep.setForeground(new Color(226, 232, 240));

        // Mini Quick Stats Panel (Courses, Attendance, GPA)
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(245, 54));

        int enrolled = currentStudent.getEnrolledCourseCount() > 0 ? currentStudent.getEnrolledCourseCount() : 9;
        double att = currentStudent.getAttendancePercentage() > 0 ? currentStudent.getAttendancePercentage() : 87.0;
        double gpa = currentStudent.getCurrentGpa() > 0 ? currentStudent.getCurrentGpa() : 2.42;

        statsPanel.add(createMiniStatBox("Courses", String.valueOf(enrolled), new Color(45, 100, 160)));
        statsPanel.add(createMiniStatBox("Attendance", String.format("%.0f%%", att), new Color(20, 165, 145)));
        statsPanel.add(createMiniStatBox("GPA", String.format("%.2f", gpa), new Color(115, 75, 180)));

        leftCard.add(profileAvatarLabel);
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(nameLabel);
        leftCard.add(Box.createVerticalStrut(5));
        leftCard.add(indexLabel);
        leftCard.add(Box.createVerticalStrut(4));
        leftCard.add(roleLabel);
        leftCard.add(Box.createVerticalStrut(12));
        leftCard.add(uploadBtn);
        leftCard.add(Box.createVerticalStrut(4));
        leftCard.add(formatHint);
        leftCard.add(Box.createVerticalStrut(12));
        leftCard.add(sep);
        leftCard.add(Box.createVerticalStrut(12));
        leftCard.add(statsPanel);

        // 2. RIGHT CARD: Categorized Profile Information
        JPanel rightCard = new JPanel(new BorderLayout());
        rightCard.setBackground(Color.WHITE);
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel categoriesContainer = new JPanel();
        categoriesContainer.setLayout(new BoxLayout(categoriesContainer, BoxLayout.Y_AXIS));
        categoriesContainer.setOpaque(false);

        // Category 1: Academic & University Records
        categoriesContainer.add(createCategorySectionHeader("🎓  Academic Registration & Department", "Faculty Records"));
        categoriesContainer.add(Box.createVerticalStrut(8));

        JPanel academicGrid = new JPanel(new GridLayout(3, 2, 12, 10));
        academicGrid.setOpaque(false);

        academicGrid.add(createProfileFieldTile("Student Index No", currentStudent.getIndexNo()));
        academicGrid.add(createProfileFieldTile("Student User ID", currentStudent.getUserId()));
        academicGrid.add(createProfileFieldTile("Department / Faculty", currentStudent.getDepartment()));
        academicGrid.add(createProfileFieldTile("Academic Level", currentStudent.getAcademicYear()));
        academicGrid.add(createProfileFieldTile("Academic Batch", currentStudent.getBatch()));
        academicGrid.add(createProfileFieldTile("Enrollment Status", currentStudent.getStudentStatus() != null ? currentStudent.getStudentStatus() + " (Active)" : "Regular"));

        categoriesContainer.add(academicGrid);
        categoriesContainer.add(Box.createVerticalStrut(16));

        // Category 2: Personal & Contact Details
        categoriesContainer.add(createCategorySectionHeader("👤  Personal & Contact Information", "Verified"));
        categoriesContainer.add(Box.createVerticalStrut(8));

        JPanel personalGrid = new JPanel(new GridLayout(2, 2, 12, 10));
        personalGrid.setOpaque(false);

        personalGrid.add(createProfileFieldTile("Full Legal Name", currentStudent.getFullName()));
        personalGrid.add(createProfileFieldTile("Official University Email", currentStudent.getEmail()));
        personalGrid.add(createProfileFieldTile("Contact Phone Number", currentStudent.getPhone()));
        personalGrid.add(createProfileFieldTile("Permanent Residence", currentStudent.getAddress()));

        categoriesContainer.add(personalGrid);

        JScrollPane scrollPane = new JScrollPane(categoriesContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        rightCard.add(scrollPane, BorderLayout.CENTER);

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

    private JPanel createMiniStatBox(String label, String value, Color color) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(248, 250, 252));
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 4, 6, 4)
        ));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Arial", Font.BOLD, 13));
        valLbl.setForeground(color);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Arial", Font.BOLD, 9));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(valLbl);
        box.add(Box.createVerticalStrut(2));
        box.add(lbl);
        return box;
    }

    private JPanel createCategorySectionHeader(String title, String badge) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 13));
        titleLbl.setForeground(darkBlue);
        p.add(titleLbl, BorderLayout.WEST);

        if (badge != null) {
            JLabel badgeLbl = new JLabel("  " + badge + "  ");
            badgeLbl.setFont(new Font("Arial", Font.BOLD, 10));
            badgeLbl.setForeground(new Color(30, 64, 175));
            badgeLbl.setBackground(new Color(239, 246, 255));
            badgeLbl.setOpaque(true);
            badgeLbl.setBorder(new LineBorder(new Color(191, 219, 254), 1, true));
            p.add(badgeLbl, BorderLayout.EAST);
        }
        return p;
    }

    private JPanel createProfileFieldTile(String label, String value) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(new Color(248, 250, 252));
        tile.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Arial", Font.BOLD, 9));
        lbl.setForeground(new Color(100, 116, 139));

        String displayVal = (value != null && !value.trim().isEmpty()) ? value : "Not specified";
        JLabel val = new JLabel(displayVal);
        val.setFont(new Font("Arial", Font.BOLD, 12));
        val.setForeground(new Color(15, 23, 42));

        tile.add(lbl);
        tile.add(Box.createVerticalStrut(3));
        tile.add(val);
        return tile;
    }

    private void addProfileField(JPanel panel, String label, String value) {
        panel.add(createProfileFieldTile(label, value));
    }

    // ==========================================
    // 8. ANNOUNCEMENTS VIEW (ADVANCE NOTICES)
    // ==========================================

    public void navigateToSection(String sectionName) {
        highlightMenuButton(sectionName);
        contentCardLayout.show(dynamicViewsPanel, sectionName);
    }

    public boolean isLecturer() {
        return currentStudent != null && "LECTURER".equalsIgnoreCase(currentStudent.getRole());
    }

    private JPanel buildAnnouncementsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(lightBackground);
        mainPanel.setBorder(new EmptyBorder(0, 25, 20, 25));

        // Header section (Clean title, NO buttons)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(lightBackground);

        JLabel sectionTitle = new JLabel("Announcements");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 19));
        sectionTitle.setForeground(textColor);

        JLabel sectionSub = new JLabel("Official notices and announcements posted by faculty lecturers");
        sectionSub.setFont(new Font("Arial", Font.PLAIN, 12));
        sectionSub.setForeground(new Color(110, 120, 130));

        headerPanel.add(sectionTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(sectionSub);

        // Announcements Feed list container
        announcementsListContainer = new JPanel();
        announcementsListContainer.setLayout(new BoxLayout(announcementsListContainer, BoxLayout.Y_AXIS));
        announcementsListContainer.setBackground(lightBackground);

        JScrollPane scrollPane = new JScrollPane(announcementsListContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(lightBackground);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Initial Load
        refreshAnnouncementsData();

        return mainPanel;
    }

    private JPanel createMiniMetricCard(String title, String initialVal, Color valColor) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        tLbl.setForeground(new Color(110, 120, 130));

        JLabel vLbl = new JLabel(initialVal);
        vLbl.setFont(new Font("Arial", Font.BOLD, 22));
        vLbl.setForeground(valColor);

        card.add(tLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(vLbl);

        card.putClientProperty("valLabel", vLbl);
        return card;
    }

    private void addAnnouncementFilterButton(JPanel parent, String label, String filterKey) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color activeBg = new Color(29, 78, 216);      // #1D4ED8 Vibrant Royal Blue
        Color inactiveBg = new Color(219, 234, 254);  // #DBEAFE Distinct Soft Blue
        Color inactiveFg = new Color(29, 78, 216);    // #1D4ED8 Royal Blue
        Color inactiveBorder = new Color(147, 197, 253);

        if (filterKey.equals(currentAnnouncementFilter)) {
            btn.setBackground(activeBg);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(30, 64, 175), 1, true),
                    new EmptyBorder(7, 14, 7, 14)
            ));
        } else {
            btn.setBackground(inactiveBg);
            btn.setForeground(inactiveFg);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(inactiveBorder, 1, true),
                    new EmptyBorder(7, 14, 7, 14)
            ));
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!filterKey.equals(currentAnnouncementFilter)) {
                    btn.setBackground(new Color(191, 219, 254));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!filterKey.equals(currentAnnouncementFilter)) {
                    btn.setBackground(inactiveBg);
                }
            }
        });

        btn.addActionListener(e -> {
            currentAnnouncementFilter = filterKey;
            for (JButton b : filterButtons) {
                b.setBackground(inactiveBg);
                b.setForeground(inactiveFg);
                b.setBorder(new CompoundBorder(
                        new LineBorder(inactiveBorder, 1, true),
                        new EmptyBorder(7, 14, 7, 14)
                ));
            }
            btn.setBackground(activeBg);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(30, 64, 175), 1, true),
                    new EmptyBorder(7, 14, 7, 14)
            ));
            updateAnnouncementsFeed();
        });

        filterButtons.add(btn);
        parent.add(btn);
    }

    private void refreshAnnouncementsData() {
        allAnnouncementsList = studentDAO.getAllAnnouncements(currentStudent != null ? currentStudent.getUserId() : null);
        updateAnnouncementMetrics();
        updateAnnouncementsFeed();
        if (notificationBtn != null) {
            notificationBtn.setBadgeCount(allAnnouncementsList != null ? allAnnouncementsList.size() : 0);
        }
    }

    private void updateAnnouncementMetrics() {
        int total = allAnnouncementsList.size();
        int quizzes = 0;
        int assignments = 0;
        for (Notice n : allAnnouncementsList) {
            String type = n.getNoticeType() != null ? n.getNoticeType().toUpperCase() : "";
            if (type.contains("QUIZ")) quizzes++;
            else if (type.contains("ASSIGNMENT")) assignments++;
        }
        if (announcementsCountLabel != null) announcementsCountLabel.setText(String.valueOf(total));
        if (quizzesCountLabel != null) quizzesCountLabel.setText(String.valueOf(quizzes));
        if (assignmentsCountLabel != null) assignmentsCountLabel.setText(String.valueOf(assignments));
    }

    private void updateAnnouncementsFeed() {
        if (announcementsListContainer == null) return;
        announcementsListContainer.removeAll();

        if (allAnnouncementsList == null || allAnnouncementsList.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(new CompoundBorder(
                    new LineBorder(new Color(225, 228, 232), 1, true),
                    new EmptyBorder(40, 20, 40, 20)
            ));
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));

            JLabel emptyIcon = new JLabel("📢");
            emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyTitle = new JLabel("No Announcements Available");
            emptyTitle.setFont(new Font("Arial", Font.BOLD, 15));
            emptyTitle.setForeground(textColor);
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptySub = new JLabel("There are currently no announcements posted by faculty lecturers.");
            emptySub.setFont(new Font("Arial", Font.PLAIN, 12));
            emptySub.setForeground(new Color(120, 130, 140));
            emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(10));
            emptyPanel.add(emptyTitle);
            emptyPanel.add(Box.createVerticalStrut(5));
            emptyPanel.add(emptySub);

            announcementsListContainer.add(emptyPanel);
        } else {
            for (Notice notice : allAnnouncementsList) {
                announcementsListContainer.add(createAnnouncementCard(notice));
                announcementsListContainer.add(Box.createVerticalStrut(12));
            }
        }

        announcementsListContainer.revalidate();
        announcementsListContainer.repaint();
    }

    private JPanel createAnnouncementCard(Notice notice) {
        String type = notice.getNoticeType() != null ? notice.getNoticeType().toUpperCase() : "GENERAL";
        Color accentBarColor;
        Color badgeBg;
        String badgeText;

        if (type.contains("QUIZ")) {
            accentBarColor = new Color(230, 126, 34); // Warm Amber
            badgeBg = new Color(243, 156, 18);
            badgeText = "🔔 QUIZ - ADVANCE NOTICE";
        } else if (type.contains("ASSIGNMENT")) {
            accentBarColor = new Color(20, 165, 145); // Teal
            badgeBg = new Color(20, 165, 145);
            badgeText = "📝 UPCOMING ASSIGNMENT";
        } else if (type.contains("EXAM")) {
            accentBarColor = new Color(142, 68, 173); // Purple
            badgeBg = new Color(142, 68, 173);
            badgeText = "🎓 EXAM SCHEDULE";
        } else {
            accentBarColor = new Color(52, 152, 219); // Blue
            badgeBg = new Color(52, 152, 219);
            badgeText = "📢 FACULTY NOTICE";
        }

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentBarColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 230, 238), 1, true),
                        BorderFactory.createEmptyBorder(14, 18, 14, 18)
                )
        ));

        // Top Badges Row
        JPanel topBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBadges.setBackground(Color.WHITE);

        JLabel typeBadge = new JLabel(" " + badgeText + " ");
        typeBadge.setFont(new Font("Arial", Font.BOLD, 10));
        typeBadge.setForeground(Color.WHITE);
        typeBadge.setBackground(badgeBg);
        typeBadge.setOpaque(true);
        typeBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        topBadges.add(typeBadge);

        if (notice.getCourseCode() != null && !notice.getCourseCode().isEmpty()) {
            JLabel courseBadge = new JLabel(" " + notice.getCourseCode() + " ");
            courseBadge.setFont(new Font("Arial", Font.BOLD, 10));
            courseBadge.setForeground(darkBlue);
            courseBadge.setBackground(new Color(235, 243, 252));
            courseBadge.setOpaque(true);
            courseBadge.setBorder(new CompoundBorder(
                    new LineBorder(new Color(195, 215, 240), 1),
                    new EmptyBorder(2, 6, 2, 6)
            ));
            topBadges.add(courseBadge);
        }

        // Advance notice pill
        String advanceNoticeText = notice.getAdvanceTimeNotice();
        if (advanceNoticeText != null && !advanceNoticeText.isEmpty()) {
            JLabel advancePill = new JLabel(" " + advanceNoticeText + " ");
            advancePill.setFont(new Font("Arial", Font.BOLD, 10));
            advancePill.setForeground(new Color(133, 77, 14));
            advancePill.setBackground(new Color(254, 243, 199));
            advancePill.setOpaque(true);
            advancePill.setBorder(new CompoundBorder(
                    new LineBorder(new Color(245, 215, 145), 1),
                    new EmptyBorder(2, 6, 2, 6)
            ));
            topBadges.add(advancePill);
        }

        // Center Content
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(notice.getTitle() != null ? notice.getTitle() : "Notice");
        titleLbl.setFont(new Font("Arial", Font.BOLD, 15));
        titleLbl.setForeground(textColor);

        // Metadata: Posted Date, Scheduled Event Date (without lecturer personal details)
        StringBuilder metaSb = new StringBuilder();
        if (notice.getNoticeDate() != null) {
            metaSb.append("📅 Posted: ").append(notice.getNoticeDate().toString());
        }
        if (notice.getEventDate() != null) {
            if (metaSb.length() > 0) metaSb.append("   •   ");
            metaSb.append("🎯 Scheduled Event / Due Date: ").append(notice.getEventDate().toString());
        }

        centerContent.add(titleLbl);
        if (metaSb.length() > 0) {
            JLabel metaLbl = new JLabel(metaSb.toString());
            metaLbl.setFont(new Font("Arial", Font.PLAIN, 11));
            metaLbl.setForeground(new Color(105, 115, 128));
            centerContent.add(Box.createVerticalStrut(4));
            centerContent.add(metaLbl);
        }

        // Description content
        String desc = notice.getDescription() != null ? notice.getDescription() : "";
        String htmlDesc = desc.replace("\n", "<br>");
        JLabel descLbl = new JLabel("<html><body style='width: 700px; font-family: Arial; font-size: 12px; color: #3c4858; line-height: 1.5;'>" + htmlDesc + "</body></html>");
        centerContent.add(Box.createVerticalStrut(8));
        centerContent.add(descLbl);

        card.add(topBadges, BorderLayout.NORTH);
        card.add(centerContent, BorderLayout.CENTER);

        return card;
    }

    private void showAnnouncementDetailsDialog(Notice notice) {
        JDialog dialog = new JDialog(this, "Announcement Details - " + notice.getTitle(), true);
        dialog.setSize(560, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(lightBackground);

        JLabel titleLbl = new JLabel(notice.getTitle());
        titleLbl.setFont(new Font("Arial", Font.BOLD, 16));
        titleLbl.setForeground(darkBlue);

        JLabel subLbl = new JLabel("Posted by: " + (notice.getAuthorName() != null ? notice.getAuthorName() : "Course Lecturer") +
                                  " • Date: " + (notice.getNoticeDate() != null ? notice.getNoticeDate().toString() : "Recent"));
        subLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        subLbl.setForeground(new Color(110, 120, 130));

        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(subLbl);

        JTextArea bodyArea = new JTextArea(notice.getDescription());
        bodyArea.setFont(new Font("Arial", Font.PLAIN, 13));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setEditable(false);
        bodyArea.setBackground(Color.WHITE);
        bodyArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(bodyArea);
        scroll.setBorder(new LineBorder(new Color(220, 225, 230), 1, true));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(lightBackground);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 12));
        closeBtn.setBackground(new Color(29, 78, 216));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void promptLecturerAuthenticationAndPost() {
        JDialog authDialog = new JDialog(this, "Lecturer Authentication Required", true);
        authDialog.setSize(440, 310);
        authDialog.setResizable(false);
        authDialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(lightBackground);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(darkBlue);
        top.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel t = new JLabel("Lecturer Authorization");
        t.setFont(new Font("Arial", Font.BOLD, 15));
        t.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Only lecturers are authorized to post announcements.");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(new Color(180, 240, 220));

        top.add(t);
        top.add(Box.createVerticalStrut(3));
        top.add(sub);
        p.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel uLbl = new JLabel("Lecturer Username (e.g., lec001):");
        uLbl.setFont(new Font("Arial", Font.BOLD, 12));
        uLbl.setForeground(textColor);

        JTextField uField = new JTextField("lec001");
        uField.setFont(new Font("Arial", Font.PLAIN, 13));
        uField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel pLbl = new JLabel("Lecturer Password (e.g., Lec@123):");
        pLbl.setFont(new Font("Arial", Font.BOLD, 12));
        pLbl.setForeground(textColor);

        JPasswordField pField = new JPasswordField("Lec@123");
        pField.setFont(new Font("Arial", Font.PLAIN, 13));
        pField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        errLbl.setForeground(new Color(210, 50, 50));

        form.add(uLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(uField);
        form.add(Box.createVerticalStrut(10));
        form.add(pLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(pField);
        form.add(Box.createVerticalStrut(4));
        form.add(errLbl);

        p.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(lightBackground);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        cancelBtn.setOpaque(true);
        cancelBtn.setBackground(new Color(234, 242, 255));
        cancelBtn.setForeground(darkBlue);
        cancelBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> authDialog.dispose());

        JButton verifyBtn = new JButton("Verify & Continue to Post");
        verifyBtn.setFont(new Font("Arial", Font.BOLD, 12));
        verifyBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        verifyBtn.setOpaque(true);
        verifyBtn.setBackground(new Color(29, 78, 216));
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        verifyBtn.setFocusPainted(false);
        verifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        verifyBtn.addActionListener(e -> {
            String u = uField.getText().trim();
            String pwd = new String(pField.getPassword()).trim();
            if (u.isEmpty() || pwd.isEmpty()) {
                errLbl.setText("Please enter both lecturer username and password.");
                return;
            }
            dao.UserDAO uDao = new dao.UserDAO();
            User verified = uDao.authenticate(u, pwd);
            if (verified != null && "LECTURER".equalsIgnoreCase(verified.getRole())) {
                authDialog.dispose();
                showPostAnnouncementDialog(verified);
            } else {
                errLbl.setText("Access Denied: Only authenticated lecturers can post.");
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(verifyBtn);
        p.add(btnPanel, BorderLayout.SOUTH);

        authDialog.add(p);
        authDialog.setVisible(true);
    }

    private void showPostAnnouncementDialog() {
        if (isLecturer()) {
            showPostAnnouncementDialog(currentStudent);
        } else {
            promptLecturerAuthenticationAndPost();
        }
    }

    private void showPostAnnouncementDialog(User authorizedLecturer) {
        if (authorizedLecturer == null || (!"LECTURER".equalsIgnoreCase(authorizedLecturer.getRole()) && !"ADMIN".equalsIgnoreCase(authorizedLecturer.getRole()))) {
            JOptionPane.showMessageDialog(this,
                    "Access Denied: Only lecturers are authorized to post announcements.",
                    "Unauthorized Action", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Post Advance Announcement (Lecturer Portal)", true);
        dialog.setSize(620, 680);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(darkBlue);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel titleLbl = new JLabel("Post Advance Announcement (Lecturer Portal)");
        titleLbl.setFont(new Font("Arial", Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Inform students in advance about upcoming quizzes, assignments, and academic events");
        subLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        subLbl.setForeground(new Color(180, 240, 220));

        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(subLbl);
        mainPanel.add(header, BorderLayout.NORTH);

        // Form fields in scrollable area
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 20, 25));

        // 1. Authenticated Lecturer Display
        JLabel lecLabel = new JLabel("Authenticated Lecturer / Author:");
        lecLabel.setFont(new Font("Arial", Font.BOLD, 12));
        lecLabel.setForeground(textColor);

        String lecDisplayName = authorizedLecturer.getFirstName() + " " + authorizedLecturer.getLastName() + " (" + authorizedLecturer.getUserId() + ") - Verified Lecturer";
        JTextField lecField = new JTextField(lecDisplayName);
        lecField.setFont(new Font("Arial", Font.BOLD, 12));
        lecField.setEditable(false);
        lecField.setBackground(new Color(245, 252, 248));
        lecField.setForeground(new Color(20, 110, 95));
        lecField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        lecField.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 230, 215), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));

        // 2. Course Offering Selection
        JLabel courseLabel = new JLabel("Target Course Offering:");
        courseLabel.setFont(new Font("Arial", Font.BOLD, 12));
        courseLabel.setForeground(textColor);

        JComboBox<CourseOfferingItem> courseCombo = new JComboBox<>();
        courseCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        courseCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        List<Course> offerings = studentDAO.getAllCourseOfferings();
        if (offerings.isEmpty()) {
            courseCombo.addItem(new CourseOfferingItem("OFF001", "ICT2132", "Object Oriented Programming"));
            courseCombo.addItem(new CourseOfferingItem("OFF002", "ICT2123", "Database Systems"));
            courseCombo.addItem(new CourseOfferingItem("OFF003", "ICT2142", "E-Business"));
        } else {
            for (Course c : offerings) {
                courseCombo.addItem(new CourseOfferingItem(c.getOfferingId(), c.getCourseCode(), c.getCourseName()));
            }
        }

        // 3. Announcement Type
        JLabel typeLabel = new JLabel("Announcement Type:");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        typeLabel.setForeground(textColor);

        String[] types = {
                "Upcoming Quiz (Advance Notice)",
                "Upcoming Assignment (Advance Notice)",
                "Examination Schedule",
                "General Course Notice"
        };
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        // 4. Title Field
        JLabel titleFieldLabel = new JLabel("Announcement Title:");
        titleFieldLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleFieldLabel.setForeground(textColor);

        JTextField titleField = new JTextField("Advance Notice: Quiz 02 - Object Oriented Design Patterns");
        titleField.setFont(new Font("Arial", Font.PLAIN, 13));
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        titleField.setBorder(new CompoundBorder(
                new LineBorder(new Color(215, 222, 230), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));

        // When type changes, adjust sample title
        typeCombo.addActionListener(e -> {
            String selected = (String) typeCombo.getSelectedItem();
            CourseOfferingItem selectedCourse = (CourseOfferingItem) courseCombo.getSelectedItem();
            String cCode = selectedCourse != null ? selectedCourse.courseCode : "ICT2132";
            if (selected != null && selected.contains("Quiz")) {
                titleField.setText("Advance Notice: Quiz 02 - Object Oriented Design Patterns (" + cCode + ")");
            } else if (selected != null && selected.contains("Assignment")) {
                titleField.setText("Advance Notice: Assignment 02 - Normalization & Complex SQL Queries (" + cCode + ")");
            } else if (selected != null && selected.contains("Exam")) {
                titleField.setText("Mid Semester Examination Timetable & Syllabus (" + cCode + ")");
            } else {
                titleField.setText("Lecture Room & Schedule Notice (" + cCode + ")");
            }
        });

        // 5. Scheduled Event Date (Advance Notice Date)
        JLabel dateLabel = new JLabel("Scheduled Assessment / Event Date (YYYY-MM-DD):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dateLabel.setForeground(textColor);

        String defaultFutureDate = LocalDate.now().plusDays(7).toString();
        JTextField dateField = new JTextField(defaultFutureDate);
        dateField.setFont(new Font("Arial", Font.PLAIN, 13));
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        dateField.setBorder(new CompoundBorder(
                new LineBorder(new Color(215, 222, 230), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JLabel dateHint = new JLabel("Students will receive advance notification prior to this date.");
        dateHint.setFont(new Font("Arial", Font.ITALIC, 11));
        dateHint.setForeground(new Color(110, 120, 130));

        // 6. Weight & Max Marks
        JPanel marksGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        marksGrid.setBackground(Color.WHITE);
        marksGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JPanel weightP = new JPanel();
        weightP.setLayout(new BoxLayout(weightP, BoxLayout.Y_AXIS));
        weightP.setBackground(Color.WHITE);
        JLabel wLbl = new JLabel("Assessment Weight (%):");
        wLbl.setFont(new Font("Arial", Font.BOLD, 12));
        wLbl.setForeground(textColor);
        JTextField weightField = new JTextField("10.00");
        weightField.setFont(new Font("Arial", Font.PLAIN, 13));
        weightField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        weightP.add(wLbl);
        weightP.add(Box.createVerticalStrut(4));
        weightP.add(weightField);

        JPanel maxMarksP = new JPanel();
        maxMarksP.setLayout(new BoxLayout(maxMarksP, BoxLayout.Y_AXIS));
        maxMarksP.setBackground(Color.WHITE);
        JLabel mLbl = new JLabel("Maximum Marks:");
        mLbl.setFont(new Font("Arial", Font.BOLD, 12));
        mLbl.setForeground(textColor);
        JTextField maxMarksField = new JTextField("100.00");
        maxMarksField.setFont(new Font("Arial", Font.PLAIN, 13));
        maxMarksField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        maxMarksP.add(mLbl);
        maxMarksP.add(Box.createVerticalStrut(4));
        maxMarksP.add(maxMarksField);

        marksGrid.add(weightP);
        marksGrid.add(maxMarksP);

        // 7. Guidelines & Syllabus Description
        JLabel descLabel = new JLabel("Detailed Guidelines & Syllabus for Students:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 12));
        descLabel.setForeground(textColor);

        JTextArea descArea = new JTextArea(5, 20);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setText(
                "Advance Notice for Students:\n" +
                "• Topics Covered: Classes, Inheritance, Polymorphism, Abstract Classes, and Exception Handling.\n" +
                "• Assessment Format: 20 Multiple Choice Questions + 2 Hands-on Coding Problems.\n" +
                "• Venue & Timing: Software Lab 01 (09:00 AM - 10:00 AM).\n" +
                "• Advance Guidelines: Review lecture slides 1 to 5 and complete the sample worksheet beforehand. Please bring your Student ID card."
        );
        descArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        descScroll.setBorder(new LineBorder(new Color(215, 222, 230), 1));

        // 8. Sync with Assessments checkbox
        JCheckBox syncCheck = new JCheckBox("Automatically register this Quiz/Assignment in the Assessments schedule table");
        syncCheck.setFont(new Font("Arial", Font.BOLD, 11));
        syncCheck.setForeground(darkBlue);
        syncCheck.setBackground(Color.WHITE);
        syncCheck.setSelected(true);
        syncCheck.setFocusPainted(false);

        // Assemble Form
        form.add(lecLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(lecField);
        form.add(Box.createVerticalStrut(10));

        form.add(courseLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(courseCombo);
        form.add(Box.createVerticalStrut(10));

        form.add(typeLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(typeCombo);
        form.add(Box.createVerticalStrut(10));

        form.add(titleFieldLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(titleField);
        form.add(Box.createVerticalStrut(10));

        form.add(dateLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(dateField);
        form.add(Box.createVerticalStrut(2));
        form.add(dateHint);
        form.add(Box.createVerticalStrut(10));

        form.add(marksGrid);
        form.add(Box.createVerticalStrut(10));

        form.add(descLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(descScroll);
        form.add(Box.createVerticalStrut(8));

        form.add(syncCheck);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getVerticalScrollBar().setUnitIncrement(12);

        mainPanel.add(formScroll, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        bottomBar.setBackground(lightBackground);
        bottomBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 230, 238)),
                new EmptyBorder(0, 15, 0, 15)
        ));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        cancelBtn.setOpaque(true);
        cancelBtn.setBackground(new Color(234, 242, 255));
        cancelBtn.setForeground(darkBlue);
        cancelBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = new JButton("Post Advance Announcement");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 12));
        submitBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        submitBtn.setOpaque(true);
        submitBtn.setBackground(new Color(29, 78, 216));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String dateStr = dateField.getText().trim();
            String descText = descArea.getText().trim();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter an announcement title.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.sql.Date sqlDate;
            try {
                sqlDate = java.sql.Date.valueOf(dateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid date in YYYY-MM-DD format (e.g., " + defaultFutureDate + ").", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CourseOfferingItem selectedCourse = (CourseOfferingItem) courseCombo.getSelectedItem();
            String rawType = (String) typeCombo.getSelectedItem();
            String typeCategory = "GENERAL";
            if (rawType != null && rawType.contains("Quiz")) typeCategory = "QUIZ";
            else if (rawType != null && rawType.contains("Assignment")) typeCategory = "ASSIGNMENT";
            else if (rawType != null && rawType.contains("Exam")) typeCategory = "MID_EXAM";

            BigDecimal weight = new BigDecimal("10.00");
            BigDecimal maxMarks = new BigDecimal("100.00");
            try {
                weight = new BigDecimal(weightField.getText().trim());
                maxMarks = new BigDecimal(maxMarksField.getText().trim());
            } catch (Exception ignored) {}

            Notice n = new Notice();
            n.setTitle(title);
            n.setDescription(descText);
            n.setNoticeDate(new java.sql.Date(System.currentTimeMillis()));
            n.setEventDate(sqlDate);
            n.setNoticeType(typeCategory);
            n.setCreatedBy(authorizedLecturer.getUserId());
            n.setAuthorName(authorizedLecturer.getFirstName() + " " + authorizedLecturer.getLastName());
            if (selectedCourse != null) {
                n.setCourseCode(selectedCourse.courseCode);
                n.setCourseName(selectedCourse.courseName);
                n.setOfferingId(selectedCourse.offeringId);
            }

            boolean ok = studentDAO.postAnnouncement(
                    n,
                    selectedCourse != null ? selectedCourse.offeringId : "OFF001",
                    typeCategory,
                    weight,
                    maxMarks,
                    syncCheck.isSelected()
            );

            if (ok) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Advance Announcement posted successfully by Lecturer " + authorizedLecturer.getFirstName() + " " + authorizedLecturer.getLastName() + "!\n\n" +
                        "• Title: " + title + "\n" +
                        "• Scheduled Date: " + sqlDate + "\n" +
                        "• Course: " + (selectedCourse != null ? selectedCourse.courseCode : "N/A") + "\n\n" +
                        "Students can now view this in their Announcements navigation section in advance.",
                        "Announcement Posted",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dialog.dispose();
                refreshAnnouncementsData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Database error: Could not save announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomBar.add(cancelBtn);
        bottomBar.add(submitBtn);

        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        dialog.add(mainPanel);
    }

    private static class LecturerItem {
        final String userId;
        final String name;
        final String title;

        LecturerItem(String userId, String name, String title) {
            this.userId = userId;
            this.name = name;
            this.title = title;
        }

        @Override
        public String toString() {
            return name + (title != null && !title.isEmpty() ? " - " + title : "") + " [" + userId + "]";
        }
    }

    private static class CourseOfferingItem {
        final String offeringId;
        final String courseCode;
        final String courseName;

        CourseOfferingItem(String offeringId, String courseCode, String courseName) {
            this.offeringId = offeringId;
            this.courseCode = courseCode;
            this.courseName = courseName;
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName + " (" + offeringId + ")";
        }
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
                b.setBackground(new Color(29, 78, 216));
                b.setFont(new Font("Arial", Font.BOLD, 13));
                b.setForeground(Color.WHITE);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(147, 197, 253)),
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
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setToolTipText("Click to view full announcement details");
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigateToSection("Announcements");
            }
        });

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

    // Helper for beautiful consistent tables (Strictly non-editable for students)
    private JTable createStandardTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setDefaultEditor(Object.class, null); // Strictly prevent any in-cell editing
        table.setRowHeight(36);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setGridColor(new Color(235, 238, 242));
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setSelectionForeground(textColor);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false); // Disallow dragging/reordering columns
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

    // ==========================================
    // NOTIFICATION BELL & POPUP VIEWER
    // ==========================================

    /**
     * Shows a modern dropdown popup displaying recent notifications when the bell is clicked.
     */
    private void showNotificationsPopup(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel container = new JPanel(new BorderLayout());
        container.setPreferredSize(new Dimension(380, 420));
        container.setBackground(Color.WHITE);

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(248, 250, 252));
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);

        JLabel bellIconLbl = new JLabel("🔔");
        bellIconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JLabel titleLbl = new JLabel("Notifications");
        titleLbl.setFont(new Font("Arial", Font.BOLD, 14));
        titleLbl.setForeground(textColor);

        int count = allAnnouncementsList != null ? allAnnouncementsList.size() : 0;
        JLabel countBadge = new JLabel(count + " new");
        countBadge.setFont(new Font("Arial", Font.BOLD, 10));
        countBadge.setForeground(Color.WHITE);
        countBadge.setOpaque(true);
        countBadge.setBackground(new Color(29, 78, 216));
        countBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        titleGroup.add(bellIconLbl);
        titleGroup.add(titleLbl);
        titleGroup.add(countBadge);

        JButton markReadBtn = new JButton("Clear Badge");
        markReadBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        markReadBtn.setForeground(new Color(100, 116, 139));
        markReadBtn.setBackground(new Color(248, 250, 252));
        markReadBtn.setBorderPainted(false);
        markReadBtn.setContentAreaFilled(false);
        markReadBtn.setFocusPainted(false);
        markReadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markReadBtn.addActionListener(ev -> {
            if (notificationBtn != null) notificationBtn.setBadgeCount(0);
            countBadge.setText("0 new");
            countBadge.setBackground(new Color(148, 163, 184));
        });

        top.add(titleGroup, BorderLayout.WEST);
        top.add(markReadBtn, BorderLayout.EAST);
        container.add(top, BorderLayout.NORTH);

        // Notifications List
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        List<Notice> notices = allAnnouncementsList;
        if (notices == null || notices.isEmpty()) {
            notices = studentDAO.getAllAnnouncements(currentStudent != null ? currentStudent.getUserId() : null);
        }

        if (notices == null || notices.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(new EmptyBorder(50, 20, 50, 20));

            JLabel emptyIcon = new JLabel("🎉");
            emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyText = new JLabel("No New Notifications");
            emptyText.setFont(new Font("Arial", Font.BOLD, 14));
            emptyText.setForeground(textColor);
            emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptySub = new JLabel("You are completely caught up!");
            emptySub.setFont(new Font("Arial", Font.PLAIN, 12));
            emptySub.setForeground(new Color(148, 163, 184));
            emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(10));
            emptyPanel.add(emptyText);
            emptyPanel.add(Box.createVerticalStrut(4));
            emptyPanel.add(emptySub);

            listPanel.add(emptyPanel);
        } else {
            int maxItems = Math.min(notices.size(), 8);
            for (int i = 0; i < maxItems; i++) {
                Notice n = notices.get(i);
                JPanel item = createNotificationItemCard(n, popup);
                listPanel.add(item);
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        container.add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JButton viewAllBtn = new JButton("View All Announcements & Notices →");
        viewAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewAllBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        viewAllBtn.setOpaque(true);
        viewAllBtn.setForeground(Color.WHITE);
        viewAllBtn.setBackground(new Color(29, 78, 216));
        viewAllBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 64, 175), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.addActionListener(ev -> {
            popup.setVisible(false);
            navigateToSection("Announcements");
        });

        footer.add(viewAllBtn, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);

        popup.add(container);

        // Position popup directly below the notification button
        int xOffset = invoker.getWidth() - container.getPreferredSize().width;
        popup.show(invoker, xOffset, invoker.getHeight() + 4);
    }

    private JPanel createNotificationItemCard(Notice n, JPopupMenu parentPopup) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon on left
        String type = n.getNoticeType() != null ? n.getNoticeType().toUpperCase() : "GENERAL";
        String iconSymbol = "📢";
        Color iconBg = new Color(238, 242, 255);
        Color iconBorder = new Color(199, 210, 254);
        if (type.contains("QUIZ")) {
            iconSymbol = "🔔";
            iconBg = new Color(254, 243, 199);
            iconBorder = new Color(253, 230, 138);
        } else if (type.contains("ASSIGNMENT")) {
            iconSymbol = "📝";
            iconBg = new Color(209, 250, 229);
            iconBorder = new Color(167, 243, 208);
        }

        JLabel iconLbl = new JLabel(iconSymbol, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        iconLbl.setPreferredSize(new Dimension(34, 34));
        iconLbl.setOpaque(true);
        iconLbl.setBackground(iconBg);
        iconLbl.setBorder(new LineBorder(iconBorder, 1, true));

        // Center texts
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        String course = n.getCourseCode() != null && !n.getCourseCode().isBlank() ? "[" + n.getCourseCode() + "] " : "";
        JLabel titleLbl = new JLabel(course + (n.getTitle() != null ? n.getTitle() : "Notice"));
        titleLbl.setFont(new Font("Arial", Font.BOLD, 12));
        titleLbl.setForeground(textColor);

        String desc = n.getDescription() != null ? n.getDescription() : "";
        if (desc.length() > 60) {
            desc = desc.substring(0, 57) + "...";
        }
        JLabel descLbl = new JLabel(desc.isEmpty() ? "Click to view full notice details." : desc);
        descLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        descLbl.setForeground(new Color(100, 116, 139));

        String timeStr = n.getNoticeDate() != null ? new SimpleDateFormat("MMM dd, yyyy").format(n.getNoticeDate()) : "Recently";
        if (n.getEventDate() != null) {
            timeStr = n.getAdvanceTimeNotice();
        }
        JLabel timeLbl = new JLabel(timeStr);
        timeLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLbl.setForeground(new Color(148, 163, 184));

        textCol.add(titleLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(descLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(timeLbl);

        card.add(iconLbl, BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 250, 252));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (parentPopup != null) parentPopup.setVisible(false);
                navigateToSection("Announcements");
            }
        });

        return card;
    }

    /**
     * Custom notification bell button with vector-rendered bell and unread badge.
     */
    private static class NotificationBellButton extends JButton {
        private int badgeCount = 0;

        public NotificationBellButton() {
            setPreferredSize(new Dimension(38, 38));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Notifications");
        }

        public void setBadgeCount(int count) {
            this.badgeCount = count;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Hover circle
            if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillOval(2, 2, w - 4, h - 4);
            }

            // Bell icon
            int cx = w / 2;
            int cy = h / 2 - 1;

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Bell top loop
            g2.drawArc(cx - 2, cy - 9, 4, 4, 0, 180);

            // Bell body
            Path2D.Double bell = new Path2D.Double();
            bell.moveTo(cx - 6, cy - 6);
            bell.curveTo(cx - 6, cy - 2, cx - 8, cy + 3, cx - 8, cy + 5);
            bell.lineTo(cx + 8, cy + 5);
            bell.curveTo(cx + 8, cy + 3, cx + 6, cy - 2, cx + 6, cy - 6);
            bell.quadTo(cx, cy - 8, cx - 6, cy - 6);
            bell.closePath();
            g2.draw(bell);

            // Bell clapper
            g2.fillOval(cx - 2, cy + 6, 4, 3);

            // Badge if badgeCount > 0
            if (badgeCount > 0) {
                String badgeText = badgeCount > 99 ? "99+" : String.valueOf(badgeCount);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                int strW = fm.stringWidth(badgeText);
                int badgeW = Math.max(16, strW + 6);
                int badgeH = 14;
                int bx = w - badgeW - 2;
                int by = 2;

                // Red badge fill
                g2.setColor(new Color(239, 68, 68)); // #EF4444
                g2.fillRoundRect(bx, by, badgeW, badgeH, 8, 8);

                // Badge border
                g2.setColor(new Color(27, 67, 103)); // match darkBlue header
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(bx, by, badgeW, badgeH, 8, 8);

                // Text
                g2.setColor(Color.WHITE);
                g2.drawString(badgeText, bx + (badgeW - strW) / 2, by + fm.getAscent() - 1);
            }

            g2.dispose();
        }
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
