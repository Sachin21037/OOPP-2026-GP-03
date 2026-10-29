package ui.technicalofficer;

import model.TimetableEntry;
import service.TimetableService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Academic Timetable Page for Technical Officers.
 * Displays weekly department lecture and laboratory schedules, venues, and timings.
 * Directly fulfills requirement: "Can see timetables of their department".
 */
public class TimetablePage extends JPanel {

    // Theme palette harmonized with Technical Officer Dashboard
    private final Color darkNavy = new Color(27, 66, 104);       // #1B4268
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color lightBg = new Color(248, 250, 252);       // #F8FAFC
    private final Color borderColor = new Color(226, 232, 240);   // #E2E8F0
    private final Color textDark = new Color(30, 41, 59);         // #1E293B
    private final Color textMuted = new Color(100, 116, 139);     // #64748B
    private final Color accentBlue = new Color(29, 78, 216);      // #1D4ED8

    private final TimetableService timetableService = new TimetableService();
    private String department = "Information and Communication Technology";

    private JTextField searchField;
    private JComboBox<String> dayFilter;
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    private JLabel totalSlotsBadge;
    private JLabel labSlotsBadge;
    private JLabel lectureSlotsBadge;

    public TimetablePage() {
        this("Information and Communication Technology");
    }

    public TimetablePage(String department) {
        if (department != null && !department.isBlank()) {
            this.department = department;
        }
        setLayout(new BorderLayout());
        setBackground(lightBg);

        initUI();
        refreshTimetable();
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 16));
        mainContainer.setBackground(lightBg);
        mainContainer.setBorder(new EmptyBorder(22, 28, 24, 28));

        // 1. Top Header Banner
        JPanel headerPanel = createHeaderPanel();

        // 2. Main Content Card (Filter Bar + Timetable Grid + Statistics)
        JPanel contentCard = createContentCard();

        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentCard, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Department Academic Timetables");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Weekly lecture, laboratory, and tutorial schedule for " + department);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titleBox.add(title);
        titleBox.add(subtitle);

        // Stats Badges
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statsPanel.setOpaque(false);

        totalSlotsBadge = createStatBadge("Slots: 0", new Color(241, 245, 249), textDark);
        labSlotsBadge = createStatBadge("Labs: 0", new Color(234, 242, 255), accentBlue);
        lectureSlotsBadge = createStatBadge("Lectures: 0", new Color(243, 232, 255), new Color(126, 34, 206));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(primaryBlue);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 14, 6, 14)));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshTimetable());

        statsPanel.add(totalSlotsBadge);
        statsPanel.add(labSlotsBadge);
        statsPanel.add(lectureSlotsBadge);
        statsPanel.add(refreshBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        return header;
    }

    private JLabel createStatBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(fg);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(6, 14, 6, 14));
        return badge;
    }

    private JPanel createContentCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(18, 20, 18, 20)));

        // Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout(14, 0));
        filterBar.setOpaque(false);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilter.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search by course code, name, venue, lecturer...");
        searchField.setBorder(new CompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(6, 10, 6, 10)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        dayFilter = new JComboBox<>(new String[]{"All Days", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"});
        dayFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dayFilter.setBackground(Color.WHITE);
        dayFilter.addActionListener(e -> applyFilters());

        leftFilter.add(new JLabel("Search:"));
        leftFilter.add(searchField);
        leftFilter.add(Box.createHorizontalStrut(8));
        leftFilter.add(new JLabel("Day:"));
        leftFilter.add(dayFilter);

        // Department Indicator chip
        JLabel deptChip = new JLabel("Dept: " + department);
        deptChip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deptChip.setForeground(accentBlue);
        deptChip.setBorder(new CompoundBorder(new LineBorder(new Color(191, 219, 254), 1), new EmptyBorder(6, 12, 6, 12)));

        filterBar.add(leftFilter, BorderLayout.WEST);
        filterBar.add(deptChip, BorderLayout.EAST);

        // Table
        String[] columns = {"Slot ID", "Day", "Time Slot", "Course Offering", "Venue", "Batch", "Assigned Lecturer"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        timetableTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timetableTable.setRowHeight(36);
        timetableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableTable.setShowVerticalLines(false);
        timetableTable.setGridColor(new Color(241, 245, 249));
        timetableTable.setSelectionBackground(new Color(234, 242, 255));
        timetableTable.setSelectionForeground(textDark);

        // Header Styling
        JTableHeader th = timetableTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(textDark);
        th.setPreferredSize(new Dimension(0, 38));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

        // Column widths
        timetableTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        timetableTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        timetableTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        timetableTable.getColumnModel().getColumn(3).setPreferredWidth(260);
        timetableTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        timetableTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        timetableTable.getColumnModel().getColumn(6).setPreferredWidth(160);

        // Day of week renderer highlighting
        timetableTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(accentBlue);
                return lbl;
            }
        });

        // Venue renderer (lab highlighting)
        timetableTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = (value != null) ? value.toString() : "";
                if (val.toLowerCase().contains("lab")) {
                    lbl.setForeground(new Color(15, 118, 110)); // Teal for lab venues
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    lbl.setForeground(textDark);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(filterBar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    public void refreshTimetable() {
        applyFilters();
    }

    private void applyFilters() {
        String kw = (searchField != null) ? searchField.getText().trim() : "";
        String day = (dayFilter != null && dayFilter.getSelectedItem() != null)
                ? dayFilter.getSelectedItem().toString()
                : "All Days";

        List<TimetableEntry> entries = timetableService.searchTimetables(kw, day, department);

        tableModel.setRowCount(0);
        int total = 0;
        int labs = 0;
        int lectures = 0;

        for (TimetableEntry e : entries) {
            total++;
            String venue = (e.getVenue() != null) ? e.getVenue() : "";
            if (venue.toLowerCase().contains("lab")) {
                labs++;
            } else {
                lectures++;
            }

            tableModel.addRow(new Object[]{
                    e.getTimetableId(),
                    e.getDayOfWeek(),
                    e.getFormattedTimeRange(),
                    e.getCourseDisplay(),
                    venue,
                    e.getBatch(),
                    e.getLecturers()
            });
        }

        if (totalSlotsBadge != null) totalSlotsBadge.setText("Total: " + total);
        if (labSlotsBadge != null) labSlotsBadge.setText("Labs: " + labs);
        if (lectureSlotsBadge != null) lectureSlotsBadge.setText("Lectures: " + lectures);
    }
}
