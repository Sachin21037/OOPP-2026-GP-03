package ui.admin;

import model.Student;
import service.StudentService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Student Management CRUD Panel for FAMS Admin Dashboard.
 * Integrates directly with undergraduates and users tables.
 */
public class StudentManagementPanel extends JPanel {

    // Harmonized Palette matching AdminDashboard
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color LIGHT_BLUE      = new Color(234, 242, 255); // #EAF2FF
    private static final Color MAIN_BG         = new Color(245, 247, 251); // #F5F7FB
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_SECONDARY  = new Color(100, 116, 139); // #64748B
    private static final Color BORDER_COLOR    = new Color(226, 232, 240); // #E2E8F0
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A
    private static final Color WARNING_COLOR   = new Color(217, 119, 6);   // #D97706
    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // #DC2626

    // Table Components
    private JTable studentTable;
    private StudentTableModel tableModel;
    private TableRowSorter<StudentTableModel> sorter;

    // Filter & Search Controls
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterAcademicStatus;
    private JComboBox<String> cmbFilterAccountStatus;
    private JComboBox<String> cmbFilterBatch;
    private JLabel lblTableStatus;

    // Action Buttons
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;

    // Summary Counter Labels
    private JLabel lblTotalStudents;
    private JLabel lblRegularStudents;
    private JLabel lblRepeatStudents;
    private JLabel lblActiveStudents;

    // Callback on data changes (e.g. to refresh AdminDashboard stats)
    private Runnable dataChangedCallback;

    public StudentManagementPanel() {
        this(null);
    }

    public StudentManagementPanel(Runnable dataChangedCallback) {
        this.dataChangedCallback = dataChangedCallback;
        initUI();
        loadStudentsAsync();
    }

