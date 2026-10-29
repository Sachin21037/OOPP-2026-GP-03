package ui.admin;

import model.TechnicalOfficer;
import service.TechnicalOfficerService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modal Dialog for Creating and Editing Technical Officer records in FAMS.
 * Implements field validation, password show/hide, and consistent Admin Dashboard styling.
 */
public class TechnicalOfficerDialog extends JDialog {

    // Palette harmonized with AdminDashboard & LecturerDialog
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color WARNING_COLOR   = new Color(217, 119, 6);   // #D97706
    private static final Color LIGHT_BG        = new Color(254, 243, 199); // #FEF3C7
    private static final Color BG_MAIN         = new Color(248, 250, 252); // #F8FAFC
    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_MUTED      = new Color(100, 116, 139); // #64748B
    private static final Color BORDER_COLOR    = new Color(203, 213, 225); // #CBD5E1
    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // #DC2626
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A

    private final boolean isEditMode;
    private final TechnicalOfficer existingOfficer;
    private boolean saved = false;

    // Form Fields
    private JTextField txtOfficerId;
    private JTextField txtStaffNo;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnTogglePassword;
    private boolean isPasswordVisible = false;

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;

    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbAccountStatus;

    private JLabel lblErrorBanner;

    public TechnicalOfficerDialog(Window parent, TechnicalOfficer officerToEdit) {
        super(parent, officerToEdit == null ? "Add New Technical Officer" : "Edit Technical Officer Details", ModalityType.APPLICATION_MODAL);
        this.existingOfficer = officerToEdit;
        this.isEditMode = (officerToEdit != null);

        initUI();
        if (isEditMode) {
            populateFields(officerToEdit);
        } else {
            // Suggest next available ID & Staff No
            String nextId = TechnicalOfficerService.getInstance().getNextSuggestedTechnicalOfficerId();
            String nextStaffNo = TechnicalOfficerService.getInstance().getNextSuggestedStaffNo();
            txtOfficerId.setText(nextId);
            txtStaffNo.setText(nextStaffNo);
            txtUsername.setText(nextId.toLowerCase());
        }
    }

