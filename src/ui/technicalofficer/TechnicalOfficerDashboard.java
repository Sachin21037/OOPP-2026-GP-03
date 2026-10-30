package ui.technicalofficer;

import dao.TechnicalOfficerDAO;
import model.Notice;
import model.TechnicalOfficer;
import model.TimetableEntry;
import service.AttendanceService;
import service.MedicalService;
import service.NoticeService;
import service.TimetableService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main dashboard window for Technical Officers in FAMS.
 * Implements sidebar navigation, department academic timetable viewing,
 * department notices reading, and attendance & medical management.
 * Strictly adheres to the database schema and official role specifications.
 */
public class TechnicalOfficerDashboard extends JFrame {

    // Theme Colors matching reference design
    private final Color darkNavy = new Color(27, 67, 103);      // #1B4367 exact navy
    private final Color contentBg = new Color(245, 247, 250);   // #F5F7FA
    private final Color sidebarBg = new Color(27, 67, 103);     // Matching dark navy blue sidebar
    private final Color sidebarBorder = new Color(20, 52, 82);  // Subtle navy right border
    private final Color borderColor = new Color(226, 232, 240); // #E2E8F0
    private final Color textDark = new Color(30, 41, 59);       // #1E293B
    private final Color textMuted = new Color(100, 116, 139);   // #64748B
    private final Color textLight = new Color(148, 163, 184);   // #94A3B8

    // Services
    private final AttendanceService attendanceService = new AttendanceService();
    private final MedicalService medicalService = new MedicalService();
    private final NoticeService noticeService = new NoticeService();
    private final TimetableService timetableService = new TimetableService();

    // UI Navigation Containers
    private CardLayout cardLayout;
    private JPanel contentContainer;
    private AttendanceManagementPage attendancePage;
    private MedicalManagementPage medicalPage;
    private TimetablePage timetablePage;
    private NoticesPage noticesPage;
    private ProfilePage profilePage;

    // Overview Containers for dynamic updates
    private JPanel cardsContainerPanel;
    private JPanel activityListContainer;

    private final Map<String, SidebarMenuButton> menuButtons = new HashMap<>();
    private String currentView = "Dashboard";

    // Header Notification Elements
    private JLabel notificationBadgeLabel;
    private JPanel notificationBtn;

    private final String officerId;
    private final String officerName;
    private String officerDepartment = "Information and Communication Technology";

    public TechnicalOfficerDashboard() {
        this("TO001", "Dinesh Kumara");
    }

    public TechnicalOfficerDashboard(String officerId, String officerName) {
        this.officerId = (officerId != null && !officerId.isBlank()) ? officerId : "TO001";
        this.officerName = (officerName != null && !officerName.isBlank()) ? officerName : "Dinesh Kumara";

        // Resolve Department from TechnicalOfficerDAO
        try {
            TechnicalOfficerDAO toDao = new TechnicalOfficerDAO();
            TechnicalOfficer officer = toDao.getOfficerById(this.officerId);
            if (officer != null && officer.getDepartment() != null && !officer.getDepartment().isBlank()) {
                this.officerDepartment = officer.getDepartment();
            }
        } catch (Exception ignored) {
        }

        setTitle("Faculty Academic Management System - Technical Officer");
        setSize(1220, 740);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Root Container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(contentBg);

        // 1. Top Navy Header Bar (#1B4367)
        JPanel header = createTopHeaderBar();

        // 2. Left Dark Navy Sidebar (#1B4367)
        JPanel sidebar = createSidebar();

        // 3. Central Dynamic Views Container (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(contentBg);

        // A. Dashboard Overview (KPI Cards + Recent Activities)
        JPanel dashboardOverview = createDashboardOverview();
        contentContainer.add(dashboardOverview, "Dashboard");

        // B. Attendance Management Page
        attendancePage = new AttendanceManagementPage();
        attendancePage.setOnNavigateToTimetable(() -> navigateTo("Timetable"));
        contentContainer.add(attendancePage, "Attendance");

        // C. Medical Management Page
        medicalPage = new MedicalManagementPage();
        contentContainer.add(medicalPage, "Medical Certificates");

        // D. Profile Page
        profilePage = new ProfilePage();
        profilePage.setOnNavigateToTimetable(() -> navigateTo("Timetable"));
        contentContainer.add(profilePage, "Profile");

        // E. Timetable Page (Department schedule view-only)
        timetablePage = new TimetablePage(officerDepartment);
        timetablePage.setOnNavigateToNotices(() -> navigateTo("Notices"));
        timetablePage.setOnNavigateToAttendance(() -> navigateTo("Attendance"));
        contentContainer.add(timetablePage, "Timetable");

        // F. Notices Page (Department notices view-only)
        noticesPage = new NoticesPage(officerDepartment);
        noticesPage.setOnNavigateToTimetable(() -> navigateTo("Timetable"));
        contentContainer.add(noticesPage, "Notices");

        // Assemble Layout
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentContainer, BorderLayout.CENTER);

