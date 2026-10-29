package ui;

import database.DBConnection;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lecturer Course Materials Management UI for Faculty Academic Management System (FAMS).
 * Allows authenticated lecturers to view, upload, edit, delete, open, and download course materials
 * for courses assigned to them.
 */
public class CourseMaterials extends JFrame {

    // Theme colours matching existing Lecturer pages
    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color RED = LecturerTheme.DANGER_RED;
    private static final Color GREEN = LecturerTheme.SUCCESS_GREEN;

    private static final Font BODY = new Font("Arial", Font.PLAIN, 13);
    private static final String STORAGE_DIR = "materials";

    // Active lecturer identifier
    private String lecturerId;
    private String initialCourseCodeFilter;

    // UI Components
    private JTable materialsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    private JComboBox<Object> courseDropdown;
    private JTextField searchField;

    private JLabel totalMaterialsValue;
    private JLabel myUploadsValue;
    private JLabel coursesValue;
    private JLabel storageValue;
    private JLabel recordCountLabel;
    private JLabel emptyNoticeLabel;
    private JLabel subtitleLabel;

    // In-memory model representing actual database records
    private final List<CourseOfferingItem> assignedCourses = new ArrayList<>();
    private final List<MaterialRecord> loadedRecords = new ArrayList<>();
    private final List<MaterialRecord> displayedRecords = new ArrayList<>();

    /**
     * Helper model for course offerings assigned to the lecturer.
     */
    public static class CourseOfferingItem {
        public final String offeringId;
        public final String courseCode;
        public final String courseName;
        public final String academicYear;
        public final String semester;

        public CourseOfferingItem(String offeringId, String courseCode, String courseName, String academicYear, String semester) {
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.semester = semester != null ? semester : "";
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    /**
     * Helper model for course materials retrieved from MariaDB.
     */
    public static class MaterialRecord {
        public final String materialId;
        public final String offeringId;
        public final String courseCode;
        public final String courseName;
        public final String lecturerId;
        public final String lecturerName;
        public final String title;
        public final String description;
        public final String fileName;
        public final String filePath;
        public final String uploadedAt;

        public MaterialRecord(String materialId, String offeringId, String courseCode, String courseName,
                              String lecturerId, String lecturerName, String title, String description,
                              String fileName, String filePath, String uploadedAt) {
            this.materialId = materialId != null ? materialId : "";
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.lecturerId = lecturerId != null ? lecturerId : "";
            this.lecturerName = (lecturerName != null && !lecturerName.trim().isEmpty()) ? lecturerName.trim() : this.lecturerId;
            this.title = title != null ? title : "";
            this.description = description != null ? description : "";
            this.fileName = fileName != null ? fileName : "";
            this.filePath = filePath != null ? filePath : "";
            this.uploadedAt = uploadedAt != null ? uploadedAt : "";
        }

        public Object[] toTableRow() {
            return new Object[]{
                    materialId,
                    courseCode,
                    title,
                    description != null && !description.isEmpty() ? description : "-",
                    fileName != null && !fileName.isEmpty() ? fileName : "-",
                    lecturerName,
                    uploadedAt
            };
        }
    }

    /**
     * Default constructor: resolves active lecturer ID dynamically.
     */
    public CourseMaterials() {
        this(resolveActiveLecturerId(), null);
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     */
    public CourseMaterials(String lecturerId) {
        this(lecturerId, null);
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID and initial course code filter.
     */
    public CourseMaterials(String lecturerId, String preselectedCourseCode) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;
        this.initialCourseCodeFilter = preselectedCourseCode;

        if (this.lecturerId != null) {
            LecturerDashboard.setActiveLecturerId(this.lecturerId);
        }

        // Ensure materials storage directory exists
        File storageDir = new File(STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        setTitle("Faculty Academic Management System - Course Materials");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        // Load courses and real materials from MariaDB
        loadDatabaseData();
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
        return LecturerTheme.createSidebar("Course Materials", lecturerId, this);
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(27, 30, 28, 30));

        // Heading
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(BACKGROUND);

        JPanel headingText = new JPanel();
        headingText.setBackground(BACKGROUND);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));

        JLabel pageTitle = new JLabel("Course Materials Management");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("Upload, manage, and share learning resources and lecture notes with enrolled students.");
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

        // 4 Summary Cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryPanel.setBackground(BACKGROUND);

        totalMaterialsValue = new JLabel("0");
        myUploadsValue = new JLabel("0");
        coursesValue = new JLabel("0");
        storageValue = new JLabel(STORAGE_DIR + "/");

        summaryPanel.add(createSummaryCard("TOTAL MATERIALS", totalMaterialsValue, NAVY, "Across assigned courses"));
        summaryPanel.add(createSummaryCard("MY UPLOADS", myUploadsValue, TEAL, "Uploaded by you"));
        summaryPanel.add(createSummaryCard("ASSIGNED COURSES", coursesValue, PURPLE, "Courses you lecture"));
        summaryPanel.add(createSummaryCard("STORAGE LOCATION", storageValue, new Color(48, 77, 111), "Active project folder"));

        body.add(summaryPanel, BorderLayout.NORTH);

        // Main Table Card
        JPanel tableCard = new JPanel(new BorderLayout(0, 16));
        tableCard.setBackground(WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 16, 18)
        ));

