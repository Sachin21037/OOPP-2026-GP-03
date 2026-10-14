
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LecturerDashboard extends JFrame {

    private static final Color NAVY = new Color(31, 66, 99);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(35, 48, 65);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color TEAL = new Color(43, 169, 155);
    private static final Color PURPLE = new Color(133, 71, 199);
    private static final Color BORDER = new Color(220, 227, 235);
    private static final Color RED = new Color(190, 55, 55);

    private static final Font BODY =
            new Font("Arial", Font.PLAIN, 13);

    public LecturerDashboard() {
        setTitle("Faculty Academic Management System - Lecturer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createDashboardContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(new EmptyBorder(0, 26, 0, 26));

        JLabel title = new JLabel(
                "FACULTY ACADEMIC MANAGEMENT SYSTEM"
        );
        title.setForeground(WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 19));

        JLabel role = new JLabel("LECTURER");
        role.setForeground(WHITE);
        role.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(title, BorderLayout.WEST);
        header.add(role, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 0, 1, BORDER
                )
        );

        JPanel menu = new JPanel();
        menu.setBackground(WHITE);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(25, 14, 12, 14));

        JLabel menuTitle = new JLabel("LECTURER MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 11));
        menuTitle.setForeground(MUTED);
        menuTitle.setBorder(
                new EmptyBorder(0, 9, 18, 0)
        );
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(menuTitle);

        menu.add(createMenuButton(
                "Dashboard", true, () -> {}
        ));

        menu.add(createMenuButton(
                "My Courses", false, () -> {
                    new MyCourses().setVisible(true);
                    dispose();
                }
        ));

        menu.add(createMenuButton(
                "Students", false,
                () -> showComingSoon("Students")
        ));

        menu.add(createMenuButton(
                "Attendance", false,
                () -> showComingSoon("Attendance")
        ));

        menu.add(createMenuButton(
                "Marks / Grades", false,
                () -> showComingSoon("Marks / Grades")
        ));

        menu.add(createMenuButton(
                "Assignments", false,
                () -> showComingSoon("Assignments")
        ));

        menu.add(createMenuButton(
                "My Profile", false,
                () -> showComingSoon("My Profile")
        ));

        sidebar.add(menu, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(WHITE);
        bottom.setBorder(new EmptyBorder(10, 14, 22, 14));

        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Arial", Font.BOLD, 13));
        logout.setForeground(RED);
        logout.setBackground(WHITE);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10, 10, 10, 10));
        logout.setFocusPainted(false);
        logout.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        logout.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                dispose();
                JOptionPane.showMessageDialog(
                        null, "You have been logged out."
                );
            }
        });

        bottom.add(logout, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createMenuButton(
            String text, boolean active, Runnable action) {

        JButton button = new JButton(text);

        button.setFont(active
                ? new Font("Arial", Font.BOLD, 13)
                : BODY);

        button.setForeground(active ? NAVY : TEXT);
        button.setBackground(
                active ? new Color(232, 240, 248) : WHITE
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );
        button.setPreferredSize(new Dimension(175, 43));

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        active
                                ? BorderFactory.createMatteBorder(
                                0, 4, 0, 0, NAVY
                        )
                                : BorderFactory.createEmptyBorder(
                                0, 4, 0, 0
                        ),
                        new EmptyBorder(0, 13, 0, 8)
                )
        );

        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> action.run());

        button.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {
                        if (!active) {
                            button.setBackground(
                                    new Color(245, 248, 252)
                            );
                        }
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {
                        button.setBackground(
                                active
                                        ? new Color(232, 240, 248)
                                        : WHITE
                        );
                    }
                }
        );

        return button;
    }

    private JPanel createDashboardContent() {
        JPanel content = new JPanel(
                new BorderLayout(0, 22)
        );

        content.setBackground(BACKGROUND);
        content.setBorder(
                new EmptyBorder(28, 30, 30, 30)
        );

        JPanel welcome = new JPanel();
        welcome.setBackground(BACKGROUND);
        welcome.setLayout(
                new BoxLayout(welcome, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Welcome, Lecturer");
        heading.setFont(new Font("Arial", Font.BOLD, 29));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Manage your courses, students, attendance "
                        + "and academic activities."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        subtitle.setBorder(
                new EmptyBorder(5, 1, 0, 0)
        );

        welcome.add(heading);
        welcome.add(subtitle);

        content.add(welcome, BorderLayout.NORTH);

        JPanel main = new JPanel(
                new BorderLayout(0, 22)
        );
        main.setBackground(BACKGROUND);

        JPanel stats = new JPanel(
                new GridLayout(1, 4, 16, 0)
        );
        stats.setBackground(BACKGROUND);

        stats.add(createStatCard(
                "MY COURSES", "4", NAVY,
                "Assigned courses"
        ));

        stats.add(createStatCard(
                "STUDENTS", "86", TEAL,
                "Across your courses"
        ));

        stats.add(createStatCard(
                "ASSIGNMENTS", "12",
                new Color(48, 77, 111),
                "Created this semester"
        ));

        stats.add(createStatCard(
                "PENDING MARKS", "8", PURPLE,
                "Need your attention"
        ));

        main.add(stats, BorderLayout.NORTH);

        JPanel activity = new JPanel(
                new BorderLayout(0, 16)
        );

        activity.setBackground(WHITE);
        activity.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(20, 22, 20, 22)
                )
        );

        JPanel activityHeading = new JPanel(
                new BorderLayout()
        );
        activityHeading.setBackground(WHITE);

        JLabel activityTitle =
                new JLabel("Lecturer Activities");

        activityTitle.setFont(
                new Font("Arial", Font.BOLD, 18)
        );
        activityTitle.setForeground(TEXT);

        JLabel recent = new JLabel("RECENT UPDATES");
        recent.setFont(new Font("Arial", Font.BOLD, 10));
        recent.setForeground(MUTED);

        activityHeading.add(
                activityTitle, BorderLayout.WEST
        );
        activityHeading.add(recent, BorderLayout.EAST);

        activity.add(activityHeading, BorderLayout.NORTH);

        JPanel activityList = new JPanel();
        activityList.setBackground(WHITE);
        activityList.setLayout(
                new BoxLayout(activityList, BoxLayout.Y_AXIS)
        );

        activityList.add(createActivity(
                "Attendance updated for ICT2132",
                "ATTENDANCE", TEAL
        ));

        activityList.add(createActivity(
                "New assignment created",
                "ASSIGNMENT", NAVY
        ));

        activityList.add(createActivity(
                "Marks submitted for Programming",
                "MARKS", PURPLE
        ));

        activityList.add(createActivity(
                "Course materials updated",
                "MATERIALS", new Color(190, 125, 45)
        ));

        activity.add(activityList, BorderLayout.CENTER);

        main.add(activity, BorderLayout.CENTER);
        content.add(main, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(
            String title, String value,
            Color accent, String caption) {

        JPanel card = new JPanel(
                new BorderLayout(0, 12)
        );

        card.setBackground(WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(17, 18, 16, 18)
                )
        );

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 11)
        );
        titleLabel.setForeground(MUTED);

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Arial", Font.BOLD, 13));

        top.add(titleLabel, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);

        JLabel number = new JLabel(value);
        number.setFont(
                new Font("Arial", Font.BOLD, 34)
        );
        number.setForeground(accent);

        JLabel note = new JLabel(caption);
        note.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );
        note.setForeground(MUTED);

        JPanel lower = new JPanel();
        lower.setBackground(WHITE);
        lower.setLayout(
                new BoxLayout(lower, BoxLayout.Y_AXIS)
        );

        lower.add(number);
        lower.add(Box.createVerticalStrut(3));
        lower.add(note);

        card.add(top, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActivity(
            String text, String category, Color accent) {

        JPanel row = new JPanel(
                new BorderLayout(12, 0)
        );

        row.setBackground(WHITE);
        row.setBorder(
                new EmptyBorder(12, 0, 12, 0)
        );
        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 58)
        );

        JPanel marker = new JPanel();
        marker.setBackground(accent);
        marker.setPreferredSize(new Dimension(4, 34));

        JLabel description = new JLabel(text);
        description.setFont(BODY);
        description.setForeground(TEXT);

        JLabel tag = new JLabel(category);
        tag.setFont(new Font("Arial", Font.BOLD, 10));
        tag.setForeground(accent);
        tag.setBorder(
                new EmptyBorder(4, 8, 4, 8)
        );
        tag.setOpaque(true);

        tag.setBackground(new Color(
                Math.min(255, accent.getRed() + 105),
                Math.min(255, accent.getGreen() + 105),
                Math.min(255, accent.getBlue() + 105)
        ));

        row.add(marker, BorderLayout.WEST);
        row.add(description, BorderLayout.CENTER);
        row.add(tag, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 60)
        );

        return wrapper;
    }

    private void showComingSoon(String page) {
        JOptionPane.showMessageDialog(
                this,
                page + " page will be added next.",
                page,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> new LecturerDashboard().setVisible(true)
        );
    }
}
