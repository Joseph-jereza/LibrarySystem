package com.library;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Register shutdown hook to close MongoDB connection properly
        Runtime.getRuntime().addShutdownHook(new Thread(MongoConfig::closeConnection));

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}