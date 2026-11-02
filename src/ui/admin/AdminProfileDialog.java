package ui.admin;

import dao.UserDAO;
import model.UserAccount;
import ui.util.UIAnimationHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Modern Administrator Profile Dialog for FAMS.
 * Allows the administrator to edit personal credentials, contact info,
 * department / office location, and change account password.
 */
public class AdminProfileDialog extends JDialog {

    // Harmonized Palette matching Soft Sage Green Theme
    private static final Color PRIMARY_NAVY    = new Color(26, 83, 71);    // #1A5347 Velvety Deep Sage Pine
    private static final Color SECONDARY_BLUE  = new Color(42, 119, 102);  // #2A7766 Soft Sage Green Accent
    private static final Color BG_MAIN         = new Color(239, 245, 242); // #EFF5F2 Soft Sage Canvas
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color TEXT_PRIMARY    = new Color(26, 56, 48);    // #1A3830 Deep Sage Slate
    private static final Color TEXT_SECONDARY  = new Color(93, 122, 114);  // #5D7A72 Muted Sage Slate
    private static final Color BORDER_COLOR    = new Color(210, 228, 220); // #D2E4DC Soft Sage Border
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A
    private static final Color DANGER_COLOR    = new Color(190, 18, 60);   // #BE123C Refined Rose Crimson
    private static final Color PURPLE_ACCENT   = new Color(42, 119, 102);

    private final String adminUserId;
    private final Consumer<UserAccount> onProfileUpdated;
    private final UserDAO userDAO = new UserDAO();

    // Form Controls
    private JTextField txtUserId;
    private JTextField txtUsername;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtDepartment;
    private JTextField txtAddress;

    // Security / Password Fields
    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private boolean isPasswordRevealed = false;

    // Header Display Elements
    private JLabel lblHeaderName;
    private JLabel lblHeaderEmail;
    private JPanel avatarPanel;

    public AdminProfileDialog(Window parent, String adminUserId, Consumer<UserAccount> onProfileUpdated) {
        super(parent, "Administrator Profile & Account Settings", ModalityType.APPLICATION_MODAL);
        this.adminUserId = (adminUserId != null && !adminUserId.isBlank()) ? adminUserId : "ADM001";
        this.onProfileUpdated = onProfileUpdated;

        initUI();
        loadProfileData();
    }

