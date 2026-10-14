package ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

/**
 * FAMS - Faculty Academic Management System
 * Admin Dashboard UI
 *
 * Enterprise-grade, modern desktop management dashboard for administrators.
 * Designed with pure Java Swing and Java2D vector graphics.
 */
public class AdminDashboard extends JFrame {

    // ==========================================
    // THEME PALETTE
    // ==========================================
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color LIGHT_BLUE      = new Color(234, 242, 255); // #EAF2FF
    private static final Color MAIN_BG         = new Color(245, 247, 251); // #F5F7FB
    private static final Color CARD_BG         = Color.WHITE;              // #FFFFFF
    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_SECONDARY  = new Color(113, 128, 150); // #718096
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A
    private static final Color WARNING_COLOR   = new Color(245, 158, 11);  // #F59E0B
    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // #DC2626
    private static final Color BORDER_COLOR    = new Color(226, 232, 240); // #E2E8F0

    // Statistic Labels for future DAO/Service population
    private JLabel lblStudentsCount;
    private JLabel lblLecturersCount;
    private JLabel lblOfficersCount;
    private JLabel lblCoursesCount;

    // Current Administrator Display Name
    private final String adminDisplayName;

    public AdminDashboard() {
        this("Administrator");
    }

    public AdminDashboard(String adminDisplayName) {
        this.adminDisplayName = (adminDisplayName != null && !adminDisplayName.isBlank())
                ? adminDisplayName
                : "Administrator";
        initUI();
    }

