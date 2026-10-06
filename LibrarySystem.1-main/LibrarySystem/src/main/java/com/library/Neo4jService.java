package com.library;

import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public class Neo4jService {

    // Method para sa User
    public void addOrUpdateUser(String email, String name, String role) {
        try (Session session = Neo4jConfig.getDriver().session()) {
            session.executeWrite(tx -> {
                String query = "MERGE (u:User {email: $email}) " +
                               "SET u.name = $name, u.role = $role " +
                               "RETURN u";
                tx.run(query, Values.parameters("email", email, "name", name, "role", role));
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error adding user to Neo4j: " + e.getMessage());
        }
    }

    // Method para sa Book (Eto ang kailangang tumugma sa BookService: 4 Strings)
    public void addOrUpdateBook(String isbn, String title, String author, String category) {
        try (Session session = Neo4jConfig.getDriver().session()) {
            session.executeWrite(tx -> {
                String query = "MERGE (b:Book {isbn: $isbn}) " +
                               "SET b.title = $title, b.author = $author, b.category = $category " +
                               "RETURN b";
                tx.run(query, Values.parameters(
                    "isbn", isbn,
                    "title", title,
                    "author", author,
                    "category", category
                ));
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error adding book to Neo4j: " + e.getMessage());
        }
    }

    // Method para sa Borrowing
    public void recordBorrowing(String userEmail, String isbn) {
        try (Session session = Neo4jConfig.getDriver().session()) {
            session.executeWrite(tx -> {
                String query = "MATCH (u:User {email: $email}) " +
                               "MATCH (b:Book {isbn: $isbn}) " +
                               "MERGE (u)-[r:BORROWED]->(b) " +
                               "SET r.status = 'BORROWED', r.date = datetime() " +
                               "RETURN r";
                tx.run(query, Values.parameters("email", userEmail, "isbn", isbn));
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error recording borrowing in Neo4j: " + e.getMessage());
        }
    }

    // Method para sa Return
    public void recordReturn(String userEmail, String isbn) {
        try (Session session = Neo4jConfig.getDriver().session()) {
            session.executeWrite(tx -> {
                String query = "MATCH (u:User {email: $email})-[r:BORROWED]->(b:Book {isbn: $isbn}) " +
                               "SET r.status = 'RETURNED' " +
                               "RETURN r";
                tx.run(query, Values.parameters("email", userEmail, "isbn", isbn));
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error recording return in Neo4j: " + e.getMessage());
        }
    }
}