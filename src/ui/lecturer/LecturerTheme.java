package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FAMS - Faculty Academic Management System
 * Shared visual theme constants and components for the Lecturer module,
 * exactly aligned with the System Administrator Dashboard theme.
 */
public final class LecturerTheme {

    // Theme Palette matching Admin Dashboard
    public static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A - dark navy
    public static final Color NAVY_DARK       = new Color(10, 25, 47);    // #0A192F - deep navy gradient end
    public static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8 - royal blue
    public static final Color LIGHT_BLUE      = new Color(234, 242, 255); // #EAF2FF - subtle blue highlight
    public static final Color MAIN_BG         = new Color(245, 247, 251); // #F5F7FB - workspace light-grey
    public static final Color CARD_BG         = Color.WHITE;              // #FFFFFF - cards & panels
    public static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D - dark navy headings
    public static final Color TEXT_SECONDARY  = new Color(113, 128, 150); // #718096 - muted secondary text
    public static final Color BORDER_COLOR    = new Color(226, 232, 240); // #E2E8F0 - subtle borders
    public static final Color NAV_TEXT_MUTED  = new Color(148, 163, 184); // #94A3B8 - section header
    public static final Color NAV_ITEM_TEXT   = new Color(203, 213, 225); // #CBD5E1 - inactive nav text
    public static final Color LOGOUT_RED      = new Color(248, 113, 113); // #F87171 - logout button
    public static final Color SUCCESS_GREEN   = new Color(22, 163, 74);   // #16A34A
    public static final Color WARNING_AMBER   = new Color(245, 158, 11);  // #F59E0B
    public static final Color DANGER_RED      = new Color(220, 38, 38);   // #DC2626

    public static final Font FONT_TITLE   = new Font("Arial", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Arial", Font.BOLD, 22);
    public static final Font FONT_BOLD_13 = new Font("Arial", Font.BOLD, 13);
    public static final Font FONT_BOLD_12 = new Font("Arial", Font.BOLD, 12);
    public static final Font FONT_PLAIN_13 = new Font("Arial", Font.PLAIN, 13);
    public static final Font FONT_PLAIN_12 = new Font("Arial", Font.PLAIN, 12);

    // In-memory cache for lecturer display names to avoid redundant DB queries
    private static final Map<String, String> LECTURER_NAME_CACHE = new ConcurrentHashMap<>();

    private LecturerTheme() {}

    /**
     * Programmatically update or preload the lecturer display name in cache.
     */
    public static void setLecturerDisplayName(String lecturerId, String name) {
        if (lecturerId != null && name != null && !name.trim().isEmpty()) {
            LECTURER_NAME_CACHE.put(lecturerId.trim(), name.trim());
        }
    }

    /**
     * Clear cached name for a given lecturer or all lecturers.
     */
    public static void clearLecturerNameCache(String lecturerId) {
        if (lecturerId != null) {
            LECTURER_NAME_CACHE.remove(lecturerId.trim());
        } else {
            LECTURER_NAME_CACHE.clear();
        }
    }

    /**
     * Resolves the lecturer display name from cache or MySQL database.
     */
    public static String getLecturerDisplayName(String lecturerId) {
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            String active = LecturerDashboard.getActiveLecturerId();
            if (active != null && !active.trim().isEmpty()) {
                lecturerId = active.trim();
            } else {
                return "Lecturer";
            }
        }

        lecturerId = lecturerId.trim();
        if (LECTURER_NAME_CACHE.containsKey(lecturerId)) {
            return LECTURER_NAME_CACHE.get(lecturerId);
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.first_name, u.last_name FROM users u WHERE u.user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String first = rs.getString("first_name");
                        String last = rs.getString("last_name");
                        String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                        if (!fullName.isEmpty()) {
                            LECTURER_NAME_CACHE.put(lecturerId, fullName);
                            return fullName;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return "Lecturer";
    }

    /**
     * Computes avatar initials from a given name (e.g., "Kasun Perera" -> "KP").
     */
    public static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "L";
        String clean = name.trim();
        String[] parts = clean.split("\\s+");
        if (parts.length >= 2) {
            int firstIdx = 0;
            if ((parts[0].equalsIgnoreCase("Dr.") || parts[0].equalsIgnoreCase("Dr") ||
                 parts[0].equalsIgnoreCase("Prof.") || parts[0].equalsIgnoreCase("Prof") ||
                 parts[0].equalsIgnoreCase("Mr.") || parts[0].equalsIgnoreCase("Mr") ||
                 parts[0].equalsIgnoreCase("Mrs.") || parts[0].equalsIgnoreCase("Mrs") ||
                 parts[0].equalsIgnoreCase("Ms.") || parts[0].equalsIgnoreCase("Ms")) && parts.length >= 3) {
                firstIdx = 1;
            }
            char c1 = parts[firstIdx].charAt(0);
            char c2 = parts[firstIdx + 1].charAt(0);
            return ("" + c1 + c2).toUpperCase();
        } else if (clean.length() >= 2) {
            return clean.substring(0, 2).toUpperCase();
        }
        return clean.toUpperCase();
    }

    /**
     * Default overload creating the modern white top header with active session lecturer.
     */
    public static JPanel createHeader() {
        return createHeader(LecturerDashboard.getActiveLecturerId(), null);
    }

    /**
     * Creates the modern white top header matching the System Administrator Dashboard screenshot:
     * - White header background with subtle bottom border (#E2E8F0)
     * - Left: Bold system title ("FACULTY ACADEMIC MANAGEMENT SYSTEM")
     * - Right: Circular bell notification button with royal blue indicator dot
     * - Right: Rounded light profile pill button with navy avatar, initials, actual lecturer name & "Lecturer" role
     * - Preserves lecturer session identity and opens existing LecturerProfile on click
     */
    public static JPanel createHeader(String lecturerId, JFrame currentFrame) {
        if (lecturerId != null && !lecturerId.trim().isEmpty()) {
            LecturerDashboard.setActiveLecturerId(lecturerId.trim());
        }

        final String activeId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : LecturerDashboard.getActiveLecturerId();

        final String displayName = getLecturerDisplayName(activeId);
        final String initials = getInitials(displayName);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(0, 26, 0, 26)
        ));