    private void initUI() {
        setSize(740, 720);
        setMinimumSize(new Dimension(680, 650));
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        // 1. Top Header Banner
        JPanel header = createHeaderBanner();
        add(header, BorderLayout.NORTH);

        // 2. Center Scrollable Form
        JPanel formContainer = createFormContainer();
        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Footer Action Buttons
        JPanel footer = createFooterPanel();
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createHeaderBanner() {
        JPanel banner = new JPanel(new BorderLayout(18, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_NAVY, getWidth(), getHeight(), SECONDARY_BLUE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        banner.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Left Avatar with halo ring
        avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = 60;
                GradientPaint ring = new GradientPaint(0, 0, new Color(244, 63, 94), d, d, new Color(139, 92, 246));
                g2.setPaint(ring);
                g2.fillOval(0, 0, d, d);

                g2.setColor(new Color(24, 34, 53));
                g2.fillOval(3, 3, d - 6, d - 6);

                g2.setColor(Color.WHITE);
                g2.setFont(AdminDashboard.getModernFont(Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String initials = getInitials(lblHeaderName != null ? lblHeaderName.getText() : "SA");
                int tx = (d - fm.stringWidth(initials)) / 2;
                int ty = ((d - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, tx, ty);

                // Online indicator
                g2.setColor(new Color(16, 185, 129));
                g2.fillOval(d - 14, d - 14, 13, 13);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(d - 14, d - 14, 13, 13);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(60, 60));
        banner.add(avatarPanel, BorderLayout.WEST);

        // Center Profile Info
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        lblHeaderName = new JLabel("System Administrator");
        lblHeaderName.setFont(AdminDashboard.getModernFont(Font.BOLD, 18));
        lblHeaderName.setForeground(Color.WHITE);

        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        roleRow.setOpaque(false);

        JLabel badge = new JLabel("  SYSTEM ADMIN • ACTIVE  ");
        badge.setFont(AdminDashboard.getModernFont(Font.BOLD, 10));
        badge.setForeground(new Color(147, 197, 253));
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 255, 255, 30));
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        JLabel lblId = new JLabel("Account: " + adminUserId);
        lblId.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        lblId.setForeground(new Color(203, 213, 225));

        roleRow.add(badge);
        roleRow.add(lblId);

        lblHeaderEmail = new JLabel("admin@fams.uor.lk");
        lblHeaderEmail.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        lblHeaderEmail.setForeground(new Color(226, 232, 240));

        infoPanel.add(lblHeaderName);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(roleRow);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblHeaderEmail);

        banner.add(infoPanel, BorderLayout.CENTER);
        return banner;
    }

    private JPanel createFormContainer() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(18, 26, 18, 26));

        // CARD 1: Personal & Contact Information
        JPanel cardPersonal = createSectionCard("Personal & Contact Details", "Basic administrative profile and university department coordinates.");

        JPanel pGrid = new JPanel(new GridLayout(0, 2, 14, 12));
        pGrid.setOpaque(false);

        txtUserId = createStyledField(false);
        pGrid.add(createFormField("Administrator ID", txtUserId));

        txtUsername = createStyledField(false);
        pGrid.add(createFormField("System Username", txtUsername));

        txtFirstName = createStyledField(true);
        pGrid.add(createFormField("First Name *", txtFirstName));

        txtLastName = createStyledField(true);
        pGrid.add(createFormField("Last Name *", txtLastName));

        txtEmail = createStyledField(true);
        pGrid.add(createFormField("Email Address *", txtEmail));

        txtPhone = createStyledField(true);
        pGrid.add(createFormField("Contact Phone", txtPhone));

        txtDepartment = createStyledField(true);
        pGrid.add(createFormField("Department / Office", txtDepartment));

        txtAddress = createStyledField(true);
        pGrid.add(createFormField("Faculty / Campus Office", txtAddress));

        cardPersonal.add(pGrid, BorderLayout.CENTER);
        container.add(cardPersonal);
        container.add(Box.createVerticalStrut(16));

        // CARD 2: Security & Password Management (Optional)
        JPanel cardSecurity = createSectionCard("Security & Password Management", "Leave these fields blank if you do not wish to change your login password.");

        JPanel sGrid = new JPanel(new GridLayout(0, 3, 12, 10));
        sGrid.setOpaque(false);

        txtCurrentPassword = createPasswordField();
        sGrid.add(createFormField("Current Password", txtCurrentPassword));

        txtNewPassword = createPasswordField();
        sGrid.add(createFormField("New Password", txtNewPassword));

        txtConfirmPassword = createPasswordField();
        sGrid.add(createFormField("Confirm New Password", txtConfirmPassword));

        JPanel secWrapper = new JPanel(new BorderLayout(0, 10));
        secWrapper.setOpaque(false);
        secWrapper.add(sGrid, BorderLayout.CENTER);

        // Show / Hide Password toggle
        JCheckBox chkShow = new JCheckBox("Show password characters");
        chkShow.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        chkShow.setForeground(TEXT_SECONDARY);
        chkShow.setOpaque(false);
        chkShow.setFocusPainted(false);
        chkShow.addActionListener(e -> {
            isPasswordRevealed = chkShow.isSelected();
            char echo = isPasswordRevealed ? (char) 0 : '•';
            txtCurrentPassword.setEchoChar(echo);
            txtNewPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });
        secWrapper.add(chkShow, BorderLayout.SOUTH);

        cardSecurity.add(secWrapper, BorderLayout.CENTER);
        container.add(cardSecurity);

        return container;
    }

