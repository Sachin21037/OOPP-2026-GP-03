package ui.lecturer;

import dao.LecturerProfileDAO;
import dao.LecturerProfileDAO.ProfileData;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * LecturerProfileUI - Presentation layer for Lecturer Profile Management.
 * Accurately styled to match the Faculty Academic Management System UI specifications.
 */
public class LecturerProfileUI extends JFrame {

    private final Color PRIMARY_NAVY = LecturerTheme.PRIMARY_NAVY;
    private final Color BACKGROUND = new Color(248, 250, 252);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color BORDER_COLOR = new Color(226, 232, 240);
    private final Color ROYAL_BLUE = new Color(29, 78, 216);

    protected String lecturerId;
    private JPanel headerPanel;

    private JLabel nameValueLabel;
    private JLabel idValueLabel;
    private JLabel emailValueLabel;
    private JLabel phoneValueLabel;
    private JLabel departmentValueLabel;
    private JLabel designationValueLabel;

    private JLabel lecturerTitle;
    private JLabel staffTitle;
    private AvatarBox avatarBox;

    private String currentPhotoPath = null;
    private ProfileData currentProfileData = null;

    public LecturerProfileUI() {
        this(resolveActiveLecturerId());
    }

    public LecturerProfileUI(String lecturerId) {
        this.lecturerId = (lecturerId != null && !lecturerId.trim().isEmpty())
                ? lecturerId.trim()
                : null;

        if (this.lecturerId != null) {
            LecturerDashboardUI.setActiveLecturerId(this.lecturerId);
        }

        setTitle("Faculty Academic Management System - Lecturer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createProfileContent(), BorderLayout.CENTER);

        loadProfileFromDatabase();
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
        return null;
    }

    private JPanel createHeader() {
        return LecturerTheme.createHeader(lecturerId, this);
    }

    private JPanel createSidebar() {
        return LecturerTheme.createSidebar("Profile", lecturerId, this);
    }

    private JPanel createProfileContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Heading panel (Title + Subtitle)
        JPanel headingPanel = new JPanel();
        headingPanel.setBackground(BACKGROUND);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("View and manage your lecturer information.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);

        headingPanel.add(title);
        headingPanel.add(Box.createVerticalStrut(4));
        headingPanel.add(sub);
        content.add(headingPanel, BorderLayout.NORTH);

        // Center Area containing the Card and the Bottom Bar
        JPanel centerArea = new JPanel(new BorderLayout(0, 16));
        centerArea.setBackground(BACKGROUND);

        // 2. Main White Profile Card
        JPanel card = new JPanel(new BorderLayout(32, 0));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(32, 32, 32, 32)
        ));

        // Left Avatar Column
        JPanel leftCol = new JPanel();
        leftCol.setBackground(WHITE);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setPreferredSize(new Dimension(190, 0));

        avatarBox = new AvatarBox();
        avatarBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        lecturerTitle = new JLabel("Senior Lecturer", SwingConstants.CENTER);
        lecturerTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lecturerTitle.setForeground(TEXT_PRIMARY);
        lecturerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        staffTitle = new JLabel("LEC-STF-001", SwingConstants.CENTER);
        staffTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        staffTitle.setForeground(TEXT_MUTED);
        staffTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftCol.add(Box.createVerticalStrut(8));
        leftCol.add(avatarBox);
        leftCol.add(Box.createVerticalStrut(14));
        leftCol.add(lecturerTitle);
        leftCol.add(Box.createVerticalStrut(4));
        leftCol.add(staffTitle);
        leftCol.add(Box.createVerticalGlue());

        card.add(leftCol, BorderLayout.WEST);

