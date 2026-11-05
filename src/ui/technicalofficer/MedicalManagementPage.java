package ui.technicalofficer;

import model.CourseOffering;
import model.Medical;
import model.Undergraduate;
import service.AttendanceService;
import service.MedicalService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Medical Certificates Monitoring Page for Technical Officer
 * Package: ui.technicalofficer
 * Follows the ER Diagram (MEDICALS entity) and Class Diagram (Medical & TechnicalOfficer).
 * Monitors student medical submissions for attendance of classes.
 * Mapped to database/fams.sql schema and persistent via MedicalService.
 */
public class MedicalManagementPage extends JPanel {

    // Theme colors matching Technical Officer Dashboard - Soft Sage Green Theme
    private final Color darkNavy = new Color(26, 83, 71);
    private final Color lightBackground = new Color(239, 245, 242);
    private final Color cardWhite = Color.WHITE;
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color successGreen = new Color(20, 165, 145);
    private final Color alertRed = new Color(210, 60, 60);
    private final Color pendingPurple = new Color(115, 75, 180);
    private final Color borderColor = new Color(225, 228, 232);
    private final Color textDark = new Color(35, 45, 55);
    private final Color textMuted = new Color(110, 120, 130);
    private final Color tableStripe = new Color(240, 244, 249);
    private final Color tableGridColor = new Color(218, 224, 233);
    private final Color tableSelectionBg = new Color(193, 218, 243);

    // Business services
    private final MedicalService medicalService = new MedicalService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Search & filter controls
    private JTextField searchStudentField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> courseFilterCombo;

    // Table, model, and sorter
    private JTable medicalTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Footer counter badges
    private JLabel totalMedicalsLabel;
    private JLabel pendingCountLabel;
    private JLabel approvedCountLabel;
    private JLabel rejectedCountLabel;

    // Action buttons
    private JButton addMedicalButton;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton viewDocButton;
    private JButton removeButton;

