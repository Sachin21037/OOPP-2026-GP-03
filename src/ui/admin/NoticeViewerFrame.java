package ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Standalone Notice Viewer Window for Non-Admin Roles
 * (Lecturers, Technical Officers, and Undergraduates).
 * Enables staff and students to browse official notices with search,
 * filtering, and clean readability without administrative controls.
 */
public class NoticeViewerFrame extends JFrame {

    private final String role;
    private final String displayName;

    public NoticeViewerFrame(String role, String displayName) {
        this.role = (role != null) ? role.toUpperCase() : "UNDERGRADUATE";
        this.displayName = (displayName != null && !displayName.isBlank()) ? displayName : "User";
        initUI();
    }

    private void initUI() {
        String roleTitle = switch (role) {
            case "LECTURER" -> "Academic Staff";
            case "TECHNICAL_OFFICER" -> "Technical Staff";
            default -> "Student";
        };

        setTitle("FAMS - Faculty Academic Management System | " + roleTitle + " Notice Portal");
        setSize(1100, 700);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(245, 247, 251));

        // Top University Branding Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(16, 40, 74));
        topBar.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel titleLbl = new JLabel("University of Ruhuna • Faculty of Technology | Academic Notices");
        titleLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        topBar.add(titleLbl, BorderLayout.WEST);

        JLabel userLbl = new JLabel("Logged in as: " + displayName + " (" + roleTitle + ")");
        userLbl.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        userLbl.setForeground(new Color(147, 197, 253));
        topBar.add(userLbl, BorderLayout.EAST);

        rootPanel.add(topBar, BorderLayout.NORTH);

        // Center: Notice Panel in non-admin viewer mode
        NoticeManagementPanel noticePanel = new NoticeManagementPanel(role, displayName, null);
        rootPanel.add(noticePanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }
}
