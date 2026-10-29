package ui.technicalofficer;

import service.MedicalService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main dashboard window for Technical Officers.
 * Provides sidebar navigation for attendance, medical records, lab maintenance, and profile.
 */
public class TechnicalOfficerDashboard extends JFrame {

    // Theme Colors
    private final Color darkNavy = new Color(27, 66, 104);       // #1B4268
    private final Color contentBg = new Color(248, 250, 252);     // #F8FAFC
    private final Color sidebarBg = darkNavy;                     // Dark Navy matching reference
    private final Color sidebarBorder = new Color(20, 50, 82);
    private final Color borderColor = new Color(226, 232, 240);   // #E2E8F0
    private final Color textDark = new Color(30, 41, 59);         // #1E293B
    private final Color textMuted = new Color(100, 116, 139);     // #64748B
    private final Color textLight = new Color(148, 163, 184);     // #94A3B8

    // Services
    private final MedicalService medicalService = new MedicalService();

    // UI Navigation
    private CardLayout cardLayout;
    private JPanel contentContainer;
    private AttendanceManagementPage attendancePage;
    private MedicalManagementPage medicalPage;
    private LabMaintenancePage labMaintenancePage;
    private ProfilePage profilePage;
    private final Map<String, SidebarMenuButton> menuButtons = new HashMap<>();
    private String currentView = "Dashboard";

    private final String officerId;
    private final String officerName;

    public TechnicalOfficerDashboard() {
        this("TO001", "Dinesh Kumara");
    }

    public TechnicalOfficerDashboard(String officerId, String officerName) {
        this.officerId = (officerId != null && !officerId.isBlank()) ? officerId : "TO001";
        this.officerName = (officerName != null && !officerName.isBlank()) ? officerName : "Dinesh Kumara";
        setTitle("Faculty Academic Management System - Technical Officer");
        setSize(1180, 720);
        setMinimumSize(new Dimension(1040, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Root Container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(contentBg);

        // 1. Top Navy Header Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkNavy);
        header.setPreferredSize(new Dimension(1180, 60));

        JLabel systemTitle = new JLabel("FACULTY ACADEMIC MANAGEMENT SYSTEM");
        systemTitle.setForeground(Color.WHITE);
        systemTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        systemTitle.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        // Right Interactive Profile Header
        JPanel profileHeaderPanel = createProfileHeaderPanel();

        header.add(systemTitle, BorderLayout.WEST);
        header.add(profileHeaderPanel, BorderLayout.EAST);

        // 2. Left Dark Navy Sidebar matching reference
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(210, 660));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, sidebarBorder));

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(sidebarBg);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("TECHNICAL OFFICER MENU");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuTitle.setForeground(new Color(140, 185, 220)); // Soft light blue matching screenshot
        menuTitle.setBorder(BorderFactory.createEmptyBorder(22, 22, 14, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menuPanel.add(menuTitle);

        // Sidebar Navigation Links
        addMenuButton(menuPanel, "Dashboard");
        addMenuButton(menuPanel, "Attendance");
        addMenuButton(menuPanel, "Medical Certificates");
        addMenuButton(menuPanel, "Lab Maintenance");
        addMenuButton(menuPanel, "Profile");

        sidebar.add(menuPanel, BorderLayout.NORTH);

        // Bottom Logout Button in Coral Red
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 18));
        logoutPanel.setBackground(sidebarBg);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutButton.setForeground(new Color(248, 113, 113)); // Coral red #F87171 matching screenshot
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

        // 3. Central Dynamic Views Container
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(contentBg);

        // A. Dashboard Overview (Matching the user screenshot)
        JPanel dashboardOverview = createDashboardOverview();
        contentContainer.add(dashboardOverview, "Dashboard");

        // B. Attendance Management Page
        attendancePage = new AttendanceManagementPage();
        contentContainer.add(attendancePage, "Attendance");

        // C. Medical Management Page
        medicalPage = new MedicalManagementPage();
        contentContainer.add(medicalPage, "Medical Certificates");

        // D. Lab Maintenance Page
        labMaintenancePage = new LabMaintenancePage();
        contentContainer.add(labMaintenancePage, "Lab Maintenance");

