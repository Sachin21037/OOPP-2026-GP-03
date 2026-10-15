package ui.admin;

import model.Course;
import service.CourseService;
import service.CourseService.OperationResult;

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
import java.util.Map;

/**
 * Modern Course Management CRUD Panel for FAMS Admin Dashboard.
 * Allows administrators to create, view, search, update, and safely manage academic courses.
 */
public class CourseManagementPanel extends JPanel {

    // Palette harmonized with AdminDashboard
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color TEAL_ACCENT     = new Color(13, 148, 136);  // #0D9488
    private static final Color WARNING_COLOR   = new Color(217, 119, 6);   // #D97706
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A
    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // #DC2626
    private static final Color MAIN_BG         = new Color(245, 247, 251); // #F5F7FB
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_SECONDARY  = new Color(100, 116, 139); // #64748B
    private static final Color BORDER_COLOR    = new Color(226, 232, 240); // #E2E8F0
    private static final Color LIGHT_BLUE      = new Color(238, 242, 255); // #EEF2FF

    // Service reference & dashboard callback
    private final CourseService courseService;
    private final Runnable dataChangedCallback;

    // Table & Models
    private JTable courseTable;
    private CourseTableModel tableModel;
    private TableRowSorter<CourseTableModel> sorter;

    // Summary Metric Labels
    private JLabel lblTotalCourses;
    private JLabel lblActiveCourses;
    private JLabel lblInactiveCourses;
    private JLabel lblTotalCredits;

    // Filters & Search
    private JTextField txtSearch;
    private JComboBox<String> cmbDeptFilter;
    private JComboBox<String> cmbStatusFilter;
    private JLabel lblTableStatus;

    // Course Details Form Controls
    private JTextField txtCourseId;
    private JTextField txtCourseCode;
    private JTextField txtCourseName;
    private JSpinner spCredits;
    private JComboBox<String> cmbDepartment;
    private JTextArea taDescription;
    private JTextField txtOfferingStatus;
    private JLabel lblFormMode;

    // Action Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;

    private Course selectedCourse = null;

    public CourseManagementPanel() {
        this(null);
    }

    public CourseManagementPanel(Runnable dataChangedCallback) {
        this.courseService = CourseService.getInstance();
        this.dataChangedCallback = dataChangedCallback;
        initUI();
        loadCoursesAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(16, 24, 20, 24));

