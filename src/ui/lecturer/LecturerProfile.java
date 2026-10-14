
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LecturerProfile extends JFrame {

    private final Color NAVY = new Color(33, 70, 100);
    private final Color LIGHT_BG = new Color(244, 247, 251);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(30, 48, 68);
    private final Color BORDER = new Color(215, 224, 235);
    private final Color ACTIVE_BG = new Color(231, 239, 248);

    private JTextField nameField;
    private JTextField idField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField departmentField;
    private JTextField designationField;

    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;

    private String originalName;
    private String originalEmail;
    private String originalPhone;
    private String originalDepartment;
    private String originalDesignation;

    public LecturerProfile() {

        setTitle("Faculty Academic Management System - Profile");
        setSize(1450, 900);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(LIGHT_BG);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(1450, 90));
        header.setBorder(
                new EmptyBorder(10, 28, 10, 30)
        );

        JLabel title = new JLabel(
                "FACULTY ACADEMIC MANAGEMENT SYSTEM"
        );

        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(WHITE);

        JLabel role = new JLabel("LECTURER");

        role.setFont(new Font("Arial", Font.BOLD, 16));
        role.setForeground(WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(role, BorderLayout.EAST);

        return header;
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setLayout(
                new BoxLayout(sidebar, BoxLayout.Y_AXIS)
        );

        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(250, 700));

        sidebar.setBorder(
                new EmptyBorder(25, 14, 20, 14)
        );

        JLabel menuTitle = new JLabel("LECTURER MENU");

        menuTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        menuTitle.setForeground(new Color(75, 100, 130));

        menuTitle.setBorder(
                new EmptyBorder(0, 12, 25, 0)
        );

        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(menuTitle);

        sidebar.add(createMenuButton("Dashboard", false, () -> {
            new LecturerDashboard().setVisible(true);
            dispose();
        }));

        sidebar.add(createMenuButton("My Courses", false, () -> {
            new MyCourses().setVisible(true);
            dispose();
        }));

        sidebar.add(createMenuButton("Students", false, () -> {
            new Student().setVisible(true);
            dispose();
        }));

        sidebar.add(createMenuButton("Attendance", false, () -> {
            showComingSoon("Attendance");
        }));

        sidebar.add(createMenuButton("Marks / Grades", false, () -> {
            showComingSoon("Marks / Grades");
        }));

        sidebar.add(createMenuButton("Assignments", false, () -> {
            showComingSoon("Assignments");
        }));

        sidebar.add(createMenuButton("Profile", true, () -> {
            // Already on Profile page
        }));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = createMenuButton(
                "Logout", false, this::logout
        );

        logoutButton.setForeground(new Color(190, 45, 45));

        sidebar.add(logoutButton);

        return sidebar;
    }

    // ================= SIDEBAR BUTTON =================

    private JButton createMenuButton(
            String text,
            boolean active,
            Runnable action
    ) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Arial",
                        active ? Font.BOLD : Font.PLAIN, 15)
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setPreferredSize(new Dimension(220, 54));
        button.setMinimumSize(new Dimension(220, 54));
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 54)
        );

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.setBorder(
                BorderFactory.createEmptyBorder(0, 18, 0, 10)
        );

        if (active) {
            button.setBackground(ACTIVE_BG);
            button.setForeground(NAVY);

            button.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(
                                    0, 5, 0, 0, NAVY
                            ),
                            BorderFactory.createEmptyBorder(
                                    0, 13, 0, 10
                            )
                    )
            );

        } else {
            button.setBackground(WHITE);
            button.setForeground(TEXT);
        }

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(e -> action.run());

        return button;
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 25));

        main.setBackground(LIGHT_BG);

        main.setBorder(
                new EmptyBorder(32, 38, 30, 38)
        );

        // Page heading
        JPanel headingPanel = new JPanel();

        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS)
        );

        headingPanel.setBackground(LIGHT_BG);

        JLabel heading = new JLabel("My Profile");

        heading.setFont(new Font("Arial", Font.BOLD, 36));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "View and manage your lecturer information."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(new Color(95, 115, 140));

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(8));
        headingPanel.add(subtitle);

        main.add(headingPanel, BorderLayout.NORTH);

        // Profile card
        JPanel profileCard = new JPanel(new BorderLayout(25, 0));

        profileCard.setBackground(WHITE);

        profileCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(30, 28, 25, 28)
                )
        );

        // Left profile section
        JPanel leftPanel = new JPanel();

        leftPanel.setLayout(
                new BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        );

        leftPanel.setBackground(WHITE);
        leftPanel.setPreferredSize(new Dimension(220, 300));

        JLabel avatar = new JLabel("L", SwingConstants.CENTER);

        avatar.setOpaque(true);
        avatar.setBackground(NAVY);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Arial", Font.BOLD, 46));

        avatar.setPreferredSize(new Dimension(110, 110));
        avatar.setMinimumSize(new Dimension(110, 110));
        avatar.setMaximumSize(new Dimension(110, 110));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lecturerName = new JLabel("Lecturer");

        lecturerName.setFont(new Font("Arial", Font.BOLD, 23));
        lecturerName.setForeground(TEXT);
        lecturerName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel staffRole = new JLabel("Academic Staff");

        staffRole.setFont(new Font("Arial", Font.PLAIN, 16));
        staffRole.setForeground(new Color(100, 120, 145));
        staffRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(avatar);
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(lecturerName);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(staffRole);

        profileCard.add(leftPanel, BorderLayout.WEST);

        // Right information section
        JPanel fieldsPanel = new JPanel(
                new GridLayout(3, 2, 22, 20)
        );

        fieldsPanel.setBackground(WHITE);

        nameField = addField(
                fieldsPanel, "Full Name", "Lecturer Name"
        );

        idField = addField(
                fieldsPanel, "Lecturer ID", "LEC001"
        );

        emailField = addField(
                fieldsPanel, "Email Address",
                "lecturer@university.edu"
        );

        phoneField = addField(
                fieldsPanel, "Phone Number", "07XXXXXXXX"
        );

        departmentField = addField(
                fieldsPanel, "Department", "ICT Department"
        );

        designationField = addField(
                fieldsPanel, "Designation", "Lecturer"
        );

        profileCard.add(fieldsPanel, BorderLayout.CENTER);

        // Save original sample values
        saveOriginalValues();

        // Bottom action buttons
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

        buttonPanel.setBackground(WHITE);

        editButton = new JButton("Edit Profile");
        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");

        styleButton(editButton);
        styleButton(saveButton);
        styleButton(cancelButton);

        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        buttonPanel.add(editButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        JPanel cardContent = new JPanel(new BorderLayout(0, 25));

        cardContent.setBackground(WHITE);
        cardContent.add(profileCard, BorderLayout.CENTER);
        cardContent.add(buttonPanel, BorderLayout.SOUTH);

        main.add(cardContent, BorderLayout.CENTER);

        setFieldsEditable(false);

        editButton.addActionListener(e -> {
            setFieldsEditable(true);

            editButton.setVisible(false);
            saveButton.setVisible(true);
            cancelButton.setVisible(true);
        });

        saveButton.addActionListener(e -> saveProfile());

        cancelButton.addActionListener(e -> {
            restoreOriginalValues();
            setFieldsEditable(false);

            editButton.setVisible(true);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
        });

        return main;
    }

    // ================= FIELD CREATION =================

    private JTextField addField(
            JPanel parent,
            String labelText,
            String value
    ) {

        JPanel fieldPanel = new JPanel(new BorderLayout(0, 9));

        fieldPanel.setBackground(WHITE);

        JLabel label = new JLabel(labelText);

        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(TEXT);

        JTextField field = new JTextField(value);

        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(220, 62));

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(8, 10, 8, 10)
                )
        );

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);

        parent.add(fieldPanel);

        return field;
    }

    // ================= EDITABLE FIELDS =================

    private void setFieldsEditable(boolean editable) {

        nameField.setEditable(editable);
        emailField.setEditable(editable);
        phoneField.setEditable(editable);
        departmentField.setEditable(editable);
        designationField.setEditable(editable);

        idField.setEditable(false);

        Color fieldColor = editable
                ? new Color(250, 252, 255)
                : new Color(244, 247, 251);

        nameField.setBackground(fieldColor);
        idField.setBackground(fieldColor);
        emailField.setBackground(fieldColor);
        phoneField.setBackground(fieldColor);
        departmentField.setBackground(fieldColor);
        designationField.setBackground(fieldColor);
    }

    // ================= ORIGINAL VALUES =================

    private void saveOriginalValues() {

        originalName = nameField.getText();
        originalEmail = emailField.getText();
        originalPhone = phoneField.getText();
        originalDepartment = departmentField.getText();
        originalDesignation = designationField.getText();
    }

    private void restoreOriginalValues() {

        nameField.setText(originalName);
        emailField.setText(originalEmail);
        phoneField.setText(originalPhone);
        departmentField.setText(originalDepartment);
        designationField.setText(originalDesignation);
    }

    // ================= SAVE PROFILE =================

    private void saveProfile() {

        if (nameField.getText().trim().isEmpty()
                || emailField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Full Name and Email Address are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        saveOriginalValues();
        setFieldsEditable(false);

        editButton.setVisible(true);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        JOptionPane.showMessageDialog(
                this,
                "Profile updated for this session.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= BUTTON STYLE =================

    private void styleButton(JButton button) {

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(NAVY);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setPreferredSize(new Dimension(155, 45));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    // ================= PLACEHOLDER =================

    private void showComingSoon(String feature) {

        JOptionPane.showMessageDialog(
                this,
                feature + " page is not connected yet.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= LOGOUT =================

    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LecturerDashboard().setVisible(true);
        }
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new LecturerProfile().setVisible(true);
        });
    }
}
