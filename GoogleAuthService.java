package com.library;

import java.io.InputStream;

public class GoogleAuthService {
    public boolean authenticate() {
        try (InputStream in = getClass().getResourceAsStream("/credentials.json")) {
            if (in == null) {
                System.err.println("credentials.json not found in src/main/resources!");
                return false;
            }
            System.out.println("Google Auth client loaded successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}