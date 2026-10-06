package com.library;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // Register shutdown hooks for both databases
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MongoConfig.closeConnection();
            Neo4jConfig.closeConnection();
        }));

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}