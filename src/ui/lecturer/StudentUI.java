package ui.lecturer;

import dao.StudentDAO;
import dao.StudentDAO.StudentItem;
import dao.StudentDAO.StudentsSummary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * StudentUI - Presentation layer for Student Records in Lecturer Module.
 * Layout and controls aligned with Faculty Academic Management System specifications.
 */
public class StudentUI extends JFrame {

    private final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private final Color WHITE = LecturerTheme.CARD_BG;
    private final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private final Color TEAL = new Color(13, 148, 136);
    private final Color PURPLE = new Color(124, 58, 237);
    private final Color BORDER = LecturerTheme.BORDER_COLOR;
    private final Color PRIMARY_BLUE = new Color(29, 78, 216); // Royal blue #1D4ED8

    protected String lecturerId;

    private JTable studentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JComboBox<String> courseFilter;
    private JComboBox<String> batchFilter;
    private JTextField searchField;

    private JLabel subtitleLabel;
    private JLabel countLabel;
    private JLabel totalStudentsValue;
    private JLabel activeStudentsValue;
    private JLabel courseCountValue;

    private final List<StudentItem> loadedStudents = new ArrayList<>();
    private final List<StudentItem> displayedStudents = new ArrayList<>();

    public StudentUI() {
        this(resolveActiveLecturerId());
    }

    public StudentUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Students");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadStudents();
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
        return LecturerTheme.createSidebar("Students", lecturerId, this);
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Header & Summary Area
        JPanel top = new JPanel();
        top.setBackground(BACKGROUND);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // 1. Heading row: "Students" title on left and "LECTURER PORTAL" on right
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(BACKGROUND);

        JPanel titles = new JPanel();
        titles.setBackground(BACKGROUND);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Students");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(TEXT);

        String activeDisplayId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
        subtitleLabel = new JLabel("View and manage students enrolled in your courses (" + activeDisplayId + ").");
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

        // 2. Summary cards (TOTAL STUDENTS, ACTIVE STUDENTS, COURSES)
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        summaryPanel.setBackground(BACKGROUND);
        summaryPanel.setPreferredSize(new Dimension(0, 96));

        totalStudentsValue = new JLabel("-");
        activeStudentsValue = new JLabel("-");
        courseCountValue = new JLabel("-");

        summaryPanel.add(createSummaryCard("TOTAL STUDENTS", totalStudentsValue, NAVY, "Enrolled across your courses"));
        summaryPanel.add(createSummaryCard("ACTIVE STUDENTS", activeStudentsValue, TEAL, "Currently marked active"));
        summaryPanel.add(createSummaryCard("COURSES", courseCountValue, PURPLE, "Courses represented in the list"));

        top.add(summaryPanel);
        content.add(top, BorderLayout.NORTH);

