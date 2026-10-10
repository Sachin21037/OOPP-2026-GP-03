import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;

public class AttendanceManagementPage extends JPanel {

    // theme colors matching the technical officer dashboard
    private final Color darkNavy = new Color(27, 67, 103);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color cardWhite = Color.WHITE;
    private final Color primaryBlue = new Color(45, 100, 160);
    private final Color successGreen = new Color(20, 165, 145);
    private final Color alertRed = new Color(210, 60, 60);
    private final Color borderColor = new Color(225, 228, 232);
    private final Color textDark = new Color(35, 45, 55);
    private final Color textMuted = new Color(110, 120, 130);
    private final Color tableStripe = new Color(249, 251, 253);

    // session configuration fields
    private JComboBox<String> courseOfferingCombo;
    private JTextField sessionDateField;
    private JComboBox<String> sessionTypeCombo;
    private JTextField sessionNumberField;
    private JTextField durationField;

    // table and model
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    // live counter badges
    private JLabel totalStudentsLabel;
    private JLabel presentCountLabel;
    private JLabel absentCountLabel;

    // action buttons
    private JButton addRowButton;
    private JButton removeRowButton;
    private JButton markAllPresentButton;
    private JButton saveAttendanceButton;
    private JButton clearButton;

    public AttendanceManagementPage() {
        setLayout(new BorderLayout());
        setBackground(lightBackground);

        initComponents();

        // start with empty counters
        updateLiveCounters();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBackground(lightBackground);
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // page title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(lightBackground);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(new EmptyBorder(0, 4, 6, 4));

        JLabel title = new JLabel("Attendance Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Configure the session details and record student attendance.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        // center body holding session settings and the attendance table
        JPanel centerBody = new JPanel(new BorderLayout(0, 12));
        centerBody.setBackground(lightBackground);
        centerBody.add(createSessionConfigPanel(), BorderLayout.NORTH);
        centerBody.add(createTablePanel(), BorderLayout.CENTER);

        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(centerBody, BorderLayout.CENTER);
        contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    // session configuration panel
    private JPanel createSessionConfigPanel() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel sectionTitle = new JLabel("Session Configuration");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(darkNavy);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // course options
        String[] courseOfferings = {
                "ICT2132 - Data Structures (Lab 02)",
                "ICT1222 - Object Oriented Programming (Lab 01)",
                "ICT2213 - Database Management Systems (Lab 01)",
                "ICT3112 - Computer Networks (Lab 03)"
        };
        courseOfferingCombo = new JComboBox<>(courseOfferings);
        styleComboBox(courseOfferingCombo);

        sessionDateField = new JTextField("2026-10-10");
        styleTextField(sessionDateField);

        String[] sessionTypes = {"Lab Practical", "Lecture", "Tutorial"};
        sessionTypeCombo = new JComboBox<>(sessionTypes);
        styleComboBox(sessionTypeCombo);

        sessionNumberField = new JTextField("05");
        styleTextField(sessionNumberField);

        durationField = new JTextField("2.0");
        styleTextField(durationField);

        // row 1: course offering, session date, session type
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formGrid.add(createLabeledField("Course Offering:", courseOfferingCombo), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1;
        formGrid.add(createLabeledField("Session Date:", sessionDateField), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 1;
        formGrid.add(createLabeledField("Session Type:", sessionTypeCombo), gbc);

        // row 2: session number, duration
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        formGrid.add(createLabeledField("Session No:", sessionNumberField), gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2;
        formGrid.add(createLabeledField("Duration (Hours):", durationField), gbc);

        card.add(sectionTitle, BorderLayout.NORTH);
        card.add(formGrid, BorderLayout.CENTER);

        return card;
    }

    // student table setup
    private JPanel createTablePanel() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("Student Attendance List");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 14));
        tableTitle.setForeground(darkNavy);

        // quick table controls on the right
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tableActions.setOpaque(false);

        addRowButton = createStyledButton("+ Add Row", primaryBlue, Color.WHITE);
        addRowButton.setFont(new Font("Arial", Font.PLAIN, 11));
        addRowButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        addRowButton.addActionListener(e -> addNewRow());

        removeRowButton = createStyledButton("- Remove Row", Color.WHITE, textDark);
        removeRowButton.setFont(new Font("Arial", Font.PLAIN, 11));
        removeRowButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        removeRowButton.addActionListener(e -> removeSelectedRow());

        tableActions.add(addRowButton);
        tableActions.add(removeRowButton);

        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(tableActions, BorderLayout.EAST);

        String[] columns = {
                "#",
                "Student ID",
                "Index No",
                "Student Name",
                "Attendance Status",
                "Remarks"
        };

        // editable model allowing technical officer to fill or edit records
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // row number is auto generated; all other columns editable
                return column > 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(32);
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 13));
        attendanceTable.setGridColor(new Color(235, 238, 242));
        attendanceTable.setShowGrid(true);
        attendanceTable.setSelectionBackground(new Color(225, 237, 248));
        attendanceTable.setSelectionForeground(darkNavy);
        attendanceTable.setFillsViewportHeight(true);

