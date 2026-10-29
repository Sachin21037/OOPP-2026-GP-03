package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class LecturerProfile extends JFrame {

    private static final Color NAVY = LecturerTheme.PRIMARY_NAVY;
    private static final Color BACKGROUND = LecturerTheme.MAIN_BG;
    private static final Color WHITE = LecturerTheme.CARD_BG;
    private static final Color TEXT = LecturerTheme.TEXT_PRIMARY;
    private static final Color MUTED = LecturerTheme.TEXT_SECONDARY;
    private static final Color BORDER = LecturerTheme.BORDER_COLOR;
    private static final Color RED = LecturerTheme.DANGER_RED;
    private static final Color ACTIVE_BG = LecturerTheme.LIGHT_BLUE;

    private static final Font BODY =
            new Font("Arial", Font.PLAIN, 13);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+ \\-()]{7,20}$");

    private String lecturerId;

    private JLabel avatar;
    private JLabel lecturerTitle;
    private JLabel staffTitle;
    private JButton changePhotoButton;

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
    private String currentPhotoPath;
    private File pendingPhotoFile;

    /**
     * Default constructor: dynamically resolves the active lecturer session.
     */
    public LecturerProfile() {
        this(resolveActiveLecturerId());
    }

    /**
     * Overloaded constructor accepting an explicit lecturer ID.
     *
     * @param lecturerId the active lecturer ID (e.g. "LEC001")
     */
    public LecturerProfile(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboard.setActiveLecturerId(this.lecturerId);
        }

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

        // Load profile data directly from MySQL
        loadProfileFromDatabase();
    }

    /**
     * Resolves the active lecturer ID from session state or system property.
     */
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

    // ================= HEADER =================

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Profile", lecturerId, this);
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
                new Dimension(155, 230)
        );

        avatar = new JLabel(
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

        changePhotoButton = new JButton("Change Photo");
        changePhotoButton.setFont(new Font("Arial", Font.PLAIN, 11));
        changePhotoButton.setForeground(NAVY);
        changePhotoButton.setBackground(ACTIVE_BG);
        changePhotoButton.setFocusPainted(false);
        changePhotoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changePhotoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePhotoButton.setMaximumSize(new Dimension(115, 26));
        changePhotoButton.setPreferredSize(new Dimension(115, 26));
        changePhotoButton.setVisible(false);
        changePhotoButton.addActionListener(e -> selectProfilePhoto());

        lecturerTitle = new JLabel("Lecturer");

        lecturerTitle.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        lecturerTitle.setForeground(TEXT);
        lecturerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        staffTitle = new JLabel("Academic Staff");

        staffTitle.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        staffTitle.setForeground(MUTED);
        staffTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lecturerPanel.add(avatar);
        lecturerPanel.add(Box.createVerticalStrut(8));
        lecturerPanel.add(changePhotoButton);
        lecturerPanel.add(Box.createVerticalStrut(10));
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
                (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId : "-"
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
            changePhotoButton.setVisible(true);

            editButton.setVisible(false);
            saveButton.setVisible(true);
            cancelButton.setVisible(true);
        });

        saveButton.addActionListener(e -> saveProfile());

        cancelButton.addActionListener(e -> {
            restoreOriginalValues();
            setFieldsEditable(false);
            changePhotoButton.setVisible(false);

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

    // ================= DATABASE OPERATIONS =================

    /**
     * Loads the profile details for the active lecturer from MySQL.
     */
    private void loadProfileFromDatabase() {
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            showEmptyProfileState("No active lecturer session found. Please log in or provide a lecturer ID.");
            return;
        }

        String sql = "SELECT l.lecturer_id, l.staff_no, l.department, l.designation, " +
                     "       u.first_name, u.last_name, u.email, u.phone, u.profile_picture " +
                     "FROM lecturers l " +
                     "LEFT JOIN users u ON l.lecturer_id = u.user_id " +
                     "WHERE l.lecturer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("lecturer_id");
                    String dept = rs.getString("department");
                    String desig = rs.getString("designation");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");
                    String photo = rs.getString("profile_picture");

                    String fullName = "";
                    if (firstName != null) {
                        fullName += firstName.trim();
                    }
                    if (lastName != null && !lastName.trim().isEmpty()) {
                        fullName = fullName.isEmpty() ? lastName.trim() : (fullName + " " + lastName.trim());
                    }
                    if (!fullName.isEmpty() && lecturerId != null) {
                        LecturerTheme.setLecturerDisplayName(lecturerId, fullName);
                    }

                    idField.setText(id != null ? id : "");
                    nameField.setText(fullName);
                    emailField.setText(email != null ? email : "");
                    phoneField.setText(phone != null ? phone : "");
                    departmentField.setText(dept != null ? dept : "");
                    designationField.setText(desig != null ? desig : "");

                    if (desig != null && !desig.trim().isEmpty()) {
                        lecturerTitle.setText(desig.trim());
                    } else {
                        lecturerTitle.setText("Lecturer");
                    }

                    currentPhotoPath = photo;
                    pendingPhotoFile = null;
                    updateAvatarDisplay(currentPhotoPath, fullName);

                    saveOriginalValues();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Lecturer profile record not found in MySQL database for ID: " + lecturerId,
                            "Profile Not Found",
                            JOptionPane.WARNING_MESSAGE
                    );
                    showEmptyProfileState("Lecturer record not found for ID: " + lecturerId);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load profile from MySQL database: " + ex.getMessage(),
                    "MySQL Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showEmptyProfileState(String message) {
        idField.setText(lecturerId != null ? lecturerId : "-");
        nameField.setText("-");
        emailField.setText("-");
        phoneField.setText("-");
        departmentField.setText("-");
        designationField.setText("-");
        lecturerTitle.setText("Lecturer");
        avatar.setIcon(null);
        avatar.setText("?");
        saveOriginalValues();
    }

    private void updateAvatarDisplay(String photoPath, String name) {
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            File photoFile = new File(photoPath.trim());
            if (photoFile.exists() && photoFile.isFile()) {
                try {
                    ImageIcon rawIcon = new ImageIcon(photoFile.getAbsolutePath());
                    Image rawImg = rawIcon.getImage();
                    Image scaled = rawImg.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                    avatar.setIcon(new ImageIcon(scaled));
                    avatar.setText("");
                    avatar.setOpaque(false);
                    return;
                } catch (Exception ignored) {
                }
            }
        }

        // Default avatar fallback with initial
        avatar.setIcon(null);
        avatar.setOpaque(true);
        avatar.setBackground(NAVY);
        avatar.setForeground(WHITE);
        String initial = "L";
        if (name != null && !name.trim().isEmpty()) {
            initial = name.trim().substring(0, 1).toUpperCase();
        }
        avatar.setText(initial);
    }

    private void selectProfilePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Image files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            if (!chosen.exists() || !chosen.isFile()) {
                JOptionPane.showMessageDialog(this, "The selected file does not exist.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String fileName = chosen.getName().toLowerCase();
            if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                JOptionPane.showMessageDialog(this, "Please select a valid image file (.png, .jpg, or .jpeg).", "Invalid Image Type", JOptionPane.WARNING_MESSAGE);
                return;
            }
            pendingPhotoFile = chosen;
            updateAvatarDisplay(chosen.getAbsolutePath(), nameField.getText());
        }
    }

    // ================= EDIT FIELDS =================

    private void setFieldsEditable(boolean editable) {
        // Editable fields
        nameField.setEditable(editable);
        emailField.setEditable(editable);
        phoneField.setEditable(editable);

        // Protected institutional fields: always non-editable
        idField.setEditable(false);
        departmentField.setEditable(false);
        designationField.setEditable(false);

        Color editableBg = editable
                ? new Color(250, 252, 255)
                : BACKGROUND;

        nameField.setBackground(editableBg);
        emailField.setBackground(editableBg);
        phoneField.setBackground(editableBg);

        idField.setBackground(BACKGROUND);
        departmentField.setBackground(BACKGROUND);
        designationField.setBackground(BACKGROUND);
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
        pendingPhotoFile = null;
        updateAvatarDisplay(currentPhotoPath, savedName);
    }

    // ================= SAVE PROFILE =================

    private void saveProfile() {
        if (lecturerId == null || lecturerId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot update profile: No active lecturer session identified.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String rawName = nameField.getText().trim();
        if (rawName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Full Name is required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            nameField.requestFocus();
            return;
        }

        String rawEmail = emailField.getText().trim();
        if (rawEmail.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Email Address is required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            emailField.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(rawEmail).matches()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid email address (e.g. lecturer@university.edu).",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            emailField.requestFocus();
            return;
        }

        String rawPhone = phoneField.getText().trim();
        if (!rawPhone.isEmpty() && !PHONE_PATTERN.matcher(rawPhone).matches()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid phone number format (e.g. 0710000002).",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            phoneField.requestFocus();
            return;
        }

        // Handle profile photo copying if a new photo was selected
        String newPhotoPath = currentPhotoPath;
        if (pendingPhotoFile != null) {
            try {
                File uploadDir = new File("uploads/profile_pictures");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String ext = "png";
                String fn = pendingPhotoFile.getName();
                int dotIdx = fn.lastIndexOf('.');
                if (dotIdx > 0 && dotIdx < fn.length() - 1) {
                    ext = fn.substring(dotIdx + 1).toLowerCase();
                }
                File dest = new File(uploadDir, lecturerId + "_" + System.currentTimeMillis() + "." + ext);
                Files.copy(pendingPhotoFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                newPhotoPath = dest.getPath();
            } catch (IOException ioEx) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to save profile photo: " + ioEx.getMessage(),
                        "File Storage Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        // Split name into first and last name for users table
        String firstName = rawName;
        String lastName = "";
        int spaceIdx = rawName.indexOf(' ');
        if (spaceIdx > 0) {
            firstName = rawName.substring(0, spaceIdx).trim();
            lastName = rawName.substring(spaceIdx + 1).trim();
        }

        String updateSql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, profile_picture = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, rawEmail);
            ps.setString(4, rawPhone.isEmpty() ? null : rawPhone);
            ps.setString(5, newPhotoPath);
            ps.setString(6, lecturerId);

            int affected = ps.executeUpdate();

            if (affected > 0) {
                currentPhotoPath = newPhotoPath;
                pendingPhotoFile = null;

                saveOriginalValues();
                setFieldsEditable(false);
                changePhotoButton.setVisible(false);

                editButton.setVisible(true);
                saveButton.setVisible(false);
                cancelButton.setVisible(false);

                JOptionPane.showMessageDialog(
                        this,
                        "Profile updated successfully in MySQL database.",
                        "Save Profile",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Refresh profile data from MySQL
                loadProfileFromDatabase();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No profile changes were saved. Lecturer record may not exist in users table.",
                        "Save Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database error while saving profile to MySQL: " + ex.getMessage(),
                    "MySQL Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= BUTTON STYLE =================

    private void styleButton(JButton button) {

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(LecturerTheme.SECONDARY_BLUE);
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

    // ================= LOGOUT =================

    private void logout() {

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (answer == JOptionPane.YES_OPTION) {
            LecturerDashboard.setActiveLecturerId(null);
            dispose();

            JOptionPane.showMessageDialog(
                    null,
                    "You have been logged out."
            );
        }
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {
        String testId = (args != null && args.length > 0) ? args[0] : null;
        SwingUtilities.invokeLater(() -> {
            LecturerProfile profile = (testId != null)
                    ? new LecturerProfile(testId)
                    : new LecturerProfile();
            profile.setVisible(true);
        });
    }
}
