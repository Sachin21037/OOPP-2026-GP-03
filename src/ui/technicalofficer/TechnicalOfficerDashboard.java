package ui.technicalofficer;

import service.MedicalService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * TechnicalOfficerDashboard Class
 * Package: ui.technicalofficer
 * Acts as the master dashboard frame for the Technical Officer role.
 * Aligned with database/fams.sql schema and connects Attendance and Medical management.
 */
public class TechnicalOfficerDashboard extends JFrame {

    // theme colors
    private final Color darkBlue = new Color(27, 67, 103);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color sidebarColor = Color.WHITE;
    private final Color textColor = new Color(35, 45, 55);
    private final Color activeMenuColor = new Color(235, 242, 250);
    private final Color borderColor = new Color(225, 228, 232);

    // services
    private final MedicalService medicalService = new MedicalService();

    // views container
    private CardLayout cardLayout;
    private JPanel contentContainer;
    private AttendanceManagementPage attendancePage;
    private MedicalManagementPage medicalPage;
    private Map<String, JButton> menuButtons = new HashMap<>();
    private String currentView = "Dashboard";

    public TechnicalOfficerDashboard() {
        setTitle("Technical Officer Dashboard - Faculty Academic Management System");
        setSize(1200, 760);
        setMinimumSize(new Dimension(1040, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // main window layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightBackground);

        // top blue navigation header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(1200, 65));

        JLabel systemTitle = new JLabel("  FACULTY ACADEMIC MANAGEMENT SYSTEM");
        systemTitle.setForeground(Color.WHITE);
        systemTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel userType = new JLabel("TECHNICAL OFFICER (TO001 - Dinesh Kumara)  ");
        userType.setForeground(new Color(180, 240, 220));
        userType.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(systemTitle, BorderLayout.WEST);
        header.add(userType, BorderLayout.EAST);

        // left sidebar menu
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(215, 695));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("  TECHNICAL OFFICER MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 11));
        menuTitle.setForeground(new Color(110, 120, 130));
        menuTitle.setBorder(BorderFactory.createEmptyBorder(20, 12, 15, 0));

        menuPanel.add(menuTitle);

        // sidebar items
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Medical Certificates");
        addMenuButton(menuPanel, "Lab Maintenance");
        addMenuButton(menuPanel, "Notices");
        addMenuButton(menuPanel, "My Profile");

        sidebar.add(menuPanel, BorderLayout.NORTH);

        // logout button at bottom of sidebar
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 13));
        logoutButton.setForeground(new Color(210, 60, 60));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        // central container switching between views
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(lightBackground);

        // 1. Dashboard Overview
        JPanel dashboardOverview = createDashboardOverview();
        contentContainer.add(dashboardOverview, "Dashboard");

        // 2. Attendance Management Page
        attendancePage = new AttendanceManagementPage();
        contentContainer.add(attendancePage, "Attendance");

        // 3. Medical Management Page
        medicalPage = new MedicalManagementPage();
        contentContainer.add(medicalPage, "Medical Certificates");

        // 4. Lab Maintenance Placeholder Page
        JPanel maintenancePage = createLabMaintenancePanel();
        contentContainer.add(maintenancePage, "Lab Maintenance");

        // 5. Notices Page
        JPanel noticesPage = createNoticesPanel();
        contentContainer.add(noticesPage, "Notices");

        // 6. My Profile Page
        JPanel profilePage = createProfilePanel();
        contentContainer.add(profilePage, "My Profile");

        // assemble main frame
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentContainer, BorderLayout.CENTER);

        // set initial active button state
        highlightActiveMenu("Dashboard");

