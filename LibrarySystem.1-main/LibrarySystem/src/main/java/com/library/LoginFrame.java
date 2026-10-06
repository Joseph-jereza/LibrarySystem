package com.library;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    
    private JTextField registerNameField;
    private JTextField registerEmailField;
    private JPasswordField registerPasswordField;
    private JComboBox<String> roleComboBox;

    private JTabbedPane tabbedPane;
    
    // Database services & collections
    private MongoCollection<Document> users;
    private Neo4jService neo4jService;

    public LoginFrame() {
        // Initialize Mongo collection and Neo4j Service
        MongoDatabase db = MongoConfig.getDatabase();
        users = db.getCollection("users");
        neo4jService = new Neo4jService();

        setTitle("Library Management System - Login");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Build Tabs
        tabbedPane.addTab("Login", createLoginPanel());
        tabbedPane.addTab("Register", createRegisterPanel());

        add(tabbedPane);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Email:"));
        loginEmailField = new JTextField();
        panel.add(loginEmailField);

        panel.add(new JLabel("Password:"));
        loginPasswordField = new JPasswordField();
        panel.add(loginPasswordField);

        JButton loginBtn = new JButton("Login");
        panel.add(new JLabel()); // Spacer
        panel.add(loginBtn);

        // Login Action Handler
        loginBtn.addActionListener(e -> {
            String email = loginEmailField.getText().trim();
            String password = new String(loginPasswordField.getPassword()).trim();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both email and password.");
                return;
            }

            try {
                Document user = users.find(Filters.and(
                    Filters.eq("email", email),
                    Filters.eq("password", password)
                )).first();

                if (user != null) {
                    String name = user.getString("name");
                    String role = user.getString("role");

                    // Sync user with Neo4j on login
                    neo4jService.addOrUpdateUser(email, name, role);

                    JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + name);
                    
                    // Launch Dashboard with 3 parameters (email, name, role)
                    new DashboardFrame(email, name, role).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid email or password.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Full Name:"));
        registerNameField = new JTextField();
        panel.add(registerNameField);

        panel.add(new JLabel("Email:"));
        registerEmailField = new JTextField();
        panel.add(registerEmailField);

        panel.add(new JLabel("Password:"));
        registerPasswordField = new JPasswordField();
        panel.add(registerPasswordField);

        panel.add(new JLabel("Role:"));
        roleComboBox = new JComboBox<>(new String[]{"Student", "Admin"});
        panel.add(roleComboBox);

        JButton registerBtn = new JButton("Register");
        panel.add(new JLabel()); // Spacer
        panel.add(registerBtn);

        // Register Action Handler
        registerBtn.addActionListener(e -> {
            String name = registerNameField.getText().trim();
            String email = registerEmailField.getText().trim();
            String password = new String(registerPasswordField.getPassword()).trim();
            String role = (String) roleComboBox.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            try {
                // Check if user already exists in MongoDB
                if (users.find(Filters.eq("email", email)).first() != null) {
                    JOptionPane.showMessageDialog(this, "User with this email already exists.");
                    return;
                }

                // Insert into MongoDB
                Document newUser = new Document("name", name)
                        .append("email", email)
                        .append("password", password)
                        .append("role", role);

                users.insertOne(newUser);

                // Sync User Node to Neo4j
                neo4jService.addOrUpdateUser(email, name, role);

                JOptionPane.showMessageDialog(this, "Account created successfully!");

                // Clear fields and switch to Login tab
                registerNameField.setText("");
                registerEmailField.setText("");
                registerPasswordField.setText("");
                tabbedPane.setSelectedIndex(0);
                loginEmailField.setText(email);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        });

        return panel;
    }
}