package com.library;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class LibraryService {
    private final MongoDatabase mongoDatabase;
    private final Driver neo4jDriver;

    public LibraryService() {
        this.mongoDatabase = MongoConfig.getDatabase();
        this.neo4jDriver = Neo4jConfig.getDriver();
    }

    // --- GET ALL USERS ---
    public List<Document> getAllUsers() {
        List<Document> users = new ArrayList<>();
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("users");
            collection.find().into(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    // --- DELETE USER (MONGODB & NEO4J) ---
    public boolean deleteUser(String email) {
        try {
            // Delete sa MongoDB
            long deletedCount = mongoDatabase.getCollection("users")
                    .deleteOne(new Document("email", email))
                    .getDeletedCount();

            // Delete sa Neo4j
            try (Session session = neo4jDriver.session()) {
                session.run("MATCH (u:User) WHERE u.email = $email DETACH DELETE u", 
                        Values.parameters("email", email));
            } catch (Exception e) {
                System.out.println("Neo4j delete user warning: " + e.getMessage());
            }

            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- DELETE BOOK (MONGODB & NEO4J) ---
    public boolean deleteBook(String isbn) {
        try {
            // Delete sa MongoDB
            long deletedCount = mongoDatabase.getCollection("books")
                    .deleteOne(new Document("isbn", isbn))
                    .getDeletedCount();

            // Delete sa Neo4j (kasama ang mga nakakabit na relasyon)
            try (Session session = neo4jDriver.session()) {
                session.run("MATCH (b:Book) WHERE b.isbn = $isbn DETACH DELETE b", 
                        Values.parameters("isbn", isbn));
            } catch (Exception e) {
                System.out.println("Neo4j delete book warning: " + e.getMessage());
            }

            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- BORROW BOOK ---
    public boolean borrowBook(String userEmail, String isbn) {
        try {
            MongoCollection<Document> booksCollection = mongoDatabase.getCollection("books");
            Document book = booksCollection.find(new Document("isbn", isbn)).first();

            if (book == null) {
                return false;
            }

            int availableCopies = book.getInteger("availableCopies", 0);
            if (availableCopies <= 0) {
                return false;
            }

            booksCollection.updateOne(
                    new Document("isbn", isbn),
                    new Document("$set", new Document("availableCopies", availableCopies - 1))
            );

            MongoCollection<Document> borrowCollection = mongoDatabase.getCollection("borrow_records");
            Document record = new Document()
                    .append("userEmail", userEmail)
                    .append("isbn", isbn)
                    .append("borrowDate", new Date())
                    .append("status", "BORROWED");
            borrowCollection.insertOne(record);

            try (Session session = neo4jDriver.session()) {
                session.run(
                        "MATCH (u:User), (b:Book) " +
                        "WHERE u.email = $email AND b.isbn = $isbn " +
                        "CREATE (u)-[:BORROWED {date: date()}]->(b)",
                        Values.parameters("email", userEmail, "isbn", isbn)
                );
            } catch (Exception e) {
                System.out.println("Neo4j borrow warning: " + e.getMessage());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- RETURN BOOK ---
    public boolean returnBook(String userEmail, String isbn) {
        try {
            MongoCollection<Document> borrowCollection = mongoDatabase.getCollection("borrow_records");
            Document record = borrowCollection.find(new Document("userEmail", userEmail)
                    .append("isbn", isbn)
                    .append("status", "BORROWED")).first();

            if (record == null) {
                return false;
            }

            borrowCollection.updateOne(
                    new Document("_id", record.get("_id")),
                    new Document("$set", new Document("status", "RETURNED")
                            .append("returnDate", new Date()))
            );

            MongoCollection<Document> booksCollection = mongoDatabase.getCollection("books");
            Document book = booksCollection.find(new Document("isbn", isbn)).first();
            if (book != null) {
                int availableCopies = book.getInteger("availableCopies", 0);
                booksCollection.updateOne(
                        new Document("isbn", isbn),
                        new Document("$set", new Document("availableCopies", availableCopies + 1))
                );
            }

            try (Session session = neo4jDriver.session()) {
                String cypherQuery = "MATCH (u:User)-[r:BORROWED]->(b:Book) " +
                                     "WHERE u.email = $email AND b.isbn = $isbn " +
                                     "DELETE r";
                
                session.run(cypherQuery, Values.parameters("email", userEmail, "isbn", isbn));
                System.out.println("Neo4j: BORROWED relationship deleted successfully.");
            } catch (Exception e) {
                System.out.println("Neo4j return error: " + e.getMessage());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- GET BORROW RECORDS ---
    public List<Document> getAllBorrowRecords() {
        List<Document> records = new ArrayList<>();
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("borrow_records");
            collection.find().into(records);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }
}