        add(mainPanel);
        setVisible(true);
    }

    // create the default dashboard overview view
    private JPanel createDashboardOverview() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(lightBackground);

        // welcome header
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(lightBackground);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(22, 25, 15, 25));

        JLabel welcome = new JLabel("Welcome, Technical Officer");
        welcome.setFont(new Font("Arial", Font.BOLD, 26));
        welcome.setForeground(textColor);

        JLabel subtitle = new JLabel("Manage lab attendance sessions, monitor student medical exemptions, and track laboratory status.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 110, 120));

        welcomePanel.add(welcome);
        welcomePanel.add(Box.createVerticalStrut(5));
        welcomePanel.add(subtitle);

        contentPanel.add(welcomePanel, BorderLayout.NORTH);

        // dashboard center body
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(lightBackground);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

        // 4 top stat cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsPanel.setBackground(lightBackground);

        int pendingCount = medicalService.getPendingMedicalCount();
        cardsPanel.add(createCard("Assigned Labs", "3", new Color(45, 100, 160)));
        cardsPanel.add(createCard("Today's Sessions", "5", new Color(20, 165, 145)));
        cardsPanel.add(createCard("Pending Medicals", String.valueOf(pendingCount), new Color(115, 75, 180)));
        cardsPanel.add(createCard("Equipment Faults", "2", new Color(210, 60, 60)));

        // recent activities card
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 228, 232)),
                        BorderFactory.createEmptyBorder(18, 18, 18, 18)
                )
        );

        JLabel activityTitle = new JLabel("Recent System Activities");
        activityTitle.setFont(new Font("Arial", Font.BOLD, 15));
        activityTitle.setForeground(textColor);

        JPanel activityList = new JPanel();
        activityList.setBackground(Color.WHITE);
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        addActivity(activityList, "Practical attendance recorded for OFF001 - ICT2132 Object Oriented Programming (Session #05)");
        addActivity(activityList, "Medical certificate MED001 approved for STU005 (Sanduni Fernando - TG/2024/2005)");
        addActivity(activityList, "Batch eligibility evaluated: 17 students eligible (>80%), 3 barred (<80%)");
        addActivity(activityList, "Pending medical certificate MED002 logged for STU010 (Piumi Kumari) awaiting verification");
        addActivity(activityList, "Attendance and medical records synchronized with MySQL database (fams.sql schema)");

        activityPanel.add(activityTitle, BorderLayout.NORTH);
        activityPanel.add(activityList, BorderLayout.CENTER);

        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(activityPanel, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        return contentPanel;
    }

    private JPanel createLabMaintenancePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(lightBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Laboratory Maintenance & Status");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(textColor);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(20, 20, 20, 20)));

        JLabel sub = new JLabel("<html><b>Lab Facilities Assigned to Technical Officer TO001:</b><br><br>" +
                "• <b>Computer Lab 01</b> (Level I) - 50 Workstations [Operational]<br>" +
                "• <b>Software Engineering Lab 02</b> (Level II) - 45 Workstations [Operational - Session #05 active]<br>" +
                "• <b>Networking & Systems Lab 03</b> (Level III) - 40 Workstations [2 Router equipment faults under maintenance]<br><br>" +
                "All maintenance logs are recorded under Faculty of Technology IT Infrastructure By-Laws.</html>");
        sub.setFont(new Font("Arial", Font.PLAIN, 14));
        sub.setForeground(textColor);
        card.add(sub, BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNoticesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(lightBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Faculty Academic Notices");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(textColor);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(20, 20, 20, 20)));

        JLabel sub = new JLabel("<html><b>Official Notices (Notices Table - database/fams.sql):</b><br><br>" +
                "1. <b>Continuous Assessment Attendance Threshold:</b> Minimum 80.0% attendance strictly required for end-semester exams.<br>" +
                "2. <b>Medical Submissions:</b> Students must submit authentic medical certificates within 7 days of absence to the Technical Officer.<br>" +
                "3. <b>Laboratory Schedule 2026/2027:</b> All Level II practical sessions will be held in Lab 02.</html>");
        sub.setFont(new Font("Arial", Font.PLAIN, 14));
        sub.setForeground(textColor);
        card.add(sub, BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(lightBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Technical Officer Profile");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(textColor);

        JPanel card = new JPanel(new GridLayout(6, 2, 10, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(24, 24, 24, 24)));

        addProfileField(card, "Officer ID:", "TO001");
        addProfileField(card, "Staff No:", "TO-STF-001");
        addProfileField(card, "Full Name:", "Dinesh Kumara");
        addProfileField(card, "Department:", "Information and Communication Technology");
        addProfileField(card, "Email:", "dinesh.to@uor.lk");
        addProfileField(card, "Phone:", "0710000007");

        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void addProfileField(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        l.setForeground(darkBlue);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.PLAIN, 14));
        v.setForeground(textColor);

        panel.add(l);
        panel.add(v);
    }

    private void addMenuButton(JPanel panel, String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setForeground(textColor);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        button.addActionListener(e -> {
            cardLayout.show(contentContainer, text);
            highlightActiveMenu(text);
        });

        menuButtons.put(text, button);
        panel.add(button);
    }

    private void highlightActiveMenu(String activeText) {
        currentView = activeText;
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(activeText)) {
                entry.getValue().setBackground(activeMenuColor);
                entry.getValue().setFont(new Font("Arial", Font.BOLD, 13));
            } else {
                entry.getValue().setBackground(Color.WHITE);
                entry.getValue().setFont(new Font("Arial", Font.PLAIN, 13));
            }
        }
    }

    private JPanel createCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 228, 232)),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)
                )
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 110, 120));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(valueColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        if ("Today's Sessions".equalsIgnoreCase(title)) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    cardLayout.show(contentContainer, "Attendance");
                    highlightActiveMenu("Attendance");
                }
            });
        } else if ("Pending Medicals".equalsIgnoreCase(title)) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    cardLayout.show(contentContainer, "Medical Certificates");
                    highlightActiveMenu("Medical Certificates");
                }
            });
        }

        return card;
    }

    private void addActivity(JPanel panel, String text) {
        JLabel activity = new JLabel("• " + text);
        activity.setFont(new Font("Arial", Font.PLAIN, 13));
        activity.setForeground(new Color(70, 80, 90));
        activity.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(activity);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new TechnicalOfficerDashboard());
    }
}
