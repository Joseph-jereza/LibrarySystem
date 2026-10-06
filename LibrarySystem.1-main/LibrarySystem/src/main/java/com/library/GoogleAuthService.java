package com.library;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

public class GoogleAuthService {

    private static final String CLIENT_SECRET_FILE = "/credentials.json";
    private static final java.util.List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/userinfo.email");

    public boolean authenticateWithGoogle() {
        try {
            InputStream in = GoogleAuthService.class.getResourceAsStream(CLIENT_SECRET_FILE);
            if (in == null) {
                System.err.println("Hindi nahanap ang credentials.json sa src/main/resources!");
                return false;
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(), new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    SCOPES)
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}