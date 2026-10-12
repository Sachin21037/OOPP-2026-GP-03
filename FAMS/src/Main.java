
import ui.LecturerDashboard;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            LecturerDashboard dashboard =
                    new LecturerDashboard();

            dashboard.setVisible(true);
        });

    }
}