        JTableHeader tableHeader = attendanceTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 12));
        tableHeader.setBackground(darkNavy);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 34));
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // column widths
        setColumnWidth(0, 45, 55);
        setColumnWidth(1, 95, 110);
        setColumnWidth(2, 115, 130);
        setColumnWidth(3, 200, 300);
        setColumnWidth(4, 140, 160);
        setColumnWidth(5, 180, 400);

        // dropdown editor for status
        JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"Present", "Absent"});
        statusDropdown.setFont(new Font("Arial", Font.BOLD, 12));
        DefaultCellEditor statusEditor = new DefaultCellEditor(statusDropdown);
        statusEditor.setClickCountToStart(1);
        attendanceTable.getColumnModel().getColumn(4).setCellEditor(statusEditor);

        // status badge renderer
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(new AttendanceStatusRenderer());

        // zebra striping renderer
        attendanceTable.setDefaultRenderer(String.class, new AlternateRowRenderer());

        // listen for edits to recalculate counters live
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE ||
                e.getType() == TableModelEvent.INSERT ||
                e.getType() == TableModelEvent.DELETE) {
                updateLiveCounters();
            }
        });

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(tableHeaderPanel, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    // summary counters and actions
    private JPanel createFooterPanel() {
        JPanel footerCard = createCardPanel();
        footerCard.setLayout(new BorderLayout(12, 0));

        // live summary badges
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statsPanel.setOpaque(false);

        totalStudentsLabel = createBadge("Total Students: 0", darkNavy, new Color(236, 242, 248));
        presentCountLabel = createBadge("Present: 0", successGreen, new Color(230, 247, 244));
        absentCountLabel = createBadge("Absent: 0", alertRed, new Color(254, 237, 237));

        statsPanel.add(totalStudentsLabel);
        statsPanel.add(presentCountLabel);
        statsPanel.add(absentCountLabel);

        // action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        clearButton = createStyledButton("Clear", Color.WHITE, textDark);
        clearButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(7, 12, 7, 12)
        ));
        clearButton.addActionListener(e -> clearTable());

        markAllPresentButton = createStyledButton("Mark All Present", primaryBlue, Color.WHITE);
        markAllPresentButton.addActionListener(e -> markAllPresent());

        saveAttendanceButton = createStyledButton("Save Attendance", successGreen, Color.WHITE);
        saveAttendanceButton.addActionListener(e -> saveAttendance());

        actionPanel.add(clearButton);
        actionPanel.add(markAllPresentButton);
        actionPanel.add(saveAttendanceButton);

        footerCard.add(statsPanel, BorderLayout.WEST);
        footerCard.add(actionPanel, BorderLayout.EAST);

        return footerCard;
    }

    // append new empty student row
    private void addNewRow() {
        int nextIndex = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{String.valueOf(nextIndex), "", "", "", "Present", ""});
        int lastRow = tableModel.getRowCount() - 1;
        attendanceTable.setRowSelectionInterval(lastRow, lastRow);
        attendanceTable.scrollRectToVisible(attendanceTable.getCellRect(lastRow, 0, true));
    }

    // delete selected row or the last row
    private void removeSelectedRow() {
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) return;

        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        int selected = attendanceTable.getSelectedRow();
        if (selected >= 0 && selected < rowCount) {
            tableModel.removeRow(selected);
        } else {
            tableModel.removeRow(rowCount - 1);
        }
        renumberRows();
    }

    // renumber row index column after deletions
    private void renumberRows() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(String.valueOf(i + 1), i, 0);
        }
        updateLiveCounters();
    }

    private void setColumnWidth(int colIndex, int preferredWidth, int maxWidth) {
        TableColumn col = attendanceTable.getColumnModel().getColumn(colIndex);
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
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(7, 14, 7, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (bg != Color.WHITE) {
                    button.setBackground(bg.darker());
                } else {
                    button.setBackground(new Color(240, 242, 245));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });

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

    // update attendance counters
    private void updateLiveCounters() {
        int total = tableModel.getRowCount();
        int present = 0;
        int absent = 0;

        for (int i = 0; i < total; i++) {
            Object statusObj = tableModel.getValueAt(i, 4);
            String status = statusObj != null ? statusObj.toString() : "";
            if ("Present".equalsIgnoreCase(status)) {
                present++;
            } else if ("Absent".equalsIgnoreCase(status)) {
                absent++;
            }
        }

        totalStudentsLabel.setText("Total Students: " + total);
        presentCountLabel.setText("Present: " + present);
        absentCountLabel.setText("Absent: " + absent);
    }

    // set all students status to present
    private void markAllPresent() {
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "No students in the list.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        for (int i = 0; i < rowCount; i++) {
            tableModel.setValueAt("Present", i, 4);
        }

        updateLiveCounters();
    }

    // clear all rows in the table
    private void clearTable() {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }
        tableModel.setRowCount(0);
        updateLiveCounters();
    }

    // validate and save attendance
    private void saveAttendance() {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Cannot save attendance: No student records in the table.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String date = sessionDateField.getText().trim();
        String sessionNo = sessionNumberField.getText().trim();
        String duration = durationField.getText().trim();
        String offering = (String) courseOfferingCombo.getSelectedItem();
        String type = (String) sessionTypeCombo.getSelectedItem();

        if (date.isEmpty() || sessionNo.isEmpty() || duration.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all session configuration fields.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int total = tableModel.getRowCount();
        int present = 0;
        int absent = 0;
        for (int i = 0; i < total; i++) {
            String status = (String) tableModel.getValueAt(i, 4);
            if ("Present".equalsIgnoreCase(status)) {
                present++;
            } else {
                absent++;
            }
        }

        String message = String.format(
                "Attendance saved successfully!\n\n" +
                "--------------------------------------------------\n" +
                "Course Offering : %s\n" +
                "Session Date    : %s\n" +
                "Session Type    : %s (Session #%s)\n" +
                "Duration        : %s Hours\n" +
                "--------------------------------------------------\n" +
                "Total Students  : %d\n" +
                "Present Count   : %d\n" +
                "Absent Count    : %d\n",
                offering, date, type, sessionNo, duration,
                total, present, absent
        );

        JOptionPane.showMessageDialog(this, message, "Attendance Recorded", JOptionPane.INFORMATION_MESSAGE);
    }

    // render status column with colored badges
    private class AttendanceStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));

            String status = value != null ? value.toString() : "";
            if ("Present".equalsIgnoreCase(status)) {
                label.setForeground(successGreen);
                if (!isSelected) {
                    label.setBackground(new Color(230, 247, 244));
                }
            } else if ("Absent".equalsIgnoreCase(status)) {
                label.setForeground(alertRed);
                if (!isSelected) {
                    label.setBackground(new Color(254, 237, 237));
                }
            } else {
                label.setForeground(textDark);
                if (!isSelected) {
                    label.setBackground(Color.WHITE);
                }
            }

            label.setBorder(new EmptyBorder(3, 8, 3, 8));
            return label;
        }
    }

    // alternate row background
    private class AlternateRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(tableStripe);
                }
            }

            if (column == 0 || column == 1 || column == 2) {
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
