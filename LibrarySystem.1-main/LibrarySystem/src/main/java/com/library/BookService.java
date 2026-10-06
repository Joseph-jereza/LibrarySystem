package com.library;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    private final MongoCollection<Document> booksCollection;
    private final Neo4jService neo4jService;

    public BookService() {
        MongoDatabase database = MongoConfig.getDatabase();
        this.booksCollection = database.getCollection("books");
        this.neo4jService = new Neo4jService();
    }

    public void addBook(String title, String author, String isbn, String category, int totalCopies) {
        Document book = new Document("title", title)
                .append("author", author)
                .append("isbn", isbn)
                .append("category", category)
                .append("totalCopies", totalCopies)
                .append("availableCopies", totalCopies);

        booksCollection.insertOne(book);

        // Sinisiguradong tumutugma sa Neo4jService method signature
        try {
            neo4jService.addOrUpdateBook(isbn, title, author, category);
        } catch (Exception e) {
            System.err.println("Error syncing with Neo4j: " + e.getMessage());
        }
    }

    public Document findBookByIsbn(String isbn) {
        return booksCollection.find(new Document("isbn", isbn)).first();
    }

    // Method para makuha ang lahat ng libro para sa View All Books Table
    public List<Document> getAllBooks() {
        List<Document> books = new ArrayList<>();
        FindIterable<Document> docs = booksCollection.find();
        for (Document doc : docs) {
            books.add(doc);
        }
        return books;
    }
}