        // E. Profile Page
        profilePage = new ProfilePage();
        contentContainer.add(profilePage, "Profile");

        // Assemble Layout
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentContainer, BorderLayout.CENTER);

        // Highlight initial menu
        highlightActiveMenu("Dashboard");

        add(mainPanel);
        setVisible(true);
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

        JLabel subtitle = new JLabel("Manage your courses, students, attendance and academic activities.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        welcomePanel.add(welcome);
        welcomePanel.add(Box.createVerticalStrut(4));
        welcomePanel.add(subtitle);

        contentPanel.add(welcomePanel, BorderLayout.NORTH);

        // Center Content: Cards Row + Lower Activity Card
        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setBackground(contentBg);

        // 4 KPI Stat Cards in a row
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setBackground(contentBg);

        int pendingCount = medicalService.getPendingMedicalCount();
        String pendingStr = pendingCount > 0 ? String.valueOf(pendingCount) : "8";

        cardsPanel.add(createStatCard("ASSIGNED LABS", "4", "Assigned courses", textDark, darkNavy));
        cardsPanel.add(createStatCard("STUDENTS", "86", "Across your courses", new Color(13, 148, 136), new Color(16, 185, 129))); // Teal 86 with green dot
        cardsPanel.add(createStatCard("TODAY'S SESSIONS", "12", "Created this semester", textDark, new Color(37, 99, 235)));
        cardsPanel.add(createStatCard("PENDING MEDICALS", pendingStr, "Need your attention", new Color(124, 58, 237), new Color(139, 92, 246))); // Purple with purple dot

        // Technical Officer Activities Card (Identical to screenshot)
        JPanel activityPanel = new JPanel(new BorderLayout(0, 16));
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(20, 22, 22, 22)
                )
        );

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
        JPanel activityList = new JPanel();
        activityList.setBackground(Color.WHITE);
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));

        // 4 exact styled rows matching screenshot:
        // 1. Cyan bar | Attendance updated | ATTENDANCE badge
        activityList.add(new ActivityRow(
                new Color(6, 182, 212), // Cyan
                "Attendance updated for ICT2132",
                "ATTENDANCE",
                new Color(204, 251, 241), // Light cyan bg
                new Color(15, 118, 110)   // Dark teal fg
        ));
        activityList.add(Box.createVerticalStrut(14));

        // 2. Navy bar | New lab session / assignment created | SESSION badge
        activityList.add(new ActivityRow(
                new Color(27, 66, 104), // Dark Navy
                "New assignment created",
                "ASSIGNMENT",
                new Color(219, 234, 254), // Soft blue bg
                new Color(30, 64, 175)    // Blue fg
        ));
        activityList.add(Box.createVerticalStrut(14));

        // 3. Purple bar | Marks / Medicals submitted | MARKS badge
        activityList.add(new ActivityRow(
                new Color(147, 51, 234), // Purple
                "Marks submitted for Programming",
                "MARKS",
                new Color(243, 232, 255), // Light purple bg
                new Color(126, 34, 206)   // Purple fg
        ));
        activityList.add(Box.createVerticalStrut(14));

        // 4. Amber bar | Course materials / lab status updated | MATERIALS badge
        activityList.add(new ActivityRow(
                new Color(217, 119, 6), // Amber
                "Course materials updated",
                "MATERIALS",
                new Color(254, 243, 199), // Light amber bg
                new Color(180, 83, 9)     // Amber fg
        ));

        activityPanel.add(activityHeader, BorderLayout.NORTH);
        activityPanel.add(activityList, BorderLayout.CENTER);

        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(activityPanel, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        return contentPanel;
    }

    /**
     * Creates a KPI Stat Card styled exactly to match the reference UI.
     */
    private JPanel createStatCard(String title, String value, String subtitle, Color valueColor, Color dotColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)
                )
        );

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

        // Interactivity
        if (title.contains("SESSIONS") || title.contains("ATTENDANCE")) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navigateTo("Attendance");
                }
            });
        } else if (title.contains("MEDICAL")) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navigateTo("Medical Certificates");
                }
            });
        } else if (title.contains("LABS") || title.contains("EQUIPMENT") || title.contains("MAINTENANCE")) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navigateTo("Lab Maintenance");
                }
            });
        }

        return card;
    }

    private void addMenuButton(JPanel panel, String text) {
        SidebarMenuButton button = new SidebarMenuButton(text);
        button.addActionListener(e -> navigateTo(text));

        menuButtons.put(text, button);
        panel.add(button);
    }

    private void navigateTo(String viewName) {
        if ("Lab Maintenance".equalsIgnoreCase(viewName) && labMaintenancePage != null) {
            labMaintenancePage.refreshAllData();
        } else if ("Profile".equalsIgnoreCase(viewName) && profilePage != null) {
            profilePage.refreshProfile();
        }
        cardLayout.show(contentContainer, viewName);
        highlightActiveMenu(viewName);
    }

    private void highlightActiveMenu(String activeText) {
        currentView = activeText;
        for (Map.Entry<String, SidebarMenuButton> entry : menuButtons.entrySet()) {
            boolean isActive = entry.getKey().equalsIgnoreCase(activeText);
            entry.getValue().setActive(isActive);
        }
    }

    /**
     * Creates the clickable profile section in the top header bar:
     * [ Circle Avatar with initials ] [ Officer Name (ID) ] [ TECHNICAL OFFICER ]
     * Clicking anywhere on this section directly accesses and opens the Profile page.
     */
    private JPanel createProfileHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14)) {
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
                    g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 12, 12, 12);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 24));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setToolTipText("Click to view & manage your Technical Officer Profile");

        // 1. Circular Avatar with initials
        String initials = extractInitials(officerName);
        CircleAvatar avatar = new CircleAvatar(initials);

        // 2. Officer Name & ID label
        JLabel nameLabel = new JLabel(officerName + " (" + officerId + ")");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // 3. Role Label in cyan accent matching student dashboard reference
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
     * Circular avatar icon displaying user initials with an elegant round border.
     */
    private static class CircleAvatar extends JComponent {
        private final String initials;

        public CircleAvatar(String initials) {
            this.initials = (initials != null) ? initials : "TO";
            Dimension dim = new Dimension(32, 32);
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

            int size = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Semi-translucent dark circular fill
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval(x, y, size, size);

            // Circular ring outline in cyan/soft-blue
            g2.setColor(new Color(180, 230, 250, 220));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(x, y, size, size);

            // Centered Initials Text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(initials)) / 2;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 1;
            g2.drawString(initials, tx, ty);

            g2.dispose();
        }
    }


    /**
     * Circular colored indicator dot for the top right of each stat card.
     */
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
    /**
     * Sidebar Menu Button with dark navy theme matching the reference design.
     */
    private static class SidebarMenuButton extends JButton {
        private boolean active = false;
        private boolean hovered = false;

        private final Color defaultBg = new Color(27, 66, 104);      // #1B4268
        private final Color activeBg = new Color(16, 40, 74);        // #10284A (solid dark active background)
        private final Color hoverBg = new Color(20, 52, 85);         // Subtle hover highlight
        private final Color activeFg = Color.WHITE;
        private final Color inactiveFg = new Color(210, 225, 240);   // Soft light blue text #D2E1F0

        public SidebarMenuButton(String text) {
            super(text);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(inactiveFg);
            setBackground(defaultBg);
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
                // Dark navy active block spanning full width matching reference image
                g2.setColor(activeBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else if (hovered) {
                g2.setColor(hoverBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Activity Row Component featuring a colored vertical bar on left, text in center,
     * and a rounded pill badge on right matching the screenshot.
     */
    private static class ActivityRow extends JPanel {
        public ActivityRow(Color barColor, String text, String badgeText, Color badgeBg, Color badgeFg) {
            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setPreferredSize(new Dimension(800, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            // Left vertical accent bar
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

            // Center text
            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(new Color(51, 65, 85)); // #334155

            // Right Pill Badge
            JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 1));
            badgeWrapper.setOpaque(false);
            PillBadge badge = new PillBadge(badgeText, badgeBg, badgeFg);
            badgeWrapper.add(badge);

            add(bar, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
            add(badgeWrapper, BorderLayout.EAST);
        }
    }

    /**
     * Rounded Pill Badge Component with colored background and bold uppercase text.
     */
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
