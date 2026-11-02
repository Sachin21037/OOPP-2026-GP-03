package ui.lecturer;

import dao.LecturerDashboardDAO;
import dao.LecturerDashboardDAO.ActivityItem;
import dao.LecturerDashboardDAO.DashboardData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * LecturerDashboardUI - Presentation layer for the Lecturer Dashboard.
 * Delegates database retrieval and logic to service.LecturerDashboardDAO.
 */
public class LecturerDashboardUI extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;

    private static final Font BODY = new Font("Arial", Font.PLAIN, 13);

    // Static active lecturer ID to support session persistence across pages
    protected static String activeLecturerId = null;

    protected final String lecturerId;
    protected DashboardData dashboardData;

    public LecturerDashboardUI() {
        this(resolveActiveLecturerId());
    }

    public LecturerDashboardUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            activeLecturerId = this.lecturerId;
        }

        // Fetch metrics via LecturerDashboardLogic service
        this.dashboardData = LecturerDashboardDAO.loadDashboardData(this.lecturerId);
        if (this.lecturerId != null && dashboardData.lecturerDisplayName != null) {
            LecturerTheme.setLecturerDisplayName(this.lecturerId, dashboardData.lecturerDisplayName);
        }

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

    public static void setActiveLecturerId(String id) {
        activeLecturerId = (id != null && !id.trim().isEmpty()) ? id.trim() : null;
    }

    public static String getActiveLecturerId() {
        return activeLecturerId;
    }

    protected static String resolveActiveLecturerId() {
        if (activeLecturerId != null && !activeLecturerId.trim().isEmpty()) {
            return activeLecturerId.trim();
        }
        String sysProp = System.getProperty("fams.lecturer.id");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return null;
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Dashboard", lecturerId, this);
    }

    private JPanel createDashboardContent() {
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(28, 30, 30, 30));

        JPanel welcome = new JPanel();
        welcome.setBackground(BACKGROUND);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Welcome, " + dashboardData.lecturerDisplayName);
        heading.setFont(new Font("Arial", Font.BOLD, 29));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(dashboardData.welcomeSubtitle);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        subtitle.setBorder(new EmptyBorder(5, 1, 0, 0));

        welcome.add(heading);
        welcome.add(subtitle);
        content.add(welcome, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(0, 22));
        main.setBackground(BACKGROUND);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setBackground(BACKGROUND);

        stats.add(createStatCard("MY COURSES", dashboardData.myCoursesValue, NAVY, dashboardData.coursesCaption));
        stats.add(createStatCard("STUDENTS", dashboardData.studentsValue, TEAL, dashboardData.studentsCaption));
        stats.add(createStatCard("ASSIGNMENTS", dashboardData.assignmentsValue, new Color(48, 77, 111), dashboardData.assignmentsCaption));
        stats.add(createStatCard("PENDING MARKS", dashboardData.pendingMarksValue, PURPLE, dashboardData.pendingMarksCaption));

        main.add(stats, BorderLayout.NORTH);

        JPanel activity = new JPanel(new BorderLayout(0, 16));
        activity.setBackground(WHITE);
        activity.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(20, 22, 20, 22)
        ));

        JPanel activityHeading = new JPanel(new BorderLayout());
        activityHeading.setBackground(WHITE);

        JLabel activityTitle = new JLabel("Lecturer Activities");
        activityTitle.setFont(new Font("Arial", Font.BOLD, 18));
        activityTitle.setForeground(TEXT);

        JLabel recent = new JLabel("RECENT UPDATES");
        recent.setFont(new Font("Arial", Font.BOLD, 10));
        recent.setForeground(MUTED);

        activityHeading.add(activityTitle, BorderLayout.WEST);
        activityHeading.add(recent, BorderLayout.EAST);
        activity.add(activityHeading, BorderLayout.NORTH);

        JPanel activityList = new JPanel();
        activityList.setBackground(WHITE);
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));

        for (ActivityItem item : dashboardData.activityItems) {
            activityList.add(createActivity(item.text, item.category, item.accent));
        }

        activity.add(activityList, BorderLayout.CENTER);
        main.add(activity, BorderLayout.CENTER);
        content.add(main, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(String title, String value, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(17, 18, 16, 18)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Arial", Font.BOLD, 13));

        top.add(titleLabel, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);

        JLabel number = new JLabel(value);
        number.setFont(new Font("Arial", Font.BOLD, 34));
        number.setForeground(accent);

        JLabel note = new JLabel(caption);
        note.setFont(new Font("Arial", Font.PLAIN, 11));
        note.setForeground(MUTED);

        JPanel lower = new JPanel();
        lower.setBackground(WHITE);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));

        lower.add(number);
        lower.add(Box.createVerticalStrut(3));
        lower.add(note);

        card.add(top, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActivity(String text, String category, Color accent) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(WHITE);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel marker = new JPanel();
        marker.setBackground(accent);
        marker.setPreferredSize(new Dimension(4, 34));

        JLabel description = new JLabel(text);
        description.setFont(BODY);
        description.setForeground(TEXT);

        JLabel tag = new JLabel(category);
        tag.setFont(new Font("Arial", Font.BOLD, 10));
        tag.setForeground(accent);
        tag.setBorder(new EmptyBorder(4, 8, 4, 8));
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
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        return wrapper;
    }

    public static void main(String[] args) {
        String lecturerArg = (args != null && args.length > 0) ? args[0] : null;
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LecturerDashboardUI(lecturerArg).setVisible(true);
        });
    }
}

