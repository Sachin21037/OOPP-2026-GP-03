package ui.lecturer;

import dao.CourseMaterialsDAO;
import dao.CourseMaterialsDAO.CourseOfferingItem;
import dao.CourseMaterialsDAO.MaterialRecord;
import dao.CourseMaterialsDAO.MaterialsSummary;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseMaterialsUI - Presentation layer for Course Materials management.
 * Matches the official FAMS portal design and provides full DB-backed CRUD operations.
 */
public class CourseMaterialsUI extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color BUTTON_BLUE = new Color(29, 78, 216); // Royal Blue #1D4ED8
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color SLATE = new Color(100, 116, 139);

    protected String lecturerId;

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable materialsTable;

    private JComboBox<Object> courseDropdown;
    private JTextField searchField;

    private JLabel totalMaterialsValue;
    private JLabel myUploadsValue;
    private JLabel assignedCoursesValue;
    private JLabel storageLocationValue;

    private JLabel subtitleLabel;
    private JLabel tableCountLabel;

    private final List<MaterialRecord> loadedRecords = new ArrayList<>();
    private final List<CourseOfferingItem> assignedCourses = new ArrayList<>();

    private String initialCourseCodeFilter = null;

    public CourseMaterialsUI() {
        this(resolveActiveLecturerId(), null);
    }

    public CourseMaterialsUI(String lecturerId) {
        this(lecturerId, null);
    }

    public CourseMaterialsUI(String lecturerId, String preselectedCourseCode) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : resolveActiveLecturerId();
        this.initialCourseCodeFilter = preselectedCourseCode;

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Course Materials");
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
        return "LEC001";
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Course Materials", lecturerId, this);
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

        JLabel mainTitle = new JLabel("Course Materials Management");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 26));
        mainTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("Upload, manage, and share learning resources and lecture notes with enrolled students.");
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

        totalMaterialsValue = new JLabel("0");
        myUploadsValue = new JLabel("0");
        assignedCoursesValue = new JLabel("0");
        storageLocationValue = new JLabel("materials/");
        storageLocationValue.setFont(new Font("Arial", Font.BOLD, 22));

        summaryPanel.add(createSummaryCard("TOTAL MATERIALS", totalMaterialsValue, NAVY, "Across assigned courses"));
        summaryPanel.add(createSummaryCard("MY UPLOADS", myUploadsValue, TEAL, "Uploaded by you"));
        summaryPanel.add(createSummaryCard("ASSIGNED COURSES", assignedCoursesValue, PURPLE, "Courses you lecture"));
        summaryPanel.add(createSummaryCard("STORAGE LOCATION", storageLocationValue, SLATE, "Active project folder"));

        return summaryPanel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color stripeColor, String caption) {
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

        if (valueLabel != storageLocationValue) {
            valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        }
        valueLabel.setForeground(TEXT);

        JLabel capLbl = new JLabel(caption);
        capLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        capLbl.setForeground(MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
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

        // Header Row: Course Materials Catalog | X MATERIALS
        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);

        JLabel listTitle = new JLabel("Course Materials Catalog");
        listTitle.setFont(new Font("Arial", Font.BOLD, 16));
        listTitle.setForeground(TEXT);

        tableCountLabel = new JLabel("0 MATERIALS");
        tableCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        tableCountLabel.setForeground(MUTED);

        listHeader.add(listTitle, BorderLayout.WEST);
        listHeader.add(tableCountLabel, BorderLayout.EAST);
        tableContainer.add(listHeader, BorderLayout.NORTH);

        // Table (7 columns)
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
            public boolean isCellEditable(int row, int column) { return false; }
        };

        rowSorter = new TableRowSorter<>(tableModel);
        materialsTable = new JTable(tableModel);
        materialsTable.setRowSorter(rowSorter);
        materialsTable.setRowHeight(34);
        materialsTable.setFont(LecturerTheme.FONT_PLAIN_13);
        materialsTable.setGridColor(new Color(241, 245, 249));
        materialsTable.setSelectionBackground(new Color(239, 246, 255));
        materialsTable.setSelectionForeground(NAVY);
        materialsTable.setShowGrid(true);

        JTableHeader th = materialsTable.getTableHeader();
        th.setFont(LecturerTheme.FONT_BOLD_12);
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(new Color(51, 65, 85));
        th.setPreferredSize(new Dimension(0, 34));

        materialsTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        materialsTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        materialsTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        materialsTable.getColumnModel().getColumn(3).setPreferredWidth(240);
        materialsTable.getColumnModel().getColumn(4).setPreferredWidth(170);
        materialsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        materialsTable.getColumnModel().getColumn(6).setPreferredWidth(130);

        materialsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());
        materialsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer());

        DefaultTableCellRenderer leftIndentRenderer = new DefaultTableCellRenderer();
        leftIndentRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftIndentRenderer.setBorder(new EmptyBorder(0, 10, 0, 0));
        materialsTable.getColumnModel().getColumn(2).setCellRenderer(leftIndentRenderer);
        materialsTable.getColumnModel().getColumn(3).setCellRenderer(leftIndentRenderer);
        materialsTable.getColumnModel().getColumn(4).setCellRenderer(leftIndentRenderer);
        materialsTable.getColumnModel().getColumn(5).setCellRenderer(leftIndentRenderer);

        materialsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer());

        materialsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    downloadOrOpenFile();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(materialsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom Toolbar: Left Filter/Search | Right 4 Action Buttons
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Left Controls: Course [ All Courses ▾ ] Search [      ]
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftControls.setOpaque(false);

        JLabel courseLbl = new JLabel("Course:");
        courseLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        courseLbl.setForeground(TEXT);

        courseDropdown = new JComboBox<>();
        courseDropdown.setFont(LecturerTheme.FONT_PLAIN_13);
        courseDropdown.setPreferredSize(new Dimension(130, 32));
        courseDropdown.addActionListener(e -> applyFilters());

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(LecturerTheme.FONT_PLAIN_13);
        searchLbl.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(LecturerTheme.FONT_PLAIN_13);
        searchField.setPreferredSize(new Dimension(115, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        leftControls.add(courseLbl);
        leftControls.add(courseDropdown);
        leftControls.add(Box.createHorizontalStrut(4));
        leftControls.add(searchLbl);
        leftControls.add(searchField);

        // Right Buttons: [ Download ] [ Edit ] [ Delete ] [ + Upload Material ]
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightButtons.setOpaque(false);

        JButton downloadBtn = createStyledButton("Download", WHITE, TEXT, 85);
        downloadBtn.addActionListener(e -> downloadOrOpenFile());

        JButton editBtn = createStyledButton("Edit", WHITE, TEXT, 65);
        editBtn.addActionListener(e -> openEditDialog());

        JButton deleteBtn = createStyledButton("Delete", WHITE, TEXT, 70);
        deleteBtn.addActionListener(e -> deleteSelectedMaterial());

        JButton uploadBtn = createStyledButton("+ Upload Material", BUTTON_BLUE, Color.WHITE, 135);
        uploadBtn.addActionListener(e -> openUploadDialog());

        rightButtons.add(downloadBtn);
        rightButtons.add(editBtn);
        rightButtons.add(deleteBtn);
        rightButtons.add(uploadBtn);

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

    /**
     * Loads materials and assigned courses via CourseMaterialsDAO from MySQL.
     */
    public void loadDatabaseData() {
        loadedRecords.clear();
        assignedCourses.clear();
        tableModel.setRowCount(0);

        if (this.lecturerId == null || this.lecturerId.trim().isEmpty()) {
            this.lecturerId = resolveActiveLecturerId();
        }

        try {
            List<CourseOfferingItem> offerings = CourseMaterialsDAO.loadAssignedCourses(lecturerId);
            assignedCourses.addAll(offerings);

            courseDropdown.removeAllItems();
            courseDropdown.addItem("All Courses");
            int matchIndex = -1;
            int idx = 1;
            for (CourseOfferingItem item : assignedCourses) {
                courseDropdown.addItem(item);
                if (initialCourseCodeFilter != null && initialCourseCodeFilter.equalsIgnoreCase(item.courseCode)) {
                    matchIndex = idx;
                }
                idx++;
            }
            if (matchIndex > 0 && matchIndex < courseDropdown.getItemCount()) {
                courseDropdown.setSelectedIndex(matchIndex);
            }

            List<MaterialRecord> materials = CourseMaterialsDAO.loadMaterials(lecturerId);
            loadedRecords.addAll(materials);

            if (subtitleLabel != null) {
                subtitleLabel.setText("Upload, manage, and share learning resources and lecture notes with enrolled students.");
            }
        } catch (SQLException e) {
            System.err.println("[CourseMaterialsUI] Error loading materials: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error: " + e.getMessage());
            }
        }

        applyFilters();
    }

    public void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        Object sel = courseDropdown != null ? courseDropdown.getSelectedItem() : null;
        String filterCode = null;
        if (sel instanceof CourseOfferingItem) {
            filterCode = ((CourseOfferingItem) sel).courseCode;
        } else if (sel != null && !sel.toString().equals("All Courses")) {
            filterCode = sel.toString();
        }

        String search = searchField != null ? searchField.getText().trim() : "";

        List<MaterialRecord> filtered = CourseMaterialsDAO.filterMaterials(loadedRecords, filterCode, search);

        for (MaterialRecord m : filtered) {
            tableModel.addRow(m.toTableRow());
        }

        if (tableCountLabel != null) {
            tableCountLabel.setText(filtered.size() + " MATERIALS");
        }

        MaterialsSummary summary = CourseMaterialsDAO.calculateSummary(filtered, assignedCourses.size(), lecturerId);
        if (totalMaterialsValue != null) totalMaterialsValue.setText(String.valueOf(summary.totalMaterials));
        if (myUploadsValue != null) myUploadsValue.setText(String.valueOf(summary.myUploads));
        if (assignedCoursesValue != null) assignedCoursesValue.setText(String.valueOf(summary.totalCourses));
        if (storageLocationValue != null) storageLocationValue.setText(summary.storageLocation);
    }

    private void openUploadDialog() {
        if (assignedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No assigned course offerings available to attach materials.",
                    "Unavailable",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Upload Course Material", true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(WHITE);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 14));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JComboBox<CourseOfferingItem> offCombo = new JComboBox<>(assignedCourses.toArray(new CourseOfferingItem[0]));
        offCombo.setFont(LecturerTheme.FONT_PLAIN_13);

        JTextField titleField = new JTextField();
        titleField.setFont(LecturerTheme.FONT_PLAIN_13);

        JTextField descField = new JTextField();
        descField.setFont(LecturerTheme.FONT_PLAIN_13);

        JPanel fileRow = new JPanel(new BorderLayout(6, 0));
        fileRow.setOpaque(false);
        JLabel fileLbl = new JLabel("No file selected");
        fileLbl.setFont(LecturerTheme.FONT_PLAIN_12);
        fileLbl.setForeground(MUTED);

        JButton chooseBtn = createStyledButton("Browse...", WHITE, TEXT, 85);
        final File[] chosenFile = new File[1];
        chooseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                chosenFile[0] = fc.getSelectedFile();
                fileLbl.setText(chosenFile[0].getName());
                fileLbl.setForeground(TEXT);
            }
        });
        fileRow.add(fileLbl, BorderLayout.CENTER);
        fileRow.add(chooseBtn, BorderLayout.EAST);

        form.add(makeFieldLabel("Course Offering:"));
        form.add(offCombo);
        form.add(makeFieldLabel("Document Title:"));
        form.add(titleField);
        form.add(makeFieldLabel("Description / Topic:"));
        form.add(descField);
        form.add(makeFieldLabel("Attachment File:"));
        form.add(fileRow);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(WHITE);
        btnPanel.setBorder(new EmptyBorder(0, 0, 10, 20));

        JButton cancelBtn = createStyledButton("Cancel", WHITE, TEXT, 85);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Upload & Save", BUTTON_BLUE, Color.WHITE, 130);
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a material title.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CourseOfferingItem off = (CourseOfferingItem) offCombo.getSelectedItem();
            String offId = off != null ? off.offeringId : "";

            try {
                String newId = CourseMaterialsDAO.saveMaterial(offId, title, descField.getText().trim(), chosenFile[0], lecturerId);
                JOptionPane.showMessageDialog(dialog, "Material '" + newId + "' uploaded successfully.", "Upload Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDatabaseData();
            } catch (SQLException | IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to upload material: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        int row = materialsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a material from the table to edit.",
                    "Edit Material",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = materialsTable.convertRowIndexToModel(row);
        String mid = String.valueOf(tableModel.getValueAt(modelRow, 0));
        MaterialRecord rec = null;
        for (MaterialRecord m : loadedRecords) {
            if (m.materialId.equalsIgnoreCase(mid)) {
                rec = m;
                break;
            }
        }
        if (rec == null) return;

        JDialog dialog = new JDialog(this, "Edit Material - " + mid, true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(WHITE);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 14));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JComboBox<CourseOfferingItem> offCombo = new JComboBox<>(assignedCourses.toArray(new CourseOfferingItem[0]));
        offCombo.setFont(LecturerTheme.FONT_PLAIN_13);
        for (int i = 0; i < offCombo.getItemCount(); i++) {
            if (offCombo.getItemAt(i).offeringId.equalsIgnoreCase(rec.offeringId)) {
                offCombo.setSelectedIndex(i);
                break;
            }
        }

        JTextField titleField = new JTextField(rec.title);
        titleField.setFont(LecturerTheme.FONT_PLAIN_13);

        JTextField descField = new JTextField("-".equals(rec.description) ? "" : rec.description);
        descField.setFont(LecturerTheme.FONT_PLAIN_13);

        JPanel fileRow = new JPanel(new BorderLayout(6, 0));
        fileRow.setOpaque(false);
        JLabel fileLbl = new JLabel(rec.fileName.isEmpty() ? "No file attached" : rec.fileName);
        fileLbl.setFont(LecturerTheme.FONT_PLAIN_12);
        fileLbl.setForeground(TEXT);

        JButton chooseBtn = createStyledButton("Replace...", WHITE, TEXT, 85);
        final File[] chosenFile = new File[1];
        chooseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                chosenFile[0] = fc.getSelectedFile();
                fileLbl.setText(chosenFile[0].getName());
            }
        });
        fileRow.add(fileLbl, BorderLayout.CENTER);
        fileRow.add(chooseBtn, BorderLayout.EAST);

        form.add(makeFieldLabel("Course Offering:"));
        form.add(offCombo);
        form.add(makeFieldLabel("Document Title:"));
        form.add(titleField);
        form.add(makeFieldLabel("Description:"));
        form.add(descField);
        form.add(makeFieldLabel("Attachment:"));
        form.add(fileRow);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(WHITE);
        btnPanel.setBorder(new EmptyBorder(0, 0, 10, 20));

        JButton cancelBtn = createStyledButton("Cancel", WHITE, TEXT, 85);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Save Changes", BUTTON_BLUE, Color.WHITE, 120);
        final String targetMid = mid;
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a material title.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CourseOfferingItem off = (CourseOfferingItem) offCombo.getSelectedItem();
            String offId = off != null ? off.offeringId : "";

            try {
                boolean ok = CourseMaterialsDAO.updateMaterial(targetMid, offId, title, descField.getText().trim(), chosenFile[0], lecturerId);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Material updated successfully.", "Update Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadDatabaseData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update material in database.", "Update Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to update material: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedMaterial() {
        int row = materialsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a material from the table to delete.",
                    "Delete Material",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = materialsTable.convertRowIndexToModel(row);
        String mid = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String title = String.valueOf(tableModel.getValueAt(modelRow, 2));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete material:\n" + mid + " - " + title + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = CourseMaterialsDAO.deleteMaterial(mid, lecturerId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Material '" + mid + "' deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadDatabaseData();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete material.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadOrOpenFile() {
        int row = materialsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a material from the table to download.",
                    "Download Material",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = materialsTable.convertRowIndexToModel(row);
        String mid = String.valueOf(tableModel.getValueAt(modelRow, 0));

        for (MaterialRecord m : loadedRecords) {
            if (m.materialId.equalsIgnoreCase(mid)) {
                if (m.filePath == null || m.filePath.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No file attachment found for this material.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                File f = CourseMaterialsDAO.resolveFile(m.filePath);
                if (f != null && f.exists()) {
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogTitle("Save Copy of Material");
                    fc.setSelectedFile(new File(m.fileName.isEmpty() ? f.getName() : m.fileName));

                    int userSelection = fc.showSaveDialog(this);
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File dest = fc.getSelectedFile();
                        try {
                            Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            int openChoice = JOptionPane.showConfirmDialog(
                                    this,
                                    "File downloaded successfully to:\n" + dest.getAbsolutePath() + "\n\nWould you like to open it now?",
                                    "Download Complete",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            if (openChoice == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(dest);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Attachment file is not currently found in local storage:\n" + m.filePath,
                            "File Missing",
                            JOptionPane.WARNING_MESSAGE);
                }
                return;
            }
        }
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LecturerTheme.FONT_BOLD_12);
        l.setForeground(MUTED);
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
            new CourseMaterialsUI(testId).setVisible(true);
        });
    }
}
