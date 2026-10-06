package com.library;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class Neo4jConfig {
    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "password123"; // Replace with your Neo4j database password

    private static Driver driver;

    public static Driver getDriver() {
        if (driver == null) {
            driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
        }
        return driver;
    }

    public static Session getSession() {
        return getDriver().session();
    }

    public static void closeConnection() {
        if (driver != null) {
            driver.close();
            driver = null;
        }
    }
}