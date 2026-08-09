package com.library;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final GoogleAuthService authService = new GoogleAuthService();

    public LoginFrame() {
        setTitle("Library Management System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        JButton loginBtn = new JButton("Login with Google");

        loginBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            new DashboardFrame().setVisible(true);
            dispose();
        });

        panel.add(loginBtn);
        add(panel);
    }
}