    private void initUI() {
        setSize(780, 650);
        setMinimumSize(new Dimension(720, 580));
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        // 1. Header Banner
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 2. Scrollable Form Content
        JPanel formContent = createFormContent();
        JScrollPane scrollPane = new JScrollPane(formContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Action Buttons (Footer)
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_NAVY, getWidth(), 0, SECONDARY_BLUE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(isEditMode ? "Edit Technical Officer Profile" : "Register New Technical Officer");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(isEditMode
                ? "Update technical staff profile, laboratory department assignments, and system access."
                : "Enter credentials, departmental assignment, and contact details for new technical staff.");
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        subLbl.setForeground(new Color(254, 243, 199));

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subLbl);

        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFormContent() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 26, 20, 26));

        // Error message banner
        lblErrorBanner = new JLabel("");
        lblErrorBanner.setOpaque(true);
        lblErrorBanner.setBackground(new Color(254, 242, 242));
        lblErrorBanner.setForeground(DANGER_COLOR);
        lblErrorBanner.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        lblErrorBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(252, 165, 165), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        lblErrorBanner.setVisible(false);
        lblErrorBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblErrorBanner);
        form.add(Box.createVerticalStrut(14));

        // SECTION 1: Staff Identification & Department
        form.add(createSectionHeader("Staff Identification & Department Assignment"));
        form.add(Box.createVerticalStrut(10));

        JPanel idGrid = new JPanel(new GridLayout(1, 3, 16, 12));
        idGrid.setOpaque(false);
        idGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        idGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtOfficerId = createStyledTextField();
        if (isEditMode) {
            txtOfficerId.setEditable(false);
            txtOfficerId.setBackground(new Color(241, 245, 249));
            txtOfficerId.setToolTipText("Technical Officer ID cannot be changed after creation.");
        }
        idGrid.add(createFieldWrapper("Officer ID *", txtOfficerId, "Primary Key (e.g. TO001)"));

        txtStaffNo = createStyledTextField();
        idGrid.add(createFieldWrapper("Staff Number *", txtStaffNo, "Faculty Staff ID (e.g. TO-STF-001)"));

        String[] departments = {
                "Information and Communication Technology",
                "Engineering Technology",
                "Biosystems Technology",
                "Multidisciplinary Studies"
        };
        cmbDepartment = new JComboBox<>(departments);
        styleComboBox(cmbDepartment);
        idGrid.add(createFieldWrapper("Department *", cmbDepartment, "Assigned laboratory department"));

        form.add(idGrid);
        form.add(Box.createVerticalStrut(18));

        // SECTION 2: User Account & Authentication
        form.add(createSectionHeader("User Account & Security Access"));
        form.add(Box.createVerticalStrut(10));

        JPanel accountGrid = new JPanel(new GridLayout(1, 3, 16, 12));
        accountGrid.setOpaque(false);
        accountGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        accountGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = createStyledTextField();
        accountGrid.add(createFieldWrapper("Username *", txtUsername, "Sign-in username"));

        // Password with toggle
        JPanel pwdPanel = new JPanel(new BorderLayout(4, 0));
        pwdPanel.setOpaque(false);
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        txtPassword.setBackground(Color.WHITE);

        btnTogglePassword = new JButton("👁");
        btnTogglePassword.setFocusPainted(false);
        btnTogglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePassword.setPreferredSize(new Dimension(36, 32));
        btnTogglePassword.setToolTipText("Show / Hide password");
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility());
        pwdPanel.add(txtPassword, BorderLayout.CENTER);
        pwdPanel.add(btnTogglePassword, BorderLayout.EAST);

        String pwdDesc = isEditMode ? "Leave blank to keep current password" : "At least 4 characters";
        accountGrid.add(createFieldWrapper(isEditMode ? "New Password" : "Password *", pwdPanel, pwdDesc));

        String[] accountStatuses = {"ACTIVE", "INACTIVE"};
        cmbAccountStatus = new JComboBox<>(accountStatuses);
        styleComboBox(cmbAccountStatus);
        accountGrid.add(createFieldWrapper("Account Status *", cmbAccountStatus, "Portal access status"));

        form.add(accountGrid);
        form.add(Box.createVerticalStrut(18));

        // SECTION 3: Personal & Contact Details
        form.add(createSectionHeader("Personal & Contact Details"));
        form.add(Box.createVerticalStrut(10));

        JPanel personalGrid = new JPanel(new GridLayout(2, 2, 16, 12));
        personalGrid.setOpaque(false);
        personalGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 136));
        personalGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtFirstName = createStyledTextField();
        personalGrid.add(createFieldWrapper("First Name *", txtFirstName, "Officer's first name"));

        txtLastName = createStyledTextField();
        personalGrid.add(createFieldWrapper("Last Name *", txtLastName, "Officer's last name"));

        txtEmail = createStyledTextField();
        personalGrid.add(createFieldWrapper("Email Address", txtEmail, "Official email (e.g. name@uor.lk)"));

        txtPhone = createStyledTextField();
        personalGrid.add(createFieldWrapper("Phone Number", txtPhone, "Contact number (e.g. 0710000007)"));

        form.add(personalGrid);
        form.add(Box.createVerticalStrut(12));

        // Address Field
        txtAddress = createStyledTextField();
        form.add(createFieldWrapper("Office / Lab Location / Address", txtAddress, "Lab location, office room, or residential address"));
        form.add(Box.createVerticalStrut(10));

        return form;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, false),
                new EmptyBorder(14, 26, 14, 26)
        ));

        JLabel reqNote = new JLabel("* Required fields");
        reqNote.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        reqNote.setForeground(TEXT_MUTED);
        footer.add(reqNote, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btnCancel.setForeground(TEXT_PRIMARY);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 18, 8, 18)
        ));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isEditMode ? "Save Changes" : "Register Officer") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(10, 25, 47)
                        : (getModel().isRollover() ? new Color(180, 83, 9) : PRIMARY_NAVY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSave.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btnSave.setForeground(Color.WHITE);
        btnSave.setContentAreaFilled(false);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(new EmptyBorder(9, 22, 9, 22));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        footer.add(btnPanel, BorderLayout.EAST);

        return footer;
    }

    private JPanel createSectionHeader(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 13));
        lbl.setForeground(PRIMARY_NAVY);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);

        panel.add(lbl, BorderLayout.WEST);
        panel.add(sep, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFieldWrapper(String labelText, JComponent field, String hint) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JTextField) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            field.setPreferredSize(new Dimension(280, 36));
        }

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(field);

        if (hint != null && !hint.isBlank()) {
            JLabel hintLbl = new JLabel(hint);
            hintLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
            hintLbl.setForeground(TEXT_MUTED);
            hintLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(Box.createVerticalStrut(2));
            wrapper.add(hintLbl);
        }

        return wrapper;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        tf.setBackground(Color.WHITE);
        return tf;
    }

    private void styleComboBox(JComboBox<?> cmb) {
        cmb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cmb.setBackground(Color.WHITE);
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmb.setPreferredSize(new Dimension(280, 36));
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePassword.setText("🔒");
        } else {
            txtPassword.setEchoChar('•');
            btnTogglePassword.setText("👁");
        }
    }

    private void populateFields(TechnicalOfficer to) {
        txtOfficerId.setText(to.getTechnicalOfficerId());
        txtStaffNo.setText(to.getStaffNo());
        txtUsername.setText(to.getUsername());
        txtFirstName.setText(to.getFirstName());
        txtLastName.setText(to.getLastName());
        txtEmail.setText(to.getEmail() != null ? to.getEmail() : "");
        txtPhone.setText(to.getPhone() != null ? to.getPhone() : "");
        txtAddress.setText(to.getAddress() != null ? to.getAddress() : "");

        if (to.getDepartment() != null) {
            cmbDepartment.setSelectedItem(to.getDepartment());
        }
        if (to.getAccountStatus() != null) {
            cmbAccountStatus.setSelectedItem(to.getAccountStatus());
        }
    }

    private void handleSave() {
        hideError();

        // Construct or update TechnicalOfficer entity
        TechnicalOfficer to = isEditMode ? existingOfficer : new TechnicalOfficer();

        to.setTechnicalOfficerId(txtOfficerId.getText().trim());
        to.setStaffNo(txtStaffNo.getText().trim());
        to.setUsername(txtUsername.getText().trim());
        to.setFirstName(txtFirstName.getText().trim());
        to.setLastName(txtLastName.getText().trim());
        to.setEmail(txtEmail.getText().trim());
        to.setPhone(txtPhone.getText().trim());
        to.setAddress(txtAddress.getText().trim());

        to.setDepartment((String) cmbDepartment.getSelectedItem());
        to.setAccountStatus((String) cmbAccountStatus.getSelectedItem());

        char[] pwdChars = txtPassword.getPassword();
        String pwd = new String(pwdChars).trim();
        if (!pwd.isEmpty() || !isEditMode) {
            to.setPassword(pwd);
        }

        TechnicalOfficerService service = TechnicalOfficerService.getInstance();
        TechnicalOfficerService.OperationResult result;

        if (isEditMode) {
            result = service.updateTechnicalOfficer(to);
        } else {
            result = service.addTechnicalOfficer(to);
        }

        if (result.isSuccess()) {
            saved = true;
            JOptionPane.showMessageDialog(
                    this,
                    result.getMessage(),
                    "Operation Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

    private void showError(String message) {
        lblErrorBanner.setText("<html>" + message + "</html>");
        lblErrorBanner.setVisible(true);
        lblErrorBanner.revalidate();
        lblErrorBanner.repaint();
    }

    private void hideError() {
        lblErrorBanner.setVisible(false);
        lblErrorBanner.setText("");
    }

    public boolean isSaved() {
        return saved;
    }
}
