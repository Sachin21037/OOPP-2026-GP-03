import ui.login.LoginFrame;
import ui.lecturer.LecturerDashboardUI;

import javax.swing.*;

/**
 * Main application entry point for the Faculty Academic Management System (FAMS).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            // Launch the Login Frame
            new LoginFrame().setVisible(true);
        });
    }
}