    public void setDataChangedCallback(Runnable callback) {
        this.dataChangedCallback = callback;
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 28, 24, 28));

        // 1. Top Summary Cards (Quick overview of student metrics)
        JPanel topSummary = createSummaryCards();
        add(topSummary, BorderLayout.NORTH);

        // 2. Main Content Card (Toolbar + Table + Pagination Footer)
        JPanel mainCard = createMainCard();
        add(mainCard, BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        row.setPreferredSize(new Dimension(1000, 86));

        lblTotalStudents   = new JLabel("--");
        lblRegularStudents = new JLabel("--");
        lblRepeatStudents  = new JLabel("--");
        lblActiveStudents  = new JLabel("--");

        row.add(createMiniCard("Total Undergraduates", lblTotalStudents, SECONDARY_BLUE, LIGHT_BLUE));
        row.add(createMiniCard("Regular Students", lblRegularStudents, SUCCESS_COLOR, new Color(220, 252, 231)));
        row.add(createMiniCard("Repeat / Missed", lblRepeatStudents, WARNING_COLOR, new Color(254, 243, 199)));
        row.add(createMiniCard("Active Accounts", lblActiveStudents, new Color(14, 165, 233), new Color(224, 242, 254)));

        return row;
    }

    private JPanel createMiniCard(String title, JLabel valLabel, Color accent, Color bgPill) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        tLbl.setForeground(TEXT_SECONDARY);

        valLabel.setFont(AdminDashboard.getModernFont(Font.BOLD, 22));
        valLabel.setForeground(TEXT_PRIMARY);

        text.add(tLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(valLabel);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMainCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 16, 20));

        // 1. Action Toolbar + Title
        JPanel topBar = createTopActionBar();
        // 2. Search & Filter Bar
        JPanel filterBar = createFilterBar();

        JPanel northGroup = new JPanel(new BorderLayout(0, 12));
        northGroup.setOpaque(false);
        northGroup.add(topBar, BorderLayout.NORTH);
        northGroup.add(filterBar, BorderLayout.SOUTH);

        card.add(northGroup, BorderLayout.NORTH);

        // 3. Table with scroll
        JScrollPane scrollPane = createTableScrollPane();
        card.add(scrollPane, BorderLayout.CENTER);

        // 4. Footer Status Bar
        JPanel footer = createFooterBar();
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTopActionBar() {
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setOpaque(false);

        // Left Header Titles
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Undergraduate Student Directory");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 17));
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel("View, search, register, modify, and manage student records and portal access.");
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        subLbl.setForeground(TEXT_SECONDARY);

        titles.add(titleLbl);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subLbl);

        bar.add(titles, BorderLayout.WEST);

        // Right Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        btnRefresh = createButton("Refresh", "🔄", false, new Color(71, 85, 105));
        btnRefresh.setToolTipText("Reload student list from database");
        btnRefresh.addActionListener(e -> loadStudentsAsync());

        btnDelete = createButton("Delete Student", "🗑", false, DANGER_COLOR);
        btnDelete.setEnabled(false);
        btnDelete.setToolTipText("Delete the selected student record");
        btnDelete.addActionListener(e -> handleDeleteStudent());

        btnEdit = createButton("Edit Student", "✏", false, SECONDARY_BLUE);
        btnEdit.setEnabled(false);
        btnEdit.setToolTipText("Modify details of selected student");
        btnEdit.addActionListener(e -> handleEditStudent());

        btnAdd = createButton("Add Student", "+", true, SECONDARY_BLUE);
        btnAdd.setToolTipText("Register a new student");
        btnAdd.addActionListener(e -> handleAddStudent());

        actions.add(btnRefresh);
        actions.add(btnDelete);
        actions.add(btnEdit);
        actions.add(btnAdd);

        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        // Left Search Field
        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(320, 36));

        txtSearch = new JTextField();
        txtSearch.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by ID, index, name, email...");

        // Real-time search listening
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        bar.add(searchPanel, BorderLayout.WEST);

        // Right Filter Dropdowns
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);

        cmbFilterAcademicStatus = new JComboBox<>(new String[]{"All Academic Statuses", "REGULAR", "REPEAT", "BATCH_MISSED"});
        styleFilterComboBox(cmbFilterAcademicStatus);
        cmbFilterAcademicStatus.addActionListener(e -> applyFilters());

        cmbFilterAccountStatus = new JComboBox<>(new String[]{"All Account Statuses", "ACTIVE", "INACTIVE"});
        styleFilterComboBox(cmbFilterAccountStatus);
        cmbFilterAccountStatus.addActionListener(e -> applyFilters());

        cmbFilterBatch = new JComboBox<>(new String[]{"All Batches", "2024/2025", "2023/2024", "2022/2023"});
        styleFilterComboBox(cmbFilterBatch);
        cmbFilterBatch.addActionListener(e -> applyFilters());

        JButton btnClear = new JButton("Clear Filters");
        btnClear.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        btnClear.setForeground(TEXT_SECONDARY);
        btnClear.setBackground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        btnClear.setPreferredSize(new Dimension(85, 34));
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearFilters());

        filters.add(new JLabel("Filter:"));
        filters.add(cmbFilterAcademicStatus);
        filters.add(cmbFilterAccountStatus);
        filters.add(cmbFilterBatch);
        filters.add(btnClear);

        bar.add(filters, BorderLayout.EAST);
        return bar;
    }

    private void styleFilterComboBox(JComboBox<String> box) {
        box.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(145, 34));
    }

    private JScrollPane createTableScrollPane() {
        tableModel = new StudentTableModel();
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(42);
        studentTable.setShowVerticalLines(false);
        studentTable.setShowHorizontalLines(true);
        studentTable.setGridColor(new Color(241, 245, 249));
        studentTable.setSelectionBackground(new Color(224, 237, 255));
        studentTable.setSelectionForeground(PRIMARY_NAVY);
        studentTable.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        studentTable.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = studentTable.getTableHeader();
        header.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        header.setBackground(PRIMARY_NAVY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Column widths
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(105); // Index No
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(140); // Full Name
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(170); // Email
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(95);  // Phone
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(85);  // Batch
        studentTable.getColumnModel().getColumn(6).setPreferredWidth(75);  // Year
        studentTable.getColumnModel().getColumn(7).setPreferredWidth(180); // Dept
        studentTable.getColumnModel().getColumn(8).setPreferredWidth(110); // Status
        studentTable.getColumnModel().getColumn(9).setPreferredWidth(85);  // Account

        // Custom Cell Renderers for Badges
        studentTable.getColumnModel().getColumn(8).setCellRenderer(new AcademicStatusBadgeRenderer());
        studentTable.getColumnModel().getColumn(9).setCellRenderer(new AccountStatusBadgeRenderer());

        // Default cell renderer with left padding
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return this;
            }
        };

        for (int i = 0; i < 8; i++) {
            studentTable.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
        }

        // Selection Listener for Edit & Delete buttons
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = studentTable.getSelectedRow() >= 0;
            btnEdit.setEnabled(hasSelection);
            btnDelete.setEnabled(hasSelection);
        });

        // Double-click to Edit
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && studentTable.getSelectedRow() >= 0) {
                    handleEditStudent();
                }
            }
        });

        // Context Menu
        setupContextMenu();

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private void setupContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemEdit = new JMenuItem("✏ Edit Student");
        itemEdit.addActionListener(e -> handleEditStudent());

        JMenuItem itemDelete = new JMenuItem("🗑 Delete Student");
        itemDelete.addActionListener(e -> handleDeleteStudent());

        JMenuItem itemView = new JMenuItem("👁 View Details");
        itemView.addActionListener(e -> handleViewStudentDetails());

        menu.add(itemView);
        menu.addSeparator();
        menu.add(itemEdit);
        menu.add(itemDelete);

        studentTable.setComponentPopupMenu(menu);
    }

    private JPanel createFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));

        lblTableStatus = new JLabel("Loading students...");
        lblTableStatus.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        lblTableStatus.setForeground(TEXT_SECONDARY);

        JLabel hint = new JLabel("Double-click a student row to view or edit details.");
        hint.setFont(AdminDashboard.getModernFont(Font.ITALIC, 11));
        hint.setForeground(new Color(148, 163, 184));

        footer.add(lblTableStatus, BorderLayout.WEST);
        footer.add(hint, BorderLayout.EAST);
        return footer;
    }

    private JButton createButton(String text, String icon, boolean primary, Color color) {
        JButton btn = new JButton((icon != null ? icon + "  " : "") + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                } else if (primary) {
                    g2.setColor(color);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btn.setForeground(primary ? Color.WHITE : color);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 12, 36));
        return btn;
    }

    // =========================================================================
    // DATA LOADING & ASYNC OPERATIONS
    // =========================================================================

    public void loadStudentsAsync() {
        lblTableStatus.setText("Refreshing students from database...");

        SwingWorker<List<Student>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Student> doInBackground() {
                return StudentService.getInstance().getAllStudents();
            }

            @Override
            protected void done() {
                try {
                    List<Student> students = get();
                    tableModel.setStudents(students);
                    updateCounters(students);
                    lblTableStatus.setText("Showing " + students.size() + " registered undergraduate students.");
                    applyFilters();

                    if (dataChangedCallback != null) {
                        dataChangedCallback.run();
                    }
                } catch (Exception e) {
                    lblTableStatus.setText("Failed to load students: " + e.getMessage());
                    System.err.println("[StudentManagementPanel ERROR] Loading failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateCounters(List<Student> list) {
        if (list == null) return;
        int total = list.size();
        int regular = 0;
        int repeat = 0;
        int active = 0;

        for (Student s : list) {
            if ("REGULAR".equalsIgnoreCase(s.getStudentStatus())) regular++;
            if ("REPEAT".equalsIgnoreCase(s.getStudentStatus()) || "BATCH_MISSED".equalsIgnoreCase(s.getStudentStatus())) repeat++;
            if ("ACTIVE".equalsIgnoreCase(s.getAccountStatus())) active++;
        }

        lblTotalStudents.setText(String.valueOf(total));
        lblRegularStudents.setText(String.valueOf(regular));
        lblRepeatStudents.setText(String.valueOf(repeat));
        lblActiveStudents.setText(String.valueOf(active));
    }

    private void applyFilters() {
        String keyword = txtSearch.getText().trim();
        String academicStatus = (String) cmbFilterAcademicStatus.getSelectedItem();
        String accountStatus = (String) cmbFilterAccountStatus.getSelectedItem();
        String batch = (String) cmbFilterBatch.getSelectedItem();

        if ("All Academic Statuses".equals(academicStatus)) academicStatus = null;
        if ("All Account Statuses".equals(accountStatus)) accountStatus = null;
        if ("All Batches".equals(batch)) batch = null;

        tableModel.filter(keyword, academicStatus, accountStatus, batch);
        lblTableStatus.setText("Showing " + tableModel.getRowCount() + " of " + tableModel.getAllStudentsCount() + " students.");
    }

    private void clearFilters() {
        txtSearch.setText("");
        cmbFilterAcademicStatus.setSelectedIndex(0);
        cmbFilterAccountStatus.setSelectedIndex(0);
        cmbFilterBatch.setSelectedIndex(0);
        applyFilters();
    }

    // =========================================================================
    // ACTION HANDLERS (Add, Edit, Delete, View)
    // =========================================================================

    private void handleAddStudent() {
        Window window = SwingUtilities.getWindowAncestor(this);
        StudentDialog dialog = new StudentDialog(window, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadStudentsAsync();
        }
    }

    private void handleEditStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = tableModel.getStudentAt(selectedRow);
        if (student == null) return;

        Window window = SwingUtilities.getWindowAncestor(this);
        StudentDialog dialog = new StudentDialog(window, student);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadStudentsAsync();
        }
    }

    private void handleDeleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = tableModel.getStudentAt(selectedRow);
        if (student == null) return;

        // Custom confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html><body style='width: 320px; font-family: sans-serif;'>" +
                "<h3 style='color: #10284A; margin: 0 0 6px 0;'>Confirm Student Deletion</h3>" +
                "<p style='color: #334155; margin: 0 0 8px 0;'>" +
                "Are you sure you want to delete student <b>" + student.getStudentId() + "</b> (" + student.getFullName() + ")?</p>" +
                "<p style='color: #DC2626; font-size: 11px; margin: 0;'><b>Notice:</b> This action cannot be undone. If this student has active academic records (attendance, marks, enrollments), the system will prevent deletion to preserve academic integrity.</p>" +
                "</body></html>",
                "Delete Student Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            StudentService.OperationResult result = StudentService.getInstance().deleteStudent(student.getStudentId());
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Student Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadStudentsAsync();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "<html><body style='width: 360px; font-family: sans-serif;'>" +
                        "<h4 style='color: #DC2626; margin: 0 0 6px 0;'>Deletion Prevented by System Constraints</h4>" +
                        "<p style='color: #334155; line-height: 1.4;'>" + result.getMessage() + "</p>" +
                        "</body></html>",
                        "Cannot Delete Student",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void handleViewStudentDetails() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0) return;

        Student s = tableModel.getStudentAt(selectedRow);
        if (s == null) return;

        String details = "<html><body style='width: 360px; font-family: sans-serif;'>" +
                "<h3 style='color: #10284A; border-bottom: 2px solid #1D4ED8; padding-bottom: 4px; margin-bottom: 8px;'>" +
                s.getFullName() + " (" + s.getStudentId() + ")</h3>" +
                "<table cellpadding='3' cellspacing='0' style='width: 100%; font-size: 12px; color: #1E293B;'>" +
                "<tr><td><b>Index No:</b></td><td>" + s.getIndexNo() + "</td></tr>" +
                "<tr><td><b>Username:</b></td><td>" + s.getUsername() + "</td></tr>" +
                "<tr><td><b>Email:</b></td><td>" + (s.getEmail() != null ? s.getEmail() : "-") + "</td></tr>" +
                "<tr><td><b>Phone:</b></td><td>" + (s.getPhone() != null ? s.getPhone() : "-") + "</td></tr>" +
                "<tr><td><b>Address:</b></td><td>" + (s.getAddress() != null ? s.getAddress() : "-") + "</td></tr>" +
                "<tr><td><b>Batch:</b></td><td>" + s.getBatch() + "</td></tr>" +
                "<tr><td><b>Academic Year:</b></td><td>" + s.getAcademicYear() + "</td></tr>" +
                "<tr><td><b>Department:</b></td><td>" + s.getDepartment() + "</td></tr>" +
                "<tr><td><b>Academic Status:</b></td><td><b>" + s.getStudentStatus() + "</b></td></tr>" +
                "<tr><td><b>Account Status:</b></td><td><b>" + s.getAccountStatus() + "</b></td></tr>" +
                "</table></body></html>";

        JOptionPane.showMessageDialog(this, details, "Student Profile: " + s.getStudentId(), JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // TABLE MODEL & BADGE RENDERERS
    // =========================================================================

    private static class StudentTableModel extends AbstractTableModel {
        private final String[] columns = {
                "Student ID", "Index No", "Full Name", "Email", "Phone",
                "Batch", "Year", "Department", "Academic Status", "Account"
        };
        private List<Student> allStudents = new ArrayList<>();
        private List<Student> displayedStudents = new ArrayList<>();

        public void setStudents(List<Student> students) {
            this.allStudents = (students != null) ? new ArrayList<>(students) : new ArrayList<>();
            this.displayedStudents = new ArrayList<>(this.allStudents);
            fireTableDataChanged();
        }

        public void filter(String query, String academicStatus, String accountStatus, String batch) {
            displayedStudents.clear();
            String q = (query != null) ? query.toLowerCase().trim() : "";

            for (Student s : allStudents) {
                // Search match
                boolean matchesSearch = q.isEmpty()
                        || s.getStudentId().toLowerCase().contains(q)
                        || s.getIndexNo().toLowerCase().contains(q)
                        || s.getFullName().toLowerCase().contains(q)
                        || (s.getEmail() != null && s.getEmail().toLowerCase().contains(q))
                        || (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(q));

                // Academic status match
                boolean matchesAcad = (academicStatus == null) || academicStatus.equalsIgnoreCase(s.getStudentStatus());

                // Account status match
                boolean matchesAcc = (accountStatus == null) || accountStatus.equalsIgnoreCase(s.getAccountStatus());

                // Batch match
                boolean matchesBatch = (batch == null) || batch.equalsIgnoreCase(s.getBatch());

                if (matchesSearch && matchesAcad && matchesAcc && matchesBatch) {
                    displayedStudents.add(s);
                }
            }
            fireTableDataChanged();
        }

        public int getAllStudentsCount() {
            return allStudents.size();
        }

        public Student getStudentAt(int row) {
            if (row >= 0 && row < displayedStudents.size()) {
                return displayedStudents.get(row);
            }
            return null;
        }

        @Override public int getRowCount() { return displayedStudents.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            if (row < 0 || row >= displayedStudents.size()) return null;
            Student s = displayedStudents.get(row);
            return switch (col) {
                case 0 -> s.getStudentId();
                case 1 -> s.getIndexNo();
                case 2 -> s.getFullName();
                case 3 -> (s.getEmail() != null && !s.getEmail().isBlank()) ? s.getEmail() : "—";
                case 4 -> (s.getPhone() != null && !s.getPhone().isBlank()) ? s.getPhone() : "—";
                case 5 -> s.getBatch();
                case 6 -> s.getAcademicYear();
                case 7 -> s.getDepartment();
                case 8 -> s.getStudentStatus();
                case 9 -> s.getAccountStatus();
                default -> null;
            };
        }
    }

    private static class AcademicStatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));

            String status = (value != null) ? value.toString() : "REGULAR";
            if (!isSelected) {
                switch (status) {
                    case "REGULAR":
                        lbl.setForeground(new Color(21, 128, 61));
                        lbl.setBackground(new Color(220, 252, 231));
                        break;
                    case "REPEAT":
                        lbl.setForeground(new Color(180, 83, 9));
                        lbl.setBackground(new Color(254, 243, 199));
                        break;
                    case "BATCH_MISSED":
                        lbl.setForeground(new Color(185, 28, 28));
                        lbl.setBackground(new Color(254, 226, 226));
                        break;
                    default:
                        lbl.setForeground(TEXT_PRIMARY);
                        lbl.setBackground(table.getBackground());
                }
                lbl.setOpaque(true);
            }
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            return lbl;
        }
    }

    private static class AccountStatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));

            String status = (value != null) ? value.toString() : "ACTIVE";
            if (!isSelected) {
                if ("ACTIVE".equalsIgnoreCase(status)) {
                    lbl.setText("● Active");
                    lbl.setForeground(SUCCESS_COLOR);
                } else {
                    lbl.setText("● Inactive");
                    lbl.setForeground(TEXT_SECONDARY);
                }
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            lbl.setBorder(new EmptyBorder(0, 6, 0, 6));
            return lbl;
        }
    }
}
