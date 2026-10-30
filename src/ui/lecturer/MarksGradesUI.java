package ui.lecturer;

import dao.MarksGradesDAO;
import dao.MarksGradesDAO.AssessmentItem;
import dao.MarksGradesDAO.CourseOfferingItem;
import dao.MarksGradesDAO.MarksSummary;
import dao.MarksGradesDAO.StudentMarksRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * MarksGradesUI - User interface for Viewing, Filtering, and Managing Student Marks & Grades.
 * Aligned with Faculty Academic Management System specifications.
 */
public class MarksGradesUI extends JFrame {

    private final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private final Color WHITE = LecturerTheme.CARD_BG;
    private final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private final Color TEAL = new Color(13, 148, 136);
    private final Color PURPLE = new Color(124, 58, 237);
    private final Color RED = new Color(220, 38, 38);
    private final Color BORDER = LecturerTheme.BORDER_COLOR;
    private final Color PRIMARY_BLUE = new Color(29, 78, 216); // Royal Blue #1D4ED8

    private String lecturerId;

    private DefaultTableModel tableModel;
    private JTable marksTable;

    private JComboBox<String> courseFilterCombo;
    private JComboBox<String> semesterFilterCombo;
    private JTextField searchField;

    private JLabel totalStudentsValue;
    private JLabel averageMarksValue;
    private JLabel highestMarksValue;
    private JLabel lowestMarksValue;

    private JLabel subtitleLabel;
    private JLabel recordCountLabel;

    private final List<StudentMarksRecord> loadedMarks = new ArrayList<>();
    private final List<CourseOfferingItem> courseOfferings = new ArrayList<>();

    public MarksGradesUI() {
        this(resolveActiveLecturerId());
    }

    public MarksGradesUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Marks / Grades");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadDatabaseData();
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
        return LecturerTheme.createSidebar("Marks / Grades", lecturerId, this);
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Top Section: Title row & 4 Summary Cards
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // 1. Heading row: "Marks / Grades" on left, "LECTURER PORTAL" on right
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel titleLabel = new JLabel("Marks / Grades");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(TEXT);

        String activeDisplayId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
        subtitleLabel = new JLabel("Displaying assessment marks for assigned courses (Lecturer: " + activeDisplayId + ").");
        subtitleLabel.setFont(LecturerTheme.FONT_PLAIN_13);
        subtitleLabel.setForeground(MUTED);

        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // LECTURER PORTAL badge
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
        topSection.add(titleRow);
        topSection.add(Box.createVerticalStrut(18));

