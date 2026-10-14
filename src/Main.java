import ui.technicalofficer.TechnicalOfficerDashboard;

import javax.swing.*;

/**
 * Main Application Launcher
 * Launches the Technical Officer Dashboard (Technical Officer subsystem)
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new TechnicalOfficerDashboard());
    }
}