        // Left System Title
        JLabel title = new JLabel("FACULTY ACADEMIC MANAGEMENT SYSTEM");
        title.setForeground(PRIMARY_NAVY);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);

        // Right Actions Panel (Notification Bell + Lecturer Profile Button)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 17));
        rightPanel.setOpaque(false);

        // 1. Notification Bell Button
        boolean[] bellHovered = new boolean[]{false};
        JButton bellBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (bellHovered[0]) {
                    g2.setColor(new Color(233, 240, 250));
                } else {
                    g2.setColor(new Color(241, 245, 249));
                }
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

                // Royal blue notification indicator dot (matching Administrator Dashboard)
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
        bellBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bellHovered[0] = true;
                bellBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bellHovered[0] = false;
                bellBtn.repaint();
            }
        });
        bellBtn.addActionListener(e -> JOptionPane.showMessageDialog(
                currentFrame,
                "<html><body style='font-family: Arial, sans-serif; padding: 4px;'>" +
                        "<h4 style='color: #10284A; margin: 0 0 6px 0;'>System Notifications</h4>" +
                        "<p style='color: #4A5568; margin: 0; font-size: 11px;'>No unread academic notifications.<br>All lecturer services and course activities are up to date.</p>" +
                        "</body></html>",
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
        ));

        // 2. Lecturer Profile Pill Button
        boolean[] profileHovered = new boolean[]{false};
        JPanel profilePill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (profileHovered[0]) {
                    g2.setColor(new Color(238, 244, 254));
                } else {
                    g2.setColor(new Color(248, 250, 252));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        profilePill.setOpaque(false);
        profilePill.setBorder(new EmptyBorder(3, 8, 3, 14));
        profilePill.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profilePill.setToolTipText("Lecturer Profile (Click to open)");

        // Circular Navy-blue Avatar with Initials
        JPanel miniAvatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_NAVY);
                g2.fillOval(0, 0, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (28 - fm.stringWidth(initials)) / 2;
                int ty = ((28 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, tx, ty);
                g2.dispose();
            }
        };
        miniAvatar.setOpaque(false);
        miniAvatar.setPreferredSize(new Dimension(28, 28));

        // Lecturer Name & Role Labels
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(displayName);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 12));
        nameLbl.setForeground(TEXT_PRIMARY);

        JLabel roleLbl = new JLabel("Lecturer");
        roleLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        roleLbl.setForeground(TEXT_SECONDARY);

        textPanel.add(nameLbl);
        textPanel.add(Box.createVerticalStrut(1));
        textPanel.add(roleLbl);

        profilePill.add(miniAvatar);
        profilePill.add(textPanel);

        // Profile Navigation Interaction
        MouseAdapter profileClick = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                profileHovered[0] = true;
                profilePill.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                profileHovered[0] = false;
                profilePill.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentFrame != null && !(currentFrame instanceof LecturerProfile)) {
                    new LecturerProfile(activeId).setVisible(true);
                    currentFrame.dispose();
                }
            }
        };
        profilePill.addMouseListener(profileClick);
        miniAvatar.addMouseListener(profileClick);
        nameLbl.addMouseListener(profileClick);
        roleLbl.addMouseListener(profileClick);

        rightPanel.add(bellBtn);
        rightPanel.add(profilePill);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    /**
     * Creates the dark navy-blue sidebar matching the Administrator Dashboard.
     */
    public static JPanel createSidebar(String activePage, String lecturerId, JFrame currentFrame) {
        final String safeLecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : LecturerDashboard.getActiveLecturerId();

        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_NAVY, 0, getHeight(), NAVY_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(215, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(18, 42, 75)));

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(24, 12, 12, 12));

        JLabel menuTitle = new JLabel("LECTURER MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 10));
        menuTitle.setForeground(NAV_TEXT_MUTED);
        menuTitle.setBorder(new EmptyBorder(0, 12, 14, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(menuTitle);

        menu.add(createNavItem("Dashboard", "Dashboard".equalsIgnoreCase(activePage), () -> {
            new LecturerDashboard(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));
        menu.add(Box.createVerticalStrut(4));

        menu.add(createNavItem("My Courses", "My Courses".equalsIgnoreCase(activePage), () -> {
            new MyCourses(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));
        menu.add(Box.createVerticalStrut(4));

        menu.add(createNavItem("Students", "Students".equalsIgnoreCase(activePage), () -> {
            new Student(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));
        menu.add(Box.createVerticalStrut(4));

        menu.add(createNavItem("Attendance", "Attendance".equalsIgnoreCase(activePage), () -> {
            new Attendance(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));
        menu.add(Box.createVerticalStrut(4));

        menu.add(createNavItem("Marks / Grades", "Marks / Grades".equalsIgnoreCase(activePage), () -> {
            new MarksGrades(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));
        menu.add(Box.createVerticalStrut(4));

        menu.add(createNavItem("Assignments", "Assignments".equalsIgnoreCase(activePage), () -> {
            new Assignments(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));
        menu.add(Box.createVerticalStrut(4));

        menu.add(createNavItem("Course Materials", "Course Materials".equalsIgnoreCase(activePage), () -> {
            new CourseMaterials(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));
        menu.add(Box.createVerticalStrut(4));

        menu.add(createNavItem("Profile", "Profile".equalsIgnoreCase(activePage), () -> {
            new LecturerProfile(safeLecturerId).setVisible(true);
            currentFrame.dispose();
        }));

        sidebar.add(menu, BorderLayout.NORTH);

        // Bottom Logout
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 12, 22, 12));

        JButton logout = createLogoutButton(() -> {
            int answer = JOptionPane.showConfirmDialog(
                    currentFrame,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (answer == JOptionPane.YES_OPTION) {
                LecturerDashboard.setActiveLecturerId(null);
                LecturerTheme.clearLecturerNameCache(null);
                currentFrame.dispose();
                SwingUtilities.invokeLater(() -> {
                    new ui.login.LoginFrame().setVisible(true);
                });
            }
        });

        bottom.add(logout, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    public static JButton createNavItem(String text, boolean active, Runnable action) {
        final boolean[] isHovered = {false};
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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
                super.paintComponent(g);
            }
        };

        button.setFont(active ? FONT_BOLD_13 : FONT_PLAIN_13);
        button.setForeground(active ? Color.WHITE : NAV_ITEM_TEXT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(190, 40));
        button.setBorder(new EmptyBorder(0, 14, 0, 10));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> {
            if (!active) {
                action.run();
            }
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    isHovered[0] = true;
                    button.setForeground(Color.WHITE);
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    isHovered[0] = false;
                    button.setForeground(NAV_ITEM_TEXT);
                    button.repaint();
                }
            }
        });

        return button;
    }

    public static JButton createLogoutButton(Runnable action) {
        final boolean[] isHovered = {false};
        JButton button = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isHovered[0]) {
                    g2.setColor(new Color(220, 38, 38, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(248, 113, 113, 140));
                } else {
                    g2.setColor(new Color(255, 255, 255, 16));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(255, 255, 255, 35));
                }
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BOLD_12);
        button.setForeground(LOGOUT_RED);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(0, 14, 0, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setPreferredSize(new Dimension(190, 38));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                button.repaint();
            }
        });

        button.addActionListener(e -> action.run());
        return button;
    }

    public static void stylePrimaryButton(JButton button) {
        button.setFont(FONT_BOLD_12);
        button.setBackground(SECONDARY_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFont(FONT_BOLD_12);
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 14, 6, 14)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(FONT_PLAIN_13);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(LIGHT_BLUE);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(238, 242, 246));
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        if (table.getTableHeader() != null) {
            table.getTableHeader().setBackground(new Color(241, 245, 249));
            table.getTableHeader().setForeground(TEXT_PRIMARY);
            table.getTableHeader().setFont(FONT_BOLD_12);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
            table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        }
    }
}