    private JPanel createSectionCard(String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 14));
        tLbl.setForeground(TEXT_PRIMARY);

        JLabel sLbl = new JLabel(subtitle);
        sLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        sLbl.setForeground(TEXT_SECONDARY);

        titleBox.add(tLbl);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sLbl);

        card.add(titleBox, BorderLayout.NORTH);
        return card;
    }

    private JPanel createFormField(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(comp);
        return p;
    }

    private JTextField createStyledField(boolean editable) {
        JTextField tf = new JTextField();
        tf.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setEditable(editable);
        tf.setBackground(editable ? Color.WHITE : new Color(248, 250, 252));
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
        tf.setPreferredSize(new Dimension(160, 36));
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(TEXT_PRIMARY);
        pf.setBackground(Color.WHITE);
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
        pf.setPreferredSize(new Dimension(160, 36));
        return pf;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel hintLbl = new JLabel("Changes take effect immediately across all system logs.");
        hintLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        hintLbl.setForeground(TEXT_SECONDARY);
        footer.add(hintLbl, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = UIAnimationHelper.createAnimatedSecondaryButton("Cancel", TEXT_SECONDARY, BORDER_COLOR, 20, e -> dispose());
        JButton btnSave = UIAnimationHelper.createAnimatedPillButton("💾 Save Profile Changes", SECONDARY_BLUE, 20, e -> saveProfile());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        footer.add(btnPanel, BorderLayout.EAST);

        return footer;
    }

    private void loadProfileData() {
        SwingWorker<UserAccount, Void> worker = new SwingWorker<>() {
            @Override
            protected UserAccount doInBackground() {
                return userDAO.getUserById(adminUserId);
            }

            @Override
            protected void done() {
                try {
                    UserAccount user = get();
                    if (user != null) {
                        txtUserId.setText(user.getUserId());
                        txtUsername.setText(user.getUsername());
                        txtFirstName.setText(user.getFirstName());
                        txtLastName.setText(user.getLastName());
                        txtEmail.setText(user.getEmail());
                        txtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                        txtDepartment.setText(user.getDepartment() != null ? user.getDepartment() : "Dean Office / Administration");
                        txtAddress.setText(user.getAddress() != null ? user.getAddress() : "Faculty of Technology, University of Ruhuna");

                        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
                        lblHeaderName.setText(!fullName.isEmpty() ? fullName : "System Administrator");
                        lblHeaderEmail.setText(user.getEmail() != null ? user.getEmail() : "admin@fams.uor.lk");
                        if (avatarPanel != null) avatarPanel.repaint();
                    }
                } catch (Exception ex) {
                    System.err.println("[AdminProfileDialog] Error loading profile: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveProfile() {
        String fn = txtFirstName.getText().trim();
        String ln = txtLastName.getText().trim();
        String email = txtEmail.getText().trim().toLowerCase();
        String phone = txtPhone.getText().trim();
        String dept = txtDepartment.getText().trim();
        String addr = txtAddress.getText().trim();

        if (fn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFirstName.requestFocusInWindow();
            return;
        }

        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocusInWindow();
            return;
        }

        // Check password change request
        String currentPass = new String(txtCurrentPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        String passwordToUpdate = null;
        if (!newPass.isEmpty() || !currentPass.isEmpty() || !confirmPass.isEmpty()) {
            if (currentPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your Current Password to authorize password change.", "Security Verification", JOptionPane.WARNING_MESSAGE);
                txtCurrentPassword.requestFocusInWindow();
                return;
            }

            if (!userDAO.verifyPassword(adminUserId, currentPass)) {
                JOptionPane.showMessageDialog(this, "Current password does not match. Please verify your password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                txtCurrentPassword.requestFocusInWindow();
                return;
            }

            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Password Policy", JOptionPane.WARNING_MESSAGE);
                txtNewPassword.requestFocusInWindow();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "New password and Confirm Password do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
                txtConfirmPassword.requestFocusInWindow();
                return;
            }

            passwordToUpdate = newPass;
        }

        final String finalPass = passwordToUpdate;
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userDAO.updateAdminProfile(adminUserId, fn, ln, email, phone, addr, dept, finalPass);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(
                                AdminProfileDialog.this,
                                "Administrator profile updated successfully!",
                                "Profile Saved",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        if (onProfileUpdated != null) {
                            UserAccount updated = userDAO.getUserById(adminUserId);
                            onProfileUpdated.accept(updated);
                        }

                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(
                                AdminProfileDialog.this,
                                "Failed to update profile. Please check database connectivity.",
                                "Update Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AdminProfileDialog.this,
                            "Error saving profile: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "SA";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        } else if (name.length() >= 2) {
            return name.substring(0, 2).toUpperCase();
        }
        return name.toUpperCase();
    }
}

