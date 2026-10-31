package ui.lecturer;

import dao.MedicalRecordsDAO;
import dao.MedicalRecordsDAO.MedicalItem;
import dao.MedicalRecordsDAO.MedicalSummary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * MedicalRecordsUI - Presentation layer for Student Medical Records.
 * Matches the official FAMS portal design and provides full DB-backed operations.
 */
public class MedicalRecordsUI extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color BUTTON_BLUE = new Color(29, 78, 216); // Royal Blue #1D4ED8
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color AMBER = new Color(245, 158, 11);
    private static final Color PURPLE = new Color(124, 58, 237);

    protected String lecturerId;

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable medicalTable;

    private JComboBox<String> courseFilter;
    private JComboBox<String> statusFilter;
    private JTextField searchField;

    private JLabel totalRecordsValue;
    private JLabel approvedValue;
    private JLabel pendingValue;
    private JLabel courseCountValue;

    private JLabel subtitleLabel;
    private JLabel tableCountLabel;

    private final List<MedicalItem> loadedRecords = new ArrayList<>();
    private final List<MedicalItem> displayedRecords = new ArrayList<>();

    private boolean isUpdatingDropdown = false;

    public MedicalRecordsUI() {
        this(resolveActiveLecturerId());
    }

    public MedicalRecordsUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : resolveActiveLecturerId();

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Medical Records");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadMedicalRecords();
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
        return "LEC001";
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Medical Records", lecturerId, this);
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Top Section: Title Bar + Summary Cards
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // 1. Page Title Bar: Left Title + Subtitle | Right: LECTURER PORTAL badge
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel mainTitle = new JLabel("Medical Records");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 26));
        mainTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("Viewing 0 medical record(s) for your courses (LEC001).");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);

        titles.add(mainTitle);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // Right Badge
        JPanel portalBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        portalBadge.setBackground(WHITE);
        portalBadge.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        JLabel portalLbl = new JLabel("LECTURER PORTAL");
        portalLbl.setFont(new Font("Arial", Font.BOLD, 11));
        portalLbl.setForeground(TEXT);
        portalBadge.add(portalLbl);

        titleBar.add(titles, BorderLayout.WEST);
        titleBar.add(portalBadge, BorderLayout.EAST);
        topSection.add(titleBar);
        topSection.add(Box.createVerticalStrut(16));

        // 2. Summary Cards (4 in horizontal row)
        topSection.add(createSummarySection());
        content.add(topSection, BorderLayout.NORTH);

        // 3. Main Table Container Card
        content.add(createTablePanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummarySection() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(0, 92));

        totalRecordsValue = new JLabel("0");
        approvedValue = new JLabel("0");
        pendingValue = new JLabel("0");
        courseCountValue = new JLabel("0");

        summaryPanel.add(createSummaryCard("TOTAL SUBMISSIONS", totalRecordsValue, NAVY, "Across your courses"));
        summaryPanel.add(createSummaryCard("APPROVED", approvedValue, TEAL, "Verified by Medical Officer"));
        summaryPanel.add(createSummaryCard("PENDING", pendingValue, AMBER, "Awaiting verification"));
        summaryPanel.add(createSummaryCard("COURSES", courseCountValue, PURPLE, "With medical submissions"));

        return summaryPanel;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color stripeColor, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(226, 232, 240));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                // 3px top horizontal stripe
                g2.setColor(stripeColor);
                g2.fillRect(0, 0, getWidth(), 3);
                // 4px left vertical stripe
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 11));
        titleLbl.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 26));
        value.setForeground(TEXT);

        JLabel capLbl = new JLabel(caption);
        capLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        capLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(capLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout(0, 12));
        tableContainer.setBackground(WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 14, 18)
        ));

        // Header Row: Student Medical Submissions | X SUBMISSIONS RECORDED
        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);

        JLabel listTitle = new JLabel("Student Medical Submissions");
        listTitle.setFont(new Font("Arial", Font.BOLD, 16));
        listTitle.setForeground(TEXT);

        tableCountLabel = new JLabel("0 SUBMISSIONS RECORDED");
        tableCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        tableCountLabel.setForeground(MUTED);

        listHeader.add(listTitle, BorderLayout.WEST);
        listHeader.add(tableCountLabel, BorderLayout.EAST);
        tableContainer.add(listHeader, BorderLayout.NORTH);

        // Table (7 Columns)
        String[] cols = {
                "Medical ID",
                "Student ID",
                "Student Name",
                "Course",
                "Medical Date",
                "Approval Status",
                "Document"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        rowSorter = new TableRowSorter<>(tableModel);
        medicalTable = new JTable(tableModel);
        medicalTable.setRowSorter(rowSorter);
        medicalTable.setRowHeight(34);
        medicalTable.setFont(LecturerTheme.FONT_PLAIN_13);
        medicalTable.setGridColor(new Color(241, 245, 249));
        medicalTable.setSelectionBackground(new Color(239, 246, 255));
        medicalTable.setSelectionForeground(NAVY);
        medicalTable.setShowGrid(true);

        JTableHeader th = medicalTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(new Color(51, 65, 85));
        th.setPreferredSize(new Dimension(0, 34));

        medicalTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        medicalTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        medicalTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        medicalTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        medicalTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        medicalTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        medicalTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        medicalTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());
        medicalTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 10, 0, 0));
        medicalTable.getColumnModel().getColumn(2).setCellRenderer(leftIndentRenderer);

        medicalTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer());
        medicalTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        medicalTable.getColumnModel().getColumn(5).setCellRenderer(statusRenderer());
        medicalTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer());

        medicalTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedMedicalDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(medicalTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom Toolbar: Left Filter/Search | Right Action Buttons
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Left Controls: Search [  ] Course [ All Courses ▾ ] Status [ All Statuses ▾ ]
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftControls.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        searchLbl.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(LecturerTheme.FONT_PLAIN_13);
        searchField.setPreferredSize(new Dimension(110, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterRecords(); }
            public void removeUpdate(DocumentEvent e) { filterRecords(); }
            public void changedUpdate(DocumentEvent e) { filterRecords(); }
        });

        JLabel courseLbl = new JLabel("Course:");
        courseLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        courseLbl.setForeground(TEXT);

        courseFilter = new JComboBox<>(new String[]{"All Courses"});
        courseFilter.setFont(LecturerTheme.FONT_PLAIN_13);
        courseFilter.setPreferredSize(new Dimension(115, 32));
        courseFilter.addActionListener(e -> {
            if (!isUpdatingDropdown) filterRecords();
        });

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        statusLbl.setForeground(TEXT);

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "APPROVED", "PENDING", "REJECTED"});
        statusFilter.setFont(LecturerTheme.FONT_PLAIN_13);
        statusFilter.setPreferredSize(new Dimension(105, 32));
        statusFilter.addActionListener(e -> {
            if (!isUpdatingDropdown) filterRecords();
        });

        leftControls.add(searchLbl);
        leftControls.add(searchField);
        leftControls.add(Box.createHorizontalStrut(4));
        leftControls.add(courseLbl);
        leftControls.add(courseFilter);
        leftControls.add(Box.createHorizontalStrut(4));
        leftControls.add(statusLbl);
        leftControls.add(statusFilter);

        // Right Buttons: [ View Document ] [ Refresh ] [ View Details ]
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightButtons.setOpaque(false);

        JButton docBtn = createStyledButton("View Document", WHITE, TEXT, 115);
        docBtn.addActionListener(e -> openSelectedDocument());

        JButton refreshBtn = createStyledButton("Refresh", WHITE, TEXT, 75);
        refreshBtn.addActionListener(e -> loadMedicalRecords());

        JButton viewBtn = createStyledButton("View Details", BUTTON_BLUE, Color.WHITE, 110);
        viewBtn.addActionListener(e -> viewSelectedMedicalDetails());

        rightButtons.add(docBtn);
        rightButtons.add(refreshBtn);
        rightButtons.add(viewBtn);

        bottomBar.add(leftControls, BorderLayout.WEST);
        bottomBar.add(rightButtons, BorderLayout.EAST);
        tableContainer.add(bottomBar, BorderLayout.SOUTH);

        return tableContainer;
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

                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                    l.setFont(LecturerTheme.FONT_BOLD_12);
                    return l;
                }

                if ("APPROVED".equalsIgnoreCase(s)) {
                    l.setBackground(new Color(220, 252, 231)); // Soft green
                    l.setForeground(new Color(22, 163, 74));   // Green text
                } else if ("PENDING".equalsIgnoreCase(s)) {
                    l.setBackground(new Color(254, 243, 199)); // Soft amber
                    l.setForeground(new Color(217, 119, 6));   // Amber text
                } else {
                    l.setBackground(new Color(254, 226, 226)); // Soft red
                    l.setForeground(new Color(220, 38, 38));   // Red text
                }
                l.setOpaque(true);
                l.setFont(LecturerTheme.FONT_BOLD_12);
                return l;
            }
        };
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int minWidth) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(WHITE.equals(bg) ? new Color(243, 244, 246) : bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (WHITE.equals(bg)) {
                    g2.setColor(new Color(209, 213, 219));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }

                g2.setFont(getFont());
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(LecturerTheme.FONT_BOLD_12);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Math.max(minWidth, 75), 34));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        return btn;
    }

    public void loadMedicalRecords() {
        loadedRecords.clear();
        displayedRecords.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        try {
            List<MedicalItem> records = MedicalRecordsDAO.loadMedicalRecords(lecturerId);
            loadedRecords.addAll(records);

            // Populate course filter safely without triggering spurious listeners
            isUpdatingDropdown = true;
            courseFilter.removeAllItems();
            courseFilter.addItem("All Courses");
            Set<String> courses = new TreeSet<>();
            for (MedicalItem m : loadedRecords) {
                if (!m.courseCode.isEmpty()) courses.add(m.courseCode);
            }
            for (String c : courses) courseFilter.addItem(c);
            isUpdatingDropdown = false;

            if (subtitleLabel != null) {
                subtitleLabel.setText("Viewing " + loadedRecords.size() + " medical record(s) for your courses (" + (lecturerId != null ? lecturerId : "LEC001") + ").");
            }
        } catch (SQLException e) {
            System.err.println("[MedicalRecordsUI] Error loading medical records: " + e.getMessage());
            if (subtitleLabel != null) subtitleLabel.setText("Database error: " + e.getMessage());
        }

        filterRecords();
    }

    public void filterRecords() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        displayedRecords.clear();

        String selCourse = courseFilter != null ? (String) courseFilter.getSelectedItem() : null;
        String selStatus = statusFilter != null ? (String) statusFilter.getSelectedItem() : null;
        String search = searchField != null ? searchField.getText().trim() : "";

        List<MedicalItem> filtered = MedicalRecordsDAO.filterRecords(loadedRecords, selCourse, selStatus, search);

        for (MedicalItem item : filtered) {
            displayedRecords.add(item);
            tableModel.addRow(item.toTableRow());
        }

        if (tableCountLabel != null) {
            tableCountLabel.setText(filtered.size() + " SUBMISSIONS RECORDED");
        }

        MedicalSummary summary = MedicalRecordsDAO.calculateSummary(filtered);
        if (totalRecordsValue != null) totalRecordsValue.setText(String.valueOf(summary.total));
        if (approvedValue != null) approvedValue.setText(String.valueOf(summary.approved));
        if (pendingValue != null) pendingValue.setText(String.valueOf(summary.pending));
        if (courseCountValue != null) courseCountValue.setText(String.valueOf(summary.courseCount));
    }

    private void viewSelectedMedicalDetails() {
        int row = medicalTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a medical record from the table to view details.",
                    "View Details",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = medicalTable.convertRowIndexToModel(row);
        MedicalItem rec = displayedRecords.get(modelRow);

        JDialog dialog = new JDialog(this, "Medical Record Details - " + rec.medicalId, true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(WHITE);

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 12));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        form.add(makeFieldLabel("Medical Record ID:"));
        form.add(makeFieldValue(rec.medicalId));

        form.add(makeFieldLabel("Student:"));
        form.add(makeFieldValue(rec.studentName + " (" + rec.studentId + ")"));

        form.add(makeFieldLabel("Course:"));
        form.add(makeFieldValue(rec.courseCode + " - " + rec.courseName));

        form.add(makeFieldLabel("Date of Absence:"));
        form.add(makeFieldValue(rec.medicalDate));

        form.add(makeFieldLabel("Approval Status:"));
        form.add(makeFieldValue(rec.approvalStatus));

        form.add(makeFieldLabel("Approved By:"));
        form.add(makeFieldValue(rec.approvedBy.isEmpty() ? "Pending Review" : rec.approvedBy));

        form.add(makeFieldLabel("Reason / Diagnosis:"));
        form.add(makeFieldValue(rec.reason));

        JTextField remarksField = new JTextField(rec.remarks);
        remarksField.setFont(LecturerTheme.FONT_PLAIN_13);
        form.add(makeFieldLabel("Lecturer Remarks:"));
        form.add(remarksField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(WHITE);
        btnPanel.setBorder(new EmptyBorder(0, 0, 10, 20));

        JButton docBtn = createStyledButton("Open Document", WHITE, TEXT, 120);
        docBtn.addActionListener(e -> {
            openSelectedDocument();
        });

        JButton saveRemarksBtn = createStyledButton("Save Remarks", BUTTON_BLUE, Color.WHITE, 120);
        saveRemarksBtn.addActionListener(e -> {
            String newRemarks = remarksField.getText().trim();
            try {
                boolean ok = MedicalRecordsDAO.updateMedicalRemarks(rec.medicalId, newRemarks);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Remarks updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadMedicalRecords();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update remarks.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeBtn = createStyledButton("Close", WHITE, TEXT, 75);
        closeBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(docBtn);
        btnPanel.add(closeBtn);
        btnPanel.add(saveRemarksBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openSelectedDocument() {
        int row = medicalTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a record from the table to open the document.",
                    "Open Document",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = medicalTable.convertRowIndexToModel(row);
        MedicalItem rec = displayedRecords.get(modelRow);

        if (rec.documentPath == null || rec.documentPath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No document attached for this medical record.",
                    "Notice",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File f = MedicalRecordsDAO.resolveDocumentFile(rec.documentPath);
        if (f != null && f.exists()) {
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Document found at:\n" + f.getAbsolutePath() + "\n\nCannot open directly: " + ex.getMessage(),
                        "File Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Document file not found in local storage:\n" + rec.documentPath,
                    "File Not Found",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LecturerTheme.FONT_BOLD_12);
        l.setForeground(MUTED);
        return l;
    }

    private JLabel makeFieldValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LecturerTheme.FONT_PLAIN_13);
        l.setForeground(TEXT);
        return l;
    }

    public static void main(String[] args) {
        String testId = (args != null && args.length > 0) ? args[0] : null;
        if (testId != null) {
            LecturerDashboardUI.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MedicalRecordsUI(testId).setVisible(true);
        });
    }
}
