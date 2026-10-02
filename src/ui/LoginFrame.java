package ui;

import javax.swing.*;

public class LoginFrame extends JFrame {

    JLabel usernameLabel;
    JLabel passwordLabel;

    JTextField usernameField;
    JPasswordField passwordField;

    JButton loginButton;

    public LoginFrame() {

        setTitle("Login");
        setSize(400, 300);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Username
        usernameLabel = new JLabel("Username");
        usernameLabel.setBounds(50, 70, 100, 30);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 70, 180, 30);
        add(usernameField);

        // Password
        passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(50, 120, 100, 30);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 120, 180, 30);
        add(passwordField);

        // Login button
        loginButton = new JButton("Login");
        loginButton.setBounds(150, 180, 100, 35);
        add(loginButton);
    }

    public static void main(String[] args) {

        LoginFrame frame = new LoginFrame();

        frame.setVisible(true);
    }
}