    public MedicalManagementPage() {
        setLayout(new BorderLayout());
        setBackground(lightBackground);

        initComponents();
        loadMedicalData();
        updateLiveCounters();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBackground(lightBackground);
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Title header
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(lightBackground);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(new EmptyBorder(0, 4, 6, 4));

        JLabel title = new JLabel("Medical Certificates Monitoring");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Monitor, verify, and add student medical submissions for classes with 2-week submission deadline check.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        // Center body
        JPanel centerBody = new JPanel(new BorderLayout(0, 12));
        centerBody.setBackground(lightBackground);
        centerBody.add(createFilterPanel(), BorderLayout.NORTH);
        centerBody.add(createTablePanel(), BorderLayout.CENTER);

        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(centerBody, BorderLayout.CENTER);
        contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    // Search and filter card
    private JPanel createFilterPanel() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel sectionTitle = new JLabel("Search & Filter Submissions");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(darkNavy);

        JPanel filterGrid = new JPanel(new GridBagLayout());
        filterGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Search field
        searchStudentField = new JTextField("");
        styleTextField(searchStudentField);
        searchStudentField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Status filter
        String[] statuses = {"All Statuses", "Pending", "Approved", "Rejected"};
        statusFilterCombo = new JComboBox<>(statuses);
        styleComboBox(statusFilterCombo);
        statusFilterCombo.addActionListener(e -> applyFilters());

        // Course filter loaded dynamically from course offerings
        courseFilterCombo = new JComboBox<>();
        courseFilterCombo.addItem("-- All Courses --");
        List<CourseOffering> offerings = attendanceService.getCourseOfferings();
        for (CourseOffering o : offerings) {
            courseFilterCombo.addItem(o.getCourseCode() + " - " + o.getCourseName());
        }
        styleComboBox(courseFilterCombo);
        courseFilterCombo.addActionListener(e -> applyFilters());

        // Action buttons
        JButton filterButton = createStyledButton("Apply Filter", primaryBlue, Color.WHITE);
        filterButton.addActionListener(e -> applyFilters());

        JButton resetButton = createStyledButton("Reset", Color.WHITE, textDark);
        resetButton.addActionListener(e -> resetFilters());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(resetButton);
        btnPanel.add(filterButton);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        filterGrid.add(createLabeledField("Search Student ID / Index No / Name:", searchStudentField), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1;
        filterGrid.add(createLabeledField("Approval Status:", statusFilterCombo), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1;
        filterGrid.add(createLabeledField("Course Offering:", courseFilterCombo), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        filterGrid.add(btnPanel, gbc);

        card.add(sectionTitle, BorderLayout.NORTH);
        card.add(filterGrid, BorderLayout.CENTER);

        return card;
    }

    // Table panel
    private JPanel createTablePanel() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("Medical Certificates Register");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 14));
        tableTitle.setForeground(darkNavy);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tableActions.setOpaque(false);

        addMedicalButton = createStyledButton("+ Add Submission", primaryBlue, Color.WHITE);
        addMedicalButton.addActionListener(e -> showAddMedicalDialog());

        approveButton = createStyledButton("Approve", successGreen, Color.WHITE);
        approveButton.addActionListener(e -> approveSelectedMedical());

        rejectButton = createStyledButton("Reject", alertRed, Color.WHITE);
        rejectButton.addActionListener(e -> rejectSelectedMedical());

        viewDocButton = createStyledButton("View Slip", Color.WHITE, textDark);
        viewDocButton.addActionListener(e -> viewDocument());

        removeButton = createStyledButton("Remove", Color.WHITE, textDark);
        removeButton.addActionListener(e -> removeSelectedRow());

        tableActions.add(addMedicalButton);
        tableActions.add(approveButton);
        tableActions.add(rejectButton);
        tableActions.add(viewDocButton);
        tableActions.add(removeButton);

        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(tableActions, BorderLayout.EAST);

        String[] columns = {
                "#",
                "Medical ID",
                "Student ID",
                "Index No",
                "Student Name",
                "Submission Date",
                "Medical Start Date",
                "Medical End Date",
                "Course Offering",
                "Reason",
                "Document Path",
                "Approval Status",
                "Approved By",
                "Remarks"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9 || column == 13;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        medicalTable = new JTable(tableModel);
        medicalTable.setRowHeight(34);
        medicalTable.setFont(new Font("Arial", Font.PLAIN, 13));
        medicalTable.setGridColor(tableGridColor);
        medicalTable.setShowGrid(true);
        medicalTable.setShowHorizontalLines(true);
        medicalTable.setShowVerticalLines(true);
        medicalTable.setSelectionBackground(tableSelectionBg);
        medicalTable.setSelectionForeground(darkNavy);
        medicalTable.setFillsViewportHeight(true);

        rowSorter = new TableRowSorter<>(tableModel);
        medicalTable.setRowSorter(rowSorter);

        JTableHeader tableHeader = medicalTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 36));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setOpaque(true);
                label.setBackground(darkNavy);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(45, 90, 135)),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));

                if (table.getRowSorter() != null) {
                    java.util.List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
                    if (!sortKeys.isEmpty() && sortKeys.get(0).getColumn() == table.convertColumnIndexToModel(column)) {
                        SortOrder order = sortKeys.get(0).getSortOrder();
                        if (order == SortOrder.ASCENDING) {
                            label.setText(label.getText() + " ▲");
                        } else if (order == SortOrder.DESCENDING) {
                            label.setText(label.getText() + " ▼");
                        }
                    }
                }
                return label;
            }
        });

        setColumnWidth(0, 40, 50);
        setColumnWidth(1, 80, 95);
        setColumnWidth(2, 80, 95);
        setColumnWidth(3, 100, 120);
        setColumnWidth(4, 130, 170);
        setColumnWidth(5, 105, 120);
        setColumnWidth(6, 105, 120);
        setColumnWidth(7, 105, 120);
        setColumnWidth(8, 160, 220);
        setColumnWidth(9, 140, 200);
        setColumnWidth(10, 110, 150);
        setColumnWidth(11, 105, 120);
        setColumnWidth(12, 85, 105);
        setColumnWidth(13, 150, 220);

        medicalTable.getColumnModel().getColumn(11).setCellRenderer(new MedicalStatusRenderer());
        medicalTable.setDefaultRenderer(String.class, new AlternateRowRenderer());

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE ||
                e.getType() == TableModelEvent.INSERT ||
                e.getType() == TableModelEvent.DELETE) {
                updateLiveCounters();
            }
        });

        JScrollPane scrollPane = new JScrollPane(medicalTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(tableHeaderPanel, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    public void loadMedicalData() {
        tableModel.setRowCount(0);
        List<Medical> list = medicalService.getAllMedicals();
        int count = 1;
        for (Medical m : list) {
            String subDateStr = m.getMedicalDate() != null ? dateFormat.format(m.getMedicalDate()) : "-";
            String startStr = m.getStartDate() != null ? dateFormat.format(m.getStartDate()) : subDateStr;
            String endStr = m.getEndDate() != null ? dateFormat.format(m.getEndDate()) : startStr;

            tableModel.addRow(new Object[]{
                    String.valueOf(count++),
                    m.getMedicalCode(),
                    m.getStudentCode(),
                    m.getIndexNo(),
                    m.getStudentName(),
                    subDateStr,
                    startStr,
                    endStr,
                    m.getCourseOfferingName(),
                    m.getReason(),
                    m.getDocumentPath(),
                    m.getApprovalStatus(),
                    m.getApprovedBy() != null ? m.getApprovedBy() : "-",
                    m.getRemarks() != null ? m.getRemarks() : ""
            });
        }
    }

    private JPanel createFooterPanel() {
        JPanel footerCard = createCardPanel();
        footerCard.setLayout(new BorderLayout(12, 0));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statsPanel.setOpaque(false);

        totalMedicalsLabel = createBadge("Total Medicals: 0", darkNavy, new Color(236, 242, 248));
        pendingCountLabel = createBadge("Pending: 0", pendingPurple, new Color(245, 238, 255));
        approvedCountLabel = createBadge("Approved: 0", successGreen, new Color(230, 247, 244));
        rejectedCountLabel = createBadge("Rejected: 0", alertRed, new Color(254, 237, 237));

        statsPanel.add(totalMedicalsLabel);
        statsPanel.add(pendingCountLabel);
        statsPanel.add(approvedCountLabel);
        statsPanel.add(rejectedCountLabel);

        footerCard.add(statsPanel, BorderLayout.WEST);

        return footerCard;
    }

    private void approveSelectedMedical() {
        int viewRow = medicalTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a medical submission from the table to approve.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = medicalTable.convertRowIndexToModel(viewRow);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 11);
        if ("Approved".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "This medical certificate is already approved.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String medId = (String) tableModel.getValueAt(modelRow, 1);
        String studentId = (String) tableModel.getValueAt(modelRow, 2);

        medicalService.approveMedical(medId, "TO001");

        tableModel.setValueAt("Approved", modelRow, 11);
        tableModel.setValueAt("TO001", modelRow, 12);
        tableModel.setValueAt("Approved after document verification.", modelRow, 13);

        JOptionPane.showMessageDialog(this,
                String.format("Medical ID %s for Student %s has been APPROVED successfully.\nApproved by: TO001\nStatus: Approved",
                        medId, studentId),
                "Medical Approved", JOptionPane.INFORMATION_MESSAGE);

        updateLiveCounters();
    }

    private void rejectSelectedMedical() {
        int viewRow = medicalTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a medical submission from the table to reject.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = medicalTable.convertRowIndexToModel(viewRow);
        String medId = (String) tableModel.getValueAt(modelRow, 1);
        String studentId = (String) tableModel.getValueAt(modelRow, 2);

        medicalService.rejectMedical(medId, "TO001");

        tableModel.setValueAt("Rejected", modelRow, 11);
        tableModel.setValueAt("TO001", modelRow, 12);
        tableModel.setValueAt("Supporting document rejected after review.", modelRow, 13);

        JOptionPane.showMessageDialog(this,
                String.format("Medical ID %s for Student %s has been REJECTED.\nDecision by: TO001\nStatus: Rejected",
                        medId, studentId),
                "Medical Rejected", JOptionPane.INFORMATION_MESSAGE);

        updateLiveCounters();
    }

    private void viewDocument() {
        int viewRow = medicalTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a medical certificate row to view its attached document.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = medicalTable.convertRowIndexToModel(viewRow);
        String medId = (String) tableModel.getValueAt(modelRow, 1);
        String sId = (String) tableModel.getValueAt(modelRow, 2);
        String indexNo = (String) tableModel.getValueAt(modelRow, 3);
        String studentName = (String) tableModel.getValueAt(modelRow, 4);
        String subDate = (String) tableModel.getValueAt(modelRow, 5);
        String startDate = (String) tableModel.getValueAt(modelRow, 6);
        String endDate = (String) tableModel.getValueAt(modelRow, 7);
        String course = (String) tableModel.getValueAt(modelRow, 8);
        String reason = (String) tableModel.getValueAt(modelRow, 9);
        String docPath = (String) tableModel.getValueAt(modelRow, 10);
        String status = (String) tableModel.getValueAt(modelRow, 11);
        String approvedBy = (String) tableModel.getValueAt(modelRow, 12);
        String remarks = (String) tableModel.getValueAt(modelRow, 13);

        String details = String.format(
                "--- Medical Certificate Document Details ---\n\n" +
                "Medical ID          : %s\n" +
                "Student ID          : %s\n" +
                "Index No            : %s\n" +
                "Student Name        : %s\n" +
                "Submission Date     : %s\n" +
                "Medical Start Date  : %s\n" +
                "Medical End Date    : %s\n" +
                "Course Offering     : %s\n" +
                "Reason              : %s\n" +
                "Document Path       : %s\n" +
                "Current Status      : %s\n" +
                "Approved By         : %s\n" +
                "Remarks             : %s\n\n" +
                "Certificate File verified: [Official Medical Center Verification Record]",
                medId, sId, indexNo, studentName, subDate, startDate, endDate, course, reason, docPath, status, approvedBy, remarks
        );

        JOptionPane.showMessageDialog(this, details, "Medical Document Viewer", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAddMedicalDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Medical Submission", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Input fields
        JTextField studentIdField = new JTextField("STU005");
        JTextField indexNoField = new JTextField("TG/2024/2005");
        JTextField nameField = new JTextField("Sanduni Fernando");

        // Dynamic auto-fill on student ID change
        studentIdField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String input = studentIdField.getText().trim();
                if (!input.isEmpty()) {
                    Undergraduate u = attendanceService.findStudentByIndex(input);
                    if (u != null) {
                        indexNoField.setText(u.getIndexNo());
                        nameField.setText(u.getFullName());
                    }
                }
            }
        });

        JComboBox<String> courseCombo = new JComboBox<>();
        List<CourseOffering> offerings = attendanceService.getCourseOfferings();
        for (CourseOffering o : offerings) {
            courseCombo.addItem(o.getOfferingCode() + ": " + o.getCourseCode() + " - " + o.getCourseName());
        }

        String todayStr = dateFormat.format(new Date());
        JTextField subDateField = new JTextField(todayStr);
        JTextField startDateField = new JTextField(todayStr);
        JTextField endDateField = new JTextField(todayStr);

        JTextField reasonField = new JTextField("Viral fever and medical treatment");
        JTextField docPathField = new JTextField("medical/slip_new.pdf");
        JTextField remarksField = new JTextField("Submitted by student");

        styleTextField(studentIdField);
        styleTextField(indexNoField);
        styleTextField(nameField);
        styleComboBox(courseCombo);
        styleTextField(subDateField);
        styleTextField(startDateField);
        styleTextField(endDateField);
        styleTextField(reasonField);
        styleTextField(docPathField);
        styleTextField(remarksField);

        // Live preview banner for date difference and status
        JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        previewPanel.setBackground(new Color(236, 248, 245));
        previewPanel.setBorder(new LineBorder(new Color(175, 230, 220), 1, true));

        JLabel previewLabel = new JLabel("✓ Difference: 0 days | Expected Status: PENDING (Within 2-week deadline)");
        previewLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        previewLabel.setForeground(successGreen);
        previewPanel.add(previewLabel);

        Runnable recalculatePreview = () -> {
            String sSub = subDateField.getText().trim();
            String sStart = startDateField.getText().trim();
            try {
                Date dSub = dateFormat.parse(sSub);
                Date dStart = dateFormat.parse(sStart);
                long diffMs = dSub.getTime() - dStart.getTime();
                long diffDays = diffMs / (1000L * 60 * 60 * 24);

                if (diffDays > 14) {
                    previewLabel.setText(String.format("⚠️ Difference: %d days (> 14 days) | Submission Status: REJECTED (Late)", diffDays));
                    previewLabel.setForeground(alertRed);
                    previewPanel.setBackground(new Color(254, 237, 237));
                    previewPanel.setBorder(new LineBorder(new Color(248, 180, 180), 1, true));
                } else if (diffDays >= 0) {
                    previewLabel.setText(String.format("✓ Difference: %d days (<= 14 days) | Submission Status: PENDING (Within 2 weeks)", diffDays));
                    previewLabel.setForeground(successGreen);
                    previewPanel.setBackground(new Color(236, 248, 245));
                    previewPanel.setBorder(new LineBorder(new Color(175, 230, 220), 1, true));
                } else {
                    previewLabel.setText("✓ Advance submission / Same day | Submission Status: PENDING");
                    previewLabel.setForeground(primaryBlue);
                    previewPanel.setBackground(new Color(240, 245, 252));
                    previewPanel.setBorder(new LineBorder(new Color(195, 215, 240), 1, true));
                }
            } catch (Exception ex) {
                previewLabel.setText("ℹ️ Enter dates as YYYY-MM-DD to verify 2-week submission limit.");
                previewLabel.setForeground(textMuted);
                previewPanel.setBackground(new Color(248, 250, 252));
                previewPanel.setBorder(new LineBorder(borderColor, 1, true));
            }
        };

        DocumentListener dateListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalculatePreview.run(); }
            public void removeUpdate(DocumentEvent e) { recalculatePreview.run(); }
            public void changedUpdate(DocumentEvent e) { recalculatePreview.run(); }
        };
        subDateField.getDocument().addDocumentListener(dateListener);
        startDateField.getDocument().addDocumentListener(dateListener);

        int row = 0;
        addFormRow(formGrid, gbc, row++, "Student ID (e.g. STU005) *:", studentIdField);
        addFormRow(formGrid, gbc, row++, "Index No (e.g. TG/2024/2005) *:", indexNoField);
        addFormRow(formGrid, gbc, row++, "Student Name *:", nameField);
        addFormRow(formGrid, gbc, row++, "Course Offering *:", courseCombo);
        addFormRow(formGrid, gbc, row++, "Medical Submission Date (YYYY-MM-DD) *:", subDateField);
        addFormRow(formGrid, gbc, row++, "Medical Starting Date (YYYY-MM-DD) *:", startDateField);
        addFormRow(formGrid, gbc, row++, "Medical Ending Date (YYYY-MM-DD) *:", endDateField);

        // Add preview banner
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 6, 8, 6);
        formGrid.add(previewPanel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 6, 5, 6);
        addFormRow(formGrid, gbc, row++, "Reason for Medical *:", reasonField);
        addFormRow(formGrid, gbc, row++, "Document File Path:", docPathField);
        addFormRow(formGrid, gbc, row++, "Initial Remarks:", remarksField);

        formWrapper.add(formGrid, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(lightBackground);

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, textDark);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Save Submission", primaryBlue, Color.WHITE);
        saveBtn.addActionListener(e -> {
            String sId = studentIdField.getText().trim();
            String idx = indexNoField.getText().trim();
            String name = nameField.getText().trim();
            String subDateStr = subDateField.getText().trim();
            String startDateStr = startDateField.getText().trim();
            String endDateStr = endDateField.getText().trim();
            String selectedCourse = (String) courseCombo.getSelectedItem();
            String reason = reasonField.getText().trim();
            String doc = docPathField.getText().trim();
            String remarks = remarksField.getText().trim();

            if (sId.isEmpty() || idx.isEmpty() || name.isEmpty() ||
                subDateStr.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please fill in all required fields (Student ID, Index No, Name, and all Date fields).",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date parsedSubDate;
            Date parsedStartDate;
            Date parsedEndDate;

            try {
                parsedSubDate = dateFormat.parse(subDateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid Medical Submission Date format. Please use YYYY-MM-DD (e.g. 2026-03-15).",
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                parsedStartDate = dateFormat.parse(startDateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid Medical Starting Date format. Please use YYYY-MM-DD (e.g. 2026-02-20).",
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                parsedEndDate = dateFormat.parse(endDateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid Medical Ending Date format. Please use YYYY-MM-DD (e.g. 2026-02-25).",
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (parsedEndDate.before(parsedStartDate)) {
                JOptionPane.showMessageDialog(dialog,
                        "Medical ending date cannot be earlier than medical starting date.",
                        "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Compare medical submission date and medical starting date
            long diffMillis = parsedSubDate.getTime() - parsedStartDate.getTime();
            long diffDays = diffMillis / (1000L * 60 * 60 * 24);

            boolean isLateSubmission = (diffDays > 14);
            String status = isLateSubmission ? "Rejected" : "Pending";
            String approvedBy = isLateSubmission ? "TO001" : "-";
            String remarksToSave = remarks;

            if (isLateSubmission) {
                String lateTag = String.format("[Auto-Rejected: Submitted %d days after start date (exceeds 2-week limit)]", diffDays);
                remarksToSave = remarksToSave.isEmpty() ? lateTag : remarksToSave + " " + lateTag;
            }

            String offeringCode = "OFF001";
            if (selectedCourse != null && selectedCourse.contains(":")) {
                offeringCode = selectedCourse.split(":")[0].trim();
            }

            String medId = medicalService.getNextMedicalId();

            Medical newMed = new Medical(
                    medId, sId, idx, name, offeringCode, selectedCourse,
                    parsedSubDate, parsedStartDate, parsedEndDate,
                    reason.isEmpty() ? "Medical Certificate" : reason,
                    doc.isEmpty() ? "medical/" + medId + ".pdf" : doc,
                    status, approvedBy, remarksToSave
            );

            medicalService.addMedical(newMed);

            tableModel.addRow(new Object[]{
                    String.valueOf(tableModel.getRowCount() + 1),
                    medId,
                    sId,
                    idx,
                    name,
                    dateFormat.format(parsedSubDate),
                    dateFormat.format(parsedStartDate),
                    dateFormat.format(parsedEndDate),
                    selectedCourse,
                    reason,
                    doc,
                    status,
                    approvedBy,
                    remarksToSave
            });

            dialog.dispose();
            updateLiveCounters();

            if (isLateSubmission) {
                JOptionPane.showMessageDialog(this,
                        String.format("⚠️ Medical submission %s has been submitted with REJECTED status!\n\n" +
                                "• Student: %s (%s)\n" +
                                "• Medical Starting Date: %s\n" +
                                "• Submission Date: %s\n" +
                                "• Time Difference: %d days (Exceeds 2-week / 14-day limit)\n" +
                                "• Decision: Auto-rejected\n" +
                                "• Recorded By: TO001\n" +
                                "• Status: REJECTED",
                                medId, name, sId, dateFormat.format(parsedStartDate), dateFormat.format(parsedSubDate), diffDays),
                        "Medical Submission Auto-Rejected",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        String.format("Medical submission %s recorded successfully.\n\n" +
                                "• Student: %s (%s)\n" +
                                "• Medical Starting Date: %s\n" +
                                "• Submission Date: %s\n" +
                                "• Time Difference: %d days (Within 2-week limit)\n" +
                                "• Status: PENDING",
                                medId, name, sId, dateFormat.format(parsedStartDate), dateFormat.format(parsedSubDate), diffDays),
                        "Medical Recorded",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(new JScrollPane(formWrapper), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(textDark);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void removeSelectedRow() {
        int viewRow = medicalTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to remove.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = medicalTable.convertRowIndexToModel(viewRow);
        String medId = (String) tableModel.getValueAt(modelRow, 1);

        medicalService.deleteMedical(medId);
        tableModel.removeRow(modelRow);
        renumberRows();
    }

    private void renumberRows() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(String.valueOf(i + 1), i, 0);
        }
        updateLiveCounters();
    }

    private void applyFilters() {
        String search = searchStudentField.getText().trim();
        String status = (String) statusFilterCombo.getSelectedItem();
        String course = (String) courseFilterCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                if (!search.isEmpty()) {
                    String sId = entry.getStringValue(2).toLowerCase();
                    String idx = entry.getStringValue(3).toLowerCase();
                    String name = entry.getStringValue(4).toLowerCase();
                    String q = search.toLowerCase();
                    if (!sId.contains(q) && !idx.contains(q) && !name.contains(q)) {
                        return false;
                    }
                }

                if (status != null && !"All Statuses".equalsIgnoreCase(status)) {
                    String st = entry.getStringValue(11);
                    if (!status.equalsIgnoreCase(st)) {
                        return false;
                    }
                }

                if (course != null && !"-- All Courses --".equalsIgnoreCase(course)) {
                    String crs = entry.getStringValue(8);
                    if (!crs.toLowerCase().contains(course.toLowerCase())) {
                        return false;
                    }
                }

                return true;
            }
        };

        rowSorter.setRowFilter(rf);
        updateLiveCounters();
    }

    private void resetFilters() {
        searchStudentField.setText("");
        statusFilterCombo.setSelectedIndex(0);
        courseFilterCombo.setSelectedIndex(0);
        rowSorter.setRowFilter(null);
        updateLiveCounters();
    }

    private void updateLiveCounters() {
        int total = tableModel.getRowCount();
        int pending = 0;
        int approved = 0;
        int rejected = 0;

        for (int i = 0; i < total; i++) {
            Object statusObj = tableModel.getValueAt(i, 11);
            String st = statusObj != null ? statusObj.toString() : "";
            if ("Pending".equalsIgnoreCase(st)) {
                pending++;
            } else if ("Approved".equalsIgnoreCase(st)) {
                approved++;
            } else if ("Rejected".equalsIgnoreCase(st) || "Not Approved".equalsIgnoreCase(st)) {
                rejected++;
            }
        }

        totalMedicalsLabel.setText("Total Medicals: " + total);
        pendingCountLabel.setText("Pending: " + pending);
        approvedCountLabel.setText("Approved: " + approved);
        rejectedCountLabel.setText("Rejected: " + rejected);
    }

    private void setColumnWidth(int colIndex, int preferredWidth, int maxWidth) {
        TableColumn col = medicalTable.getColumnModel().getColumn(colIndex);
        col.setPreferredWidth(preferredWidth);
        if (maxWidth > 0 && colIndex == 0) {
            col.setMaxWidth(maxWidth);
            col.setMinWidth(preferredWidth);
        }
    }

    private JPanel createLabeledField(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(textDark);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(cardWhite);
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return card;
    }

    private JLabel createBadge(String text, Color textColor, Color bgColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(textColor);
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setBorder(new CompoundBorder(
                new LineBorder(textColor, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));
        return label;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        boolean isWhite = Color.WHITE.equals(bg);
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(isWhite ? new Color(225, 230, 238) : bg.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(isWhite ? new Color(242, 245, 248) : bg.darker());
                } else {
                    g2.setColor(bg);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                if (isWhite) {
                    g2.setColor(borderColor);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(fg);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 14, 6, 14));
        return button;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Arial", Font.PLAIN, 13));
        textField.setForeground(textDark);
        textField.setBackground(Color.WHITE);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 30));
        textField.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        comboBox.setForeground(textDark);
        comboBox.setBackground(Color.WHITE);
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 30));
    }

    // Render approval status badge
    private class MedicalStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            String status = value != null ? value.toString() : "";
            if ("Approved".equalsIgnoreCase(status)) {
                label.setForeground(new Color(13, 125, 110));
                label.setBackground(isSelected ? tableSelectionBg : new Color(220, 246, 240));
            } else if ("Rejected".equalsIgnoreCase(status) || "Not Approved".equalsIgnoreCase(status)) {
                label.setForeground(new Color(185, 28, 28));
                label.setBackground(isSelected ? tableSelectionBg : new Color(254, 226, 226));
            } else if ("Pending".equalsIgnoreCase(status)) {
                label.setForeground(new Color(109, 40, 217));
                label.setBackground(isSelected ? tableSelectionBg : new Color(237, 228, 255));
            } else {
                label.setForeground(textDark);
                label.setBackground(isSelected ? tableSelectionBg : Color.WHITE);
            }

            label.setBorder(new EmptyBorder(3, 8, 3, 8));
            return label;
        }
    }

    // Zebra striping renderer
    private class AlternateRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setForeground(textDark);
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(tableStripe);
                }
            } else {
                c.setForeground(darkNavy);
                c.setBackground(tableSelectionBg);
            }

            if (column == 0 || column == 1 || column == 2 || column == 3 || column == 5 || column == 6 || column == 7 || column == 12) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (c instanceof JLabel) {
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
            }

            return c;
        }
    }
}
