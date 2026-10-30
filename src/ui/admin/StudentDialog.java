package ui.admin;

import model.Student;
import service.StudentService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modal Dialog for Creating and Editing Student records in FAMS.
 * Implements field validation, password show/hide, and consistent Admin Dashboard styling.
 */
public class StudentDialog extends JDialog {

    // Palette harmonized with AdminDashboard & LoginFrame
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color LIGHT_BLUE      = new Color(234, 242, 255); // #EAF2FF
    private static final Color BG_MAIN         = new Color(248, 250, 252); // #F8FAFC
    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_MUTED      = new Color(100, 116, 139); // #64748B
    private static final Color BORDER_COLOR    = new Color(203, 213, 225); // #CBD5E1
    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // #DC2626
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A

    private final boolean isEditMode;
    private final Student existingStudent;
    private boolean saved = false;

    // Form Fields
    private JTextField txtStudentId;
    private JTextField txtIndexNo;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnTogglePassword;
    private boolean isPasswordVisible = false;

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;

    private JComboBox<String> cmbBatch;
    private JComboBox<String> cmbAcademicYear;
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbStudentStatus;
    private JComboBox<String> cmbAccountStatus;

    private JLabel lblErrorBanner;

    public StudentDialog(Window parent, Student studentToEdit) {
        super(parent, studentToEdit == null ? "Add New Undergraduate Student" : "Edit Student Details", ModalityType.APPLICATION_MODAL);
        this.existingStudent = studentToEdit;
        this.isEditMode = (studentToEdit != null);

        initUI();
        if (isEditMode) {
            populateFields(studentToEdit);
        } else {
            // Suggest next available ID
            String nextId = StudentService.getInstance().getNextSuggestedStudentId();
            txtStudentId.setText(nextId);
            txtUsername.setText(nextId.toLowerCase());
        }
    }

