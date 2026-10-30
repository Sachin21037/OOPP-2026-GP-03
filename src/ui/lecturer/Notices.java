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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Notice Board screen for Lecturer portal.
 * Displays official academic notices with visibility handling (active/all)
 * and read-only access.
 */
public class Notices extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color RED = LecturerTheme.DANGER_RED;

    private JTable noticesTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> statusFilter;
    private JTextField searchField;

    private JLabel activeNoticesValue;
    private JLabel recentNoticesValue;
    private JLabel totalNoticesValue;
    private JLabel subtitleLabel;
    private JLabel tableHintLabel;
    private JLabel emptyNoticeLabel;

    // Active lecturer ID
    private String lecturerId;

    // Loaded and displayed records
    private final List<NoticeItem> loadedNotices = new ArrayList<>();
    private final List<NoticeItem> displayedNotices = new ArrayList<>();

    /**
     * Inner helper model representing a notice entry.
     */
    public static class NoticeItem {
        public final String noticeId;
        public final String title;
        public final String description;
        public final String noticeDate;
        public final String createdBy;
        public final String creatorName;
        public final String status;

        public NoticeItem(String noticeId, String title, String description,
                          String noticeDate, String createdBy, String creatorName, String status) {
            this.noticeId = noticeId != null ? noticeId : "";
            this.title = title != null ? title : "";
            this.description = description != null ? description : "";
            this.noticeDate = noticeDate != null ? noticeDate : "";
            this.createdBy = createdBy != null ? createdBy : "";
            this.creatorName = creatorName != null ? creatorName : "";
            this.status = status != null ? status.trim().toUpperCase(Locale.ROOT) : "ACTIVE";
        }

        public Object[] toTableRow() {
            String poster = !creatorName.isEmpty() ? creatorName + " (" + createdBy + ")" : createdBy;
            return new Object[]{noticeId, title, noticeDate, poster, status};
        }
    }

    /**
     * Default constructor: resolves active lecturer ID dynamically.
     */
    public Notices() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the active lecturer ID (e.g. "LEC001")
     */
    public Notices(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboard.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Notice Board");
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
        return LecturerTheme.createSidebar("Notices", lecturerId, this);
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

        JLabel pageTitle = new JLabel("Notice Board");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(TEXT);

        subtitleLabel = new JLabel("View academic announcements and faculty notices.");
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
        JPanel summary = new JPanel(new GridLayout(1, 3, 16, 0));
        summary.setBackground(BACKGROUND);

        activeNoticesValue = new JLabel("-");
        recentNoticesValue = new JLabel("-");
        totalNoticesValue = new JLabel("-");

        summary.add(createSummaryCard("ACTIVE NOTICES", activeNoticesValue, TEAL, "Currently active faculty notices"));
        summary.add(createSummaryCard("RECENT (30 DAYS)", recentNoticesValue, NAVY, "Published in the last 30 days"));
        summary.add(createSummaryCard("ALL NOTICES", totalNoticesValue, PURPLE, "Total published announcements"));

        body.add(summary, BorderLayout.NORTH);

        // Notices Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(0, 16));
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 16, 18)
        ));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setBackground(WHITE);

        JLabel tableTitle = new JLabel("Published Notices");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 18));
        tableTitle.setForeground(TEXT);

        tableHintLabel = new JLabel("Double-click any notice to read the full description");
        tableHintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        tableHintLabel.setForeground(MUTED);

        tableHeading.add(tableTitle, BorderLayout.WEST);
        tableHeading.add(tableHintLabel, BorderLayout.EAST);
        tablePanel.add(tableHeading, BorderLayout.NORTH);

        // Table Model
        String[] columns = {
                "Notice ID",
                "Notice Title",
                "Date",
                "Posted By",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        noticesTable = new JTable(tableModel);
        noticesTable.setRowHeight(39);
        noticesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        noticesTable.setForeground(TEXT);
        noticesTable.setBackground(WHITE);
        noticesTable.setGridColor(BORDER);
        noticesTable.setShowVerticalLines(false);
        noticesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noticesTable.setSelectionBackground(new Color(226, 238, 249));
        noticesTable.setSelectionForeground(TEXT);
        noticesTable.setFillsViewportHeight(true);
        noticesTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = noticesTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(237, 242, 247));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 39));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        noticesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());
        noticesTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        noticesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer());
        noticesTable.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);
        noticesTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());

        noticesTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        noticesTable.getColumnModel().getColumn(1).setPreferredWidth(340);
        noticesTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        noticesTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        noticesTable.getColumnModel().getColumn(4).setPreferredWidth(95);

        // Double-click row listener
        noticesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && noticesTable.getSelectedRow() >= 0) {
                    viewNoticeDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(noticesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        // Empty state container
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(WHITE);

        emptyNoticeLabel = new JLabel("No notices currently published.", SwingConstants.CENTER);
        emptyNoticeLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        emptyNoticeLabel.setForeground(MUTED);
        emptyNoticeLabel.setBorder(new EmptyBorder(14, 10, 14, 10));
        emptyNoticeLabel.setVisible(false);

        centerContainer.add(emptyNoticeLabel, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(centerContainer, BorderLayout.CENTER);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 10));
        toolbar.setBackground(WHITE);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setBackground(WHITE);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Search title, description, or notice ID");
        searchField.setPreferredSize(new Dimension(190, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 9, 0, 9)
        ));

        statusFilter = new JComboBox<>(new String[]{"Active Notices", "All Notices", "Inactive Notices"});
        statusFilter.setFont(new Font("Arial", Font.PLAIN, 12));
        statusFilter.setPreferredSize(new Dimension(145, 34));
        statusFilter.setBackground(WHITE);

        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(new JLabel("Filter:"));
        filters.add(statusFilter);

        JButton viewButton = createActionButton("Read Notice", true);
        JButton refreshButton = createActionButton("Refresh", false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(WHITE);
        actions.add(refreshButton);
        actions.add(viewButton);

        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);
        tablePanel.add(toolbar, BorderLayout.SOUTH);

        // Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterNotices(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterNotices(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterNotices(); }
        });

        statusFilter.addActionListener(e -> filterNotices());
        viewButton.addActionListener(e -> viewNoticeDetails());
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            if (statusFilter.getItemCount() > 0) statusFilter.setSelectedIndex(0);
            loadNotices();
            JOptionPane.showMessageDialog(this, "Notice board refreshed.", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });

        body.add(tablePanel, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);

        loadNotices();
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
                    if ("ACTIVE".equals(status)) {
                        label.setForeground(new Color(29, 130, 103));
                        label.setBackground(new Color(232, 247, 240));
                    } else {
                        label.setForeground(MUTED);
                        label.setBackground(new Color(241, 245, 249));
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
        button.setPreferredSize(new Dimension(primary ? 115 : 90, 34));
        button.setBorder(primary
                ? BorderFactory.createEmptyBorder(7, 12, 7, 12)
                : BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 11, 6, 11)
        ));
        return button;
    }

    // ================= DATABASE QUERY =================

    private void loadNotices() {
        loadedNotices.clear();
        displayedNotices.clear();
        tableModel.setRowCount(0);

        String sql =
                "SELECT n.notice_id, n.title, n.description, n.notice_date, n.created_by, n.status, " +
                "       u.first_name, u.last_name " +
                "FROM notices n " +
                "LEFT JOIN users u ON n.created_by = u.user_id " +
                "ORDER BY n.notice_date DESC, n.notice_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String noticeId = rs.getString("notice_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                java.sql.Date d = rs.getDate("notice_date");
                String noticeDate = d != null ? d.toString() : "";
                String createdBy = rs.getString("created_by");
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String creatorName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                String status = rs.getString("status");

                loadedNotices.add(new NoticeItem(
                        noticeId, title, description, noticeDate, createdBy, creatorName, status
                ));
            }

            if (loadedNotices.isEmpty()) {
                if (emptyNoticeLabel != null) {
                    emptyNoticeLabel.setText("No notices found in the database.");
                    emptyNoticeLabel.setVisible(true);
                }
                if (tableHintLabel != null) {
                    tableHintLabel.setText("0 NOTICES");
                }
            } else {
                if (emptyNoticeLabel != null) {
                    emptyNoticeLabel.setVisible(false);
                }
                if (tableHintLabel != null) {
                    tableHintLabel.setText(loadedNotices.size() + " NOTICES FOUND");
                }
            }

            filterNotices();

        } catch (SQLException e) {
            System.err.println("[Notices] Database error: " + e.getMessage());
            if (subtitleLabel != null) {
                subtitleLabel.setText("Database error loading notices: " + e.getMessage());
            }
            if (emptyNoticeLabel != null) {
                emptyNoticeLabel.setText("Database connection error: " + e.getMessage());
                emptyNoticeLabel.setVisible(true);
            }
        }

        updateSummaryMetrics();
    }

    // ================= SEARCH & FILTER =================

    private void filterNotices() {
        if (tableModel == null) return;

        String query = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String selectedStatusFilter = statusFilter != null ? (String) statusFilter.getSelectedItem() : "Active Notices";

        tableModel.setRowCount(0);
        displayedNotices.clear();

        for (NoticeItem item : loadedNotices) {
            boolean matchesSearch = query.isEmpty()
                    || item.title.toLowerCase(Locale.ROOT).contains(query)
                    || item.description.toLowerCase(Locale.ROOT).contains(query)
                    || item.noticeId.toLowerCase(Locale.ROOT).contains(query)
                    || item.createdBy.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesStatus = true;
            if ("Active Notices".equals(selectedStatusFilter)) {
                matchesStatus = "ACTIVE".equalsIgnoreCase(item.status);
            } else if ("Inactive Notices".equals(selectedStatusFilter)) {
                matchesStatus = "INACTIVE".equalsIgnoreCase(item.status);
            }

            if (matchesSearch && matchesStatus) {
                displayedNotices.add(item);
                tableModel.addRow(item.toTableRow());
            }
        }

        if (emptyNoticeLabel != null) {
            if (loadedNotices.isEmpty()) {
                emptyNoticeLabel.setText("No notices found in the database.");
                emptyNoticeLabel.setVisible(true);
            } else if (displayedNotices.isEmpty()) {
                emptyNoticeLabel.setText("No notices match your search or filter criteria.");
                emptyNoticeLabel.setVisible(true);
            } else {
                emptyNoticeLabel.setVisible(false);
            }
        }

        updateSummaryMetrics();
    }

    private void updateSummaryMetrics() {
        if (activeNoticesValue == null || recentNoticesValue == null || totalNoticesValue == null) {
            return;
        }

        int activeCount = 0;
        int recentCount = 0;
        int totalCount = loadedNotices.size();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        for (NoticeItem item : loadedNotices) {
            if ("ACTIVE".equalsIgnoreCase(item.status)) {
                activeCount++;
            }
            if (!item.noticeDate.isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(item.noticeDate);
                    if (!date.isBefore(thirtyDaysAgo)) {
                        recentCount++;
                    }
                } catch (DateTimeParseException ignored) {
                }
            }
        }

        activeNoticesValue.setText(String.valueOf(activeCount));
        recentNoticesValue.setText(String.valueOf(recentCount));
        totalNoticesValue.setText(String.valueOf(totalCount));
    }

    // ================= ACTIONS =================

    private void viewNoticeDetails() {
        int selectedRow = noticesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a notice to read.", "No Notice Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = noticesTable.convertRowIndexToModel(selectedRow);
        NoticeItem item = (modelRow >= 0 && modelRow < displayedNotices.size())
                ? displayedNotices.get(modelRow)
                : null;

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Unable to load the selected notice details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel details = new JPanel(new BorderLayout(0, 14));
        details.setBorder(new EmptyBorder(12, 14, 14, 14));
        details.setPreferredSize(new Dimension(520, 340));

        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 4, 6));
        headerPanel.setBackground(WHITE);

        JLabel titleLbl = new JLabel(item.title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 16));
        titleLbl.setForeground(NAVY);

        JLabel metaLbl = new JLabel("Posted on: " + item.noticeDate + "  •  By: " + (!item.creatorName.isEmpty() ? item.creatorName : item.createdBy) + "  •  Status: " + item.status);
        metaLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        metaLbl.setForeground(MUTED);

        headerPanel.add(titleLbl);
        headerPanel.add(metaLbl);
        details.add(headerPanel, BorderLayout.NORTH);

        JTextArea descArea = new JTextArea(item.description);
        descArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollArea = new JScrollPane(descArea);
        scrollArea.setBorder(BorderFactory.createLineBorder(BORDER));
        details.add(scrollArea, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, details, "Notice: " + item.noticeId, JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        String testId = args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboard.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> new Notices(testId).setVisible(true));
    }
}