        // Right Section: 6 Info Boxes in 3x2 Grid
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 16, 16));
        gridPanel.setBackground(WHITE);

        nameValueLabel = new JLabel("Kasun Perera");
        idValueLabel = new JLabel("LEC001");
        emailValueLabel = new JLabel("kasun.perera@uor.lk");
        phoneValueLabel = new JLabel("0718596874");
        departmentValueLabel = new JLabel("Information and Communication Technology");
        designationValueLabel = new JLabel("Senior Lecturer");

        gridPanel.add(createInfoBox("Full Name", nameValueLabel));
        gridPanel.add(createInfoBox("Lecturer ID", idValueLabel));
        gridPanel.add(createInfoBox("Email Address", emailValueLabel));
        gridPanel.add(createInfoBox("Phone Number", phoneValueLabel));
        gridPanel.add(createInfoBox("Department", departmentValueLabel));
        gridPanel.add(createInfoBox("Designation", designationValueLabel));

        card.add(gridPanel, BorderLayout.CENTER);
        centerArea.add(card, BorderLayout.CENTER);

        // 3. Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JButton editProfileBtn = createStyledButton("Edit Profile", ROYAL_BLUE, WHITE, 125);
        editProfileBtn.addActionListener(e -> openEditProfileDialog());
        bottomBar.add(editProfileBtn, BorderLayout.EAST);

        centerArea.add(bottomBar, BorderLayout.SOUTH);
        content.add(centerArea, BorderLayout.CENTER);

        return content;
    }

    private JPanel createInfoBox(String labelText, JLabel valueLabel) {
        JPanel box = new JPanel(new BorderLayout(0, 10));
        box.setBackground(new Color(248, 250, 252));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_PRIMARY);

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(TEXT_PRIMARY);

        box.add(lbl, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);

        return box;
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Math.max(minWidth, 100), 36));
        return btn;
    }

    public void loadProfileFromDatabase() {
        try {
            ProfileData data = LecturerProfileDAO.loadProfile(lecturerId);
            if (data != null) {
                currentProfileData = data;
                idValueLabel.setText(data.id.isEmpty() ? "-" : data.id);
                nameValueLabel.setText(data.fullName.isEmpty() ? "-" : data.fullName);
                emailValueLabel.setText(data.email.isEmpty() ? "-" : data.email);
                phoneValueLabel.setText(data.phone.isEmpty() ? "-" : data.phone);
                departmentValueLabel.setText(data.department.isEmpty() ? "-" : data.department);
                designationValueLabel.setText(data.designation.isEmpty() ? "-" : data.designation);

                lecturerTitle.setText(!data.designation.isEmpty() ? data.designation : "Senior Lecturer");
                staffTitle.setText(!data.staffNo.isEmpty() ? data.staffNo : "LEC-STF-001");

                String initial = "K";
                if (!data.fullName.isEmpty()) {
                    initial = data.fullName.substring(0, 1).toUpperCase();
                    if (lecturerId != null) {
                        LecturerTheme.setLecturerDisplayName(lecturerId, data.fullName);
                    }
                }
                avatarBox.setInitial(initial);
                avatarBox.setPhotoPath(data.photoPath);
                currentPhotoPath = data.photoPath;
            } else {
                idValueLabel.setText(lecturerId != null ? lecturerId : "LEC001");
                nameValueLabel.setText("Kasun Perera");
                emailValueLabel.setText("kasun.perera@uor.lk");
                phoneValueLabel.setText("0718596874");
                departmentValueLabel.setText("Information and Communication Technology");
                designationValueLabel.setText("Senior Lecturer");
            }
        } catch (SQLException ex) {
            System.err.println("[LecturerProfileUI] DB Error: " + ex.getMessage());
        }
    }

    private void openEditProfileDialog() {
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(WHITE);

        // Header
        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBackground(WHITE);
        head.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel titleLbl = new JLabel("Edit Profile");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel("Update your personal and academic details in the database.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(TEXT_MUTED);

        head.add(titleLbl);
        head.add(Box.createVerticalStrut(4));
        head.add(subLbl);
        dialog.add(head, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(6, 24, 12, 24));

        JTextField nameField = createDialogField(currentProfileData != null ? currentProfileData.fullName : nameValueLabel.getText());
        JTextField emailField = createDialogField(currentProfileData != null ? currentProfileData.email : emailValueLabel.getText());
        JTextField phoneField = createDialogField(currentProfileData != null ? currentProfileData.phone : phoneValueLabel.getText());
        JTextField deptField = createDialogField(currentProfileData != null ? currentProfileData.department : departmentValueLabel.getText());
        JTextField desigField = createDialogField(currentProfileData != null ? currentProfileData.designation : designationValueLabel.getText());

        form.add(createDialogRow("Full Name:", nameField));
        form.add(Box.createVerticalStrut(10));
        form.add(createDialogRow("Email Address:", emailField));
        form.add(Box.createVerticalStrut(10));
        form.add(createDialogRow("Phone Number:", phoneField));
        form.add(Box.createVerticalStrut(10));
        form.add(createDialogRow("Department:", deptField));
        form.add(Box.createVerticalStrut(10));
        form.add(createDialogRow("Designation:", desigField));
        form.add(Box.createVerticalStrut(10));

        // Photo selector row
        final File[] chosenPhoto = new File[1];
        JPanel photoRow = new JPanel(new BorderLayout(8, 0));
        photoRow.setBackground(WHITE);
        JLabel photoNameLbl = new JLabel(currentPhotoPath != null ? new File(currentPhotoPath).getName() : "No photo chosen");
        photoNameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        photoNameLbl.setForeground(TEXT_MUTED);

        JButton browseBtn = createStyledButton("Browse Photo...", WHITE, TEXT_PRIMARY, 120);
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                chosenPhoto[0] = fc.getSelectedFile();
                photoNameLbl.setText(chosenPhoto[0].getName());
            }
        });
        photoRow.add(photoNameLbl, BorderLayout.CENTER);
        photoRow.add(browseBtn, BorderLayout.EAST);
        form.add(createDialogRow("Profile Picture (Optional):", photoRow));

        dialog.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 16));
        btnPanel.setBackground(WHITE);
        btnPanel.setBorder(new EmptyBorder(0, 24, 16, 24));

        JButton cancelBtn = createStyledButton("Cancel", WHITE, TEXT_PRIMARY, 85);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Save Changes", ROYAL_BLUE, WHITE, 120);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String dept = deptField.getText().trim();
            String desig = desigField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Full Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Email Address is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String newPhoto = LecturerProfileDAO.updateProfile(
                        lecturerId, name, email, phone, dept, desig,
                        chosenPhoto[0], currentPhotoPath
                );
                currentPhotoPath = newPhoto;

                // Update lecturer display name in the session theme
                if (lecturerId != null) {
                    LecturerTheme.setLecturerDisplayName(lecturerId, name);
                }

                // Refresh top header to reflect name change
                remove(headerPanel);
                headerPanel = createHeader();
                add(headerPanel, BorderLayout.NORTH);
                revalidate();
                repaint();

                loadProfileFromDatabase();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException valEx) {
                JOptionPane.showMessageDialog(dialog, valEx.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException | IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating profile: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createDialogRow(String labelText, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(WHITE);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_PRIMARY);
        row.add(lbl, BorderLayout.NORTH);
        row.add(input, BorderLayout.CENTER);
        return row;
    }

    private JTextField createDialogField(String text) {
        JTextField tf = new JTextField(text != null && !"-".equals(text) ? text : "");
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(0, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    public void saveProfile() {
        openEditProfileDialog();
    }

    private static class AvatarBox extends JComponent {
        private String initial = "K";
        private String photoPath = null;
        private Image photoImg = null;

        public AvatarBox() {
            setPreferredSize(new Dimension(96, 96));
            setMaximumSize(new Dimension(96, 96));
            setMinimumSize(new Dimension(96, 96));
        }

        public void setInitial(String initial) {
            this.initial = (initial != null && !initial.isEmpty()) ? initial : "K";
            repaint();
        }

        public void setPhotoPath(String path) {
            this.photoPath = path;
            this.photoImg = null;
            if (path != null && !path.trim().isEmpty()) {
                File f = new File(path);
                if (f.exists()) {
                    try {
                        this.photoImg = ImageIO.read(f);
                    } catch (Exception ignored) {
                    }
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(15, 23, 42)); // Dark Navy #0F172A
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            if (photoImg != null) {
                Shape oldClip = g2.getClip();
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                g2.drawImage(photoImg, 0, 0, w, h, null);
                g2.setClip(oldClip);
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 44));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(initial)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, tx, ty);
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        String testId = args != null && args.length > 0 ? args[0] : null;
        if (testId != null) {
            LecturerDashboardUI.setActiveLecturerId(testId);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LecturerProfileUI(testId).setVisible(true);
        });
    }
}