        // 2. Summary Cards (TOTAL STUDENTS, AVERAGE MARKS, HIGHEST MARKS, LOWEST MARKS)
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(0, 96));

        totalStudentsValue = new JLabel("-");
        averageMarksValue = new JLabel("-");
        highestMarksValue = new JLabel("-");
        lowestMarksValue = new JLabel("-");

        summaryPanel.add(createSummaryCard("TOTAL STUDENTS", totalStudentsValue, NAVY, "Enrolled in course"));
        summaryPanel.add(createSummaryCard("AVERAGE MARKS", averageMarksValue, TEAL, "Class average score"));
        summaryPanel.add(createSummaryCard("HIGHEST MARKS", highestMarksValue, PURPLE, "Top recorded score"));
        summaryPanel.add(createSummaryCard("LOWEST MARKS", lowestMarksValue, RED, "Minimum score"));

        topSection.add(summaryPanel);
        content.add(topSection, BorderLayout.NORTH);

        // 3. Main Container Card
        JPanel tableContainer = new JPanel(new BorderLayout(0, 12));
        tableContainer.setBackground(WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Container Top Area: Title + Record count, followed by Filter Row
        JPanel containerTop = new JPanel();
        containerTop.setLayout(new BoxLayout(containerTop, BoxLayout.Y_AXIS));
        containerTop.setBackground(WHITE);

        // Header: "Student Assessment Marks & Grades" (left) and "20 RECORDS" (right)
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setBackground(WHITE);

        JLabel tableTitle = new JLabel("Student Assessment Marks & Grades");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 15));
        tableTitle.setForeground(TEXT);

        recordCountLabel = new JLabel("0 RECORDS");
        recordCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        recordCountLabel.setForeground(MUTED);

        tableHeaderBar.add(tableTitle, BorderLayout.WEST);
        tableHeaderBar.add(recordCountLabel, BorderLayout.EAST);
        containerTop.add(tableHeaderBar);
        containerTop.add(Box.createVerticalStrut(12));

        // Filter Row: Course, Semester, Search on left | Filter, Reset on right
        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setBackground(WHITE);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilters.setBackground(WHITE);

        JLabel courseLbl = new JLabel("Course:");
        courseLbl.setFont(new Font("Arial", Font.BOLD, 12));
        courseLbl.setForeground(TEXT);

        courseFilterCombo = new JComboBox<>(new String[]{"All Courses"});
        courseFilterCombo.setFont(LecturerTheme.FONT_PLAIN_13);
        courseFilterCombo.setPreferredSize(new Dimension(150, 32));
        courseFilterCombo.setBackground(WHITE);
        courseFilterCombo.addActionListener(e -> applyFilters());

        JLabel semLbl = new JLabel("Semester:");
        semLbl.setFont(new Font("Arial", Font.BOLD, 12));
        semLbl.setForeground(TEXT);

        semesterFilterCombo = new JComboBox<>(new String[]{"All Semesters", "Semester 1", "Semester 2"});
        semesterFilterCombo.setFont(LecturerTheme.FONT_PLAIN_13);
        semesterFilterCombo.setPreferredSize(new Dimension(135, 32));
        semesterFilterCombo.setBackground(WHITE);
        semesterFilterCombo.addActionListener(e -> applyFilters());

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("Arial", Font.BOLD, 12));
        searchLbl.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(LecturerTheme.FONT_PLAIN_13);
        searchField.setPreferredSize(new Dimension(150, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 8, 0, 8)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        leftFilters.add(courseLbl);
        leftFilters.add(courseFilterCombo);
        leftFilters.add(semLbl);
        leftFilters.add(semesterFilterCombo);
        leftFilters.add(searchLbl);
        leftFilters.add(searchField);

        JPanel rightFilterBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightFilterBtns.setBackground(WHITE);

        JButton filterBtn = new JButton("Filter");
        filterBtn.setFont(LecturerTheme.FONT_BOLD_12);
        filterBtn.setBackground(PRIMARY_BLUE);
        filterBtn.setForeground(Color.WHITE);
        filterBtn.setFocusPainted(false);
        filterBtn.setBorderPainted(false);
        filterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filterBtn.setPreferredSize(new Dimension(85, 32));
        filterBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(LecturerTheme.FONT_BOLD_12);
        resetBtn.setBackground(WHITE);
        resetBtn.setForeground(TEXT);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.setPreferredSize(new Dimension(80, 32));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(4, 12, 4, 12)
        ));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            if (courseFilterCombo.getItemCount() > 0) courseFilterCombo.setSelectedIndex(0);
            semesterFilterCombo.setSelectedIndex(0);
            applyFilters();
        });

        rightFilterBtns.add(filterBtn);
        rightFilterBtns.add(resetBtn);

        filterBar.add(leftFilters, BorderLayout.WEST);
        filterBar.add(rightFilterBtns, BorderLayout.EAST);
        containerTop.add(filterBar);

        tableContainer.add(containerTop, BorderLayout.NORTH);

        // 4. Table with 8 columns matching screenshot
        String[] cols = {"Student ID", "Student Name", "Assignment", "Quiz", "Mid Exam", "Final Exam", "Total Marks", "Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return (c >= 2 && c <= 5);
            }
        };

        marksTable = new JTable(tableModel);
        marksTable.setRowHeight(38);
        marksTable.setFont(LecturerTheme.FONT_PLAIN_13);
        marksTable.setGridColor(BORDER);
        marksTable.setSelectionBackground(LecturerTheme.LIGHT_BLUE);
        marksTable.setSelectionForeground(NAVY);
        marksTable.setShowGrid(true);

        JTableHeader th = marksTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        marksTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        marksTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        marksTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        marksTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        marksTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        marksTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        marksTable.getColumnModel().getColumn(6).setPreferredWidth(105);
        marksTable.getColumnModel().getColumn(7).setPreferredWidth(85);

        marksTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 12, 0, 0));
        marksTable.getColumnModel().getColumn(1).setCellRenderer(leftIndentRenderer);

        marksTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer());
        marksTable.getColumnModel().getColumn(6).setCellRenderer(totalMarksRenderer());
        marksTable.getColumnModel().getColumn(7).setCellRenderer(gradeRenderer());

        marksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedRecordDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(marksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // 5. Bottom Toolbar: Left: Helper text | Right: 6 Action Buttons
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(WHITE);
        bottomBar.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel helperText = new JLabel("Double-click or click 'Edit Marks' to update student scores.");
        helperText.setFont(new Font("Arial", Font.PLAIN, 12));
        helperText.setForeground(MUTED);

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomButtons.setBackground(WHITE);

        // [ View Marks ] (Royal Blue)
        JButton viewMarksBtn = createStyledButton("View Marks", PRIMARY_BLUE, Color.WHITE, 95);
        viewMarksBtn.addActionListener(e -> viewSelectedRecordDetails());

        // [ Enter Marks ] (Royal Blue)
        JButton enterMarksBtn = createStyledButton("Enter Marks", PRIMARY_BLUE, Color.WHITE, 100);
        enterMarksBtn.addActionListener(e -> openEnterMarksDialog());

        // [ Edit Marks ] (White) - Opens targeted edit form for selected student
        JButton editMarksBtn = createOutlineButton("Edit Marks", 90);
        editMarksBtn.addActionListener(e -> {
            int selectedRow = marksTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a student record from the table to edit.",
                        "Edit Marks",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            int modelRow = marksTable.convertRowIndexToModel(selectedRow);
            String sid = String.valueOf(tableModel.getValueAt(modelRow, 0));
            StudentMarksRecord editRecord = findRecordByStudentId(sid);
            if (editRecord != null) {
                openEditMarksDialog(editRecord);
            }
        });

        // [ Save ] (White) - Persists any table changes to database
        JButton saveBtn = createOutlineButton("Save", 70);
        saveBtn.addActionListener(e -> handleSaveMarks());

        // [ Cancel ] (White)
        JButton cancelBtn = createOutlineButton("Cancel", 75);
        cancelBtn.addActionListener(e -> {
            if (marksTable.isEditing()) {
                marksTable.getCellEditor().cancelCellEditing();
            }
            applyFilters();
        });

        // [ Refresh ] (White)
        JButton refreshBtn = createOutlineButton("Refresh", 80);
        refreshBtn.addActionListener(e -> {
            if (marksTable.isEditing()) {
                marksTable.getCellEditor().cancelCellEditing();
            }
            searchField.setText("");
            if (courseFilterCombo.getItemCount() > 0) courseFilterCombo.setSelectedIndex(0);
            semesterFilterCombo.setSelectedIndex(0);
            loadDatabaseData();
        });

        bottomButtons.add(viewMarksBtn);
        bottomButtons.add(enterMarksBtn);
        bottomButtons.add(editMarksBtn);
        bottomButtons.add(saveBtn);
        bottomButtons.add(cancelBtn);
        bottomButtons.add(refreshBtn);

        bottomBar.add(helperText, BorderLayout.WEST);
        bottomBar.add(bottomButtons, BorderLayout.EAST);
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

        JLabel captionLbl = new JLabel(caption);
        captionLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        captionLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(captionLbl, BorderLayout.SOUTH);
        return card;
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int width) {
        JButton btn = new JButton(text);
        btn.setFont(LecturerTheme.FONT_BOLD_12);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 32));
        return btn;
    }

    private JButton createOutlineButton(String text, int width) {
        JButton btn = new JButton(text);
        btn.setFont(LecturerTheme.FONT_BOLD_12);
        btn.setBackground(WHITE);
        btn.setForeground(TEXT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 32));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(4, 12, 4, 12)
        ));
        return btn;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    private DefaultTableCellRenderer totalMarksRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
            ) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Arial", Font.BOLD, 13));
                l.setForeground(TEXT);
                return l;
            }
        };
    }

    private DefaultTableCellRenderer gradeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
            ) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String g = value != null ? value.toString().trim() : "";
                if (!isSelected) {
                    if ("F".equalsIgnoreCase(g)) {
                        l.setBackground(new Color(254, 242, 242)); // soft red/pink #FEF2F2
                        l.setForeground(new Color(220, 38, 38));   // red #DC2626
                        l.setFont(new Font("Arial", Font.BOLD, 12));
                    } else if (!g.isEmpty() && !"-".equals(g)) {
                        l.setBackground(new Color(236, 253, 245)); // soft mint green #ECFDF5
                        l.setForeground(new Color(5, 150, 105));   // dark green #059669
                        l.setFont(new Font("Arial", Font.BOLD, 12));
                    } else {
                        l.setBackground(WHITE);
                        l.setForeground(MUTED);
                        l.setFont(new Font("Arial", Font.PLAIN, 12));
                    }
                }
                return l;
            }
        };
    }

    /**
     * Loads marks via MarksGradesDAO service.
     */
    public void loadDatabaseData() {
        loadedMarks.clear();
        courseOfferings.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        try {
            List<CourseOfferingItem> offerings = MarksGradesDAO.loadCourseOfferings(lecturerId);
            courseOfferings.addAll(offerings);

            courseFilterCombo.removeAllItems();
            courseFilterCombo.addItem("All Courses");
            for (CourseOfferingItem item : courseOfferings) {
                courseFilterCombo.addItem(item.courseCode);
            }

            List<StudentMarksRecord> marks = MarksGradesDAO.loadMarks(lecturerId, null);
            loadedMarks.addAll(marks);

            if (subtitleLabel != null) {
                String idLabel = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";
                subtitleLabel.setText("Displaying assessment marks for assigned courses (Lecturer: " + idLabel + ").");
            }
        } catch (SQLException e) {
            System.err.println("[MarksGradesUI] Error loading marks: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error: " + e.getMessage());
            }
        }

        applyFilters();
    }

    public void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        String selCourse = courseFilterCombo != null ? (String) courseFilterCombo.getSelectedItem() : null;
        String selSem = semesterFilterCombo != null ? (String) semesterFilterCombo.getSelectedItem() : null;
        String search = searchField != null ? searchField.getText().trim() : "";

        List<StudentMarksRecord> filtered = MarksGradesDAO.filterMarks(loadedMarks, selCourse, selSem, null, search);

        for (StudentMarksRecord r : filtered) {
            tableModel.addRow(r.toTableRow());
        }

        if (recordCountLabel != null) {
            recordCountLabel.setText(filtered.size() + " RECORDS");
        }

        MarksSummary summary = MarksGradesDAO.calculateSummary(filtered);
        if (totalStudentsValue != null) totalStudentsValue.setText(String.valueOf(summary.totalStudents));
        if (averageMarksValue != null) averageMarksValue.setText(summary.classAverageFormatted);
        if (highestMarksValue != null) highestMarksValue.setText(summary.highestMarksFormatted);
        if (lowestMarksValue != null) lowestMarksValue.setText(summary.lowestMarksFormatted);
    }

    private StudentMarksRecord findRecordByStudentId(String studentId) {
        if (studentId == null) return null;
        for (StudentMarksRecord r : loadedMarks) {
            if (r.studentId.equalsIgnoreCase(studentId.trim())) {
                return r;
            }
        }
        return null;
    }

    private Double parseDoubleOrNull(Object val) {
        if (val == null) return null;
        String s = val.toString().trim();
        if (s.isEmpty() || "-".equals(s)) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Opens interactive modal dialog to edit marks for a specific selected student.
     */
    private void openEditMarksDialog(StudentMarksRecord editRecord) {
        JDialog dialog = new JDialog(this, "Edit Student Marks - " + editRecord.studentId, true);
        dialog.setSize(520, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(16, 20, 10, 20));

        // Student details summary card
        JPanel infoCard = new JPanel(new GridLayout(3, 2, 8, 6));
        infoCard.setBackground(new Color(248, 250, 252));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblId = new JLabel("Student ID:");
        lblId.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel valId = new JLabel(editRecord.studentId);
        valId.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel lblName = new JLabel("Student Name:");
        lblName.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel valName = new JLabel(editRecord.studentName);
        valName.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel lblCourse = new JLabel("Course Code:");
        lblCourse.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel valCourse = new JLabel(editRecord.courseCode);
        valCourse.setFont(new Font("Arial", Font.PLAIN, 12));

        infoCard.add(lblId); infoCard.add(valId);
        infoCard.add(lblName); infoCard.add(valName);
        infoCard.add(lblCourse); infoCard.add(valCourse);

        contentPanel.add(infoCard);
        contentPanel.add(Box.createVerticalStrut(14));

        // Assessments fields
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        List<AssessmentItem> assessments = new ArrayList<>();
        Map<String, Double> existingMarks = new LinkedHashMap<>();
        try {
            assessments = MarksGradesDAO.loadAssessmentsForOffering(editRecord.offeringId);
            existingMarks = MarksGradesDAO.loadStudentAssessmentMarks(editRecord.offeringId, editRecord.studentId);
        } catch (SQLException ex) {
            System.err.println("Error loading assessments: " + ex.getMessage());
        }

        final Map<AssessmentItem, JTextField> fieldMap = new LinkedHashMap<>();
        final List<AssessmentItem> effectiveAssessments = new ArrayList<>();

        if (!assessments.isEmpty()) {
            effectiveAssessments.addAll(assessments);
            for (AssessmentItem a : assessments) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

                JLabel l = new JLabel(a.name + " (Max " + (int) a.maxMarks + ", " + (int) a.weight + "%):");
                l.setFont(new Font("Arial", Font.PLAIN, 12));

                JTextField tf = new JTextField(8);
                tf.setPreferredSize(new Dimension(110, 28));
                tf.setFont(new Font("Arial", Font.PLAIN, 13));

                Double currentVal = existingMarks.get(a.id);
                if (currentVal == null) {
                    String type = a.type != null ? a.type.toUpperCase(Locale.ROOT) : "";
                    String name = a.name != null ? a.name.toUpperCase(Locale.ROOT) : "";
                    if ((type.contains("ASSIGNMENT") || name.contains("ASSIGNMENT")) && editRecord.assignmentMark != null) currentVal = editRecord.assignmentMark;
                    else if ((type.contains("QUIZ") || name.contains("QUIZ")) && editRecord.quizMark != null) currentVal = editRecord.quizMark;
                    else if ((type.contains("MID") || name.contains("MID")) && editRecord.midExamMark != null) currentVal = editRecord.midExamMark;
                    else if ((type.contains("FINAL") || type.contains("EXAM") || name.contains("FINAL") || name.contains("EXAM")) && editRecord.finalExamMark != null) currentVal = editRecord.finalExamMark;
                }

                if (currentVal != null) {
                    tf.setText(String.format(Locale.US, "%.1f", currentVal));
                }

                fieldMap.put(a, tf);
                row.add(l, BorderLayout.CENTER);
                row.add(tf, BorderLayout.EAST);
                fieldsPanel.add(row);
                fieldsPanel.add(Box.createVerticalStrut(8));
            }
        } else {
            // Default fallback standard 4 assessments
            AssessmentItem aAss = new AssessmentItem("ASS", "Assignment", "ASSIGNMENT", 20.0, 100.0);
            AssessmentItem aQz = new AssessmentItem("QZ", "Quiz", "QUIZ", 10.0, 100.0);
            AssessmentItem aMid = new AssessmentItem("MID", "Mid Exam", "MID_EXAM", 25.0, 100.0);
            AssessmentItem aFin = new AssessmentItem("FIN", "Final Exam", "FINAL_EXAM", 45.0, 100.0);

            effectiveAssessments.add(aAss);
            effectiveAssessments.add(aQz);
            effectiveAssessments.add(aMid);
            effectiveAssessments.add(aFin);

            JTextField tfAss = new JTextField(editRecord.assignmentMark != null ? String.format(Locale.US, "%.1f", editRecord.assignmentMark) : "", 8);
            JTextField tfQz = new JTextField(editRecord.quizMark != null ? String.format(Locale.US, "%.1f", editRecord.quizMark) : "", 8);
            JTextField tfMid = new JTextField(editRecord.midExamMark != null ? String.format(Locale.US, "%.1f", editRecord.midExamMark) : "", 8);
            JTextField tfFin = new JTextField(editRecord.finalExamMark != null ? String.format(Locale.US, "%.1f", editRecord.finalExamMark) : "", 8);

            fieldMap.put(aAss, tfAss);
            fieldMap.put(aQz, tfQz);
            fieldMap.put(aMid, tfMid);
            fieldMap.put(aFin, tfFin);

            for (Map.Entry<AssessmentItem, JTextField> entry : fieldMap.entrySet()) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                JLabel l = new JLabel(entry.getKey().name + " (Max 100, " + (int) entry.getKey().weight + "%):");
                l.setFont(new Font("Arial", Font.PLAIN, 12));
                entry.getValue().setPreferredSize(new Dimension(110, 28));
                entry.getValue().setFont(new Font("Arial", Font.PLAIN, 13));
                row.add(l, BorderLayout.CENTER);
                row.add(entry.getValue(), BorderLayout.EAST);
                fieldsPanel.add(row);
                fieldsPanel.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane fieldsScroll = new JScrollPane(fieldsPanel);
        fieldsScroll.setBorder(BorderFactory.createEmptyBorder());
        fieldsScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(fieldsScroll);

        // Bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton saveBtn = createStyledButton("Save Changes", PRIMARY_BLUE, Color.WHITE, 120);
        JButton cancelBtn = createOutlineButton("Cancel", 80);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            Map<AssessmentItem, Double> parsedMarks = new LinkedHashMap<>();
            for (Map.Entry<AssessmentItem, JTextField> entry : fieldMap.entrySet()) {
                AssessmentItem a = entry.getKey();
                String txt = entry.getValue().getText().trim();
                if (!txt.isEmpty() && !"-".equals(txt)) {
                    try {
                        double v = Double.parseDouble(txt);
                        if (v < 0 || v > a.maxMarks) {
                            JOptionPane.showMessageDialog(dialog, "Marks for '" + a.name + "' must be between 0 and " + (int) a.maxMarks + ".", "Validation Error", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        parsedMarks.put(a, v);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric mark for '" + a.name + "'.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            try {
                MarksGradesDAO.saveMarksAndResults(editRecord.offeringId, editRecord.studentId, parsedMarks, effectiveAssessments, lecturerId);
                JOptionPane.showMessageDialog(dialog, "Marks saved and recalculated successfully in MySQL database for " + editRecord.studentId + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDatabaseData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to save marks: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleSaveMarks() {
        if (marksTable.isEditing()) {
            marksTable.getCellEditor().stopCellEditing();
        }

        try {
            int updatedCount = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String sid = String.valueOf(tableModel.getValueAt(i, 0));
                StudentMarksRecord rec = findRecordByStudentId(sid);
                if (rec == null) continue;

                Double assVal = parseDoubleOrNull(tableModel.getValueAt(i, 2));
                Double qzVal = parseDoubleOrNull(tableModel.getValueAt(i, 3));
                Double midVal = parseDoubleOrNull(tableModel.getValueAt(i, 4));
                Double feVal = parseDoubleOrNull(tableModel.getValueAt(i, 5));

                MarksGradesDAO.updateStudentRowMarks(rec.offeringId, sid, assVal, qzVal, midVal, feVal, lecturerId);
                updatedCount++;
            }

            JOptionPane.showMessageDialog(this, "Marks saved and results recalculated successfully for " + updatedCount + " student(s) in MySQL database.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadDatabaseData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save marks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedRecordDetails() {
        int row = marksTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student record to view details.", "View Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = marksTable.convertRowIndexToModel(row);
        String sid = String.valueOf(tableModel.getValueAt(modelRow, 0));
        StudentMarksRecord rec = findRecordByStudentId(sid);
        if (rec == null) return;

        JOptionPane.showMessageDialog(this,
                "Student ID: " + rec.studentId + "\n" +
                "Name: " + rec.studentName + "\n" +
                "Course: " + rec.courseCode + "\n\n" +
                "Assignment Mark: " + (rec.assignmentMark != null ? rec.assignmentMark : "Not entered") + "\n" +
                "Quiz Mark: " + (rec.quizMark != null ? rec.quizMark : "Not entered") + "\n" +
                "Mid Exam Mark: " + (rec.midExamMark != null ? rec.midExamMark : "Not entered") + "\n" +
                "Final Exam Mark: " + (rec.finalExamMark != null ? rec.finalExamMark : "Not entered") + "\n\n" +
                "CA Mark: " + (rec.caMark != null ? rec.caMark : "-") + "\n" +
                "Total Final Mark: " + (rec.totalMark != null ? rec.totalMark : "-") + "\n" +
                "Grade: " + rec.grade + "\n" +
                "Grade Point (GP): " + (rec.gradePoint != null ? rec.gradePoint : "-") + "\n" +
                "Attendance: " + (rec.attendancePercentage != null ? String.format(Locale.US, "%.1f%%", rec.attendancePercentage) : "N/A") + "\n" +
                "CA Eligible: " + (rec.caEligible != null ? (rec.caEligible == 1 ? "Yes" : "No") : "Pending") + "\n" +
                "Final Eligible: " + (rec.finalEligible != null ? (rec.finalEligible == 1 ? "Yes" : "No") : "Pending") + "\n\n" +
                "Semester GPA (SGPA): " + (rec.sgpa != null ? rec.sgpa : "-") + "\n" +
                "Cumulative GPA (CGPA): " + (rec.cgpa != null ? rec.cgpa : "-"),
                "Academic Evaluation Details - " + rec.studentId,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openEnterMarksDialog() {
        if (courseOfferings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assigned course offerings available.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Enter / Edit Student Marks", true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        topPanel.setBorder(new EmptyBorder(12, 16, 6, 16));

        JComboBox<CourseOfferingItem> offCombo = new JComboBox<>(courseOfferings.toArray(new CourseOfferingItem[0]));
        JComboBox<String> studentCombo = new JComboBox<>();

        topPanel.add(new JLabel("Select Course Offering:"));
        topPanel.add(offCombo);
        topPanel.add(new JLabel("Select Enrolled Student:"));
        topPanel.add(studentCombo);

        JPanel marksFieldsPanel = new JPanel();
        marksFieldsPanel.setLayout(new BoxLayout(marksFieldsPanel, BoxLayout.Y_AXIS));
        marksFieldsPanel.setBorder(new EmptyBorder(8, 16, 8, 16));
        JScrollPane scrollFields = new JScrollPane(marksFieldsPanel);

        final Map<AssessmentItem, JTextField> fieldMap = new LinkedHashMap<>();
        final List<AssessmentItem>[] currentAssessments = new List[]{new ArrayList<>()};

        Runnable loadStudentAndAssessments = () -> {
            CourseOfferingItem selOff = (CourseOfferingItem) offCombo.getSelectedItem();
            if (selOff == null) return;
            marksFieldsPanel.removeAll();
            fieldMap.clear();

            try {
                // Populate students
                studentCombo.removeAllItems();
                Map<String, String> students = MarksGradesDAO.loadEnrolledStudents(selOff.offeringId);
                for (Map.Entry<String, String> entry : students.entrySet()) {
                    studentCombo.addItem(entry.getKey() + " - " + entry.getValue());
                }

                // Populate assessments
                currentAssessments[0] = MarksGradesDAO.loadAssessmentsForOffering(selOff.offeringId);
                if (currentAssessments[0].isEmpty()) {
                    marksFieldsPanel.add(new JLabel("No assessments found for this offering."));
                } else {
                    for (AssessmentItem a : currentAssessments[0]) {
                        JPanel row = new JPanel(new BorderLayout(8, 0));
                        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                        JLabel l = new JLabel(a.name + " (" + a.type + ", " + String.format(Locale.US, "%.0f%% weight, max %.0f", a.weight, a.maxMarks) + "):");
                        JTextField tf = new JTextField();
                        tf.setPreferredSize(new Dimension(100, 28));
                        fieldMap.put(a, tf);
                        row.add(l, BorderLayout.CENTER);
                        row.add(tf, BorderLayout.EAST);
                        marksFieldsPanel.add(row);
                        marksFieldsPanel.add(Box.createVerticalStrut(6));
                    }
                }
                marksFieldsPanel.revalidate();
                marksFieldsPanel.repaint();
            } catch (SQLException ex) {
                System.err.println("Error loading assessments/students: " + ex.getMessage());
            }
        };

        offCombo.addActionListener(e -> loadStudentAndAssessments.run());
        loadStudentAndAssessments.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = createStyledButton("Save Marks", PRIMARY_BLUE, Color.WHITE, 120);
        JButton cancelBtn = createOutlineButton("Cancel", 80);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            CourseOfferingItem selOff = (CourseOfferingItem) offCombo.getSelectedItem();
            String selStudent = (String) studentCombo.getSelectedItem();
            if (selOff == null || selStudent == null) {
                JOptionPane.showMessageDialog(dialog, "Please select both offering and student.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String studentId = selStudent.substring(0, selStudent.indexOf(" - ")).trim();

            Map<AssessmentItem, Double> enteredMarks = new LinkedHashMap<>();
            for (Map.Entry<AssessmentItem, JTextField> entry : fieldMap.entrySet()) {
                String valStr = entry.getValue().getText().trim();
                if (!valStr.isEmpty()) {
                    try {
                        double val = Double.parseDouble(valStr);
                        enteredMarks.put(entry.getKey(), val);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(dialog, "Invalid number for: " + entry.getKey().name, "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            try {
                MarksGradesDAO.saveMarksAndResults(selOff.offeringId, studentId, enteredMarks, currentAssessments[0], lecturerId);
                JOptionPane.showMessageDialog(dialog, "Marks and results saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDatabaseData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to save marks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scrollFields, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
            new MarksGradesUI(testId).setVisible(true);
        });
    }
}