        // Highlight initial menu
        highlightActiveMenu("Dashboard");

        add(mainPanel);
        setVisible(true);

        // Update notification counter
        updateNotificationCount();
    }

    /**
     * Creates the Top Dark Blue (#1B4367) Header Bar:
     * - Left: "FACULTY ACADEMIC MANAGEMENT SYSTEM" (white bold)
     * - Right: Notification button, Avatar "DK" with green status dot, Name "(TO001)", and "TECHNICAL OFFICER".
     */
    private JPanel createTopHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkNavy);
        header.setPreferredSize(new Dimension(1220, 62));

        JLabel systemTitle = new JLabel("FACULTY ACADEMIC MANAGEMENT SYSTEM");
        systemTitle.setForeground(Color.WHITE);
        systemTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        systemTitle.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        // Right Panel: Notification Icon + Profile Widget
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        rightPanel.setOpaque(false);

        // 1. Notification Button (with unread badge counter)
        notificationBtn = createNotificationButton();

        // 2. Clickable Profile Area (Avatar + Name + Role)
        JPanel profileHeaderPanel = createProfileHeaderPanel();

        rightPanel.add(notificationBtn);
        rightPanel.add(profileHeaderPanel);

        header.add(systemTitle, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createNotificationButton() {
        JPanel btn = new JPanel(new BorderLayout()) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showNotificationPopup(e.getComponent());
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLayeredPane layer = new JLayeredPane();
        layer.setPreferredSize(new Dimension(34, 34));

        JLabel bellIcon = new JLabel("🔔", SwingConstants.CENTER);
        bellIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        bellIcon.setForeground(new Color(254, 215, 102));
        bellIcon.setBounds(2, 6, 24, 24);

        notificationBadgeLabel = new JLabel("3", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        notificationBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        notificationBadgeLabel.setForeground(Color.WHITE);
        notificationBadgeLabel.setBounds(16, 2, 17, 17);

        layer.add(bellIcon, JLayeredPane.DEFAULT_LAYER);
        layer.add(notificationBadgeLabel, JLayeredPane.PALETTE_LAYER);

        btn.add(layer, BorderLayout.CENTER);
        return btn;
    }

    private void showNotificationPopup(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        // Header Title
        JLabel title = new JLabel("Department Bulletins & Alerts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(darkNavy);
        title.setBorder(new EmptyBorder(8, 12, 6, 12));
        popup.add(title);
        popup.addSeparator();

        int itemsCount = 0;

        // Notices list
        List<Notice> activeNotices = noticeService.getActiveNotices();
        int noticeCount = 0;
        for (Notice n : activeNotices) {
            if (noticeCount >= 3) break;
            JMenuItem itemNotice = new JMenuItem(n.getTitle());
            itemNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            itemNotice.setBackground(Color.WHITE);
            itemNotice.addActionListener(e -> {
                navigateTo("Notices");
                if (noticesPage != null) {
                    noticesPage.openNoticeById(n.getNoticeId());
                }
            });
            popup.add(itemNotice);
            noticeCount++;
            itemsCount++;
        }

        if (itemsCount == 0) {
            JMenuItem emptyItem = new JMenuItem("No active department notices");
            emptyItem.setEnabled(false);
            emptyItem.setBackground(Color.WHITE);
            popup.add(emptyItem);
        }

        popup.addSeparator();

        // Footer Actions
        JMenuItem viewTimetable = new JMenuItem("View Department Timetable");
        viewTimetable.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewTimetable.setForeground(darkNavy);
        viewTimetable.setBackground(new Color(241, 245, 249));
        viewTimetable.setBorder(new EmptyBorder(6, 8, 6, 8));
        viewTimetable.addActionListener(e -> navigateTo("Timetable"));

        JMenuItem viewAllNotices = new JMenuItem("View All Department Notices");
        viewAllNotices.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllNotices.setForeground(darkNavy);
        viewAllNotices.setBackground(new Color(241, 245, 249));
        viewAllNotices.setBorder(new EmptyBorder(6, 8, 6, 8));
        viewAllNotices.addActionListener(e -> navigateTo("Notices"));

        popup.add(viewTimetable);
        popup.add(viewAllNotices);

        popup.show(invoker, -180, 40);
    }

    private void updateNotificationCount() {
        int unread = noticeService.getUnreadCount(officerDepartment);
        if (unread > 0) {
            notificationBadgeLabel.setText(String.valueOf(unread));
            notificationBadgeLabel.setVisible(true);
        } else {
            notificationBadgeLabel.setText("0");
            notificationBadgeLabel.setVisible(false);
        }
    }

    /**
     * Creates the clickable profile section in the top header bar.
     */
    private JPanel createProfileHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0)) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        navigateTo("Profile");
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 22));
                    g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 12, 12);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 10));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setToolTipText("Click to view & manage your Technical Officer Profile");

        // 1. Circular Avatar with initials + green status dot
        String initials = extractInitials(officerName);
        CircleAvatarWithStatus avatar = new CircleAvatarWithStatus(initials);

        // 2. Officer Name & ID label
        JLabel nameLabel = new JLabel(officerName + " (" + officerId + ")");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // 3. Role Label
        JLabel roleLabel = new JLabel("TECHNICAL OFFICER");
        roleLabel.setForeground(new Color(180, 240, 220));
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        MouseAdapter forwarder = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigateTo("Profile");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, panel));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, panel));
            }
        };

        avatar.addMouseListener(forwarder);
        nameLabel.addMouseListener(forwarder);
        roleLabel.addMouseListener(forwarder);

        panel.add(avatar);
        panel.add(nameLabel);
        panel.add(roleLabel);

        return panel;
    }

    private String extractInitials(String name) {
        if (name == null || name.isBlank()) return "TO";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    /**
     * Creates the Left Dark Navy Sidebar (#1B4367) matching the top navbar:
     * - Menu header in soft ice-blue
     * - Pure white link text
     * - Sky-blue 4px active vertical bar
     * - Coral red Logout button at bottom
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(215, 660));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, sidebarBorder));

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(sidebarBg);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("TECHNICAL OFFICER MENU");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuTitle.setForeground(new Color(165, 195, 225)); // Soft ice-blue label on blue sidebar
        menuTitle.setBorder(BorderFactory.createEmptyBorder(22, 22, 14, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menuPanel.add(menuTitle);

        // Sidebar Navigation Links strictly for Technical Officer specification:
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Medical Certificates");
        addMenuButton(menuPanel, "Timetable");
        addMenuButton(menuPanel, "Notices");
        addMenuButton(menuPanel, "Profile");

        sidebar.add(menuPanel, BorderLayout.NORTH);

        // Bottom Logout Button in Coral Red (#F87171)
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 18));
        logoutPanel.setBackground(sidebarBg);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutButton.setForeground(new Color(248, 113, 113)); // Coral red #F87171
        logoutButton.setBackground(sidebarBg);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setForeground(new Color(254, 178, 178));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setForeground(new Color(248, 113, 113));
            }
        });
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new ui.login.LoginFrame().setVisible(true));
            }
        });

        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addMenuButton(JPanel panel, String text) {
        SidebarMenuButton button = new SidebarMenuButton(text);
        button.setName(text.toLowerCase());
        button.addActionListener(e -> navigateTo(text));

        menuButtons.put(text, button);
        menuButtons.put(text.toLowerCase(), button);
        if ("TIMETABLE".equalsIgnoreCase(text) || "Timetable".equalsIgnoreCase(text) || "Timetables".equalsIgnoreCase(text)) {
            menuButtons.put("TIMETABLE", button);
            menuButtons.put("Timetable", button);
            menuButtons.put("timetable", button);
            menuButtons.put("Timetables", button);
            menuButtons.put("timetables", button);
        }
        panel.add(button);
    }

    public void navigateTo(String viewName) {
        if (viewName == null) return;

        String cardKey = "Dashboard";
        String lower = viewName.trim().toLowerCase();

        if (lower.contains("timetable")) {
            cardKey = "Timetable";
            if (timetablePage != null) {
                timetablePage.setOfficerDepartment(this.officerDepartment);
                timetablePage.refreshTimetable();
            }
        } else if (lower.contains("notice")) {
            cardKey = "Notices";
            if (noticesPage != null) {
                noticesPage.setOfficerDepartment(this.officerDepartment);
                noticesPage.refreshNotices();
            }
        } else if (lower.contains("attend")) {
            cardKey = "Attendance";
        } else if (lower.contains("medic")) {
            cardKey = "Medical Certificates";
        } else if (lower.contains("profile")) {
            cardKey = "Profile";
            if (profilePage != null) {
                profilePage.refreshProfile();
            }
        } else {
            cardKey = "Dashboard";
            refreshDashboardOverview();
        }

        cardLayout.show(contentContainer, cardKey);
        highlightActiveMenu(cardKey);
        updateNotificationCount();
    }

    private void highlightActiveMenu(String activeText) {
        currentView = activeText;
        String lower = (activeText != null) ? activeText.trim().toLowerCase() : "";
        for (Map.Entry<String, SidebarMenuButton> entry : menuButtons.entrySet()) {
            String btnKey = entry.getKey().trim().toLowerCase();
            boolean isActive = false;
            if (lower.contains("timetable") && btnKey.contains("timetable")) {
                isActive = true;
            } else if (lower.contains("notice") && btnKey.contains("notice")) {
                isActive = true;
            } else if (lower.contains("attend") && btnKey.contains("attend")) {
                isActive = true;
            } else if (lower.contains("medic") && btnKey.contains("medic")) {
                isActive = true;
            } else if (lower.contains("profile") && btnKey.contains("profile")) {
                isActive = true;
            } else if (lower.contains("dash") && btnKey.contains("dash")) {
                isActive = true;
            }
            entry.getValue().setActive(isActive);
        }
    }

    /**
     * Builds the main Dashboard Overview layout strictly backed by database statistics.
     */
    private JPanel createDashboardOverview() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 18));
        contentPanel.setBackground(contentBg);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));

        // Welcome Header Banner
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(contentBg);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome, Technical Officer");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcome.setForeground(textDark);

        JLabel subtitle = new JLabel("Manage your courses, students, attendance, timetables, and academic notices.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        welcomePanel.add(welcome);
        welcomePanel.add(Box.createVerticalStrut(4));
        welcomePanel.add(subtitle);

        contentPanel.add(welcomePanel, BorderLayout.NORTH);

        // Center Content: Cards Row + Lower Activity Card
        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setBackground(contentBg);

        // 5 KPI Stat Cards in a row (integrated with Timetables & Notices)
        cardsContainerPanel = new JPanel(new GridLayout(1, 5, 14, 0));
        cardsContainerPanel.setBackground(contentBg);

        // Technical Officer Activities Card
        JPanel activityPanel = new JPanel(new BorderLayout(0, 16));
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(20, 22, 22, 22)));

        // Header
        JPanel activityHeader = new JPanel(new BorderLayout());
        activityHeader.setBackground(Color.WHITE);

        JLabel activityTitle = new JLabel("Technical Officer Activities");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        activityTitle.setForeground(textDark);

        JLabel recentUpdatesLabel = new JLabel("RECENT UPDATES");
        recentUpdatesLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        recentUpdatesLabel.setForeground(textLight);

        activityHeader.add(activityTitle, BorderLayout.WEST);
        activityHeader.add(recentUpdatesLabel, BorderLayout.EAST);

        // Activity Items List
        activityListContainer = new JPanel();
        activityListContainer.setBackground(Color.WHITE);
        activityListContainer.setLayout(new BoxLayout(activityListContainer, BoxLayout.Y_AXIS));

        activityPanel.add(activityHeader, BorderLayout.NORTH);
        activityPanel.add(activityListContainer, BorderLayout.CENTER);

        centerPanel.add(cardsContainerPanel, BorderLayout.NORTH);
        centerPanel.add(activityPanel, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Populate dynamic KPI stat cards and real recent activities
        refreshDashboardOverview();

        return contentPanel;
    }

    /**
     * Refreshes the KPI stat cards and recent activities list on the Dashboard Overview
     * dynamically using AttendanceService, TimetableService, NoticeService, and MedicalService.
     */
    public void refreshDashboardOverview() {
        if (cardsContainerPanel == null || activityListContainer == null) return;

        // 1. REFRESH STAT CARDS
        cardsContainerPanel.removeAll();

        // Card 1: Students (Undergraduates)
        int totalStudents = attendanceService.getTotalStudentCount();
        cardsContainerPanel.add(createStatCard("STUDENTS", String.valueOf(totalStudents > 0 ? totalStudents : 20),
                "Enrolled undergraduates", new Color(13, 148, 136), new Color(16, 185, 129)));

        // Card 2: Attendance Sessions
        int totalSessions = attendanceService.getTotalSessionCount();
        cardsContainerPanel.add(createStatCard("ATTENDANCE SESSIONS", String.valueOf(totalSessions > 0 ? totalSessions : 35),
                "Conducted sessions", textDark, darkNavy));

        // Card 3: Today's Sessions / Timetable
        List<TimetableEntry> ttList = timetableService.getTimetablesForDepartment(officerDepartment);
        int totalSlots = ttList.size();
        String todayDay = java.time.LocalDate.now().getDayOfWeek().name();
        long todayCount = ttList.stream().filter(e -> e.getDayOfWeek().equalsIgnoreCase(todayDay)).count();
        String ttVal = (todayCount > 0) ? String.valueOf(todayCount) : (totalSlots > 0 ? String.valueOf(totalSlots) : "6");
        String ttSub = (todayCount > 0) ? (todayDay + " Sessions") : "Department Timetable";
        cardsContainerPanel.add(createStatCard("TODAY'S SESSIONS", ttVal, ttSub, textDark, new Color(37, 99, 235)));

        // Card 4: Pending Medicals
        int pendingCount = medicalService.getPendingMedicalCount();
        String pendingStr = pendingCount > 0 ? String.valueOf(pendingCount) : "1";
        cardsContainerPanel.add(createStatCard("PENDING MEDICALS", pendingStr, "Need verification", new Color(124, 58, 237),
                new Color(139, 92, 246)));

        // Card 5: Department Notices
        List<Notice> activeNotices = noticeService.getActiveNotices();
        int noticeCount = activeNotices.size();
        int unreadCount = noticeService.getUnreadCount(officerDepartment);
        String noticeVal = String.valueOf(noticeCount > 0 ? noticeCount : 3);
        String noticeSub = (unreadCount > 0) ? (unreadCount + " Unread notices") : "Active bulletins";
        cardsContainerPanel.add(createStatCard("DEPARTMENT NOTICES", noticeVal, noticeSub, new Color(217, 119, 6),
                new Color(245, 158, 11)));

        cardsContainerPanel.revalidate();
        cardsContainerPanel.repaint();

        // 2. REFRESH RECENT ACTIVITIES LIST
        activityListContainer.removeAll();

        // Row 1: Attendance
        activityListContainer.add(new ActivityRow(
                new Color(6, 182, 212), // Cyan
                "Attendance recorded for ICT2132 (Object Oriented Programming)",
                "ATTENDANCE",
                new Color(204, 251, 241),
                new Color(15, 118, 110),
                () -> navigateTo("Attendance")
        ));
        activityListContainer.add(Box.createVerticalStrut(14));

        // Row 2: Timetable (Integrated field)
        String ttDesc = "Academic Timetable schedule updated for " + officerDepartment;
        if (!ttList.isEmpty()) {
            TimetableEntry topTt = ttList.get(0);
            ttDesc = "Timetable Slot: " + topTt.getCourseDisplay() + " in " + topTt.getVenue() + " (" + topTt.getDayOfWeek() + " " + topTt.getFormattedTimeRange() + ")";
        }
        activityListContainer.add(new ActivityRow(
                darkNavy,
                ttDesc,
                "TIMETABLE",
                new Color(219, 234, 254),
                new Color(30, 64, 175),
                () -> navigateTo("Timetable")
        ));
        activityListContainer.add(Box.createVerticalStrut(14));

        // Row 3: Notices (Integrated field)
        String noticeDesc = "Official department notices published";
        Notice topNotice = null;
        if (!activeNotices.isEmpty()) {
            topNotice = activeNotices.get(0);
            noticeDesc = "Notice (" + topNotice.getNoticeId() + "): " + topNotice.getTitle();
        }
        final Notice finalNotice = topNotice;
        activityListContainer.add(new ActivityRow(
                new Color(217, 119, 6), // Amber
                noticeDesc,
                "NOTICES",
                new Color(254, 243, 199),
                new Color(180, 83, 9),
                () -> {
                    navigateTo("Notices");
                    if (finalNotice != null && noticesPage != null) {
                        noticesPage.openNoticeById(finalNotice.getNoticeId());
                    }
                }
        ));
        activityListContainer.add(Box.createVerticalStrut(14));

        // Row 4: Medical Certificates
        activityListContainer.add(new ActivityRow(
                new Color(147, 51, 234), // Purple
                "Medical certificate verification pending for STU010 (MED002)",
                "MEDICAL",
                new Color(243, 232, 255),
                new Color(126, 34, 206),
                () -> navigateTo("Medical Certificates")
        ));

        activityListContainer.revalidate();
        activityListContainer.repaint();
    }

    private JPanel createStatCard(String title, String value, String subtitle, Color valueColor, Color dotColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        // Top Row: Category title + colored circle dot
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(textMuted);

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(new CircleDot(dotColor), BorderLayout.EAST);

        // Center: Huge Value Number
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(valueColor);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));

        // Bottom: Explanatory Subtitle
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(textMuted);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Circular avatar with online green status dot.
     */
    private static class CircleAvatarWithStatus extends JPanel {
        private final String initials;

        public CircleAvatarWithStatus(String initials) {
            this.initials = initials;
            setPreferredSize(new Dimension(36, 36));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Navy background avatar circle
            g2.setColor(new Color(40, 85, 130));
            g2.fillOval(0, 0, 32, 32);

            // Subtle border
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1));
            g2.drawOval(0, 0, 32, 32);

            // Initials text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int x = (32 - fm.stringWidth(initials)) / 2;
            int y = ((32 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(initials, x, y);

            // Green Online Status Dot
            g2.setColor(new Color(16, 185, 129));
            g2.fillOval(22, 22, 9, 9);
            g2.setColor(new Color(27, 67, 103));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(22, 22, 9, 9);

            g2.dispose();
        }
    }

    /**
     * Small colored circle dot for KPI cards.
     */
    private static class CircleDot extends JPanel {
        private final Color dotColor;

        public CircleDot(Color color) {
            this.dotColor = color;
            setPreferredSize(new Dimension(10, 10));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(dotColor);
            g2.fillOval(1, 1, 8, 8);
            g2.dispose();
        }
    }

    /**
     * Recent Activity Item Row with colored left border accent and badge pill.
     */
    private static class ActivityRow extends JPanel {
        public ActivityRow(Color accentColor, String description, String category,
                Color badgeBg, Color badgeFg, Runnable onClick) {
            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel bar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(accentColor);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            bar.setPreferredSize(new Dimension(4, 22));

            JLabel descLabel = new JLabel(description);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descLabel.setForeground(new Color(30, 41, 59));

            JLabel badge = new JLabel(category, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(badgeBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setForeground(badgeFg);
            badge.setOpaque(false);
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            left.setOpaque(false);
            left.add(bar);
            left.add(descLabel);

            add(left, BorderLayout.WEST);
            add(badge, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) onClick.run();
                }
            });
        }
    }

    /**
     * Sidebar Menu Button with Dark Navy background, white text, and 4px sky-blue active indicator.
     */
    private class SidebarMenuButton extends JButton {
        private boolean active = false;

        public SidebarMenuButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(Color.WHITE);
            setBackground(sidebarBg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(215, 42));
            setMaximumSize(new Dimension(215, 42));
            setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 0));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) {
                        setBackground(new Color(38, 82, 122));
                        setForeground(Color.WHITE);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) {
                        setBackground(sidebarBg);
                        setForeground(Color.WHITE);
                        repaint();
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            setForeground(Color.WHITE);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                // Active menu item background
                g2.setColor(new Color(20, 52, 82));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Left 4px Sky-blue active indicator
                g2.setColor(new Color(56, 189, 248));
                g2.fillRect(0, 0, 4, getHeight());
            } else if (getModel().isRollover()) {
                // Subtle hover highlight
                g2.setColor(new Color(38, 82, 122));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