        // Table Header row
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(WHITE);

        JLabel tableTitle = new JLabel("Course Materials Catalog");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 18));
        tableTitle.setForeground(TEXT);

        recordCountLabel = new JLabel("0 MATERIALS");
        recordCountLabel.setFont(new Font("Arial", Font.BOLD, 10));
        recordCountLabel.setForeground(MUTED);

        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(recordCountLabel, BorderLayout.EAST);
        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table Model
        String[] columns = {
                "Material ID",
                "Course",
                "Title",
                "Description",
                "File Name",
                "Uploaded By",
                "Upload Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        materialsTable = new JTable(tableModel);
        materialsTable.setRowHeight(38);
        materialsTable.setFont(BODY);
        materialsTable.setForeground(TEXT);
        materialsTable.setBackground(WHITE);
        materialsTable.setGridColor(BORDER);
        materialsTable.setShowVerticalLines(false);
        materialsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialsTable.setSelectionBackground(new Color(226, 238, 249));
        materialsTable.setSelectionForeground(TEXT);
        materialsTable.setFillsViewportHeight(true);
        materialsTable.setIntercellSpacing(new Dimension(0, 1));

        tableSorter = new TableRowSorter<>(tableModel);
        materialsTable.setRowSorter(tableSorter);

        JTableHeader th = materialsTable.getTableHeader();
        th.setFont(new Font("Arial", Font.BOLD, 11));
        th.setBackground(new Color(237, 242, 247));
        th.setForeground(NAVY);
        th.setPreferredSize(new Dimension(0, 38));
        th.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        materialsTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        materialsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer());
        materialsTable.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        materialsTable.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);
        materialsTable.getColumnModel().getColumn(4).setCellRenderer(leftRenderer);
        materialsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer());
        materialsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer());

        materialsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        materialsTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        materialsTable.getColumnModel().getColumn(2).setPreferredWidth(210);
        materialsTable.getColumnModel().getColumn(3).setPreferredWidth(230);
        materialsTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        materialsTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        materialsTable.getColumnModel().getColumn(6).setPreferredWidth(130);

        // Double-click row to open or download material
        materialsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && materialsTable.getSelectedRow() >= 0) {
                    openOrDownloadSelectedMaterial();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(materialsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        // Center container with empty state notice
        JPanel tableCenter = new JPanel(new BorderLayout());
        tableCenter.setBackground(WHITE);
        tableCenter.add(scrollPane, BorderLayout.CENTER);

        emptyNoticeLabel = new JLabel("No course materials found for this lecturer.", SwingConstants.CENTER);
        emptyNoticeLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        emptyNoticeLabel.setForeground(MUTED);
        emptyNoticeLabel.setBorder(new EmptyBorder(16, 0, 16, 0));
        emptyNoticeLabel.setVisible(false);
        tableCenter.add(emptyNoticeLabel, BorderLayout.SOUTH);

        tableCard.add(tableCenter, BorderLayout.CENTER);

        // Bottom Controls: Filters (left) & Actions (right)
        JPanel bottomTools = new JPanel(new BorderLayout(12, 0));
        bottomTools.setBackground(WHITE);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setBackground(WHITE);

        courseDropdown = new JComboBox<>();
        courseDropdown.setFont(BODY);
        courseDropdown.setPreferredSize(new Dimension(210, 34));
        courseDropdown.setBackground(WHITE);
        courseDropdown.addItem("All Courses");

        searchField = new JTextField(15);
        searchField.setFont(BODY);
        searchField.setPreferredSize(new Dimension(170, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));
        searchField.setToolTipText("Search title, description, or file name");

        filters.add(new JLabel("Course:"));
        filters.add(courseDropdown);
        filters.add(Box.createHorizontalStrut(6));
        filters.add(new JLabel("Search:"));
        filters.add(searchField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(WHITE);

        JButton refreshBtn = createActionButton("Refresh", false, 85);
        JButton openDownloadBtn = createActionButton("Open / Download", false, 135);
        JButton editBtn = createActionButton("Edit", false, 75);
        JButton deleteBtn = createActionButton("Delete", false, 80);
        JButton uploadBtn = createActionButton("+ Upload Material", true, 140);

        actions.add(refreshBtn);
        actions.add(openDownloadBtn);
        actions.add(editBtn);
        actions.add(deleteBtn);
        actions.add(uploadBtn);

        bottomTools.add(filters, BorderLayout.WEST);
        bottomTools.add(actions, BorderLayout.EAST);
        tableCard.add(bottomTools, BorderLayout.SOUTH);

        body.add(tableCard, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);

        // Wire Event Listeners
        courseDropdown.addActionListener(e -> applyFilters());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        refreshBtn.addActionListener(e -> {
            loadMaterialsFromDatabase();
            JOptionPane.showMessageDialog(this, "Course materials refreshed from database.", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });

        openDownloadBtn.addActionListener(e -> openOrDownloadSelectedMaterial());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelectedMaterial());
        uploadBtn.addActionListener(e -> openUploadDialog());

        return content;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
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
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 10));
        titleLbl.setForeground(MUTED);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(TEXT);

        JLabel captionLbl = new JLabel(caption);
        captionLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        captionLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(captionLbl, BorderLayout.SOUTH);

        return card;
    }

    private DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    private JButton createActionButton(String text, boolean primary, int width) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(primary ? WHITE : TEXT);
        button.setBackground(primary ? LecturerTheme.SECONDARY_BLUE : WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, 34));

        button.setBorder(primary
                ? BorderFactory.createEmptyBorder(7, 10, 7, 10)
                : BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 9, 6, 9)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (primary) {
                    button.setBackground(new Color(26, 68, 190));
                } else {
                    button.setBackground(new Color(245, 248, 252));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primary ? LecturerTheme.SECONDARY_BLUE : WHITE);
            }
        });

        return button;
    }

    // ================= DATABASE INTEGRATION =================

    private void loadDatabaseData() {
        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        populateAssignedCourses();
        loadMaterialsFromDatabase();
    }

    private void populateAssignedCourses() {
        assignedCourses.clear();
        courseDropdown.removeAllItems();
        courseDropdown.addItem("All Courses");

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            return;
        }

        String sql = "SELECT DISTINCT co.offering_id, c.course_code, c.course_name, co.academic_year, co.semester " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code";

        int matchIndex = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                int idx = 1;
                while (rs.next()) {
                    String offId = rs.getString("offering_id");
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    String year = rs.getString("academic_year");
                    String sem = rs.getString("semester");

                    CourseOfferingItem item = new CourseOfferingItem(offId, code, name, year, sem);
                    assignedCourses.add(item);
                    courseDropdown.addItem(item);

                    if (initialCourseCodeFilter != null && initialCourseCodeFilter.equalsIgnoreCase(code)) {
                        matchIndex = idx;
                    }
                    idx++;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CourseMaterials] Error populating assigned courses: " + e.getMessage());
            showDbError("Failed to load assigned courses from database.", e);
        }

        if (matchIndex > 0 && matchIndex < courseDropdown.getItemCount()) {
            courseDropdown.setSelectedIndex(matchIndex);
        }

        coursesValue.setText(String.valueOf(assignedCourses.size()));
    }

    private void loadMaterialsFromDatabase() {
        loadedRecords.clear();

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            updateSummary(new ArrayList<>());
            return;
        }

        // Retrieve materials for offerings assigned to this lecturer
        String sql = "SELECT cm.material_id, cm.offering_id, c.course_code, c.course_name, " +
                "       cm.lecturer_id, u.first_name, u.last_name, " +
                "       cm.title, cm.description, cm.file_name, cm.file_path, cm.uploaded_at " +
                "FROM course_materials cm " +
                "JOIN course_offerings co ON cm.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                "LEFT JOIN users u ON cm.lecturer_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY cm.uploaded_at DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String matId = rs.getString("material_id");
                    String offId = rs.getString("offering_id");
                    String code = rs.getString("course_code");
                    String cname = rs.getString("course_name");
                    String lecId = rs.getString("lecturer_id");
                    String fn = rs.getString("first_name");
                    String ln = rs.getString("last_name");
                    String lecName = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
                    if (lecName.isEmpty()) lecName = lecId;

                    String title = rs.getString("title");
                    String desc = rs.getString("description");
                    String fileName = rs.getString("file_name");
                    String filePath = rs.getString("file_path");

                    java.sql.Timestamp ts = rs.getTimestamp("uploaded_at");
                    String uploadDate = ts != null ? sdf.format(new Date(ts.getTime())) : "-";

                    loadedRecords.add(new MaterialRecord(
                            matId, offId, code, cname, lecId, lecName, title, desc, fileName, filePath, uploadDate
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CourseMaterials] Error querying materials: " + e.getMessage());
            showDbError("Failed to load course materials from database.", e);
            return;
        }

        applyFilters();
    }

    private void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        displayedRecords.clear();

        Object selObj = courseDropdown != null ? courseDropdown.getSelectedItem() : null;
        String filterOfferingId = null;
        if (selObj instanceof CourseOfferingItem) {
            filterOfferingId = ((CourseOfferingItem) selObj).offeringId;
        }

        String query = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";

        List<MaterialRecord> filtered = new ArrayList<>();

        for (MaterialRecord r : loadedRecords) {
            boolean matchesCourse = (filterOfferingId == null) || r.offeringId.equalsIgnoreCase(filterOfferingId);
            boolean matchesSearch = query.isEmpty()
                    || r.title.toLowerCase(Locale.ROOT).contains(query)
                    || r.description.toLowerCase(Locale.ROOT).contains(query)
                    || r.fileName.toLowerCase(Locale.ROOT).contains(query)
                    || r.courseCode.toLowerCase(Locale.ROOT).contains(query)
                    || r.materialId.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesSearch) {
                filtered.add(r);
                displayedRecords.add(r);
                tableModel.addRow(r.toTableRow());
            }
        }

        updateSummary(filtered);
    }

    private void updateSummary(List<MaterialRecord> currentList) {
        int count = currentList != null ? currentList.size() : 0;

        if (recordCountLabel != null) {
            recordCountLabel.setText(count + " MATERIALS");
        }

        if (count == 0) {
            if (emptyNoticeLabel != null) {
                if (loadedRecords.isEmpty()) {
                    emptyNoticeLabel.setText("No course materials uploaded yet. Click '+ Upload Material' to add one.");
                } else {
                    emptyNoticeLabel.setText("No materials match your filter / search query.");
                }
                emptyNoticeLabel.setVisible(true);
            }
        } else {
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setVisible(false);
            }
        }

        int myUploads = 0;
        for (MaterialRecord r : loadedRecords) {
            if (r.lecturerId.equalsIgnoreCase(this.lecturerId)) {
                myUploads++;
            }
        }

        if (totalMaterialsValue != null) totalMaterialsValue.setText(String.valueOf(loadedRecords.size()));
        if (myUploadsValue != null) myUploadsValue.setText(String.valueOf(myUploads));
        if (coursesValue != null) coursesValue.setText(String.valueOf(assignedCourses.size()));
        if (storageValue != null) storageValue.setText(STORAGE_DIR + "/");
    }

    // ================= CRUD ACTIONS =================

    /**
     * Upload a new course material: file chooser, metadata entry, and database persistence.
     */
    private void openUploadDialog() {
        if (assignedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No assigned courses found for lecturer " + lecturerId + ".\n" +
                    "You can only upload materials to courses assigned to you.",
                    "Upload Material",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JPanel form = new JPanel(new BorderLayout(10, 14));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel fields = new JPanel(new GridLayout(0, 2, 10, 10));

        JComboBox<CourseOfferingItem> offeringCombo = new JComboBox<>(assignedCourses.toArray(new CourseOfferingItem[0]));
        Object currentSel = courseDropdown.getSelectedItem();
        if (currentSel instanceof CourseOfferingItem) {
            offeringCombo.setSelectedItem(currentSel);
        }

        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        JLabel fileChosenLabel = new JLabel("No file selected", SwingConstants.LEFT);
        fileChosenLabel.setForeground(MUTED);
        fileChosenLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JButton browseBtn = new JButton("Choose File...");
        browseBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        browseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final File[] selectedFile = new File[1];

        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Course Material File");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int opt = chooser.showOpenDialog(this);
            if (opt == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (f != null && f.exists()) {
                    selectedFile[0] = f;
                    fileChosenLabel.setText(f.getName() + " (" + formatFileSize(f.length()) + ")");
                    fileChosenLabel.setForeground(TEXT);
                    if (titleField.getText().trim().isEmpty()) {
                        String name = f.getName();
                        int dot = name.lastIndexOf('.');
                        titleField.setText(dot > 0 ? name.substring(0, dot) : name);
                    }
                }
            }
        });

        JPanel fileChooserRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        fileChooserRow.add(browseBtn);
        fileChooserRow.add(fileChosenLabel);

        fields.add(new JLabel("Course Offering: *"));
        fields.add(offeringCombo);

        fields.add(new JLabel("Material Title: *"));
        fields.add(titleField);

        fields.add(new JLabel("Select File: *"));
        fields.add(fileChooserRow);

        fields.add(new JLabel("Description:"));
        fields.add(descScroll);

        form.add(fields, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Upload Course Material",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            CourseOfferingItem chosenOffering = (CourseOfferingItem) offeringCombo.getSelectedItem();
            if (chosenOffering == null) {
                JOptionPane.showMessageDialog(this, "Please select an assigned course offering.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isOfferingAssignedToLecturer(chosenOffering.offeringId)) {
                JOptionPane.showMessageDialog(this, "You are not assigned to this course offering. Upload denied.", "Unauthorized Course", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a material title.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedFile[0] == null || !selectedFile[0].exists()) {
                JOptionPane.showMessageDialog(this, "Please choose a valid file to upload.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String desc = descArea.getText().trim();

            // Persist material file and database metadata
            saveMaterial(chosenOffering.offeringId, title, desc, selectedFile[0]);
        }
    }

    private void saveMaterial(String offeringId, String title, String description, File sourceFile) {
        String insertSql = "INSERT INTO course_materials (material_id, offering_id, lecturer_id, title, description, file_name, file_path, uploaded_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String nextId = getNextMaterialId(conn);

                // Prepare storage directory
                File dir = new File(STORAGE_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String origName = sourceFile.getName();
                String cleanName = origName.replaceAll("[^a-zA-Z0-9._-]", "_");
                String destFileName = nextId + "_" + cleanName;
                File destFile = new File(dir, destFileName);

                // Copy file safely
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String relativePath = STORAGE_DIR + "/" + destFileName;

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, nextId);
                    ps.setString(2, offeringId);
                    ps.setString(3, lecturerId);
                    ps.setString(4, title);
                    ps.setString(5, description.isEmpty() ? null : description);
                    ps.setString(6, origName);
                    ps.setString(7, relativePath);
                    ps.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(
                        this,
                        "Course material uploaded successfully!\n\n" +
                        "Material ID: " + nextId + "\n" +
                        "Title: " + title + "\n" +
                        "File: " + origName + "\n" +
                        "Saved Path: " + relativePath,
                        "Upload Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadMaterialsFromDatabase();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("[CourseMaterials] Error uploading material: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to upload course material: " + e.getMessage(), "Upload Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Edit existing material title and description (only allowed if uploaded by active lecturer).
     */
    private void openEditDialog() {
        MaterialRecord rec = getSelectedRecord();
        if (rec == null) {
            JOptionPane.showMessageDialog(this, "Please select a material from the table to edit.", "Edit Material", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verify that this material was uploaded by the active lecturer
        if (!rec.lecturerId.equalsIgnoreCase(this.lecturerId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Permission Denied: You can only edit materials that you uploaded.\n" +
                    "This material was uploaded by lecturer " + rec.lecturerId + ".",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!isOfferingAssignedToLecturer(rec.offeringId)) {
            JOptionPane.showMessageDialog(this, "You are not assigned to this course offering. Edit denied.", "Unauthorized Course", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField titleField = new JTextField(rec.title, 20);
        JTextArea descArea = new JTextArea(rec.description, 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        form.add(new JLabel("Material ID:"));
        form.add(new JLabel("<html><b>" + rec.materialId + "</b></html>"));

        form.add(new JLabel("Course:"));
        form.add(new JLabel(rec.courseCode + " - " + rec.courseName));

        form.add(new JLabel("File Name:"));
        form.add(new JLabel(rec.fileName));

        form.add(new JLabel("Title: *"));
        form.add(titleField);

        form.add(new JLabel("Description:"));
        form.add(descScroll);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit Course Material - " + rec.materialId,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newTitle = titleField.getText().trim();
            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Material title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newDesc = descArea.getText().trim();

            String updateSql = "UPDATE course_materials SET title = ?, description = ? WHERE material_id = ? AND lecturer_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {

                ps.setString(1, newTitle);
                ps.setString(2, newDesc.isEmpty() ? null : newDesc);
                ps.setString(3, rec.materialId);
                ps.setString(4, lecturerId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Course material updated successfully.", "Edit Material", JOptionPane.INFORMATION_MESSAGE);
                    loadMaterialsFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "No record was updated. Please verify lecturer permissions.", "Update Failed", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                showDbError("Failed to update course material.", e);
            }
        }
    }

    /**
     * Delete material after confirmation (only allowed for active lecturer's own materials).
     */
    private void deleteSelectedMaterial() {
        MaterialRecord rec = getSelectedRecord();
        if (rec == null) {
            JOptionPane.showMessageDialog(this, "Please select a material from the table to delete.", "Delete Material", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!rec.lecturerId.equalsIgnoreCase(this.lecturerId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Permission Denied: You cannot delete materials uploaded by other lecturers (" + rec.lecturerId + ").",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!isOfferingAssignedToLecturer(rec.offeringId)) {
            JOptionPane.showMessageDialog(this, "You are not assigned to this course offering. Delete denied.", "Unauthorized Course", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this course material?\n\n" +
                "Material ID: " + rec.materialId + "\n" +
                "Title: " + rec.title + "\n" +
                "File: " + rec.fileName + "\n" +
                "Course: " + rec.courseCode + "\n\n" +
                "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer == JOptionPane.YES_OPTION) {
            String deleteSql = "DELETE FROM course_materials WHERE material_id = ? AND lecturer_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteSql)) {

                ps.setString(1, rec.materialId);
                ps.setString(2, lecturerId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Also clean up physical file from disk if present
                    File f = resolveFile(rec.filePath);
                    if (f != null && f.exists()) {
                        f.delete();
                    }

                    JOptionPane.showMessageDialog(this, "Course material deleted successfully.", "Delete Material", JOptionPane.INFORMATION_MESSAGE);
                    loadMaterialsFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed. Verify permissions.", "Delete Failed", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                showDbError("Failed to delete course material from database.", e);
            }
        }
    }

    /**
     * View, open with system default application, or download/save as.
     */
    private void openOrDownloadSelectedMaterial() {
        MaterialRecord rec = getSelectedRecord();
        if (rec == null) {
            JOptionPane.showMessageDialog(this, "Please select a material from the table first.", "Open / Download", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File file = resolveFile(rec.filePath);
        boolean fileExists = (file != null && file.exists());

        JPanel details = new JPanel(new GridLayout(0, 2, 8, 6));
        details.setBorder(new EmptyBorder(10, 10, 10, 10));

        details.add(new JLabel("Material ID:"));
        details.add(new JLabel("<html><b>" + rec.materialId + "</b></html>"));

        details.add(new JLabel("Course:"));
        details.add(new JLabel(rec.courseCode + " - " + rec.courseName));

        details.add(new JLabel("Title:"));
        details.add(new JLabel("<html><b>" + rec.title + "</b></html>"));

        details.add(new JLabel("Uploaded By:"));
        details.add(new JLabel(rec.lecturerName + " (" + rec.lecturerId + ")"));

        details.add(new JLabel("Upload Date:"));
        details.add(new JLabel(rec.uploadedAt));

        details.add(new JLabel("File Name:"));
        details.add(new JLabel(rec.fileName));

        details.add(new JLabel("File Status:"));
        if (fileExists) {
            details.add(new JLabel("<html><font color='#0D9488'><b>Available on disk</b> (" + formatFileSize(file.length()) + ")</font></html>"));
        } else {
            details.add(new JLabel("<html><font color='#E53E3E'><b>File not found on disk</b></font></html>"));
        }

        details.add(new JLabel("Storage Path:"));
        details.add(new JLabel(rec.filePath));

        if (!rec.description.isEmpty()) {
            details.add(new JLabel("Description:"));
            details.add(new JLabel("<html><i>" + rec.description + "</i></html>"));
        }

        Object[] options;
        if (fileExists) {
            options = new Object[]{"Open File", "Download / Save As...", "Close"};
        } else {
            options = new Object[]{"Close"};
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                details,
                "Material Details - " + rec.title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (fileExists) {
            if (choice == 0) {
                // Open File
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                        Desktop.getDesktop().open(file);
                    } else {
                        JOptionPane.showMessageDialog(this, "Desktop integration is not supported on this platform.", "Open File", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not open file: " + ex.getMessage(), "Open Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (choice == 1) {
                // Download / Save As
                JFileChooser saveChooser = new JFileChooser();
                saveChooser.setDialogTitle("Save Material Copy");
                saveChooser.setSelectedFile(new File(rec.fileName));
                int sopt = saveChooser.showSaveDialog(this);
                if (sopt == JFileChooser.APPROVE_OPTION) {
                    File dest = saveChooser.getSelectedFile();
                    try {
                        Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(
                                this,
                                "File saved successfully to:\n" + dest.getAbsolutePath(),
                                "Download Complete",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage(), "Download Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private MaterialRecord getSelectedRecord() {
        int selectedRow = materialsTable.getSelectedRow();
        if (selectedRow < 0) return null;
        int modelRow = materialsTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < displayedRecords.size()) {
            return displayedRecords.get(modelRow);
        }
        return null;
    }

    // ================= HELPERS =================

    private boolean isOfferingAssignedToLecturer(String offeringId) {
        if (offeringId == null || lecturerId == null) return false;
        for (CourseOfferingItem item : assignedCourses) {
            if (item.offeringId.equalsIgnoreCase(offeringId)) {
                return true;
            }
        }
        String sql = "SELECT 1 FROM course_lecturers WHERE offering_id = ? AND lecturer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, offeringId);
            ps.setString(2, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static String getNextMaterialId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(material_id, 4) AS UNSIGNED)), 0) + 1 FROM course_materials";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                long nextNum = rs.getLong(1);
                return String.format(Locale.US, "MAT%03d", nextNum);
            }
        }
        return "MAT001";
    }

    private static File resolveFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return null;
        File f = new File(filePath.trim());
        if (f.isAbsolute() && f.exists()) {
            return f;
        }
        File rel = new File(System.getProperty("user.dir"), filePath.trim());
        if (rel.exists()) {
            return rel;
        }
        return f;
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    private void showDbError(String message, SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n\nError: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // ================= MAIN METHOD FOR TESTING =================

    public static void main(String[] args) {
        String testId = args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboard.setActiveLecturerId(testId);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new CourseMaterials(testId).setVisible(true);
        });
    }
}