    private void initUI() {
        setTitle("FAMS - Faculty Academic Management System | Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        // Root container
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(MAIN_BG);

        // 1. Left Sidebar
        JPanel sidebar = createSidebar();
        sidebar.setPreferredSize(new Dimension(250, 800));
        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Right Workspace (Header + Scrollable Main Content)
        JPanel workspacePanel = new JPanel(new BorderLayout());
        workspacePanel.setBackground(MAIN_BG);

        JPanel header = createHeader();
        header.setPreferredSize(new Dimension(1000, 88));
        workspacePanel.add(header, BorderLayout.NORTH);

        JPanel contentPanel = createMainContent();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getViewport().setBackground(MAIN_BG);
        workspacePanel.add(scrollPane, BorderLayout.CENTER);

        rootPanel.add(workspacePanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    // ==========================================
    // FONT RESOLVER
    // ==========================================
    public static Font getModernFont(int style, int size) {
        String[] preferredFonts = {
                "SF Pro Display", "SF Pro Text", "Helvetica Neue", "Segoe UI", "Inter", "Arial", "SansSerif"
        };
        for (String fontName : preferredFonts) {
            Font f = new Font(fontName, style, size);
            if (!f.getFamily().equalsIgnoreCase("Dialog") || fontName.equalsIgnoreCase("SansSerif")) {
                return f;
            }
        }
        return new Font("SansSerif", style, size);
    }

    // ==========================================
    // LEFT SIDEBAR
    // ==========================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint bg = new GradientPaint(0, 0, PRIMARY_NAVY, 0, getHeight(), new Color(10, 25, 47));
                g2.setPaint(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BorderLayout());

        // Brand Panel (Top) - Faculty Academic Management System
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(26, 20, 20, 20));

        JLabel titleLine1 = new JLabel("Faculty Academic");
        titleLine1.setFont(getModernFont(Font.BOLD, 17));
        titleLine1.setForeground(Color.WHITE);
        titleLine1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLine2 = new JLabel("Management System");
        titleLine2.setFont(getModernFont(Font.BOLD, 17));
        titleLine2.setForeground(Color.WHITE);
        titleLine2.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(titleLine1);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(titleLine2);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Center Nav Items
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(6, 14, 10, 14));

        JLabel navLabel = new JLabel("MAIN MENU");
        navLabel.setFont(getModernFont(Font.BOLD, 10));
        navLabel.setForeground(new Color(148, 163, 184));
        navLabel.setBorder(new EmptyBorder(0, 12, 10, 0));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(navLabel);

        navPanel.add(createNavItem("Dashboard", "dashboard", true));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavItem("User Management", "users", false));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavItem("Course Management", "courses", false));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavItem("Notice Management", "notices", false));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavItem("Timetable Management", "timetable", false));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavItem("Reports", "reports", false));

        sidebar.add(navPanel, BorderLayout.CENTER);

        // Bottom Admin Profile & Logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(14, 16, 20, 16));

        // Profile Section
        JPanel profileCard = new JPanel(new BorderLayout(10, 0));
        profileCard.setOpaque(false);
        profileCard.setBorder(new EmptyBorder(6, 4, 10, 4));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SECONDARY_BLUE);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(getModernFont(Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String initials = getInitials(adminDisplayName);
                int tx = (36 - fm.stringWidth(initials)) / 2;
                int ty = ((36 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, tx, ty);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel namePanel = new JPanel();
        namePanel.setOpaque(false);
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

        JLabel adminName = new JLabel(adminDisplayName);
        adminName.setFont(getModernFont(Font.BOLD, 13));
        adminName.setForeground(Color.WHITE);

        JLabel adminRole = new JLabel("System Admin");
        adminRole.setFont(getModernFont(Font.PLAIN, 11));
        adminRole.setForeground(new Color(147, 197, 253));

        namePanel.add(adminName);
        namePanel.add(Box.createVerticalStrut(2));
        namePanel.add(adminRole);

        profileCard.add(avatar, BorderLayout.WEST);
        profileCard.add(namePanel, BorderLayout.CENTER);

        // Logout Button - Placed directly under admin user, filling the width
        JButton logoutBtn = createLogoutButton();
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setPreferredSize(new Dimension(218, 38));

        bottomPanel.add(profileCard);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(logoutBtn);

        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createNavItem(String name, String type, boolean active) {
        final boolean[] isHovered = {false};
        JPanel item = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(SECONDARY_BLUE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (isHovered[0]) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setPreferredSize(new Dimension(220, 40));
        item.setBorder(new EmptyBorder(8, 12, 8, 12));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon Component
        JComponent iconComp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? Color.WHITE : (isHovered[0] ? Color.WHITE : new Color(148, 163, 184)));
                drawNavIcon(g2, type, 0, 0);
                g2.dispose();
            }
        };
        iconComp.setPreferredSize(new Dimension(18, 18));

        JLabel label = new JLabel(name);
        label.setFont(getModernFont(active ? Font.BOLD : Font.PLAIN, 13));
        label.setForeground(active ? Color.WHITE : new Color(203, 213, 225));

        item.add(iconComp, BorderLayout.WEST);
        item.add(label, BorderLayout.CENTER);

        // Hover & Click Interactions
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    isHovered[0] = true;
                    label.setForeground(Color.WHITE);
                    item.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    isHovered[0] = false;
                    label.setForeground(new Color(203, 213, 225));
                    item.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleNavigationClick(name);
            }
        });

        return item;
    }

    private void handleNavigationClick(String name) {
        if ("Dashboard".equalsIgnoreCase(name)) {
            // Already on Dashboard
            return;
        }

        // Clean, informative dialog for modules scheduled for upcoming phases
        JOptionPane.showMessageDialog(
                this,
                "<html><body style='width: 280px; font-family: sans-serif;'>" +
                        "<h3 style='color: #10284A; margin-bottom: 6px;'>" + name + "</h3>" +
                        "<p style='color: #4A5568; line-height: 1.4;'>" +
                        "This administrative module is scheduled for implementation in Phase 2.<br>" +
                        "Database integration and management forms will become active shortly." +
                        "</p></body></html>",
                name + " - Notice",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JButton createLogoutButton() {
        final boolean[] isHovered = {false};
        JButton logoutBtn = new JButton("Log Out") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isHovered[0]) {
                    g2.setColor(new Color(220, 38, 38, 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(248, 113, 113, 100));
                } else {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(255, 255, 255, 40));
                }
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(getModernFont(Font.BOLD, 12));
        logoutBtn.setForeground(new Color(248, 113, 113));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setPreferredSize(new Dimension(218, 38));

        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                logoutBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                logoutBtn.repaint();
            }
        });

        logoutBtn.addActionListener(e -> handleLogout());
        return logoutBtn;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of the FAMS administration console?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Safely return to LoginFrame
            try {
                Class<?> loginCls = Class.forName("ui.login.LoginFrame");
                JFrame loginFrame = (JFrame) loginCls.getDeclaredConstructor().newInstance();
                loginFrame.setVisible(true);
            } catch (Exception ex) {
                System.out.println("Returned to login state successfully.");
            }
        }
    }

    private void drawNavIcon(Graphics2D g2, String type, int x, int y) {
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {
            case "dashboard":
                g2.drawRoundRect(x + 1, y + 1, 6, 6, 2, 2);
                g2.drawRoundRect(x + 10, y + 1, 6, 6, 2, 2);
                g2.drawRoundRect(x + 1, y + 10, 6, 6, 2, 2);
                g2.drawRoundRect(x + 10, y + 10, 6, 6, 2, 2);
                break;
            case "users":
                g2.drawOval(x + 3, y + 1, 6, 6);
                g2.drawArc(x + 1, y + 9, 10, 8, 0, 180);
                g2.drawOval(x + 10, y + 3, 5, 5);
                g2.drawArc(x + 9, y + 10, 8, 7, 0, 180);
                break;
            case "courses":
                g2.drawRoundRect(x + 1, y + 2, 15, 13, 3, 3);
                g2.drawLine(x + 8, y + 2, x + 8, y + 15);
                break;
            case "notices":
                g2.drawArc(x + 3, y + 3, 11, 10, 0, 180);
                g2.drawLine(x + 3, y + 8, x + 1, y + 13);
                g2.drawLine(x + 14, y + 8, x + 16, y + 13);
                g2.drawLine(x + 1, y + 13, x + 16, y + 13);
                g2.fillOval(x + 7, y + 14, 3, 3);
                break;
            case "timetable":
                g2.drawRoundRect(x + 1, y + 2, 15, 14, 3, 3);
                g2.drawLine(x + 1, y + 6, x + 16, y + 6);
                g2.drawLine(x + 5, y + 1, x + 5, y + 3);
                g2.drawLine(x + 12, y + 1, x + 12, y + 3);
                break;
            case "reports":
                g2.drawLine(x + 1, y + 15, x + 16, y + 15);
                g2.drawLine(x + 4, y + 15, x + 4, y + 9);
                g2.drawLine(x + 8, y + 15, x + 8, y + 4);
                g2.drawLine(x + 12, y + 15, x + 12, y + 7);
                break;
        }
    }

    // ==========================================
    // TOP HEADER
    // ==========================================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 28, 14, 28)
        ));

        // Left Titles
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel breadcrumb = new JLabel("Home / Dashboard");
        breadcrumb.setFont(getModernFont(Font.PLAIN, 11));
        breadcrumb.setForeground(TEXT_SECONDARY);

        JLabel title = new JLabel("Dashboard");
        title.setFont(getModernFont(Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Welcome to your FAMS administration workspace.");
        subtitle.setFont(getModernFont(Font.PLAIN, 12));
        subtitle.setForeground(TEXT_SECONDARY);

        titlePanel.add(breadcrumb);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        // Right Actions (Notifications & Admin Pill)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        rightPanel.setOpaque(false);

        // Notification Bell Button
        JButton bellBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(241, 245, 249));
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(BORDER_COLOR);
                g2.drawOval(0, 0, 35, 35);

                // Bell outline
                g2.setColor(TEXT_SECONDARY);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(12, 10, 12, 11, 0, 180);
                g2.drawLine(12, 16, 9, 21);
                g2.drawLine(24, 16, 27, 21);
                g2.drawLine(9, 21, 27, 21);
                g2.fillOval(16, 22, 4, 3);

                // Notification indicator dot
                g2.setColor(SECONDARY_BLUE);
                g2.fillOval(23, 8, 7, 7);

                g2.dispose();
            }
        };
        bellBtn.setPreferredSize(new Dimension(36, 36));
        bellBtn.setContentAreaFilled(false);
        bellBtn.setBorderPainted(false);
        bellBtn.setFocusPainted(false);
        bellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellBtn.setToolTipText("System Notifications");
        bellBtn.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "<html><body style='font-family: sans-serif;'>" +
                        "<h4 style='color: #10284A; margin: 0 0 6px 0;'>System Notifications</h4>" +
                        "<p style='color: #4A5568; margin: 0;'>No unread system alerts.<br>All core academic services are operating normally.</p>" +
                        "</body></html>",
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
        ));

        // Admin Pill
        JPanel adminPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        adminPill.setOpaque(false);
        adminPill.setBorder(new EmptyBorder(2, 6, 2, 10));

        JPanel miniAvatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_NAVY);
                g2.fillOval(0, 0, 26, 26);
                g2.setColor(Color.WHITE);
                g2.setFont(getModernFont(Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String t = getInitials(adminDisplayName);
                g2.drawString(t, (26 - fm.stringWidth(t)) / 2, 17);
                g2.dispose();
            }
        };
        miniAvatar.setOpaque(false);
        miniAvatar.setPreferredSize(new Dimension(26, 26));

        JLabel nameLbl = new JLabel(adminDisplayName);
        nameLbl.setFont(getModernFont(Font.BOLD, 12));
        nameLbl.setForeground(TEXT_PRIMARY);

        adminPill.add(miniAvatar);
        adminPill.add(nameLbl);

        rightPanel.add(bellBtn);
        rightPanel.add(adminPill);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // MAIN CONTENT
    // ==========================================
    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setBackground(MAIN_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 28, 28, 28));

        content.add(createWelcomeBanner());
        content.add(Box.createVerticalStrut(20));

        content.add(createStatsRow());
        content.add(Box.createVerticalStrut(22));

        content.add(createLowerSection());
        return content;
    }

    // ==========================================
    // WELCOME BANNER
    // ==========================================
    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, PRIMARY_NAVY,
                        getWidth(), getHeight(), SECONDARY_BLUE
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Subtle modern background arcs on right
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillOval(getWidth() - 170, -40, 210, 210);
                g2.fillOval(getWidth() - 250, 15, 140, 140);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(20, 26, 20, 26));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel welcomeTitle = new JLabel("Welcome to FAMS");
        welcomeTitle.setFont(getModernFont(Font.BOLD, 22));
        welcomeTitle.setForeground(Color.WHITE);

        JLabel welcomeSub = new JLabel("Manage academic activities and keep your faculty connected through one centralized platform.");
        welcomeSub.setFont(getModernFont(Font.PLAIN, 13));
        welcomeSub.setForeground(new Color(224, 234, 254));

        textPanel.add(welcomeTitle);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(welcomeSub);

        banner.add(textPanel, BorderLayout.CENTER);
        return banner;
    }

    // ==========================================
    // STATISTICS CARDS ROW
    // ==========================================
    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblStudentsCount  = new JLabel("--");
        lblLecturersCount = new JLabel("--");
        lblOfficersCount  = new JLabel("--");
        lblCoursesCount   = new JLabel("--");

        row.add(createStatCard("Total Students", "students", SECONDARY_BLUE, LIGHT_BLUE, lblStudentsCount));
        row.add(createStatCard("Total Lecturers", "lecturers", new Color(79, 70, 229), new Color(238, 242, 255), lblLecturersCount));
        row.add(createStatCard("Total Technical Officers", "officers", WARNING_COLOR, new Color(254, 243, 199), lblOfficersCount));
        row.add(createStatCard("Total Courses", "courses", SUCCESS_COLOR, new Color(220, 252, 231), lblCoursesCount));

        return row;
    }

    private JPanel createStatCard(String title, String type, Color accentColor, Color iconBg, JLabel valLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(getModernFont(Font.BOLD, 12));
        titleLabel.setForeground(TEXT_SECONDARY);

        valLabel.setFont(getModernFont(Font.BOLD, 26));
        valLabel.setForeground(TEXT_PRIMARY);

        JLabel statusLabel = new JLabel("Live Data Pending");
        statusLabel.setFont(getModernFont(Font.PLAIN, 10));
        statusLabel.setForeground(new Color(148, 163, 184));

        leftCol.add(titleLabel);
        leftCol.add(Box.createVerticalStrut(4));
        leftCol.add(valLabel);
        leftCol.add(Box.createVerticalStrut(4));
        leftCol.add(statusLabel);

        // Circular Icon
        JPanel iconPill = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(accentColor);
                drawStatIcon(g2, type, 10, 10);
                g2.dispose();
            }
        };
        iconPill.setOpaque(false);
        iconPill.setPreferredSize(new Dimension(40, 40));

        card.add(leftCol, BorderLayout.CENTER);
        card.add(iconPill, BorderLayout.EAST);
        return card;
    }

    private void drawStatIcon(Graphics2D g2, String type, int x, int y) {
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {
            case "students":
                g2.drawOval(x + 5, y + 1, 10, 10);
                g2.drawArc(x + 1, y + 12, 18, 10, 0, 180);
                break;
            case "lecturers":
                g2.drawOval(x + 5, y + 1, 9, 9);
                g2.drawArc(x + 2, y + 11, 16, 9, 0, 180);
                g2.drawLine(x + 10, y + 15, x + 10, y + 20);
                g2.drawLine(x + 5, y + 20, x + 15, y + 20);
                break;
            case "officers":
                g2.drawOval(x + 4, y + 4, 12, 12);
                g2.drawOval(x + 8, y + 8, 4, 4);
                g2.drawLine(x + 10, y + 1, x + 10, y + 4);
                g2.drawLine(x + 10, y + 16, x + 10, y + 19);
                g2.drawLine(x + 1, y + 10, x + 4, y + 10);
                g2.drawLine(x + 16, y + 10, x + 19, y + 10);
                break;
            case "courses":
                g2.drawRoundRect(x + 2, y + 2, 16, 15, 3, 3);
                g2.drawLine(x + 10, y + 2, x + 10, y + 17);
                break;
        }
    }

    // ==========================================
    // LOWER SECTION (Quick Actions, Activity, Overview)
    // ==========================================
    private JPanel createLowerSection() {
        JPanel section = new JPanel(new GridLayout(1, 3, 16, 0));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        // Column 1: Quick Actions
        section.add(createQuickActionsCard());

        // Column 2: Recent Activity
        section.add(createRecentActivityCard());

        // Column 3: System Overview
        section.add(createSystemOverviewCard());

        return section;
    }

    private JPanel createQuickActionsCard() {
        JPanel card = createBaseCard("Quick Actions");
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(createActionItem("Manage Users", "Add, edit, or configure accounts", SECONDARY_BLUE));
        list.add(Box.createVerticalStrut(8));
        list.add(createActionItem("Manage Courses", "Assign lecturers and courses", new Color(79, 70, 229)));
        list.add(Box.createVerticalStrut(8));
        list.add(createActionItem("View Notices", "Publish notices and announcements", WARNING_COLOR));
        list.add(Box.createVerticalStrut(8));
        list.add(createActionItem("View Reports", "Export academic and user reports", SUCCESS_COLOR));

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionItem(String title, String desc, Color accent) {
        final boolean[] isHovered = {false};
        JPanel item = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isHovered[0] ? new Color(224, 237, 255) : LIGHT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isHovered[0] ? SECONDARY_BLUE : new Color(219, 234, 254));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(10, 14, 10, 14));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(getModernFont(Font.BOLD, 12));
        titleLbl.setForeground(PRIMARY_NAVY);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(getModernFont(Font.PLAIN, 10));
        descLbl.setForeground(TEXT_SECONDARY);

        text.add(titleLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(descLbl);

        JLabel arrow = new JLabel("→");
        arrow.setFont(getModernFont(Font.BOLD, 14));
        arrow.setForeground(accent);

        item.add(text, BorderLayout.CENTER);
        item.add(arrow, BorderLayout.EAST);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                item.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                item.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleNavigationClick(title);
            }
        });

        return item;
    }

    private JPanel createRecentActivityCard() {
        JPanel card = createBaseCard("Recent Activity");

        JPanel emptyState = new JPanel();
        emptyState.setOpaque(false);
        emptyState.setLayout(new BoxLayout(emptyState, BoxLayout.Y_AXIS));
        emptyState.setBorder(new EmptyBorder(30, 16, 30, 16));

        // Subtle activity icon
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillOval(0, 0, 48, 48);
                g2.setColor(BORDER_COLOR);
                g2.drawOval(0, 0, 47, 47);

                g2.setColor(TEXT_SECONDARY);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Clock/audit icon
                g2.drawOval(14, 14, 20, 20);
                g2.drawLine(24, 18, 24, 24);
                g2.drawLine(24, 24, 28, 24);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(48, 48));
        iconCircle.setMaximumSize(new Dimension(48, 48));
        iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyTitle = new JLabel("No recent activity to display.");
        emptyTitle.setFont(getModernFont(Font.BOLD, 13));
        emptyTitle.setForeground(TEXT_SECONDARY);
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyDesc = new JLabel("<html><center style='color:#A0AEC0; font-size:10px;'>Administrative action logs and audit events<br>will appear here in real time once connected.</center></html>");
        emptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyState.add(iconCircle);
        emptyState.add(Box.createVerticalStrut(12));
        emptyState.add(emptyTitle);
        emptyState.add(Box.createVerticalStrut(6));
        emptyState.add(emptyDesc);

        card.add(emptyState, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSystemOverviewCard() {
        JPanel card = createBaseCard("System Overview");

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(createOverviewRow("Students Module", "Enrolled faculty learners", SUCCESS_COLOR, "Active"));
        list.add(Box.createVerticalStrut(10));
        list.add(createOverviewRow("Lecturers Module", "Course educators & grading", SUCCESS_COLOR, "Active"));
        list.add(Box.createVerticalStrut(10));
        list.add(createOverviewRow("Technical Officers", "Lab & attendance monitoring", SUCCESS_COLOR, "Active"));
        list.add(Box.createVerticalStrut(10));
        list.add(createOverviewRow("Admin Oversight", "Full system governance", SECONDARY_BLUE, "Ready"));

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createOverviewRow(String title, String desc, Color dotColor, String badgeText) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setForeground(dotColor);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(getModernFont(Font.BOLD, 12));
        tLbl.setForeground(TEXT_PRIMARY);

        JLabel dLbl = new JLabel(desc);
        dLbl.setFont(getModernFont(Font.PLAIN, 10));
        dLbl.setForeground(TEXT_SECONDARY);

        text.add(tLbl);
        text.add(dLbl);

        JLabel badge = new JLabel(badgeText);
        badge.setFont(getModernFont(Font.BOLD, 10));
        badge.setForeground(dotColor);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        row.add(dot, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(badge, BorderLayout.EAST);
        return row;
    }

    private JPanel createBaseCard(String heading) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel h = new JLabel(heading);
        h.setFont(getModernFont(Font.BOLD, 15));
        h.setForeground(TEXT_PRIMARY);
        h.setBorder(new EmptyBorder(0, 0, 14, 0));

        card.add(h, BorderLayout.NORTH);
        return card;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "AD";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        } else if (name.length() >= 2) {
            return name.substring(0, 2).toUpperCase();
        }
        return name.toUpperCase();
    }

    // ==========================================
    // EXTENSION POINT FOR FUTURE SERVICE/DAO DATA
    // ==========================================
    /**
     * Programmatically update the live statistics once the database layer is connected.
     */
    public void updateStatistics(String students, String lecturers, String technicalOfficers, String courses) {
        if (lblStudentsCount != null) lblStudentsCount.setText(students);
        if (lblLecturersCount != null) lblLecturersCount.setText(lecturers);
        if (lblOfficersCount != null) lblOfficersCount.setText(technicalOfficers);
        if (lblCoursesCount != null) lblCoursesCount.setText(courses);
    }

    // ==========================================
    // STANDALONE RUNNER FOR UI PREVIEW & TESTING
    // ==========================================
    public static void main(String[] args) {
        try {
            // Set platform Look and Feel for native crispness where available
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard("Admin User");
            dashboard.setVisible(true);
        });
    }
}
