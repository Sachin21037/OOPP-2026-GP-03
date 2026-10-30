package ui.lecturer;

import dao.MyCoursesDAO;
import dao.MyCoursesDAO.CourseItem;
import dao.MyCoursesDAO.CoursesSummary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MyCoursesUI - Presentation layer for Lecturer Assigned Courses.
 * Layout and controls aligned with Faculty Academic Management System specifications.
 */
public class MyCoursesUI extends JFrame {

    private final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private final Color WHITE = LecturerTheme.CARD_BG;
    private final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private final Color TEAL = new Color(13, 148, 136);
    private final Color BORDER = LecturerTheme.BORDER_COLOR;
    private final Color PRIMARY_BLUE = new Color(29, 78, 216); // Royal blue #1D4ED8

    protected String lecturerId;

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> semesterFilter;

    private JLabel totalCoursesValue;
    private JLabel totalStudentsValue;
    private JLabel subtitleLabel;
    private JLabel countLabel;

    private final List<CourseItem> loadedCourses = new ArrayList<>();

    public MyCoursesUI() {
        this(resolveActiveLecturerId());
    }

    public MyCoursesUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - My Courses");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadCourses();
    }

    private static String resolveActiveLecturerId() {
        String active = LecturerDashboardUI.getActiveLecturerId();
        if (active != null && !active.trim().isEmpty()) {
            return active.trim();
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
        return LecturerTheme.createSidebar("My Courses", lecturerId, this);
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Header & Summary Area
        JPanel top = new JPanel();
        top.setBackground(BACKGROUND);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // 1. Heading row with title on left and "LECTURER PORTAL" on right
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(BACKGROUND);

        JPanel titles = new JPanel();
        titles.setBackground(BACKGROUND);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Courses");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(TEXT);

        String activeDisplayId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
        subtitleLabel = new JLabel("View and manage the courses assigned to you (" + activeDisplayId + ").");
        subtitleLabel.setFont(LecturerTheme.FONT_PLAIN_13);
        subtitleLabel.setForeground(MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // LECTURER PORTAL badge on top right
        JPanel portalBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        portalBadge.setBackground(WHITE);
        portalBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(4, 12, 4, 12)
        ));
        JLabel portalLabel = new JLabel("LECTURER PORTAL");
        portalLabel.setFont(new Font("Arial", Font.BOLD, 11));
        portalLabel.setForeground(new Color(30, 41, 59));
        portalBadge.add(portalLabel);

        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(portalBadge, BorderLayout.EAST);
        top.add(titleRow);
        top.add(Box.createVerticalStrut(18));

        // 2. Summary cards (TOTAL COURSES and TOTAL STUDENTS)
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        summaryPanel.setBackground(BACKGROUND);
        summaryPanel.setPreferredSize(new Dimension(0, 96));

        totalCoursesValue = new JLabel("-");
        totalStudentsValue = new JLabel("-");

        summaryPanel.add(createSummaryCard("TOTAL COURSES", totalCoursesValue, NAVY, "Courses assigned to you"));
        summaryPanel.add(createSummaryCard("TOTAL STUDENTS", totalStudentsValue, TEAL, "Students across all courses"));

        top.add(summaryPanel);
        content.add(top, BorderLayout.NORTH);

        // 3. Courses Assigned to Me Table Container Card
        JPanel tableContainer = new JPanel(new BorderLayout(0, 14));
        tableContainer.setBackground(WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Table Header Bar: "Courses Assigned to Me" (left) and "1 COURSES" (right)
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setBackground(WHITE);

        JLabel tblTitle = new JLabel("Courses Assigned to Me");
        tblTitle.setFont(new Font("Arial", Font.BOLD, 15));
        tblTitle.setForeground(TEXT);

        countLabel = new JLabel("0 COURSES");
        countLabel.setFont(new Font("Arial", Font.BOLD, 11));
        countLabel.setForeground(MUTED);

        tableHeaderBar.add(tblTitle, BorderLayout.WEST);
        tableHeaderBar.add(countLabel, BorderLayout.EAST);
        tableContainer.add(tableHeaderBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Course Code", "Course Name", "Students", "Semester"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(38);
        courseTable.setFont(LecturerTheme.FONT_PLAIN_13);
        courseTable.setGridColor(BORDER);
        courseTable.setSelectionBackground(LecturerTheme.LIGHT_BLUE);
        courseTable.setSelectionForeground(NAVY);
        courseTable.setShowGrid(true);

        JTableHeader th = courseTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        courseTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(360);
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(140);

        courseTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 12, 0, 0));
        courseTable.getColumnModel().getColumn(1).setCellRenderer(leftIndentRenderer);

        courseTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer());
        courseTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // 4. Bottom Toolbar: Left: Search & Semester | Right: Refresh & View Course
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(WHITE);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Left controls: Search: [ input ] [ All Semesters ▾ ]
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setBackground(WHITE);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("Arial", Font.BOLD, 12));
        searchLbl.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(LecturerTheme.FONT_PLAIN_13);
        searchField.setPreferredSize(new Dimension(175, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 8, 0, 8)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterCourses(); }
            public void removeUpdate(DocumentEvent e) { filterCourses(); }
            public void changedUpdate(DocumentEvent e) { filterCourses(); }
        });

        semesterFilter = new JComboBox<>(new String[]{"All Semesters", "Semester 1", "Semester 2"});
        semesterFilter.setFont(LecturerTheme.FONT_PLAIN_13);
        semesterFilter.setBackground(WHITE);
        semesterFilter.setPreferredSize(new Dimension(135, 32));
        semesterFilter.addActionListener(e -> filterCourses());

        leftControls.add(searchLbl);
        leftControls.add(searchField);
        leftControls.add(semesterFilter);

        // Right controls: [ Refresh ] [ View Course ]
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setBackground(WHITE);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(LecturerTheme.FONT_BOLD_12);
        refreshBtn.setBackground(WHITE);
        refreshBtn.setForeground(TEXT);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(95, 32));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(4, 12, 4, 12)
        ));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            semesterFilter.setSelectedIndex(0);
            loadCourses();
        });

        JButton viewCourseBtn = new JButton("View Course");
        viewCourseBtn.setFont(LecturerTheme.FONT_BOLD_12);
        viewCourseBtn.setBackground(PRIMARY_BLUE);
        viewCourseBtn.setForeground(Color.WHITE);
        viewCourseBtn.setFocusPainted(false);
        viewCourseBtn.setBorderPainted(false);
        viewCourseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewCourseBtn.setPreferredSize(new Dimension(115, 32));
        viewCourseBtn.addActionListener(e -> viewSelectedCourse());

        rightControls.add(refreshBtn);
        rightControls.add(viewCourseBtn);

        bottomBar.add(leftControls, BorderLayout.WEST);
        bottomBar.add(rightControls, BorderLayout.EAST);

        tableContainer.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableContainer, BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                // Top accent stripe & left accent stripe matching screenshot
                g2.setColor(accent);
                g2.fillRect(0, 0, getWidth(), 3);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 11));
        titleLbl.setForeground(MUTED);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(TEXT);

        JLabel capLbl = new JLabel(caption);
        capLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        capLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(capLbl, BorderLayout.SOUTH);
        return card;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    public void loadCourses() {
        loadedCourses.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        try {
            List<CourseItem> list = MyCoursesDAO.loadCourses(lecturerId);
            loadedCourses.addAll(list);

            if (subtitleLabel != null) {
                String idLabel = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
                subtitleLabel.setText("View and manage the courses assigned to you (" + idLabel + ").");
            }
        } catch (SQLException e) {
            System.err.println("[MyCoursesUI] DB Error: " + e.getMessage());
            if (subtitleLabel != null) subtitleLabel.setText("Database error: " + e.getMessage());
        }

        filterCourses();
    }

    public void filterCourses() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        String sem = semesterFilter != null ? (String) semesterFilter.getSelectedItem() : null;
        String search = searchField != null ? searchField.getText().trim() : "";

        List<CourseItem> filtered = MyCoursesDAO.filterCourses(loadedCourses, sem, search);

        for (CourseItem c : filtered) {
            tableModel.addRow(c.toTableRow());
        }

        if (countLabel != null) {
            countLabel.setText(filtered.size() + " COURSES");
        }

        CoursesSummary summary = MyCoursesDAO.calculateSummary(filtered);
        if (totalCoursesValue != null) totalCoursesValue.setText(String.valueOf(summary.totalCourses));
        if (totalStudentsValue != null) totalStudentsValue.setText(String.valueOf(summary.totalStudents));
    }

    private void viewSelectedCourse() {
        int row = courseTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to view details.", "View Course", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String code = String.valueOf(tableModel.getValueAt(row, 0));
        CourseItem found = null;
        for (CourseItem c : loadedCourses) {
            if (c.courseCode.equalsIgnoreCase(code)) {
                found = c;
                break;
            }
        }
        if (found == null) return;

        JOptionPane.showMessageDialog(this,
                "Course Code: " + found.courseCode + "\n" +
                "Course Name: " + found.courseName + "\n" +
                "Academic Year: " + found.academicYear + "\n" +
                "Semester: " + found.semester + "\n" +
                "Credits: " + found.credits + "\n" +
                "Batch: " + (found.batch.isEmpty() ? "Standard" : found.batch) + "\n" +
                "Enrolled Students: " + found.studentCount,
                "Course Details - " + found.courseCode,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        String testId = args != null && args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboardUI.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MyCoursesUI(testId).setVisible(true);
        });
    }
}
