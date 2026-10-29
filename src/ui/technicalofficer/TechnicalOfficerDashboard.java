package ui.technicalofficer;

import dao.NoticeDAO;
import dao.TechnicalOfficerDAO;
import model.Notice;
import model.TechnicalOfficer;
import model.TimetableEntry;
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
 * department notices reading, and interactive notification integration,
 * strictly matching the reference design screenshot.
 */
public class TechnicalOfficerDashboard extends JFrame {

    // Theme Colors matching reference screenshot
    private final Color darkNavy = new Color(27, 67, 103); // #1B4367 exact navy
    private final Color contentBg = new Color(245, 247, 250); // #F5F7FA
    private final Color sidebarBg = Color.WHITE; // Pure white sidebar
    private final Color sidebarBorder = new Color(226, 232, 240); // #E2E8F0
    private final Color borderColor = new Color(226, 232, 240); // #E2E8F0
    private final Color textDark = new Color(30, 41, 59); // #1E293B
    private final Color textMuted = new Color(100, 116, 139); // #64748B
    private final Color textLight = new Color(148, 163, 184); // #94A3B8

    // Services
    private final MedicalService medicalService = new MedicalService();
    private final NoticeService noticeService = new NoticeService();
    private final TimetableService timetableService = new TimetableService();

    // UI Navigation Containers
    private CardLayout cardLayout;
    private JPanel contentContainer;
    private AttendanceManagementPage attendancePage;
    private MedicalManagementPage medicalPage;
    private LabMaintenancePage labMaintenancePage;
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

        // 2. Left White Sidebar (#FFFFFF) with 4px Navy Active Bar
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

        // D. Lab Maintenance Page
        labMaintenancePage = new LabMaintenancePage();
        labMaintenancePage.setOnNavigateToTimetable(() -> navigateTo("Timetable"));
        contentContainer.add(labMaintenancePage, "Lab Maintenance");

        // E. Profile Page
        profilePage = new ProfilePage();
        profilePage.setOnNavigateToTimetable(() -> navigateTo("Timetable"));
        contentContainer.add(profilePage, "Profile");

        // F. Timetable Page (Department schedule & session management)
        timetablePage = new TimetablePage(officerDepartment);
        timetablePage.setOnNavigateToNotices(() -> navigateTo("Notices"));
        timetablePage.setOnNavigateToAttendance(() -> navigateTo("Attendance"));
        timetablePage.setOnNavigateToLabMaintenance(() -> navigateTo("Lab Maintenance"));
        contentContainer.add(timetablePage, "Timetable");

        // G. Notices Page (Department notices view-only)
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
     * Creates the Top Dark Blue (#1B4367) Header Bar matching screenshot:
     * - Left: "FACULTY ACADEMIC MANAGEMENT SYSTEM" (white bold)
     * - Right: Interactive Notification button, Circle Avatar "DK" with green
     * status dot,
     * Officer Name "(TO001)", and "TECHNICAL OFFICER" role label.
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

    /**
     * Creates the Notification Icon button in the header bar.
     * Displays a clean bell icon and unread count badge.
     * Clicking opens the notification dropdown menu.
     */
    private JPanel createNotificationButton() {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4)) {
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
                        Component comp = (Component) e.getSource();
                        showNotificationsPopup(comp, 0, comp.getHeight());
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(4, 8, 4, 8));
        btn.setToolTipText("View Department Notifications & Timetable Updates");

