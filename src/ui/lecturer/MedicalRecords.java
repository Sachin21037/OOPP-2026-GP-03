package ui;

import database.DBConnection;

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
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lecturer Medical Records Management screen.
 * Allows authenticated lecturers to view verified medical records for students
 * enrolled in their assigned course offerings.
 */
public class MedicalRecords extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color RED = LecturerTheme.DANGER_RED;
    private static final Color AMBER = new Color(217, 119, 6);

    private JTable medicalTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> courseFilter;
    private JComboBox<String> statusFilter;
    private JTextField searchField;

    private JLabel totalRecordsValue;
    private JLabel approvedValue;
    private JLabel pendingValue;
    private JLabel courseCountValue;
    private JLabel subtitleLabel;
    private JLabel tableHintLabel;
    private JLabel emptyNoticeLabel;

    // Active lecturer ID
    private String lecturerId;

    // Loaded and displayed records
    private final List<MedicalItem> loadedRecords = new ArrayList<>();
    private final List<MedicalItem> displayedRecords = new ArrayList<>();

    /**
     * Inner helper model representing a student medical record.
     */
    public static class MedicalItem {
        public final String medicalId;
        public final String studentId;
        public final String indexNo;
        public final String studentName;
        public final String courseCode;
        public final String courseName;
        public final String medicalDate;
        public final String reason;
        public final String documentPath;
        public final String approvalStatus;
        public final String approvedBy;
        public final String remarks;

        public MedicalItem(String medicalId, String studentId, String indexNo,
                           String studentName, String courseCode, String courseName,
                           String medicalDate, String reason, String documentPath,
                           String approvalStatus, String approvedBy, String remarks) {
            this.medicalId = medicalId != null ? medicalId : "";
            this.studentId = studentId != null ? studentId : "";
            this.indexNo = indexNo != null ? indexNo : "";
            this.studentName = studentName != null ? studentName : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.medicalDate = medicalDate != null ? medicalDate : "";
            this.reason = reason != null ? reason : "";
            this.documentPath = documentPath != null ? documentPath : "";
            this.approvalStatus = approvalStatus != null ? approvalStatus.trim().toUpperCase(Locale.ROOT) : "PENDING";
            this.approvedBy = approvedBy != null ? approvedBy : "";
            this.remarks = remarks != null ? remarks : "";
        }

        public Object[] toTableRow() {
            String docStatus = (documentPath != null && !documentPath.trim().isEmpty()) ? "Available" : "None";
            return new Object[]{medicalId, studentId, studentName, courseCode, medicalDate, approvalStatus, docStatus};
        }
    }

    /**
     * Default constructor: resolves active lecturer ID dynamically.
     */
    public MedicalRecords() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the active lecturer ID (e.g. "LEC001")
     */
    public MedicalRecords(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboard.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Medical Records");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private static String resolveActiveLecturerId() {
        String active = LecturerDashboard.getActiveLecturerId();
        if (active != null && !active.trim().isEmpty()) {
            return active.trim();
        }
        String sysProp = System.getProperty("fams.lecturer.id");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return null;
    }

    // ================= HEADER & SIDEBAR =================

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Medical Records", lecturerId, this);
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(27, 30, 28, 30));

        // Page Heading
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(BACKGROUND);

        JPanel headingText = new JPanel();
        headingText.setBackground(BACKGROUND);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));

        JLabel pageTitle = new JLabel("Medical Records");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("View student medical submissions for your assigned courses.");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setBorder(new EmptyBorder(5, 1, 0, 0));

        headingText.add(pageTitle);
        headingText.add(subtitleLabel);

        JLabel portalLabel = new JLabel("LECTURER PORTAL");
        portalLabel.setFont(new Font("Arial", Font.BOLD, 10));
        portalLabel.setForeground(NAVY);
        portalLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));

        heading.add(headingText, BorderLayout.WEST);
        heading.add(portalLabel, BorderLayout.EAST);
        content.add(heading, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(BACKGROUND);

        // Summary Cards
        JPanel summary = new JPanel(new GridLayout(1, 4, 16, 0));
        summary.setBackground(BACKGROUND);

        totalRecordsValue = new JLabel("-");
        approvedValue = new JLabel("-");
        pendingValue = new JLabel("-");
        courseCountValue = new JLabel("-");

        summary.add(createSummaryCard("TOTAL SUBMISSIONS", totalRecordsValue, NAVY, "Across your courses"));
        summary.add(createSummaryCard("APPROVED", approvedValue, TEAL, "Verified by Medical Officer"));
        summary.add(createSummaryCard("PENDING", pendingValue, AMBER, "Awaiting verification"));
        summary.add(createSummaryCard("COURSES", courseCountValue, PURPLE, "With medical submissions"));

        body.add(summary, BorderLayout.NORTH);

        // Medical Records Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(0, 16));
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 16, 18)
        ));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setBackground(WHITE);

        JLabel tableTitle = new JLabel("Student Medical Submissions");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 18));
        tableTitle.setForeground(TEXT);

        tableHintLabel = new JLabel("Select a medical record to view details");
        tableHintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        tableHintLabel.setForeground(MUTED);

        tableHeading.add(tableTitle, BorderLayout.WEST);
        tableHeading.add(tableHintLabel, BorderLayout.EAST);
        tablePanel.add(tableHeading, BorderLayout.NORTH);

        // Table Model
        String[] columns = {
                "Medical ID",
                "Student ID",
                "Student Name",
                "Course",
                "Medical Date",
                "Approval Status",
                "Document"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        medicalTable = new JTable(tableModel);
        medicalTable.setRowHeight(39);
        medicalTable.setFont(new Font("Arial", Font.PLAIN, 12));
        medicalTable.setForeground(TEXT);
        medicalTable.setBackground(WHITE);
        medicalTable.setGridColor(BORDER);
        medicalTable.setShowVerticalLines(false);
        medicalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medicalTable.setSelectionBackground(new Color(226, 238, 249));
        medicalTable.setSelectionForeground(TEXT);
        medicalTable.setFillsViewportHeight(true);
        medicalTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = medicalTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(237, 242, 247));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 39));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        medicalTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());
        medicalTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer());
        medicalTable.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        medicalTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());
        medicalTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        medicalTable.getColumnModel().getColumn(5).setCellRenderer(statusRenderer());
        medicalTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer());

        medicalTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        medicalTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        medicalTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        medicalTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        medicalTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        medicalTable.getColumnModel().getColumn(5).setPreferredWidth(125);
        medicalTable.getColumnModel().getColumn(6).setPreferredWidth(95);

        // Double-click row listener
        medicalTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && medicalTable.getSelectedRow() >= 0) {
                    viewSelectedMedicalDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(medicalTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        // Empty state container
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(WHITE);

        emptyNoticeLabel = new JLabel("No medical records found for your assigned courses.", SwingConstants.CENTER);
        emptyNoticeLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        emptyNoticeLabel.setForeground(MUTED);
        emptyNoticeLabel.setBorder(new EmptyBorder(14, 10, 14, 10));
        emptyNoticeLabel.setVisible(false);

        centerContainer.add(emptyNoticeLabel, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(centerContainer, BorderLayout.CENTER);

        // Filter / Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 10));
        toolbar.setBackground(WHITE);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setBackground(WHITE);

        searchField = new JTextField(14);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Search student ID, index no, name, or medical ID");
        searchField.setPreferredSize(new Dimension(175, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));

        courseFilter = new JComboBox<>(new String[]{"All Courses"});
        courseFilter.setFont(new Font("Arial", Font.PLAIN, 12));
        courseFilter.setPreferredSize(new Dimension(140, 34));
        courseFilter.setBackground(WHITE);

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "APPROVED", "PENDING", "NOT_APPROVED"});
        statusFilter.setFont(new Font("Arial", Font.PLAIN, 12));
        statusFilter.setPreferredSize(new Dimension(135, 34));
        statusFilter.setBackground(WHITE);

        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(new JLabel("Course:"));
        filters.add(courseFilter);
        filters.add(new JLabel("Status:"));
        filters.add(statusFilter);

        JButton viewButton = createActionButton("View Details", true);
        JButton openDocButton = createActionButton("Open Document", false);
        JButton refreshButton = createActionButton("Refresh", false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(WHITE);
        actions.add(openDocButton);
        actions.add(refreshButton);
        actions.add(viewButton);

        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);
        tablePanel.add(toolbar, BorderLayout.SOUTH);

        // Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterRecords(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterRecords(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterRecords(); }
        });

        courseFilter.addActionListener(e -> filterRecords());
        statusFilter.addActionListener(e -> filterRecords());

        viewButton.addActionListener(e -> viewSelectedMedicalDetails());
        openDocButton.addActionListener(e -> openSelectedDocument());
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            if (courseFilter.getItemCount() > 0) courseFilter.setSelectedIndex(0);
            if (statusFilter.getItemCount() > 0) statusFilter.setSelectedIndex(0);
            loadMedicalRecords();
            JOptionPane.showMessageDialog(this, "Medical records refreshed from database.", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });

        body.add(tablePanel, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);

        loadMedicalRecords();
        return content;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryCard(String title, JLabel value, Color accent, String description) {
        JPanel card = new JPanel(new BorderLayout(0, 9)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };

        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 17, 14, 17)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 29));
        value.setForeground(TEXT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        descLabel.setForeground(MUTED);

        JPanel lower = new JPanel();
        lower.setBackground(WHITE);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        lower.add(value);
        lower.add(descLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);
        return card;
    }

    // ================= TABLE RENDERERS =================

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, selected, focused, row, column
                );
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 11));

                if (!selected) {
                    String status = String.valueOf(value).toUpperCase(Locale.ROOT);
                    if ("APPROVED".equals(status)) {
                        label.setForeground(new Color(29, 130, 103));
                        label.setBackground(new Color(232, 247, 240));
                    } else if ("PENDING".equals(status)) {
                        label.setForeground(AMBER);
                        label.setBackground(new Color(254, 243, 199));
                    } else if ("NOT_APPROVED".equals(status)) {
                        label.setForeground(RED);
                        label.setBackground(new Color(253, 235, 235));
                    } else {
                        label.setForeground(TEXT);
                        label.setBackground(WHITE);
                    }
                } else {
                    label.setForeground(TEXT);
                }
                return label;
            }
        };
    }

    private JButton createActionButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(primary ? WHITE : TEXT);
        button.setBackground(primary ? LecturerTheme.SECONDARY_BLUE : WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(primary ? 115 : 120, 34));
        button.setBorder(primary
                ? BorderFactory.createEmptyBorder(7, 12, 7, 12)
                : BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 11, 6, 11)
        ));
        return button;
    }

    // ================= DATABASE QUERY =================

    private void loadMedicalRecords() {
        loadedRecords.clear();
        displayedRecords.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            if (subtitleLabel != null) {
                subtitleLabel.setText("No active lecturer session found. Please log in.");
            }
            if (tableHintLabel != null) {
                tableHintLabel.setText("NO LECTURER ID");
            }
            updateSummary();
            return;
        }

        String sql =
                "SELECT m.medical_id, m.student_id, ug.index_no, u.first_name, u.last_name, " +
                "       c.course_code, c.course_name, m.medical_date, m.reason, m.document_path, " +
                "       m.approval_status, m.approved_by, m.remarks " +
                "FROM medicals m " +
                "JOIN course_offerings co ON m.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                "JOIN undergraduates ug ON m.student_id = ug.student_id " +
                "JOIN users u ON ug.student_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY m.medical_date DESC, m.medical_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String medicalId = rs.getString("medical_id");
                    String studentId = rs.getString("student_id");
                    String indexNo = rs.getString("index_no");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    java.sql.Date d = rs.getDate("medical_date");
                    String medicalDate = d != null ? d.toString() : "";
                    String reason = rs.getString("reason");
                    String docPath = rs.getString("document_path");
                    String approvalStatus = rs.getString("approval_status");
                    String approvedBy = rs.getString("approved_by");
                    String remarks = rs.getString("remarks");

                    loadedRecords.add(new MedicalItem(
                            medicalId, studentId, indexNo, fullName,
                            courseCode, courseName, medicalDate, reason,
                            docPath, approvalStatus, approvedBy, remarks
                    ));
                }
            }

            populateCourseDropdown();

            if (loadedRecords.isEmpty()) {
                if (subtitleLabel != null) {
                    subtitleLabel.setText("No medical records submitted for courses assigned to lecturer " + lecturerId + ".");
                }
                if (tableHintLabel != null) {
                    tableHintLabel.setText("0 SUBMISSIONS");
                }
                if (emptyNoticeLabel != null) {
                    emptyNoticeLabel.setText("No medical records found for your assigned courses.");
                    emptyNoticeLabel.setVisible(true);
                }
            } else {
                if (subtitleLabel != null) {
                    subtitleLabel.setText("Viewing " + loadedRecords.size() + " medical record(s) for your courses (" + lecturerId + ").");
                }
                if (tableHintLabel != null) {
                    tableHintLabel.setText(loadedRecords.size() + " SUBMISSIONS RECORDED");
                }
                if (emptyNoticeLabel != null) {
                    emptyNoticeLabel.setVisible(false);
                }
            }

            for (MedicalItem item : loadedRecords) {
                displayedRecords.add(item);
                tableModel.addRow(item.toTableRow());
            }

        } catch (SQLException e) {
            System.err.println("[MedicalRecords] Database error: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error loading medical records: " + e.getMessage());
            }
            if (tableHintLabel != null) {
                tableHintLabel.setText("DB ERROR");
            }
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setText("Database connection error: " + e.getMessage());
                emptyNoticeLabel.setVisible(true);
            }
        }

        updateSummary();
    }

    private void populateCourseDropdown() {
        if (courseFilter == null) return;
        String prev = (String) courseFilter.getSelectedItem();

        courseFilter.removeAllItems();
        courseFilter.addItem("All Courses");

        Set<String> courses = new TreeSet<>();
        for (MedicalItem item : loadedRecords) {
            if (!item.courseCode.isEmpty()) {
                courses.add(item.courseCode);
            }
        }
        for (String c : courses) {
            courseFilter.addItem(c);
        }

        if (prev != null && courses.contains(prev)) {
            courseFilter.setSelectedItem(prev);
        } else {
            courseFilter.setSelectedIndex(0);
        }
    }

    // ================= SEARCH & FILTER =================

    private void filterRecords() {
        if (tableModel == null) return;

        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedCourse = (String) courseFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        tableModel.setRowCount(0);
        displayedRecords.clear();

        for (MedicalItem item : loadedRecords) {
            boolean matchesSearch = query.isEmpty()
                    || item.studentId.toLowerCase(Locale.ROOT).contains(query)
                    || item.indexNo.toLowerCase(Locale.ROOT).contains(query)
                    || item.studentName.toLowerCase(Locale.ROOT).contains(query)
                    || item.medicalId.toLowerCase(Locale.ROOT).contains(query)
                    || item.reason.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesCourse = selectedCourse == null
                    || "All Courses".equals(selectedCourse)
                    || item.courseCode.equalsIgnoreCase(selectedCourse);

            boolean matchesStatus = selectedStatus == null
                    || "All Statuses".equals(selectedStatus)
                    || item.approvalStatus.equalsIgnoreCase(selectedStatus);

            if (matchesSearch && matchesCourse && matchesStatus) {
                displayedRecords.add(item);
                tableModel.addRow(item.toTableRow());
            }
        }

        if (emptyNoticeLabel != null) {
            if (loadedRecords.isEmpty()) {
                emptyNoticeLabel.setText("No medical records found for your assigned courses.");
                emptyNoticeLabel.setVisible(true);
            } else if (displayedRecords.isEmpty()) {
                emptyNoticeLabel.setText("No medical records match your filter criteria.");
                emptyNoticeLabel.setVisible(true);
            } else {
                emptyNoticeLabel.setVisible(false);
            }
        }

        updateSummary();
    }

    private void updateSummary() {
        if (totalRecordsValue == null || approvedValue == null || pendingValue == null || courseCountValue == null) {
            return;
        }

        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            totalRecordsValue.setText("-");
            approvedValue.setText("-");
            pendingValue.setText("-");
            courseCountValue.setText("-");
            return;
        }

        int total = tableModel.getRowCount();
        int approved = 0;
        int pending = 0;
        Set<String> courses = new java.util.HashSet<>();

        for (int i = 0; i < total; i++) {
            Object statusObj = tableModel.getValueAt(i, 5);
            Object courseObj = tableModel.getValueAt(i, 3);

            if (statusObj != null) {
                String s = statusObj.toString().toUpperCase(Locale.ROOT);
                if ("APPROVED".equals(s)) approved++;
                else if ("PENDING".equals(s)) pending++;
            }
            if (courseObj != null) {
                courses.add(courseObj.toString());
            }
        }

        totalRecordsValue.setText(String.valueOf(total));
        approvedValue.setText(String.valueOf(approved));
        pendingValue.setText(String.valueOf(pending));
        courseCountValue.setText(String.valueOf(courses.size()));
    }

    // ================= ACTIONS =================

    private void viewSelectedMedicalDetails() {
        int selectedRow = medicalTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table.", "No Record Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = medicalTable.convertRowIndexToModel(selectedRow);
        MedicalItem item = (modelRow >= 0 && modelRow < displayedRecords.size())
                ? displayedRecords.get(modelRow)
                : null;

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Unable to load details for the selected record.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel details = new JPanel(new GridLayout(0, 1, 6, 8));
        details.setBorder(new EmptyBorder(12, 14, 12, 14));

        details.add(new JLabel("Medical Record ID: " + item.medicalId));
        details.add(new JLabel("Student ID: " + item.studentId + " (" + item.studentName + ")"));
        if (!item.indexNo.isEmpty()) {
            details.add(new JLabel("Index Number: " + item.indexNo));
        }
        details.add(new JLabel("Course Code: " + item.courseCode + (!item.courseName.isEmpty() ? " - " + item.courseName : "")));
        details.add(new JLabel("Medical Date: " + item.medicalDate));
        details.add(new JLabel("Reason: " + item.reason));
        details.add(new JLabel("Approval Status: " + item.approvalStatus));
        if (!item.approvedBy.isEmpty()) {
            details.add(new JLabel("Verified / Approved By: " + item.approvedBy));
        }
        if (!item.remarks.isEmpty()) {
            details.add(new JLabel("Remarks: " + item.remarks));
        }
        details.add(new JLabel("Document Path: " + (!item.documentPath.isEmpty() ? item.documentPath : "None attached")));

        JOptionPane.showMessageDialog(this, details, "Medical Record Details - " + item.medicalId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void openSelectedDocument() {
        int selectedRow = medicalTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table to view its document.", "No Record Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = medicalTable.convertRowIndexToModel(selectedRow);
        MedicalItem item = (modelRow >= 0 && modelRow < displayedRecords.size())
                ? displayedRecords.get(modelRow)
                : null;

        if (item == null || item.documentPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No document path is attached to this medical record.", "No Document", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File docFile = new File(item.documentPath);
        if (!docFile.exists()) {
            // Check relative to current working directory
            docFile = new File(System.getProperty("user.dir"), item.documentPath);
        }

        if (!docFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Document file not found at path:\n" + item.documentPath + "\n\nPlease ensure the document is present in the storage directory.",
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(docFile);
            } else {
                JOptionPane.showMessageDialog(this, "Desktop file opening is not supported on this environment.", "Action Not Supported", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening document: " + ex.getMessage(), "Open Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        String testId = args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboard.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> new MedicalRecords(testId).setVisible(true));
    }
}
