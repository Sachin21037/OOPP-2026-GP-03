import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestPreviewAttendance {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        File scratchDir = new File("C:\\Users\\user\\.gemini\\antigravity-ide\\brain\\f35321d3-eabf-43b5-94f3-952639287d34\\scratch");
        scratchDir.mkdirs();

        // 1. Preview Tab 1 of AttendanceManagementPage
        JFrame frameAtt = new JFrame();
        view.AttendanceManagementPage attPage = new view.AttendanceManagementPage();
        frameAtt.add(attPage);
        frameAtt.setSize(1120, 700);
        frameAtt.setLocationRelativeTo(null);
        frameAtt.setVisible(true);

        Thread.sleep(400);

        BufferedImage imgTab1 = new BufferedImage(1120, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g1 = imgTab1.createGraphics();
        attPage.paint(g1);
        g1.dispose();

        File out1 = new File(scratchDir, "tab1_session_logging.png");
        ImageIO.write(imgTab1, "png", out1);
        System.out.println("Saved Tab 1 to " + out1.getAbsolutePath());

        // 2. Click Tab 2 (Batch Eligibility & 5 Scenarios)
        findAndClickButton(attPage, "Batch Eligibility & 5 Scenarios");
        Thread.sleep(400);

        BufferedImage imgTab2 = new BufferedImage(1120, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imgTab2.createGraphics();
        attPage.paint(g2);
        g2.dispose();

        File out2 = new File(scratchDir, "tab2_batch_eligibility.png");
        ImageIO.write(imgTab2, "png", out2);
        System.out.println("Saved Tab 2 to " + out2.getAbsolutePath());

        // 3. Click Tab 3 (Student Profile & History)
        findAndClickButton(attPage, "Student Profile & History");
        Thread.sleep(200);
        findAndClickButton(attPage, "Search Student Profile");
        Thread.sleep(400);

        BufferedImage imgTab3 = new BufferedImage(1120, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 = imgTab3.createGraphics();
        attPage.paint(g3);
        g3.dispose();

        File out3 = new File(scratchDir, "tab3_student_history.png");
        ImageIO.write(imgTab3, "png", out3);
        System.out.println("Saved Tab 3 to " + out3.getAbsolutePath());

        frameAtt.dispose();
    }

    private static void findAndClickButton(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) {
                ((JButton) c).doClick();
                return;
            } else if (c instanceof Container) {
                findAndClickButton((Container) c, text);
            }
        }
    }
}