        // 3. Table Container Card
        JPanel tableContainer = new JPanel(new BorderLayout(0, 14));
        tableContainer.setBackground(WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Header bar inside card: "Student Directory" (left) and "20 STUDENTS ENROLLED" (right)
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setBackground(WHITE);

        JLabel tblTitle = new JLabel("Student Directory");
        tblTitle.setFont(new Font("Arial", Font.BOLD, 15));
        tblTitle.setForeground(TEXT);

        countLabel = new JLabel("0 STUDENTS ENROLLED");
        countLabel.setFont(new Font("Arial", Font.BOLD, 11));
        countLabel.setForeground(MUTED);

        tableHeaderBar.add(tblTitle, BorderLayout.WEST);
        tableHeaderBar.add(countLabel, BorderLayout.EAST);
        tableContainer.add(tableHeaderBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Student ID", "Index Number", "Student Name", "Course Code", "Batch", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        rowSorter = new TableRowSorter<>(tableModel);
        studentTable = new JTable(tableModel);
        studentTable.setRowSorter(rowSorter);
        studentTable.setRowHeight(38);
        studentTable.setFont(LecturerTheme.FONT_PLAIN_13);
        studentTable.setGridColor(BORDER);
        studentTable.setSelectionBackground(LecturerTheme.LIGHT_BLUE);
        studentTable.setSelectionForeground(NAVY);
        studentTable.setShowGrid(true);

        JTableHeader th = studentTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        studentTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(110);

        studentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());
        studentTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 12, 0, 0));
        studentTable.getColumnModel().getColumn(2).setCellRenderer(leftIndentRenderer);

        studentTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());
        studentTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        studentTable.getColumnModel().getColumn(5).setCellRenderer(statusRenderer());

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // 4. Bottom Toolbar: Left: Search, Course, Batch | Right: Refresh, View Details
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(WHITE);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Left controls: Search: [ input ] [ All Courses ▾ ] [ All Batches ▾ ]
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
            public void insertUpdate(DocumentEvent e) { filterStudents(); }
            public void removeUpdate(DocumentEvent e) { filterStudents(); }
            public void changedUpdate(DocumentEvent e) { filterStudents(); }
        });

        courseFilter = new JComboBox<>(new String[]{"All Courses"});
        courseFilter.setFont(LecturerTheme.FONT_PLAIN_13);
        courseFilter.setBackground(WHITE);
        courseFilter.setPreferredSize(new Dimension(135, 32));
        courseFilter.addActionListener(e -> filterStudents());

        batchFilter = new JComboBox<>(new String[]{"All Batches"});
        batchFilter.setFont(LecturerTheme.FONT_PLAIN_13);
        batchFilter.setBackground(WHITE);
        batchFilter.setPreferredSize(new Dimension(125, 32));
        batchFilter.addActionListener(e -> filterStudents());

        leftControls.add(searchLbl);
        leftControls.add(searchField);
        leftControls.add(courseFilter);
        leftControls.add(batchFilter);

        // Right controls: [ Refresh ] [ View Details ]
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
            if (courseFilter.getItemCount() > 0) courseFilter.setSelectedIndex(0);
            if (batchFilter.getItemCount() > 0) batchFilter.setSelectedIndex(0);
            loadStudents();
        });

        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(LecturerTheme.FONT_BOLD_12);
        viewBtn.setBackground(PRIMARY_BLUE);
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFocusPainted(false);
        viewBtn.setBorderPainted(false);
        viewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewBtn.setPreferredSize(new Dimension(115, 32));
        viewBtn.addActionListener(e -> viewSelectedStudent());

        rightControls.add(refreshBtn);
        rightControls.add(viewBtn);

        bottomBar.add(leftControls, BorderLayout.WEST);
        bottomBar.add(rightControls, BorderLayout.EAST);

        tableContainer.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableContainer, BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color accent, String description) {
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

        value.setFont(new Font("Arial", Font.BOLD, 26));
        value.setForeground(TEXT);

        JLabel descLbl = new JLabel(description);
        descLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        descLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(descLbl, BorderLayout.SOUTH);
        return card;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String s = value != null ? value.toString() : "";
                if (!isSelected) {
                    if ("Active".equalsIgnoreCase(s)) {
                        l.setBackground(new Color(236, 253, 245)); // soft mint green #ECFDF5
                        l.setForeground(new Color(5, 150, 105));   // dark emerald green #059669
                        l.setFont(new Font("Arial", Font.BOLD, 12));
                    } else {
                        l.setBackground(new Color(254, 242, 242));
                        l.setForeground(new Color(220, 38, 38));
                        l.setFont(new Font("Arial", Font.BOLD, 12));
                    }
                }
                return l;
            }
        };
    }

    public void loadStudents() {
        loadedStudents.clear();
        displayedStudents.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        try {
            List<StudentItem> list = StudentDAO.loadStudents(lecturerId);
            loadedStudents.addAll(list);

            // Populate filters
            courseFilter.removeAllItems();
            courseFilter.addItem("All Courses");
            batchFilter.removeAllItems();
            batchFilter.addItem("All Batches");

            Set<String> courses = new TreeSet<>();
            Set<String> batches = new TreeSet<>();
            for (StudentItem s : loadedStudents) {
                if (!s.courseCode.isEmpty()) courses.add(s.courseCode);
                if (!s.batch.isEmpty()) batches.add(s.batch);
            }
            for (String c : courses) courseFilter.addItem(c);
            for (String b : batches) batchFilter.addItem(b);

            if (subtitleLabel != null) {
                String idLabel = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
                subtitleLabel.setText("View and manage students enrolled in your courses (" + idLabel + ").");
            }
        } catch (SQLException e) {
            System.err.println("[StudentUI] DB Error: " + e.getMessage());
            if (subtitleLabel != null) subtitleLabel.setText("Database error: " + e.getMessage());
        }

        filterStudents();
    }

    public void filterStudents() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        displayedStudents.clear();

        String selCourse = courseFilter != null ? (String) courseFilter.getSelectedItem() : null;
        String selBatch = batchFilter != null ? (String) batchFilter.getSelectedItem() : null;
        String search = searchField != null ? searchField.getText().trim() : "";

        List<StudentItem> filtered = StudentDAO.filterStudents(loadedStudents, selCourse, selBatch, search);

        for (StudentItem s : filtered) {
            displayedStudents.add(s);
            tableModel.addRow(s.toTableRow());
        }

        if (countLabel != null) {
            countLabel.setText(filtered.size() + " STUDENTS ENROLLED");
        }

        StudentsSummary summary = StudentDAO.calculateSummary(filtered);
        if (totalStudentsValue != null) totalStudentsValue.setText(String.valueOf(summary.totalStudents));
        if (activeStudentsValue != null) activeStudentsValue.setText(String.valueOf(summary.activeStudents));
        if (courseCountValue != null) courseCountValue.setText(String.valueOf(summary.courseCount));
    }

    private void viewSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to view details.", "View Student", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = studentTable.convertRowIndexToModel(row);
        StudentItem s = displayedStudents.get(modelRow);

        JOptionPane.showMessageDialog(this,
                "Student ID: " + s.studentId + "\n" +
                "Index Number: " + s.indexNumber + "\n" +
                "Full Name: " + s.studentName + "\n" +
                "Email: " + (s.email.isEmpty() ? "Not specified" : s.email) + "\n" +
                "Enrolled Course: " + s.courseCode + " - " + s.courseName + "\n" +
                "Batch: " + s.batch + "\n" +
                "Academic Year: " + s.academicYear + "\n" +
                "Department: " + (s.department.isEmpty() ? "Faculty Department" : s.department) + "\n" +
                "Account Status: " + s.status + "\n" +
                "Enrollment Status: " + (s.studentStatus.isEmpty() ? "Active Student" : s.studentStatus),
                "Student Information - " + s.studentId,
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
            new StudentUI(testId).setVisible(true);
        });
    }
}