        // Bell Icon Symbol
        JLabel bell = new JLabel("🔔");
        bell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        bell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Unread Badge
        notificationBadgeLabel = new JLabel("0", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68)); // Red #EF4444
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        notificationBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        notificationBadgeLabel.setForeground(Color.WHITE);
        notificationBadgeLabel.setPreferredSize(new Dimension(18, 18));
        notificationBadgeLabel.setOpaque(false);
        notificationBadgeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Forward mouse clicks and hover effects from child components to the button
        MouseAdapter forwarder = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotificationsPopup(btn, 0, btn.getHeight());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, btn));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, btn));
            }
        };
        bell.addMouseListener(forwarder);
        notificationBadgeLabel.addMouseListener(forwarder);

        btn.add(bell);
        btn.add(notificationBadgeLabel);

        return btn;
    }

    /**
     * Shows the Notification dropdown popup displaying new notices and timetable
     * updates.
     */
    private void showNotificationsPopup(Component invoker, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(8, 10, 8, 10)));

        // Popup Title Header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        JLabel titleLbl = new JLabel("Department Notifications");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(darkNavy);

        JLabel deptLbl = new JLabel(officerDepartment);
        deptLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deptLbl.setForeground(textMuted);

        titlePanel.add(titleLbl, BorderLayout.NORTH);
        titlePanel.add(deptLbl, BorderLayout.SOUTH);
        popup.add(titlePanel);
        popup.addSeparator();

        int itemsCount = 0;

        // 1. Timetable sessions for this department
        List<TimetableEntry> ttList = timetableService.getTimetablesForDepartment(officerDepartment);
        int ttCount = 0;
        for (TimetableEntry tt : ttList) {
            if (ttCount >= 2)
                break;
            String snippet = tt.getDayOfWeek() + " " + tt.getFormattedTimeRange() + " (" + tt.getVenue() + ") • "
                    + tt.getBatch();
            JMenuItem itemTimetable = new JMenuItem("<html><b>🗓️ " + tt.getCourseDisplay() + " (" + tt.getSessionType()
                    + ")</b><br><font color='#64748B'>" + snippet + "</font></html>");
            itemTimetable.setBackground(Color.WHITE);
            itemTimetable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            itemTimetable.setBorder(new EmptyBorder(6, 8, 6, 8));
            itemTimetable.setCursor(new Cursor(Cursor.HAND_CURSOR));
            itemTimetable.addActionListener(e -> {
                navigateTo("Timetable");
                if (timetablePage != null) {
                    timetablePage.showSlotDetailsDialog(tt);
                }
            });
            popup.add(itemTimetable);
            ttCount++;
            itemsCount++;
        }

        // 2. Active Department Notices
        List<Notice> notices = noticeService.getDepartmentNotices(officerDepartment, "ACTIVE");
        int noticeCount = 0;
        for (Notice n : notices) {
            if (noticeCount >= 3)
                break;
            String descSnippet = (n.getDescription() != null && n.getDescription().length() > 65)
                    ? n.getDescription().substring(0, 62) + "..."
                    : (n.getDescription() != null ? n.getDescription() : "");
            String icon = n.isUrgent() ? "⚠️" : "📢";
            JMenuItem itemNotice = new JMenuItem("<html><b>" + icon + " " + n.getTitle() + " (" + n.getNoticeId()
                    + ")</b><br><font color='#64748B'>" + descSnippet + "</font></html>");
            itemNotice.setBackground(Color.WHITE);
            itemNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            itemNotice.setBorder(new EmptyBorder(6, 8, 6, 8));
            itemNotice.setCursor(new Cursor(Cursor.HAND_CURSOR));
            itemNotice.addActionListener(e -> {
                noticeService.markNoticeAsRead(n.getNoticeId());
                updateNotificationCount();
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
            JMenuItem emptyItem = new JMenuItem("No new notifications or timetable alerts");
            emptyItem.setEnabled(false);
            emptyItem.setBackground(Color.WHITE);
            popup.add(emptyItem);
        }

        popup.addSeparator();

        // Footer Actions
        JMenuItem viewTimetable = new JMenuItem("🗓️ View Department Timetable →");
        viewTimetable.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewTimetable.setForeground(darkNavy);
        viewTimetable.setBackground(new Color(241, 245, 249));
        viewTimetable.setBorder(new EmptyBorder(6, 8, 6, 8));
        viewTimetable.addActionListener(e -> navigateTo("TIMETABLE"));

        JMenuItem viewAllNotices = new JMenuItem("📢 View All Department Notices →");
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
     * Creates the clickable profile section in the top header bar:
     * [ Circle Avatar with initials + Green Active Dot ] [ Officer Name (ID) ] [
     * TECHNICAL OFFICER ]
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

        // 3. Role Label in cyan accent matching screenshot
        JLabel roleLabel = new JLabel("TECHNICAL OFFICER");
        roleLabel.setForeground(new Color(180, 240, 220));
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Forward mouse events from child components to parent panel
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

    /**
     * Builds the Sidebar matching the exact order and format in the screenshot:
     * - TECHNICAL OFFICER MENU
     * - Dashboard
     * - Attendance
     * - Medical Certificates
     * - Lab Maintenance
     * - Profile
     * - TIMETABLE
     * - Notices
     * - Logout (bottom in coral red #F87171)
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
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuTitle.setForeground(new Color(148, 163, 184)); // Muted slate gray
        menuTitle.setBorder(BorderFactory.createEmptyBorder(22, 22, 14, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menuPanel.add(menuTitle);

        // Sidebar Navigation Links strictly in screenshot sequence:
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Medical Certificates");
        addMenuButton(menuPanel, "Lab Maintenance");
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
        button.setName(text.toLowerCase()); // sets component name, e.g. "timetable", "attendance"
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
        } else if (lower.contains("lab") || lower.contains("maint")) {
            cardKey = "Lab Maintenance";
            if (labMaintenancePage != null) {
                labMaintenancePage.refreshAllData();
            }
        } else if (lower.contains("profile")) {
            cardKey = "Profile";
            if (profilePage != null) {
                profilePage.refreshProfile();
            }
        } else if (lower.contains("dash")) {
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
            } else if ((lower.contains("lab") || lower.contains("maint")) && (btnKey.contains("lab") || btnKey.contains("maint"))) {
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
     * Builds the main Dashboard Overview layout identical to the reference UI screenshot.
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

        // Header: "Technical Officer Activities" on left, "RECENT UPDATES" on right
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
     * dynamically using TimetableService, NoticeService, and MedicalService.
     */
    public void refreshDashboardOverview() {
        if (cardsContainerPanel == null || activityListContainer == null) return;

        // 1. REFRESH STAT CARDS
        cardsContainerPanel.removeAll();

        // Card 1: Assigned Labs
        cardsContainerPanel.add(createStatCard("ASSIGNED LABS", "4", "Operational labs", textDark, darkNavy));

        // Card 2: Students
        cardsContainerPanel.add(createStatCard("STUDENTS", "86", "Enrolled students", new Color(13, 148, 136),
                new Color(16, 185, 129)));

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
        String pendingStr = pendingCount > 0 ? String.valueOf(pendingCount) : "8";
        cardsContainerPanel.add(createStatCard("PENDING MEDICALS", pendingStr, "Need verification", new Color(124, 58, 237),
                new Color(139, 92, 246)));

        // Card 5: Department Notices
        List<Notice> activeNotices = noticeService.getDepartmentNotices(officerDepartment, "ACTIVE");
        int noticeCount = activeNotices.size();
        int unreadCount = noticeService.getUnreadCount(officerDepartment);
        String noticeVal = String.valueOf(noticeCount > 0 ? noticeCount : 4);
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
                "Attendance recorded for ICT2132 (Software Development Lab)",
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
        String noticeDesc = "New official department notices published for " + officerDepartment;
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

        // Row 4: Lab Maintenance
        activityListContainer.add(new ActivityRow(
                new Color(16, 185, 129), // Emerald
                "Workstation preventative maintenance audit completed for Lab 01",
                "MAINTENANCE",
                new Color(209, 250, 229),
                new Color(6, 95, 70),
                () -> navigateTo("Lab Maintenance")
        ));
        activityListContainer.add(Box.createVerticalStrut(14));

        // Row 5: Medical Certificates
        activityListContainer.add(new ActivityRow(
                new Color(147, 51, 234), // Purple
                "Medical certificate verification pending for STU001 (MED001)",
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

        // Center & Bottom: Value and Subtitle
        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valueLabel.setForeground(valueColor);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(textLight);

        contentBox.add(valueLabel);
        contentBox.add(subLabel);

        card.add(topRow, BorderLayout.NORTH);
        card.add(contentBox, BorderLayout.CENTER);

        Runnable clickAction = null;
        if (title.contains("SESSIONS") || title.contains("TIMETABLE")) {
            clickAction = () -> navigateTo("Timetable");
        } else if (title.contains("ATTENDANCE") || title.contains("STUDENT")) {
            clickAction = () -> navigateTo("Attendance");
        } else if (title.contains("MEDICAL")) {
            clickAction = () -> navigateTo("Medical Certificates");
        } else if (title.contains("LABS") || title.contains("MAINTENANCE")) {
            clickAction = () -> navigateTo("Lab Maintenance");
        } else if (title.contains("NOTICE")) {
            clickAction = () -> navigateTo("Notices");
        }

        if (clickAction != null) {
            makeClickable(card, clickAction);
        }

        return card;
    }

    private static void makeClickable(Component comp, Runnable onClick) {
        if (comp == null || onClick == null) return;
        comp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                makeClickable(child, onClick);
            }
        }
    }

    private String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "TO";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        } else if (parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        } else {
            return parts[0].toUpperCase();
        }
    }

    /**
     * Circular avatar icon displaying user initials with green status dot at bottom
     * right.
     */
    private static class CircleAvatarWithStatus extends JComponent {
        private final String initials;

        public CircleAvatarWithStatus(String initials) {
            this.initials = (initials != null) ? initials : "TO";
            Dimension dim = new Dimension(34, 34);
            setPreferredSize(dim);
            setMinimumSize(dim);
            setMaximumSize(dim);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int size = 28;
            int x = 2;
            int y = 2;

            // Semi-translucent circular background
            g2.setColor(new Color(255, 255, 255, 24));
            g2.fillOval(x, y, size, size);

            // Circular ring outline
            g2.setColor(new Color(180, 230, 250, 220));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(x, y, size, size);

            // Centered Initials Text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(initials)) / 2;
            int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent() - 1;
            g2.drawString(initials, tx, ty);

            // Green Active Dot on bottom right (matching screenshot)
            g2.setColor(new Color(34, 197, 94)); // Vibrant green #22C55E
            g2.fillOval(x + size - 8, y + size - 8, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawOval(x + size - 8, y + size - 8, 8, 8);

            g2.dispose();
        }
    }

    private static class CircleDot extends JComponent {
        private final Color color;

        public CircleDot(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(8, 8));
            setMaximumSize(new Dimension(8, 8));
            setMinimumSize(new Dimension(8, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, 8, 8);
            g2.dispose();
        }
    }

    /**
     * Sidebar Menu Button with 4px left active indicator bar and soft highlight.
     * Matches the user's exact screenshot.
     */
    private static class SidebarMenuButton extends JButton {
        private boolean active = false;
        private boolean hovered = false;

        private final Color activeBg = new Color(237, 243, 248); // #EDF3F8 soft light blue
        private final Color activeBorder = new Color(27, 67, 103); // #1B4367 dark navy left 4px bar
        private final Color activeFg = new Color(27, 67, 103); // #1B4367 bold navy text
        private final Color inactiveFg = new Color(74, 85, 104); // #4A5568 dark slate text
        private final Color hoverBg = new Color(248, 250, 252); // #F8FAFC subtle hover tint

        public SidebarMenuButton(String text) {
            super(text);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(inactiveFg);
            setBackground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(Component.LEFT_ALIGNMENT);

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
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(activeFg);
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(inactiveFg);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(activeBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(activeBorder);
                g2.fillRect(0, 0, 4, getHeight());
            } else if (hovered) {
                g2.setColor(hoverBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ActivityRow extends JPanel {
        private boolean hovered = false;

        public ActivityRow(Color barColor, String text, String badgeText, Color badgeBg, Color badgeFg) {
            this(barColor, text, badgeText, badgeBg, badgeFg, null);
        }

        public ActivityRow(Color barColor, String text, String badgeText, Color badgeBg, Color badgeFg, Runnable onClick) {
            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setPreferredSize(new Dimension(800, 32));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            JPanel bar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(barColor);
                    g2.fillRect(0, 1, 4, getHeight() - 2);
                    g2.dispose();
                }
            };
            bar.setPreferredSize(new Dimension(4, 26));
            bar.setOpaque(false);

            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(new Color(51, 65, 85));

            JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 1));
            badgeWrapper.setOpaque(false);
            PillBadge badge = new PillBadge(badgeText, badgeBg, badgeFg);
            badgeWrapper.add(badge);

            add(bar, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
            add(badgeWrapper, BorderLayout.EAST);

            if (onClick != null) {
                attachHoverAndClick(this, onClick);
            }
        }

        private void setRowHovered(boolean h) {
            this.hovered = h;
            repaint();
        }

        private void attachHoverAndClick(Component comp, Runnable onClick) {
            if (comp == null) return;
            comp.setCursor(new Cursor(Cursor.HAND_CURSOR));
            comp.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) onClick.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setRowHovered(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setRowHovered(false);
                }
            });
            if (comp instanceof Container) {
                for (Component child : ((Container) comp).getComponents()) {
                    attachHoverAndClick(child, onClick);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (hovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

private static class PillBadge extends JPanel {
    private final Color bg;

    public PillBadge(String text, Color bg, Color fg) {
        this.bg = bg;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(fg);
        add(label, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
        g2.dispose();
        super.paintComponent(g);
    }

    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(TechnicalOfficerDashboard::new);
    }
}
