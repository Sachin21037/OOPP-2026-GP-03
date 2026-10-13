import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestPreviewBoth {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Preview AttendanceManagementPage
        JFrame frameAtt = new JFrame();
        AttendanceManagementPage attPage = new AttendanceManagementPage();
        frameAtt.getContentPane().add(attPage);
        frameAtt.addNotify();
        frameAtt.setSize(1100, 680);
        frameAtt.validate();

        BufferedImage attImage = new BufferedImage(1100, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D gAtt = attImage.createGraphics();
        attPage.printAll(gAtt);
        gAtt.dispose();

        File attOut = new File("C:\\Users\\user\\.gemini\\antigravity-ide\\brain\\1db8a934-432f-4dbd-b75a-5df7d689a70d\\scratch\\attendance_preview.png");
        attOut.getParentFile().mkdirs();
        ImageIO.write(attImage, "png", attOut);
        System.out.println("Saved attendance preview to: " + attOut.getAbsolutePath());

        // Preview MedicalManagementPage
        JFrame frameMed = new JFrame();
        MedicalManagementPage medPage = new MedicalManagementPage();
        frameMed.getContentPane().add(medPage);
        frameMed.addNotify();
        frameMed.setSize(1100, 680);
        frameMed.validate();

        BufferedImage medImage = new BufferedImage(1100, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D gMed = medImage.createGraphics();
        medPage.printAll(gMed);
        gMed.dispose();

        File medOut = new File("C:\\Users\\user\\.gemini\\antigravity-ide\\brain\\1db8a934-432f-4dbd-b75a-5df7d689a70d\\scratch\\medical_preview.png");
        ImageIO.write(medImage, "png", medOut);
        System.out.println("Saved medical preview to: " + medOut.getAbsolutePath());

        // Preview TechnicalOfficerDashboard
        TechnicalOfficerDashboard toDash = new TechnicalOfficerDashboard();
        toDash.setSize(1180, 740);
        toDash.validate();

        BufferedImage dashImage = new BufferedImage(1180, 740, BufferedImage.TYPE_INT_RGB);
        Graphics2D gDash = dashImage.createGraphics();
        toDash.printAll(gDash);
        gDash.dispose();

        File dashOut = new File("C:\\Users\\user\\.gemini\\antigravity-ide\\brain\\1db8a934-432f-4dbd-b75a-5df7d689a70d\\scratch\\dashboard_preview.png");
        ImageIO.write(dashImage, "png", dashOut);
        System.out.println("Saved dashboard preview to: " + dashOut.getAbsolutePath());

        toDash.dispose();
        frameAtt.dispose();
        frameMed.dispose();
        System.exit(0);
    }
}
