package ui.student;

import dao.StudentDAO;
import model.*;
import ui.login.LoginFrame;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 18));
        headerRight.setOpaque(false);

        JLabel userType = new JLabel("STUDENT  ");
        userType.setForeground(new Color(180, 240, 220));
        userType.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel studentBadge = new JLabel(currentStudent.getFirstName() + " (" + currentStudent.getIndexNo() + ")  ");
        studentBadge.setForeground(Color.WHITE);
        studentBadge.setFont(new Font("Arial", Font.PLAIN, 12));

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
    // 7. MY PROFILE VIEW
    // ==========================================
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(lightBackground);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JLabel sectionTitle = new JLabel("Undergraduate Student Profile");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        sectionTitle.setForeground(textColor);
        panel.add(sectionTitle, BorderLayout.NORTH);

        JPanel profileCard = new JPanel(new GridLayout(4, 2, 20, 15));
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        addProfileField(profileCard, "Full Name", currentStudent.getFullName());
        addProfileField(profileCard, "Student Index No", currentStudent.getIndexNo());
        addProfileField(profileCard, "Department", currentStudent.getDepartment());
        addProfileField(profileCard, "Academic Batch", currentStudent.getBatch());
        addProfileField(profileCard, "Current Academic Level", currentStudent.getAcademicYear());
        addProfileField(profileCard, "Official Email", currentStudent.getEmail());
        addProfileField(profileCard, "Contact Number", currentStudent.getPhone());
        addProfileField(profileCard, "Address / Residence", currentStudent.getAddress());

        panel.add(profileCard, BorderLayout.CENTER);
        return panel;
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