    private void initUI() {
        setSize(780, 680);
        setMinimumSize(new Dimension(720, 620));
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

        JLabel titleLbl = new JLabel(isEditMode ? "Edit Undergraduate Student" : "Register New Undergraduate Student");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(isEditMode
                ? "Update academic records, personal information, and portal account settings."
                : "Fill in the required information to create an undergraduate account in the system.");
        subLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        subLbl.setForeground(new Color(224, 234, 254));

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subLbl);

        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFormContent() {
        JPanel content = new JPanel();
        content.setBackground(BG_MAIN);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 26, 16, 26));

        // Error message banner
        lblErrorBanner = new JLabel("");
        lblErrorBanner.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        lblErrorBanner.setForeground(DANGER_COLOR);
        lblErrorBanner.setBackground(new Color(254, 242, 242));
        lblErrorBanner.setOpaque(true);
        lblErrorBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        lblErrorBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblErrorBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblErrorBanner.setVisible(false);

        content.add(lblErrorBanner);
        content.add(Box.createVerticalStrut(10));

        // Section 1: Account Credentials
        content.add(createSectionTitle("1. Account & Identification Credentials"));
        content.add(Box.createVerticalStrut(8));
        content.add(createAccountSection());
        content.add(Box.createVerticalStrut(16));

        // Section 2: Personal Information
        content.add(createSectionTitle("2. Personal Information"));
        content.add(Box.createVerticalStrut(8));
        content.add(createPersonalSection());
        content.add(Box.createVerticalStrut(16));

        // Section 3: Academic Details & Status
        content.add(createSectionTitle("3. Academic Details & Status"));
        content.add(Box.createVerticalStrut(8));
        content.add(createAcademicSection());

        return content;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 13));
        lbl.setForeground(PRIMARY_NAVY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel createAccountSection() {
        JPanel card = createCardPanel();
        card.setLayout(new GridLayout(2, 2, 16, 12));

        // Row 1, Col 1: Student ID
        txtStudentId = createStyledTextField();
        if (isEditMode) {
            txtStudentId.setEditable(false);
            txtStudentId.setBackground(new Color(241, 245, 249));
            txtStudentId.setToolTipText("Student ID is the unique primary key and cannot be modified.");
        }
        card.add(createFieldWrapper("Student ID *", txtStudentId, isEditMode ? "Primary identifier (read-only)" : "e.g. STU021"));

        // Row 1, Col 2: Index Number
        txtIndexNo = createStyledTextField();
        card.add(createFieldWrapper("Index Number *", txtIndexNo, "e.g. TG/2024/2021"));

        // Row 2, Col 1: Username
        txtUsername = createStyledTextField();
        card.add(createFieldWrapper("Username *", txtUsername, "Unique login username (no spaces)"));

        // Row 2, Col 2: Password with eye toggle
        JPanel pwdPanel = new JPanel(new BorderLayout(4, 0));
        pwdPanel.setOpaque(false);
        txtPassword = new JPasswordField();
        txtPassword.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));

        btnTogglePassword = new JButton("👁");
        btnTogglePassword.setPreferredSize(new Dimension(36, 32));
        btnTogglePassword.setFocusPainted(false);
        btnTogglePassword.setBackground(Color.WHITE);
        btnTogglePassword.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        btnTogglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility());

        pwdPanel.add(txtPassword, BorderLayout.CENTER);
        pwdPanel.add(btnTogglePassword, BorderLayout.EAST);

        String pwdDesc = isEditMode ? "Leave blank to retain current password" : "Minimum 4 characters";
        card.add(createFieldWrapper("Password " + (isEditMode ? "(Optional)" : "*"), pwdPanel, pwdDesc));

        return card;
    }

    private JPanel createPersonalSection() {
        JPanel card = createCardPanel();
        card.setLayout(new GridLayout(3, 2, 16, 12));

        // Row 1: First Name & Last Name
        txtFirstName = createStyledTextField();
        card.add(createFieldWrapper("First Name *", txtFirstName, "e.g. Kamal"));

        txtLastName = createStyledTextField();
        card.add(createFieldWrapper("Last Name *", txtLastName, "e.g. Silva"));

        // Row 2: Email & Phone
        txtEmail = createStyledTextField();
        card.add(createFieldWrapper("Email Address", txtEmail, "e.g. kamal.silva@student.uor.lk"));

        txtPhone = createStyledTextField();
        card.add(createFieldWrapper("Contact Phone", txtPhone, "e.g. 0711000021"));

        // Row 3: Address (spans full width logically)
        txtAddress = createStyledTextField();
        card.add(createFieldWrapper("Residential Address", txtAddress, "City or postal address"));

        // Account status
        String[] accountStatuses = {"ACTIVE", "INACTIVE"};
        cmbAccountStatus = new JComboBox<>(accountStatuses);
        styleComboBox(cmbAccountStatus);
        card.add(createFieldWrapper("User Account Status *", cmbAccountStatus, "Access permission status"));

        return card;
    }

    private JPanel createAcademicSection() {
        JPanel card = createCardPanel();
        card.setLayout(new GridLayout(2, 3, 14, 12));

        // Batch
        String[] batches = {"2024/2025", "2023/2024", "2022/2023", "2021/2022", "2020/2021"};
        cmbBatch = new JComboBox<>(batches);
        cmbBatch.setEditable(true);
        styleComboBox(cmbBatch);
        card.add(createFieldWrapper("Batch *", cmbBatch, "Academic intake batch"));

        // Academic Year
        String[] years = {"Level I", "Level II", "Level III", "Level IV"};
        cmbAcademicYear = new JComboBox<>(years);
        cmbAcademicYear.setSelectedItem("Level II");
        styleComboBox(cmbAcademicYear);
        card.add(createFieldWrapper("Academic Year / Level *", cmbAcademicYear, "Current academic level"));

        // Academic Status
        String[] academicStatuses = {"REGULAR", "REPEAT", "BATCH_MISSED"};
        cmbStudentStatus = new JComboBox<>(academicStatuses);
        styleComboBox(cmbStudentStatus);
        card.add(createFieldWrapper("Student Academic Status *", cmbStudentStatus, "Enrollment classification"));

        // Department
        String[] depts = {
                "Information and Communication Technology",
                "Engineering Technology",
                "Biosystems Technology",
                "Multidisciplinary Studies"
        };
        cmbDepartment = new JComboBox<>(depts);
        cmbDepartment.setEditable(true);
        styleComboBox(cmbDepartment);
        // Span 3 columns for department
        JPanel deptWrapper = createFieldWrapper("Department *", cmbDepartment, "Faculty department");

        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        wrapper.add(deptWrapper, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        return card;
    }

    private JPanel createFieldWrapper(String labelText, JComponent input, String helpText) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel help = new JLabel(helpText);
        help.setFont(AdminDashboard.getModernFont(Font.PLAIN, 10));
        help.setForeground(TEXT_MUTED);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(input);
        wrapper.add(Box.createVerticalStrut(2));
        wrapper.add(help);

        return wrapper;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        field.setPreferredSize(new Dimension(200, 34));
        return field;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(200, 34));
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btnCancel.setForeground(TEXT_PRIMARY);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setPreferredSize(new Dimension(95, 36));
        btnCancel.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isEditMode ? "Save Changes" : "Create Student");
        btnSave.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(SECONDARY_BLUE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(135, 36));
        btnSave.setBorder(new LineBorder(SECONDARY_BLUE, 1, true));

        btnSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSave.setBackground(PRIMARY_NAVY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSave.setBackground(SECONDARY_BLUE);
            }
        });

        btnSave.addActionListener(e -> handleSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
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

    private void populateFields(Student s) {
        txtStudentId.setText(s.getStudentId());
        txtIndexNo.setText(s.getIndexNo());
        txtUsername.setText(s.getUsername());
        txtFirstName.setText(s.getFirstName());
        txtLastName.setText(s.getLastName());
        txtEmail.setText(s.getEmail() != null ? s.getEmail() : "");
        txtPhone.setText(s.getPhone() != null ? s.getPhone() : "");
        txtAddress.setText(s.getAddress() != null ? s.getAddress() : "");

        if (s.getBatch() != null) cmbBatch.setSelectedItem(s.getBatch());
        if (s.getAcademicYear() != null) cmbAcademicYear.setSelectedItem(s.getAcademicYear());
        if (s.getDepartment() != null) cmbDepartment.setSelectedItem(s.getDepartment());
        if (s.getStudentStatus() != null) cmbStudentStatus.setSelectedItem(s.getStudentStatus());
        if (s.getAccountStatus() != null) cmbAccountStatus.setSelectedItem(s.getAccountStatus());
    }

    private void handleSave() {
        lblErrorBanner.setVisible(false);

        Student s = isEditMode ? existingStudent : new Student();
        s.setStudentId(txtStudentId.getText().trim());
        s.setIndexNo(txtIndexNo.getText().trim());
        s.setUsername(txtUsername.getText().trim());

        String pwd = new String(txtPassword.getPassword()).trim();
        if (!pwd.isEmpty() || !isEditMode) {
            s.setPassword(pwd);
        }

        s.setFirstName(txtFirstName.getText().trim());
        s.setLastName(txtLastName.getText().trim());
        s.setEmail(txtEmail.getText().trim());
        s.setPhone(txtPhone.getText().trim());
        s.setAddress(txtAddress.getText().trim());

        Object bObj = cmbBatch.getSelectedItem();
        s.setBatch(bObj != null ? bObj.toString().trim() : "");

        Object yObj = cmbAcademicYear.getSelectedItem();
        s.setAcademicYear(yObj != null ? yObj.toString().trim() : "");

        Object dObj = cmbDepartment.getSelectedItem();
        s.setDepartment(dObj != null ? dObj.toString().trim() : "");

        Object ssObj = cmbStudentStatus.getSelectedItem();
        s.setStudentStatus(ssObj != null ? ssObj.toString().trim() : "REGULAR");

        Object asObj = cmbAccountStatus.getSelectedItem();
        s.setAccountStatus(asObj != null ? asObj.toString().trim() : "ACTIVE");

        StudentService service = StudentService.getInstance();
        StudentService.OperationResult result = isEditMode
                ? service.updateStudent(s)
                : service.addStudent(s);

        if (result.isSuccess()) {
            this.saved = true;
            JOptionPane.showMessageDialog(
                    this,
                    result.getMessage(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            lblErrorBanner.setText("<html>" + result.getMessage() + "</html>");
            lblErrorBanner.setVisible(true);
            lblErrorBanner.revalidate();
            lblErrorBanner.repaint();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
