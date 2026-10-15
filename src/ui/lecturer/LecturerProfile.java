
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LecturerProfile extends JFrame {

    private static final Color NAVY = new Color(31, 66, 99);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(35, 48, 65);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(220, 227, 235);
    private static final Color RED = new Color(190, 55, 55);
    private static final Color ACTIVE_BG = new Color(232, 240, 248);

    private static final Font BODY =
            new Font("Arial", Font.PLAIN, 13);

    private JTextField nameField;
    private JTextField idField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField departmentField;
    private JTextField designationField;

    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;

    private String savedName;
    private String savedEmail;
    private String savedPhone;
    private String savedDepartment;
    private String savedDesignation;

    public LecturerProfile() {

        setTitle("Faculty Academic Management System - Lecturer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createProfileContent(), BorderLayout.CENTER);
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(new EmptyBorder(0, 26, 0, 26));

        JLabel title = new JLabel(
                "FACULTY ACADEMIC MANAGEMENT SYSTEM"
        );

        title.setForeground(WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 19));

        JLabel role = new JLabel("LECTURER");

        role.setForeground(WHITE);
        role.setFont(new Font("Arial", Font.BOLD, 13));

        header.add(title, BorderLayout.WEST);
        header.add(role, BorderLayout.EAST);

        return header;
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());

        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(205, 0));

        sidebar.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 0, 1, BORDER
                )
        );

        JPanel menu = new JPanel();

        menu.setBackground(WHITE);
        menu.setLayout(
                new BoxLayout(menu, BoxLayout.Y_AXIS)
        );

        menu.setBorder(
                new EmptyBorder(25, 14, 12, 14)
        );

        JLabel menuTitle = new JLabel("LECTURER MENU");

        menuTitle.setFont(new Font("Arial", Font.BOLD, 11));
        menuTitle.setForeground(MUTED);
        menuTitle.setBorder(
                new EmptyBorder(0, 9, 18, 0)
        );

        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(menuTitle);

        menu.add(createMenuButton("Dashboard", false, () -> {
            new LecturerDashboard().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("My Courses", false, () -> {
            new MyCourses().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("Students", false, () -> {
            new Student().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("Attendance", false, () -> {
            new Attendance().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("Marks / Grades", false, () -> {
            new MarksGrades().setVisible(true);
            dispose();
        }));

        menu.add(createMenuButton("Assignments", false, () -> {
            showComingSoon("Assignments");
        }));

        // Profile is highlighted because this is the Profile page
        menu.add(createMenuButton("Profile", true, () -> {
            // Already on the Profile page
        }));

        sidebar.add(menu, BorderLayout.NORTH);

        // Logout section
        JPanel bottom = new JPanel(new BorderLayout());

        bottom.setBackground(WHITE);
        bottom.setBorder(
                new EmptyBorder(10, 14, 22, 14)
        );

        JButton logout = new JButton("Logout");

        logout.setFont(new Font("Arial", Font.BOLD, 13));
        logout.setForeground(RED);
        logout.setBackground(WHITE);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10, 10, 10, 10));
        logout.setFocusPainted(false);
        logout.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        logout.addActionListener(e -> logout());

        bottom.add(logout, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    // ================= MENU BUTTON =================

    private JButton createMenuButton(
            String text,
            boolean active,
            Runnable action
    ) {

        JButton button = new JButton(text);

        button.setFont(active
                ? new Font("Arial", Font.BOLD, 13)
                : BODY);

        button.setForeground(active ? NAVY : TEXT);

        button.setBackground(
                active ? ACTIVE_BG : WHITE
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );

        button.setPreferredSize(
                new Dimension(175, 43)
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        active
                                ? BorderFactory.createMatteBorder(
                                0, 4, 0, 0, NAVY
                        )
                                : BorderFactory.createEmptyBorder(
                                0, 4, 0, 0
                        ),
                        new EmptyBorder(0, 13, 0, 8)
                )
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> action.run());

        button.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {

                        if (!active) {
                            button.setBackground(
                                    new Color(245, 248, 252)
                            );
                        }
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {

                        button.setBackground(
                                active ? ACTIVE_BG : WHITE
                        );
                    }
                }
        );

        return button;
    }

    // ================= PROFILE CONTENT =================

    private JPanel createProfileContent() {

        JPanel content = new JPanel(
                new BorderLayout(0, 22)
        );

        content.setBackground(BACKGROUND);

        content.setBorder(
                new EmptyBorder(28, 30, 30, 30)
        );

        // Page heading
        JPanel headingPanel = new JPanel();

        headingPanel.setBackground(BACKGROUND);

        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("My Profile");

        heading.setFont(new Font("Arial", Font.BOLD, 29));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "View and manage your lecturer information."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        subtitle.setBorder(
                new EmptyBorder(5, 1, 0, 0)
        );

        headingPanel.add(heading);
        headingPanel.add(subtitle);

        content.add(headingPanel, BorderLayout.NORTH);

        // White profile panel
        JPanel profileCard = new JPanel(
                new BorderLayout(22, 18)
        );

        profileCard.setBackground(WHITE);

        profileCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(22, 22, 20, 22)
                )
        );

        // Left-side lecturer information
        JPanel lecturerPanel = new JPanel();

        lecturerPanel.setBackground(WHITE);

        lecturerPanel.setLayout(
                new BoxLayout(lecturerPanel, BoxLayout.Y_AXIS)
        );

        lecturerPanel.setPreferredSize(
                new Dimension(155, 220)
        );

        JLabel avatar = new JLabel(
                "L", SwingConstants.CENTER
        );

        avatar.setOpaque(true);
        avatar.setBackground(NAVY);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Arial", Font.BOLD, 40));

        avatar.setPreferredSize(new Dimension(90, 90));
        avatar.setMinimumSize(new Dimension(90, 90));
        avatar.setMaximumSize(new Dimension(90, 90));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lecturerTitle = new JLabel("Lecturer");

        lecturerTitle.setFont(
                new Font("Arial", Font.BOLD, 19)
        );

        lecturerTitle.setForeground(TEXT);
        lecturerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel staffTitle = new JLabel("Academic Staff");

        staffTitle.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        staffTitle.setForeground(MUTED);
        staffTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lecturerPanel.add(avatar);
        lecturerPanel.add(Box.createVerticalStrut(14));
        lecturerPanel.add(lecturerTitle);
        lecturerPanel.add(Box.createVerticalStrut(6));
        lecturerPanel.add(staffTitle);

        profileCard.add(
                lecturerPanel, BorderLayout.WEST
        );

        // Lecturer fields
        JPanel fieldsPanel = new JPanel(
                new GridLayout(3, 2, 16, 15)
        );

        fieldsPanel.setBackground(WHITE);

        nameField = addField(
                fieldsPanel,
                "Full Name",
                "Lecturer Name"
        );

        idField = addField(
                fieldsPanel,
                "Lecturer ID",
                "LEC001"
        );

        emailField = addField(
                fieldsPanel,
                "Email Address",
                "lecturer@university.edu"
        );

        phoneField = addField(
                fieldsPanel,
                "Phone Number",
                "07XXXXXXXX"
        );

        departmentField = addField(
                fieldsPanel,
                "Department",
                "ICT Department"
        );

        designationField = addField(
                fieldsPanel,
                "Designation",
                "Lecturer"
        );

        profileCard.add(
                fieldsPanel, BorderLayout.CENTER
        );

        // Bottom buttons
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
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

        JPanel cardContainer = new JPanel(
                new BorderLayout(0, 16)
        );

        cardContainer.setBackground(BACKGROUND);
        cardContainer.add(profileCard, BorderLayout.CENTER);
        cardContainer.add(buttonPanel, BorderLayout.SOUTH);

        content.add(cardContainer, BorderLayout.CENTER);

        saveOriginalValues();
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

        return content;
    }

    // ================= FIELD CREATION =================

    private JTextField addField(
            JPanel panel,
            String labelText,
            String value
    ) {

        JPanel fieldPanel = new JPanel(
                new BorderLayout(0, 7)
        );

        fieldPanel.setBackground(WHITE);

        JLabel label = new JLabel(labelText);

        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(TEXT);

        JTextField field = new JTextField(value);

        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(200, 43));

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(5, 9, 5, 9)
                )
        );

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);

        panel.add(fieldPanel);

        return field;
    }

    // ================= EDIT FIELDS =================

    private void setFieldsEditable(boolean editable) {

        nameField.setEditable(editable);
        emailField.setEditable(editable);
        phoneField.setEditable(editable);
        departmentField.setEditable(editable);
        designationField.setEditable(editable);

        idField.setEditable(false);

        Color fieldColor = editable
                ? new Color(250, 252, 255)
                : BACKGROUND;

        nameField.setBackground(fieldColor);
        idField.setBackground(fieldColor);
        emailField.setBackground(fieldColor);
        phoneField.setBackground(fieldColor);
        departmentField.setBackground(fieldColor);
        designationField.setBackground(fieldColor);
    }

    // ================= SAVE ORIGINAL VALUES =================

    private void saveOriginalValues() {

        savedName = nameField.getText();
        savedEmail = emailField.getText();
        savedPhone = phoneField.getText();
        savedDepartment = departmentField.getText();
        savedDesignation = designationField.getText();
    }

    private void restoreOriginalValues() {

        nameField.setText(savedName);
        emailField.setText(savedEmail);
        phoneField.setText(savedPhone);
        departmentField.setText(savedDepartment);
        designationField.setText(savedDesignation);
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

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(NAVY);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setPreferredSize(
                new Dimension(125, 36)
        );

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    // ================= OTHER PAGES =================

    private void showComingSoon(String page) {

        JOptionPane.showMessageDialog(
                this,
                page + " page will be added next.",
                page,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= LOGOUT =================

    private void logout() {

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (answer == JOptionPane.YES_OPTION) {
            dispose();

            JOptionPane.showMessageDialog(
                    null,
                    "You have been logged out."
            );
        }
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                () -> new LecturerProfile().setVisible(true)
        );
    }
}