        // 1. Top Section: Page Header & Summary Cards
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        topContainer.add(createPageHeader());
        topContainer.add(Box.createVerticalStrut(12));
        topContainer.add(createSummaryCards());
        topContainer.add(Box.createVerticalStrut(14));

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: Master-Detail (Left: Course Table, Right: Course Details Form)
        JPanel masterDetailPanel = createMasterDetailSection();
        add(masterDetailPanel, BorderLayout.CENTER);
    }


    // SECTION A: PAGE HEADER

    private JPanel createPageHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("COURSE MANAGEMENT");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 20));
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel("Manage Academic Courses and Curriculum Modules");
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        subLbl.setForeground(TEXT_SECONDARY);

        titles.add(titleLbl);
        titles.add(Box.createVerticalStrut(3));
        titles.add(subLbl);
        header.add(titles, BorderLayout.WEST);

        // Right side: Quick Refresh Button
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightHeader.setOpaque(false);

        btnRefresh = createIconButton("↻", "Refresh Courses", e -> loadCoursesAsync());
        rightHeader.add(btnRefresh);

        header.add(rightHeader, BorderLayout.EAST);
        return header;
    }


    // SECTION B: SUMMARY CARDS

    private JPanel createSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row.setPreferredSize(new Dimension(1000, 82));

        lblTotalCourses    = new JLabel("--");
        lblActiveCourses   = new JLabel("--");
        lblInactiveCourses = new JLabel("--");
        lblTotalCredits    = new JLabel("--");

        row.add(createMetricCard("Total Courses", lblTotalCourses, PRIMARY_NAVY, new Color(241, 245, 249), "Catalog Curriculum"));
        row.add(createMetricCard("Active Courses", lblActiveCourses, SUCCESS_COLOR, new Color(220, 252, 231), "Scheduled in Offerings"));
        row.add(createMetricCard("Inactive Courses", lblInactiveCourses, WARNING_COLOR, new Color(254, 243, 199), "Unscheduled Modules"));
        row.add(createMetricCard("Total Credits", lblTotalCredits, TEAL_ACCENT, LIGHT_BLUE, "Curriculum Hours"));

        return row;
    }

    private JPanel createMetricCard(String title, JLabel valLabel, Color accent, Color bgPill, String subtext) {
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
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        tLbl.setForeground(TEXT_SECONDARY);

        valLabel.setFont(AdminDashboard.getModernFont(Font.BOLD, 20));
        valLabel.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtext);
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        subLbl.setForeground(new Color(148, 163, 184));

        text.add(tLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(valLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(subLbl);

        card.add(text, BorderLayout.CENTER);
        return card;
    }


    // SECTION C & D & E: MASTER-DETAIL WORKSPACE

    private JPanel createMasterDetailSection() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Panel: Course Table (62% width)
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 12);
        container.add(createTableCard(), gbc);

        // Right Panel: Course Details Form (38% width)
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(createFormCard(), gbc);

        return container;
    }

    // SECTION D: COURSE TABLE CARD

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
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
        card.setBorder(new EmptyBorder(14, 14, 12, 14));

        // 1. Toolbar (Search + Filters)
        JPanel toolbar = createToolbar();
        card.add(toolbar, BorderLayout.NORTH);

        // 2. JTable
        tableModel = new CourseTableModel();
        sorter = new TableRowSorter<>(tableModel);

        courseTable = new JTable(tableModel);
        courseTable.setRowSorter(sorter);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setRowHeight(38);
        courseTable.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        courseTable.setForeground(TEXT_PRIMARY);
        courseTable.setBackground(Color.WHITE);
        courseTable.setShowGrid(false);
        courseTable.setIntercellSpacing(new Dimension(0, 0));
        courseTable.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = courseTable.getTableHeader();
        header.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        header.setForeground(TEXT_SECONDARY);
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);

        setupColumnRenderers();

        // Selection Listener
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // 3. Footer Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 4, 0, 4));

        lblTableStatus = new JLabel("Loading courses...");
        lblTableStatus.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        lblTableStatus.setForeground(TEXT_SECONDARY);
        footer.add(lblTableStatus, BorderLayout.WEST);

        JLabel tipLbl = new JLabel("Tip: Select any course to view details or update.");
        tipLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        tipLbl.setForeground(new Color(148, 163, 184));
        footer.add(tipLbl, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        // Search Input
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(210, 34));
        txtSearch.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search course code or name...");
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterCourses(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterCourses(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterCourses(); }
        });
        leftPanel.add(txtSearch);

        // Status Filter Dropdown (Active Courses / Inactive Courses)
        String[] statuses = {"All Status", "ACTIVE", "UNOFFERED"};
        cmbStatusFilter = new JComboBox<>(statuses);
        styleFilterCombo(cmbStatusFilter, 115);
        cmbStatusFilter.addActionListener(e -> filterCourses());
        leftPanel.add(cmbStatusFilter);

        // Department Filter Dropdown
        String[] depts = {"All Departments", "Information and Communication Technology"};
        cmbDeptFilter = new JComboBox<>(depts);
        styleFilterCombo(cmbDeptFilter, 150);
        cmbDeptFilter.addActionListener(e -> filterCourses());
        leftPanel.add(cmbDeptFilter);

        // Reset Button
        JButton btnReset = new JButton("Reset");
        btnReset.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        btnReset.setForeground(TEXT_SECONDARY);
        btnReset.setBackground(Color.WHITE);
        btnReset.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(6, 10, 6, 10)));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetFilters());
        leftPanel.add(btnReset);

        toolbar.add(leftPanel, BorderLayout.CENTER);
        return toolbar;
    }

    private void styleFilterCombo(JComboBox<String> cmb, int width) {
        cmb.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        cmb.setBackground(Color.WHITE);
        cmb.setPreferredSize(new Dimension(width, 34));
    }

    private void setupColumnRenderers() {
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(new Color(224, 237, 255));
                    c.setForeground(PRIMARY_NAVY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < courseTable.getColumnCount(); i++) {
            courseTable.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
        }

        // Col 0: Course ID
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(75);

        // Col 1: Course Code Badge
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        courseTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String code = (value != null) ? value.toString() : "";
                JLabel badge = new JLabel(" " + code + " ");
                badge.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
                badge.setOpaque(true);
                badge.setBackground(LIGHT_BLUE);
                badge.setForeground(SECONDARY_BLUE);
                badge.setBorder(new EmptyBorder(2, 6, 2, 6));

                p.add(badge);
                return p;
            }
        });

        // Col 2: Course Name
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(170);

        // Col 3: Credits Pill
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        courseTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String cr = (value != null) ? value.toString() + " CR" : "0 CR";
                JLabel badge = new JLabel(" " + cr + " ");
                badge.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
                badge.setOpaque(true);
                badge.setBackground(new Color(241, 245, 249));
                badge.setForeground(TEXT_SECONDARY);
                badge.setBorder(new EmptyBorder(2, 6, 2, 6));

                p.add(badge);
                return p;
            }
        });

        // Col 4: Department
        courseTable.getColumnModel().getColumn(4).setPreferredWidth(160);

        // Col 5: Offering Status Pill
        courseTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        courseTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
                p.setOpaque(true);
                p.setBackground(isSelected ? new Color(224, 237, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String status = (value != null) ? value.toString() : "UNOFFERED";
                boolean active = "ACTIVE".equalsIgnoreCase(status);

                JLabel pill = new JLabel("  " + status + "  ");
                pill.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
                pill.setOpaque(true);
                pill.setBorder(new EmptyBorder(2, 8, 2, 8));

                if (active) {
                    pill.setBackground(new Color(220, 252, 231));
                    pill.setForeground(SUCCESS_COLOR);
                } else {
                    pill.setBackground(new Color(254, 243, 199));
                    pill.setForeground(WARNING_COLOR);
                }

                p.add(pill);
                return p;
            }
        });
    }

    // SECTION E: COURSE DETAILS FORM CARD

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
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
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Form Header
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Course Details");
        formTitle.setFont(AdminDashboard.getModernFont(Font.BOLD, 14));
        formTitle.setForeground(TEXT_PRIMARY);

        lblFormMode = new JLabel("NEW COURSE");
        lblFormMode.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
        lblFormMode.setForeground(SECONDARY_BLUE);
        lblFormMode.setOpaque(true);
        lblFormMode.setBackground(LIGHT_BLUE);
        lblFormMode.setBorder(new EmptyBorder(3, 8, 3, 8));

        formHeader.add(formTitle, BorderLayout.WEST);
        formHeader.add(lblFormMode, BorderLayout.EAST);
        card.add(formHeader, BorderLayout.NORTH);

        // Fields Panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        // Row 1: Course ID & Credits (Grid 1x2)
        JPanel rowIdCredits = new JPanel(new GridLayout(1, 2, 10, 0));
        rowIdCredits.setOpaque(false);

        txtCourseId = createStyledTextField();
        txtCourseId.setEditable(false);
        txtCourseId.setBackground(new Color(248, 250, 252));
        rowIdCredits.add(createFormField("Course ID", txtCourseId));

        spCredits = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spCredits.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        spCredits.setPreferredSize(new Dimension(80, 34));
        rowIdCredits.add(createFormField("Credits (1-10) *", spCredits));
        fieldsPanel.add(rowIdCredits);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Row 2: Course Code
        txtCourseCode = createStyledTextField();
        txtCourseCode.putClientProperty("JTextField.placeholderText", "e.g. ICT2132");
        fieldsPanel.add(createFormField("Course Code *", txtCourseCode));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Row 3: Course Name
        txtCourseName = createStyledTextField();
        txtCourseName.putClientProperty("JTextField.placeholderText", "e.g. Object Oriented Programming");
        fieldsPanel.add(createFormField("Course Name *", txtCourseName));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Row 4: Department
        String[] deptOptions = {
                "Information and Communication Technology",
                "Engineering Technology",
                "Biosystems Technology",
                "Multidisciplinary Studies"
        };
        cmbDepartment = new JComboBox<>(deptOptions);
        styleFilterCombo(cmbDepartment, 140);
        fieldsPanel.add(createFormField("Department *", cmbDepartment));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Row 5: Description
        taDescription = new JTextArea(3, 20);
        taDescription.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        taDescription.setForeground(TEXT_PRIMARY);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        taDescription.setBorder(new EmptyBorder(5, 8, 5, 8));

        JScrollPane taScroll = new JScrollPane(taDescription);
        taScroll.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        taScroll.setPreferredSize(new Dimension(100, 65));
        fieldsPanel.add(createFormField("Description / Syllabus", taScroll));
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Row 6: Offerings Info (Read-only status)
        txtOfferingStatus = createStyledTextField();
        txtOfferingStatus.setEditable(false);
        txtOfferingStatus.setBackground(new Color(248, 250, 252));
        fieldsPanel.add(createFormField("Semester Offerings Status", txtOfferingStatus));
        fieldsPanel.add(Box.createVerticalStrut(12));

        // Informational Policy Banner
        JPanel noticeCard = new JPanel(new BorderLayout(8, 0));
        noticeCard.setBackground(new Color(248, 250, 252));
        noticeCard.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel noticeLbl = new JLabel("<html><body style='width: 250px; font-size: 10px; color: #64748B;'>" +
                "<b>Curriculum Rule:</b> Course catalog records define modules across the faculty. " +
                "Semester-specific offerings, lecturer assignments, and enrollments are kept distinct to preserve academic records." +
                "</body></html>");
        noticeCard.add(noticeLbl, BorderLayout.CENTER);
        fieldsPanel.add(noticeCard);

        JScrollPane formScroll = new JScrollPane(fieldsPanel);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        card.add(formScroll, BorderLayout.CENTER);

        // Action Buttons Row (Add, Update, Delete, Clear)
        JPanel actionsPanel = new JPanel();
        actionsPanel.setOpaque(false);
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));

        // Top button row: Add + Update
        JPanel rowBtns1 = new JPanel(new GridLayout(1, 2, 8, 0));
        rowBtns1.setOpaque(false);

        btnAdd = createPrimaryButton("+ Add Course", SECONDARY_BLUE, e -> onAddCourse());
        rowBtns1.add(btnAdd);

        btnUpdate = createPrimaryButton("Update Course", new Color(13, 148, 136), e -> onUpdateCourse());
        btnUpdate.setEnabled(false);
        rowBtns1.add(btnUpdate);

        actionsPanel.add(rowBtns1);
        actionsPanel.add(Box.createVerticalStrut(6));

        // Bottom button row: Delete + Clear
        JPanel rowBtns2 = new JPanel(new GridLayout(1, 2, 8, 0));
        rowBtns2.setOpaque(false);

        btnDelete = createOutlineButton("Delete Course", DANGER_COLOR, e -> onDeleteCourse());
        btnDelete.setEnabled(false);
        rowBtns2.add(btnDelete);

        btnClear = createOutlineButton("Clear Form", TEXT_SECONDARY, e -> clearForm());
        rowBtns2.add(btnClear);

        actionsPanel.add(rowBtns2);
        card.add(actionsPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFormField(String label, JComponent comp) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        field.add(l);
        field.add(Box.createVerticalStrut(3));
        field.add(comp);
        return field;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        tf.setPreferredSize(new Dimension(100, 34));
        return tf;
    }

    private JButton createPrimaryButton(String text, Color bg, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text);
        btn.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        btn.addActionListener(l);
        return btn;
    }

    private JButton createOutlineButton(String text, Color color, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text);
        btn.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        btn.setForeground(color);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(color, 1, true), new EmptyBorder(6, 10, 6, 10)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 34));
        btn.addActionListener(l);
        return btn;
    }

    private JButton createIconButton(String iconText, String tooltip, java.awt.event.ActionListener l) {
        JButton btn = new JButton(iconText);
        btn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        btn.setToolTipText(tooltip);
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(4, 10, 4, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(l);
        return btn;
    }

    // DATA OPERATIONS & HANDLERS


    /**
     * Asynchronously loads courses from the database.
     */
    public void loadCoursesAsync() {
        lblTableStatus.setText("Refreshing courses from database...");
        btnRefresh.setEnabled(false);

        SwingWorker<List<Course>, Void> worker = new SwingWorker<>() {
            private Map<String, Integer> summaryCounts;

            @Override
            protected List<Course> doInBackground() {
                summaryCounts = courseService.getCourseSummaryCounts();
                return courseService.getAllCourses();
            }

            @Override
            protected void done() {
                try {
                    List<Course> courses = get();
                    tableModel.setCourses(courses);
                    filterCourses();
                    updateSummaryCards(summaryCounts);
                    lblTableStatus.setText("Showing " + courses.size() + " academic courses.");

                    if (selectedCourse == null) {
                        clearForm();
                    }
                } catch (Exception e) {
                    lblTableStatus.setText("Error loading courses: " + e.getMessage());
                } finally {
                    btnRefresh.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /**
     * Synchronous load for automated testing.
     */
    public void loadCoursesSync() {
        List<Course> courses = courseService.getAllCourses();
        tableModel.setCourses(courses);
        filterCourses();
        updateSummaryCards(courseService.getCourseSummaryCounts());
        if (selectedCourse == null) {
            clearForm();
        }
    }

    private void updateSummaryCards(Map<String, Integer> counts) {
        if (counts == null) return;
        lblTotalCourses.setText(String.valueOf(counts.getOrDefault("total", 0)));
        lblActiveCourses.setText(String.valueOf(counts.getOrDefault("offered", 0)));
        lblInactiveCourses.setText(String.valueOf(counts.getOrDefault("unscheduled", 0)));
        lblTotalCredits.setText(counts.getOrDefault("total_credits", 0) + " Credits");
    }

    private void filterCourses() {
        String keyword = txtSearch.getText().trim();
        String deptFilter = (String) cmbDeptFilter.getSelectedItem();
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();

        List<RowFilter<CourseTableModel, Integer>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword)));
        }

        if (deptFilter != null && !deptFilter.equalsIgnoreCase("All Departments")) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(deptFilter) + "$", 4));
        }

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Status")) {
            filters.add(RowFilter.regexFilter("^" + statusFilter + "$", 5));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }

        int visible = courseTable.getRowCount();
        int total = tableModel.getRowCount();
        lblTableStatus.setText("Showing " + visible + " of " + total + " courses.");
    }

    private void resetFilters() {
        txtSearch.setText("");
        cmbDeptFilter.setSelectedIndex(0);
        cmbStatusFilter.setSelectedIndex(0);
        filterCourses();
    }

    private void onTableRowSelected() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int modelIdx = courseTable.convertRowIndexToModel(selectedRow);
        selectedCourse = tableModel.getCourseAt(modelIdx);
        if (selectedCourse == null) return;

        lblFormMode.setText("EDITING COURSE");
        lblFormMode.setBackground(new Color(224, 237, 255));
        lblFormMode.setForeground(PRIMARY_NAVY);

        // Populate fields
        txtCourseId.setText(selectedCourse.getCourseId());
        txtCourseCode.setText(selectedCourse.getCourseCode());
        txtCourseName.setText(selectedCourse.getCourseName());
        spCredits.setValue(selectedCourse.getCredits());
        cmbDepartment.setSelectedItem(selectedCourse.getDepartment());
        taDescription.setText(selectedCourse.getDescription() != null ? selectedCourse.getDescription() : "");

        int offerings = selectedCourse.getOfferingCount();
        String offDetails = selectedCourse.getOfferingDetails();
        if (offerings > 0) {
            txtOfferingStatus.setText("Active (" + offerings + " semester offerings: " + (offDetails != null ? offDetails : "") + ")");
        } else {
            txtOfferingStatus.setText("Unscheduled (No semester offerings registered)");
        }

        // Configure button states
        btnAdd.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    public void clearForm() {
        courseTable.clearSelection();
        selectedCourse = null;

        lblFormMode.setText("NEW COURSE");
        lblFormMode.setBackground(LIGHT_BLUE);
        lblFormMode.setForeground(SECONDARY_BLUE);

        String nextId = courseService.getNextSuggestedCourseId();
        txtCourseId.setText(nextId);
        txtCourseCode.setText("");
        txtCourseName.setText("");
        spCredits.setValue(3);
        cmbDepartment.setSelectedIndex(0);
        taDescription.setText("");
        txtOfferingStatus.setText("New un-offered course module");

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        txtCourseCode.requestFocusInWindow();
    }

    private void onAddCourse() {
        Course newCourse = new Course();
        newCourse.setCourseId(txtCourseId.getText().trim());
        newCourse.setCourseCode(txtCourseCode.getText().trim());
        newCourse.setCourseName(txtCourseName.getText().trim());
        newCourse.setCredits((Integer) spCredits.getValue());
        newCourse.setDepartment((String) cmbDepartment.getSelectedItem());
        newCourse.setDescription(taDescription.getText().trim());

        OperationResult result = courseService.addCourse(newCourse);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Course Added", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadCoursesAsync();
            if (dataChangedCallback != null) dataChangedCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateCourse() {
        if (selectedCourse == null) return;

        Course toUpdate = new Course();
        toUpdate.setCourseId(txtCourseId.getText().trim());
        toUpdate.setCourseCode(txtCourseCode.getText().trim());
        toUpdate.setCourseName(txtCourseName.getText().trim());
        toUpdate.setCredits((Integer) spCredits.getValue());
        toUpdate.setDepartment((String) cmbDepartment.getSelectedItem());
        toUpdate.setDescription(taDescription.getText().trim());

        OperationResult result = courseService.updateCourse(toUpdate);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Course Updated", JOptionPane.INFORMATION_MESSAGE);
            loadCoursesAsync();
            if (dataChangedCallback != null) dataChangedCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteCourse() {
        if (selectedCourse == null) return;

        String id = selectedCourse.getCourseId();
        String code = selectedCourse.getCourseCode();
        String name = selectedCourse.getCourseName();

        // 1. Check for dependent semester offerings first
        Map<String, Integer> related = courseService.getDependentRecordsSummary(id);
        if (!related.isEmpty()) {
            StringBuilder desc = new StringBuilder("<html><body style='font-family: sans-serif; width: 340px;'>");
            desc.append("<h4 style='color: #DC2626; margin-bottom: 6px;'>Cannot Delete Course ").append(code).append("</h4>");
            desc.append("<p style='color: #4A5568; line-height: 1.4;'>");
            desc.append("Course <b>").append(code).append(" - ").append(name).append("</b> has dependent academic records:<br><br>");
            for (Map.Entry<String, Integer> entry : related.entrySet()) {
                desc.append("• <b>").append(entry.getValue()).append("</b> in <i>").append(entry.getKey()).append("</i><br>");
            }
            desc.append("<br>To maintain academic history and foreign-key constraints, courses with registered semester offerings, enrollments, or exam marks cannot be deleted.<br><br>");
            desc.append("<small style='color: #718096;'>You may edit course details or remove semester offerings first if safe.</small>");
            desc.append("</p></body></html>");

            JOptionPane.showMessageDialog(
                    this,
                    desc.toString(),
                    "Foreign Key Constraint - Deletion Blocked",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Safe deletion confirmation
        int choice = JOptionPane.showConfirmDialog(
                this,
                "<html><body style='font-family: sans-serif; width: 280px;'>" +
                "Are you sure you want to permanently delete course:<br><br>" +
                "<b>" + code + " - " + name + "</b> (" + id + ")?<br><br>" +
                "<small style='color: #DC2626;'>This action cannot be undone.</small>" +
                "</body></html>",
                "Confirm Course Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        OperationResult result = courseService.deleteCourse(id);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Course Deleted", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadCoursesAsync();
            if (dataChangedCallback != null) dataChangedCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Error Deleting Course", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getters for programmatic testing
    public JTable getCourseTable() {
        return courseTable;
    }

    public JTextField getTxtSearch() {
        return txtSearch;
    }

    public JComboBox<String> getCmbDeptFilter() {
        return cmbDeptFilter;
    }

    public JComboBox<String> getCmbStatusFilter() {
        return cmbStatusFilter;
    }

    public JTextField getTxtCourseCode() {
        return txtCourseCode;
    }

    public JTextField getTxtCourseName() {
        return txtCourseName;
    }

    public JSpinner getSpCredits() {
        return spCredits;
    }

    public void selectRow(int viewRowIdx) {
        if (viewRowIdx >= 0 && viewRowIdx < courseTable.getRowCount()) {
            courseTable.setRowSelectionInterval(viewRowIdx, viewRowIdx);
            onTableRowSelected();
        }
    }

    // INNER CLASS: TABLE MODEL

    private static class CourseTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {
                "Course ID", "Course Code", "Course Name", "Credits", "Department", "Offering Status"
        };
        private List<Course> courses = new ArrayList<>();

        public void setCourses(List<Course> courses) {
            this.courses = (courses != null) ? new ArrayList<>(courses) : new ArrayList<>();
            fireTableDataChanged();
        }

        public Course getCourseAt(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < courses.size()) {
                return courses.get(rowIndex);
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return courses.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Course c = courses.get(rowIndex);
            switch (columnIndex) {
                case 0: return c.getCourseId();
                case 1: return c.getCourseCode();
                case 2: return c.getCourseName();
                case 3: return c.getCredits();
                case 4: return c.getDepartment();
                case 5: return c.getStatus();
                default: return "";
            }
        }
    }
}
