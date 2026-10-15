import ui.login.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Set modern system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Launch Student Login Window
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
