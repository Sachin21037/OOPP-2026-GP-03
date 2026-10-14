
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LecturerProfile extends JFrame {

    private final Color NAVY = new Color(20, 40, 80);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(40, 45, 55);

    private JTextField nameField;
    private JTextField idField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField departmentField;
    private JTextField designationField;

    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;

    public LecturerProfile() {
        setTitle("Lecturer Profile");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createProfileContent(), BorderLayout.CENTER);
    }

    // HEADER
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(1100, 65));
        header.setBorder(new EmptyBorder(10, 25, 10, 25));

        JLabel title = new JLabel(
                "Faculty Academic Management System"
        );
        title.setFont(new Font("Arial", Font.BOLD, 21));
        title.setForeground(WHITE);

        JLabel role = new JLabel("LECTURER");
        role.setFont(new Font("Arial", Font.BOLD, 13));
        role.setForeground(WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(role, BorderLayout.EAST);

        return header;
    }

    // SIDEBAR
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(
                new BoxLayout(sidebar, BoxLayout.Y_AXIS)
        );
        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(new EmptyBorder(25, 12, 20, 12));

        JLabel menuTitle = new JLabel("LECTURER MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 13));
        menuTitle.setForeground(Color.GRAY);
        menuTitle.setBorder(
                new EmptyBorder(0, 12, 20, 0)
        );
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(menuTitle);

        sidebar.add(menuButton("Dashboard", false, () -> {
            new LecturerDashboard().setVisible(true);
            dispose();
        }));

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(menuButton("My Courses", false, () -> {
            new MyCourses().setVisible(true);
            dispose();
        }));

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(menuButton("Students", false, () -> {
            new Student().setVisible(true);
            dispose();
        }));

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(menuButton("Attendance", false, () -> {
            showComingSoon("Attendance");
        }));

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(menuButton("Marks", false, () -> {
            showComingSoon("Marks");
        }));

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(menuButton("Profile", true, () -> {
            // Already on Profile page
        }));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(menuButton("Logout", false, () -> {
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
        }));

        return sidebar;
    }

    // SIDEBAR BUTTON DESIGN
    private JButton menuButton(
            String text,
            boolean active,
            Runnable action
    ) {
        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );
        button.setPreferredSize(new Dimension(195, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBorder(
                new EmptyBorder(0, 15, 0, 10)
        );

        if (active) {
            button.setBackground(NAVY);
            button.setForeground(WHITE);
            button.setFont(
                    new Font("Arial", Font.BOLD, 15)
            );
        } else {
            button.setBackground(WHITE);
            button.setForeground(TEXT);
        }

        button.addActionListener(e -> action.run());

        return button;
    }

    // PROFILE CONTENT
    private JPanel createProfileContent() {
        JPanel main = new JPanel(new BorderLayout(15, 20));
        main.setBackground(LIGHT_BG);
        main.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel heading = new JLabel("My Profile");
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "View and manage your lecturer information."
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS)
        );
        headingPanel.setBackground(LIGHT_BG);
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(8));
        headingPanel.add(subtitle);
        headingPanel.add(Box.createVerticalStrut(20));

        main.add(headingPanel, BorderLayout.NORTH);

        // Profile card
        JPanel card = new JPanel(new BorderLayout(25, 10));
        card.setBackground(WHITE);
        card.setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );

        // Profile avatar
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(
                new BoxLayout(avatarPanel, BoxLayout.Y_AXIS)
        );
        avatarPanel.setBackground(WHITE);
        avatarPanel.setPreferredSize(new Dimension(190, 300));

        JLabel avatar = new JLabel("L", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(NAVY);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Arial", Font.BOLD, 48));
        avatar.setPreferredSize(new Dimension(110, 110));
        avatar.setMinimumSize(new Dimension(110, 110));
        avatar.setMaximumSize(new Dimension(110, 110));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Lecturer");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        roleLabel.setForeground(NAVY);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("Academic Staff");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(avatar);
        avatarPanel.add(Box.createVerticalStrut(15));
        avatarPanel.add(roleLabel);
        avatarPanel.add(Box.createVerticalStrut(8));
        avatarPanel.add(statusLabel);

        card.add(avatarPanel, BorderLayout.WEST);

        // Lecturer information fields
        JPanel fieldsPanel = new JPanel(
                new GridLayout(0, 2, 18, 18)
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

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
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

        JPanel cardContent = new JPanel(new BorderLayout(0, 20));
        cardContent.setBackground(WHITE);
        cardContent.add(card, BorderLayout.CENTER);
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
            setSampleData();
            setFieldsEditable(false);
            editButton.setVisible(true);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
        });

        return main;
    }

    // CREATE FIELD
    private JTextField addField(
            JPanel panel,
            String labelText,
            String value
    ) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 7));
        fieldPanel.setBackground(WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(TEXT);

        JTextField field = new JTextField(value);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(220, 35));
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 215, 225)
                        ),
                        new EmptyBorder(5, 8, 5, 8)
                )
        );

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);
        panel.add(fieldPanel);

        return field;
    }

    // ENABLE / DISABLE EDITING
    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        emailField.setEditable(editable);
        phoneField.setEditable(editable);
        departmentField.setEditable(editable);
        designationField.setEditable(editable);

        // Lecturer ID is read-only
        idField.setEditable(false);

        Color fieldColor = editable
                ? new Color(250, 252, 255)
                : new Color(245, 247, 250);

        nameField.setBackground(fieldColor);
        idField.setBackground(fieldColor);
        emailField.setBackground(fieldColor);
        phoneField.setBackground(fieldColor);
        departmentField.setBackground(fieldColor);
        designationField.setBackground(fieldColor);
    }

    // SAVE PROFILE
    private void saveProfile() {
        if (nameField.getText().trim().isEmpty()
                || emailField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Name and email cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        setFieldsEditable(false);

        editButton.setVisible(true);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        JOptionPane.showMessageDialog(
                this,
                "Profile updated for this session only.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // RESTORE SAMPLE DATA
    private void setSampleData() {
        nameField.setText("Lecturer Name");
        idField.setText("LEC001");
        emailField.setText("lecturer@university.edu");
        phoneField.setText("07XXXXXXXX");
        departmentField.setText("ICT Department");
        designationField.setText("Lecturer");
    }

    // BUTTON STYLE
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(NAVY);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(125, 35));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    private void showComingSoon(String feature) {
        JOptionPane.showMessageDialog(
                this,
                feature + " page is not connected yet.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // MAIN METHOD
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LecturerProfile().setVisible(true);
        });
    }
}
